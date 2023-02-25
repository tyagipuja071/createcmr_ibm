package com.ibm.cio.cmr.request.util.oauth;

import java.io.IOException;
import java.util.Base64;
import java.util.StringTokenizer;

import com.ibm.json.java.JSONObject;

public class SimpleJWT {
  JSONObject _part1;
  JSONObject _part2;
  String _part3;

  public SimpleJWT(String s) throws IOException {
    StringTokenizer st = new StringTokenizer(s, ".");
    if (st.countTokens() == 3) {
      String str = st.nextToken();
      String decodedStr = decodeBase64(str);
      _part1 = JSONObject.parse(decodedStr);

      str = st.nextToken();
      decodedStr = decodeBase64(str);

      _part2 = JSONObject.parse(decodedStr);

      _part3 = st.nextToken();
    }
  }

  public JSONObject getHeader() {
    return _part1;
  }

  public JSONObject getClaims() {
    return _part2;
  }

  public String getPart3() {
    return _part3;
  }

  public String decodeBase64(String s) {
    String result = null;
    result = new String(Base64.getUrlDecoder().decode(s.getBytes()));
    return result;
  }
}
