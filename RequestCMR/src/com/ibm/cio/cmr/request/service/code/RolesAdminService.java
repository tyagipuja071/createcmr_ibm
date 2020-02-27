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
import com.ibm.cio.cmr.request.entity.Roles;
import com.ibm.cio.cmr.request.model.code.RolesModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

@Component
public class RolesAdminService extends BaseService<RolesModel, Roles> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(RolesAdminService.class);
  }

  @Override
  protected void performTransaction(RolesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<RolesModel> doSearch(RolesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.ROLES");
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<Roles> rolesList = q.getResults(Roles.class);
    List<RolesModel> rolesModels = new ArrayList<>();
    RolesModel rolesModel = null;
    for (Roles role : rolesList) {
      rolesModel = new RolesModel();
      copyValuesFromEntity(role, rolesModel);

      rolesModel.setCreateTsString((formatter.format(role.getCreateTs())));
      rolesModel.setUpdateTsString((formatter.format(role.getCreateTs())));
      if (role.getStatus() != null && role.getStatus().length() > 0) {
        rolesModel.setStatusDefinition((role.getStatus().equals("1")) ? "Active" : "Inactive");
      }
      if (role.getApplicationCd() != null && role.getApplicationCd().length() > 0) {
        switch (role.getApplicationCd()) {
        case "R":
          rolesModel.setApplicationCdDefinition("Request CMR");
          break;
        case "C":
          rolesModel.setApplicationCdDefinition("Create CMR");
          break;
        case "F":
          rolesModel.setApplicationCdDefinition("Find CMR");
          break;
        case "RCF":
          rolesModel.setApplicationCdDefinition("All");
          break;
        }
      }

      rolesModels.add(rolesModel);
    }
    return rolesModels;
  }

  @Override
  protected Roles getCurrentRecord(RolesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected Roles createFromModel(RolesModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
