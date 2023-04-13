package com.ibm.cio.cmr.request.util.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.UserPref;
import com.ibm.cio.cmr.request.entity.UserPrefPK;
import com.ibm.cio.cmr.request.model.login.LogInUserModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.user.UserService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cmr.services.client.AuthorizationClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.auth.Authorization;
import com.ibm.cmr.services.client.auth.AuthorizationRequest;
import com.ibm.cmr.services.client.auth.AuthorizationRequest.ApplicationCode;
import com.ibm.cmr.services.client.auth.AuthorizationResponse;
import com.ibm.cmr.services.client.auth.Role;

/*
 * Utility class to interact with Authorization Server.
 * 
 * @author Hesller Huller
 * 
 * */
public class OAuthUtils {
	public static final String AUTHORIZE_ENDPOINT = "/authorize";
	public static final String TOKEN_ENDPOINT = "/token";
	public static final String USERINFO_ENDPOINT = "/userinfo";
	public static final String JWKS_ENDPOINT = "/jwks";
	public static final String REVOKE_ENDPOINT = "/revoke";
	public static final String INTROSPECT_ENDPOINT = "/introspect";
	public static final String ISSUER_URL = SystemConfiguration.getValue("SSO_ISSUER_URL");

	private static final byte JWT_PART_SEPARATOR = (byte) 46;
	private static final String ALGORITHM = "SHA256withRSA";

	private static final Logger LOG = Logger.getLogger(OAuthUtils.class);

	public static Tokens tokens = new Tokens();
	public static String jwt = null;

	/**
	 * This builds the URL with the parameters to obtain the Authorization Code
	 * 
	 * @return
	 */
	public static String getAuthorizationCodeURL() {
		// TODO discuss the possibility of changing SSO_ISSUER_URL and
		// SSO_REDIRECT_URL and W3_CLIENT_ID to SystemParameters
		// set scopes and response type
		String scopes = "openid%20profile%20email";
		String responseType = "code";

		// build url
		String authorizationCodeUrl = ISSUER_URL + AUTHORIZE_ENDPOINT;

		// add scopes url encoded
		authorizationCodeUrl += "?scope=" + scopes;

		// add response type
		authorizationCodeUrl += "&response_type=" + responseType;

		// add client ID
		authorizationCodeUrl += "&client_id=" + SystemConfiguration.getValue("W3_CLIENT_ID");

		// add redirect url
		authorizationCodeUrl += "&redirect_uri=" + SystemConfiguration.getValue("SSO_REDIRECT_URL");

		return authorizationCodeUrl;
	}

	/**
	 * Use this method to obtain the access token
	 * 
	 * @param code
	 * @return
	 * @throws IOException
	 */
	public static String getAccessToken(String code) throws IOException {
		OAuthServices service = new OAuthServices(ISSUER_URL + TOKEN_ENDPOINT);

		jwt = service.getAccessToken(ISSUER_URL + TOKEN_ENDPOINT, code);

		LOG.debug("Token generated for code " + code);
		LOG.debug("Token: " + jwt);
		return jwt;
	}

	/**
	 * This method gets the String response from Authorization Server and stores
	 * the access_token and id_token into the Tokens object
	 * 
	 * @param token
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static Tokens parseToken(String token) throws JsonParseException, JsonMappingException, IOException {
		@SuppressWarnings("unchecked")
		HashMap<String, Object> tokenMap = new ObjectMapper().readValue(token, HashMap.class);

		tokens.setExpires_in((Integer) tokenMap.get("expires_in"));
		tokens.setAccess_token((String) tokenMap.get("access_token"));
		tokens.setGrant_id((String) tokenMap.get("grant_id"));
		tokens.setId_token((String) tokenMap.get("id_token"));
		tokens.setJwtToken((String) tokenMap.get("id_token"));
		tokens.setScopes((String) tokenMap.get("scope"));
		tokens.setTokenType((String) tokenMap.get("token_type"));

		return tokens;
	}

	/**
	 * This method validates the signature of the JWT received from AS.
	 * 
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static boolean validateSignature() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		// connect to jwks endpoint and get the Json Web Keys
		List<Map<String, String>> keys = getKeysFromAS();

		// generate public key using JWT Header's kid
		String kid = (String) tokens.getId_token()._header.get("kid");
		PublicKey publicKey = null;

		for (Map<String, String> key : keys) {
			if (key.get("kid").equalsIgnoreCase(kid)) {

				publicKey = getPublicKey(key.get("n").toString(), key.get("e").toString());

				break;
			}
		}

		boolean isValid = false;

		try {

			String jwtToken = tokens.getEncodedJwtToken();

			String header = jwtToken.substring(0, jwtToken.indexOf("."));
			String payload = jwtToken.substring(jwtToken.indexOf(".") + 1, jwtToken.lastIndexOf("."));
			String tokenSignature = jwtToken.substring(jwtToken.lastIndexOf(".") + 1);
			byte[] tokenSignatureDecoded = java.util.Base64.getUrlDecoder().decode(tokenSignature);

			isValid = verifySignatureFor(ALGORITHM, publicKey, header.getBytes(), payload.getBytes(),
					tokenSignatureDecoded);

			return isValid;

		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(e.getMessage());
			return false;
		}
	}

	/**
	 * This method validates the signature of the JWT received from AS.
	 * 
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static boolean validateSignature(SimpleJWT jwt)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		// connect to jwks endpoint and get the Json Web Keys
		List<Map<String, String>> keys = getKeysFromAS();

		// generate public key using JWT Header's kid
		String kid = (String) jwt._header.get("kid");
		PublicKey publicKey = null;

		for (Map<String, String> key : keys) {
			if (key.get("kid").equalsIgnoreCase(kid)) {

				publicKey = getPublicKey(key.get("n").toString(), key.get("e").toString());

				break;
			}
		}

		boolean isValid = false;

		try {

			String jwtToken = tokens.getEncodedJwtToken();

			String header = jwtToken.substring(0, jwtToken.indexOf("."));
			String payload = jwtToken.substring(jwtToken.indexOf(".") + 1, jwtToken.lastIndexOf("."));
			String tokenSignature = jwtToken.substring(jwtToken.lastIndexOf(".") + 1);
			byte[] tokenSignatureDecoded = java.util.Base64.getUrlDecoder().decode(tokenSignature);

			isValid = verifySignatureFor(ALGORITHM, publicKey, header.getBytes(), payload.getBytes(),
					tokenSignatureDecoded);

			return isValid;

		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Verify signature for JWT header and payload using a public key.
	 *
	 * @param algorithm
	 *            algorithm name.
	 * @param publicKey
	 *            the public key to use for verification.
	 * @param headerBytes
	 *            JWT header.
	 * @param payloadBytes
	 *            JWT payload.
	 * @param signatureBytes
	 *            JWT signature.
	 * @return true if signature is valid.
	 * @throws NoSuchAlgorithmException
	 *             if the algorithm is not supported.
	 * @throws InvalidKeyException
	 *             if the given key is inappropriate for initializing the
	 *             specified algorithm.
	 */
	public static boolean verifySignatureFor(String algorithm, PublicKey publicKey, byte[] headerBytes,
			byte[] payloadBytes, byte[] signatureBytes)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		final Signature s = Signature.getInstance(algorithm);
		s.initVerify(publicKey);
		s.update(headerBytes);
		s.update(JWT_PART_SEPARATOR);
		s.update(payloadBytes);
		return s.verify(signatureBytes);
	}

	/**
	 * Get the keys from Authorization Server's JWKS endpoint
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> getKeysFromAS() {
		StringBuffer responseContent = new StringBuffer();
		List<Map<String, String>> keys = null;

		try {
			// prepare the request
			String endPoint = SystemConfiguration.getValue("SSO_ISSUER_URL") + JWKS_ENDPOINT;
			URL url = new URL(endPoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setConnectTimeout(5000);

			// execute the request
			LOG.trace("Connecting to JWKS endpoint to obtain AS Public key information: " + endPoint);
			int statusCode = conn.getResponseCode();

			if (statusCode == 200) {
				// request executed successfully
				// reading request response
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				while (((inputLine = in.readLine()) != null)) {
					responseContent.append(inputLine);
				}

				// closing reader
				in.close();

			} else if (statusCode > 299) {
				throw new CmrException(MessageUtil.ERROR_GET_PUBLIC_KEY);

			}

			// closing connection
			conn.disconnect();

			// parse the keys
			keys = (List<Map<String, String>>) new ObjectMapper().readValue(responseContent.toString(), HashMap.class)
					.get("keys");

			return keys;

		} catch (IOException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(e.getMessage());
		}

		return keys;
	}

	/**
	 * Construct the Public Key using the modulus and exponent
	 * 
	 * @param modulus
	 * @param exponent
	 * @return
	 */
	private static PublicKey getPublicKey(String modulus, String exponent) {
		try {
			byte exponentB[] = org.apache.commons.codec.binary.Base64.decodeBase64(exponent);
			byte modulusB[] = org.apache.commons.codec.binary.Base64.decodeBase64(modulus);
			BigInteger bigExponent = new BigInteger(1, exponentB);
			BigInteger bigModulus = new BigInteger(1, modulusB);

			PublicKey publicKey;

			publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(bigModulus, bigExponent));

			return publicKey;

		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Revoke the access token make it inactive
	 * 
	 * @param access_token
	 * @return
	 */
	public static boolean revokeToken(String access_token) {
		if (StringUtils.isNotBlank(access_token)) {
			try {

				Map<String, String> headers = new HashMap<String, String>();

				HttpPostForm httpPostForm = new HttpPostForm(
						SystemConfiguration.getValue("SSO_ISSUER_URL") + REVOKE_ENDPOINT, "utf-8", headers);

				// httpPostForm.addFormField("grant_type",
				// "authorization_code");
				httpPostForm.addFormField("token", access_token);
				httpPostForm.addFormField("client_id", SystemConfiguration.getValue("W3_CLIENT_ID"));
				httpPostForm.addFormField("client_secret", SystemConfiguration.getValue("W3_CLIENT_SECRET"));

				String response = httpPostForm.finish();

				LOG.debug(response);
				LOG.debug("Access Token revoked!");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				LOG.debug(e);
				return false;
			}
		}
		LOG.debug("No access token to be revoked!");
		return false;
	}

	/**
	 * Verify if the toke is still active
	 * 
	 * @param access_token
	 * @return
	 */
	public static boolean isTokenActive(String access_token) {
		if (StringUtils.isNotBlank(access_token)) {
			try {

				Map<String, String> headers = new HashMap<String, String>();

				HttpPostForm httpPostForm = new HttpPostForm(
						SystemConfiguration.getValue("SSO_ISSUER_URL") + INTROSPECT_ENDPOINT, "utf-8", headers);

				// httpPostForm.addFormField("grant_type",
				// "authorization_code");
				httpPostForm.addFormField("token", access_token);
				httpPostForm.addFormField("client_id", SystemConfiguration.getValue("W3_CLIENT_ID"));
				httpPostForm.addFormField("client_secret", SystemConfiguration.getValue("W3_CLIENT_SECRET"));

				String response = httpPostForm.finish();
				LOG.debug("verifying if token is valid: " + access_token);
				JSONObject jsonResp = new JSONObject(response);
				return jsonResp.getBoolean("active");
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("Unable to verify the token: " + access_token);
				LOG.debug(e);
				return false;
			}
		}
		LOG.debug("No access token to be revoked!");
		return false;
	}

	/**
	 * After JWT Signature be validated, this method is called to authenticate
	 * user via service and set its role.
	 * 
	 * @param loginUser
	 * @param request
	 * @param response
	 * @throws CmrException,
	 *             Exception
	 */
	public void authorizeAndSetRoles(LogInUserModel loginUser, UserService userService, HttpServletRequest request,
			HttpServletResponse response) throws CmrException, Exception {
		boolean isApprover = false;
		// implement the new Role mapping
		AuthorizationResponse authResp = authenticateViaService(loginUser.getUsername());

		// authResp.setAuthorized(false);

		if (authResp.isError()) {
			// error in service layer
			throw new CmrException(MessageUtil.ERROR_CANNOT_AUTHENTICATE);
		}

		EntityManager entityManager = JpaManager.getEntityManager();
		try {

			if (!authResp.isAuthorized()) {
				// user has no roles, check if approver first
				LOG.debug("User has no CreateCMR roles. Checking if approver..");
				isApprover = isApprover(entityManager, loginUser.getUsername());

				if (!isApprover) {
					throw new CmrException(MessageUtil.ERROR_BLUEGROUPS_AUTH);
				} else {
					Authorization auth = new Authorization();
					auth.setRoles(new ArrayList<Role>());
					authResp.setAuthorization(auth);
				}
			}

			LOG.debug("User " + loginUser.getUsername() + " authenticated and authorised successfully");

			String userCnum = userService.getUserCnum(loginUser.getUsername());

			Map<String, String> bpPersonDetails = BluePagesHelper
					.getBluePagesDetailsByIntranetAddr(loginUser.getUsername());

			boolean inAdminGroup = false;
			boolean inProcessorGroup = false;
			boolean inRequestorGroup = false;
			boolean inCMDEGroup = false;

			AppUser appUser = new AppUser();
			appUser.setUserCnum(userCnum);
			appUser.setIntranetId(loginUser.getUsername().toLowerCase());
			appUser.setEmpName(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_NAME));
			appUser.setCountryCode(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_COUNTRY_CODE));
			String notesEmail = bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_NOTES_MAIL);

			// set the user roles
			String roleKey = null;
			Authorization auth = authResp.getAuthorization();
			for (Role role : auth.getRoles()) {
				roleKey = role.getRoleId();
				if (roleKey.equals(CmrConstants.ROLES.ADMIN.toString())) {
					inAdminGroup = true;
				}
				if (roleKey.equals(CmrConstants.ROLES.PROCESSOR.toString())) {
					inProcessorGroup = true;
				}
				if (roleKey.equals(CmrConstants.ROLES.REQUESTER.toString())) {
					inRequestorGroup = true;
				}
				if (roleKey.equals(CmrConstants.ROLES.CMDE.toString())) {
					inCMDEGroup = true;
				}
				appUser.addRole(roleKey, role.getSubRoleId());
			}

			if (isApprover) {
				appUser.setApprover(true);
			} else if (isApprover(entityManager, loginUser.getUsername())) {
				appUser.setHasApprovals(true);
			}
			appUser.setAuth(auth);
			LOG.debug("User " + loginUser.getUsername() + ": Roles = " + auth.getRdcRoles().size() + " Auth Groups: "
					+ auth.getAuthGroups().size());

			// set CMR Owner via blue pages' notes email
			appUser.setNotesEmailId(notesEmail);
			if (notesEmail.toUpperCase().contains("LENOVO")) {
				appUser.setCompanyCode("KAU");
			} else if (notesEmail.toUpperCase().contains("TRURO")) {
				appUser.setCompanyCode("TRU");
			} else if (notesEmail.toUpperCase().contains("FONSECA")) {
				appUser.setCompanyCode("FON");
			} else if (notesEmail.toUpperCase().contains("IBM")) {
				appUser.setCompanyCode("IBM");
			}

			PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("LOGIN.HAS_PREF"));
			query.setParameter("USER_ID", appUser.getIntranetId().toLowerCase());
			List<Object[]> results = query.getResults(2);
			boolean hasDelegate = false;
			boolean hasRecord = false;
			for (Object[] result : results) {
				hasRecord = true;
				if (!StringUtils.isEmpty((String) result[2])) {
					hasDelegate = true;
				}
				if (!StringUtils.isEmpty((String) result[3]) && appUser.getProcessingCenter() == null) {
					appUser.setProcessingCenter((String) result[3]);
				}
				if (!StringUtils.isEmpty((String) result[1]) && appUser.getCmrIssuingCntry() == null) {
					appUser.setCmrIssuingCntry((String) result[1]);
				}
				if (!StringUtils.isEmpty((String) result[4]) && appUser.getBluePagesName() == null) {
					appUser.setBluePagesName((String) result[4]);
				}
				if (!StringUtils.isEmpty((String) result[5])) {
					appUser.setDefaultLineOfBusn((String) result[5]);
				}
				if (!StringUtils.isEmpty((String) result[6])) {
					appUser.setDefaultRequestRsn((String) result[6]);
				}
				if (!StringUtils.isEmpty((String) result[7])) {
					appUser.setDefaultReqType((String) result[7]);
				}
				if (!StringUtils.isEmpty((String) result[8])) {
					appUser.setDefaultNoOfRecords(Integer.parseInt((String) result[8]));
				}
				if (!StringUtils.isEmpty((String) result[9])) {
					appUser.setHasCountries(true);
				}
				appUser.setShowPendingOnly("Y".equals(result[10]));
				appUser.setShowLatestFirst("Y".equals(result[11]));
			}
			if (hasDelegate) {
				appUser.setPreferencesSet(true);
				if (appUser.getBluePagesName() == null) {
					Person p = BluePagesHelper.getPerson(appUser.getIntranetId());
					if (p != null) {
						appUser.setBluePagesName(p.getName());
					}
				}

			} else if (!hasRecord) {
				// create the default record
				String name = createUserPrefRecord(entityManager, appUser.getIntranetId(), appUser.getEmpName(),
						appUser.getCountryCode(), notesEmail);
				appUser.setBluePagesName(name);
			}

			appUser.setAdmin(inAdminGroup);
			appUser.setProcessor(inProcessorGroup);
			appUser.setRequestor(inRequestorGroup);
			appUser.setCmde(inCMDEGroup);

			// Set it in the session so that it can be later accessed in UI
			// for display
			request.getSession().setAttribute("displayName",
					bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_NAME));

			// assign appuser
			request.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, appUser);

			request.getSession().setAttribute("cmr.last.request.date", new Date());

			request.getSession().setAttribute("logged-in", "true");

			request.getSession().setAttribute("WTMStatus", "P");

			request.getSession().setMaxInactiveInterval(60 * 60 * 3);

			response.addHeader("Set-Cookie",
					"countryCode=" + appUser.getCountryCode() + "; HttpOnly; Secure; SameSite=Strict");

			// if (appUser.isPreferencesSet()) {
			// if (loginUser.getR() > 0) {
			// mv = new ModelAndView("redirect:/request/" + loginUser.getR(),
			// "appUser", appUser);
			// } else if (!StringUtils.isBlank(loginUser.getC())) {
			// String c = loginUser.getC();
			// String decoded = new String(Base64.getDecoder().decode(c));
			// String params = decoded.substring(2);
			// if (decoded.startsWith("f")) {
			// mv = new ModelAndView("redirect:/findcmr?" + params, "appUser",
			// appUser);
			// } else if (decoded.startsWith("r")) {
			// mv = new ModelAndView("redirect:/request?" + params, "appUser",
			// appUser);
			// } else {
			// mv = new ModelAndView("redirect:/home", "appUser", appUser);
			// }
			// } else if (appUser.isApprover()) {
			// mv = new ModelAndView("redirect:/myappr", "approval", new
			// MyApprovalsModel());
			// } else {
			// mv = new ModelAndView("redirect:/home", "appUser", appUser);
			// }
			// // setPageKeys("HOME", "OVERVIEW", mv);
			// } else {
			// UserPrefModel pref = new UserPrefModel();
			// pref.setRequesterId(appUser.getIntranetId());
			// mv = new ModelAndView("redirect:/preferences", "pref", pref);
			// // setPageKeys("PREFERENCE", "PREF_SUB", mv);
			// }

			LOG.debug("User roles and preferences set successfully: " + appUser.getIntranetId());

			SystemParameters.logUserAccess("CreateCMR", appUser.getIntranetId());
			AuthCodeRetriever authCode = new AuthCodeRetriever(loginUser.getUsername(), request.getSession());
			Thread authThread = new Thread(authCode);
			authThread.start();

		} catch (Exception e) {
			LOG.error("Error in retrieving Preference settings.", e);
			if (e instanceof CmrException) {
				throw e;
			}
			throw new CmrException(MessageUtil.ERROR_GENERAL);
		} finally {
			entityManager.clear();
			entityManager.close();
		}

	}

	private AuthorizationResponse authenticateViaService(String user) throws Exception {
		try {
			String url = SystemConfiguration.getValue("CMR_SERVICES_URL", "");
			if ("".equals(url)) {
				AuthorizationResponse resp = new AuthorizationResponse();
				Authorization det = new Authorization();
				resp.setAuthorized(false);
				resp.setAuthorization(det);
				return resp;
			}

			AuthorizationClient auth = CmrServicesFactory.getInstance().createClient(url, AuthorizationClient.class);
			AuthorizationRequest request = new AuthorizationRequest();
			request.setApplicationCode(ApplicationCode.CreateCMR);
			request.setUserId(user.toLowerCase());

			AuthorizationResponse response = auth.executeAndWrap(request, AuthorizationResponse.class);
			return response;
		} catch (Exception e) {
			LOG.error("Error in connecting to the Authorization Service: " + e.getMessage());
			throw new CmrException(MessageUtil.ERROR_CANNOT_AUTHENTICATE);
		}
	}

	/**
	 * Checks if the user is currently an approver
	 * 
	 * @param entityManager
	 * @param intranetId
	 * @return
	 */
	private boolean isApprover(EntityManager entityManager, String intranetId) {
		String sql = ExternalizedQuery.getSql("APPROVAL.CHECK_APPROVER");
		PreparedQuery query = new PreparedQuery(entityManager, sql);
		query.setParameter("ID", intranetId.toLowerCase());
		query.setForReadOnly(true);
		return query.exists();
	}

	private String createUserPrefRecord(EntityManager entityManager, String intranetId, String name, String cntryCode,
			String notesId) throws Exception {
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		try {
			LOG.debug("Creating user preferences for " + name + " (" + intranetId + ")");
			UserPref pref = new UserPref();
			UserPrefPK pk = new UserPrefPK();
			pk.setRequesterId(intranetId);
			pref.setId(pk);

			pref.setReceiveMailInd(CmrConstants.YES_NO.Y.toString());
			Person p = BluePagesHelper.getPerson(intranetId, notesId);
			if (p == null || StringUtils.isEmpty(p.getName())) {
				pref.setRequesterNm(name);
			} else {
				pref.setRequesterNm(p.getName());
			}
			LOG.debug("Pref being added: " + pref.getId().getRequesterId() + " (" + pref.getRequesterNm() + ")");
			pref.setDftIssuingCntry(getMappedCountryCode(cntryCode));

			entityManager.persist(pref);
			entityManager.flush();
			tx.commit();

			return pref.getRequesterNm();
		} catch (Exception e) {
			LOG.error("Error in creating User Preference record", e);
			tx.rollback();
			throw e;
		}
	}

	// for exceptions on the country code
	private String getMappedCountryCode(String countryCode) {
		if ("PH1".equals(countryCode)) {
			return "818";
		}
		return countryCode;
	}

	class AuthCodeRetriever implements Runnable {

		private String username;
		private HttpSession session;

		public AuthCodeRetriever(String username, HttpSession session) {
			this.username = username;
			this.session = session;
		}

		@Override
		public void run() {
			try {
				LOG.debug("Starting Authorization Code retrieval from Find CMR ["
						+ SystemConfiguration.getValue("FIND_CMR_URL") + "]");
				StringBuilder sb = new StringBuilder();
				URL url = new URL(SystemConfiguration.getValue("FIND_CMR_URL") + "/authorize?username=" + this.username
						+ "&appName=RequestCMR");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				InputStream is = conn.getInputStream();
				try {
					InputStreamReader isr = new InputStreamReader(is, "UTF-8");
					try {
						BufferedReader br = new BufferedReader(isr);
						try {
							String line = null;
							while ((line = br.readLine()) != null) {
								sb.append(line);
							}
						} finally {
							br.close();
						}
					} finally {
						isr.close();
					}
				} finally {
					is.close();
				}
				LOG.debug("Returned " + sb.toString());
				AppUser user = (AppUser) this.session.getAttribute(CmrConstants.SESSION_APPUSER_KEY);
				if (user != null) {
					user.setAuthCode(sb.toString());
				}
				conn.disconnect();
			} catch (Exception e) {
				LOG.error("Error in retrieving authorization code " + e.getMessage());
			}
		}

	}

}
