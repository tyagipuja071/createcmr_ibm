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
import com.ibm.cio.cmr.request.entity.JpJsicCodeMap;
import com.ibm.cio.cmr.request.model.code.JpJsicCodeMapModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author XiangBinLiu
 *
 */

@Component
public class JPJsicCodeMapService extends BaseService<JpJsicCodeMapModel, JpJsicCodeMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(JPJsicCodeMapService.class);
  }

  @Override
  protected void performTransaction(JpJsicCodeMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<JpJsicCodeMapModel> doSearch(JpJsicCodeMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("JP.GET.JSIC_CODE_MAP_LIST");

    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);

    List<JpJsicCodeMap> jpJsicCodeMapList = q.getResults(JpJsicCodeMap.class);
    List<JpJsicCodeMapModel> list = new ArrayList<JpJsicCodeMapModel>();

    for (JpJsicCodeMap element : jpJsicCodeMapList) {

      JpJsicCodeMapModel jpJsicCodeMapModel = new JpJsicCodeMapModel();
      jpJsicCodeMapModel.setJsicCd(element.getId().getJsicCd() != null ? element.getId().getJsicCd() : "");
      jpJsicCodeMapModel.setSubIndustryCd(element.getId().getSubIndustryCd() != null ? element.getId().getSubIndustryCd() : "");
      jpJsicCodeMapModel.setIsuCd(element.getId().getIsuCd() != null ? element.getId().getIsuCd() : "");
      jpJsicCodeMapModel.setIsicCd(element.getId().getIsicCd() != null ? element.getId().getIsicCd() : "");
      jpJsicCodeMapModel.setDept(element.getId().getDept() != null ? element.getId().getDept() : "");
      jpJsicCodeMapModel.setSectorCd(element.getSectorCd() != null ? element.getSectorCd() : "");
      jpJsicCodeMapModel.setCreateBy(element.getCreateBy());
      jpJsicCodeMapModel.setCreateTs(element.getCreateTs());
      jpJsicCodeMapModel.setUpdateBy(element.getUpdateBy());
      jpJsicCodeMapModel.setUpdateTs(element.getUpdateTs());

      list.add(jpJsicCodeMapModel);
    }

    return list;
  }

  @Override
  protected JpJsicCodeMap getCurrentRecord(JpJsicCodeMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected JpJsicCodeMap createFromModel(JpJsicCodeMapModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
