package com.ibm.cio.cmr.request.service.requestentry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
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
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.ScorecardPK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cio.cmr.request.util.pdf.impl.DnBPDFConverter;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbData;

/**
 * package com.ibm.cio.cmr.request.service.requestentry;
 * 
 * import java.util.Collections;
 * 
 * /**
 * 
 * @author Jeffrey Zamora
 * 
 */
@Component
public class ImportDnBService extends BaseSimpleService<ImportCMRModel> {

  private static final Logger LOG = Logger.getLogger(ImportDnBService.class);

  @Autowired
  RequestEntryService reqEntryService;

  @Autowired
  AddressService addressService;

  @Override
  protected ImportCMRModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    LOG.debug("Request - doProcess");

    if (reqEntryService == null) {
      reqEntryService = new RequestEntryService();
    }
    if (addressService == null) {
      addressService = new AddressService();
    }
    AppUser user = AppUser.getUser(request);
    Long reqId = (Long) params.getParam("reqId");
    String system = (String) params.getParam("system");
    RequestEntryModel reqModel = (RequestEntryModel) params.getParam("model");
    String quickSearchData = (String) params.getParam("quickSearchData");
    GEOHandler geoHandler = RequestUtils.getGEOHandler(reqModel.getCmrIssuingCntry());

    FindCMRResultModel result = (FindCMRResultModel) params.getParam("results");
    if (result.getItems().size() > 0) {
      Collections.sort(result.getItems()); // have main record on top
    }

    boolean importAddress = true;

    FindCMRRecordModel mainRecord = result.getItems().size() > 0 ? result.getItems().get(0) : null;
    long reqIdToUse = reqId;
    boolean newRequest = false;

    // updateDnbValues(entityManager, mainRecord);

    GEOHandler converter = RequestUtils.getGEOHandler(reqModel.getCmrIssuingCntry());

    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();
    try {

      if (reqId <= 0) {
        // generate a Request ID for new requests
        LOG.debug("Generate a Request ID for new requests and newRequest equal true");
        reqIdToUse = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "REQ_ID");
        newRequest = true;
      }

      Admin admin = null;
      Data data = null;
      Scorecard scorecard = null;

      if (!newRequest) {
        // get the current records
        LOG.debug("Get the current records and newRequest equal false");
        RequestEntryModel model = new RequestEntryModel();
        model.setReqId(reqIdToUse);
        CompoundEntity entity = getCurrentRecord(model, entityManager, request);
        admin = entity.getEntity(Admin.class);
        data = entity.getEntity(Data.class);
        scorecard = entity.getEntity(Scorecard.class);

        savePageData(reqModel, admin, data);

      } else {
        // create/update the records
        LOG.debug("Create/update the records and newRequest equal true");
        admin = new Admin();
        AdminPK adminPK = new AdminPK();
        adminPK.setReqId(reqIdToUse);
        admin.setId(adminPK);

        data = new Data();
        DataPK dataPK = new DataPK();
        dataPK.setReqId(reqIdToUse);
        data.setId(dataPK);

        savePageData(reqModel, admin, data);

        if (geoHandler != null) {
          geoHandler.setDataDefaultsOnCreate(data, entityManager);
        }

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

      if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) || !CmrConstants.REQUEST_STATUS.DRA.toString().equals(admin.getReqStatus())) {
        // jz: 1449267 : do not import address for non-draft or update requests
        importAddress = false;
      }

      if (SystemLocation.CHINA.equals(data.getCmrIssuingCntry())) {
        // do import for CHINA update
        if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && CmrConstants.REQUEST_STATUS.DRA.toString().equals(admin.getReqStatus())) {
          importAddress = true;
        }
      }

      LOG.debug("D&B Address to be imported? " + importAddress);

      if ("706".equals(data.getCmrIssuingCntry())) {
        data.setCountryUse("706");
      }

      if (!StringUtils.isBlank(mainRecord.getCmrIsic())) {
        if (!CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())
            || (SystemLocation.CHINA.equals(data.getCmrIssuingCntry()) && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()))) {
          LOG.debug("Retrieving ISIC and Subindustry [ISIC=" + mainRecord.getCmrIsic() + "]");
          data.setIsicCd(mainRecord.getCmrIsic());
          data.setSubIndustryCd(getSubindCode(mainRecord.getCmrIsic(), entityManager));
          LOG.debug("- ISIC: " + data.getIsicCd() + "  Subindustry: " + data.getSubIndustryCd());
        }
      }

      if (!StringUtils.isBlank(mainRecord.getCmrDuns())) {
        LOG.debug("Retrieveing CMR dunsNo [CmrDusNumber=" + mainRecord.getCmrDuns() + "]");
        if ("678".equals(reqModel.getCmrIssuingCntry()) || "702".equals(reqModel.getCmrIssuingCntry()) || "806".equals(reqModel.getCmrIssuingCntry())
            || "846".equals(reqModel.getCmrIssuingCntry()) || SystemLocation.ISRAEL.equals(reqModel.getCmrIssuingCntry())) {
          if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqModel.getReqType())) {
            data.setDunsNo(mainRecord.getCmrDuns());
            LOG.debug("- REQ_TYPE_CREATE DunsNo : " + data.getDunsNo());
          }
        } else {
          data.setDunsNo(mainRecord.getCmrDuns());
          LOG.debug("-DunsNo : " + data.getDunsNo());
        }
      }
      if (!StringUtils.isBlank(mainRecord.getCmrVat()) && importAddress) {
        data.setVat(mainRecord.getCmrVat());
      }
      if (!StringUtils.isBlank(mainRecord.getCmrBusinessReg()) && importAddress) {
        data.setTaxCd1(mainRecord.getCmrBusinessReg());
      }
      if (SystemLocation.CHINA.equals(reqModel.getCmrIssuingCntry()) && !StringUtils.isBlank(mainRecord.getCreditCd()) && importAddress) {
        data.setBusnType(mainRecord.getCreditCd());
      }

      if (newRequest) {
        admin.setMainAddrType(mainRecord.getCmrAddrTypeCode());

        int splitLength = 70;
        int splitLength2 = 70;
        if (geoHandler != null) {
          splitLength = geoHandler.getName1Length();
          splitLength2 = geoHandler.getName2Length();
        }
        String[] parts = splitName(mainRecord.getCmrName1Plain().trim(), mainRecord.getCmrName2Plain(), splitLength, splitLength2);
        admin.setMainCustNm1(parts[0]);
        admin.setMainCustNm2(parts[1]);

        if (!StringUtils.isBlank(reqModel.getCmrIssuingCntry())) {
          data.setCmrIssuingCntry(reqModel.getCmrIssuingCntry());
        } else {
          data.setCmrIssuingCntry(mainRecord.getCmrIssuedBy());
        }

      } else if (importAddress) {
        // update admin
        // if (StringUtils.isBlank(admin.getMainAddrType())) {
        admin.setMainAddrType(mainRecord.getCmrAddrTypeCode());
        // }
        // if (StringUtils.isBlank(admin.getMainCustNm1())) {
        int splitLength = 70;
        int splitLength2 = 70;
        if (geoHandler != null) {
          splitLength = geoHandler.getName1Length();
          splitLength2 = geoHandler.getName2Length();
        }
        String[] parts = splitName(mainRecord.getCmrName1Plain().trim(), mainRecord.getCmrName2Plain(), splitLength, splitLength2);
        admin.setMainCustNm1(parts[0]);
        admin.setMainCustNm2(parts[1]);
        // }

      }
      // else if (geoHandler != null && !geoHandler.customerNamesOnAddress() &&
      // importAddress) {
      // int splitLength = 70;
      // int splitLength2 = 70;
      // if (geoHandler != null) {
      // splitLength = geoHandler.getName1Length();
      // splitLength2 = geoHandler.getName2Length();
      // }
      // String[] parts = splitName(mainRecord.getCmrName1Plain().trim(),
      // mainRecord.getCmrName2Plain(), splitLength, splitLength2);
      // admin.setMainCustNm1(parts[0]);
      // admin.setMainCustNm2(parts[1]);
      // }

      // transfer here back to model
      mainRecord.setCmrName1Plain(admin.getMainCustNm1());
      mainRecord.setCmrName2Plain(admin.getMainCustNm2());

      if (!newRequest) {
        if (StringUtils.isEmpty(data.getCmrIssuingCntry())) {
          data.setCmrIssuingCntry(mainRecord.getCmrIssuedBy());
        }
      }

      saveScorecard(user, mainRecord, scorecard, system);
      // Ed|1043386| Only require DPL check for Create requests
      if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(admin.getReqType()) && importAddress) {
        scorecard.setDplChkResult(CmrConstants.Scorecard_Not_Done);
      } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        // jz: 1449267 : do not import address for non-draft or update requests
        scorecard.setDplChkResult(CmrConstants.Scorecard_Not_Required);
        scorecard.setDplChkUsrId(null);
        scorecard.setDplChkUsrNm(null);
        scorecard.setDplChkTs(null);
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

        if (!PageManager.autoProcEnabled(data.getCmrIssuingCntry(), admin.getReqType())) {
          admin.setDisableAutoProc(CmrConstants.YES_NO.Y.toString());
        }
        RequestUtils.setProspLegalConversionFlag(entityManager, admin, data);
        reqEntryService.updateEntity(admin, entityManager);
        reqEntryService.updateEntity(data, entityManager);
        reqEntryService.updateEntity(scorecard, entityManager);

        // update the mirror
        LOG.debug("Update the mirror - adding data into DATA rdc");
        insertUpdateDataRdc(entityManager, newRequest, admin, data);

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
        reqEntryService.createEntity(admin, entityManager);
        reqEntryService.createEntity(data, entityManager);
        reqEntryService.createEntity(scorecard, entityManager);

        // create the mirror
        LOG.debug("Create the mirror - adding data into DATA rdc");
        insertUpdateDataRdc(entityManager, newRequest, admin, data);

      }

      // jz: 1449267 : do not import address for non-draft or update requests
      if (mainRecord != null && importAddress) {
        // jz: delete the current sold-to first

        Addr curr = getCurrSoldTo(entityManager, reqIdToUse);
        if (curr != null) {
          LOG.debug("Deleting current sold to address..");
          reqEntryService.deleteEntity(curr, entityManager);
        }

        AddrRdc currRdc = getCurrSoldToRDC(entityManager, reqIdToUse);
        if (currRdc != null) {
          LOG.debug("Deleting current sold to (rdc) address..");
          reqEntryService.deleteEntity(currRdc, entityManager);
        }

        if ("641".equals(reqModel.getCmrIssuingCntry())) {
          IntlAddr iAddr = getCurrSoldToIntlAddr(entityManager, reqIdToUse);
          if (iAddr != null) {
            LOG.debug("Deleting current sold to (IntlAddr) address..");
            reqEntryService.deleteEntity(iAddr, entityManager);
          }
        }
        extractAddresses(entityManager, mainRecord, converter, reqIdToUse, reqModel);
      }
      if (geoHandler != null) {
        geoHandler.createOtherAddressesOnDNBImport(entityManager, admin, data);
        geoHandler.convertDnBImportValues(entityManager, admin, data);
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

      String comment = "DUNS No. " + mainRecord.getCmrDuns() + " imported into the request from D&B.";
      if (!StringUtils.isBlank(quickSearchData)) {
        comment = "DUNS No. " + mainRecord.getCmrDuns() + " imported into the request from D&B via Quick Search. Search Params:\n" + quickSearchData;
      }

      String dnBname = (String) params.getParam("dnBname") != null ? (String) params.getParam("dnBname") : "";
      if (geoHandler != null && dnBname.length() > (geoHandler.getName1Length() + geoHandler.getName2Length())) {

        comment += "\n Imported name length is greater than the combined length of Customer Name1 and Customer Name2.Hence name will be trimmed.";
      }

      createCommentLog(reqEntryService, entityManager, "CreateCMR", reqIdToUse, comment);

      CmrInternalTypes type = RequestUtils.computeInternalType(entityManager, admin.getReqType(), data.getCmrIssuingCntry(), reqIdToUse);
      if (type != null) {
        admin.setInternalTyp(type.getId().getInternalTyp());
        admin.setSepValInd(type.getSepValInd());
      }
      reqEntryService.updateEntity(admin, entityManager);

      // save D&B Record as attachment

      if (mainRecord != null && !StringUtils.isEmpty(mainRecord.getCmrDuns())) {
        saveDnBAttachment(entityManager, user, reqIdToUse, mainRecord.getCmrDuns());
      }

      transaction.commit();

      ImportCMRModel retModel = new ImportCMRModel();
      retModel.setReqId(reqIdToUse);
      return retModel;
    } catch (Exception e) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw e;
    }
  }

  private void insertUpdateDataRdc(EntityManager entityManager, boolean newRequest, Admin admin, Data data)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    // update the mirror
    // 1851537: EMEA, LA, ASIA&Pacific - D&B import done on update requests
    // is interfering with updates on UI
    // jz - do NOT update the mirror
    DataRdc rdc = new DataRdc();
    DataPK rdcpk = new DataPK();
    rdcpk.setReqId(data.getId().getReqId());
    rdc.setId(rdcpk);
    PropertyUtils.copyProperties(rdc, data);
    if (newRequest) {
      LOG.debug("Adding data into DATA rdc - new request true ");
      reqEntryService.createEntity(rdc, entityManager);
    } else {
      LOG.debug("Updating data into DATA rdc - new request false ");
      reqEntryService.updateEntity(rdc, entityManager);
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

  private void saveScorecard(AppUser user, FindCMRRecordModel mainRecord, Scorecard scorecard, String system) {
    // update scorecard
    scorecard.setFindDnbUsrNm(user.getBluePagesName());
    scorecard.setFindDnbUsrId(user.getIntranetId());
    scorecard.setFindDnbTs(SystemUtil.getCurrentTimestamp());
    scorecard.setFindDnbResult(mainRecord != null ? CmrConstants.RESULT_ACCEPTED : CmrConstants.RESULT_NO_RESULT);
  }

  private Addr getCurrSoldTo(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("DNB.GET_CURR_SOLD_TO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    return query.getSingleResult(Addr.class);
  }

  private AddrRdc getCurrSoldToRDC(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("DNB.GET_CURR_SOLD_TO_RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    return query.getSingleResult(AddrRdc.class);
  }

  private IntlAddr getCurrSoldToIntlAddr(EntityManager entityManager, long reqId) {
    // TODO Auto-generated method stub
    String sql = ExternalizedQuery.getSql("DNB.GET_CURR_SOLD_TO_INTLADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    return query.getSingleResult(IntlAddr.class);
  }

  /**
   * Uses the country-specific address sequence generation to get the next
   * sold-to sequence
   * 
   * @param entityManager
   * @param country
   * @param reqId
   * @return
   */
  private String getNextAddressSequence(EntityManager entityManager, String country, long reqId) {
    String newAddrSeq = null;
    try {
      AddressService addrService = new AddressService();
      GEOHandler geoHandler = RequestUtils.getGEOHandler(country);
      String processingType = PageManager.getProcessingType(country, "U");
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType) && geoHandler != null) {
        newAddrSeq = addrService.generateAddrSeqLD(entityManager, "ZS01", reqId, country, geoHandler);
      }

      if (geoHandler != null && newAddrSeq == null) {
        newAddrSeq = geoHandler.generateAddrSeq(entityManager, "ZS01", reqId, country);
      }
      if (newAddrSeq == null) {
        newAddrSeq = addrService.generateAddrSeq(entityManager, "ZS01", reqId);
      }
    } catch (Exception e) {
      LOG.warn("Address sequence cannot be computed.", e);
    }
    return newAddrSeq;
  }

  /**
   * Creates addresses (1 per line)
   * 
   * @param entityManager
   * @param result
   * @param reqId
   * @throws Exception
   */
  private void extractAddresses(EntityManager entityManager, FindCMRRecordModel cmr, GEOHandler converter, long reqId, RequestEntryModel reqModel)
      throws Exception {
    Admin admin = null;
    AdminPK adminPK = null;
    admin = new Admin();
    adminPK = new AdminPK();
    adminPK.setReqId(reqId);
    admin.setId(adminPK);
    Addr addr = new Addr();
    AddrPK addrPk = new AddrPK();
    addrPk.setReqId(reqId);
    String type = cmr.getCmrAddrTypeCode();
    if (reqModel.getCmrIssuingCntry() != null && SystemLocation.ISRAEL.equalsIgnoreCase(reqModel.getCmrIssuingCntry())) {
      addrPk.setAddrType("CTYA");
    } else {
      addrPk.setAddrType(type);
    }

    String addrSeq = getNextAddressSequence(entityManager, reqModel.getCmrIssuingCntry(), reqId);
    if (addrSeq == null) {
      int nextSeq = getNextSoldToSeq(entityManager, reqId);
      addrSeq = nextSeq + "";
    }

    if (SystemLocation.UNITED_STATES.equals(reqModel.getCmrIssuingCntry()) && "C".equals(reqModel.getReqType())) {
      if ("ZS01".equals(addrPk.getAddrType())) {
        addrSeq = "001";
      } else if ("ZI01".equals(addrPk.getAddrType())) {
        addrSeq = "002";
      }
    }
    LOG.debug("Assigning address sequence " + addrSeq);
    addrPk.setAddrSeq(addrSeq);
    addr.setId(addrPk);

    addr.setCity1(cmr.getCmrCity());
    addr.setStateProv(cmr.getCmrState());
    if (converter != null && (converter instanceof CNHandler) && SystemLocation.CHINA.equals(reqModel.getCmrIssuingCntry())
        && StringUtils.isNotBlank(cmr.getCmrState())) {
      CNHandler cnHandler = (CNHandler) converter;
      cnHandler.convertChinaStateNameToStateCode(addr, cmr, entityManager);
    }
    if (!StringUtils.isBlank(addr.getStateProv()) && addr.getStateProv().length() > 3
        && (SystemLocation.AUSTRIA.equals(reqModel.getCmrIssuingCntry()) || SystemLocation.SWITZERLAND.equals(reqModel.getCmrIssuingCntry()))) {
      convertStateNameToStateCode(addr, cmr, entityManager);
    }
    if (!StringUtils.isBlank(addr.getStateProv()) && addr.getStateProv().length() > 3) {
      addr.setStateProv(null);
    }

    if (reqModel.getCmrIssuingCntry() != null && LAHandler.isLACountry(reqModel.getCmrIssuingCntry())) {
      String postalCode = cmr.getCmrPostalCode();
      String street = cmr.getCmrStreet();

      postalCode = postalCode != null ? postalCode.replace("-", "") : "";
      addr.setPostCd(postalCode);

      addr.setTransportZone(StringUtils.isNotBlank(cmr.getCmrTransportZone()) ? cmr.getCmrTransportZone() : "Z000000001");

      int addrLength = 35;
      if (street != null && street.length() > addrLength) {
        if (!StringUtils.isBlank(cmr.getCmrStreetAddressCont())) {
          // there is a con't, trim this only
          addr.setAddrTxt(street.substring(0, addrLength));
        } else {
          // no street address con't, overflow
          String street1 = street.substring(0, addrLength);
          String street2 = street.substring(addrLength, street.length());
          addr.setAddrTxt(street1);
          addr.setAddrTxt2(street2);
        }
      } else {
        addr.setAddrTxt(street);
      }

    } else if (SystemLocation.CHINA.equals(reqModel.getCmrIssuingCntry())) {
      addr.setPostCd(cmr.getCmrPostalCode());
      int addrLength = 35;
      String street = cmr.getCmrStreet();
      if (street != null && street.length() > addrLength) {
        if (!StringUtils.isBlank(cmr.getCmrStreetAddressCont())) {
          // there is a con't, trim this only
          addr.setAddrTxt(street.substring(0, addrLength));
          if (cmr.getCmrStreetAddressCont().length() > addrLength) {
            addr.setAddrTxt2(cmr.getCmrStreetAddressCont().substring(0, addrLength));
          } else {
            addr.setAddrTxt2(cmr.getCmrStreetAddressCont());
          }
        } else {
          // no street address con't, overflow
          String[] streetParts;
          streetParts = converter.splitName123(street, "", "", 35, 24, 100);
          String street1 = streetParts[0];
          String street2 = streetParts[1];
          String dept = streetParts[2];
          addr.setAddrTxt(street1);
          addr.setAddrTxt2(street2);
          addr.setDept(dept);
          if (StringUtils.isBlank(cmr.getCmrDept())) {
            cmr.setCmrDept(dept);
          }
        }
      } else {
        addr.setAddrTxt(street);
        if (!StringUtils.isBlank(cmr.getCmrStreetAddressCont())) {
          if (cmr.getCmrStreetAddressCont().length() > addrLength) {
            addr.setAddrTxt2(cmr.getCmrStreetAddressCont().substring(0, addrLength));
          } else {
            addr.setAddrTxt2(cmr.getCmrStreetAddressCont());
          }
        }
      }

      cmr.setCmrStreet(addr.getAddrTxt());
      cmr.setCmrStreetAddress(addr.getAddrTxt());
      cmr.setCmrStreetAddressCont(addr.getAddrTxt2());

    } else {
      addr.setPostCd(cmr.getCmrPostalCode());
      int addrLength = SystemLocation.UNITED_STATES.equals(reqModel.getCmrIssuingCntry()) ? 24 : 30;
      if (SystemLocation.FRANCE.equals(reqModel.getCmrIssuingCntry()) || SystemLocation.GERMANY.equals(reqModel.getCmrIssuingCntry())
          || SystemLocation.AUSTRIA.equals(reqModel.getCmrIssuingCntry()) || SystemLocation.SWITZERLAND.equals(reqModel.getCmrIssuingCntry())
          || SystemLocation.LIECHTENSTEIN.equals(reqModel.getCmrIssuingCntry())) {
        addrLength = 35;
      }
      String street = cmr.getCmrStreet();
      if (street != null && street.length() > addrLength) {
        if (!StringUtils.isBlank(cmr.getCmrStreetAddressCont())) {
          // there is a con't, trim this only
          addr.setAddrTxt(street.substring(0, addrLength));
          if (cmr.getCmrStreetAddressCont().length() > addrLength) {
            addr.setAddrTxt2(cmr.getCmrStreetAddressCont().substring(0, addrLength));
          } else {
            addr.setAddrTxt2(cmr.getCmrStreetAddressCont());
          }
        } else {
          // no street address con't, overflow
          String[] streetParts;
          if (SystemLocation.AUSTRIA.equals(reqModel.getCmrIssuingCntry()) || SystemLocation.GERMANY.equals(reqModel.getCmrIssuingCntry())
              || SystemLocation.LIECHTENSTEIN.equals(reqModel.getCmrIssuingCntry())
              || SystemLocation.SWITZERLAND.equals(reqModel.getCmrIssuingCntry())) {
            streetParts = converter.doSplitName(street, "", 35, 35);
          } else {
            streetParts = converter.doSplitName(street, "", 30, 30);
          }
          String street1 = streetParts[0];
          String street2 = streetParts[1];
          addr.setAddrTxt(street1);
          addr.setAddrTxt2(street2);
        }
      } else {
        addr.setAddrTxt(street);
        if (!StringUtils.isBlank(cmr.getCmrStreetAddressCont())) {
          if (cmr.getCmrStreetAddressCont().length() > addrLength) {
            addr.setAddrTxt2(cmr.getCmrStreetAddressCont().substring(0, addrLength));
          } else {
            addr.setAddrTxt2(cmr.getCmrStreetAddressCont());
          }
        }
      }

      if (SystemLocation.FRANCE.equals(reqModel.getCmrIssuingCntry())) {
        if (street != null && street.length() > addrLength) {
          street = street.substring(0, addrLength);
        }
        addr.setAddrTxt(street);
        addr.setAddrTxt2(null); // addr con't removed from UI of FR
      }

      if (converter.has3AddressLines(reqModel.getCmrIssuingCntry())) {
        // special handling for countries supporting 3 address lines (AP)
        boolean doSplit = (cmr.getCmrStreet() != null && cmr.getCmrStreet().length() > addrLength)
            || (cmr.getCmrStreetAddressCont() != null && cmr.getCmrStreetAddressCont().length() > addrLength);
        if (doSplit) {
          String fullStreet = cmr.getCmrStreet();
          fullStreet += !StringUtils.isBlank(cmr.getCmrStreetAddressCont()) ? " " + cmr.getCmrStreetAddressCont() : "";

          // use name splitting
          String[] linesA = converter.doSplitName(fullStreet, "", addrLength, addrLength);
          String line1 = linesA[0];
          String remaining = "";
          if (fullStreet.length() > line1.length()) {
            remaining = fullStreet.substring(line1.length()).trim();
          }
          String[] linesB = converter.doSplitName(remaining, "", addrLength, addrLength);
          String line2 = linesB[0];
          String line3 = linesB[1];
          LOG.debug("3 Line Split: " + fullStreet);
          LOG.debug(" - 1: " + line1);
          LOG.debug(" - 2: " + line2);
          LOG.debug(" - 3: " + line3);

          addr.setAddrTxt(line1);
          addr.setAddrTxt2(line2);
          converter.setAddressLine3(reqModel.getCmrIssuingCntry(), addr, cmr, line3);
        }
      }
      cmr.setCmrStreet(addr.getAddrTxt());
      cmr.setCmrStreetAddress(addr.getAddrTxt());
      cmr.setCmrStreetAddressCont(addr.getAddrTxt2());
    }

    addr.setLandCntry(cmr.getCmrCountryLanded());
    if (addr.getLandCntry() != null && addr.getLandCntry().length() > 2) {
      addr.setLandCntry(null);
    }
    addr.setCounty(cmr.getCmrCountyCode());
    addr.setCountyName(cmr.getCmrCounty());
    if (!StringUtils.isBlank(addr.getCounty()) && addr.getCounty().length() > 3) {
      addr.setCounty(null);
    }
    addr.setImportInd("D");
    // Defect 1185741: Correct Street flow while importing data from D&B
    // Author: Denns T Natad
    // Date: 04-26-2017
    // Address text passing is moved above
    // addr.setAddrTxt(cmr.getCmrStreet());
    addr.setCustPhone(cmr.getCmrCustPhone());
    addr.setCustFax(cmr.getCmrCustFax());

    // CREATCMR-5741 - no addr std
    /*
     * if ("U".equals(reqModel.getReqType())) { addr.setAddrStdResult("X"); }
     * else { // CMR-3994 - county if
     * (SystemLocation.UNITED_STATES.equals(reqModel.getCmrIssuingCntry()) &&
     * !StringUtils.isBlank(addr.getCounty())) { addr.setAddrStdResult("C");
     * addr.setAddrStdAcceptInd("Y");
     * addr.setAddrStdTs(SystemUtil.getCurrentTimestamp()); } }
     */

    // CREATCMR-5741 - no addr std
    addr.setAddrStdResult("X");
    cmr.setCmrIssuedBy(reqModel.getCmrIssuingCntry());

    if (converter != null) {
      if (converter.customerNamesOnAddress()) {
        addr.setCustNm1(cmr.getCmrName1Plain());
        addr.setCustNm2(cmr.getCmrName2Plain());
      }
      converter.setAddressValuesOnImport(addr, admin, cmr, null);
    }

    // Ed|1043386| Only require DPL check for Create requests
    if (!CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqModel.getReqType())) {
      addr.setDplChkResult(CmrConstants.ADDRESS_Not_Required);
      addr.setDplChkInfo(null);
    }

    if (converter != null && (converter instanceof CNHandler) && SystemLocation.CHINA.equals(reqModel.getCmrIssuingCntry())
        && StringUtils.isNotBlank(addr.getCity1())) {
      CNHandler cnHandler = (CNHandler) converter;
      cnHandler.setCNAddressENCityOnImport(addr, cmr, entityManager);
    }

    reqEntryService.createEntity(addr, entityManager);

    AddrRdc rdc = new AddrRdc();
    AddrPK rdcpk = new AddrPK();
    PropertyUtils.copyProperties(rdc, addr);
    PropertyUtils.copyProperties(rdcpk, addr.getId());
    rdc.setId(rdcpk);
    reqEntryService.createEntity(rdc, entityManager);

    if (SystemLocation.CHINA.equals(reqModel.getCmrIssuingCntry())
        && (StringUtils.isNotBlank(cmr.getCmrIntlAddress()) || StringUtils.isNotBlank(cmr.getCmrIntlName()))) {
      AddressModel model = new AddressModel();
      if (converter != null && (converter instanceof CNHandler) && StringUtils.isNotBlank(cmr.getCmrIntlCity1())) {
        CNHandler cnHandler = (CNHandler) converter;
        cnHandler.setCNAddressCityOnImport(model, cmr, addr, entityManager);
      }
      setCNIntlAddrModel(model, cmr);
      addressService.createCNIntlAddr(model, addr, entityManager);
    }

    // Ed|1043386| Only require DPL check for Create requests
    if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqModel.getReqType())) {
      AddressService.clearDplResults(entityManager, reqId);
    }

  }

  private void setCNIntlAddrModel(AddressModel model, FindCMRRecordModel cmr) {
    // TODO Auto-generated method stub
    model.setCnAddrTxt(cmr.getCmrIntlAddress());
    model.setCnAddrTxt2("");
    model.setCnCustName1(cmr.getCmrIntlName());
    model.setCnCustName2("");
    model.setCnCustName3("");
    model.setCnDistrict(cmr.getCmrIntlCity2());
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

  private void updateDnbValues(EntityManager entityManager, FindCMRRecordModel record) {
    String land1 = record.getCmrCountryLanded();
    String mandt = SystemConfiguration.getValue("MANDT");
    String state = record.getCmrState();
    String county = record.getCmrCounty();

    if (StringUtils.isBlank(county) || StringUtils.isBlank(state)) {
      return;
    }
    if (!StringUtils.isEmpty(county)) {
      county = county.toUpperCase();
      county = county.replaceAll("COUNTY", "");
      county = county.trim();
    }

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.DNBFILL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("LAND1", land1);
    query.setParameter("STATE", state);
    query.setParameter("COUNTY", "%" + county + "%");

    List<Object[]> results = query.getResults();
    if (results != null) {
      for (Object[] result : results) {
        if ("COUNTRY".equals(result[0])) {
          record.setCmrIssuedBy((String) result[1]);
        }
        if ("COUNTY".equals(result[0])) {
          record.setCmrCounty((String) result[1]);
        }
      }
    }
  }

  private int getNextSoldToSeq(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", CmrConstants.ADDR_TYPE.ZS01.toString());
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      try {
        int max = results.get(0)[0] != null ? Integer.parseInt((String) results.get(0)[0]) : 0;
        return max + 1;
      } catch (Exception e) {
        return 1;
      }
    } else {
      return 1;
    }
  }

  private String getSubindCode(String isicCd, EntityManager entityManager) {
    if (StringUtils.isBlank(isicCd) || isicCd.trim().length() < 4) {
      return null;
    }
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.DNB.GETSUBIND");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISIC", isicCd);
    return query.getSingleResult(String.class);
  }

  private String[] splitName(String name1, String name2, int length1, int length2) {
    String name = name1 + " " + (name2 != null ? name2 : "");
    String[] parts = name.split("[ ]");

    String namePart1 = "";
    String namePart2 = "";
    boolean part1Ok = false;
    for (String part : parts) {
      if ((namePart1 + " " + part).trim().getBytes(Charset.forName("UTF-8")).length > length1 || part1Ok) {
        part1Ok = true;
        namePart2 += " " + part;
      } else {
        namePart1 += " " + part;
      }
    }
    namePart1 = namePart1.trim();

    if (namePart1.length() == 0) {
      namePart1 = name.substring(0, length1);
      namePart2 = name.substring(length1);
    }
    if (namePart1.length() > length1) {
      namePart1 = namePart1.substring(0, length1);
    }
    namePart2 = namePart2.trim();
    if (namePart2.getBytes(Charset.forName("UTF-8")).length > length2) {
      namePart2 = namePart2.substring(0, length2);
    }

    return new String[] { namePart1, namePart2 };

  }

  /**
   * /** Connects to D&B and retrieves the details based on the DUNS. Proceeds
   * to create an attachment of type D&B Record
   * 
   * @param entityManager
   * @param user
   * @param reqId
   * @param dunsNo
   * @throws Exception
   */
  private void saveDnBAttachment(EntityManager entityManager, AppUser user, long reqId, String dunsNo) throws Exception {
    saveDnBAttachment(entityManager, user, reqId, dunsNo, "DnBImportRecord", -1);
  }

  /**
   * Connects to D&B and retrieves the details based on the DUNS. Proceeds to
   * create an attachment of type D&B Record
   * 
   * @param entityManager
   * @param dunsNo
   * @throws Exception
   */
  public void saveDnBAttachment(EntityManager entityManager, AppUser user, long reqId, String dunsNo, String fileNamePrefix, long confidenceCode)
      throws Exception {
    LOG.debug("Connecting to D&B service to get D&B Data..");
    CmrClientService dnbService = new CmrClientService();
    ModelMap map = new ModelMap();
    try {
      dnbService.getDnBDetails(map, dunsNo);
    } catch (Exception e) {
      LOG.error("Error in getting D&B data.", e);
      map.put("success", false);
    }
    Boolean success = (Boolean) map.get("success");
    if (success != null && success) {

      DnbData data = (DnbData) map.get("data");
      if (data == null) {
        LOG.warn("D&B data cannot be retrieved at the moment.");
        return;
      }
      if (data.getResults() == null || data.getResults().size() == 0) {
        LOG.warn("D&B data cannot be retrieved at the moment.");
        return;
      }

      AttachmentService attachmentService = new AttachmentService();
      attachmentService.removeAttachmentsOfType(entityManager, reqId, "DNB");

      DnBCompany company = data.getResults().get(0);
      DnBPDFConverter pdf = new DnBPDFConverter(company, dunsNo, reqId);
      if (confidenceCode > 0) {
        pdf.setConfidenceCode(confidenceCode);
      }
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        LOG.debug("Generating PDF content for DUNS " + dunsNo + "..");
        pdf.exportToPdf(entityManager, null, null, bos, null);

        byte[] pdfBytes = bos.toByteArray();

        LOG.debug("Creating request attachment..");
        try (ByteArrayInputStream bis = new ByteArrayInputStream(pdfBytes)) {
          try {
            attachmentService.addExternalAttachment(entityManager, user, reqId, "DNB", fileNamePrefix + "_" + dunsNo + ".pdf",
                "Details of record imported from D&B", bis);
          } catch (Exception e) {
            LOG.warn("Unable to save DnB attachment.", e);
          }
        }
      }
    } else {
      LOG.warn("D&B data cannot be retrieved at the moment.");
    }
  }

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

  public void convertStateNameToStateCode(Addr addr, FindCMRRecordModel cmr, EntityManager entityManager) {
    LOG.debug("Convert  StateName to StateCode Begin >>>");
    String stateCode = null;
    String stateName = cmr.getCmrState().trim();
    List<Object[]> results = new ArrayList<Object[]>();
    String cnStateProvCD = ExternalizedQuery.getSql("GET.WW_STATE_PROV_CD");
    PreparedQuery query = new PreparedQuery(entityManager, cnStateProvCD);
    query.setParameter("STATE_PROV_DESC", stateName);
    results = query.getResults();
    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      stateCode = sResult[0].toString();
    }
    if (StringUtils.isNotBlank(stateCode)) {
      addr.setStateProv(stateCode);
      cmr.setCmrState(stateCode);
      LOG.debug("Convert StateName to StateCode End >>>");
    }
  }
}
