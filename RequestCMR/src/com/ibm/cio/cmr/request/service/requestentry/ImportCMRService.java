/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrInternalTypes;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.ProlifChecklist;
import com.ibm.cio.cmr.request.entity.ProlifChecklistPK;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.ScorecardPK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.CheckListModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class ImportCMRService extends BaseSimpleService<ImportCMRModel> {

  private static final Logger LOG = Logger.getLogger(ImportCMRService.class);

  @Autowired
  RequestEntryService reqEntryService;

  @Autowired
  AddressService addressService;

  private boolean autoEngineProcess;

  @Override
  public ImportCMRModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {

    if (this.reqEntryService == null) {
      this.reqEntryService = new RequestEntryService();
    }
    if (this.addressService == null) {
      this.addressService = new AddressService();
    }
    AppUser user = AppUser.getUser(request);
    Long reqId = (Long) params.getParam("reqId");
    String system = (String) params.getParam("system");
    RequestEntryModel reqModel = (RequestEntryModel) params.getParam("model");
    ImportCMRModel searchModel = (ImportCMRModel) params.getParam("searchModel");

    GEOHandler geoHandler = null;
    if (searchModel != null && searchModel.isAddressOnly()) {
      geoHandler = RequestUtils.getGEOHandler(searchModel.getCmrIssuingCntry());
      if (geoHandler == null) {
        geoHandler = new DefaultImportAddressHandler();
      }
      return geoHandler.handleImportAddress(entityManager, request, params, searchModel);
    }
    Boolean skipAddress = (Boolean) params.getParam("skipAddress");
    if (skipAddress == null) {
      skipAddress = false;
    }
    FindCMRResultModel result = (FindCMRResultModel) params.getParam("results");
    if (result.getItems().size() > 0) {
      Collections.sort(result.getItems()); // have main record on top
    }

    FindCMRRecordModel mainRecord = result.getItems().size() > 0 ? result.getItems().get(0) : null;
    String mainCustNm1 = "";
    String mainCustNm2 = "";
    String prospectSeqNum = "";
    if (mainRecord != null) {
      mainCustNm1 = mainRecord.getCmrName1Plain();
      mainCustNm2 = mainRecord.getCmrName2Plain();
      if (CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock())) {
        prospectSeqNum = mainRecord.getCmrAddrSeq();
      }
    }

    geoHandler = convertResults(entityManager, reqModel, result, searchModel);

    mainRecord = result.getItems().size() > 0 ? result.getItems().get(0) : null;
    long reqIdToUse = reqId;
    boolean newRequest = false;
    String cmrNo = "";

    boolean endTransaction = false;
    EntityTransaction transaction = entityManager.getTransaction();
    if (transaction == null || !transaction.isActive()) {
      transaction.begin();
      endTransaction = true;
    }
    try {

      if (reqId <= 0) {
        // generate a Request ID for new requests
        if (reqModel.getOverrideReqId() > 0) {
          LOG.debug("Override Req ID specified: " + reqModel.getOverrideReqId());
          reqIdToUse = reqModel.getOverrideReqId();
        } else {
          reqIdToUse = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "REQ_ID");
        }
        newRequest = true;
      } else {
        if (mainRecord != null) {
          if (!skipAddress) {
            // delete only when there are records found
            deleteAddrRecords(entityManager, reqIdToUse, false);
            deleteAddrRecords(entityManager, reqIdToUse, true);
          } else {
            LOG.debug("Skipping address import.");
          }
        }
      }

      Admin admin = null;
      Data data = null;
      Scorecard scorecard = null;

      if (!newRequest) {
        // get the current records
        RequestEntryModel model = new RequestEntryModel();
        model.setReqId(reqIdToUse);
        CompoundEntity entity = getCurrentRecord(model, entityManager, request);
        admin = entity.getEntity(Admin.class);
        data = entity.getEntity(Data.class);
        scorecard = entity.getEntity(Scorecard.class);

        savePageData(reqModel, admin, data);

      } else {
        // create/update the records
        admin = new Admin();
        AdminPK adminPK = new AdminPK();
        adminPK.setReqId(reqIdToUse);
        admin.setId(adminPK);

        data = new Data();
        DataPK dataPK = new DataPK();
        dataPK.setReqId(reqIdToUse);
        data.setId(dataPK);
        if (geoHandler != null) {
          geoHandler.setDataDefaultsOnCreate(data, entityManager);
        }

        savePageData(reqModel, admin, data);
        setAdminDefaults(admin, user, request);
        if (geoHandler != null) {
          geoHandler.setAdminDefaultsOnCreate(admin);
        }

        scorecard = new Scorecard();
        ScorecardPK scorecardPK = new ScorecardPK();
        scorecardPK.setReqId(reqIdToUse);
        scorecard.setId(scorecardPK);
        setScorecardDefaults(scorecard);
      }

      if (searchModel.isPoolRecord()) {
        geoHandler.setPoolProcessing(true);
      }

      if (mainRecord != null) {
        cmrNo = mainRecord.getCmrNum();

        // update data
        loadRecordToData(geoHandler, result, mainRecord, admin, data);
        if (CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock())) {
          // convert Prospect to Legal CMR
          admin.setReqType("C");
          admin.setProspLegalInd(CmrConstants.YES_NO.Y.toString());
          admin.setDelInd(null);
          admin.setModelCmrNo(null);

          if (CmrConstants.LA_COUNTRIES.contains(data.getCmrIssuingCntry())) {
            data.setProspectSeqNo(prospectSeqNum);
          }
          String sysLoc = StringUtils.isEmpty(searchModel.getSearchIssuingCntry()) ? searchModel.getCmrIssuingCntry()
              : searchModel.getSearchIssuingCntry();
          String desc = DropdownListController.getDescription("CMRIssuingCntry", sysLoc, sysLoc, false);
          if (StringUtils.isEmpty(desc)) {
            desc = sysLoc;
          }
          String comment = "Prospect record imported from CMR# " + cmrNo + " from System Location " + desc + ".";
          if (!StringUtils.isBlank(searchModel.getQuickSearchData())) {
            comment = "Prospect record imported from CMR# " + cmrNo + " from System Location " + desc + " via Quick Search. Search Params:\n"
                + searchModel.getQuickSearchData();
          }
          createCommentLog(reqEntryService, entityManager, "CreateCMR", reqIdToUse, comment);
        } else if (!"U".equals(reqModel.getReqType()) && !"X".equals(reqModel.getReqType())) {
          // create by model
          data.setCmrNo(null);
          admin.setReqType("C");
          admin.setDelInd(CmrConstants.YES_NO.Y.toString());
          admin.setModelCmrNo(cmrNo);
          if (geoHandler != null) {
            geoHandler.handleImportByType(reqModel.getReqType(), admin, data, true);
          } else {
            // no converter, use global logic
            admin.setCustType(null);
            data.setCustGrp(CmrConstants.CREATE_BY_MODEL_GROUP);
            data.setCustSubGrp(CmrConstants.CREATE_BY_MODEL_SUB_GROUP);
          }

          String sysLoc = StringUtils.isEmpty(searchModel.getSearchIssuingCntry()) ? searchModel.getCmrIssuingCntry()
              : searchModel.getSearchIssuingCntry();
          String desc = DropdownListController.getDescription("CMRIssuingCntry", sysLoc, sysLoc, false);
          if (StringUtils.isEmpty(desc)) {
            desc = sysLoc;
          }
          String comment = "CMR# " + cmrNo + " imported into the request from System Location " + desc + ".";
          if (!StringUtils.isBlank(searchModel.getQuickSearchData())) {
            comment = "CMR# " + cmrNo + " imported into the request from System Location " + desc + " via Quick Search. Search Params:\n"
                + searchModel.getQuickSearchData();
          }
          createCommentLog(reqEntryService, entityManager, "CreateCMR", reqIdToUse, comment);
        } else if ("U".equals(reqModel.getReqType()) || "X".equals(reqModel.getReqType())) {

          // clear the group and sub group first
          admin.setDelInd(null);
          admin.setModelCmrNo(null);

          if (geoHandler != null) {
            geoHandler.handleImportByType(reqModel.getReqType(), admin, data, true);
          } else {
            // no converter, use global logic
            data.setCustGrp(null);
            data.setCustSubGrp(null);
          }
          String sysLoc = StringUtils.isEmpty(searchModel.getSearchIssuingCntry()) ? searchModel.getCmrIssuingCntry()
              : searchModel.getSearchIssuingCntry();
          String desc = DropdownListController.getDescription("CMRIssuingCntry", sysLoc, sysLoc, false);
          if (StringUtils.isEmpty(desc)) {
            desc = sysLoc;
          }
          String comment = "CMR# " + cmrNo + " imported into the request from System Location " + desc + ".";
          if (!StringUtils.isBlank(searchModel.getQuickSearchData())) {
            comment = "CMR# " + cmrNo + " imported into the request from System Location " + desc + " via Qiuck Search. Search Params:\n"
                + searchModel.getQuickSearchData();
          }
          createCommentLog(reqEntryService, entityManager, "CreateCMR", reqIdToUse, comment);
        }
        // update admin
        if (!skipAddress) {
          admin.setMainAddrType(mainRecord.getCmrAddrTypeCode());

          if (mainCustNm1 != null) {
            admin.setMainCustNm1(mainCustNm1);
            admin.setMainCustNm2(mainCustNm2);
          }
        } else {
          LOG.debug("Skipping name assignment, address import skipped.");
        }

        if (geoHandler != null) {
          geoHandler.setAdminValuesOnImport(admin, mainRecord);
        }
      }

      saveScorecard(user, mainRecord, scorecard, system);
      // Ed|1043386| Only require DPL check for Create requests
      if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(admin.getReqType())) {
        scorecard.setDplChkResult(CmrConstants.Scorecard_Not_Done);
      } else {
        // Dennis: 1557838 : For CN, will need to require DPL check even for
        // Update requests
        if (CNHandler.isCNIssuingCountry(data.getCmrIssuingCntry())) {
          scorecard.setDplChkResult(CmrConstants.Scorecard_Not_Done);
        } else {
          scorecard.setDplChkResult(CmrConstants.Scorecard_Not_Required);
          scorecard.setDplChkUsrId(null);
          scorecard.setDplChkUsrNm(null);
          scorecard.setDplChkTs(null);
        }

      }

      if (!newRequest) {
        // clear cmt field in admin table
        if (StringUtils.isEmpty(admin.getLockInd())) {
          admin.setLockInd(CmrConstants.YES_NO.N.toString());
        }
        if (StringUtils.isEmpty(admin.getProcessedFlag())) {
          admin.setLockInd(CmrConstants.YES_NO.N.toString());
        }
        if (StringUtils.isEmpty(admin.getDisableAutoProc())) {
          admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
        }
        admin.setCovBgRetrievedInd(CmrConstants.YES_NO.Y.toString());
        if (!PageManager.autoProcEnabled(data.getCmrIssuingCntry(), admin.getReqType())) {
          admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
        }
        removingBlankSpaceOfData(data);
        reqEntryService.updateEntity(admin, entityManager);
        reqEntryService.updateEntity(data, entityManager);
        reqEntryService.updateEntity(scorecard, entityManager);

        // update the mirror
        DataRdc rdc = new DataRdc();
        DataPK rdcpk = new DataPK();
        rdcpk.setReqId(data.getId().getReqId());
        rdc.setId(rdcpk);
        rdc = entityManager.find(DataRdc.class, rdcpk);
        if (rdc != null) {
          PropertyUtils.copyProperties(rdc, data);
          rdc.setCmrNo(cmrNo); // retain CMR no in old file
          reqEntryService.updateEntity(rdc, entityManager);
        } else {
          // recreate missing one
          rdc = new DataRdc();
          rdcpk = new DataPK();
          rdcpk.setReqId(data.getId().getReqId());
          rdc.setId(rdcpk);
          PropertyUtils.copyProperties(rdc, data);
          rdc.setCmrNo(cmrNo); // retain CMR no in old file
          reqEntryService.createEntity(rdc, entityManager);
        }

      } else {
        // clear cmt field in admin table
        if (StringUtils.isEmpty(admin.getLockInd())) {
          admin.setLockInd(CmrConstants.YES_NO.N.toString());
        }
        if (StringUtils.isEmpty(admin.getProcessedFlag())) {
          admin.setLockInd(CmrConstants.YES_NO.N.toString());
        }
        if (StringUtils.isEmpty(admin.getDisableAutoProc())) {
          admin.setDisableAutoProc(CmrConstants.YES_NO.N.toString());
        }
        if (!PageManager.autoProcEnabled(data.getCmrIssuingCntry(), admin.getReqType())) {
          admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
        }
        removingBlankSpaceOfData(data);
        reqEntryService.createEntity(admin, entityManager);
        reqEntryService.createEntity(data, entityManager);
        reqEntryService.createEntity(scorecard, entityManager);

        // create the mirror
        DataRdc rdc = new DataRdc();
        DataPK rdcpk = new DataPK();
        rdcpk.setReqId(data.getId().getReqId());
        rdc.setId(rdcpk);
        PropertyUtils.copyProperties(rdc, data);
        rdc.setCmrNo(cmrNo); // retain CMR no in old file
        reqEntryService.createEntity(rdc, entityManager);
      }

      if (mainRecord != null) {
        if (!skipAddress) {
          extractAddresses(admin, entityManager, result, geoHandler, reqIdToUse, reqModel, mainRecord.getCmrNum());
        } else {
          LOG.debug("Address import skipped.");
        }
      }

      if (newRequest) {
        RequestUtils.createWorkflowHistory(reqEntryService, entityManager, request, admin, "AUTO: Request created.", CmrConstants.Save());

        RequestUtils.addToNotifyList(reqEntryService, entityManager, user, reqIdToUse);
      }
      // save comment in req_cmt_log table .
      // save only if it is not null or not blank
      if (null != reqModel.getCmt() && !reqModel.getCmt().isEmpty()) {
        // RequestUtils.createCommentLog(reqEntryService, entityManager, user,
        // reqIdToUse, reqModel.getCmt());
      }

      CmrInternalTypes type = RequestUtils.computeInternalType(entityManager, admin.getReqType(), data.getCmrIssuingCntry(), reqIdToUse);
      if (type != null) {
        admin.setInternalTyp(type.getId().getInternalTyp());
        admin.setSepValInd(type.getSepValInd());
      }

      reqEntryService.updateEntity(admin, entityManager);

      reqEntryService.computeInternalType(entityManager, reqModel, admin);

      if (geoHandler != null) {
        geoHandler.doAfterImport(entityManager, admin, data);
        if (!skipAddress) {
          geoHandler.createOtherAddressesOnDNBImport(entityManager, admin, data);
        } else {
          LOG.debug("DnB other address import skipped.");
        }

        if (geoHandler.hasChecklist(reqModel.getCmrIssuingCntry())) {
          clearChecklistAfterImport(entityManager, user, reqIdToUse);
        }

      }

      if (endTransaction) {
        transaction.commit();
      }

      ImportCMRModel retModel = new ImportCMRModel();
      retModel.setReqId(reqIdToUse);
      retModel.setProspect(CmrConstants.PROSPECT_ORDER_BLOCK.equals(data.getOrdBlk()));

      return retModel;
    } catch (Exception e) {
      LOG.debug("Error in fetching current data.", e);
      if (endTransaction && transaction.isActive()) {
        transaction.rollback();
      }
      if (e instanceof CmrException) {
        throw e;
      }
      throw new CmrException(MessageUtil.ERROR_IMPORT_DATA, e);
    }
  }

  private void removingBlankSpaceOfData(Data data) {
    LOG.debug("Removing blank spaces from data fields....");
    data.setAbbrevLocn(!StringUtils.isEmpty(data.getAbbrevLocn()) ? data.getAbbrevLocn().trim() : "");
    data.setAbbrevNm(!StringUtils.isEmpty(data.getAbbrevNm()) ? data.getAbbrevNm().trim() : "");
    data.setAffiliate(!StringUtils.isEmpty(data.getAffiliate()) ? data.getAffiliate().trim() : "");
    data.setEmail2(!StringUtils.isEmpty(data.getEmail2()) ? data.getEmail2().trim() : "");
    data.setEmail3(!StringUtils.isEmpty(data.getEmail3()) ? data.getEmail3().trim() : "");
    data.setEnterprise(!StringUtils.isEmpty(data.getEnterprise()) ? data.getEnterprise().trim() : "");
    data.setInacCd(!StringUtils.isEmpty(data.getInacCd()) ? data.getInacCd().trim() : "");
    data.setInacType(!StringUtils.isEmpty(data.getInacType()) ? data.getInacType().trim() : "");
    data.setTaxCd1(!StringUtils.isEmpty(data.getTaxCd1()) ? data.getTaxCd1().trim() : "");
    data.setTaxCd2(!StringUtils.isEmpty(data.getTaxCd2()) ? data.getTaxCd2().trim() : "");
    data.setVat(!StringUtils.isEmpty(data.getVat()) ? data.getVat().trim() : "");
    data.setCapInd(!StringUtils.isEmpty(data.getCapInd()) ? data.getCapInd().trim() : "");
    data.setClientTier(!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier().trim() : "");
    data.setCollectionCd(!StringUtils.isEmpty(data.getCollectionCd()) ? data.getCollectionCd().trim() : "");
    data.setCrosSubTyp(!StringUtils.isEmpty(data.getCrosSubTyp()) ? data.getCrosSubTyp().trim() : "");
    data.setCustClass(!StringUtils.isEmpty(data.getCustClass()) ? data.getCustClass().trim() : "");
    data.setCustClass(!StringUtils.isEmpty(data.getCustClass()) ? data.getCustClass().trim() : "");
    data.setHwSvcsRepTeamNo(!StringUtils.isEmpty(data.getHwSvcsRepTeamNo()) ? data.getHwSvcsRepTeamNo().trim() : "");

    // Resolve Data issue (length of field ISIC_CD is 4 in db
    data.setIsicCd(!StringUtils.isEmpty(data.getIsicCd()) ? data.getIsicCd().trim().substring(0, 4) : "");

    data.setIsuCd(!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd().trim() : "");
    data.setRepTeamMemberNo(!StringUtils.isEmpty(data.getRepTeamMemberNo()) ? data.getRepTeamMemberNo().trim() : "");
    data.setSalesBusOffCd(!StringUtils.isEmpty(data.getSalesBusOffCd()) ? data.getSalesBusOffCd().trim() : "");
    data.setCompany(!StringUtils.isEmpty(data.getCompany()) ? data.getCompany().trim() : "");
    // Resolve Data issue length of field MiscBillCd is 3 in db
    if (!StringUtils.isEmpty(data.getMiscBillCd())) {
      if (data.getMiscBillCd().trim().length() > 3) {
        data.setMiscBillCd(data.getMiscBillCd().trim().substring(0, 3));
      } else {
        data.setMiscBillCd(data.getMiscBillCd().trim());
      }
    }

  }

  private void clearChecklistAfterImport(EntityManager entityManager, AppUser user, long reqId) {
    LOG.debug("Clearing checklist details for request..");
    String sql = ExternalizedQuery.getSql("REQENTRY.GETCHECKLIST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    ProlifChecklist checklist = query.getSingleResult(ProlifChecklist.class);
    if (checklist != null) {
      // clear all values
      CheckListModel model = new CheckListModel();
      model.setReqId(reqId);
      try {
        PropertyUtils.copyProperties(checklist, model);
      } catch (Exception e) {
        LOG.warn("Cannot copy properties.", e);
      }
    } else {
      checklist = new ProlifChecklist();
      ProlifChecklistPK chkPk = new ProlifChecklistPK();
      chkPk.setReqId(reqId);
      checklist.setId(chkPk);
      checklist.setCreateBy(user.getIntranetId());
      checklist.setCreateTs(SystemUtil.getCurrentTimestamp());
      checklist.setLastUpdtBy(user.getIntranetId());
      checklist.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

      reqEntryService.createEntity(checklist, entityManager);
    }
  }

  private void saveScorecard(AppUser user, FindCMRRecordModel mainRecord, Scorecard scorecard, String system) {
    // update scorecard
    if ("dnb".equals(system)) {
      scorecard.setFindDnbUsrNm(user.getBluePagesName());
      scorecard.setFindDnbUsrId(user.getIntranetId());
      scorecard.setFindDnbTs(SystemUtil.getCurrentTimestamp());
      scorecard.setFindDnbResult(mainRecord != null ? CmrConstants.RESULT_ACCEPTED : CmrConstants.RESULT_NO_RESULT);
    } else {
      scorecard.setFindCmrUsrNm(user.getBluePagesName());
      scorecard.setFindCmrUsrId(user.getIntranetId());
      scorecard.setFindCmrTs(SystemUtil.getCurrentTimestamp());
      scorecard.setFindCmrResult(mainRecord != null ? CmrConstants.RESULT_ACCEPTED : CmrConstants.RESULT_NO_RESULT);
    }
  }

  /**
   * Transfers the details of the retrieved record to the Data entity
   * 
   * @param record
   * @param data
   * @throws Exception
   */
  public void loadRecordToData(GEOHandler converter, FindCMRResultModel results, FindCMRRecordModel record, Admin admin, Data data) throws Exception {
    data.setAbbrevNm(record.getCmrShortName());
    data.setAffiliate(record.getCmrAffiliate());
    data.setCapInd(record.getCmrCapIndicator());
    data.setClientTier(record.getCmrTier());
    data.setCmrIssuingCntry(StringUtils.isNotEmpty(record.getCmrIssuedBy()) ? record.getCmrIssuedBy() : data.getCmrIssuingCntry());
    data.setCmrNo(record.getCmrNum());
    if (record.getCmrOwner() != null) {
      if ("LENOVO".equalsIgnoreCase(record.getCmrOwner())) {
        data.setCmrOwner(CmrConstants.LENOVO);
      } else if ("TRURO".equalsIgnoreCase(record.getCmrOwner())) {
        data.setCmrOwner(CmrConstants.TRURO);
      } else if ("FONSECA".equalsIgnoreCase(record.getCmrOwner())) {
        data.setCmrOwner(CmrConstants.FONSECA);
      } else if ("IBM".equalsIgnoreCase(record.getCmrOwner())) {
        data.setCmrOwner(CmrConstants.IBM);
      } else {
        data.setCmrOwner(record.getCmrOwner().length() > 3 ? record.getCmrOwner().substring(0, 3) : record.getCmrOwner());
      }

    }
    data.setCompany(record.getCmrCompanyNo());
    data.setCustClass(record.getCmrClass());
    data.setEnterprise(record.getCmrEnterpriseNumber());
    data.setInacCd(record.getCmrInac());
    data.setInacType(record.getCmrInacType());
    // Resolve Data issue (length of field ISIC_CD is 4 in db
    // data.setIsicCd(record.getCmrIsic());
    data.setIsicCd(!StringUtils.isEmpty(record.getCmrIsic())
        ? (record.getCmrIsic().trim().length() > 4 ? record.getCmrIsic().trim().substring(0, 4) : record.getCmrIsic().trim()) : "");
    data.setUsSicmen(!StringUtils.isEmpty(record.getCmrIsic())
        ? (record.getCmrIsic().trim().length() > 4 ? record.getCmrIsic().trim().substring(0, 4) : record.getCmrIsic().trim()) : "");
    data.setIsuCd(record.getCmrIsu());
    data.setSearchTerm(record.getCmrSortl());
    data.setSitePartyId(record.getCmrSitePartyID());
    data.setSubIndustryCd(record.getCmrSubIndustry());
    data.setTaxCd1(record.getCmrBusinessReg());
    data.setVat(record.getCmrVat());
    data.setVatInd(record.getCmrVatInd());
    data.setCustPrefLang(record.getCmrPrefLang());
    data.setTaxCd2(record.getCmrLocalTax2());
    if (record.getCmrSensitiveFlag() != null) {
      if (record.getCmrSensitiveFlag().toUpperCase().endsWith("_S")) {
        data.setSensitiveFlag(CmrConstants.SENSITIVE);
      } else if (record.getCmrSensitiveFlag().toUpperCase().endsWith("_M")) {
        data.setSensitiveFlag(CmrConstants.MASKED);
      } else {
        data.setSensitiveFlag(CmrConstants.REGULAR);
      }
    }
    data.setPpsceid(record.getCmrPpsceid());
    // CREATCMR-8243
    if (!SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
      data.setMemLvl(record.getCmrMembLevel());
      data.setBpRelType(record.getCmrBPRelType());
    }

    data.setCovId(record.getCmrCoverage());
    data.setBgId(record.getCmrBuyingGroup());
    data.setGeoLocationCd(record.getCmrGeoLocCd());
    data.setOrdBlk(record.getCmrOrderBlock());

    data.setCovDesc(record.getCmrCoverageName());
    data.setBgDesc(record.getCmrBuyingGroupDesc());
    data.setBgRuleId(record.getCmrLde());
    data.setGeoLocDesc(record.getCmrGeoLocDesc());
    data.setGbgId(record.getCmrGlobalBuyingGroup());
    data.setGbgDesc(record.getCmrGlobalBuyingGroupDesc());

    /* 1490262: Client Tier Code is set to Unassigned after Retrieving values */
    /* jz: do not set to when empty */
    // if (StringUtils.isEmpty(record.getCmrTier()) ||
    // CmrConstants.FIND_CMR_BLANK_CLIENT_TIER.equals(record.getCmrTier())) {
    // data.setClientTier(CmrConstants.CLIENT_TIER_UNASSIGNED);
    // }

    data.setMilitary("X".equals(record.getMilitaryFlag()) ? "Y" : null);

    if (converter != null) {
      converter.setDataValuesOnImport(admin, data, results, record);
    }
  }

  /**
   * Save current Admin data
   * 
   * @param model
   * @param admin
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   */
  private void savePageData(RequestEntryModel model, Admin admin, Data data)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    PropertyUtils.copyProperties(admin, model);
    PropertyUtils.copyProperties(data, model);
  }

  /**
   * Creates addresses (1 per line)
   * 
   * @param entityManager
   * @param result
   * @param reqId
   * @throws Exception
   */
  private void extractAddresses(Admin admin, EntityManager entityManager, FindCMRResultModel result, GEOHandler converter, long reqId,
      RequestEntryModel reqModel, String cmrNo) throws Exception {
    List<FindCMRRecordModel> cmrs = result.getItems();
    Addr addr = null;
    AddrPK addrPk = null;
    String type = null;
    Map<String, Integer> seqMap = new HashMap<String, Integer>();
    Integer seq = null;
    for (FindCMRRecordModel cmr : cmrs) {
      if (cmr.getCmrAddrTypeCode().equals("ZLST") && "897".equals(cmr.getCmrIssuedBy())) {
        continue;
      }

      // CREATCMR-7152
      if ("897".equals(cmr.getCmrIssuedBy())) {
        if (!"ZS01".equals(cmr.getCmrAddrTypeCode()) && !"ZI01".equals(cmr.getCmrAddrTypeCode()) && !"PG01".equals(cmr.getCmrAddrTypeCode())) {
          continue;
        }
      }
      addr = new Addr();
      addrPk = new AddrPK();
      addrPk.setReqId(reqId);
      type = cmr.getCmrAddrTypeCode();
      addrPk.setAddrType(type);
      if (("U".equals(reqModel.getReqType()) || "X".equals(reqModel.getReqType())) && converter != null && converter.useSeqNoFromImport()) {
        addrPk.setAddrSeq(cmr.getCmrAddrSeq());

      } else {
        if (seqMap.get(type) == null) {
          seqMap.put(type, new Integer(0));
        }
        seq = seqMap.get(type);
        addrPk.setAddrSeq((seq + 1) + "");
        seqMap.put(type, new Integer(seq + 1));
      }
      // if ("618".equals(reqModel.getCmrIssuingCntry()) &&
      // "C".equals(reqModel.getReqType())) {
      // addrPk.setAddrSeq(cmr.getCmrAddrSeq());
      // }
      // if
      // (SystemLocation.UNITED_STATES.equals(reqModel.getCmrIssuingCntry())
      // && CmrConstants.RDC_BILL_TO.equals(type)) {
      // addrPk.setAddrSeq(cmr.getCmrAddrSeq());
      // }

      // if (Arrays.asList("897", "866", "754", "618", "624", "788", "706",
      // "848").contains(reqModel.getCmrIssuingCntry())
      // && ("C".equals(reqModel.getReqType()) ||
      // "U".equals(reqModel.getReqType()))) {
      // addrPk.setAddrSeq(cmr.getCmrAddrSeq());
      // }

      /*
       * GEOHandler geoHandler =
       * RequestUtils.getGEOHandler(reqModel.getCmrIssuingCntry()); if
       * (geoHandler.setAddrSeqByImport(addrPk, entityManager, result) &&
       * ("C".equals(reqModel.getReqType()) ||
       * "U".equals(reqModel.getReqType()))) {
       * addrPk.setAddrSeq(cmr.getCmrAddrSeq()); }
       */
      // start- US ZI01 null sequence Import fix - 8 Apr 2022 - garima
      if (SystemLocation.UNITED_STATES.equals(reqModel.getCmrIssuingCntry()) && CmrConstants.RDC_BILL_TO.equals(type)) {
        addrPk.setAddrSeq(cmr.getCmrAddrSeq());
      }
      if (SystemLocation.UNITED_STATES.equals(reqModel.getCmrIssuingCntry()) && "C".equals(reqModel.getReqType())
          && (CmrConstants.RDC_SOLD_TO.equals(type) || CmrConstants.RDC_INSTALL_AT.equals(type))) {
        if (StringUtils.isBlank(cmr.getCmrAddrSeq())) {
          String addrSeq = type.equalsIgnoreCase("ZS01") ? "001" : "002";
          addrPk.setAddrSeq(addrSeq);
        } else {
          addrPk.setAddrSeq(cmr.getCmrAddrSeq());
        }
      }
      if (!StringUtils.isBlank(cmr.getCmrAddrSeq())) {
        addrPk.setAddrSeq(cmr.getCmrAddrSeq());
      } else if (StringUtils.isBlank(cmr.getCmrAddrSeq()) && "897".equals(reqModel.getCmrIssuingCntry())) {
        addrPk.setAddrSeq("ZI01".equals(type) ? "002" : ("ZP01".equals(type) ? "1" : "001"));
      }

      // end -US ZI01 null sequence Import fix - 8 Apr 2022 - garima
      addr.setId(addrPk);

      addr.setCity1(cmr.getCmrCity());
      addr.setCity2(cmr.getCmrCity2());
      addr.setStateProv(cmr.getCmrState());
      addr.setPostCd(cmr.getCmrPostalCode());
      addr.setLandCntry(cmr.getCmrCountryLanded());
      if ("U".equals(reqModel.getReqType()) || "X".equals(reqModel.getReqType())) {
        addr.setSapNo(cmr.getCmrSapNumber());
        addr.setIerpSitePrtyId(cmr.getCmrSitePartyID()); // ierpSitePrtyId
        addr.setExtWalletId(cmr.getExtWalletId());
        addr.setAddrStdResult("X");
        addr.setRdcCreateDt(cmr.getCmrRdcCreateDate());
        addr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp()); // placeholder
                                                                 // for now
      }

      // CREATCMR-5741 - no addr std
      addr.setAddrStdResult("X");
      addr.setCounty(cmr.getCmrCountyCode());
      addr.setCountyName(cmr.getCmrCounty());
      // addr.setCustNm1(cmr.getCmrName1Plain());
      // addr.setCustNm2(cmr.getCmrName2Plain());
      // addr.setCustNm3(cmr.getCmrName3());
      // addr.setCustNm4(cmr.getCmrName4());
      addr.setAddrTxt(cmr.getCmrStreetAddress());
      addr.setImportInd(CmrConstants.YES_NO.Y.toString());

      if (!StringUtils.isBlank(cmr.getCmrCustPhone()) && cmr.getCmrCustPhone().length() > 16) {
        addr.setCustPhone(cmr.getCmrCustPhone().substring(0, 16));
      } else {
        addr.setCustPhone(cmr.getCmrCustPhone());
      }
      if (!StringUtils.isBlank(cmr.getCmrCustFax()) && cmr.getCmrCustFax().length() > 16) {
        addr.setCustFax(cmr.getCmrCustFax().substring(0, 16));
      } else {
        addr.setCustFax(cmr.getCmrCustFax());
      }
      addr.setTransportZone(cmr.getCmrTransportZone());
      addr.setPoBox(cmr.getCmrPOBox());
      addr.setPoBoxCity(cmr.getCmrPOBoxCity());
      addr.setPoBoxPostCd(cmr.getCmrPOBoxPostCode());
      addr.setBldg(cmr.getCmrBldg());
      addr.setFloor(cmr.getCmrFloor());
      addr.setOffice(cmr.getCmrOffice());
      addr.setExtWalletId(cmr.getExtWalletId());
      addr.setDept(cmr.getCmrDept());
      if (converter != null) {
        converter.setAddressValuesOnImport(addr, admin, cmr, cmrNo);
      }

      // Ed|1043386| Only require DPL check for Create requests
      if (!CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqModel.getReqType()) && !PageManager.fromGeo("LA", reqModel.getCmrIssuingCntry())) {
        addr.setDplChkResult(CmrConstants.ADDRESS_Not_Required);
        addr.setDplChkInfo(null);
      }

      AddrRdc rdc = new AddrRdc();
      AddrPK rdcpk = new AddrPK();
      PropertyUtils.copyProperties(rdc, addr);
      PropertyUtils.copyProperties(rdcpk, addr.getId());
      rdc.setId(rdcpk);
      reqEntryService.updateEntity(rdc, entityManager);
      // if (this.autoEngineProcess) {
      // } else {
      // reqEntryService.createEntity(rdc, entityManager);
      // }

      reqEntryService.updateEntity(addr, entityManager);
      // if (this.autoEngineProcess) {
      // } else {
      // reqEntryService.createEntity(addr, entityManager);
      // }
    }

    // Ed|1043386| Only require DPL check for Create requests
    if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqModel.getReqType())) {
      AddressService.clearDplResults(entityManager, reqId);
    }
    // MK| Story 1585370 :Enhance CreateCMR to support single reactivation
    // requests
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && CmrConstants.DEACTIVATE_CMR_ORDER_BLOCK.equals(cmrs.get(0).getCmrOrderBlock())
        && !PageManager.fromGeo("LA", reqModel.getCmrIssuingCntry())) {
      AddressService.clearDplResults(entityManager, reqId);
    }

  }

  /**
   * Gets the current records (Admin/Data/Scorecard)
   * 
   * @param model
   * @param entityManager
   * @param request
   * @return
   * @throws CmrException
   */
  private CompoundEntity getCurrentRecord(RequestEntryModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return reqEntryService.getCurrentRecord(model, entityManager, request);
  }

  /**
   * Admin defaults
   * 
   * @param admin
   * @param user
   * @param request
   */
  private void setAdminDefaults(Admin admin, AppUser user, HttpServletRequest request) {
    admin.setReqStatus(CmrConstants.REQUEST_STATUS.DRA.toString());
    admin.setRequesterId(user.getIntranetId());
    admin.setRequesterNm(user.getBluePagesName());
    admin.setCreateTs(SystemUtil.getCurrentTimestamp());
    admin.setLastUpdtBy(user.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
    admin.setCovBgRetrievedInd(CmrConstants.YES_NO.Y.toString());
    admin.setProcessedFlag(CmrConstants.YES_NO.N.toString());
    String sysType = SystemConfiguration.getValue("SYSTEM_TYPE");
    admin.setWaitInfoInd(!StringUtils.isBlank(sysType) ? sysType.substring(0, 1) : null);
    RequestUtils.setClaimDetails(admin, request);
  }

  /**
   * Scorecard defaults
   * 
   * @param scorecard
   */
  private void setScorecardDefaults(Scorecard scorecard) {
    scorecard.setFindCmrResult(CmrConstants.Scorecard_Not_Done);
    scorecard.setFindDnbResult(CmrConstants.Scorecard_Not_Done);
  }

  /**
   * Deletes all address for the given request
   * 
   * @param entityManager
   * @param reqId
   */
  private void deleteAddrRecords(EntityManager entityManager, long reqId, boolean rdc) {
    String sql = ExternalizedQuery.getSql(rdc ? "REQUESTENTRY.DELETE_ADDRESS_RDC" : "REQUESTENTRY.DELETE_ADDRESS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    int deleted = query.executeSql();
    LOG.debug("Deleted " + deleted + " records from ADDR" + (rdc ? "_RDC" : "") + " for Request ID " + reqId);
    entityManager.flush();
  }

  private GEOHandler convertResults(EntityManager entityManager, RequestEntryModel model, FindCMRResultModel results, ImportCMRModel searchModel)
      throws Exception {
    String cmrIssuingCntry = model.getCmrIssuingCntry();
    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrIssuingCntry);
    if (geoHandler != null) {
      try {
        geoHandler.convertFrom(entityManager, results, model, searchModel);
      } catch (Exception e) {
        LOG.error("An error occurred during conversion of results.", e);
        throw e;
      }
    }
    return geoHandler;
  }

  /**
   * Creates a basic Comment log record for prospect cmr import
   * 
   * @param service
   * @param entityManager
   * 
   * @param user
   * @param req
   *          id
   * @param cmt
   * @throws CmrException
   * @throws SQLException
   */
  public void createCommentLog(BaseService<?, ?> service, EntityManager entityManager, String user, long reqId, String cmt)
      throws CmrException, SQLException {
    ReqCmtLog reqCmtLog = new ReqCmtLog();
    ReqCmtLogPK reqCmtLogpk = new ReqCmtLogPK();
    reqCmtLogpk.setCmtId(SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID"));
    reqCmtLog.setId(reqCmtLogpk);
    reqCmtLog.setReqId(reqId);
    reqCmtLog.setCmt(cmt);
    // save cmtlockedIn as Y default for current realese
    reqCmtLog.setCmtLockedIn(CmrConstants.CMT_LOCK_IND_YES);
    reqCmtLog.setCreateById(user);
    reqCmtLog.setCreateByNm(user);
    // set createTs as current timestamp and updateTs same as CreateTs
    reqCmtLog.setCreateTs(SystemUtil.getCurrentTimestamp());
    reqCmtLog.setUpdateTs(reqCmtLog.getCreateTs());
    service.createEntity(reqCmtLog, entityManager);
  }

  public class DefaultImportAddressHandler extends GEOHandler {

    @Override
    public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
        throws Exception {
    }

    @Override
    public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    }

    @Override
    public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    }

    @Override
    public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    }

    @Override
    public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    }

    @Override
    public int getName1Length() {
      return 0;
    }

    @Override
    public int getName2Length() {
      return 0;
    }

    @Override
    public void setAdminDefaultsOnCreate(Admin admin) {
    }

    @Override
    public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    }

    @Override
    public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    }

    @Override
    public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    }

    @Override
    public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
        List<UpdatedDataModel> results) {
    }

    @Override
    public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    }

    @Override
    public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
      return false;
    }

    @Override
    public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
    }

    @Override
    public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    }

    @Override
    public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    }

    @Override
    public boolean customerNamesOnAddress() {
      return false;
    }

    @Override
    public boolean useSeqNoFromImport() {
      return false;
    }

    @Override
    public boolean skipOnSummaryUpdate(String cntry, String field) {
      return false;
    }

    @Override
    public void doAfterImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    }

    @Override
    public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
      return null;
    }

    @Override
    public boolean hasChecklist(String cmrIssiungCntry) {
      return false;
    }

    @Override
    public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
      return false;
    }

  }

  public boolean isAutoEngineProcess() {
    return autoEngineProcess;
  }

  public void setAutoEngineProcess(boolean autoEngineProcess) {
    this.autoEngineProcess = autoEngineProcess;
  }

}
