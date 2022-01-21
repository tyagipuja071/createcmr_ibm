package com.ibm.cio.cmr.request.service.revivedcmr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.revivedcmr.RevivedCMRModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.utils.coverage.CoverageRules;
import com.ibm.cio.cmr.utils.coverage.JarProperties;
import com.ibm.cio.cmr.utils.coverage.objects.CoverageInput;
import com.ibm.cio.cmr.utils.coverage.rules.Coverage;
import com.ibm.cio.cmr.utils.coverage.rules.Rule;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.matching.gbg.GBGResponse;

/**
 * @author clint
 * 
 */
@Component
public class RevivedCMRService extends BaseSimpleService<List<RevivedCMRModel>> {

  private static final Logger LOG = Logger.getLogger(RevivedCMRService.class);

  private static final String QUERY_BG = "AUTO.COV.GET_COV_FROM_BG";
  private static final String QUERY_VAT = "AUTO.COV.GET_COV_FROM_VAT";

  public static final String COV_BG = "COV_BG";
  public static final String COV_VAT = "COV_VAT";
  public static final String COV_ODM = "COV_ODM";
  public static final String BG_NONE = "BG_NONE";
  public static final String NONE = "NONE";

  private Map<Integer, String> columnMap = new HashMap<>();

  private static CoverageRules coverageRules;

  // private static final ResourceBundle COV_RULES_PROPS =
  // ResourceBundle.getBundle("rulejar");

  @Override
  protected List<RevivedCMRModel> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    // parse file
    List<RevivedCMRModel> revCMRList = parseFile(request, params);

    StringBuilder details = new StringBuilder();

    findGBG(revCMRList, entityManager, details);
    calculateCoverage(revCMRList, entityManager, details);
    computeFields(revCMRList, entityManager, details);

    LOG.debug("details: " + details);

    return revCMRList;
  }

  public void findGBG(List<RevivedCMRModel> revCMRList, EntityManager entityManager, StringBuilder details) throws Exception {
    for (RevivedCMRModel revCmr : revCMRList) {
      GEOHandler geoHandler = RequestUtils.getGEOHandler(revCmr.getIssuingCountry());
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(revCmr.getIssuingCountry());
      // if model not null
      GBGFinderRequest gbgRequest = new GBGFinderRequest();
      GBGFinderRequest dnbrequest = new GBGFinderRequest();
      String sql = ExternalizedQuery.getSql("GET.KNA1.REVIVED");
      PreparedQuery query = new PreparedQuery(entityManager, sql);

      // still need to check per country what is the main address, right now
      // it's
      // always ZS01
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("ZZKV_CUSNO", revCmr.getCmrNo());
      query.setParameter("KATR6", revCmr.getIssuingCountry());

      MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          MatchingServiceClient.class);
      client.setReadTimeout(1000 * 60 * 30);

      List<Kna1> kna1List = query.getResults(Kna1.class);

      if (kna1List != null && kna1List.size() > 0) {
        for (Kna1 kna1 : kna1List) {
          dnbrequest.setMandt(SystemConfiguration.getValue("MANDT"));
          dnbrequest.setCustomerName(kna1.getName1());
          dnbrequest.setStreetLine1(kna1.getStras());
          dnbrequest.setCity(kna1.getOrt01());
          dnbrequest.setStateProv(kna1.getRegio());
          dnbrequest.setLandedCountry(kna1.getLand1());
          dnbrequest.setMinConfidence("7");

          MatchingResponse<DnBMatchingResponse> dnbresponse = new MatchingResponse<DnBMatchingResponse>();
          MatchingResponse<DnBMatchingResponse> rawResponse = client.executeAndWrap(MatchingServiceClient.DNB_SERVICE_ID, dnbrequest,
              MatchingResponse.class);

          ObjectMapper mapper = new ObjectMapper();
          String json = mapper.writeValueAsString(rawResponse);

          TypeReference<MatchingResponse<DnBMatchingResponse>> ref = new TypeReference<MatchingResponse<DnBMatchingResponse>>() {
          };

          dnbresponse = mapper.readValue(json, ref);
          List<DnBMatchingResponse> dnbMatches = dnbresponse.getMatches();

          if (dnbMatches.size() == 0) { // there are no matches, automation gets
                                        // it from data.getDunsNo. how to get
                                        // from here?
            revCmr.setDunsNo("No matches");
            break;
          }

          DnBMatchingResponse dnbRecord = dnbMatches.get(0);

          gbgRequest.setMandt(SystemConfiguration.getValue("MANDT"));
          gbgRequest.setIssuingCountry(kna1.getKatr6());
          gbgRequest.setCustomerName(kna1.getName1());
          gbgRequest.setStreetLine1(kna1.getStras());
          // gbgRequest.setStreetLine2(record.getStreetAddress2());
          gbgRequest.setLandedCountry(kna1.getLand1());
          gbgRequest.setPostalCode(kna1.getPstlz());
          gbgRequest.setStateProv(kna1.getRegio());
          gbgRequest.setCity(kna1.getOrt01());
          gbgRequest.setDunsNo(dnbRecord.getDunsNo());
          revCmr.setDunsNo(dnbRecord.getDunsNo());
          gbgRequest.setMinConfidence("6");
          revCmr.setVat(kna1.getStcd1());
        }
      }

      // if (automationUtil != null) {
      // automationUtil.tweakGBGFinderRequest(entityManager, request,
      // requestData, engineData);
      // }

      client.setRequestMethod(Method.Get);

      LOG.debug("Connecting to the GBG Finder Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.GBG_SERVICE_ID, gbgRequest, MatchingResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);

      TypeReference<MatchingResponse<GBGResponse>> ref = new TypeReference<MatchingResponse<GBGResponse>>() {
      };
      MatchingResponse<GBGResponse> response = mapper.readValue(json, ref);

      if (response != null && response.getMatched()) {
        List<GBGResponse> gbgMatches = response.getMatches();
        Collections.sort(gbgMatches, new GBGComparator(gbgRequest.getLandedCountry()));

        // LOG - Matches found
        details.append(gbgMatches.size() + " record(s) found.");
        if (gbgMatches.size() > 5) {
          gbgMatches = gbgMatches.subList(0, 4);
          details.append("Showing top 5 matches only.");
        }
        boolean domesticGBGFound = false;
        for (GBGResponse gbg : gbgMatches) {
          if (gbg.isDomesticGBG()) {
            domesticGBGFound = true;
            break;
          }
        }
        if (!domesticGBGFound) {
          LOG.debug("Non-Local gbg found");
          details.append("Matches for Global Buying Groups retrieved but no domestic Global Buying Group was found during the matching.\n");
        }
        GBGResponse gbg = gbgMatches.get(0);

        if (gbg.isDomesticGBG() || !domesticGBGFound) {
          details.append("\n");
          if (gbg.isDnbMatch()) {
            LOG.debug("Matches found via D&B matching..");
            details.append("\n").append("Found via DUNS matching:");
          } else if (gbg.isVatMatch()) {
            LOG.debug("Matches found via ORG ID matching..");
            details.append("\n").append("Found via ORG ID matching:");

          }
          revCmr.setGbgId(gbg.getGbgId());
          revCmr.setBgId(gbg.getBgId());
          revCmr.setCmrCount(String.valueOf(gbg.getCmrCount()));
          revCmr.setLdeRule(gbg.getLdeRule());
          revCmr.setIntAcctType(gbg.getIntAcctType() != null ? gbg.getIntAcctType() : "-");
          details.append("\n").append("GBG: " + gbg.getGbgId() + " (" + gbg.getGbgName() + ")");
          details.append("\n").append("BG: " + gbg.getBgId() + " (" + gbg.getBgName() + ")");
          details.append("\n").append("Country: " + gbg.getCountry());
          details.append("\n").append("CMR Count: " + gbg.getCmrCount());
          details.append("\n").append("LDE Rule: " + gbg.getLdeRule());
          details.append("\n").append("IA Account: " + (gbg.getIntAcctType() != null ? gbg.getIntAcctType() : "-"));
          if (gbg.isDnbMatch()) {
            revCmr.setGuDunsNo(gbg.getGuDunsNo());
            details.append("\n").append("GU DUNS: " + gbg.getGuDunsNo() + "\nDUNS: " + gbg.getDunsNo());
          }
        }
      } else {
        // no GBG
      }
    }

  }

  public void calculateCoverage(List<RevivedCMRModel> revCMRList, EntityManager entityManager, StringBuilder details) throws Exception {
    if (coverageRules == null) {
      try {
        initCoverageRules();
      } catch (Exception e) {
        LOG.error("Error in initialization", e);
        // noInit = true;
      }
    }
    for (RevivedCMRModel revCmr : revCMRList) {
      GEOHandler geoHandler = RequestUtils.getGEOHandler(revCmr.getIssuingCountry());
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(revCmr.getIssuingCountry());
      List<CoverageContainer> coverages = null;
      boolean withCmrData = false;
      boolean coverageNotFound = false;
      boolean isCoverageCalculated = false;
      CoverageContainer calculatedCoverageContainer = new CoverageContainer();
      String covFrom = "XXX";
      String bgId = revCmr.getBgId();
      String gbgId = revCmr.getGbgId();
      if (bgId != null && !"BGNONE".equals(bgId.trim())) {
        coverages = computeCoverageFromRDCQuery(entityManager, QUERY_BG, bgId, revCmr.getIssuingCountry(), false);
        if (coverages != null && !coverages.isEmpty()) {
          CoverageContainer preferredCoverage = coverages.get(0);
          if (preferredCoverage.getFinalCoverageRules() == null) {
            details.append("The preferred coverage '" + preferredCoverage.getFinalCoverage() + "' determined using Buying Group '" + bgId
                + "' was not found in coverage rules.").append("\n");
            details.append("Proceeding with other calculated coverages -").append("\n");
            // LOG.debug("PREFERRED_COVERAGE_ERROR", "The preferred
            // coverage
            // '" + preferredCoverage.getFinalCoverage()
            // + "' determined using Buying Group '" + bgId + "' was not
            // found in coverage rules.");
            coverages = getValidCoverages(coverages);
          }
        }

        if (coverages == null || coverages.isEmpty()) {
          // if (countryUtil != null) { TODO implement for country
          // specific
          // LOG.debug("Performing Gbg based on coverage");
          // countryUtil.performCoverageBasedOnGBG(this, entityManager,
          // result, details, output, requestData, engineData, covFrom,
          // calculatedCoverageContainer, isCoverageCalculated);
          // }
          // CoverageInput inputBG = extractCoverageInput(entityManager,
          // requestData, data, addr, gbgId, bgId);
          CoverageInput inputBG = extractCoverageInput(entityManager, null, null, null, gbgId, bgId);
          List<Coverage> currCoverage = coverageRules.findCoverage(inputBG);
          if (currCoverage != null && !currCoverage.isEmpty()) {
            CoverageContainer cont = new CoverageContainer();
            cont.setFinalCoverage(currCoverage.get(0).getType() + currCoverage.get(0).getId());
            cont.setFinalCoverageRules(coverageRules.findRule(currCoverage.get(0).getType() + currCoverage.get(0).getId()));
            if (currCoverage.size() > 1) {
              cont.setBaseCoverage(currCoverage.get(1).getType() + currCoverage.get(1).getId());
              cont.setBaseCoverageRules(coverageRules.findRule(currCoverage.get(1).getType() + currCoverage.get(1).getId()));
            }
            coverages.add(cont);
          }
        } else {
          withCmrData = true;
        }
        covFrom = COV_BG;
      } else if (StringUtils.isNotEmpty(revCmr.getVat())) {
        coverages = computeCoverageFromRDCQuery(entityManager, QUERY_VAT, revCmr.getVat(), revCmr.getIssuingCountry(), false);
        if (coverages == null || coverages.isEmpty()) {
          // CoverageInput inputBG = extractCoverageInput(entityManager,
          // requestData, data, addr, gbgId, bgId);
          CoverageInput inputBG = extractCoverageInput(entityManager, null, null, null, gbgId, bgId);
          List<Coverage> currCoverage = coverageRules.findCoverage(inputBG);
          if (currCoverage != null && !currCoverage.isEmpty()) {
            CoverageContainer cont = new CoverageContainer();
            cont.setFinalCoverage(currCoverage.get(0).getType() + currCoverage.get(0).getId());
            cont.setFinalCoverageRules(coverageRules.findRule(currCoverage.get(0).getType() + currCoverage.get(0).getId()));
            if (currCoverage.size() > 1) {
              cont.setBaseCoverage(currCoverage.get(1).getType() + currCoverage.get(1).getId());
              cont.setBaseCoverageRules(coverageRules.findRule(currCoverage.get(1).getType() + currCoverage.get(1).getId()));
            }
            coverages.add(cont);
          }
        } else {
          withCmrData = true;
        }
        covFrom = COV_VAT;
        // } else if (!StringUtils.isBlank(data.getCovId()) &&
        // data.getCovId().matches("[AITP]{1}[0-9]{7}")) {
        // try {
        // coverages = getCoverageRuleForID(data.getCovId());
        // } catch (Exception e) {
        // coverages = new ArrayList<CoverageContainer>();
        // coverageNotFound = true;
        // }
        // covFrom = COV_ODM;
      } else if (bgId != null && "BGNONE".equals(bgId.trim())) {
        details.append("Projected Buying Group on the request is 'BGNONE'. Skipping Coverage Calculation from Buying Group.\n");
        covFrom = BG_NONE;
      }

      // TO-DO: get default coverage
      String defaultCoverage = getDefaultCoverage(revCmr.getIssuingCountry());
      // CoverageInput input = null;
      // // coverage id calculation from BG/VAT
      // input = extractCoverageInput(entityManager, null, null, null,
      // gbgId, bgId);
      if (coverages != null && !coverages.isEmpty()) {
        switch (covFrom) {
        case COV_BG:
          details.append("\nCoverages from Buying Group ID " + bgId + " (" + gbgId + ")" + (withCmrData ? "[from current CMRs]" : "")).append("\n");
          break;
        case COV_VAT:
          // details.append("\nCoverages from CMR VAT data for VAT Number
          // "
          // + data.getVat() + (withCmrData ? " [from current CMRs]" :
          // ""))
          // .append("\n");
          break;
        case COV_ODM:
          // details.append("\nCoverage from ODM Projected/User Specified
          // Coverage ID " + data.getCovId()).append("\n");
          break;
        }

        List<String> coverageIds = new ArrayList<String>();
        boolean logNegativeCheck = !COV_ODM.equals(covFrom);
        for (CoverageContainer container : coverages) {
          if (coverages.size() > 3 && coverages.indexOf(container) > 3) {
            break;
          }
          LOG.debug("Logging Final Coverage ID: " + container.getFinalCoverage());
          // logCoverage(entityManager, engineData, requestData,
          // coverageIds, details, output, input, container, FINAL,
          // covFrom,
          // logNegativeCheck);
          logNegativeCheck = false;
          // Save the first calculated coverage ID in the engine data to
          // allow
          // next elements to use it
          if (!isCoverageCalculated
              && (StringUtils.isNotBlank(container.getFinalCoverage()) || StringUtils.isNotBlank(container.getBaseCoverage()))) {
            String finalCoverage = container.getFinalCoverage();
            if (StringUtils.isNotBlank(finalCoverage)) {
              if (!finalCoverage.equals(calculatedCoverageContainer.getFinalCoverage()) && !finalCoverage.equals(defaultCoverage)) {
                // result.setResults("Calculated");
                isCoverageCalculated = true;
              } else if (finalCoverage.equals(calculatedCoverageContainer.getFinalCoverage())) {
                // result.setResults("Review Needed");
                details.append("\nCalculated Coverage is same as the coverage calculated using Request Data.").append("\n");
              } else if (finalCoverage.equals(defaultCoverage)) {
                // result.setResults("Default Coverage");
                details.append("\nCalculated Coverage is same as the Default Coverage.").append("\n");
              }
            }
            calculatedCoverageContainer = container;

          }
        }
        revCmr.setFinalCoverage(calculatedCoverageContainer.getFinalCoverage());
      } else if (BG_NONE.equals(covFrom)) {
        LOG.debug("No Calculated BG Found. Projected BG='BGNONE'. Skipping Regular Coverage Calculation.");
        // result.setResults("Skipped");
      } else {
        // result.setResults("Cannot Calculate");
        covFrom = NONE;
      }

      // hook to perform country specific operations

      // if it is the first calculated coverage, use it to do country
      // specific coverage calculations
      boolean hasCountryCheck = false;
      if (countryUtil != null) {
        // RequestData requestData = new RequestData(entityManager);
        // Data data = new Data();
        String originalScenario = countryUtil.getOriginalScenarioForRevivedCMRs(entityManager, revCmr.getCmrNo());
        // data.setCustSubGrp(originalScenario);
        // requestData.setData(data);
        // hook to perform calculations and update results
        hasCountryCheck = countryUtil.performCountrySpecificCoverageCalculationsForRevivedCMRs(revCmr, originalScenario, covFrom,
            calculatedCoverageContainer, isCoverageCalculated);
      }

      if (!hasCountryCheck) {
        if ("NONE".equals(covFrom)) {
          if (coverageNotFound) {
            // details.append("Coverage ID " + data.getCovId() + " not
            // found
            // in the coverage rules.");
          } else {
            // result.setResults("Skipped");
            details.append("No projected Buying Group or ODM Coverage found, coverage adjustments will be skipped.");
          }
        } else if (!isCoverageCalculated && !BG_NONE.equals(covFrom)) {
          details.setLength(0);
          details.append("Coverage could not be calculated.");
        } else if (isCoverageCalculated) {
          // engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
          if (calculatedCoverageContainer != null) {
            // engineData.put(AutomationEngineData.COVERAGE_CALCULATED,
            // calculatedCoverageContainer.getFinalCoverage());

            details.append("\n").append("Final Coverage: " + calculatedCoverageContainer.getFinalCoverage());
          }

        }

      } else

      {
        LOG.debug("NO GBG FOUND!");
      }
    }
  }

  public void computeFields(List<RevivedCMRModel> revCMRList, EntityManager entityManager, StringBuilder details) throws Exception {
    for (RevivedCMRModel revCmr : revCMRList) {
      AutomationUtil countryUtil = AutomationUtil.getNewCountryUtil(revCmr.getIssuingCountry());
      if (countryUtil != null) {
        String originalScenario = countryUtil.getOriginalScenarioForRevivedCMRs(entityManager, revCmr.getCmrNo());
        countryUtil.doCountryFieldComputationsForRevivedCMRs(entityManager, revCmr, originalScenario);
      }
    }
  }

  public List<RevivedCMRModel> parseFile(HttpServletRequest request, ParamContainer params) throws Exception {
    DiskFileItemFactory factory = new DiskFileItemFactory();

    String massUpdtDir = SystemConfiguration.getValue("MASS_UPDATE_FILES_DIR");
    String tmpDir = massUpdtDir + "/" + "revcmrtmp";
    String cmrIssuingCntry = "";
    File uploadDir = new File(tmpDir);
    if (!uploadDir.exists()) {
      uploadDir.mkdirs();
    }
    // Set factory constraints
    factory.setSizeThreshold(5000);
    factory.setRepository(uploadDir);

    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
    String extName = ".xlsx";
    String fileName = "revcmrs-test.xlsx";

    List<RevivedCMRModel> revCMRList = new ArrayList<RevivedCMRModel>();
    for (FileItem item : items) {
      if (item.isFormField() && "processTokenId".equals(item.getFieldName())) {
        params.addParam("token", item.getString());
      }
      if (!item.isFormField()) {
        if ("revivedcmrsFile".equals(item.getFieldName())) {

          String filePath = uploadDir.getAbsolutePath() + "/" + fileName;
          filePath = filePath.replaceAll("[\\\\]", "/");

          // MASS FILE | write the file
          File file = new File(filePath);

          if (file.exists()) {
            file.delete();
            // log.info("Existing mass file will be replaced.");
          }
          FileOutputStream fos = new FileOutputStream(file);
          try {
            IOUtils.copy(item.getInputStream(), fos);
          } finally {
            fos.close();
          }
          XSSFWorkbook book = new XSSFWorkbook(item.getInputStream());
          try {

            DecimalFormat formatter = new DecimalFormat("#");
            XSSFSheet sheet = book.getSheet("Data");

            if (sheet != null) {
              String fieldName = null;
              XSSFCell sheetCell = null;
              XSSFRow sheetRow = sheet.getRow(0);

              int columnIndex = 0;
              int rowIndex = 0;

              // track the column names first
              // for (Cell cell : sheetRow) {
              // sheetCell = (XSSFCell) cell;
              // fieldName = sheetCell.getStringCellValue();
              // if (!StringUtils.isBlank(fieldName)) {
              // columnMap.put(columnIndex, fieldName);
              // if (columnIndex > maxMappedCol) {
              // maxMappedCol = columnIndex;
              // }
              // }
              // columnIndex++;
              // }

              columnMap.put(0, SystemConfiguration.getSystemProperty("excelcolumn.0"));
              columnMap.put(1, SystemConfiguration.getSystemProperty("excelcolumn.1"));
              columnMap.put(2, SystemConfiguration.getSystemProperty("excelcolumn.2"));
              columnMap.put(3, SystemConfiguration.getSystemProperty("excelcolumn.3"));
              columnMap.put(4, SystemConfiguration.getSystemProperty("excelcolumn.4"));
              columnMap.put(5, SystemConfiguration.getSystemProperty("excelcolumn.5"));
              columnMap.put(6, SystemConfiguration.getSystemProperty("excelcolumn.6"));
              columnMap.put(7, SystemConfiguration.getSystemProperty("excelcolumn.7"));
              columnMap.put(8, SystemConfiguration.getSystemProperty("excelcolumn.8"));
              columnMap.put(9, SystemConfiguration.getSystemProperty("excelcolumn.9"));
              columnMap.put(10, SystemConfiguration.getSystemProperty("excelcolumn.10"));
              columnMap.put(11, SystemConfiguration.getSystemProperty("excelcolumn.11"));
              columnMap.put(12, SystemConfiguration.getSystemProperty("excelcolumn.12"));
              columnMap.put(13, SystemConfiguration.getSystemProperty("excelcolumn.13"));
              columnMap.put(14, SystemConfiguration.getSystemProperty("excelcolumn.14"));
              columnMap.put(15, SystemConfiguration.getSystemProperty("excelcolumn.15"));
              columnMap.put(16, SystemConfiguration.getSystemProperty("excelcolumn.16"));
              columnMap.put(17, SystemConfiguration.getSystemProperty("excelcolumn.17"));
              columnMap.put(18, SystemConfiguration.getSystemProperty("excelcolumn.18"));
              // massCreateFile.setColumnMap(columnMap);
              int maxMappedCol = columnMap.size();
              // parse the records
              Map<String, Object> record = null;
              String cellValue = null;
              boolean valid = false;
              int cellIndex = 0;

              for (Row row : sheet) {
                sheetRow = (XSSFRow) row;
                valid = false;
                if (rowIndex >= 1) {
                  columnIndex = 0;
                  RevivedCMRModel revCmrModel = new RevivedCMRModel();
                  for (cellIndex = 0; cellIndex <= maxMappedCol; cellIndex++) {
                    // for (Cell cell : sheetRow) {

                    sheetCell = sheetRow.getCell(cellIndex);
                    if (sheetCell == null) {
                      sheetCell = sheetRow.createCell(cellIndex);
                    }
                    if (sheetCell != null && columnIndex == 0 && !StringUtils.isBlank(sheetCell.getStringCellValue())) {
                      record = new HashMap<>();
                      valid = true;
                    }
                    if (sheetCell != null && valid) {

                      if (columnMap.get(columnIndex) != null) {
                        if ("CMR No.".equalsIgnoreCase(columnMap.get(columnIndex).split(",")[0])) {
                          revCmrModel.setCmrNo(sheetCell.getStringCellValue());
                        } else if ("Issuing Country".equalsIgnoreCase(columnMap.get(columnIndex).split(",")[0])) {
                          revCmrModel.setIssuingCountry(String.valueOf((int) sheetCell.getNumericCellValue()));
                        }
                      }

                    }

                    columnIndex++;
                  }
                  if (valid) {
                    revCMRList.add(revCmrModel);
                  }
                }
                rowIndex++;
              }
            }
          } finally {
            book.close();
          }
        }
      }
    }
    return revCMRList;
  }

  public List<CoverageContainer> getValidCoverages(List<CoverageContainer> coverages) {
    List<CoverageContainer> validCoverages = new ArrayList<CoverageContainer>();
    if (coverages != null && !coverages.isEmpty()) {
      for (CoverageContainer container : coverages) {
        if (container.getFinalCoverageRules() != null) {
          validCoverages.add(container);
        }
      }
    }
    return validCoverages;
  }

  public List<CoverageContainer> computeCoverageFromRDCQuery(EntityManager entityManager, String sqlKey, String key, String cmrIssuingCountry,
      boolean validOnly) {
    List<CoverageContainer> coverages = new ArrayList<>();
    String sql = ExternalizedQuery.getSql(sqlKey);
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KEY", key);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("COUNTRY", cmrIssuingCountry);
    String isoCntry = PageManager.getDefaultLandedCountry(cmrIssuingCountry);
    System.err.println("ISO: " + isoCntry);
    query.setParameter("ISO_CNTRY", isoCntry);
    query.setForReadOnly(true);

    LOG.debug("Getting coverages using query " + sqlKey + " under Country " + cmrIssuingCountry + " for key: " + key);
    List<Object[]> results = query.getResults(5);
    if (results != null && !results.isEmpty()) {
      for (Object[] coverage : results) {
        // 0 - base coverage
        // 1 - final coverage
        // 2 - ISU
        // 3 - CTC

        CoverageContainer container = new CoverageContainer();
        container.setFinalCoverage((String) coverage[0]);
        container.setBaseCoverage((String) coverage[0]);
        if (!StringUtils.isBlank((String) coverage[1])) {
          container.setFinalCoverage((String) coverage[1]);
        }

        List<Rule> rule = coverageRules.findRule(container.getFinalCoverage());
        container.setFinalCoverageRules(rule);
        rule = coverageRules.findRule(container.getBaseCoverage());
        container.setBaseCoverageRules(rule);

        container.setIsuCd((String) coverage[2]);
        container.setClientTierCd((String) coverage[3]);

        if (validOnly && container.getFinalCoverageRules() != null) {
          coverages.add(container);
        } else {
          coverages.add(container);
        }
      }
    }

    return coverages;

  }

  public static synchronized void initCoverageRules() throws Exception {
    // determine ruleset
    String ruleSetId = SystemParameters.getString("COV_RULES_ID");
    if (StringUtils.isBlank(ruleSetId)) {
      Date dt = new Date();
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(dt);
      int month = cal.get(Calendar.MONTH);
      SimpleDateFormat yrFormatter = new SimpleDateFormat("yyyy");
      ruleSetId = (month < 7 ? "1" : "2") + "H" + yrFormatter.format(dt);
    }
    LOG.debug("Initializing Coverage Rules for " + ruleSetId);

    coverageRules = new CoverageRules(ruleSetId);
    // LOG.debug("JarProperties jar country dir: " +
    // COV_RULES_PROPS.getString("jar.zip.dir.root"));
    String zipFile = JarProperties.getProperty("jar.zip.dir.root");
    zipFile = zipFile + File.separator + ruleSetId + ".zip";
    File zip = new File(zipFile);
    if (!zip.exists()) {
      zipFile = JarProperties.getProperty("jar.zip.dir.root");
      zipFile = zipFile + File.separator + ruleSetId + ".jar";
    }
    coverageRules.initializeFrom(zipFile);
  }

  private CoverageInput extractCoverageInput(EntityManager entityManager, RequestData requestData, Data data, Addr addr, String gbgId, String bgId)
      throws Exception {
    CoverageInput coverage = new CoverageInput();

    coverage.setAffiliateGroup(data.getAffiliate());
    coverage.setClassification(data.getCustClass());
    coverage.setCompanyNumber(data.getCompany());
    coverage.setSegmentation("IBM".equals(data.getCmrOwner()) ? null : data.getCmrOwner());
    String isoCntryCd = SystemUtil.getISOCountryCode(data.getCmrIssuingCntry());
    LOG.debug("System Location: " + data.getCmrIssuingCntry() + " ISO Code: " + isoCntryCd);
    coverage.setCountryCode(isoCntryCd != null ? isoCntryCd : data.getCmrIssuingCntry());
    coverage.setDb2ID(data.getCmrNo());
    coverage.setEnterprise(data.getEnterprise());
    coverage.setGbQuadSectorTier(data.getClientTier());
    coverage.setInac(data.getInacCd());
    coverage.setIndustryClass(data.getSubIndustryCd());
    coverage
        .setIndustryCode(data.getSubIndustryCd() != null && data.getSubIndustryCd().length() > 0 ? data.getSubIndustryCd().substring(0, 1) : null);
    coverage.setIndustrySolutionUnit(data.getIsuCd());
    coverage.setNationalTaxID(data.getTaxCd1());
    coverage.setSORTL(data.getSearchTerm());
    coverage.setUnISIC(data.getIsicCd());
    coverage.setSitePartyID(data.getSitePartyId());
    coverage.setCity(addr.getCity1());
    coverage.setCounty(addr.getCounty());
    coverage.setPostalCode(addr.getPostCd());
    if (!"''".equals(addr.getStateProv())) {
      coverage.setStatePrefectureCode(addr.getStateProv());
    }
    coverage.setPhysicalAddressCountry(addr.getLandCntry());

    coverage.setGlobalBuyingGroupID(gbgId);
    coverage.setDomesticBuyingGroupID(bgId);
    coverage.setGeoLocationCode(data.getGeoLocationCd());

    GEOHandler geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    if (geoHandler != null) {
      geoHandler.convertCoverageRulesInput(entityManager, coverage, addr, requestData.createModelFromRequest());
    }

    return coverage;
  }

  private List<CoverageContainer> getCoverageRuleForID(String coverageId) {
    List<CoverageContainer> coverages = new ArrayList<>();
    List<Rule> rule = coverageRules.findRule(coverageId);
    CoverageContainer container = new CoverageContainer();
    container.setFinalCoverage(coverageId);
    container.setFinalCoverageRules(rule);
    coverages.add(container);
    return coverages;
  }

  private String getDefaultCoverage(String cmrIssuingCntry) throws Exception {
    CoverageInput input = new CoverageInput();
    String isoCntryCd = SystemUtil.getISOCountryCode(cmrIssuingCntry);
    LOG.debug("System Location: " + cmrIssuingCntry + " ISO Code: " + isoCntryCd);
    input.setCountryCode(isoCntryCd != null ? isoCntryCd : cmrIssuingCntry);
    List<Coverage> coverages = coverageRules.findCoverage(input);
    if (coverages != null && !coverages.isEmpty()) {
      Coverage coverage = coverages.get(0);
      return (coverage.getType() + coverage.getId());
    }
    return null;
  }

  private class GBGComparator implements Comparator<GBGResponse> {

    private String landedCountry;

    public GBGComparator(String landedCountry) {
      this.landedCountry = landedCountry;
    }

    @Override
    public int compare(GBGResponse o1, GBGResponse o2) {
      // matched cmrs on the country
      if (this.landedCountry.equals(o1.getCountry()) && !this.landedCountry.equals(o2.getCountry())) {
        return -1;
      }
      if (!this.landedCountry.equals(o1.getCountry()) && this.landedCountry.equals(o2.getCountry())) {
        return 1;
      }
      // cmr count
      if (o1.getCmrCount() > o2.getCmrCount()) {
        return -1;
      }
      if (o1.getCmrCount() < o2.getCmrCount()) {
        return 1;
      }

      // Null pointer exception encountered. when comparing using this.
      if (StringUtils.isNotBlank(o1.getLdeRule()) && StringUtils.isNotBlank(o2.getLdeRule())) {
        // rule is country specific
        if (o1.getLdeRule().contains(this.landedCountry) && !o2.getLdeRule().contains(this.landedCountry)) {
          return -1;
        }
        if (!o1.getLdeRule().contains(this.landedCountry) && o2.getLdeRule().contains(this.landedCountry)) {
          return 1;
        }
      }

      return o1.getBgId().compareTo(o2.getBgId());
    }

  }

  public void exportToExcel(List<RevivedCMRModel> revCMRList, HttpServletResponse response)
      throws IOException, ParseException, IllegalArgumentException, IllegalAccessException {

    // if (config == null) {
    // initConfig();
    // }
    String columnName = null;
    Object rawValue = null;
    // List<AutomationStatsModel> stats = container.getAutomationRecords();
    LOG.info("Exporting records to excel..");
    XSSFWorkbook report = new XSSFWorkbook();
    try {
      XSSFSheet sheet = report.createSheet("Revived CMRs");

      Drawing drawing = sheet.createDrawingPatriarch();
      CreationHelper helper = report.getCreationHelper();

      XSSFFont bold = report.createFont();
      bold.setBold(true);
      bold.setFontHeight(10);
      XSSFCellStyle boldStyle = report.createCellStyle();
      boldStyle.setFont(bold);

      XSSFFont regular = report.createFont();
      regular.setFontHeight(10);
      XSSFCellStyle regularStyle = report.createCellStyle();
      regularStyle.setFont(regular);
      regularStyle.setWrapText(true);
      regularStyle.setVerticalAlignment(VerticalAlignment.TOP);

      // StatXLSConfig sc = null;
      // for (int i = 0; i < config.size(); i++) {
      // sc = config.get(i);
      // sheet.setColumnWidth(i, sc.getWidth() * 256);
      // }
      // create title
      // XSSFRow titleRow = sheet.createRow(0);
      // XSSFCell cell = titleRow.createCell(0);
      // cell.setCellStyle(boldStyle);
      // String title = "Automation Statistics";
      // // if (model != null) {
      // // title = "Automation Statistics from " + model.getDateFrom() + " to "
      // +
      // // model.getDateTo();
      // // if (!StringUtils.isEmpty(model.getCountry())) {
      // // title += " for " + model.getCountry();
      // // } else {
      // // if (!StringUtils.isEmpty(model.getGroupByProcCenter())) {
      // // title += " for " + model.getGroupByProcCenter();
      // // }
      // // if (!StringUtils.isEmpty(model.getGroupByGeo())) {
      // // title += " (" + model.getGroupByGeo() + ")";
      // // }
      // // }
      // // }
      // cell.setCellValue(title);

      // create headers
      XSSFRow headerRow = sheet.createRow(0);
      XSSFCell cell = headerRow.createCell(0);
      for (int i : columnMap.keySet()) {
        columnName = columnMap.get(i).split(",")[0];
        int width = Integer.valueOf(columnMap.get(i).split(",")[1]);
        sheet.setColumnWidth(i, width * 256);
        cell = headerRow.createCell(i++);
        cell.setCellStyle(boldStyle);
        cell.setCellValue(columnName);
      }
      // createHeaders(header, boldStyle, drawing, config, helper);

      List<List<String>> rawValues = new ArrayList<List<String>>();

      for (RevivedCMRModel revivedCMRModel : revCMRList) {
        List<String> strValues = new ArrayList<String>();
        strValues.add(revivedCMRModel.getCmrNo());
        strValues.add(revivedCMRModel.getIssuingCountry());
        strValues.add(revivedCMRModel.getGbgId());
        strValues.add(revivedCMRModel.getBgId());
        strValues.add(revivedCMRModel.getCmrCount());
        strValues.add(revivedCMRModel.getLdeRule());
        strValues.add(revivedCMRModel.getIntAcctType());
        strValues.add(revivedCMRModel.getGuDunsNo());
        strValues.add(revivedCMRModel.getDunsNo());
        strValues.add(revivedCMRModel.getFinalCoverage());
        strValues.add(revivedCMRModel.getCsoSite());
        strValues.add(revivedCMRModel.getPccArDept());
        strValues.add(revivedCMRModel.getMtkgArDept());
        strValues.add(revivedCMRModel.getMktgDept());
        strValues.add(revivedCMRModel.getSvcArOffice());
        strValues.add(revivedCMRModel.getIsuCd());
        strValues.add(revivedCMRModel.getClientTier());
        strValues.add(revivedCMRModel.getIsicCd());
        strValues.add(revivedCMRModel.getSubIndustryCd());
        strValues.add(revivedCMRModel.getUsSicmen());
        rawValues.add(strValues);
      }

      String[][] rawValuesArr = new String[rawValues.size()][];
      for (int i = 0; i < rawValues.size(); i++) {
        List<String> row = rawValues.get(i);
        rawValuesArr[i] = row.toArray(new String[row.size()]);
      }
      // add the data
      XSSFRow dataRow = null;
      int current = 1;

      for (String[] data : rawValuesArr) {
        dataRow = sheet.createRow(current++);

        int columnCount = 0;

        for (String field : data) {
          cell = dataRow.createCell(columnCount++);
          cell.setCellValue(field);
        }

      }

      String type = "application/octet-stream";
      String fileName = "RevCMR-results";
      // if (model != null) {
      // fileName += StringUtils.replace(model.getDateFrom(), "-", "");
      // fileName += "-";
      // fileName += StringUtils.replace(model.getDateTo(), "-", "");
      // if (!StringUtils.isEmpty(model.getCountry())) {
      // fileName += "_" + model.getCountry();
      // } else {
      // if (!StringUtils.isEmpty(model.getGroupByProcCenter())) {
      // fileName += "_" + model.getGroupByProcCenter();
      // }
      // if (!StringUtils.isBlank(model.getGroupByGeo())) {
      // fileName += "_" + model.getGroupByGeo();
      // }
      // }
      // }
      if (response != null) {
        response.setContentType(type);
        response.addHeader("Content-Type", type);
        response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"");
        report.write(response.getOutputStream());
      } else {
        FileOutputStream fos = new FileOutputStream("C:/" + fileName + ".xlsx");
        try {
          report.write(fos);
        } finally {
          fos.close();
        }
      }
    } finally {
      report.close();
    }
  }
}
