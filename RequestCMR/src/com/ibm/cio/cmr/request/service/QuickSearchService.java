package com.ibm.cio.cmr.request.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.requestentry.RequestEntryController;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.ScorecardPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.service.requestentry.ImportCMRService;
import com.ibm.cio.cmr.request.service.requestentry.ImportDnBService;
import com.ibm.cio.cmr.request.service.requestentry.RequestEntryService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;

/**
 *
 * @author JeffZAMORA
 *
 */
@Component
public class QuickSearchService extends BaseSimpleService<RequestEntryModel> {

  private static final Logger LOG = Logger.getLogger(QuickSearchService.class);

  @Override
  protected RequestEntryModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    CompanyRecordModel model = (CompanyRecordModel) params.getParam("model");
    if (model == null) {
      throw new Exception("Model is null");
    }

    if (CompanyRecordModel.REC_TYPE_CMR.equals(model.getRecType())) {
      // CMR was chosen
      return importCMR(entityManager, request, model);
    } else if (CompanyRecordModel.REC_TYPE_DNB.equals(model.getRecType())) {
      // D&B record was chosen
      return importDNB(entityManager, request, model);
    } else if ("X".equals(model.getReqType())) {
      // create from scratch
      return createNewRecord(entityManager, request, model);
    }

    return null;
  }

  /**
   * Calls the {@link ImportCMRService} internally via
   * {@link RequestEntryController} and imports the CMR as a create by model or
   * update request
   *
   * @param entityManager
   * @param request
   * @param model
   * @return
   * @throws Exception
   */
  private RequestEntryModel importCMR(EntityManager entityManager, HttpServletRequest request, CompanyRecordModel model) throws Exception {

    RequestEntryController controller = new RequestEntryController();

    // check CMR Details for OB 93
    FindCMRResultModel result = CompanyFinder.getCMRDetails(model.getIssuingCntry(), model.getCmrNo(), 5, null, null);
    if (result != null && result.getItems() != null && !result.getItems().isEmpty() && !model.isPoolRecord()) {
      Collections.sort(result.getItems());
      if ("93".equals(result.getItems().get(0).getCmrOrderBlock())) {
        throw new CmrException(new Exception(
            "This CMR is marked as Inactive/Logically Deleted. For Reactivation, pls import the CMR using CMR Search from the request details page."));
      }
    }

    RequestEntryModel reqModel = new RequestEntryModel();
    reqModel.setReqType(model.getReqType());
    reqModel.setCmrIssuingCntry(model.getIssuingCntry());
    controller.setDefaultValues(reqModel, request);
    if (model.getCmrNo() != null && model.getCmrNo().startsWith("P")) {
      reqModel.setProspLegalInd("Y");
    }
    if (SystemLocation.JAPAN.equals(model.getIssuingCntry())) {
      reqModel.setCustType("CEA");
    }
    reqModel.setCountryUse(model.getSubRegion());
    reqModel.setOverrideReqId(model.getOverrideReqId());

    ImportCMRModel importModel = new ImportCMRModel();
    importModel.setAddressOnly(false);
    importModel.setCmrIssuingCntry(model.getIssuingCntry());
    /*
     * if (SystemLocation.ISRAEL.equals(model.getIssuingCntry())) {
     * importModel.setSearchIssuingCntry(SystemLocation.SAP_ISRAEL_SOF_ONLY); }
     */
    importModel.setCmrNum(model.getCmrNo());
    importModel.setSystem("cmr");
    // check added for external service
    if (!StringUtils.isBlank(model.getName())) {
      importModel.setQuickSearchData(formatSearchParams(model));
    }
    importModel.setPoolRecord(model.isPoolRecord());

    LOG.debug("Creating request from CMR " + model.getCmrNo() + " under " + model.getIssuingCntry());
    ModelAndView mv = controller.importCMRs(request, null, importModel, reqModel);
    if (mv != null) {
      Boolean success = (Boolean) mv.getModelMap().get("success");
      if (success != null && success) {
        Long reqId = (Long) mv.getModelMap().get("internalReqId");
        reqModel.setReqId(reqId != null ? reqId : 0);

        entityManager.flush();
        ScorecardPK pk = new ScorecardPK();
        pk.setReqId(reqId);
        Scorecard scorecard = entityManager.find(Scorecard.class, pk);
        if (scorecard != null) {
          AppUser user = AppUser.getUser(request);
          if (model.isHasDnb()) {
            scorecard.setFindDnbResult(CmrConstants.RESULT_REJECTED);
            scorecard.setFindDnbRejReason("Record Not Found");
            scorecard.setFindDnbRejCmt("CMR Results from Quick Search did not contain the required company details.");
            entityManager.merge(scorecard);
          } else if (!model.isHasDnb() && model.isHasCmr() && (model.getCmrNo() != null && model.getCmrNo().startsWith("P"))
              && !SystemLocation.CHINA.equals(model.getIssuingCntry())) {
            scorecard.setFindDnbResult(CmrConstants.DNBSEARCH_NOT_DONE);
          } else if (model.getMatchGrade() != null && Arrays.asList("F4", "F5", "VAT").contains(model.getMatchGrade())) {
            scorecard.setFindDnbResult(CmrConstants.RESULT_NO_RESULT);
          } else {
            scorecard.setFindDnbResult("Not Required");
          }
          scorecard.setFindDnbTs(SystemUtil.getActualTimestamp());
          scorecard.setFindDnbUsrId(user.getIntranetId());
          scorecard.setFindDnbUsrNm(user.getBluePagesName());
          entityManager.merge(scorecard);
          entityManager.flush();
        }

        return reqModel;
      } else {
        String error = (String) mv.getModel().get("errorMessage");
        throw new CmrException(new Exception(error != null ? error : "The CMR cannot be imported at this time. Please try again later"));
      }
    } else {
      throw new Exception("The CMR cannot be imported at this time. Please try again later");
    }

  }

  /**
   * Imports a D&B record using the {@link ImportDnBService} and creates a new
   * request
   *
   * @param entityManager
   * @param request
   * @param model
   * @return
   * @throws Exception
   */
  private RequestEntryModel importDNB(EntityManager entityManager, HttpServletRequest request, CompanyRecordModel model) throws Exception {

    // covert from D&B data to FindCMR model
    FindCMRResultModel results = new FindCMRResultModel();
    FindCMRRecordModel cmrRecord = DnBUtil.extractRecordFromDnB(model.getIssuingCntry(), model.getDunsNo(), model.getPostCd());

    results.setItems(new ArrayList<FindCMRRecordModel>());
    results.getItems().add(cmrRecord);

    // build extra params
    ParamContainer params = new ParamContainer();
    RequestEntryController controller = new RequestEntryController();
    RequestEntryModel reqModel = new RequestEntryModel();
    reqModel.setReqType(model.getReqType());
    reqModel.setCmrIssuingCntry(model.getIssuingCntry());
    reqModel.setVat(cmrRecord.getCmrVat());
    reqModel.setTaxCd1(cmrRecord.getCmrBusinessReg());
    controller.setDefaultValues(reqModel, request);
    reqModel.setCountryUse(model.getSubRegion());

    params.addParam("reqId", new Long(0));
    params.addParam("results", results);
    params.addParam("system", "dnb");
    params.addParam("model", reqModel);

    String quickSearchData = formatSearchParams(model);
    params.addParam("quickSearchData", quickSearchData);
    // call the service
    ImportDnBService dnbService = new ImportDnBService();
    ImportCMRModel retModel = dnbService.process(request, params);
    if (retModel != null && retModel.getReqId() > 0) {
      reqModel.setReqId(retModel.getReqId());
      ScorecardPK pk = new ScorecardPK();
      pk.setReqId(retModel.getReqId());
      AppUser user = AppUser.getUser(request);
      Scorecard scorecard = entityManager.find(Scorecard.class, pk);
      if (!model.isHasCmr()) {
        scorecard.setFindCmrResult(CmrConstants.RESULT_NO_RESULT);
      } else {
        scorecard.setFindCmrResult(CmrConstants.RESULT_REJECTED);
        scorecard.setFindCmrRejReason("Record Not Found");
        scorecard.setFindCmrRejCmt("CMR Results from Quick Search did not contain the required company details.");
      }
      scorecard.setFindCmrTs(SystemUtil.getActualTimestamp());
      scorecard.setFindCmrUsrId(user.getIntranetId());
      scorecard.setFindCmrUsrNm(user.getBluePagesName());
      entityManager.merge(scorecard);
      return reqModel;
    }
    return null;

  }

  /**
   * Creates a brand new record from scratch using the address information
   * supplied by users
   *
   * @param entityManager
   * @param request
   * @param model
   * @return
   * @throws Exception
   */
  private RequestEntryModel createNewRecord(EntityManager entityManager, HttpServletRequest request, CompanyRecordModel model) throws Exception {
    // create the request first
    LOG.debug("Creating the request..");
    RequestEntryController controller = new RequestEntryController();
    RequestEntryModel reqEntryModel = new RequestEntryModel();
    reqEntryModel.setState(BaseModel.STATE_NEW);
    reqEntryModel.setAction(CmrConstants.Save().toString());
    reqEntryModel.setReqType("C");
    reqEntryModel.setCmrIssuingCntry(model.getIssuingCntry());
    reqEntryModel.setMainAddrType("ZS01");
    reqEntryModel.setVat(model.getVat());
    reqEntryModel.setTaxCd1(model.getTaxCd1());
    controller.setDefaultValues(reqEntryModel, request);
    reqEntryModel.setCountryUse(model.getSubRegion());

    GEOHandler geoHandler = RequestUtils.getGEOHandler(model.getIssuingCntry());
    if (geoHandler != null) {
      int splitLength = geoHandler.getName1Length();
      int splitLength2 = geoHandler.getName2Length();
      String[] parts = geoHandler.doSplitName(model.getName(), "", splitLength, splitLength2);
      reqEntryModel.setMainCustNm1(parts[0]);
      reqEntryModel.setMainCustNm2(parts[1]);
    } else {
      reqEntryModel.setMainCustNm1(model.getName());
    }

    RequestEntryService reqService = new RequestEntryService();
    reqService.performSave(reqEntryModel, entityManager, request, false);

    long reqId = reqEntryModel.getReqId();
    LOG.debug("Request ID " + reqId + " generated.");
    entityManager.flush();

    // add a sold to address from the information
    LOG.debug("Adding the main address..");
    AddressService addrService = new AddressService();
    AddressModel addrModel = new AddressModel();
    addrModel.setReqId(reqId);
    addrModel.setLandCntry(model.getCountryCd());
    addrModel.setAddrTxt(model.getStreetAddress1() != null ? model.getStreetAddress1() : null);
    addrModel.setAddrTxt2(model.getStreetAddress2() != null ? model.getStreetAddress2() : null);
    addrModel.setCity1(model.getCity() != null ? model.getCity() : null);
    addrModel.setStateProv(model.getStateProv());
    addrModel.setPostCd(model.getPostCd());
    addrModel.setState(BaseModel.STATE_NEW);
    addrModel.setAction("ADD_ADDRESS");

    addrModel.setAddrType(CmrConstants.ADDR_TYPE.ZS01.toString());
    handleCountrySpecificTypesForLocalLanguage(model, addrModel);

    addrModel.setCmrIssuingCntry(model.getIssuingCntry());
    if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
      addrModel.setCustNm1(reqEntryModel.getMainCustNm1());
      addrModel.setCustNm2(reqEntryModel.getMainCustNm2());
    }
    addrService.performTransaction(addrModel, entityManager, request);
    entityManager.flush();

    // update the scorecard
    LOG.debug("Updating scorecard details..");
    ScorecardPK pk = new ScorecardPK();
    pk.setReqId(reqId);
    AppUser user = AppUser.getUser(request);
    Scorecard scorecard = entityManager.find(Scorecard.class, pk);
    String rejectComment = "CMR Results from Quick Search did not contain the required company details.";
    if (!model.isHasCmr()) {
      scorecard.setFindCmrResult(CmrConstants.RESULT_NO_RESULT);
    } else {
      scorecard.setFindCmrResult(CmrConstants.RESULT_REJECTED);
      scorecard.setFindCmrRejReason("Record Not Found");
      scorecard.setFindCmrRejCmt(rejectComment);
    }
    scorecard.setFindCmrTs(SystemUtil.getActualTimestamp());
    scorecard.setFindCmrUsrId(user.getIntranetId());
    scorecard.setFindCmrUsrNm(user.getBluePagesName());

    if (!model.isHasDnb()) {
      if ((model.getCmrNo() != null && model.getCmrNo().startsWith("P")) && !SystemLocation.CHINA.equals(model.getIssuingCntry())) {
        scorecard.setFindDnbResult(CmrConstants.DNBSEARCH_NOT_DONE);
      } else {
        scorecard.setFindDnbResult(CmrConstants.RESULT_NO_RESULT);
      }
    } else {
      scorecard.setFindDnbResult(CmrConstants.RESULT_REJECTED);
      scorecard.setFindDnbRejReason("Record Not Found");
      scorecard.setFindDnbRejCmt(rejectComment);
    }
    scorecard.setFindDnbTs(SystemUtil.getActualTimestamp());
    scorecard.setFindDnbUsrId(user.getIntranetId());
    scorecard.setFindDnbUsrNm(user.getBluePagesName());

    entityManager.merge(scorecard);

    ReqCmtLog cmt = new ReqCmtLog();
    ReqCmtLogPK cmtPk = new ReqCmtLogPK();
    cmtPk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    cmt.setId(cmtPk);
    cmt.setReqId(reqId);
    cmt.setCmt("Request created from Quick Search details. CMR and D&B search results were recorded accordingly. Search Params:\n"
        + formatSearchParams(model));
    // save cmtlockedIn as Y default for current realese
    cmt.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    cmt.setCreateById("CreateCMR");
    cmt.setCreateByNm("CreateCMR");
    // set createTs as current timestamp and updateTs same as CreateTs
    cmt.setCreateTs(SystemUtil.getCurrentTimestamp());
    cmt.setUpdateTs(cmt.getCreateTs());

    entityManager.persist(cmt);

    entityManager.flush();

    return reqEntryModel;
  }

  /**
   * Handles the country-specific address types to set if the address
   * information is in latin/non latin
   *
   * @param model
   * @param addrModel
   */
  private void handleCountrySpecificTypesForLocalLanguage(CompanyRecordModel model, AddressModel addrModel) {
    // ISRAEL - non-latin = ZS01, latin = CTYA
    if (SystemLocation.ISRAEL.equals(model.getIssuingCntry()) || SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(model.getIssuingCntry())) {
      if (CompanyFinder.isLatin(model.getName())) {
        addrModel.setAddrType("CTYA");
      }
    }

    // GREECE - non-latin ZP01
    if (SystemLocation.GREECE.equals(model.getIssuingCntry())) {
      if (!CompanyFinder.isLatin(model.getName())) {
        addrModel.setAddrType("ZP01");
      }
    }

    // G addresses
    if (Arrays.asList("358", "359", "363", "603", "607", "626", "644", "651", "668", "693", "694", "695", "699", "704", "705", "707", "708", "740",
        "741", "787", "820", "821", "826", "889", "620", "642", "677", "680", "752", "762", "767", "768", "772", "805", "808", "823", "832", "849",
        "850", "865").contains(model.getIssuingCntry())) {
      if (!CompanyFinder.isLatin(model.getName())) {
        addrModel.setAddrType("ZP02");
      }

    }
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

  private String formatSearchParams(CompanyRecordModel searchParams) {
    StringBuilder sb = new StringBuilder();
    sb.append(searchParams.getName().toUpperCase());
    if (!StringUtils.isBlank(searchParams.getStreetAddress1())) {
      sb.append("\n" + searchParams.getStreetAddress1().toUpperCase());
    }
    if (!StringUtils.isBlank(searchParams.getStreetAddress2())) {
      sb.append("\n" + searchParams.getStreetAddress2().toUpperCase());
    }
    if (!StringUtils.isBlank(searchParams.getCity())) {
      sb.append("\n" + searchParams.getCity().toUpperCase());
      if (!StringUtils.isBlank(searchParams.getStateProv())) {
        sb.append(", " + searchParams.getStateProv());
      }
    }
    sb.append("\n" + searchParams.getCountryCd());
    if (!StringUtils.isBlank(searchParams.getPostCd())) {
      sb.append(" " + searchParams.getPostCd().toUpperCase());
    }
    if (!StringUtils.isBlank(searchParams.getVat())) {
      sb.append("\nVAT: " + searchParams.getVat().toUpperCase());
    }
    if (!StringUtils.isBlank(searchParams.getTaxCd1())) {
      String[] cRNCntries = { "IE", "GB" };
      List<String> cRNCntriesList = Arrays.asList(cRNCntries);
      String label = "SIRET: ";
      if (cRNCntriesList.contains(searchParams.getCountryCd())) {
        label = "CRN: ";
      }
      sb.append("\n" + label + searchParams.getTaxCd1().toUpperCase());
    }
    return sb.toString();
  }

}
