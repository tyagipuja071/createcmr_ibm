package com.ibm.cio.cmr.request.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

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

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.oauth.OAuthUtils;

/**
 * This filter ensures valid user is present at the request. If not, the request
 * redirects user to SSO. Retrieves and validates the JWT using JWK presented at
 * jwks endpoint.
 * 
 * @author Hesller Huller
 *
 */
@WebFilter(filterName = "AppUserInjectFilter", urlPatterns = "/*")
public class AppUserInjectFilter implements Filter {

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

		HttpServletRequest req = (HttpServletRequest) request;
		String url = req.getRequestURI();

		HttpSession session = shouldCreateSession(req);
		String userIntranetEmail = (String) session.getAttribute("userIntranetEmail");

		try {
			if (shouldFilter(req, (HttpServletResponse) response)) {

				AppUser user = AppUser.getUser(req);

				LOG.debug(url);
				LOG.debug(user);
				LOG.debug("session id: " + session.getId());

				if (user == null) {
					LOG.warn("No user on the session yet...");

					// get all request params
					Set<String> paramNames = request.getParameterMap().keySet();

					if (paramNames.contains("code") && paramNames.contains("grant_id")) {
						req.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, new AppUser());
						// req.getRequestDispatcher("/oidcclient/redirect/createcmr").forward(req,
						// response);
						filterChain.doFilter(req, response);
						return;
					}

					LOG.debug("Redirecting to W3 ID Provisioner...");
					HttpServletResponse httpResp = (HttpServletResponse) response;
					session.invalidate();
					httpResp.sendRedirect(OAuthUtils.getAuthorizationCodeURL());
					return;
				}
			}
		} catch (Exception e) {
			LOG.error("Error processing AppUserInjectFilter", e);
		}

		filterChain.doFilter(req, response);

	}

	@Override
	public void destroy() {
		// NOOP
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		LOG.info("CreateCMR " + config.getFilterName() + " initialized.");
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
