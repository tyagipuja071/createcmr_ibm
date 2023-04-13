/**
 * 
 */
package com.ibm.cio.cmr.request.controller.util;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Stxl;
import com.ibm.cio.cmr.request.entity.TaxData;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author P100JGPH1
 * 
 */
@Controller
public class BrazilRetrieveDataController {

  private static final Logger LOG = Logger.getLogger(BrazilRetrieveDataController.class);

  @RequestMapping(
      value = "/getBRData")
  public ModelMap getBRData(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    String issuingCntry = request.getParameter("issuingCntry");
    String cmrNo = request.getParameter("cmrNo");
    FindCMRResultModel searchFindCMR = SystemUtil.findCMRs(cmrNo, issuingCntry, 2000);

    LOG.debug("BR cmr search: " + searchFindCMR.isSuccess());
    if (searchFindCMR.isSuccess()) {
      String muniFiscalCodeLE = "";
      String muniFiscalCodeUS = "";
      String kunnr = "";
      String email1 = "";
      String email2 = "";
      String email3 = "";
      String taxSepInd = "";
      String proxiLocNo = "";
      String company = "";
      String inacCd = "";
      String isuCd = "";
      String collectorNameNo = "";
      String salesBusOffCd = "";

      for (FindCMRRecordModel item : searchFindCMR.getItems()) {
        if ("ZS01".equals(item.getCmrAddrTypeCode())) {
          muniFiscalCodeLE = item.getCmrFiscalCd();
          kunnr = item.getCmrSapNumber();
          proxiLocNo = item.getCmrProxiLocn();

          company = item.getCmrCompanyNo();
          inacCd = item.getCmrInac();
          isuCd = item.getIsuCode();
          collectorNameNo = item.getCmrCollectorNo();
          salesBusOffCd = item.getCmrSortl();

        } else if ("ZI01".equals(item.getCmrAddrTypeCode())) {
          muniFiscalCodeUS = item.getCmrFiscalCd();
        }
      }

      EntityManager entityManager = JpaManager.getEntityManager();
      List<Stxl> stxlList = getStxlAddlContactsByKunnr(entityManager, kunnr);

      for (Stxl stxl : stxlList) {
        if ("ZOA1".equals(stxl.getId().getTdid())) {
          email1 = stxl.getClustd();
        } else if ("ZOA2".equals(stxl.getId().getTdid())) {
          email2 = stxl.getClustd();
        } else if ("ZOA3".equals(stxl.getId().getTdid())) {
          email3 = stxl.getClustd();
        }
      }

      List<TaxData> taxDataList = getTaxDataByKunnr(entityManager, kunnr);
      for (TaxData taxData : taxDataList) {
        if ("30".equals(taxData.getId().getTaxCd())) {
          taxSepInd = taxData.getTaxSeparationIndc();
        }
      }

      map.addAttribute("email1", email1);
      map.addAttribute("email2", email2);
      map.addAttribute("email3", email3);
      map.addAttribute("taxSepInd", taxSepInd);
      map.addAttribute("muniFiscalCodeLE", muniFiscalCodeLE);
      map.addAttribute("muniFiscalCodeUS", muniFiscalCodeUS);
      map.addAttribute("proxiLocnNo", proxiLocNo);
      map.addAttribute("company", company);
      map.addAttribute("inacCd", inacCd);
      map.addAttribute("isuCd", isuCd);
      map.addAttribute("collectorNameNo", collectorNameNo);
      map.addAttribute("salesBusOffCd", salesBusOffCd);

      map.addAttribute("success", true);
    } else {
      map.addAttribute("success", false);
    }

    return map;
  }

  private List<Stxl> getStxlAddlContactsByKunnr(EntityManager entityManager, String kunnr) {
    String tdname = kunnr + "%";
    String sql = ExternalizedQuery.getSql("LA.GET_STXL_ADDL_CONTACTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("TDNAME", tdname);

    return query.getResults(Stxl.class);
  }

  private List<TaxData> getTaxDataByKunnr(EntityManager entityManager, String kunnr) {
    String sql = ExternalizedQuery.getSql("LA.GET_LAINTERIM_TAXDATA_BY_KUNNR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KUNNR", kunnr);

    return query.getResults(TaxData.class);
  }

}
