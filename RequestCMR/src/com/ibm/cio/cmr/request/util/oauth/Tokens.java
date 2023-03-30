package com.ibm.cio.cmr.request.util.oauth;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Tokens {
	private String access_token;
	private String scopes;
	private String grant_id;
	private SimpleJWT id_token;
	private String encodedJwtToken;
	private String tokenType;
	private LocalDateTime expires_in;

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	public String getGrant_id() {
		return grant_id;
	}

	public void setGrant_id(String grant_id) {
		this.grant_id = grant_id;
	}

	public SimpleJWT getId_token() {
		return id_token;
	}

	public void setId_token(String id_token) throws IOException {
		this.id_token = new SimpleJWT(id_token);
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public LocalDateTime getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(Integer expires_in) {
		this.expires_in = LocalDateTime.now().plus(new Long(expires_in - 1), ChronoUnit.SECONDS);
	}

	public String getEncodedJwtToken() {
		return encodedJwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.encodedJwtToken = jwtToken;
	}

}
