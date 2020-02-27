package com.ibm.cio.cmr.request.service.code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.SystParameters;
import com.ibm.cio.cmr.request.model.code.SysParametersModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Priy Ranjan
 * 
 * 
 */
@Component
public class SysParametersAdminService extends BaseService<SysParametersModel, SystParameters> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SysParametersAdminService.class);
  }

  @Override
  protected void performTransaction(SysParametersModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<SysParametersModel> doSearch(SysParametersModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.SYSTEM_PARAMETER");
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    String cd = model.getParameterCd();
    String name = model.getParameterName();

    if (StringUtils.isEmpty(cd)) {
      cd = "";
    }
    if (StringUtils.isEmpty(name)) {
      name = "";
    }

    q.setParameter("CD", "%" + cd.toUpperCase() + "%");
    q.setParameter("NAME", "%" + name.toUpperCase() + "%");
    q.setForReadOnly(true);
    List<SystParameters> systParameters = q.getResults(SystParameters.class);
    List<SysParametersModel> sysParametersModels = new ArrayList<>();
    SysParametersModel sysParametersModel = null;
    for (SystParameters systParameter : systParameters) {
      sysParametersModel = new SysParametersModel();
      copyValuesFromEntity(systParameter, sysParametersModel);

      sysParametersModel.setCreateDtStringFormat((formatter.format(systParameter.getCreateTs())));
      sysParametersModels.add(sysParametersModel);
    }
    return sysParametersModels;
  }

  @Override
  protected SystParameters getCurrentRecord(SysParametersModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected SystParameters createFromModel(SysParametersModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
