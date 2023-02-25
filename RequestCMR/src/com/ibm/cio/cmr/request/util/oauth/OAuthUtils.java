package com.ibm.cio.cmr.request.util.oauth;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.config.SystemConfiguration;

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
    String kid = (String) tokens.getId_token()._part1.get("kid");
    PublicKey publicKey = null;

    for (Map<String, String> key : keys) {
      if (key.get("kid").equalsIgnoreCase(kid)) {

        publicKey = getPublicKey(key.get("n").toString(), key.get("e").toString());

        break;
      }
    }

    // validate the signature using the public key
    String jwtToken = tokens.getEncodedJwtToken();
    String header = jwtToken.substring(0, jwtToken.indexOf("."));
    String payload = jwtToken.substring(jwt.indexOf(".") + 1, jwt.lastIndexOf("."));
    String tokenSignature = jwtToken.substring(jwtToken.lastIndexOf(".") + 1);
    byte[] tokenSignatureDecoded = java.util.Base64.getUrlDecoder().decode(tokenSignature);

    // verify signature
    boolean isValid = verifySignatureFor(ALGORITHM, publicKey, header.getBytes(), payload.getBytes(), tokenSignatureDecoded);

    if (!isValid) {
      throw new SignatureException("Invalid Signature!");
    }

    return isValid;
  }

  /**
   * Verify signature for JWT header and payload using a public key.
   *
   * @param algorithm
   *          algorithm name.
   * @param publicKey
   *          the public key to use for verification.
   * @param headerBytes
   *          JWT header.
   * @param payloadBytes
   *          JWT payload.
   * @param signatureBytes
   *          JWT signature.
   * @return true if signature is valid.
   * @throws NoSuchAlgorithmException
   *           if the algorithm is not supported.
   * @throws InvalidKeyException
   *           if the given key is inappropriate for initializing the specified
   *           algorithm.
   */
  public static boolean verifySignatureFor(String algorithm, PublicKey publicKey, byte[] headerBytes, byte[] payloadBytes, byte[] signatureBytes)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    final Signature s = Signature.getInstance(algorithm);
    s.initVerify(publicKey);
    s.update(headerBytes);
    s.update(JWT_PART_SEPARATOR);
    s.update(payloadBytes);
    return s.verify(signatureBytes);
  }

  /**
   * This method gets the keys from Authorization Server JWKS endpoint
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
        throw new Exception("An error ocurred when trying to get AS Public key information");
      }

      // closing connection
      conn.disconnect();

      // parse the keys
      keys = (List<Map<String, String>>) new ObjectMapper().readValue(responseContent.toString(), HashMap.class).get("keys");

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

  private static PublicKey getPublicKey(String modulus, String exponent) {
    try {
      byte exponentB[] = Base64.decodeBase64(exponent);
      byte modulusB[] = Base64.decodeBase64(modulus);
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

  public static RSAPrivateKey readPrivateKey(File file) throws Exception {
    String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

    String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll(System.lineSeparator(), "").replace("-----END PRIVATE KEY-----",
        "");

    byte[] encoded = Base64.decodeBase64(privateKeyPEM);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }

}
