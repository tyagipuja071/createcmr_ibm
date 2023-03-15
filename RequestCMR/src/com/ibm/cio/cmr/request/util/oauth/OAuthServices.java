package com.ibm.cio.cmr.request.util.oauth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cmr.services.client.ServiceClient;

public class OAuthServices extends ServiceClient {

	protected OAuthServices(String baseUrl) {
		super(baseUrl);
		// TODO Auto-generated constructor stub
	}

	private static final Logger LOG = Logger.getLogger(OAuthServices.class);

	public String getAuthorizationCode(String url) throws Exception {
		String authorizationCode = new String();

		try (CloseableHttpClient httpClient = HttpClients.createSystem()) {

			HttpGet get = new HttpGet(url);

			get.setHeader("Accept", "application/json");
			get.setHeader("Content-Type", "application/json");

			LOG.debug("Connecting to " + url);
			HttpResponse httpResponse = httpClient.execute(get);

			HttpEntity entity = httpResponse.getEntity();

			LOG.debug("Service call to " + url + " completed.");
		}

		return null;
	}

	public String getAccessToken(String url, String code) throws IOException {
		String accessToken = new String();

		try {

			Map<String, String> headers = new HashMap<String, String>();

			HttpPostForm httpPostForm = new HttpPostForm(url, "utf-8", headers);

			httpPostForm.addFormField("grant_type", "authorization_code");
			httpPostForm.addFormField("code", code);
			httpPostForm.addFormField("client_id", SystemConfiguration.getValue("W3_CLIENT_ID"));
			httpPostForm.addFormField("client_secret", SystemConfiguration.getValue("W3_CLIENT_SECRET"));
			httpPostForm.addFormField("redirect_uri", SystemConfiguration.getValue("SSO_REDIRECT_URL"));

			accessToken = httpPostForm.finish();

			System.out.println(accessToken);

			LOG.debug("Access token generated!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accessToken;
	}

	@Override
	protected String getServiceId() {
		// TODO Auto-generated method stub
		return "cmr";
	}

}
