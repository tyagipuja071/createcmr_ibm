package com.ibm.cio.cmr.request.service.requestentry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.GeoTaxInfo;
import com.ibm.cio.cmr.request.entity.GeoTaxInfoPK;
import com.ibm.cio.cmr.request.model.requestentry.GeoTaxInfoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;

/**
 * @author Sonali Jain
 * 
 */
@Component
public class TaxInfoService extends BaseService<GeoTaxInfoModel, GeoTaxInfo> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(TaxInfoService.class);
  }

  @Override
  protected void performTransaction(GeoTaxInfoModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

    int newGeoTaxInfoID;
    String action = model.getAction();
    StringBuilder uniqTaxInfo = new StringBuilder();
    uniqTaxInfo.append(model.getGeoTaxInfoId());
    AppUser user = AppUser.getUser(request);

    if ("ADD_TAXINFO".equals(action)) {
      newGeoTaxInfoID = generateGeoTaxInfoID(entityManager, model.getReqId());
      model.setGeoTaxInfoId(newGeoTaxInfoID);

      if (taxinfoExists(entityManager, model.getGeoTaxInfoId(), model.getReqId())) {
        throw new CmrException(MessageUtil.ERROR_ALREADY_TAXINFO, uniqTaxInfo.toString());
      }

      GeoTaxInfo geoTaxInfo = createFromModel(model, entityManager, request);
      geoTaxInfo.setCreateById(user.getIntranetId());
      geoTaxInfo.setCreateTs(SystemUtil.getCurrentTimestamp());
      geoTaxInfo.setUpdtTs(SystemUtil.getCurrentTimestamp());
      geoTaxInfo.setUpdtById(user.getIntranetId());

      createEntity(geoTaxInfo, entityManager);

    } else if ("UPDATE_TAXINFO".equals(action)) {
      GeoTaxInfo geoTaxInfo = getCurrentRecord(model, entityManager, request);
      copyValuesToEntity(model, geoTaxInfo);
      geoTaxInfo.setUpdtTs(SystemUtil.getCurrentTimestamp());
      geoTaxInfo.setUpdtById(user.getIntranetId());
      updateEntity(geoTaxInfo, entityManager);

    } else if ("REMOVE_TAXINFO".equals(action)) {
      GeoTaxInfo taxinfoList = getCurrentRecord(model, entityManager, request);
      long reqId = taxinfoList.getId().getReqId();
      int geoTaxInfoId = taxinfoList.getId().getGeoTaxInfoId();
      deleteEntity(taxinfoList, entityManager);
      // String sql =
      // ExternalizedQuery.getSql("REQUESTENTRY.DELETE_TAXINFO_RDC_SINGLE");
      // PreparedQuery delete = new PreparedQuery(entityManager, sql);
      // delete.setParameter("REQ_ID", reqId);
      // delete.setParameter("GEO_TAX_INFO_ID", geoTaxInfoId);
      // delete.executeSql();
    }
  }

  private boolean taxinfoExists(EntityManager entityManager, int geoTaxInfoId, long reqId) {
    String sql = ExternalizedQuery.getSql("TAXINFO.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("GEO_TAX_INFO_ID", geoTaxInfoId);
    return query.exists();
  }

  @Override
  protected List<GeoTaxInfoModel> doSearch(GeoTaxInfoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<GeoTaxInfoModel> results = new ArrayList<GeoTaxInfoModel>();

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.TAXINFO.SEARCH_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setForReadOnly(true);

    List<GeoTaxInfo> rs = query.getResults(GeoTaxInfo.class);

    GeoTaxInfoModel geoTaxInfoModel = null;

    if (rs != null) {
      for (GeoTaxInfo geoTaxInfo : rs) {

        geoTaxInfoModel = new GeoTaxInfoModel();
        copyValuesFromEntity(geoTaxInfo, geoTaxInfoModel);

        results.add(geoTaxInfoModel);
      }
    }
    return results;
  }

  @Override
  protected GeoTaxInfo getCurrentRecord(GeoTaxInfoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    String sql = ExternalizedQuery.getSql("TAXINFO.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("GEO_TAX_INFO_ID", model.getGeoTaxInfoId());
    List<GeoTaxInfo> geoTaxInfoList = query.getResults(GeoTaxInfo.class);
    if (geoTaxInfoList != null && geoTaxInfoList.size() > 0) {
      return geoTaxInfoList.get(0);
    }

    return null;
  }

  public int generateGeoTaxInfoID(EntityManager entityManager, long reqId) {

    String maxGeoTaxInfoID = null;
    int geoTaxInfoID = 0;
    int newGeoTaxInfoID = 0;
    String sql = ExternalizedQuery.getSql("TAXINFO.GETGEOTAXINFOID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxGeoTaxInfoID = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxGeoTaxInfoID)) {
        maxGeoTaxInfoID = "0";
      }
      try {
        geoTaxInfoID = Integer.parseInt(maxGeoTaxInfoID);
      } catch (Exception e) {
        // if returned value is invalid
      }
      geoTaxInfoID++;
    }
    newGeoTaxInfoID = geoTaxInfoID;
    return newGeoTaxInfoID;
  }

  public int getTaxInfoCountByReqId(EntityManager entityManager, long reqId) {
    int count = 0;
    String sql = ExternalizedQuery.getSql("TAXINFO.COUNTTAXINFORECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Object[]> countList;
    Object[] countObj;
    try {
      countList = query.getResults();
      countObj = countList.get(0);
      count = (Integer) countObj[0];
    } catch (Exception ex) {
      count = 0;
    }
    return count;
  }

  @Override
  protected GeoTaxInfo createFromModel(GeoTaxInfoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    GeoTaxInfo list = new GeoTaxInfo();
    GeoTaxInfoPK pk = new GeoTaxInfoPK();
    list.setId(pk);
    copyValuesToEntity(model, list);
    return list;
  }

  public void deleteAllTaxInfoById(List<GeoTaxInfo> taxInfoList, EntityManager entityManager, long reqId) {

    if (taxInfoList != null && taxInfoList.size() > 0) {
      for (int i = 0; i < taxInfoList.size(); i++) {
        GeoTaxInfo taxInfo = taxInfoList.get(i);
        GeoTaxInfo merged = entityManager.merge(taxInfo);
        if (merged != null) {
          entityManager.remove(merged);
        }
      }
    }
  }

  public List<GeoTaxInfoModel> getCurrTaxInfo(GeoTaxInfoModel model, EntityManager entityManager, String issuingCntry) throws CmrException {
    List<GeoTaxInfoModel> results = new ArrayList<GeoTaxInfoModel>();

    String sql = ExternalizedQuery.getSql("REQUESTENTRY.TAXINFO.SEARCH_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setForReadOnly(true);

    List<GeoTaxInfo> rs = query.getResults(GeoTaxInfo.class);

    GeoTaxInfoModel geoTaxInfoModel = null;

    if (rs != null) {
      // LA Prod Defect Fix
      // if (LAHandler.isMXIssuingCountry(issuingCntry) ||
      // LAHandler.isSSAIssuingCountry(issuingCntry)) {
      String taxNum = "";

      if (LAHandler.isLACountry(issuingCntry)) {
        // sql =
        // ExternalizedQuery.getSql("REQUESTENTRY.TAXNUMBER.SEARCH_ADMIN_BY_REQID");
        // query = new PreparedQuery(entityManager, sql);
        // query.setParameter("REQ_ID", model.getReqId());
        // Object[] resultAdmin = query.getSingleResult(Object[].class);
        // if ("U".equals(resultAdmin[0])) {
        // sql =
        // ExternalizedQuery.getSql("REQUESTENTRY.TAXNUMBER.SEARCH_DATA_BY_REQID");
        // query = new PreparedQuery(entityManager, sql);
        // query.setParameter("REQ_ID", model.getReqId());
        // Object[] resultData = query.getSingleResult(Object[].class);
        // if (resultData != null && resultData[0] != null) {
        // taxNum = resultData[0].toString();
        // }
        // }

        for (GeoTaxInfo geoTaxInfo : rs) {
          geoTaxInfoModel = new GeoTaxInfoModel();
          copyValuesFromEntity(geoTaxInfo, geoTaxInfoModel);
          results.add(geoTaxInfoModel);
        }
      }
    }
    return results;
  }
}
