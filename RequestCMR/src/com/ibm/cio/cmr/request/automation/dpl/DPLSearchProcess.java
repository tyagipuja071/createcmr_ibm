/**
 * 
 */
package com.ibm.cio.cmr.request.automation.dpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.xml.sax.SAXException;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.Person;
import com.ibm.cio.cmr.request.util.SystemParameters;

/**
 * Connects to the DPL Check service via a user's encrypted LTPA token and
 * retrieves the results
 * 
 * @author JeffZAMORA
 * 
 */
public class DPLSearchProcess {

  private static final Logger LOG = Logger.getLogger(DPLSearchProcess.class);

  private String accessToken;
  private String notesId;
  private DPLSearchResult currentResult;

  /**
   * Creates a new instance of the DPL Process
   * 
   * @param accessToken
   * @param userId
   * @throws CmrException
   */
  public DPLSearchProcess(String accessToken, String userId) throws CmrException {
    this.accessToken = accessToken;
    Person person = BluePagesHelper.getPerson(userId);
    if (person != null) {
      this.notesId = person.getNotesEmail();
    } else {
      this.notesId = userId;
    }
  }

  /**
   * Connects to the defined DPL Check Web URL and performs a search.
   * 
   * @param companyName
   * @throws Exception
   */
  public void performDplCheck(String companyName) throws Exception {

    String dplCheckUrl = SystemParameters.getString("DPL_CHECK_URL");

    if (StringUtils.isBlank(dplCheckUrl)) {
      dplCheckUrl = "https://w3-03.ibm.com/legal/denied-parties-list/denied.nsf";
    }
    ClientConfig config = new ClientConfig();
    config.followRedirects(true);
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    RestClient client = new RestClient(config);

    Resource resource = client.resource(dplCheckUrl + "/dplwsw3ds?OpenAgent");
    resource.queryParam("SearchFor", urlEscape(companyName.toLowerCase()));
    resource.queryParam("LocalSD", sdf.format(new Date()));
    resource.queryParam("AbbrUsr", urlEscape(this.notesId));

    Cookie cookie = new Cookie("IBM_W3SSO_ACCESS", "", "/", ".ibm.com");
    resource.cookie(cookie);
    cookie = new Cookie("LtpaToken", this.accessToken, "/", ".ibm.com");
    resource.cookie(cookie);
    ClientResponse resp = resource.get();
    String resultId = resp.getEntity(String.class);
    String[] parts = resultId.split(">");
    for (String p : parts) {
      if (!p.trim().startsWith("<")) {
        resultId = p;
        break;
      }
    }
    if (resultId.length() > 33) {
      resultId = resultId.substring(0, 33).trim();
    }
    if (resultId == null || resultId.contains("IBM")) {
      throw new Exception("Access Token is invalid.");
    }
    LOG.debug("DPL Check Result ID: " + resultId.trim());
    resource = client.resource(dplCheckUrl + "/viewdplsrw3ds/" + resultId);

    cookie = new Cookie("IBM_W3SSO_ACCESS", "", "/", ".ibm.com");
    resource.cookie(cookie);
    cookie = new Cookie("LtpaToken", this.accessToken, "/", ".ibm.com");
    resource.cookie(cookie);
    resp = resource.get();
    String results = resp.getEntity(String.class).toUpperCase();
    List<DPLSearchItem> dplResults = parseResults(results);
    DPLSearchResult result = new DPLSearchResult();
    result.setName(companyName.toLowerCase());
    result.setDate(sdf.format(new Date()));
    result.setItems(dplResults);
    result.setResultId(resultId);

    this.currentResult = result;
  }

  /**
   * Parses the HTML results from DPL check
   * 
   * @param dplCheckResults
   * @return
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  private List<DPLSearchItem> parseResults(String dplCheckResults) throws ParserConfigurationException, SAXException, IOException {
    if (!dplCheckResults.contains("<TBODY")) {
      LOG.warn("Cannot parse DPL Check results..");
      return new ArrayList<DPLSearchItem>();
    }
    String table = dplCheckResults.substring(dplCheckResults.indexOf("<TBODY") - 5);
    table = table.substring(0, table.indexOf("</TBODY"));

    table = table.replaceAll("&", "&amp;");
    table = "<MAIN>" + table + "</TBODY></MAIN>";
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    ByteArrayInputStream bis = new ByteArrayInputStream(table.getBytes());
    try {
      DPLSearchResultHandler resultParser = new DPLSearchResultHandler();
      parser.parse(bis, resultParser);

      return resultParser.getResults();
    } finally {
      bis.close();
    }
  }

  /**
   * Returns the results. This result will be blank unless
   * {@link #performDplCheck(String)} has been done
   * 
   * @return
   */
  public DPLSearchResult getResult() {
    return this.currentResult;
  }

  private String urlEscape(String value) {
    return value.replaceAll(" ", "%20");
  }
}
