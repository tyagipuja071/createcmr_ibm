package com.ibm.cio.cmr.request.service.window;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.window.SingleReactQueryRequest;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author MukeshKumar
 * 
 */
@Component
public class SingleReactService extends BaseSimpleService<FindCMRResultModel> {

  private static final Logger LOG = Logger.getLogger(SingleReactService.class);

  public static void main(String[] args) {
    System.out.println("Test.....");
    for (Field f : SingleReactQueryRequest.class.getDeclaredFields()) {
      System.out.println("<form:hidden path=\"" + f.getName() + "\" />");
    }
  }

  @Override
  protected FindCMRResultModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    FindCMRResultModel queryResponse = new FindCMRResultModel();
    LOG.debug("Executin Single Reactivation query..");

    SingleReactQueryRequest qRequest = (SingleReactQueryRequest) params.getParam("criteria");
    String sql = ExternalizedQuery.getSql("GET.KNA1_FOR_SINGLE_REACTIVATION");

    StringBuilder sb = new StringBuilder();
    sb.append(sql);

    if (!StringUtils.isEmpty(qRequest.getZzkvCusno())) {
      sb.append(" AND ZZKV_CUSNO='" + qRequest.getZzkvCusno() + "'");
    }

    if (!StringUtils.isEmpty(qRequest.getPstlz())) {
      sb.append(" AND PSTLZ LIKE '%" + qRequest.getPstlz() + "'");
    }
    if (!StringUtils.isEmpty(qRequest.getStras())) {
      sb.append(" AND STRAS LIKE '%" + qRequest.getStras() + "'");
    }
    if (!StringUtils.isEmpty(qRequest.getTelx1())) {
      sb.append(" AND TELX1 LIKE '%" + qRequest.getTelx1() + "'");
    }
    if (!StringUtils.isEmpty(qRequest.getName1())) {
      sb.append(" AND NAME1 LIKE '%" + qRequest.getName1() + "'");
    }
    if (!StringUtils.isEmpty(qRequest.getStcd1())) {
      sb.append(" AND STCD1 LIKE '%" + qRequest.getStcd1() + "'");
    }
    if (!StringUtils.isEmpty(qRequest.getKunnr())) {
      sb.append(" AND KUNNR ='" + qRequest.getKunnr() + "'");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sb.toString());
    query.setParameter("KATR6", qRequest.getKatr6());
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    List<Kna1> resultList = query.getResults(Kna1.class);

    if (resultList != null && resultList.size() > 0) {
      List<FindCMRRecordModel> cmrRecordModels = new ArrayList<>();
      for (Kna1 kna1 : resultList) {
        FindCMRRecordModel cmrRecord = new FindCMRRecordModel();

        // Confirmed
        cmrRecord.setCmrName1Plain(kna1.getName1());
        cmrRecord.setCmrName2Plain(kna1.getName2());
        cmrRecord.setCmrName3(kna1.getName3());
        cmrRecord.setCmrName4(kna1.getName4());
        cmrRecord.setCmrOrderBlock(kna1.getAufsd());
        cmrRecord.setCmrIsu(kna1.getBrsch());
        cmrRecord.setCmrAffiliate(kna1.getKonzs());
        cmrRecord.setCmrAddrTypeCode(kna1.getKtokd());
        cmrRecord.setCmrAddrType("");
        cmrRecord.setCmrPrefLang(kna1.getSpras());
        cmrRecord.setCmrBusinessReg(kna1.getStcd1());
        cmrRecord.setCmrShortName(kna1.getTelx1());
        cmrRecord.setCmrVat(kna1.getStceg());
        cmrRecord.setCmrSubIndustry(kna1.getBran1());
        cmrRecord.setCmrPpsceid(kna1.getBran3());
        cmrRecord.setCmrSitePartyID(kna1.getBran5());
        cmrRecord.setCmrIssuedBy(kna1.getKatr6());     
        cmrRecord.setCmrCapIndicator(kna1.getKatr8());
        cmrRecord.setCmrNum(kna1.getZzkvCusno());
        cmrRecord.setCmrInac(kna1.getZzkvInac());
        cmrRecord.setCmrCompanyNo(!StringUtils.isEmpty(kna1.getZzkvNode1()) ? kna1.getZzkvNode1().trim() : "");
        cmrRecord.setCmrEnterpriseNumber(!StringUtils.isEmpty(kna1.getZzkvNode2()) ? kna1.getZzkvNode2().trim() : "");
        cmrRecord.setCmrBusinessReg(kna1.getStcd1());
        cmrRecord.setCmrLocalTax2(kna1.getStcd2());
        cmrRecord.setCmrClass(kna1.getKukla());

        // (Addr table)Need to confirm with KNA1 table mapping and set
        cmrRecord.setCmrSapNumber(kna1.getId().getKunnr());
        cmrRecord.setCmrAddrSeq(kna1.getZzkvSeqno());

        cmrRecord.setCmrCity(!StringUtils.isEmpty(kna1.getOrt01()) ? kna1.getOrt01() : "");
        cmrRecord.setCmrCity2(!StringUtils.isEmpty(kna1.getOrt02()) ? kna1.getOrt02() : "");
        cmrRecord.setCmrState(!StringUtils.isEmpty(kna1.getRegio()) ? kna1.getRegio() : "");
        cmrRecord.setCmrPostalCode(!StringUtils.isEmpty(kna1.getPstlz()) ? kna1.getPstlz() : "");

        cmrRecord.setCmrCountyCode("");
        cmrRecord.setCmrCounty("");
        cmrRecord.setCmrStreetAddress(!StringUtils.isEmpty(kna1.getStras()) ? kna1.getStras() : "");
        cmrRecord.setCmrCustPhone("");
        cmrRecord.setCmrCustFax(!StringUtils.isEmpty(kna1.getTelfx()) ? kna1.getTelfx() : "");
        cmrRecord.setCmrBusNmLangCd("");
        cmrRecord.setCmrTransportZone(!StringUtils.isEmpty(kna1.getLzone()) ? kna1.getLzone() : "");
        cmrRecord.setCmrPOBox(!StringUtils.isEmpty(kna1.getPfach()) ? kna1.getPfach() : "");
        cmrRecord.setCmrPOBoxCity(!StringUtils.isEmpty(kna1.getPfort()) ? kna1.getPfort() : "");
        cmrRecord.setCmrPOBoxPostCode(!StringUtils.isEmpty(kna1.getPstl2()) ? kna1.getPstl2() : "");
        cmrRecord.setCmrBldg("");
        cmrRecord.setCmrFloor("");
        cmrRecord.setCmrOffice("");
        cmrRecord.setCmrDept("");

        cmrRecord.setCmrTier("");
        cmrRecord.setCmrInacType("");
        cmrRecord.setCmrIsic(!StringUtils.isEmpty(kna1.getZzkvSic()) ? kna1.getZzkvSic().trim().substring(0, 4) : "");
        cmrRecord.setCmrSortl("");
        cmrRecord.setCmrIssuedByDesc("");
        cmrRecord.setCmrRdcCreateDate("");
        cmrRecord.setCmrCountryLanded(!StringUtils.isEmpty(kna1.getLand1()) ? kna1.getLand1() : "");
        cmrRecordModels.add(cmrRecord);
      }

      queryResponse.setItems(cmrRecordModels);
      queryResponse.setSuccess(true);
      queryResponse.setMessage("Records found..");
    } else {
      queryResponse.setSuccess(false);
      queryResponse.setMessage("Records not found..");
    }

    return queryResponse;
  }
}
