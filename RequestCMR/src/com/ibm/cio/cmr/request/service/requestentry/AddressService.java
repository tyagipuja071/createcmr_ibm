package com.ibm.cio.cmr.request.service.requestentry;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.GeoContactInfo;
import com.ibm.cio.cmr.request.entity.GeoContactInfoPK;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.IntlAddrPK;
import com.ibm.cio.cmr.request.entity.MachinesToInstall;
import com.ibm.cio.cmr.request.entity.MachinesToInstallPK;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.service.dpl.DPLSearchService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cio.cmr.request.util.geo.impl.EMEAHandler;
import com.ibm.cio.cmr.request.util.geo.impl.JPHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cio.cmr.request.util.geo.impl.NORDXHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.DPLCheckClient;
import com.ibm.cmr.services.client.dpl.DPLCheckRequest;
import com.ibm.cmr.services.client.dpl.DPLCheckResponse;
import com.ibm.cmr.services.client.dpl.DPLCheckResult;
import com.ibm.comexp.api.EWSClient;
import com.ibm.comexp.at.exportchecks.ews.CEAppData;
import com.ibm.comexp.at.exportchecks.ews.CECustomerData;
import com.ibm.comexp.at.exportchecks.ews.CEEroData;
import com.ibm.comexp.at.exportchecks.ews.EWSProperties;

/**
 * @author Sonali Jain
 * 
 */
@Component
public class AddressService extends BaseService<AddressModel, Addr> {

  private final DataService dataService = new DataService();
  private final AdminService adminService = new AdminService();
  public static final List<String> LD_CEMA_COUNTRY = Arrays.asList("862");

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(AddressService.class);
  }

  @Override
  public void performTransaction(AddressModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

    String newAddrSeq = null;
    String action = model.getAction();
    StringBuilder uniqAddr = new StringBuilder();
    uniqAddr.append(model.getAddrType());
    uniqAddr.append("-");
    uniqAddr.append(model.getAddrSeq());
    GEOHandler geoHandler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());

    if (SystemLocation.JAPAN.equals(model.getCmrIssuingCntry())) {
      String[] nameArray = geoHandler.dividingCustName1toName2(model.getCustNm1(), model.getCustNm2());
      if (nameArray != null && nameArray.length == 2) {
        model.setCustNm1(nameArray[0]);
        model.setCustNm2(nameArray[1]);
      }
    }
    AppUser user = AppUser.getUser(request);
    if ("ADD_ADDRESS".equals(action)) {
      AdminPK pk = new AdminPK();
      pk.setReqId(model.getReqId());
      Admin admin = entityManager.find(Admin.class, pk);
      /*
       * if (SystemLocation.NETHERLANDS.equals(model.getCmrIssuingCntry()) &&
       * (model.getAddrType().equals("ZD01"))) { newAddrSeq =
       * generateShippingAddrSeqNL(entityManager, model.getAddrType(),
       * model.getReqId()); } else if
       * (SystemLocation.NETHERLANDS.equals(model.getCmrIssuingCntry()) &&
       * (model.getAddrType().equals("ZP01") ||
       * model.getAddrType().equals("ZI01") ||
       * model.getAddrType().equals("ZS01"))) { newAddrSeq =
       * generateAddrSeqNL(entityManager, model.getAddrType(),
       * model.getReqId()); } else if
       * (SystemLocation.SPAIN.equals(model.getCmrIssuingCntry()) &&
       * !StringUtils.isEmpty(admin.getReqType()) &&
       * "U".equals(admin.getReqType()) && (model.getAddrType().equals("ZD01")
       * || model.getAddrType().equals("ZI01"))) { newAddrSeq =
       * generateShipInstallAddrSeqES(entityManager, model.getAddrType(),
       * model.getReqId()); if ("N".equals(model.getImportInd())) {
       * model.setDplChkResult(null); } } else if
       * (SystemLocation.SPAIN.equals(model.getCmrIssuingCntry()) &&
       * !StringUtils.isEmpty(admin.getReqType()) &&
       * "C".equals(admin.getReqType()) && (model.getAddrType().equals("ZD01")
       * || model.getAddrType().equals("ZI01"))) { newAddrSeq =
       * generateShipInstallAddrSeqESCreate(entityManager, model.getAddrType(),
       * model.getReqId()); } else {
       */
      String processingType = PageManager.getProcessingType(model.getCmrIssuingCntry(), "U");
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType) && geoHandler != null) {
        newAddrSeq = generateAddrSeqLD(entityManager, model.getAddrType(), model.getReqId(), model.getCmrIssuingCntry(), geoHandler);
      }

      if (geoHandler != null && newAddrSeq == null) {
        newAddrSeq = geoHandler.generateAddrSeq(entityManager, model.getAddrType(), model.getReqId(), model.getCmrIssuingCntry());
      }
      if (newAddrSeq == null) {
        newAddrSeq = generateAddrSeq(entityManager, model.getAddrType(), model.getReqId());
      }

      if ("618".equals(model.getCmrIssuingCntry())) {

        newAddrSeq = generateMAddrSeqCopy(entityManager, model.getReqId(), admin.getReqType(), model.getAddrType());

      }

      if (LD_CEMA_COUNTRY.contains(model.getCmrIssuingCntry())) {
        int zd01cout = Integer.valueOf(getTrZD01Count(entityManager, model.getReqId()));
        int zi01cout = Integer.valueOf(getTrZI01Count(entityManager, model.getReqId()));
        String existAddTypeText = "";
        if (model.getAddrType().equals("ZS01")) {
          newAddrSeq = "00003";
          existAddTypeText = "Sold-To";
        }
        // update
        if (model.getAddrType().equals("ZP01")) {
          if ("C".equals(admin.getReqType())) {
            newAddrSeq = "00001";
          } else {
            newAddrSeq = "00002";
          }
          existAddTypeText = "Local Language Translation of Sold-To";
        }
        if (model.getAddrType().equals("ZD01")) {
          if (zd01cout == 0) {
            newAddrSeq = "00004";
          } else if (zd01cout == 1 && zi01cout == 0) {
            newAddrSeq = "00006";
          } else {
            newAddrSeq = generateEMEAddrSeqCopy(entityManager, model.getReqId());
          }
          existAddTypeText = "Ship-To";
        }
        if (model.getAddrType().equals("ZI01")) {
          boolean seq5Exist = seq5Exists(entityManager, model.getReqId());
          if (!seq5Exist) {
            newAddrSeq = "00005";
          } else {
            newAddrSeq = generateEMEAddrSeqCopy(entityManager, model.getReqId());
          }
          existAddTypeText = "Install-At";
        }
        // If address type already exist, the err msg is address type text xxxx
        // for turkey
        uniqAddr.delete(0, uniqAddr.length());
        uniqAddr.append(existAddTypeText);
      }

      // if ("864".equals(model.getCmrIssuingCntry())) {
      // newAddrSeq = generateMAddrSeqCopy(entityManager, model.getReqId());
      // System.out.println("newAdd =" + newAddrSeq);
      // }

      // if (SystemLocation.AUSTRIA.equals(model.getCmrIssuingCntry())) {
      // if (model.getAddrType().equals("ZS01")) {
      // newAddrSeq = "90001";
      // } else if (model.getAddrType().equals("ZI01")) {
      // newAddrSeq = "90002";
      // } else if (model.getAddrType().equals("ZP01")) {
      // newAddrSeq = "90003";
      // } else if (model.getAddrType().equals("ZD01")) {
      // newAddrSeq = "90004";
      // } else if (model.getAddrType().equals("ZS02")) {
      // newAddrSeq = "90005";
      // } else {
      // newAddrSeq = generateAddrSeq(entityManager, model.getAddrType(),
      // model.getReqId());
      // }
      // }

      // }

      if (SystemLocation.UNITED_STATES.equals(model.getCmrIssuingCntry())) {
        // if ("C".equals(admin.getReqType())) {
        if ("ZS01".equals(model.getAddrType())) {
          newAddrSeq = "001";
        } else if ("ZI01".equals(model.getAddrType())) {
          newAddrSeq = "002";
        }
        // }
      }
      if (NORDXHandler.isNordicsCountry(model.getCmrIssuingCntry()) || SystemLocation.GREECE.equals(model.getCmrIssuingCntry())) {
        if ("U".equals(admin.getReqType())) {
          String maxAddrSeq = null;
          maxAddrSeq = getMaxSequenceAddr(entityManager, model.getReqId());
          if (Integer.parseInt(maxAddrSeq) > Integer.parseInt(newAddrSeq)) {
            newAddrSeq = maxAddrSeq;
          }
        }
      }

      model.setAddrSeq(newAddrSeq);
      if (addrExists(entityManager, model.getAddrType(), model.getAddrSeq(), model.getReqId())) {
        throw new CmrException(MessageUtil.ERROR_ALREADY_ADDRESS, uniqAddr.toString());
      }
      // set default values in case of new address record
      // model.setAddrStdResult("Not Done"); Invalid, should be TGME code (2
      // letters)
      // model.setAddrStdRejReason("Not Applicable");
      Addr addr = createFromModel(model, entityManager, request);
      addr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());

      if (LAHandler.isLACountry(model.getCmrIssuingCntry())) {
        try {
          assignLocationCode(entityManager, addr, model.getCmrIssuingCntry());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (SystemLocation.UNITED_STATES.equals(model.getCmrIssuingCntry())) {
        assignCountyName(entityManager, addr);
      }

      if (geoHandler != null) {
        try {
          geoHandler.doBeforeAddrSave(entityManager, addr, model.getCmrIssuingCntry());
        } catch (Exception e) {
          throw new CmrException(e);
        }
      }

      addr.setAddrStdResult("X");
      createEntity(addr, entityManager);

      if (LAHandler.isBRIssuingCountry(model.getCmrIssuingCntry())) {
        updateDataForBRCreate(entityManager, request, addr);
      }

      if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
        updateAdminRec(model, entityManager, request);
      }

      if (EMEAHandler.isITIssuingCountry(model.getCmrIssuingCntry())) {
        setStateProvForITAddr(entityManager, addr);
      }

      if (JPHandler.isJPIssuingCountry(model.getCmrIssuingCntry())) {
        createJPIntlAddr(model, addr, entityManager);
      }

      if (CNHandler.isCNIssuingCountry(model.getCmrIssuingCntry())) {
        createCNIntlAddr(model, addr, entityManager);
        createCNGeoContactInfo(model, addr, entityManager);
        updateCNCityInfo(entityManager, addr, model);
      }

      if (NORDXHandler.isNordicsCountry(model.getCmrIssuingCntry())) {
        createMachines(model, addr, entityManager, request);
      }

      // after adding an address, do not clear DPL results but just recompute
      recomputeDPLResult(user, entityManager, model.getReqId());

      // clearDplResults(entityManager, model.getReqId());

    } else if ("UPDATE_ADDRESS".equals(action)) {
      Addr addr = getCurrentRecord(model, entityManager, request);
      boolean clearDpl = false;
      String parCmr = "";
      if (addr != null && model != null) {

        // fine tune check for changed customer name
        String aCustNm1 = addr.getCustNm1() != null ? addr.getCustNm1().trim().toLowerCase() : "";
        String aCustNm2 = addr.getCustNm2() != null ? addr.getCustNm2().trim().toLowerCase() : "";

        String mCustNm1 = model.getCustNm1() != null ? model.getCustNm1().trim().toLowerCase() : "";
        String mCustNm2 = model.getCustNm2() != null ? model.getCustNm2().trim().toLowerCase() : "";

        if (!StringUtils.equals(aCustNm1, mCustNm1) || !StringUtils.equals(aCustNm2, mCustNm2)) {
          clearDpl = true;
        }
        if (addr.getLandCntry() != null && !addr.getLandCntry().equals(model.getLandCntry())) {
          clearDpl = true;
        }
        if (CNHandler.isCNIssuingCountry(model.getCmrIssuingCntry()) && CNHandler.isClearDPL(model, addr, entityManager)) {
          clearDpl = true;
        }
        // DENNIS:Defect 1508112
        if (LAHandler.isLACountry(model.getCmrIssuingCntry()) && LAHandler.isClearDPL(model, addr, entityManager)) {
          clearDpl = true;
        }
        if (JPHandler.isJPIssuingCountry(model.getCmrIssuingCntry()))
          clearDpl = JPHandler.isClearDPL(model, addr, entityManager);

      }

      parCmr = addr.getParCmrNo();

      copyValuesToEntity(model, addr);

      addr.setParCmrNo(parCmr);

      if (StringUtils.isEmpty(model.getAddrType())) {
        if (!StringUtils.isEmpty(model.getSaveAddrType())) {
          addr.getId().setAddrType(model.getSaveAddrType());
        } else {
          addr.getId().setAddrType(model.getRemAddrType());
        }
      } else {
        addr.getId().setAddrType(model.getAddrType());
      }
      addr.setRdcLastUpdtDt(SystemUtil.getCurrentTimestamp());

      if (SystemLocation.UNITED_STATES.equals(model.getCmrIssuingCntry())) {
        assignCountyName(entityManager, addr);
      }

      if (LAHandler.isLACountry(model.getCmrIssuingCntry())) {
        try {
          assignLocationCode(entityManager, addr, model.getCmrIssuingCntry());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (geoHandler != null) {
        try {
          geoHandler.doBeforeAddrSave(entityManager, addr, model.getCmrIssuingCntry());
        } catch (Exception e) {
          throw new CmrException(e);
        }
      }

      if (clearDpl) {
        addr.setDplChkResult(null);
        addr.setDplChkById(null);
        addr.setDplChkByNm(null);
        addr.setDplChkErrList(null);
        addr.setDplChkInfo(null);
        addr.setDplChkTs(null);
      }

      boolean changed = RequestUtils.isUpdated(entityManager, addr, model.getCmrIssuingCntry());
      if (geoHandler != null) {
        changed = geoHandler.isAddressChanged(entityManager, addr, model.getCmrIssuingCntry(), changed);
      }
      addr.setChangedIndc(changed ? "Y" : null);
      addr.setAddrStdResult("X");
      updateEntity(addr, entityManager);

      if (LAHandler.isBRIssuingCountry(model.getCmrIssuingCntry())) {
        updateDataForBRCreate(entityManager, request, addr);
      }

      if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
        if (SystemLocation.JAPAN.equals(model.getCmrIssuingCntry())) {
          model.setCustNm1(addr.getCustNm1());
          model.setCustNm2(addr.getCustNm2());
        }
        updateAdminRec(model, entityManager, request);
      }

      if (clearDpl) {
        // after adding an address, do not clear DPL results but just recompute
        recomputeDPLResult(user, entityManager, model.getReqId());
        // clearDplResults(entityManager, model.getReqId());
      }

      if (EMEAHandler.isITIssuingCountry(model.getCmrIssuingCntry())) {
        setStateProvForITAddr(entityManager, addr);
      }

      if (CNHandler.isCNIssuingCountry(model.getCmrIssuingCntry())) {
        updateCNIntlAddr(model, entityManager, addr);
        updateCNGeoContactInfo(model, entityManager, addr);
        updateCNCityInfo(entityManager, addr, model);
      }

      if (JPHandler.isJPIssuingCountry(model.getCmrIssuingCntry())) {
        updateJPIntlAddr(model, entityManager, addr);
      }

      if (NORDXHandler.isNordicsCountry(model.getCmrIssuingCntry())) {
        createMachines(model, addr, entityManager, request);
      }

    } else if ("REMOVE_ADDRESS".equals(action)) {
      Addr addrList = getCurrentRecord(model, entityManager, request);
      // boolean imported = false;
      long reqId = addrList.getId().getReqId();
      String type = addrList.getId().getAddrType();
      String seq = addrList.getId().getAddrSeq();
      // if ("Y".equals(addrList.getImportInd()) ||
      // "D".equals(addrList.getImportInd())) {
      // imported = true;
      // }
      deleteEntity(addrList, entityManager);

      if ("D".endsWith(addrList.getImportInd())) {
        this.log.debug("Removing Addr Rdc record for DnB Imported record..");
        AddrPK addrRdcPk = new AddrPK();
        addrRdcPk.setReqId(addrList.getId().getReqId());
        addrRdcPk.setAddrSeq(addrList.getId().getAddrSeq());
        addrRdcPk.setAddrType(addrList.getId().getAddrType());
        AddrRdc addrRdc = entityManager.find(AddrRdc.class, addrRdcPk);
        if (addrRdc != null) {
          deleteEntity(addrRdc, entityManager);
        }
      }

      if (CNHandler.isCNIssuingCountry(model.getCmrIssuingCntry())) {
        IntlAddr iAddr = getIntlAddrById(addrList, entityManager);
        deleteEntity(iAddr, entityManager);
      }

      if (JPHandler.isJPIssuingCountry(model.getCmrIssuingCntry())) {
        IntlAddr iAddr = getIntlAddrById(addrList, entityManager);
        if (iAddr != null)
          deleteEntity(iAddr, entityManager);
      }

      if (NORDXHandler.isNordicsCountry(model.getCmrIssuingCntry())) {
        deleteMachines(model, addrList, entityManager, request);
      }

      String processingType = PageManager.getProcessingType(model.getCmrIssuingCntry(), "U");
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType) && SystemLocation.ISRAEL.equals(model.getCmrIssuingCntry())) {
        deleteShippingPairedSeq(addrList, entityManager);
      }

      // after removing an address, recompute DPL
      recomputeDPLResult(user, entityManager, model.getReqId());
      // Defect 1744532
      // if (imported) {
      // String sql =
      // ExternalizedQuery.getSql("REQUESTENTRY.DELETE_ADDRESS_RDC_SINGLE");
      // PreparedQuery delete = new PreparedQuery(entityManager, sql);
      // delete.setParameter("REQ_ID", reqId);
      // delete.setParameter("TYPE", type);
      // delete.setParameter("SEQ", seq);
      // delete.executeSql();
      // }

    } else if ("REMOVE_ADDRESSES".equals(action)) {
      List<KeyContainer> keys = extractKeys(model);

      String sql = ExternalizedQuery.getSql("ADDRESS.COPY.GETSOURCE");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      Addr addr = null;
      long reqId = 0;
      String addrType = null;
      String addrSeq = null;
      for (KeyContainer key : keys) {
        reqId = Long.parseLong(key.getKey("reqId"));
        addrType = key.getKey("addrType");
        addrSeq = key.getKey("addrSeq");

        query.setParameter("REQ_ID", reqId);
        query.setParameter("ADDR_TYPE", addrType);
        query.setParameter("ADDR_SEQ", addrSeq);

        addr = query.getSingleResult(Addr.class);
        if (addr != null) {
          this.log.debug("Removing address [Request ID: " + reqId + " Type: " + addrType + " Sequence: " + model.getAddrSeq() + "]");
          deleteEntity(addr, entityManager);

          if ("D".endsWith(addr.getImportInd())) {
            this.log.debug("Removing Addr Rdc record for DnB Imported record..");
            AddrPK addrRdcPk = new AddrPK();
            addrRdcPk.setReqId(addr.getId().getReqId());
            addrRdcPk.setAddrSeq(addr.getId().getAddrSeq());
            addrRdcPk.setAddrType(addr.getId().getAddrType());
            AddrRdc addrRdc = entityManager.find(AddrRdc.class, addrRdcPk);
            if (addrRdc != null) {
              deleteEntity(addrRdc, entityManager);
            }
          }

          if (CNHandler.isCNIssuingCountry(model.getCmrIssuingCntry())) {
            IntlAddr iAddr = getIntlAddrById(addr, entityManager);
            deleteEntity(iAddr, entityManager);
          }

          if (JPHandler.isJPIssuingCountry(model.getCmrIssuingCntry())) {
            IntlAddr iAddr = getIntlAddrById(addr, entityManager);
            if (iAddr != null)
              deleteEntity(iAddr, entityManager);
          }

          if (NORDXHandler.isNordicsCountry(model.getCmrIssuingCntry())) {
            deleteMachines(model, addr, entityManager, request);
          }

          String processingType = PageManager.getProcessingType(model.getCmrIssuingCntry(), "U");
          if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType) && SystemLocation.ISRAEL.equals(model.getCmrIssuingCntry())) {
            deleteShippingPairedSeq(addr, entityManager);
          }
        }
      }

      // after removing an address, recompute DPL
      recomputeDPLResult(user, entityManager, model.getReqId());
    } else if ("ADD_MACHINE".equals(action)) {
      Addr addr = getCurrentRecord(model, entityManager, request);
      createMachines(model, addr, entityManager, request);
    } else if ("REMOVE_MACHINE".equals(action)) {
      Addr addr = getCurrentRecord(model, entityManager, request);
      deleteSingleMachine(model, addr, entityManager, request);

    }
  }

  private void deleteShippingPairedSeq(Addr addr, EntityManager entityManager) {
    if ("ZD01".equals(addr.getId().getAddrType())) {
      Addr pairedAddr = getAddrByPairedAddrSeq(entityManager, addr.getId().getReqId(), "CTYC", addr.getId().getAddrSeq());
      if (pairedAddr != null) {
        deleteEntity(pairedAddr, entityManager);
      }
    } else if ("CTYC".equals(addr.getId().getAddrType())) {
      Addr pairedAddr = getAddrByAddrSeq(entityManager, addr.getId().getReqId(), "ZD01", addr.getPairedAddrSeq());
      if (pairedAddr != null) {
        deleteEntity(pairedAddr, entityManager);
      }
    }
  }

  private Addr getAddrByAddrSeq(EntityManager entityManager, long reqId, String addrType, String addrSeq) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    query.setParameter("ADDR_SEQ", addrSeq);
    List<Addr> addrList = query.getResults(Addr.class);

    if (!addrList.isEmpty()) {
      return addrList.get(0);
    }
    return null;
  }

  private Addr getAddrByPairedAddrSeq(EntityManager entityManager, long reqId, String addrType, String pairedAddrSeq) {
    String sql = ExternalizedQuery.getSql("GET.ADDR.BY_PAIRED_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    query.setParameter("PAIRED_ADDR_SEQ", pairedAddrSeq);
    List<Addr> addrList = query.getResults(Addr.class);

    if (!addrList.isEmpty()) {
      return addrList.get(0);
    }
    return null;
  }

  private boolean addrExists(EntityManager entityManager, String addrType, String addrSeq, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    query.setParameter("ADDR_SEQ", addrSeq);
    return query.exists();
  }

  @Override
  protected List<AddressModel> doSearch(AddressModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<AddressModel> results = new ArrayList<AddressModel>();
    SimpleDateFormat dateFormat = CmrConstants.DATE_FORMAT();

    // methods to retrieve current values
    boolean forUpdate = "Y".equals(model.getUpdateInd());
    List<AddrRdc> addrRdcList = null;
    if (forUpdate) {
      addrRdcList = getAddrRdcRecords(entityManager, model);
      if (addrRdcList == null) {
        addrRdcList = Collections.emptyList();
      }
    }
    // Defect 1583766: PP:On Import of CMR 159716, Multiple addresses are
    // getting imported but in DB its working fine
    String sql = null;
    if ("758".equalsIgnoreCase(model.getCmrIssuingCntry())) {
      sql = ExternalizedQuery.getSql("REQUESTENTRY.ADDR.SEARCH_BY_REQID_IT");
    } else {
      sql = ExternalizedQuery.getSql("REQUESTENTRY.ADDR.SEARCH_BY_REQID");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);

    List<CompoundEntity> rs = null;
    rs = query.getCompundResults(Addr.class, Addr.ADDRESS_TYPE_MAPPING);
    // List<Addr> rs = query.getResults(-1, Addr.class);
    AddressModel addrModel = null;
    Addr addr = null;
    String addrTypeText = null;
    String adddrCreateDate = null;
    Date addrLastUpdDate;
    String addrResultTxt = null;
    String country = null;
    String state = null;
    if (rs != null) {
      for (CompoundEntity entity : rs) {
        addr = entity.getEntity(Addr.class);
        addrTypeText = (String) entity.getValue("ADDR_TYPE_TEXT");
        adddrCreateDate = (String) entity.getValue("ADDR_CREATE_DATE");
        addrLastUpdDate = (Date) entity.getValue("ADDR_LAST_UPD_DATE");
        addrResultTxt = (String) entity.getValue("ADDR_RESULT_TEXT");
        country = (String) entity.getValue("COUNTRY_DESC");
        state = (String) entity.getValue("STATE_PROV_DESC");
        if (adddrCreateDate != null && adddrCreateDate.length() == 8 && !adddrCreateDate.equalsIgnoreCase("00000000")) {
          adddrCreateDate = adddrCreateDate.substring(0, 4) + "-" + adddrCreateDate.substring(4, 6) + "-" + adddrCreateDate.substring(6, 8);
        } else {
          adddrCreateDate = "";
        }
        addrModel = new AddressModel();
        if (JPHandler.isJPIssuingCountry(model.getCmrIssuingCntry())) {
          addr.setCustNm4((addr.getCustNm4() == null ? "" : addr.getCustNm4()) + (addr.getPoBoxCity() == null ? "" : addr.getPoBoxCity()));
          addr.setPoBoxCity(null);
          addr.setAddrTxt((addr.getAddrTxt() == null ? "" : addr.getAddrTxt()) + (addr.getAddrTxt2() == null ? "" : addr.getAddrTxt2()));
          addr.setAddrTxt2(null);
        }
        copyValuesFromEntity(addr, addrModel);
        addrModel.setAddrTypeText(addrTypeText);
        addrModel.setCreateTsString(adddrCreateDate);
        if (addrLastUpdDate != null) {
          addrModel.setLastUpdtTsString(dateFormat.format(addrLastUpdDate));
        }

        addrModel.setAddrStdResultText(addrResultTxt);

        addrModel.setPairedSeq(addr.getPairedAddrSeq());

        addrModel.setOldAddrText(entity.getValue("OLD_ADDR_TXT") != null ? entity.getValue("OLD_ADDR_TXT").toString() : "");

        addrModel.setOldAddrText2(entity.getValue("OLD_ADDR_TXT_2") != null ? entity.getValue("OLD_ADDR_TXT_2").toString() : "");

        addrModel.setCountryDesc(country);
        addrModel.setStateProvDesc(state);

        if (forUpdate) {
          if ("N".equals(addr.getImportInd())) {
            // new address
            addrModel.setUpdateInd("N");
          } else {
            addrModel.setUpdateInd("Y".equals(addr.getChangedIndc()) ? "U" : "");
            for (AddrRdc addrRdc : addrRdcList) {
              if (addr.getId().getAddrType().equals(addrRdc.getId().getAddrType())
                  && addr.getId().getAddrSeq().equals(addrRdc.getId().getAddrSeq())) {
                if (RequestUtils.isUpdated(addr, addrRdc, model.getCmrIssuingCntry())) {
                  addrModel.setUpdateInd("U");
                }
                break;
              }
            }
          }
        }
        // addrModel.setState(BaseModel.STATE_EXISTING);

        if (CNHandler.isCNIssuingCountry(model.getCmrIssuingCntry())) {
          // fetch data from INTL_ADDR
          IntlAddr iAddr = getIntlAddrById(addr, entityManager);

          if (iAddr != null && iAddr.getId().getReqId() != 0) {
            addrModel.setCnAddrTxt(iAddr.getAddrTxt());
            addrModel.setCnAddrTxt2(iAddr.getIntlCustNm3());
            addrModel.setCnCustName1(iAddr.getIntlCustNm1());
            addrModel.setCnCustName2(iAddr.getIntlCustNm2());
            addrModel.setCnCity(iAddr.getCity1());
            addrModel.setCnDistrict(iAddr.getCity2());
          }

          // fetch data from GEO_ADL_CONT_DTL
          GeoContactInfo geoContactInfo = getGeoContactInfoById(addr, entityManager);
          if (geoContactInfo != null && geoContactInfo.getId().getReqId() != 0) {
            addrModel.setCnCustContJobTitle(geoContactInfo.getContactFunc());
            addrModel.setCnCustContNm(geoContactInfo.getContactName());
            addrModel.setCnCustContPhone2(geoContactInfo.getContactPhone());
          }
        }

        if (JPHandler.isJPIssuingCountry(model.getCmrIssuingCntry())) {
          // fetch data from INTL_ADDR
          IntlAddr iAddr = getIntlAddrById(addr, entityManager);

          if (iAddr != null && iAddr.getId().getReqId() != 0) {
            addrModel.setCnAddrTxt(iAddr.getAddrTxt());
            addrModel.setCnAddrTxt2(iAddr.getIntlCustNm4());
            addrModel.setCnCustName1(iAddr.getIntlCustNm1());
            addrModel.setCnCustName2(iAddr.getIntlCustNm2());
            addrModel.setCnCity(iAddr.getCity1());
            addrModel.setCnDistrict(iAddr.getCity2());
          }

        }

        results.add(addrModel);
      }
    }

    if ("Y".equalsIgnoreCase(model.getFilterInd()) && "631".equalsIgnoreCase(model.getCmrIssuingCntry())) {
      List<AddressModel> filteredRecords = new ArrayList<AddressModel>();
      RequestEntryModel reqEntry = new RequestEntryModel();
      reqEntry.setReqType("C");
      reqEntry.setReqId(model.getReqId());
      reqEntry.setCustType(model.getCurrCustType());
      reqEntry.setCmrIssuingCntry(model.getCmrIssuingCntry());
      if (!results.isEmpty() && results.size() > 0) {
        LAHandler.doFilterAddresses(reqEntry, results, filteredRecords);
        if (!filteredRecords.isEmpty() && filteredRecords.size() > 0) {
          return filteredRecords;
        }
      }
    }

    GEOHandler handler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());
    if (handler != null) {
      handler.doFilterAddresses(results);
    }

    return results;
  }

  /**
   * Gets a list of {@link AddrRdc} records
   * 
   * @param entityManager
   * @param model
   * @return
   */
  private List<AddrRdc> getAddrRdcRecords(EntityManager entityManager, AddressModel model) {
    this.log.debug("Searching for ADDR_RDC records for Request " + model.getReqId());
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADDRRDC.SEARCH_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setForReadOnly(true);
    return query.getResults(AddrRdc.class);
  }

  @Override
  protected Addr getCurrentRecord(AddressModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("ADDRESS.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    if (StringUtils.isEmpty(model.getAddrType())) {
      if (!StringUtils.isEmpty(model.getSaveAddrType())) {
        query.setParameter("ADDR_TYPE", model.getSaveAddrType());
      } else {
        query.setParameter("ADDR_TYPE", model.getRemAddrType());
      }
    } else {
      query.setParameter("ADDR_TYPE", model.getAddrType());
    }
    query.setParameter("ADDR_SEQ", model.getAddrSeq());
    List<Addr> addrList = query.getResults(1, Addr.class);
    if (addrList != null && addrList.size() > 0) {
      Addr addr = addrList.get(0);
      if (addr != null && JPHandler.isJPIssuingCountry(model.getCmrIssuingCntry())) {
        String custNm4 = ((addr.getCustNm4() == null ? "" : addr.getCustNm4()) + (addr.getPoBoxCity() == null ? "" : addr.getPoBoxCity())).trim();
        addr.setCustNm4(custNm4.length() > 23 ? custNm4.substring(0, 23) : custNm4);
        addr.setPoBoxCity(null);
        String addrTxt = ((addr.getAddrTxt() == null ? "" : addr.getAddrTxt()) + (addr.getAddrTxt2() == null ? "" : addr.getAddrTxt2())).trim();
        addr.setAddrTxt(addrTxt.length() > 23 ? addrTxt.substring(0, 23) : addrTxt);
        addr.setAddrTxt2(null);
      }
      return addr;
    }

    return null;
  }

  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId) {

    String maxAddrSeq = null;
    int addrSeq = 0;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  @Deprecated
  protected String generateShippingAddrSeqNL(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 20800;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "20800");

      if (!(Integer.valueOf(maxAddrSeq) >= 20800 && Integer.valueOf(maxAddrSeq) <= 20849)) {
        maxAddrSeq = "";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "20800";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  @Deprecated
  protected String generateShipInstallAddrSeqES(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 7;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.ES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "1");
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
        if (addrSeq < 5) {
          addrSeq = 7;
        } else {
          addrSeq++;
        }
      } catch (Exception e) {
        // if returned value is invalid
      }
    }
    newAddrSeq = Integer.toString(addrSeq);
    // newAddrSeq = StringUtils.leftPad(newAddrSeq, 5, '0');
    return newAddrSeq;
  }

  @Deprecated
  private String generateShipInstallAddrSeqESCreate(EntityManager entityManager, String addrType, long reqId) {

    String maxAddrSeq = null;
    int addrSeq = 0;
    int addrSeqCopy = 6;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.ES.CREATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      try {
        if ("0".equals(maxAddrSeq))
          addrSeq = Integer.parseInt(maxAddrSeq);
        else {
          addrSeq = Integer.parseInt(maxAddrSeq);

          sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.ES.MAX");
          query = new PreparedQuery(entityManager, sql);
          query.setParameter("REQ_ID", reqId);

          results = query.getResults();
          if (results != null && results.size() > 0) {
            result = results.get(0);
            maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
            addrSeq = Integer.parseInt(maxAddrSeq);
            if (addrSeq < 6) {
              addrSeq = addrSeqCopy;
            }
          }
        }
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    // newAddrSeq = StringUtils.leftPad(newAddrSeq, 5, '0');
    return newAddrSeq;
  }

  @Deprecated
  protected String generateAddrSeqNL(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 1;
    String newAddrSeq = null;
    if (addrType.equals("ZI01")) {
      addrSeq = 20701;
    }
    if (addrType.equals("ZP01")) {
      addrSeq = 29901;
    }
    if (addrType.equals("ZS01")) {
      addrSeq = 99901;
    }
    try {
      addrSeq = Integer.parseInt(newAddrSeq);
    } catch (Exception e) {
      // if returned value is invalid
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  @Override
  protected Addr createFromModel(AddressModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    Addr list = new Addr();
    AddrPK pk = new AddrPK();
    list.setId(pk);
    copyValuesToEntity(model, list);
    return list;
  }

  /**
   * Performs DPL Checks
   * 
   * @deprecated
   * @param user
   * @param reqId
   * @throws CmrException
   */
  @Deprecated
  public void performDPLCheck(AppUser user, long reqId) throws CmrException {
    log.debug("Entering performDPLCheck()");
    EntityTransaction transaction = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      GEOHandler geoHandler = null;

      Data data = dataService.getCurrentRecordById(reqId, entityManager);
      if (data != null) {
        geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      }

      Map<String, List<String>> records = new HashMap<String, List<String>>();

      String candidateKey = "DPL.GETCANDIDATES";
      if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
        candidateKey = "DPL.GETCANDIDATES.BYADDR";
      }

      String sql = ExternalizedQuery.getSql(candidateKey);
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      query.setForReadOnly(true);
      List<Object[]> results = query.getResults();
      if (results != null && results.size() > 0) {
        for (Object[] record : results) {
          if (!records.containsKey(record[0])) {
            records.put((String) record[0], new ArrayList<String>());
          }
          records.get(record[0]).add((String) record[1]);
        }
      }

      // start the transaction
      transaction = entityManager.getTransaction();
      transaction.begin();

      if (geoHandler != null) {
        // geoHandler.doBeforeDPLCheck(entityManager, data, records);
      }

      List<String> names = null;
      DPLCheckResult dplResult = null;
      String errorInfo = null;
      for (String cntry : records.keySet()) {
        names = records.get(cntry);
        for (String name : names) {
          errorInfo = null;
          Boolean errorStatus = false;
          try {
            dplResult = dplCheckViaServices(reqId, cntry, name);
          } catch (Exception e) {
            log.error("Error in performing DPL Check when call EVS on Request ID" + reqId, e);
            if (dplResult == null) {
              dplResult = new DPLCheckResult();
            }
            errorStatus = true;
          }
          if (dplResult.isPassed()) {
            String sqlKey = "DPL.UPDATEPASSED";
            if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
              sqlKey = "DPL.UPDATEPASSED.BYADDR";
            }

            query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(sqlKey));
            query.setParameter("DPL_CHK_BY_ID", user.getIntranetId());
            query.setParameter("DPL_CHK_BY_NM", user.getBluePagesName());
            query.setParameter("REQ_ID", reqId);
            query.setParameter("CNTRY", cntry);
            query.setParameter("NAME", name.toUpperCase());
            query.executeSql();
          } else {
            errorInfo = "";
            if (dplResult.isUnderReview()) {
              errorInfo += " Export under review";
            }
            if (errorStatus) {
              errorInfo = MessageUtil.getMessage(MessageUtil.ERROR_DPL_EVS_ERROR);
            } else if (!StringUtils.isEmpty(dplResult.getFailureDesc())) {
              errorInfo += ", " + dplResult.getFailureDesc();
            }
            String sqlKey = "DPL.UPDATEFAILED";
            if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
              sqlKey = "DPL.UPDATEFAILED.BYADDR";
            }

            query = new PreparedQuery(entityManager, ExternalizedQuery.getSql(sqlKey));
            query.setParameter("DPL_CHK_BY_ID", user.getIntranetId());
            query.setParameter("DPL_CHK_BY_NM", user.getBluePagesName());

            List<String> available = EWSProperties.listAllDplExportLocation();

            List<String> passed = dplResult.getLocationsPassed();
            StringBuilder failedList = new StringBuilder();
            for (String loc : available) {
              if (passed != null && !passed.contains(loc) && !"ALL".equals(loc)) {
                failedList.append(failedList.length() > 0 ? ", " : "");
                failedList.append(loc);
              }
            }
            query.setParameter("DPL_CHK_ERR_LIST", failedList.toString());
            query.setParameter("REQ_ID", reqId);
            query.setParameter("CNTRY", cntry);
            query.setParameter("NAME", name.toUpperCase());
            query.setParameter("INFO", errorInfo);
            query.executeSql();
          }
        }
      }

      // compute the overall score
      recomputeDPLResult(user, entityManager, reqId);

      // commit only once
      transaction.commit();

    } catch (Exception e) {

      // rollback transaction when exception occurs
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }

      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        log.error("Error in performing DPL Check on Request ID" + ((CmrException) e).getMessage());
        throw (CmrException) e;
      } else {
        log.error("Error in performing DPL Check on Request ID" + reqId, e);
        throw new CmrException(MessageUtil.ERROR_DPL_ERROR);
      }
    } finally {
      // try to rollback, for safekeeping
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }

      // empty the manager
      entityManager.clear();
      entityManager.close();
      log.debug("Exiting performDPLCheck()");
    }
  }

  public void performDPLCheckPerAddress(AppUser user, long reqId) throws CmrException {
    log.debug("Entering performDPLCheck()");
    EntityTransaction transaction = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      ChangeLogListener.setManager(entityManager);
      GEOHandler geoHandler = null;

      Data data = dataService.getCurrentRecordById(reqId, entityManager);
      Admin admin = adminService.getCurrentRecordById(reqId, entityManager);

      if (data != null) {
        geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
      }

      String sql = ExternalizedQuery.getSql("DPL.NEW.GETADDRESSES");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      List<Addr> addresses = query.getResults(Addr.class);

      // start the transaction
      transaction = entityManager.getTransaction();
      transaction.begin();

      if (addresses == null || addresses.size() == 0) {
        // recompute here, to always ensure computation yields correct overall
        // status
        /* 1540420: Score card is not getting updated with DPL check result */
        recomputeDPLResult(user, entityManager, reqId);
        // commit only once
        transaction.commit();
        return;
      }

      for (Addr addr : addresses) {
        // initialize all
        addr.setDplChkResult(null);
      }

      if (geoHandler != null) {
        geoHandler.doBeforeDPLCheck(entityManager, data, addresses);
      }

      DPLCheckResult dplResult = null;
      String errorInfo = null;

      String soldToLandedCountry = null;
      for (Addr addr : addresses) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          soldToLandedCountry = addr.getLandCntry();
        }
      }
      for (Addr addr : addresses) {
        errorInfo = null;
        if (addr.getDplChkResult() == null) {
          Boolean errorStatus = false;
          Boolean isPrivate = isPrivate(data);
          try {
            dplResult = dplCheckAddress(admin, addr, soldToLandedCountry, data.getCmrIssuingCntry(),
                geoHandler != null ? !geoHandler.customerNamesOnAddress() : false, isPrivate);
          } catch (Exception e) {
            log.error("Error in performing DPL Check when call EVS on Request ID " + reqId + " Addr " + addr.getId().getAddrType() + "/"
                + addr.getId().getAddrSeq(), e);
            if (dplResult == null) {
              dplResult = new DPLCheckResult();
            }
            errorStatus = true;
          }
          if (dplResult.isPassed()) {
            addr.setDplChkResult("P");
            addr.setDplChkById(user.getIntranetId());
            addr.setDplChkByNm(user.getBluePagesName());
            addr.setDplChkErrList(null);
            addr.setDplChkInfo(null);
            addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
            entityManager.merge(addr);
          } else {
            errorInfo = "";
            if (dplResult.isUnderReview()) {
              errorInfo += " Export under review";
            }
            if (errorStatus) {
              errorInfo = MessageUtil.getMessage(MessageUtil.ERROR_DPL_EVS_ERROR);
            } else if (!StringUtils.isEmpty(dplResult.getFailureDesc())) {
              errorInfo += ", " + dplResult.getFailureDesc();
            }
            List<String> available = EWSProperties.listAllDplExportLocation();
            List<String> passed = dplResult.getLocationsPassed();
            StringBuilder failedList = new StringBuilder();
            for (String loc : available) {
              if (passed != null && !passed.contains(loc) && !"ALL".equals(loc)) {
                failedList.append(failedList.length() > 0 ? ", " : "");
                failedList.append(loc);
              }
            }

            addr.setDplChkResult("F");
            addr.setDplChkById(user.getIntranetId());
            addr.setDplChkByNm(user.getBluePagesName());
            addr.setDplChkErrList(failedList.toString());
            addr.setDplChkInfo(errorInfo);
            addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
            entityManager.merge(addr);
          }

        } else {
          addr.setDplChkById(user.getIntranetId());
          addr.setDplChkByNm(user.getBluePagesName());
          addr.setDplChkErrList(null);
          addr.setDplChkInfo(null);
          addr.setDplChkTs(SystemUtil.getCurrentTimestamp());
          entityManager.merge(addr);
        }
      }

      entityManager.flush();
      // compute the overall score
      recomputeDPLResult(user, entityManager, reqId);

      // commit only once
      transaction.commit();

    } catch (Exception e) {

      // rollback transaction when exception occurs
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }

      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        log.error("Error in performing DPL Check on Request ID" + ((CmrException) e).getMessage());
        throw (CmrException) e;
      } else {
        log.error("Error in performing DPL Check on Request ID" + reqId, e);
        throw new CmrException(MessageUtil.ERROR_DPL_ERROR);
      }
    } finally {
      // try to rollback, for safekeeping
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }

      ChangeLogListener.clearManager();
      // empty the manager
      entityManager.clear();
      entityManager.close();
      log.debug("Exiting performDPLCheck()");
    }
  }

  /**
   * @deprecated - use the new dplCheckAddress function
   * @param reqId
   * @param cntry
   * @param name
   * @return
   * @throws Exception
   */
  @Deprecated
  public DPLCheckResult dplCheckViaServices(long reqId, String cntry, String name) throws Exception {
    String servicesUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String appId = SystemConfiguration.getSystemProperty("evs.appID");
    DPLCheckClient dplClient = CmrServicesFactory.getInstance().createClient(servicesUrl, DPLCheckClient.class);
    DPLCheckRequest request = new DPLCheckRequest();

    String id = name;
    id = id.replaceAll(" ", "");
    id = id.replaceAll("&", "");
    id = id.replaceAll("'", "");
    id = id.replaceAll("\"", "");
    id = id.replaceAll("@", "");
    id = id.replaceAll("#", "");
    id = id.replaceAll("\\$", "");
    id = id.replaceAll("%", "");
    id = id.replaceAll("\\^", "");
    id = id.replaceAll("&", "");
    id = id.replaceAll("\\*", "");
    id = id.replaceAll("\\(", "");
    id = id.replaceAll("\\)", "");
    if (id.length() > 20) {
      id = id.substring(0, 20);
    }

    request.setCountry(cntry);
    request.setApplication(appId);
    request.setId(id);
    request.setCompanyName(name);
    log.debug("Performing DPL Check (service) on Request ID " + reqId + " (" + id + ")");

    DPLCheckResponse response = dplClient.executeAndWrap(DPLCheckClient.EVS_APP_ID, request, DPLCheckResponse.class);
    if (!response.isSuccess()) {
      throw new Exception("Failed to connect to DPL check service");
    } else {
      DPLCheckResult result = response.getResult();
      log.debug(" - Export checks passed = " + result.isPassed());
      log.debug(" - Export locations passed = " + result.getLocationsPassed().toString());

      // if export checks failed look if it's under reveiw and the failure type.
      if (!result.isPassed()) {
        log.debug(" - Customer under review: " + result.isUnderReview() + "  Export Failure: " + result.getFailureDesc());
      }
      return result;
    }
  }

  /**
   * Performs a DPL Check on the address specified
   * 
   * @param admin
   * @param addr
   * @param useNameOnMain
   * @return
   * @throws Exception
   */
  public DPLCheckResult dplCheckAddress(Admin admin, Addr addr, String soldToLandedCountry, String issuingCountry, boolean useNameOnMain,
      boolean isPrivate) throws Exception {
    String servicesUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String appId = SystemConfiguration.getSystemProperty("evs.appID");
    DPLCheckClient dplClient = CmrServicesFactory.getInstance().createClient(servicesUrl, DPLCheckClient.class);
    DPLCheckRequest request = new DPLCheckRequest();

    String id = addr.getId().getReqId() + "-" + addr.getId().getAddrType() + "-" + addr.getId().getAddrSeq();
    if (id.length() > 20) {
      id = id.substring(0, 20);
    }

    String name = addr.getCustNm1() + (!StringUtils.isEmpty(addr.getCustNm2()) ? " " + addr.getCustNm2() : "");
    if (useNameOnMain) {
      name = admin.getMainCustNm1() + (!StringUtils.isEmpty(admin.getMainCustNm2()) ? " " + admin.getMainCustNm2() : "");
    }
    request.setCountry(addr.getLandCntry());
    if (StringUtils.isBlank(addr.getLandCntry())) {
      if (!StringUtils.isBlank(soldToLandedCountry)) {
        request.setCountry(soldToLandedCountry);
      } else {
        request.setCountry(PageManager.getDefaultLandedCountry(issuingCountry));
      }
      if (StringUtils.isBlank(addr.getLandCntry())) {
        request.setCountry("US");
      }
    }
    request.setApplication(appId);
    request.setCity(addr.getCity1());
    request.setAddr1(addr.getAddrTxt());
    request.setAddr2(addr.getAddrTxt2());
    request.setId(id);
    request.setPrivate(isPrivate);
    if (JPHandler.isJPIssuingCountry(issuingCountry))
      request.setCompanyName(addr.getCustNm3());
    else
      request.setCompanyName(name);
    log.debug("Performing DPL Check (service) on Request ID " + admin.getId().getReqId() + " (" + id + ")");
    log.debug(" - Name: " + name);
    String dplSystemId = SystemUtil.useKYCForDPLChecks() ? DPLCheckClient.KYC_APP_ID : DPLCheckClient.EVS_APP_ID;
    DPLCheckResponse response = dplClient.executeAndWrap(dplSystemId, request, DPLCheckResponse.class);
    if (!response.isSuccess()) {
      throw new Exception("Failed to connect to DPL check service: " + response.getMsg());
    } else {
      DPLCheckResult result = response.getResult();
      log.debug(" - Export checks passed = " + result.isPassed());
      log.debug(" - Export locations passed = " + result.getLocationsPassed().toString());

      // if export checks failed look if it's under reveiw and the failure type.
      if (!result.isPassed()) {
        log.debug(" - Customer under review: " + result.isUnderReview() + "  Export Failure: " + result.getFailureDesc());
      }
      return result;
    }
  }

  /**
   * @deprecated use dplCheckViaServices instead
   * @param reqId
   * @param cntry
   * @param name
   * @return
   * @throws Exception
   */
  @Deprecated
  protected CEEroData dplCheck(long reqId, String cntry, String name) throws Exception {
    String url = SystemConfiguration.getValue("EVS_URL");
    String user = System.getProperty("evs.user");
    String pass = System.getProperty("evs.password");

    EWSClient client = new EWSClient(url, user, pass);

    CEAppData appData = new CEAppData();
    appData.setAppID(System.getProperty("evs.appID"));

    appData.addDownloadSite("ALL");
    appData.setExportOverrideSupport(false);

    CECustomerData custData = new CECustomerData();
    String id = name;
    id = id.replaceAll(" ", "");
    id = id.replaceAll("&", "");
    id = id.replaceAll("'", "");
    id = id.replaceAll("\"", "");
    id = id.replaceAll("@", "");
    id = id.replaceAll("#", "");
    id = id.replaceAll("\\$", "");
    id = id.replaceAll("%", "");
    id = id.replaceAll("\\^", "");
    id = id.replaceAll("&", "");
    id = id.replaceAll("\\*", "");
    id = id.replaceAll("\\(", "");
    id = id.replaceAll("\\)", "");
    if (id.length() > 20) {
      id = id.substring(0, 20);
    }

    custData.setCustID(reqId + "-" + cntry + "-" + id);
    custData.setDataSrcID(SystemConfiguration.getSystemProperty("evs.appID"));
    custData.setCntryCode(cntry);
    custData.setLangLoc("en_US");
    custData.setCompany(name);
    // custData.setLastName("Osama bin Laden"); // DPL failure test

    log.debug("Performing DPL Check on Request ID " + reqId + " (" + custData.getCustID() + ")");
    CEEroData eroData = client.basicChecksDetailsAPI(appData, custData);

    log.debug(" - Export checks passed = " + eroData.getChecksPassed());
    log.debug(" - Export locations passed = " + eroData.getExportLocationsAllowedForAllEROIDs().toString());

    // if export checks failed look if it's under reveiw and the failure type.
    if (!eroData.getChecksPassed()) {
      log.debug(" - Customer under review: " + eroData.isExportUnderReview() + "  Export Failure: " + eroData.getExportFailureTypeDescription());
      log.debug(" - Export Failure: " + eroData.getExportFailureTypeDescription());
    }

    return eroData;

  }

  public void recomputeDPLResult(AppUser user, long reqId) throws Exception {
    EntityTransaction transaction = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      // start the transaction
      transaction = entityManager.getTransaction();
      transaction.begin();

      recomputeDPLResult(user, entityManager, reqId);

      transaction.commit();
    } catch (Exception e) {
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      throw e;
    } finally {
      // try to rollback, for safekeeping
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  public static void clearDplResults(long reqId) throws Exception {
    EntityTransaction transaction = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      // start the transaction
      transaction = entityManager.getTransaction();
      transaction.begin();

      clearDplResults(entityManager, reqId);

      transaction.commit();
    } catch (Exception e) {
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      throw e;
    } finally {
      // try to rollback, for safekeeping
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  public static void clearDplResults(EntityManager entityManager, long reqId) {
    String addrClearSql = ExternalizedQuery.getSql("DPL.CLEARADDR");
    String scoreClearSql = ExternalizedQuery.getSql("DPL.CLEARSCORECARD");

    PreparedQuery query = new PreparedQuery(entityManager, addrClearSql);
    query.setParameter("REQ_ID", reqId);
    query.executeSql();

    query = new PreparedQuery(entityManager, scoreClearSql);
    query.setParameter("REQ_ID", reqId);
    query.executeSql();
  }

  public void recomputeDPLResult(AppUser user, EntityManager entityManager, long reqId) throws CmrException {

    Scorecard scorecard = getScorecardRecord(entityManager, reqId);

    if (scorecard == null) {
      return;
    }

    String currentResult = scorecard.getDplChkResult();

    this.log.debug("Recomputing DPL Results for Request ID " + reqId);
    String sql = ExternalizedQuery.getSql("DPL.GETDPLCOUNTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      int all = 0;
      int passed = 0;
      int failed = 0;
      int notdone = 0;
      int notrequired = 0;
      for (Object[] record : results) {
        if ("ALL".equals(record[0])) {
          all = Integer.parseInt(record[1].toString());
        } else if ("PASSED".equals(record[0])) {
          passed = Integer.parseInt(record[1].toString());
        } else if ("FAILED".equals(record[0])) {
          failed = Integer.parseInt(record[1].toString());
        } else if ("NOTDONE".equals(record[0])) {
          notdone = Integer.parseInt(record[1].toString());
        } else if ("NOTREQUIRED".equals(record[0])) {
          notrequired = Integer.parseInt(record[1].toString());
        }
      }

      if (all == notrequired) {
        scorecard.setDplChkResult("NR");
        // not required
      } else if ((all == passed + notrequired) || (all == passed)) {
        scorecard.setDplChkResult("AP");
        // all passed
      } else if ((all == failed + notrequired) || (all == failed)) {
        // all failed
        scorecard.setDplChkResult("AF");
      } else if ((passed > 0 && all != passed) || (failed > 0 && all != failed)) {
        // some passed, some failed/not done
        scorecard.setDplChkResult("SF");
      }

      // if there is at least one Not done, set to not done
      if (notdone > 0) {
        scorecard.setDplChkResult("Not Done");
      }
      if (notdone != all) {
        // update if DPL has indeed been performed
        scorecard.setDplChkTs(SystemUtil.getCurrentTimestamp());
        scorecard.setDplChkUsrId(user.getIntranetId());
        scorecard.setDplChkUsrNm(user.getBluePagesName());
      }
      if (scorecard.getDplChkUsrId() == null) {
        scorecard.setDplChkUsrId(user.getIntranetId());
        scorecard.setDplChkUsrNm(user.getBluePagesName());
      }

      if (currentResult == null || !currentResult.equals(scorecard.getDplChkResult())) {
        // it has changed, clear assessment
        scorecard.setDplAssessmentBy(null);
        scorecard.setDplAssessmentDate(null);
        scorecard.setDplAssessmentCmt(null);
        scorecard.setDplAssessmentResult(null);
      }
      this.log.debug(" - DPL Status for Request ID " + reqId + " : " + scorecard.getDplChkResult());
      updateEntity(scorecard, entityManager);

      if (failed > 0) {
        this.log.debug("Performing DPL Search for Request " + reqId + " with DPL Status: " + scorecard.getDplChkResult());

        ParamContainer params = new ParamContainer();
        params.addParam("processType", "ATTACH");
        params.addParam("reqId", reqId);
        params.addParam("user", user);
        params.addParam("filePrefix", "AutoDPLSearch_");

        try {
          DPLSearchService dplService = new DPLSearchService();
          // dplService.process(null, params);
          dplService.doProcess(entityManager, null, params);
        } catch (Exception e) {
          this.log.warn("DPL results not attached to the request", e);
        }
      }
    }
  }

  public Scorecard getScorecardRecord(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("DPL.GETSCORECARD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Scorecard> records = query.getResults(1, Scorecard.class);
    if (records != null && records.size() > 0) {
      return records.get(0);
    }
    return new Scorecard();
  }

  private void assignCountyName(EntityManager entityManager, Addr address) {
    if ("US".equals(address.getLandCntry()) && !StringUtils.isBlank(address.getCounty())) {
      String sql = ExternalizedQuery.getSql("ADDRESS.GETCOUNTYNAME");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("STATE", address.getStateProv());
      query.setParameter("COUNTRY", address.getLandCntry());
      query.setParameter("COUNTY", address.getCounty());
      String countyName = query.getSingleResult(String.class);
      if (!StringUtils.isEmpty(countyName)) {
        address.setCountyName(countyName);
        this.log.debug("County Assigned: " + address.getCounty() + " - " + address.getCountyName());
      }
    } else if (!"US".equals(address.getLandCntry())) {
      String county = "";
      if (!StringUtils.isBlank(address.getCountyName())) {
        county = address.getCountyName().trim();
      } else {
        this.log.debug("Landed Country: " + address.getLandCntry());
        String sql = ExternalizedQuery.getSql("ADDRESS.GETCNTRY");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("COUNTRY", address.getLandCntry());
        county = query.getSingleResult(String.class);
      }

      String sql = ExternalizedQuery.getSql("ADDRESS.GETNONUSCOUNTY");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CITY", address.getCity1());
      query.setParameter("COUNTY", county != null && county.length() > 1 ? county.substring(0, 2).toUpperCase() + "%" : county);
      List<Object[]> results = query.getResults(1);
      if (results != null && results.size() > 0) {
        address.setCountyName((String) results.get(0)[1]);
        address.setCounty((String) results.get(0)[0]);
        this.log.debug("County Assigned: " + address.getCounty() + " - " + address.getCountyName());
      }

    }
  }

  /**
   * @param reqId
   * @param entityManager
   * @param address
   * @param model
   * @return
   * @throws Exception
   */
  public void assignLocationCode(EntityManager entityManager, Addr address, String issuingCntry) throws Exception {
    Object[] result = new Object[1];
    String sql = ""; // ExternalizedQuery.getSql("GET_REQ_ISSUING_CNTRY");
    PreparedQuery query = null; // new PreparedQuery(entityManager, sql);
    List<Object[]> results = new ArrayList<Object[]>();
    log.debug("*** Begin location code assignment for request ID " + address.getId().getReqId());
    if (LAHandler.isLACountry(issuingCntry)) {
      sql = ExternalizedQuery.getSql("GET_LOCATION_CD");
      sql = sql.replace("STATE_CD", address.getStateProv());
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("CITY_DESC", address.getCity1());
      query.setParameter("ISSUING_CNTRY", issuingCntry);
      results = query.getResults();
      if (results != null && results.size() > 0) {
        result = results.get(0);
        String tempLocCode = result[0] != null ? (String) result[0] : "";
        // String suffix = tempLocCode.substring(0, 2);
        // tempLocCode = tempLocCode.replace(suffix, "");
        // change for Defect 1467888 fix
        tempLocCode = tempLocCode.substring(2, tempLocCode.length());
        log.debug("*** Assigning " + tempLocCode + " as location code for request " + address.getId().getReqId());
        address.setLocationCode(tempLocCode);
      }
    } else {
      address.setLocationCode("");

    }
    log.debug("*** End location code assignment for request ID " + address.getId().getReqId());
  }

  /**
   * Performs an update of the data on DATA table based on the saved current
   * ZS01 address landed country. Currently only applicable for BR requests
   * 
   * @author Dennis Natad
   * @param entityManager
   * @param request
   * @param addr
   */
  public void updateDataForBRCreate(EntityManager entityManager, HttpServletRequest request, Addr addr) {
    DataService ds = new DataService();
    AdminService as = new AdminService();
    Data currentData = new Data();
    Admin currentAdmin = new Admin();
    RequestEntryModel model = new RequestEntryModel();

    try {
      if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(addr.getId().getAddrType())) {
        model.setReqId(addr.getId().getReqId());
        currentData = ds.getCurrentRecord(model, entityManager, request);
        currentAdmin = as.getCurrentRecord(model, entityManager, request);

        if (!"BR".equals(addr.getLandCntry())) {
          currentData.setPartnershipInd("N");
          currentData.setMarketingContCd("0");
          if (CmrConstants.REQ_TYPE_CREATE.equals(currentAdmin.getReqType())) {
            currentData.setLeasingCompanyIndc("N");
          }
        } else {
          if ("BUSPR".equalsIgnoreCase(currentAdmin.getCustType())) {
            currentData.setPartnershipInd("Y");
            currentData.setMarketingContCd("1");
          } else {
            currentData.setPartnershipInd("N");
            currentData.setMarketingContCd("0");
          }
          if (CmrConstants.REQ_TYPE_CREATE.equals(currentAdmin.getReqType())) {
            if ("LEASI".equalsIgnoreCase(currentAdmin.getCustType())) {
              currentData.setLeasingCompanyIndc("Y");
            } else {
              currentData.setLeasingCompanyIndc("N");
            }
          }
        }

        if (CmrConstants.REQ_TYPE_UPDATE.equals(currentAdmin.getReqType())) {
          if (CmrConstants.CUST_CLASS_33.equals(currentData.getCustClass()) || CmrConstants.CUST_CLASS_34.equals(currentData.getCustClass())) {
            currentData.setLeasingCompanyIndc(CmrConstants.DEFAULT_LEASI_LEASINGCOMP_IND);
          } else {
            currentData.setLeasingCompanyIndc(CmrConstants.DEFAULT_NONLEASI_LEASINGCOMP_IND);
          }
        }

        Map<String, Object> hwBoRepTeam = getHWBranchOffRepTeam(addr.getStateProv());

        // Story 1247153
        if (hwBoRepTeam != null && !hwBoRepTeam.isEmpty()) {
          currentData.setHwSvcsBoNo(hwBoRepTeam.get("hardwBO") != null ? hwBoRepTeam.get("hardwBO").toString() : "");
          currentData.setHwSvcsRepTeamNo(hwBoRepTeam.get("hardwRTNo") != null ? hwBoRepTeam.get("hardwRTNo").toString() : "");
          currentData.setLocationNumber(hwBoRepTeam.get("locationNo") != null ? hwBoRepTeam.get("locationNo").toString() : "");
          currentData.setHwSvcsTeamCd(CmrConstants.DEFAULT_TEAM_CD);
          currentData.setHwSvcRepTmDateOfAssign(SystemUtil.getCurrentTimestamp());
        }

        updateEntity(currentData, entityManager);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Utility method to return the mapped Hardware Branch Office and Rep Team
   * code from the constant HARDWARE_BO_REPTEAM_MAP. This is as required by
   * Story 1247153
   * 
   * @author Dennis Natad
   * @param state
   * @return
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getHWBranchOffRepTeam(String state) {
    log.debug("Starting retrieval of HW Branch Office and Rep Team Code based on State Code");
    Map<String, Object> hwBranchOffRepTeam = CmrConstants.HARDWARE_BO_REPTEAM_MAP;
    log.debug("*** Map of constant values size is " + hwBranchOffRepTeam.size());
    log.debug("*** Retrieving for state code " + state);
    HashMap<String, Object> hwLocal = (HashMap<String, Object>) hwBranchOffRepTeam.get(state);
    log.debug("*** Retrieved values for state code " + state + (hwLocal != null ? " is not empty == " + hwLocal.isEmpty() : "is empty"));
    return hwLocal;
  }

  private void updateAdminRec(AddressModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {

    if ("ZS01".equals(model.getAddrType())) {
      log.debug("Setting customer name from main address type..");
      AdminPK pk = new AdminPK();
      pk.setReqId(model.getReqId());
      Admin admin = entityManager.find(Admin.class, pk);
      admin.setMainCustNm1(model.getCustNm1());
      admin.setMainCustNm2(model.getCustNm2());
      admin.setMainAddrType("ZS01");
      updateEntity(admin, entityManager);
    }
  }

  public void updateDataRecord(EntityManager entityManager, long reqId, String addrType, String addrSeq, String abbrevLoc) throws CmrException {
    Data dataRecords = getDataRecord(entityManager, reqId, addrType, addrSeq);
    if (abbrevLoc.length() > 12) {
      dataRecords.setAbbrevLocn(abbrevLoc.substring(0, 12));
    } else {
      dataRecords.setAbbrevLocn(abbrevLoc);
    }
    updateEntity(dataRecords, entityManager);
  }

  private Data getDataRecord(EntityManager entityManager, long reqId, String addrType, String addrSeq) throws CmrException {
    String sql = ExternalizedQuery.getSql("QUERY.DATA.GET.ABBREVLOC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Data> dataRecord = query.getResults(1, Data.class);
    if (dataRecord != null && dataRecord.size() > 0) {
      return dataRecord.get(0);
    }
    return null;
  }

  private void setStateProvForITAddr(EntityManager entityManager, Addr addres) {

    // If landed country is not Italy, that means it is crossborder
    if (!"IT".equalsIgnoreCase(addres.getLandCntry()) && !"OTH".equalsIgnoreCase(addres.getLocationCode())) {
      // we need to pass the value of locationCode to stateProv
      addres.setStateProv(addres.getLocationCode());
      updateEntity(addres, entityManager);
    } else if (!"IT".equalsIgnoreCase(addres.getLandCntry()) && "OTH".equalsIgnoreCase(addres.getLocationCode())) {
      addres.setStateProv(addres.getCity2());
      updateEntity(addres, entityManager);
    }
  }

  public void updateSSAStateProvLocnCode(EntityManager em, String stateProv, String locCd, String reqId, String addrType) {
    String strUpdQuery = ExternalizedQuery.getSql("UPDATE.ADDRESS.SSA.STATEPROV.LOCCD");
    strUpdQuery = strUpdQuery.replace(":STATE_PROV", stateProv);
    strUpdQuery = strUpdQuery.replace(":REQ_ID", reqId);
    strUpdQuery = strUpdQuery.replace(":LOCN_CD", locCd);
    strUpdQuery = strUpdQuery.replace(":ADDR_TYPE", addrType);
    initLogger().debug("SQL FOR UPDATING STATE PROV LOCN >> " + strUpdQuery);

    String strSelQuery = ExternalizedQuery.getSql("GET.ADDRESS.SSA.BY.REQID.ADDRTYP");
    PreparedQuery query = new PreparedQuery(em, strSelQuery);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYP", addrType);
    List<Addr> addresses = query.getResults(Addr.class);

    if (addresses != null && addresses.size() > 0) {
      query = new PreparedQuery(em, strUpdQuery);
      query.executeSql();
    }
  }

  public String getAddressSapNo(EntityManager em, String reqId, String addrType) {
    String sapNo = "";
    String qryGetSapNo = ExternalizedQuery.getSql("GET.ADDRESS.SSA.BY.REQID.ADDRTYP");
    PreparedQuery query = new PreparedQuery(em, qryGetSapNo);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYP", addrType);
    List<Addr> addresses = query.getResults(Addr.class);

    if (addresses != null && addresses.size() > 0) {
      Addr single = addresses.get(0);
      sapNo = single.getSapNo();
    }

    return sapNo;
  }

  public void createJPIntlAddr(AddressModel model, Addr addr, EntityManager entityManager) {
    IntlAddr iAddr = createIntlAddrFromModel(model, addr, entityManager);
    IntlAddr iAddrExist = getIntlAddrById(addr, entityManager);
    if (iAddrExist != null) {
      updateEntity(iAddr, entityManager);
    } else {
      createEntity(iAddr, entityManager);
    }
  }

  public void createCNIntlAddr(AddressModel model, Addr addr, EntityManager entityManager) {
    int tempNewLen = 0;
    String newTxt = "";

    if (CNHandler.getLengthInUtf8(model.getCnAddrTxt()) > CNHandler.CN_STREET_ADD_TXT) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnAddrTxt(), CNHandler.CN_STREET_ADD_TXT, CNHandler.CN_STREET_ADD_TXT);
      newTxt = model.getCnAddrTxt() != null ? model.getCnAddrTxt().substring(0, tempNewLen) : "";
      String excess = model.getCnAddrTxt().substring(tempNewLen);
      model.setCnAddrTxt(newTxt);
      model.setCnAddrTxt2(excess + model.getCnAddrTxt2());
    }

    if (CNHandler.getLengthInUtf8(model.getCnAddrTxt2()) > CNHandler.CN_STREET_ADD_TXT2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnAddrTxt2(), CNHandler.CN_STREET_ADD_TXT2, CNHandler.CN_STREET_ADD_TXT2);
      newTxt = model.getCnAddrTxt2() != null ? model.getCnAddrTxt2().substring(0, tempNewLen) : "";
      model.setCnAddrTxt2(newTxt);
    }

    if (CNHandler.getLengthInUtf8(model.getCnCustName1()) > CNHandler.CN_CUST_NAME_1) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnCustName1(), CNHandler.CN_CUST_NAME_1, CNHandler.CN_CUST_NAME_1);
      newTxt = model.getCnCustName1() != null ? model.getCnCustName1().substring(0, tempNewLen) : "";
      String excess = model.getCnCustName1().substring(tempNewLen);
      model.setCnCustName1(newTxt);
      model.setCnCustName2(excess + model.getCnCustName2());
    }

    if (CNHandler.getLengthInUtf8(model.getCnCustName2()) > CNHandler.CN_CUST_NAME_2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnCustName2(), CNHandler.CN_CUST_NAME_2, CNHandler.CN_CUST_NAME_2);
      newTxt = model.getCnCustName2() != null ? model.getCnCustName2().substring(0, tempNewLen) : "";
      model.setCnCustName2(newTxt);
    }

    if (CNHandler.getLengthInUtf8(model.getCnCustName3()) > CNHandler.CN_CUST_NAME_2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnCustName3(), CNHandler.CN_CUST_NAME_2, CNHandler.CN_CUST_NAME_2);
      newTxt = model.getCnCustName3() != null ? model.getCnCustName3().substring(0, tempNewLen) : "";
      model.setCnCustName3(newTxt);
    }

    IntlAddr iAddr = createIntlAddrFromModel(model, addr, entityManager);
    createEntity(iAddr, entityManager);
  }

  private IntlAddr createIntlAddrFromModel(AddressModel model, Addr addr, EntityManager entityManager) {
    IntlAddr iAddr = new IntlAddr();
    IntlAddrPK iAddrPK = new IntlAddrPK();

    iAddrPK.setAddrSeq(addr.getId().getAddrSeq());
    iAddrPK.setAddrType(addr.getId().getAddrType());
    iAddrPK.setReqId(addr.getId().getReqId());

    iAddr.setId(iAddrPK);
    iAddr.setIntlCustNm1(model.getCnCustName1());
    iAddr.setIntlCustNm2(model.getCnCustName2());
    iAddr.setIntlCustNm3(model.getCnCustName3());
    iAddr.setAddrTxt(model.getCnAddrTxt());
    iAddr.setIntlCustNm4(model.getCnAddrTxt2());
    iAddr.setCity1(model.getCnCity());
    iAddr.setCity2(model.getCnDistrict());
    iAddr.setLangCd(StringUtils.isEmpty(getCustPrefLang(addr, entityManager)) ? "1" : getCustPrefLang(addr, entityManager));

    return iAddr;

  }

  public IntlAddr createIntlAddrFromModel(FindCMRRecordModel model, Addr addr, EntityManager entityManager) {
    IntlAddr iAddr = new IntlAddr();
    IntlAddrPK iAddrPK = new IntlAddrPK();

    iAddrPK.setAddrSeq(addr.getId().getAddrSeq());
    iAddrPK.setAddrType(addr.getId().getAddrType());
    iAddrPK.setReqId(addr.getId().getReqId());

    iAddr.setId(iAddrPK);
    iAddr.setIntlCustNm1(
        StringUtils.isNoneBlank(model.getCmrName()) ? model.getCmrName() : (StringUtils.isNoneBlank(model.getCmrName3()) ? model.getCmrName3() : ""));
    iAddr.setIntlCustNm2(StringUtils.isNoneBlank(model.getCmrName2()) ? model.getCmrName2() : "");
    iAddr.setAddrTxt(model.getCmrStreet());
    iAddr.setIntlCustNm4("");
    iAddr.setCity1(model.getCmrCity());
    iAddr.setCity2(model.getCmrCity2());
    iAddr.setLangCd(StringUtils.isEmpty(getCustPrefLang(addr, entityManager)) ? "1" : getCustPrefLang(addr, entityManager));

    return iAddr;

  }

  public boolean updateJPIntlAddr(AddressModel model, EntityManager entityManager, Addr addr) {
    IntlAddr iAddr = getIntlAddrById(addr, entityManager);

    if (iAddr != null) {
      iAddr.setIntlCustNm1(model.getCnCustName1());
      iAddr.setIntlCustNm2(model.getCnCustName2());
      iAddr.setIntlCustNm3(model.getCnCustName3());
      iAddr.setAddrTxt(model.getCnAddrTxt());
      iAddr.setIntlCustNm4(model.getCnAddrTxt2());
      iAddr.setCity1(model.getCnCity());
      iAddr.setCity2(model.getCnDistrict());

      updateEntity(iAddr, entityManager);

    } else {
      createJPIntlAddr(model, addr, entityManager);

    }
    return true;
  }

  public boolean updateCNIntlAddr(AddressModel model, EntityManager entityManager, Addr addr) {
    IntlAddr iAddr = getIntlAddrById(addr, entityManager);

    int tempNewLen = 0;
    String newTxt = "";

    if (CNHandler.getLengthInUtf8(model.getCnAddrTxt()) > CNHandler.CN_STREET_ADD_TXT) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnAddrTxt(), CNHandler.CN_STREET_ADD_TXT, CNHandler.CN_STREET_ADD_TXT);
      newTxt = model.getCnAddrTxt() != null ? model.getCnAddrTxt().substring(0, tempNewLen) : "";
      String excess = model.getCnAddrTxt().substring(tempNewLen);
      model.setCnAddrTxt(newTxt);
      model.setCnAddrTxt2(excess + model.getCnAddrTxt2());
    }

    if (CNHandler.getLengthInUtf8(model.getCnAddrTxt2()) > CNHandler.CN_STREET_ADD_TXT2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnAddrTxt2(), CNHandler.CN_STREET_ADD_TXT2, CNHandler.CN_STREET_ADD_TXT2);
      newTxt = model.getCnAddrTxt2() != null ? model.getCnAddrTxt2().substring(0, tempNewLen) : "";
      model.setCnAddrTxt2(newTxt);
    }

    if (CNHandler.getLengthInUtf8(model.getCnCustName1()) > CNHandler.CN_CUST_NAME_1) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnCustName1(), CNHandler.CN_CUST_NAME_1, CNHandler.CN_CUST_NAME_1);
      newTxt = model.getCnCustName1() != null ? model.getCnCustName1().substring(0, tempNewLen) : "";
      String excess = model.getCnCustName1().substring(tempNewLen);
      model.setCnCustName1(newTxt);
      model.setCnCustName2(excess + model.getCnCustName2());
    }

    if (CNHandler.getLengthInUtf8(model.getCnCustName2()) > CNHandler.CN_CUST_NAME_2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnCustName2(), CNHandler.CN_CUST_NAME_2, CNHandler.CN_CUST_NAME_2);
      newTxt = model.getCnCustName2() != null ? model.getCnCustName2().substring(0, tempNewLen) : "";
      model.setCnCustName2(newTxt);
    }

    if (CNHandler.getLengthInUtf8(model.getCnCustName3()) > CNHandler.CN_CUST_NAME_2) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(model.getCnCustName3(), CNHandler.CN_CUST_NAME_2, CNHandler.CN_CUST_NAME_2);
      newTxt = model.getCnCustName3() != null ? model.getCnCustName3().substring(0, tempNewLen) : "";
      model.setCnCustName3(newTxt);
    }

    if (iAddr != null) {
      iAddr.setIntlCustNm1(model.getCnCustName1());
      iAddr.setIntlCustNm2(model.getCnCustName2());
      iAddr.setIntlCustNm3(model.getCnCustName3());
      iAddr.setAddrTxt(model.getCnAddrTxt());
      iAddr.setIntlCustNm4(model.getCnAddrTxt2());
      iAddr.setCity1(model.getCnCity());
      iAddr.setCity2(model.getCnDistrict());

      updateEntity(iAddr, entityManager);

    } else {
      createCNIntlAddr(model, addr, entityManager);

    }
    return true;
  }

  public IntlAddr getIntlAddrById(Addr addr, EntityManager entityManager) {
    IntlAddr iAddr = new IntlAddr();
    String qryIntlAddrById = ExternalizedQuery.getSql("GET.INTL_ADDR_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, qryIntlAddrById);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());

    iAddr = query.getSingleResult(IntlAddr.class);

    return iAddr;
  }

  private String getCustPrefLang(Addr addr, EntityManager entityManager) {
    Data custPrefLang = null;
    String qryIntlAddrById = ExternalizedQuery.getSql("BATCH.GET_DATA");
    PreparedQuery query = new PreparedQuery(entityManager, qryIntlAddrById);
    query.setParameter("REQ_ID", addr.getId().getReqId());

    custPrefLang = query.getSingleResult(Data.class);

    if (custPrefLang == null) {
      return "";
    } else {
      return custPrefLang.getCustPrefLang();
    }

  }

  public GeoContactInfo getGeoContactInfoById(Addr addr, EntityManager entityManager) {
    GeoContactInfo geoContactInfo = new GeoContactInfo();
    String qryGeoContactInfoById = ExternalizedQuery.getSql("GET.GEO_CONTACT_INFO_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, qryGeoContactInfoById);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());

    geoContactInfo = query.getSingleResult(GeoContactInfo.class);

    return geoContactInfo;
  }

  public void createCNGeoContactInfo(AddressModel model, Addr addr, EntityManager entityManager) {
    GeoContactInfo geoContactInfo = createGeoContactInfoFromModel(model, addr, entityManager);
    createEntity(geoContactInfo, entityManager);
  }

  private GeoContactInfo createGeoContactInfoFromModel(AddressModel model, Addr addr, EntityManager entityManager) {
    GeoContactInfo geoContactInfo = new GeoContactInfo();
    GeoContactInfoPK geoContactInfoPK = new GeoContactInfoPK();
    int contactId = 0;
    try {
      contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(addr.getId().getReqId()));
      geoContactInfoPK.setContactInfoId(contactId);
    } catch (CmrException ex) {
      log.debug("Exception while getting contactId : " + ex.getMessage(), ex);
    }
    geoContactInfoPK.setReqId(addr.getId().getReqId());
    geoContactInfo.setId(geoContactInfoPK);

    geoContactInfo.setContactFunc(model.getCnCustContJobTitle());
    geoContactInfo.setContactName(model.getCnCustContNm());
    geoContactInfo.setContactPhone(model.getCnCustContPhone2());
    geoContactInfo.setContactType(addr.getId().getAddrType());
    geoContactInfo.setContactSeqNum(addr.getId().getAddrSeq());
    geoContactInfo.setCreateTs(SystemUtil.getCurrentTimestamp());
    geoContactInfo.setUpdtTs(SystemUtil.getCurrentTimestamp());

    return geoContactInfo;

  }

  public boolean updateCNGeoContactInfo(AddressModel model, EntityManager entityManager, Addr addr) {
    GeoContactInfo geoContactInfo = getGeoContactInfoById(addr, entityManager);

    if (geoContactInfo != null) {
      geoContactInfo.setContactFunc(model.getCnCustContJobTitle());
      geoContactInfo.setContactName(model.getCnCustContNm());
      geoContactInfo.setContactPhone(model.getCnCustContPhone2());
      geoContactInfo.setUpdtTs(SystemUtil.getCurrentTimestamp());

      updateEntity(geoContactInfo, entityManager);

    } else {
      createCNGeoContactInfo(model, addr, entityManager);

    }
    return true;
  }

  public void createMachines(AddressModel model, Addr addr, EntityManager entityManager, HttpServletRequest request) throws CmrException {

    if ((addr.getId().getAddrType().equalsIgnoreCase("ZI01") || addr.getId().getAddrType().equalsIgnoreCase("ZP02"))
        && !model.getMachineTyp().isEmpty() && !model.getMachineSerialNo().isEmpty()) {

      this.log.debug("Creating Machines To Install for  Addr record:  " + " [Request ID: " + model.getReqId() + " ,Addr Type: " + model.getAddrType()
          + " ,Addr Seq: " + model.getAddrSeq() + "]");
      AppUser user = AppUser.getUser(request);

      if (machineExists(entityManager, model, addr)) {
        throw new CmrException(MessageUtil.ERROR_MACHINE_ALREADY_EXISTS, model.getMachineTyp() + model.getMachineSerialNo());
      }

      MachinesToInstall machines = new MachinesToInstall();
      MachinesToInstallPK machinesPK = new MachinesToInstallPK();

      // Setting primary key fields
      machinesPK.setReqId(addr.getId().getReqId());
      machinesPK.setAddrType(addr.getId().getAddrType());
      machinesPK.setAddrSeq(addr.getId().getAddrSeq());
      machinesPK.setMachineTyp(model.getMachineTyp());
      machinesPK.setMachineSerialNo(model.getMachineSerialNo());

      // setting ramaining fields
      machines.setId(machinesPK);
      machines.setCreateBy(user.getIntranetId());
      machines.setCurrentIndc(""); // Confirm from Jeff
      machines.setLastUpdtBy(user.getIntranetId());

      machines.setCreateTs(SystemUtil.getCurrentTimestamp());
      machines.setLastUpdtTs(machines.getCreateTs());

      createEntity(machines, entityManager);
    }
  }

  public void deleteMachines(AddressModel model, Addr addr, EntityManager entityManager, HttpServletRequest request) {
    this.log.debug("Deleting Machines To Install for  Addr record:  " + " [Request ID: " + model.getReqId() + " ,Addr Type: " + model.getAddrType()
        + " ,Addr Seq: " + model.getAddrSeq() + "]");

    String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_MACHINES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());

    List<MachinesToInstall> machines = query.getResults(MachinesToInstall.class);

    for (MachinesToInstall machine : machines) {
      deleteEntity(machine, entityManager);
    }
  }

  public void deleteSingleMachine(AddressModel model, Addr addr, EntityManager entityManager, HttpServletRequest request) {
    this.log.debug("Deleting Machine To Install for  Addr record:  " + " [Request ID: " + model.getReqId() + " ,Addr Type: " + model.getAddrType()
        + " ,Addr Seq: " + model.getAddrSeq() + "]");

    String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_SINGLE_MACHINE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setParameter("MACHINE_SERIAL_NO", model.getMachineSerialNo());
    query.setParameter("MACHINE_TYP", model.getMachineTyp());

    List<MachinesToInstall> machines = query.getResults(MachinesToInstall.class);

    for (MachinesToInstall machine : machines) {
      deleteEntity(machine, entityManager);
    }
  }

  private boolean machineExists(EntityManager entityManager, AddressModel model, Addr addr) {
    String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_SINGLE_MACHINE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    query.setParameter("MACHINE_SERIAL_NO", model.getMachineSerialNo());
    query.setParameter("MACHINE_TYP", model.getMachineTyp());
    return query.exists();
  }

  private void updateCNCityInfo(EntityManager entityManager, Addr address, AddressModel model) {

    // If landed country is not China, that means it is crossborder
    if (!"CN".equalsIgnoreCase(address.getLandCntry())) {
      // we need to pass the value of locationCode to stateProv
      address.setCity1(model.getCity1());
      updateEntity(address, entityManager);
    } else {
      address.setCity1(model.getCity1DrpDown());
      updateEntity(address, entityManager);
    }
  }

  private boolean seq5Exists(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GETMADDRSEQ_5");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      return true;
    }
    return false;
  }

  public String generateAddrSeqLD(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry, GEOHandler geoHandler) {
    int addrSeq = 0;
    String newSeq = null;
    int noOfMandtAddr = 0;
    List<String> mandtAddrType = geoHandler.getMandtAddrTypeForLDSeqGen(cmrIssuingCntry);
    List<String> addtionalAddrType = geoHandler.getAdditionalAddrTypeForLDSeqGen(cmrIssuingCntry);
    List<String> optionalAddrType = geoHandler.getOptionalAddrTypeForLDSeqGen(cmrIssuingCntry);
    List<String> reserverdSeqNo = geoHandler.getReservedSeqForLDSeqGen(cmrIssuingCntry);

    if (mandtAddrType != null) {
      noOfMandtAddr = mandtAddrType.size();
      // for creates always 1
      if (mandtAddrType.contains(addrType) && addtionalAddrType != null && !addtionalAddrType.contains(addrType)) {
        newSeq = generateAddrSeq(entityManager, addrType, reqId);
      } else if (mandtAddrType.contains(addrType) && addtionalAddrType == null) {
        newSeq = generateAddrSeq(entityManager, addrType, reqId);
      } else if (mandtAddrType.contains(addrType) && addtionalAddrType != null && addtionalAddrType.contains(addrType)) {
        String sql = ExternalizedQuery.getSql("COUNT.ADDR_ON_REQUEST");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", reqId);
        query.setParameter("ADDR_TYPE", addrType);
        int count = query.getSingleResult(Integer.class);

        if (count == 0) {
          addrSeq++;
          newSeq = Integer.toString(addrSeq);
        } else {
          addrSeq = getMaxSequenceOnAddr(entityManager, addrType, reqId);
          addrSeq = addrSeq + 1;
          if (addrSeq <= noOfMandtAddr) {
            addrSeq = noOfMandtAddr + 1;
          }
        }
        newSeq = Integer.toString(addrSeq);
      } else if (optionalAddrType != null && optionalAddrType.contains(addrType)) {
        addrSeq = getMaxSequenceOnAddr(entityManager, addrType, reqId);
        addrSeq = addrSeq + 1;
        if (addrSeq <= noOfMandtAddr) {
          addrSeq = noOfMandtAddr + 1;
        }
        newSeq = Integer.toString(addrSeq);
      }
      if (reserverdSeqNo != null && newSeq != null && reserverdSeqNo.contains(newSeq)) {
        addrSeq = Integer.parseInt(newSeq);
        addrSeq++;
        newSeq = Integer.toString(addrSeq);
      }

      if (newSeq != null) {
        newSeq = StringUtils.leftPad(newSeq, 5, '0');
      }

    }
    return newSeq;
  }

  private int getMaxSequenceOnAddr(EntityManager entityManager, String addrType, long reqId) {
    String maxAddrSeq = null;
    int addrSeq = 0;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.ES.MAX");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      addrSeq = Integer.parseInt(maxAddrSeq);
    }

    return addrSeq;
  }

  protected String generateMAddrSeqCopy(EntityManager entityManager, long reqId, String reqType, String addrType) {
    if ("ZD02".equals(addrType)) {
      return "598";
    } else if ("ZP02".equals(addrType)) {
      return "599";
    }
    int addrSeq = 0;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETMADDRSEQ_AT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "0");
      if (StringUtils.isAlpha(maxAddrSeq)) {
        maxAddrSeq = String.valueOf((int) ((Math.random() * 9 + 1) * 10));
      }
      if (!(Integer.valueOf(maxAddrSeq) >= 0 && Integer.valueOf(maxAddrSeq) <= 20849)) {
        maxAddrSeq = "1";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "1";
      }
      log.debug("Address Services maxAddrSeq = " + maxAddrSeq);
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
      // Compare with RDC SEQ FOR UPDATE REQUEST
      if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
        String cmrNo = null;
        if (result != null && result.length > 0 && result[2] != null) {
          cmrNo = (String) result[2];
        }
        if (!StringUtils.isEmpty(cmrNo)) {
          String sqlRDC = ExternalizedQuery.getSql("ADDRESS.GETMADDRSEQ_RDC_AT");
          PreparedQuery queryRDC = new PreparedQuery(entityManager, sqlRDC);
          queryRDC.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          queryRDC.setParameter("ZZKV_CUSNO", cmrNo);
          List<Object[]> resultsRDC = queryRDC.getResults();
          List<String> seqList = new ArrayList<String>();
          for (int i = 0; i < resultsRDC.size(); i++) {
            String item = String.valueOf(resultsRDC.get(i));
            if (!StringUtils.isEmpty(item)) {
              seqList.add(item);
            }
          }
          while (seqList.contains(Integer.toString(addrSeq))) {
            addrSeq++;
          }
        }
      }
    }

    newAddrSeq = Integer.toString(addrSeq);

    // newAddrSeq = newAddrSeq.substring(newAddrSeq.length() - 5,
    // newAddrSeq.length());

    return newAddrSeq;
  }

  protected String generateEMEAddrSeqCopy(EntityManager entityManager, long reqId) {
    int addrSeq = 0;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETMADDRSEQ_TR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "00000");

      // if (!(Integer.valueOf(maxAddrSeq) >= 00000 &&
      // Integer.valueOf(maxAddrSeq) <= 20849)) {
      // maxAddrSeq = "";
      // }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "00000";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }

    // newAddrSeq = "0000" + Integer.toString(addrSeq);

    // newAddrSeq = newAddrSeq.substring(newAddrSeq.length() - 5,
    // newAddrSeq.length());

    String straddrSeq = Integer.toString(addrSeq);

    newAddrSeq = StringUtils.leftPad(straddrSeq, 5, '0');

    return newAddrSeq;
  }

  public String getTrZD01Count(EntityManager entityManager, long reqId) {
    String zd01count = "";
    String sql = ExternalizedQuery.getSql("TR.GETZD01COUNT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      zd01count = sResult[0].toString();
    }
    System.out.println("zd01count = " + zd01count);

    return zd01count;
  }

  public String getTrZI01Count(EntityManager entityManager, long reqId) {
    String zi01count = "";
    String sql = ExternalizedQuery.getSql("TR.GETZI01COUNT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      zi01count = sResult[0].toString();
    }
    System.out.println("zi01count = " + zi01count);

    return zi01count;
  }

  public String getMaxSequenceAddr(EntityManager entityManager, long reqId) {

    int maxintSeqLegacy = 0;

    try {

      Data data = dataService.getCurrentRecordById(reqId, entityManager);

      int maxintSeqFromLegacy = getMaxSequenceOnLegacyAddr(entityManager, data.getCmrIssuingCntry(), data.getCmrNo());
      int maxintSeqFromRdc = getMaxSequenceFromRdc(entityManager, SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(), data.getCmrNo());

      if (maxintSeqFromRdc > maxintSeqFromLegacy) {
        maxintSeqLegacy = maxintSeqFromRdc;
      } else {
        maxintSeqLegacy = maxintSeqFromLegacy;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return StringUtils.leftPad(Integer.toString(maxintSeqLegacy), 5, '0');

  }

  public int getMaxSequenceOnLegacyAddr(EntityManager entityManager, String rcyaa, String cmrNo) {
    String maxAddrSeq = null;
    int addrSeq = 0;
    String sql = ExternalizedQuery.getSql("CEE.GETADDRSEQ.MAX.FROM.LEGACY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmrNo);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      addrSeq = Integer.parseInt(maxAddrSeq);
      addrSeq = ++addrSeq;
      System.out.println("maxseqOnLegacy = " + addrSeq);
    }
    return addrSeq;

  }

  public int getMaxSequenceFromRdc(EntityManager entityManager, String mandt, String katr6, String cmrNo) {
    String maxAddrSeq = null;
    int addrSeq = 0;
    String sql = ExternalizedQuery.getSql("CEE.GETADDRSEQ.MAX1");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", katr6);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      addrSeq = Integer.parseInt(maxAddrSeq);
      addrSeq = ++addrSeq;
      System.out.println("maxseqRdc = " + addrSeq);
    }

    return addrSeq;
  }

  private boolean isPrivate(Data data) {
    String subGrp = data.getCustSubGrp();
    if (subGrp != null) {
      if (subGrp.toUpperCase().contains("PRIV") || subGrp.toUpperCase().contains("PRIPE") || subGrp.toUpperCase().contains("PRICU")) {
        return true;
      }
    }
    return "60".equals(data.getCustClass()) || "9500".equals(data.getIsicCd());
  }

}
