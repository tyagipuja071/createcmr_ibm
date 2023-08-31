package com.ibm.cio.cmr.request.service.code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.JpBoCodesMap;
import com.ibm.cio.cmr.request.model.code.JpBoCodesMapModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author XiangBinLiu
 *
 */

@Component
public class JPBoCodesMapService extends BaseService<JpBoCodesMapModel, JpBoCodesMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(JPBoCodesMapService.class);
  }

  @Override
  protected void performTransaction(JpBoCodesMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<JpBoCodesMapModel> doSearch(JpBoCodesMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("JP.GET.BO_CODES_MAP_LIST");

    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);

    List<JpBoCodesMap> jpBoCodesMapList = q.getResults(JpBoCodesMap.class);
    List<JpBoCodesMapModel> list = new ArrayList<JpBoCodesMapModel>();

    for (JpBoCodesMap element : jpBoCodesMapList) {

      JpBoCodesMapModel jpBoCodesMapModel = new JpBoCodesMapModel();
      jpBoCodesMapModel.setSubsidiaryCd(element.getId().getSubsidiaryCd() != null ? element.getId().getSubsidiaryCd() : "");
      jpBoCodesMapModel.setOfficeCd(element.getId().getOfficeCd() != null ? element.getId().getOfficeCd() : "");
      jpBoCodesMapModel.setSubOfficeCd(element.getId().getSubOfficeCd() != null ? element.getId().getSubOfficeCd() : "");
      jpBoCodesMapModel.setBoCd(element.getBoCd() != null ? element.getBoCd() : "");
      jpBoCodesMapModel.setFieldSalesCd(element.getFieldSalesCd() != null ? element.getFieldSalesCd() : "");
      jpBoCodesMapModel.setSalesOfficeCd(element.getSalesOfficeCd() != null ? element.getSalesOfficeCd() : "");
      jpBoCodesMapModel.setMktgDivCd(element.getMktgDivCd() != null ? element.getMktgDivCd() : "");
      jpBoCodesMapModel.setMrcCd(element.getMrcCd() != null ? element.getMrcCd() : "");
      jpBoCodesMapModel.setDeptCd(element.getDeptCd() != null ? element.getDeptCd() : "");
      jpBoCodesMapModel.setMktgDeptName(element.getMktgDeptName() != null ? element.getMktgDeptName() : "");
      jpBoCodesMapModel.setClusterId(element.getClusterId() != null ? element.getClusterId() : "");
      jpBoCodesMapModel.setClientTierCd(element.getClientTierCd() != null ? element.getClientTierCd() : "");
      jpBoCodesMapModel.setIsuCdOverride(element.getIsuCdOverride() != null ? element.getIsuCdOverride() : "");
      jpBoCodesMapModel.setIsicCd(element.getIsicCd() != null ? element.getIsicCd() : "");
      jpBoCodesMapModel.setCreateBy(element.getCreateBy());
      jpBoCodesMapModel.setCreateTs(element.getCreateTs());
      jpBoCodesMapModel.setUpdateBy(element.getUpdateBy());
      jpBoCodesMapModel.setUpdateTs(element.getUpdateTs());
      if (element.getCreateTs() != null) {
        jpBoCodesMapModel.setCreateTsStr(formatter.format(element.getCreateTs()));
      }
      if (element.getUpdateTs() != null) {
        jpBoCodesMapModel.setUpdateTsStr(formatter.format(element.getUpdateTs()));
      }

      list.add(jpBoCodesMapModel);
    }

    return list;
  }

  @Override
  protected JpBoCodesMap getCurrentRecord(JpBoCodesMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected JpBoCodesMap createFromModel(JpBoCodesMapModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
