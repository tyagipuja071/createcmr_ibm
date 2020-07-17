package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * Holds field values for Spain fields computation logic -> 32S
 * 
 * @author PoojaTyagi
 *
 */
public class SpainFieldsCompContainer {
  private String sbo = null;
  private String salesRep = null;
  private String enterprise = null;

  public SpainFieldsCompContainer(EntityManager entityManager, Data data, String isuCd, String clientTier) {
    // get salesrep value
    String salesRepSql = ExternalizedQuery.getSql("QUERY.GET.SRLIST.BYISU");
    PreparedQuery query = new PreparedQuery(entityManager, salesRepSql);
    query.setParameter("ISSUING_CNTRY", data.getCmrIssuingCntry());
    query.setParameter("ISU", "%" + isuCd + clientTier + "%");
    query.setForReadOnly(true);
    List<Object[]> salesRepRes = query.getResults();
    if (salesRepRes != null) {
      if (salesRepRes.size() == 1) {
        // for single value, override
        setSalesRep(salesRepRes.get(0)[0].toString());
      }
    }

    // get sbo & enterprise value
    String sboEntpSql = ExternalizedQuery.getSql("QUERY.GET.SBO.BYSR_ISUCTC");
    query = new PreparedQuery(entityManager, sboEntpSql);
    query.setParameter("ISSUING_CNTRY", data.getCmrIssuingCntry());
    query.setParameter("REP_TEAM_CD", data.getLocationNumber());
    query.setParameter("CLIENT_TIER", "%" + isuCd + clientTier + "%");
    query.setForReadOnly(true);
    List<Object[]> sboEntpRes = query.getResults();
    if (sboEntpRes != null && !sboEntpRes.isEmpty()) {
      if (sboEntpRes.size() == 1) {
        setSbo(sboEntpRes.get(0)[0].toString());
        setEnterprise(sboEntpRes.get(0)[2].toString());
      }
    } else {
      sboEntpSql = ExternalizedQuery.getSql("QUERY.GET.SBO.BYSR_ES");
      query = new PreparedQuery(entityManager, sboEntpSql);
      query.setParameter("ISSUING_CNTRY", data.getCmrIssuingCntry());
      query.setParameter("REP_TEAM_CD", data.getLocationNumber());
      query.setForReadOnly(true);
      sboEntpRes = query.getResults();
      if (sboEntpRes != null && !sboEntpRes.isEmpty() && sboEntpRes.size() == 1) {
        setSbo(sboEntpRes.get(0)[0].toString());
        setEnterprise(sboEntpRes.get(0)[2].toString());
      }
    }
  }

  public boolean allFieldsCalculated() {
    return this.salesRep != null && this.sbo != null && this.enterprise != null && StringUtils.isNotBlank(this.salesRep)
        && StringUtils.isNotBlank(this.sbo) && StringUtils.isNotBlank(this.enterprise);
  }

  public String getSbo() {
    return sbo;
  }

  public void setSbo(String sbo) {
    this.sbo = sbo;
  }

  public String getSalesRep() {
    return salesRep;
  }

  public void setSalesRep(String salesRep) {
    this.salesRep = salesRep;
  }

  public String getEnterprise() {
    return enterprise;
  }

  public void setEnterprise(String enterprise) {
    this.enterprise = enterprise;
  }

}
