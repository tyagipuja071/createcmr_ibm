package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.GeoCities;
import com.ibm.cio.cmr.request.entity.GeoCitiesPK;
import com.ibm.cio.cmr.request.model.code.CollectorNameNoModel;
import com.ibm.cio.cmr.request.model.code.GeoCitiesModel;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Controller
public class GeoCitiesService extends BaseService<GeoCitiesModel, GeoCities> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(GeoCitiesService.class);
  }

  @Override
  protected void performTransaction(GeoCitiesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<GeoCitiesModel> doSearch(GeoCitiesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    List<GeoCitiesModel> list = new ArrayList<>();
    String cntry = request.getParameter("cmrIssuingCntry");
    String prov = request.getParameter("stateProv");
    String city = request.getParameter("cityId");

    // if(only issuing country is present, show all cities)
    if (!StringUtils.isEmpty(cntry) && StringUtils.isEmpty(prov) && StringUtils.isEmpty(city)) {
      list = doSearchPerCntry(entityManager, cntry);
    } else if (!StringUtils.isEmpty(cntry) && !StringUtils.isEmpty(prov) && StringUtils.isEmpty(city)) {
      list = doSearchPerCntryStateProv(entityManager, cntry, prov);
    } else if (!StringUtils.isEmpty(cntry) && StringUtils.isEmpty(prov) && !StringUtils.isEmpty(city)) {
      list = doSearchPerCntryCity(entityManager, cntry, city);
    } else {
      // Do nothing
    }

    return list;
  }

  private List<GeoCitiesModel> doSearchPerCntryCity(EntityManager entityManager, String cntry, String city) {
    List<GeoCitiesModel> list = new ArrayList<>();
    String sql = "SELECT * FROM CREQCMR.GEO_CITIES WHERE ISSUING_CNTRY = :CNTRY AND CITY_ID = :CITY";
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY", cntry);
    q.setParameter("CITY", city);

    List<GeoCities> result = q.getResults(GeoCities.class);
    GeoCitiesModel model = null;

    if (result != null && result.size() > 0) {
      for (GeoCities gc : result) {
        model = new GeoCitiesModel();
        model.setCmrIssuingCntry(gc.getId().getIssuingCntry());
        model.setCityId(gc.getId().getCityId());
        model.setCityDesc(gc.getCityDesc());
        list.add(model);
      }
    }

    return list;
  }

  private List<GeoCities> doSearchPerCntryCity2(EntityManager entityManager, String cntry, String city) {
    String sql = "SELECT * FROM CREQCMR.GEO_CITIES WHERE ISSUING_CNTRY = :CNTRY AND CITY_ID = :CITY";
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY", cntry);
    q.setParameter("CITY", city);

    List<GeoCities> result = q.getResults(GeoCities.class);
    return result;
  }

  private List<GeoCitiesModel> doSearchPerCntry(EntityManager entityManager, String cntry) {
    List<GeoCitiesModel> list = new ArrayList<>();
    String sql = "SELECT * FROM CREQCMR.GEO_CITIES WHERE ISSUING_CNTRY = :CNTRY";
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY", cntry);

    List<GeoCities> result = q.getResults(GeoCities.class);
    GeoCitiesModel model = null;

    if (result != null && result.size() > 0) {
      for (GeoCities gc : result) {
        model = new GeoCitiesModel();
        model.setCmrIssuingCntry(gc.getId().getIssuingCntry());
        model.setCityId(gc.getId().getCityId());
        model.setCityDesc(gc.getCityDesc());
        list.add(model);
      }
    }

    return list;
  }

  private List<GeoCitiesModel> doSearchPerCntryStateProv(EntityManager entityManager, String cntry, String stateProv) {
    List<GeoCitiesModel> list = new ArrayList<>();
    String sql = "SELECT * FROM CREQCMR.GEO_CITIES WHERE ISSUING_CNTRY = :CNTRY AND CITY_ID LIKE('PARAM%')";
    sql = sql.replace("PARAM", stateProv);
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY", cntry);

    List<GeoCities> result = q.getResults(GeoCities.class);
    GeoCitiesModel model = null;

    if (result != null && result.size() > 0) {
      for (GeoCities gc : result) {
        model = new GeoCitiesModel();
        model.setCmrIssuingCntry(gc.getId().getIssuingCntry());
        model.setCityId(gc.getId().getCityId());
        model.setCityDesc(gc.getCityDesc());
        list.add(model);
      }
    }

    return list;
  }

  @Override
  protected GeoCities getCurrentRecord(GeoCitiesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String cntry = model.getCmrIssuingCntry();
    String city = model.getCityId();
    GeoCities oneCity = null;
    List<GeoCities> result = doSearchPerCntryCity2(entityManager, cntry, city);

    if (result != null && result.size() > 0) {
      oneCity = result.get(0);
    }

    return oneCity;

  }

  public CollectorNameNoModel save(CollectorNameNoModel model, HttpServletRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected GeoCities createFromModel(GeoCitiesModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    GeoCities geoCities = new GeoCities();
    GeoCitiesPK pk = new GeoCitiesPK();
    // pk.setCollectorNo(model.getCollectorNo());
    // pk.setIssuingCntry(model.getCmrIssuingCntry());
    pk.setCityId(model.getCityId());
    pk.setIssuingCntry(model.getCmrIssuingCntry());
    geoCities.setCityDesc(model.getCityDesc());
    geoCities.setId(pk);
    geoCities.setCreateById(model.getCreateBy());
    geoCities.setCreateTs(model.getCreateTs());
    return geoCities;
  }
}
