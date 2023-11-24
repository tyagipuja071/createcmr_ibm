package com.ibm.cio.cmr.request.util.oauth;

import java.io.IOException;
import java.util.Base64;
import java.util.StringTokenizer;

import com.ibm.json.java.JSONObject;

public class SimpleJWT {
	JSONObject _header;
	JSONObject _payload;
	String _signature;

	public SimpleJWT(String s) throws IOException {
		StringTokenizer st = new StringTokenizer(s, ".");
		if (st.countTokens() == 3) {
			String nextToken = st.nextToken();
			String decodedToken = decodeBase64(nextToken);
			_header = JSONObject.parse(decodedToken);

			nextToken = st.nextToken();
			decodedToken = decodeBase64(nextToken);

			_payload = JSONObject.parse(decodedToken);

			_signature = st.nextToken();
		}
	}

	public JSONObject getHeader() {
		return _header;
	}

	public JSONObject getClaims() {
		return _payload;
	}

	public String getPart3() {
		return _signature;
	}

	public String decodeBase64(String s) {
		String result = null;
		result = new String(Base64.getUrlDecoder().decode(s.getBytes()));
		return result;
	}
}
