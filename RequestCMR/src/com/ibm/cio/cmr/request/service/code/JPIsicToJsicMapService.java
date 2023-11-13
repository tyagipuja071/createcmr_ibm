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
import com.ibm.cio.cmr.request.entity.JpIsicToJsicMap;
import com.ibm.cio.cmr.request.entity.JpJsicCodeMap;
import com.ibm.cio.cmr.request.model.code.JpIsicToJsicMapModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author XiangBinLiu
 *
 */

@Component
public class JPIsicToJsicMapService extends BaseService<JpIsicToJsicMapModel, JpJsicCodeMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(JPIsicToJsicMapService.class);
  }

  @Override
  protected void performTransaction(JpIsicToJsicMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<JpIsicToJsicMapModel> doSearch(JpIsicToJsicMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("JP.GET.ISIC_TO_JSIC_MAP_LIST");

    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);

    List<JpIsicToJsicMap> jpIsicToJsicMapList = q.getResults(JpIsicToJsicMap.class);
    List<JpIsicToJsicMapModel> list = new ArrayList<JpIsicToJsicMapModel>();

    for (JpIsicToJsicMap element : jpIsicToJsicMapList) {

      JpIsicToJsicMapModel jpIsicToJsicMapModel = new JpIsicToJsicMapModel();
      jpIsicToJsicMapModel.setMandt(element.getId().getMandt() != null ? element.getId().getMandt() : "");
      jpIsicToJsicMapModel.setJsicCd(element.getId().getJsicCd() != null ? element.getId().getJsicCd() : "");
      jpIsicToJsicMapModel.setIsicCd(element.getIsicCd() != null ? element.getIsicCd() : "");
      jpIsicToJsicMapModel.setCreateBy(element.getCreateBy());
      jpIsicToJsicMapModel.setCreateTs(element.getCreateTs());
      jpIsicToJsicMapModel.setUpdateBy(element.getUpdateBy());
      jpIsicToJsicMapModel.setUpdateTs(element.getUpdateTs());
      if (element.getCreateTs() != null) {
        jpIsicToJsicMapModel.setCreateTsStr(formatter.format(element.getCreateTs()));
      }
      if (element.getUpdateTs() != null) {
        jpIsicToJsicMapModel.setUpdateTsStr(formatter.format(element.getUpdateTs()));
      }

      list.add(jpIsicToJsicMapModel);
    }

    return list;
  }

  @Override
  protected JpJsicCodeMap getCurrentRecord(JpIsicToJsicMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected JpJsicCodeMap createFromModel(JpIsicToJsicMapModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
