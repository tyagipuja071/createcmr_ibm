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
import com.ibm.cio.cmr.request.entity.JPOfficeSectorInacMap;
import com.ibm.cio.cmr.request.model.code.JPOfficeSectorInacMapModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author XiangBinLiu
 *
 */

@Component
public class JPOfficeSectorInacMappingService extends BaseService<JPOfficeSectorInacMapModel, JPOfficeSectorInacMap> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(JPOfficeSectorInacMappingService.class);
  }

  @Override
  protected void performTransaction(JPOfficeSectorInacMapModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  protected List<JPOfficeSectorInacMapModel> doSearch(JPOfficeSectorInacMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("JP.GET.JP_OFFICE_SECTOR_INAC_MAPPING_LIST");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);

    List<JPOfficeSectorInacMap> jPOfficeSectorInacMapList = q.getResults(JPOfficeSectorInacMap.class);
    List<JPOfficeSectorInacMapModel> list = new ArrayList<JPOfficeSectorInacMapModel>();

    for (JPOfficeSectorInacMap element : jPOfficeSectorInacMapList) {

      JPOfficeSectorInacMapModel jPOfficeSectorInacMapModel = new JPOfficeSectorInacMapModel();
      jPOfficeSectorInacMapModel.setOfficeCd(element.getId().getOfficeCd() != null ? element.getId().getOfficeCd() : "");
      jPOfficeSectorInacMapModel.setSectorCd(element.getId().getSectorCd() != null ? element.getId().getSectorCd() : "");
      jPOfficeSectorInacMapModel.setInacCd(element.getId().getInacCd() != null ? element.getId().getInacCd() : "");
      jPOfficeSectorInacMapModel.setApCustClusterId(element.getId().getApCustClusterId() != null ? element.getId().getApCustClusterId() : "");
      jPOfficeSectorInacMapModel.setCreateBy(element.getCreateBy());
      jPOfficeSectorInacMapModel.setCreateTs(element.getCreateTs());
      jPOfficeSectorInacMapModel.setUpdateBy(element.getUpdateBy());
      jPOfficeSectorInacMapModel.setUpdateTs(element.getUpdateTs());
      if (element.getCreateTs() != null) {
        jPOfficeSectorInacMapModel.setCreateTsStr(formatter.format(element.getCreateTs()));
      }
      if (element.getUpdateTs() != null) {
        jPOfficeSectorInacMapModel.setUpdateTsStr(formatter.format(element.getUpdateTs()));
      }

      list.add(jPOfficeSectorInacMapModel);
    }

    return list;
  }

  @Override
  protected JPOfficeSectorInacMap getCurrentRecord(JPOfficeSectorInacMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected JPOfficeSectorInacMap createFromModel(JPOfficeSectorInacMapModel model, EntityManager entityManager, HttpServletRequest request)
      throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

}
