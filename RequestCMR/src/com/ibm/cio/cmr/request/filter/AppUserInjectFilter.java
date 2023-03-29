package com.ibm.cio.cmr.request.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.login.LogInUserModel;
import com.ibm.cio.cmr.request.service.user.UserService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.oauth.OAuthUtils;
import com.ibm.cio.cmr.request.util.oauth.SimpleJWT;
import com.ibm.cio.cmr.request.util.oauth.UserHelper;
import com.ibm.json.java.JSONObject;

/**
 * This filter ensures valid user is present at the request. If not, the request
 * redirects user to SSO. Retrieves and validates the JWT using JWK presented at
 * jwks endpoint.
 * 
 * @author Hesller Huller
 *
 */
@Component
@WebFilter(filterName = "AppUserInjectFilter", urlPatterns = "/*")
public class AppUserInjectFilter implements Filter {

	@Autowired
	UserService userService;

	protected static final Logger LOG = Logger.getLogger(AppUserInjectFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		// can be used to deactivate this filter completely
		String activateFilter = SystemConfiguration.getValue("ACTIVATE_SSO");
		if (activateFilter.equalsIgnoreCase("false")) {
			filterChain.doFilter(request, response);
			return;
		}

		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpResp = (HttpServletResponse) response;

		String url = httpReq.getRequestURI();

		HttpSession session = shouldCreateSession(httpReq);
		String userIntranetEmail = (String) session.getAttribute("userIntranetEmail");

		try {
			if (shouldFilter(httpReq, (HttpServletResponse) response)) {

				AppUser user = AppUser.getUser(httpReq);

				LOG.debug(url);
				LOG.debug(user);
				LOG.debug("session id: " + session.getId());

				if (user == null) {
					LOG.warn("No user on the session yet...");
					UserHelper userHelper = new UserHelper();

					// connect to W3 to build user profile
					String ibmUniqueId = userHelper.getUNID();

					LOG.debug("Subject is set: " + ibmUniqueId);

					if (ibmUniqueId == null || ibmUniqueId.trim().isEmpty()) {
						LOG.debug("No IBM ID detected. Redirecting to W3 ID intercept..");
						httpReq.getSession().invalidate();
						httpResp.sendRedirect("/oidcclient/redirect/client01");
						return;
					} else {
						LOG.debug("IBM ID found. Injecting App User");
						LOG.trace("User:" + userHelper.getDisplayName() + "'s ibmUniqueId is " + userHelper.getUNID());
						LOG.trace("User:" + userHelper.getDisplayName() + "'s ibmFullName is "
								+ userHelper.getPrincipalName());
					}

					LOG.debug("Redirecting to W3 ID Provisioner...");

					session.setAttribute("userHelper", userHelper);
					setSessionAttributes(httpReq, httpResp);
					// filterChain.doFilter(httpReq, response);
					httpResp.sendRedirect("/oidcclient/redirect/client01");
					return;
				}
			}
		} catch (Exception e) {
			LOG.error("Error processing AppUserInjectFilter", e);
		}

		filterChain.doFilter(httpReq, response);

	}

	@Override
	public void destroy() {
		// NOOP
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
		LOG.info("CreateCMR " + config.getFilterName() + " initialized.");
	}

	private void setSessionAttributes(HttpServletRequest request, HttpServletResponse response)
			throws CmrException, Exception {
		HttpSession session = request.getSession();

		UserHelper userHelper = (UserHelper) session.getAttribute("userHelper");
		SimpleJWT idToken = userHelper.getIDToken();
		String accessToken = userHelper.getAccessToken();
		Long expiresIn = Long.parseLong(userHelper.getPrivateHashtableAttr("expires_in"));
		JSONObject claims = idToken.getClaims();

		String userIntranetEmail = ((String) claims.get("emailAddress")).toLowerCase();
		session.setAttribute("userIntranetEmail", userIntranetEmail);

		LogInUserModel loginUser = new LogInUserModel();
		loginUser.setUsername(userIntranetEmail);

		session.setAttribute("loggedInUserModel", loginUser);
		session.setAttribute("accessToken", accessToken);
		session.setAttribute("tokenExpiringTime", LocalDateTime.now().plus(expiresIn, ChronoUnit.SECONDS));

		session.setAttribute("loginUser", loginUser);

		OAuthUtils oAuthUtils = new OAuthUtils();
		oAuthUtils.authorizeAndSetRoles(loginUser, userService, request, response);

	}

	private boolean shouldFilter(HttpServletRequest request, HttpServletResponse response) {
		String url = request.getRequestURI();
		int status = response.getStatus();
		if (url.endsWith("/update")) {
			return false;
		}
		if (url.endsWith("/logout")) {
			return false;
		}
		if (url.endsWith("/external.json")) {
			return false;
		}

		if (request.getParameterMap().keySet().contains("errorMessage") && url.endsWith("/login")) {

			return false;
		}

		if (url.contains("/") && url.substring(url.lastIndexOf("/")).contains(".")) {
			// static resources
			return false;
		}

		// if (response.getStatus() == 302 && url.contains("/home")) {
		// request.getSession(true);
		// return false;
		// }
		// if (url.contains("/") &&
		// url.substring(url.lastIndexOf("/")).contains(".")) {
		// return false;
		// }

		return true;
	}

	private HttpSession shouldCreateSession(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return req.getSession();
		}
		return session;
	}

	public String getBody(HttpServletRequest request) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

}
