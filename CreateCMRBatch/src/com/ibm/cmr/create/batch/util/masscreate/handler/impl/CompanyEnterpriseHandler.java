/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * @author Jeffrey Zamora
 * 
 */
public class CompanyEnterpriseHandler implements RowHandler {

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    RowResult result = new RowResult();
    MassCreateData data = row.getData();
    String custNm1 = !StringUtils.isEmpty(data.getCustNm1()) ? data.getCustNm1() : "";
    String custNm2 = !StringUtils.isEmpty(data.getCustNm2()) ? data.getCustNm2() : "";
    String custNm = custNm1 + custNm2;

    custNm = custNm.toUpperCase().replace(" ", "");

    String companyNo = data.getCompany();
    String enterpriseNo = data.getEnterprise();
    List<Object[]> results = new ArrayList<Object[]>();
    String sql = "";

    if (!StringUtils.isEmpty(custNm)) {
      if (!StringUtils.isEmpty(companyNo) && !StringUtils.isEmpty(enterpriseNo)) {
        // a. Enterprise and Company both specified on request
        sql = ExternalizedQuery.getSql("QUERY.US_COMPANY.GET_ENT_NO");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("ENT_NO", enterpriseNo.trim());
        query.setParameter("COMP_NO", companyNo.trim());
        query.setParameter("COMP_LEGAL_NAME", custNm);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        results = query.getResults();
        if (results == null) {
          result.addError("The Company Number or Enterprise Number cannot be found.");
        }
      } else if (!StringUtils.isEmpty(enterpriseNo)) {
        // b. Enterprise specified on request
        sql = ExternalizedQuery.getSql("QUERY.US_ENTERPRISE.GET_ENT_NO");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("ENT_NO", enterpriseNo.trim());
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        results = query.getResults();
        if (results != null && results.size() > 0) {
          sql = ExternalizedQuery.getSql("QUERY.US_COMPANY.GET_COMP");
          query = new PreparedQuery(entityManager, sql);
          query.setParameter("ENT_NO", enterpriseNo.trim());
          query.setParameter("COMP_LEGAL_NAME", custNm);
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          results = query.getResults();
          if (results != null && results.size() > 0) {
            data.setCompany((String) results.get(0)[0]);
          }
        } else {
          result.addError("The Company Number or Enterprise Number cannot be found.");
        }
      } else if (!StringUtils.isEmpty(companyNo)) {
        // c. Company specified on request
        sql = ExternalizedQuery.getSql("QUERY.US_COMPANY.GET_ENT_COMP");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("COMP_NO", companyNo.trim());
        query.setParameter("COMP_LEGAL_NAME", custNm);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        results = query.getResults();
        if (results != null && results.size() > 0) {
          data.setEnterprise((String) results.get(0)[0]);
        } else {
          result.addError("The Company Number or Enterprise Number cannot be found.");
        }
      } else if (StringUtils.isEmpty(companyNo) && StringUtils.isEmpty(enterpriseNo)) {
        // d. None specified on request
        sql = ExternalizedQuery.getSql("QUERY.US_COMPANY.COMP_LEGAL_NAME");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("COMP_LEGAL_NAME", custNm);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        results = query.getResults();
        if (results != null && results.size() > 0) {
          data.setEnterprise((String) results.get(0)[0]);
          data.setCompany((String) results.get(0)[1]);
        }
      }
    }
    return result;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
