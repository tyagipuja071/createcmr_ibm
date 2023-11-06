/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.IntlAddrPK;
import com.ibm.cio.cmr.request.model.requestentry.CopyAddressModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.JPHandler;
import com.ibm.cio.cmr.request.util.geo.impl.NORDXHandler;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class CopyAddressService extends BaseService<CopyAddressModel, Addr> {

  public static final List<String> LD_CEMA_COUNTRY = Arrays.asList("862");

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CopyAddressService.class);
  }

  @Override
  protected void performTransaction(CopyAddressModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    this.log
        .debug("Retrieving address [Request ID: " + model.getReqId() + " Type: " + model.getAddrType() + " Sequence: " + model.getAddrSeq() + "]");
    String sql = ExternalizedQuery.getSql("ADDRESS.COPY.GETSOURCE");

    GEOHandler handler = RequestUtils.getGEOHandler(model.getCmrIssuingCntry());

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("ADDR_TYPE", model.getAddrType());
    if (StringUtils.isBlank(model.getAddrSeq()) && !(model.getCmrIssuingCntry().equals("788"))) {
      if (model.getCmrIssuingCntry().equals(SystemLocation.SWITZERLAND)) {
        query.setParameter("ADDR_SEQ", "00001");
      } else {
        query.setParameter("ADDR_SEQ", "1");
      }
    } else if (StringUtils.isBlank(model.getAddrSeq()) && (model.getCmrIssuingCntry().equals("788"))) {
      String addrseq = "";
      switch (model.getAddrType()) {
      case "ZS01":
        addrseq = "99901";
        break;
      case "ZD01":
        addrseq = "20801";
        break;
      case "ZP01":
        addrseq = "29901";
        break;
      case "ZI01":
        addrseq = "20701";
        break;
      default:
        addrseq = "";
      }
      query.setParameter("ADDR_SEQ", addrseq);
    } else {
      query.setParameter("ADDR_SEQ", model.getAddrSeq());
    }
    Addr sourceAddr = query.getSingleResult(Addr.class);
    entityManager.detach(sourceAddr);

    sql = ExternalizedQuery.getSql("ADDRESS.COPY.GETADDRESSES");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("ADDR_TYPE", model.getAddrType());
    query.setParameter("ADDR_SEQ", model.getAddrSeq());
    List<String> typesToCopy = Arrays.asList(model.getCopyTypes());
    this.log.debug("Copying address types " + typesToCopy.toString());
    List<Addr> results = query.getResults(Addr.class);
    List<String> copiedTypes = new ArrayList<String>();

    List<String> createOnlyTypes = !StringUtils.isEmpty(model.getCreateOnly()) ? Arrays.asList(model.getCreateOnly().split("[|]"))
        : new ArrayList<String>();

    Map<String, Integer> seqMap = new HashMap<String, Integer>();
    if (StringUtils.isNumeric(model.getAddrSeq())) {
      seqMap.put(model.getAddrType(), StringUtils.isBlank(model.getAddrSeq()) ? 1 : Integer.parseInt(model.getAddrSeq()));
    }
    if (results != null) {

      String sapNo = null;
      String importInd = null;
      String createDt = null;
      int seq = 0;
      for (Addr copyAddr : results) {

        if (handler != null && handler.checkCopyToAdditionalAddress(entityManager, copyAddr, model.getCmrIssuingCntry())) {
          continue;
        }
        if (!seqMap.containsKey(copyAddr.getId().getAddrType())) {
          seqMap.put(copyAddr.getId().getAddrType(), 0);
        }
        if (StringUtils.isNumeric(copyAddr.getId().getAddrSeq())) {
          seq = seqMap.get(copyAddr.getId().getAddrType());
          if (seq < Integer.parseInt(copyAddr.getId().getAddrSeq())) {
            seqMap.put(copyAddr.getId().getAddrType(), Integer.parseInt(copyAddr.getId().getAddrSeq()));
          }
        }

        sapNo = copyAddr.getSapNo();
        importInd = copyAddr.getImportInd();
        createDt = copyAddr.getRdcCreateDt();
        if (typesToCopy.contains(copyAddr.getId().getAddrType()) && !createOnlyTypes.contains(copyAddr.getId().getAddrType())) {
          sourceAddr.setId(copyAddr.getId());
          PropertyUtils.copyProperties(copyAddr, sourceAddr);

          copyAddr.setSapNo(sapNo);
          copyAddr.setImportInd(importInd);
          copyAddr.setRdcCreateDt(createDt);

          if (handler != null) {
            handler.doBeforeAddrSave(entityManager, copyAddr, model.getCmrIssuingCntry());
          }

          if (!"N".equals(importInd)) {
            boolean changed = RequestUtils.isUpdated(entityManager, copyAddr, model.getCmrIssuingCntry());
            copyAddr.setChangedIndc(changed ? "Y" : null);
          } else {
            copyAddr.setChangedIndc(null);
          }
          updateEntity(copyAddr, entityManager);
          if (!copiedTypes.contains(copyAddr.getId().getAddrType())) {
            copiedTypes.add(copyAddr.getId().getAddrType());
          }
        }
      }
    }
    for (String toCopy : typesToCopy) {
      if (!copiedTypes.contains(toCopy) && (!toCopy.equals(model.getAddrType()) || createOnlyTypes.contains(model.getAddrType()))) {
        this.log.debug("Creating new address type " + toCopy);
        AddrPK newPk = new AddrPK();
        Addr newAddr = new Addr();
        String addrSeq = null;
        String newAddrSeq = null;
        newPk.setReqId(model.getReqId());
        newPk.setAddrType(toCopy);
        if (!seqMap.containsKey(toCopy) || seqMap.get(toCopy) == 0) {
          newPk.setAddrSeq("1");
        } else {
          newPk.setAddrSeq((seqMap.get(toCopy) + 1) + "");
        }

        String processingType = PageManager.getProcessingType(model.getCmrIssuingCntry(), "U");
        if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType) && handler != null) {
          AddressService addressService = new AddressService();
          newAddrSeq = addressService.generateAddrSeqLD(entityManager, newPk.getAddrType(), model.getReqId(), model.getCmrIssuingCntry(), handler);
        }

        if (StringUtils.isEmpty(newAddrSeq)) {
          newAddrSeq = handler.generateModifyAddrSeqOnCopy(entityManager, newPk.getAddrType(), model.getReqId(), newPk.getAddrSeq(),
              model.getCmrIssuingCntry());
        }

        if ("618".equals(model.getCmrIssuingCntry())) {
          // newAddrSeq = generateMAddrSeqCopy(entityManager, model.getReqId());
          AdminPK pk = new AdminPK();
          pk.setReqId(model.getReqId());
          Admin admin = entityManager.find(Admin.class, pk);
          newAddrSeq = generateMAddrSeqCopy(entityManager, model.getReqId(), admin.getReqType(), model.getAddrType());
        }

        if (LD_CEMA_COUNTRY.contains(model.getCmrIssuingCntry())) {
          int zd01cout = Integer.valueOf(getTrZD01Count(entityManager, model.getReqId()));
          int zi01cout = Integer.valueOf(getTrZI01Count(entityManager, model.getReqId()));
          AdminPK pk = new AdminPK();
          pk.setReqId(model.getReqId());
          Admin admin = entityManager.find(Admin.class, pk);
          String sourceType = sourceAddr.getId().getAddrType();
          if (sourceType.equals("ZS01")) {
            sourceAddr.setCustPhone(null);
          }
          if (toCopy.equals("ZS01")) {
            newAddrSeq = "00003";
          }
          if (toCopy.equals("ZP01")) {
            if ("C".equals(admin.getReqType())) {
              newAddrSeq = "00001";
            } else {
              newAddrSeq = "00002";
            }
          }
          if (toCopy.equals("ZD01")) {
            if (zd01cout == 0) {
              newAddrSeq = "00004";
            } else if (zd01cout == 1 && zi01cout == 0) {
              newAddrSeq = "00006";
            } else {
              newAddrSeq = generateEMEAddrSeqCopy(entityManager, model.getReqId());
            }
          }
          if (toCopy.equals("ZI01")) {
            if (zi01cout == 0) {
              newAddrSeq = "00005";
            } else {
              newAddrSeq = generateEMEAddrSeqCopy(entityManager, model.getReqId());
            }
          }
        }

        // if ("864".equals(model.getCmrIssuingCntry())) {
        // newAddrSeq = generateMAddrSeqCopy(entityManager, model.getReqId());
        // System.out.println("newAdd =" + newAddrSeq);
        // }
        if (NORDXHandler.isNordicsCountry(model.getCmrIssuingCntry())) {
          AdminPK pk = new AdminPK();
          pk.setReqId(model.getReqId());
          Admin admin = entityManager.find(Admin.class, pk);
          if ("U".equals(admin.getReqType())) {
            String maxAddrSeq = null;
            AddressService addressService1 = new AddressService();
            maxAddrSeq = addressService1.getMaxSequenceAddr(entityManager, model.getReqId());
            if (Integer.parseInt(maxAddrSeq) > Integer.parseInt(newAddrSeq)) {
              newAddrSeq = maxAddrSeq;
            }
          }
        }

        if (!StringUtils.isEmpty(newAddrSeq)) {
          newPk.setAddrSeq(newAddrSeq);
        }

        /*
         * if ("788".equals(model.getCmrIssuingCntry())) { if
         * (newPk.getAddrType().equals("ZS01")) { newPk.setAddrSeq("99901"); }
         * if (newPk.getAddrType().equals("ZI01")) { newPk.setAddrSeq("20701");
         * } if (newPk.getAddrType().equals("ZP01")) {
         * newPk.setAddrSeq("29901"); } if (newPk.getAddrType().equals("ZD01"))
         * { addrSeq = generateShippingAddrSeqNLCopy(entityManager,
         * newPk.getAddrType(), model.getReqId()); newPk.setAddrSeq(addrSeq); }
         * }
         */

        // if ("618".equals(model.getCmrIssuingCntry())) {
        // if (newPk.getAddrType().equals("ZS01")) {
        // newPk.setAddrSeq("90001");
        // }
        // if (newPk.getAddrType().equals("ZI01")) {
        // newPk.setAddrSeq("90002");
        // }
        // if (newPk.getAddrType().equals("ZP01")) {
        // newPk.setAddrSeq("90003");
        // }
        // if (newPk.getAddrType().equals("ZD01")) {
        // newPk.setAddrSeq("90004");
        // }
        // if (newPk.getAddrType().equals("ZS02")) {
        // newPk.setAddrSeq("90005");
        // }
        // }

        PropertyUtils.copyProperties(newAddr, sourceAddr);
        newAddr.setImportInd(CmrConstants.YES_NO.N.toString());
        newAddr.setSapNo(null);
        newAddr.setRdcCreateDt(null);
        newAddr.setId(newPk);

        if (JPHandler.isJPCountry(model.getCmrIssuingCntry())) {
          AddressService addressService = new AddressService();
          IntlAddr theJPIntelAddr = addressService.getIntlAddrById(sourceAddr, entityManager);
          if (theJPIntelAddr != null) {
            IntlAddr newJPIntelAddr = addressService.getIntlAddrById(newAddr, entityManager);
            if (newJPIntelAddr == null) {
              newJPIntelAddr = new IntlAddr();
              PropertyUtils.copyProperties(newJPIntelAddr, theJPIntelAddr);
              IntlAddrPK pk = new IntlAddrPK();
              pk.setReqId(sourceAddr.getId().getReqId());
              pk.setAddrSeq(newAddr.getId().getAddrSeq());
              pk.setAddrType(newAddr.getId().getAddrType());
              newJPIntelAddr.setId(pk);
              createEntity(newJPIntelAddr, entityManager);
            } else {
              PropertyUtils.copyProperties(newJPIntelAddr, theJPIntelAddr);
              updateEntity(newJPIntelAddr, entityManager);
            }
          }
        }

        if (handler != null) {
          handler.doBeforeAddrSave(entityManager, newAddr, model.getCmrIssuingCntry());
        }

        createEntity(newAddr, entityManager);
      }
    }
  }

  @Override
  protected List<CopyAddressModel> doSearch(CopyAddressModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected Addr getCurrentRecord(CopyAddressModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected Addr createFromModel(CopyAddressModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
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
      log.debug("Copy address maxAddrSeq = " + maxAddrSeq);
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

  // NL(788) Shipping addr seq logic should work while copying of address
  @Deprecated
  protected String generateShippingAddrSeqNLCopy(EntityManager entityManager, String addrType, long reqId) {
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

    newAddrSeq = "0000" + Integer.toString(addrSeq);

    newAddrSeq = newAddrSeq.substring(newAddrSeq.length() - 5, newAddrSeq.length());

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

}
