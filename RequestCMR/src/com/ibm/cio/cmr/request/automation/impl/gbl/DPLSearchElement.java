/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.dpl.DPLSearchProcess;
import com.ibm.cio.cmr.request.automation.dpl.DPLSearchResult;
import com.ibm.cio.cmr.request.automation.impl.MatchingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.MatchingOutput;
import com.ibm.cio.cmr.request.automation.util.CommonWordsUtil;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationMatching;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.AttachmentService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.pdf.impl.DPLSearchPDFConverter;
import com.ibm.cmr.services.client.dpl.DPLRecord;
import com.ibm.cmr.services.client.dpl.DPLSearchResults;

/**
 * {@link AutomationElement} that connects to the defined DPL Check web
 * application and performs a search on the name
 * 
 * @author JeffZAMORA
 * 
 */
public class DPLSearchElement extends MatchingElement {

  private static final Logger LOG = Logger.getLogger(DPLSearchElement.class);

  public DPLSearchElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public boolean importMatch(EntityManager entityManager, RequestData requestData, AutomationMatching match) {
    // no import to data
    return true;
  }

  @Override
  public AutomationResult<MatchingOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Admin admin = requestData.getAdmin();
    long reqId = admin.getId().getReqId();
    StringBuilder details = new StringBuilder();

    AutomationResult<MatchingOutput> result = buildResult(reqId);
    MatchingOutput output = new MatchingOutput();

    Scorecard scorecard = requestData.getScorecard();
    if ("NR".equals(scorecard.getDplChkResult()) || "AP".equals(scorecard.getDplChkResult())) {
      result.setResults("Not required");
      result.setProcessOutput(output);
      result.setDetails("Request passed DPL check, name search is not required.");
      return result;
    }

    List<DPLSearchResults> dplResults = new ArrayList<DPLSearchResults>();
    DPLSearchResults searchResult = null;
    List<String> names = extractCompanyNames(entityManager, requestData);

    DPLSearchProcess dplSearch = new DPLSearchProcess();
    for (String name : names) {
      dplSearch.performDplSearch(name, isPrivate(requestData));
      searchResult = dplSearch.getResult();
      if (searchResult != null) {
        dplResults.add(searchResult);
      }
    }
    details.append("DPL name search performed successfully.\n");

    if (dplResults.isEmpty()) {
      result.setOnError(true);
      result.setResults("Not Done");
      result.setDetails("DPL name search cannot be performed for this request.");
    } else {
      int cnt = 0;
      boolean exactMatch = false;
      boolean partialMatch = false;

      for (DPLSearchResults dplResult : dplResults) {
        cnt += dplResult.getDeniedPartyRecords().size();
        if (DPLSearchResult.exactMatchFound(dplResult)) {
          exactMatch = true;
        }
        if (DPLSearchResult.partialMatchFound(dplResult)) {
          partialMatch = true;
        }
      }
      if (cnt == 0) {
        result.setResults("No matches found");
        details.append("No match found against the DPL database for the request.\n");
        details.append("Names used for searching:\n");
        for (String name : names) {
          details.append(" - " + name.toLowerCase() + "\n");
        }
        result.setDetails(details.toString());
      } else {
        if (exactMatch) {
          result.setResults("Exact matches");
        } else if (partialMatch) {
          result.setResults("Partial matches");
        } else {
          result.setResults("Potential matches");
        }
        details.append("Matches against the DPL database found for the request.\n");
        details.append("Complete results added as attachment.\n");
        int resultNo = 1;
        for (DPLSearchResults dplResult : dplResults) {
          output.addMatch(getProcessCode(), "ENTITY_ID", resultNo + "", "", "", "DPL", resultNo);
          details.append("\nName: " + dplResult.getSearchArgument()).append("\n");
          details.append("Potential Matches: ").append(dplResult.getDeniedPartyRecords().size()).append("\n");
          details.append("Highest matches: ").append("\n");

          for (DPLRecord item : DPLSearchResult.getTopMatches(dplResult)) {
            if (!StringUtils.isBlank(item.getCompanyName())) {
              details.append(" - ").append(item.getCompanyName()).append("(" + item.getCountryCode() + ")").append("\n");
            } else if (!StringUtils.isBlank(item.getCustomerLastName())) {
              details.append(" - ").append(item.getCustomerFirstName() + " " + item.getCustomerLastName()).append("(" + item.getCountryCode() + ")")
                  .append("\n");
            }
          }

          resultNo++;
        }
        result.setDetails(details.toString().trim());
      }

      Timestamp currTs = SystemUtil.getActualTimestamp();

      DPLSearchPDFConverter pdf = new DPLSearchPDFConverter(admin.getRequesterNm() + "  [" + admin.getRequesterId() + "]", currTs.getTime(),
          names.get(0).toUpperCase(), dplResults);
      AttachmentService attachmentService = new AttachmentService();
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        LOG.debug("Generating PDF content for DPL search matches for Request " + admin.getId().getReqId() + "..");
        pdf.exportToPdf(entityManager, null, null, bos, null);

        byte[] pdfBytes = bos.toByteArray();

        LOG.debug("Creating request attachment..");
        try (ByteArrayInputStream bis = new ByteArrayInputStream(pdfBytes)) {
          AppUser dummyUser = new AppUser();
          dummyUser.setEmpName(admin.getRequesterNm());
          dummyUser.setBluePagesName(admin.getRequesterNm());
          dummyUser.setIntranetId(admin.getRequesterId());
          attachmentService.addExternalAttachment(entityManager, dummyUser, reqId, "DPL", "DPLSearchResults_" + admin.getId().getReqId() + ".pdf",
              "DPL database name search details", bis);
        }
      }

    }

    result.setProcessOutput(output);
    return result;
  }

  /**
   * Extracts the company names to be used for DPL check
   * 
   * @param entityManager
   * @param requestData
   * @return
   */
  private List<String> extractCompanyNames(EntityManager entityManager, RequestData requestData) {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    GEOHandler handler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    List<String> allNames = new ArrayList<String>();
    if (handler != null && handler.customerNamesOnAddress()) {
      String sql = ExternalizedQuery.getSql("AUTOMATION.DPL.GET_UNIQUE_NAMES");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      query.setForReadOnly(true);
      List<String> uniques = query.getResults(String.class);
      allNames.addAll(uniques);
      for (String unique : uniques) {
        String minimized = CommonWordsUtil.minimize(unique).toLowerCase().trim();
        if (!allNames.contains(minimized)) {
          allNames.add(minimized);
        }
      }
    } else {
      String name = admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2());
      allNames.add(name);
      String minimized = CommonWordsUtil.minimize(name).toLowerCase().trim();
      if (!allNames.contains(minimized)) {
        allNames.add(minimized);
      }
    }
    return allNames;
  }

  private boolean isPrivate(RequestData reqData) {
    Data data = reqData.getData();
    String subGrp = data.getCustSubGrp();
    if (subGrp != null) {
      if (subGrp.toUpperCase().contains("PRIV") || subGrp.toUpperCase().contains("PRIPE") || subGrp.toUpperCase().contains("PRICU")) {
        return true;
      }
    }
    return "60".equals(data.getCustClass()) || "9500".equals(data.getIsicCd());
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_DPL_SEARCH;
  }

  @Override
  public String getProcessDesc() {
    return "DPL Search";
  }

  @Override
  public boolean isNonImportable() {
    return true;
  }

}
