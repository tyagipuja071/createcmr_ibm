/**
 * 
 */
package com.ibm.cio.cmr.request.controller.window;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.cris.CRISService;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cmr.services.client.cris.CRISAccount;
import com.ibm.cmr.services.client.cris.CRISCompany;
import com.ibm.cmr.services.client.cris.CRISData;
import com.ibm.cmr.services.client.cris.CRISEstablishment;
import com.ibm.cmr.services.client.cris.CRISFullAccountRequest;
import com.ibm.cmr.services.client.cris.CRISQueryRequest;
import com.ibm.cmr.services.client.cris.CRISQueryResponse;

/**
 * @author JeffZAMORA
 * 
 */
@Controller
public class CRISSearchController extends BaseWindowController {

  private static final String SESSION_KEY_CRITERIA = "_cris_crit";
  private static final String SESSION_KEY_RESULTS = "_cris_results";
  private static final Logger LOG = Logger.getLogger(CRISSearchController.class);

  @Autowired
  private CRISService service;

  @RequestMapping(
      value = WINDOW_URL + "/crissearch")
  public ModelAndView showSearchPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("crissearch");
    if ("Y".equals(request.getParameter("clear"))) {
      request.getSession().removeAttribute(SESSION_KEY_CRITERIA);
    }
    addCriteria(request, mv);
    return getWindow(mv, "CRIS - Search");
  }

  @RequestMapping(
      value = WINDOW_URL + "/criscomplist")
  public ModelAndView showCompanyList(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("criscomplist");
    CRISData data = (CRISData) request.getSession().getAttribute(SESSION_KEY_RESULTS);
    mv.addObject("list", data.getCompanies());
    addCriteria(request, mv);
    return getWindow(mv, "CRIS - Company List Results");
  }

  @RequestMapping(
      value = "/criscomplistget")
  public ModelMap getCompanyList(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    CRISData data = (CRISData) request.getSession().getAttribute(SESSION_KEY_RESULTS);
    map.addAttribute("items", data.getCompanies());
    return map;
  }

  @RequestMapping(
      value = WINDOW_URL + "/criscompdet/{id}")
  public ModelAndView showCompanyDetails(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("criscompdet");
    CRISQueryRequest query = new CRISQueryRequest();
    query.setCompanyNo(id);
    ParamContainer params = new ParamContainer();
    params.addParam("criteria", query);
    CRISQueryResponse queryResp = this.service.process(request, params);
    if (queryResp != null && queryResp.isSuccess() && "C".equals(queryResp.getData().getResultType())
        && queryResp.getData().getCompanies().size() > 0) {
      mv.addObject("record", queryResp.getData().getCompanies().get(0));
    } else {
      mv.addObject("record", new CRISCompany());
    }
    addCriteria(request, mv);
    return getWindow(mv, "CRIS - Company Details - " + id);
  }

  @RequestMapping(
      value = WINDOW_URL + "/crisestablist")
  public ModelAndView showEstablishmentList(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("crisestablist");
    CRISData data = (CRISData) request.getSession().getAttribute(SESSION_KEY_RESULTS);
    mv.addObject("list", data.getEstablishments());
    addCriteria(request, mv);
    String companyNo = request.getParameter("companyNo");
    if (!StringUtils.isEmpty(companyNo)) {
      return getWindow(mv, "CRIS - Establishment List (Company " + companyNo + ")");
    } else {
      return getWindow(mv, "CRIS - Establishment List Results");
    }
  }

  @RequestMapping(
      value = "/crisestablistget")
  public ModelMap getEstablishments(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    String companyNo = request.getParameter("companyNo");
    if (!StringUtils.isEmpty(companyNo)) {
      CRISQueryRequest query = new CRISQueryRequest();
      query.setEstabCompanyNo(companyNo);
      ParamContainer params = new ParamContainer();
      params.addParam("criteria", query);
      CRISQueryResponse queryResp = this.service.process(request, params);
      if (queryResp != null && queryResp.isSuccess() && "E".equals(queryResp.getData().getResultType())) {
        map.addAttribute("items", queryResp.getData().getEstablishments());
      } else {
        map.addAttribute("items", new ArrayList<CRISEstablishment>());
      }
    } else {
      CRISData data = (CRISData) request.getSession().getAttribute(SESSION_KEY_RESULTS);
      map.addAttribute("items", data.getEstablishments() != null ? data.getEstablishments() : new ArrayList<CRISEstablishment>());
    }
    return map;
  }

  @RequestMapping(
      value = WINDOW_URL + "/crisestabdet/{id}")
  public ModelAndView showEstablishmentDetails(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("crisestabdet");

    CRISQueryRequest query = new CRISQueryRequest();
    query.setEstablishmentNo(id);
    ParamContainer params = new ParamContainer();
    params.addParam("criteria", query);
    CRISQueryResponse queryResp = this.service.process(request, params);
    if (queryResp != null && queryResp.isSuccess() && "E".equals(queryResp.getData().getResultType())
        && queryResp.getData().getEstablishments().size() > 0) {
      CRISEstablishment establishment = queryResp.getData().getEstablishments().get(0);
      mv.addObject("record", establishment);
      query = new CRISQueryRequest();
      query.setCompanyNo(establishment.getCompanyNo());
      params = new ParamContainer();
      params.addParam("criteria", query);
      queryResp = this.service.process(request, params);
      if (queryResp != null && queryResp.isSuccess() && "C".equals(queryResp.getData().getResultType())
          && queryResp.getData().getCompanies().size() > 0) {
        CRISCompany company = queryResp.getData().getCompanies().get(0);
        mv.addObject("company", company);
      } else {
        mv.addObject("company", new CRISCompany());
      }
    } else {
      mv.addObject("record", new CRISEstablishment());
      mv.addObject("company", new CRISCompany());
    }

    addCriteria(request, mv);
    return getWindow(mv, "CRIS - Establishment Details - " + id);
  }

  @RequestMapping(
      value = WINDOW_URL + "/crisaccountlist")
  public ModelAndView showAccountList(ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("crisaccountlist");
    CRISData data = (CRISData) request.getSession().getAttribute(SESSION_KEY_RESULTS);
    mv.addObject("list", data.getAccounts());
    addCriteria(request, mv);
    String establishmentNo = request.getParameter("establishmentNo");
    if (!StringUtils.isEmpty(establishmentNo)) {
      return getWindow(mv, "CRIS - Account List (Establishment " + establishmentNo + ")");
    } else {
      return getWindow(mv, "CRIS - Account List");
    }
  }

  @RequestMapping(
      value = "/crisaccountlistget")
  public ModelMap getAccounts(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelMap map = new ModelMap();
    String establishmentNo = request.getParameter("establishmentNo");
    if (!StringUtils.isEmpty(establishmentNo)) {
      CRISQueryRequest query = new CRISQueryRequest();
      query.setAccountEstablishmentNo(establishmentNo);
      ParamContainer params = new ParamContainer();
      params.addParam("criteria", query);
      CRISQueryResponse queryResp = this.service.process(request, params);
      if (queryResp != null && queryResp.isSuccess() && "A".equals(queryResp.getData().getResultType())) {
        map.addAttribute("items", queryResp.getData().getAccounts());
      } else {
        map.addAttribute("items", new ArrayList<CRISAccount>());
      }
    } else {
      CRISData data = (CRISData) request.getSession().getAttribute(SESSION_KEY_RESULTS);
      map.addAttribute("items", data.getAccounts() != null ? data.getAccounts() : new ArrayList<CRISAccount>());
    }
    return map;
  }

  @RequestMapping(
      value = WINDOW_URL + "/crisaccountdet/{id}")
  public ModelAndView showAccountDetails(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("crisaccountdet");
    CRISAccount record = null;
    CRISQueryRequest query = new CRISQueryRequest();
    query.setAccountNo(id);
    ParamContainer params = new ParamContainer();
    params.addParam("criteria", query);
    CRISQueryResponse queryResp = this.service.process(request, params);
    if (queryResp != null && queryResp.isSuccess() && "A".equals(queryResp.getData().getResultType()) && queryResp.getData().getAccounts().size() > 0) {
      record = queryResp.getData().getAccounts().get(0);
    }
    if (record != null) {
      CRISFullAccountRequest acctReq = new CRISFullAccountRequest(record.getId().getAccountNo());
      params = new ParamContainer();
      params.addParam("criteria", acctReq);
      CRISQueryResponse acctResp = this.service.process(request, params);
      if (acctResp != null && acctResp.isSuccess()) {
        record = acctResp.getData().getAccount();
        query = new CRISQueryRequest();
        query.setEstablishmentNo(record.getEstablishmentNo());
        params = new ParamContainer();
        params.addParam("criteria", query);
        queryResp = this.service.process(request, params);
        if (queryResp != null && queryResp.isSuccess() && "E".equals(queryResp.getData().getResultType())
            && queryResp.getData().getEstablishments().size() > 0) {
          CRISEstablishment establishment = queryResp.getData().getEstablishments().get(0);
          mv.addObject("estab", establishment);
          query = new CRISQueryRequest();
          query.setCompanyNo(establishment.getCompanyNo());
          params = new ParamContainer();
          params.addParam("criteria", query);
          queryResp = this.service.process(request, params);
          if (queryResp != null && queryResp.isSuccess() && "C".equals(queryResp.getData().getResultType())
              && queryResp.getData().getCompanies().size() > 0) {
            CRISCompany company = queryResp.getData().getCompanies().get(0);
            mv.addObject("company", company);
          } else {
            mv.addObject("company", new CRISCompany());
          }
        } else {
          mv.addObject("estab", new CRISEstablishment());
          mv.addObject("company", new CRISCompany());
        }
        mv.addObject("record", record);
      } else {
        mv.addObject("record", new CRISAccount());
      }
    } else {
      mv.addObject("record", new CRISAccount());
    }
    addCriteria(request, mv);
    return getWindow(mv, "CRIS - Account Details - " + id);
  }

  @RequestMapping(
      value = WINDOW_URL + "/crisnoresults")
  public ModelAndView showNoResults(HttpServletRequest request, HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView("crisnoresults");
    addCriteria(request, mv);
    return getWindow(mv, "CRIS - No Results");
  }

  private void addCriteria(HttpServletRequest request, ModelAndView mv) {
    CRISQueryRequest criteria = (CRISQueryRequest) request.getSession().getAttribute(SESSION_KEY_CRITERIA);
    if (criteria == null) {
      criteria = new CRISQueryRequest();
    }
    mv.addObject("crit", criteria);
  }

  private void storeToSession(Object object, String key, HttpServletRequest request) {
    request.getSession().setAttribute(key, object);
  }

  @RequestMapping(
      value = WINDOW_URL + "/crisprocess")
  public ModelAndView searchAndRedirect(CRISQueryRequest criteria, HttpServletRequest request, HttpServletResponse response) throws Exception {

    storeToSession(criteria, SESSION_KEY_CRITERIA, request);
    ParamContainer params = new ParamContainer();
    params.addParam("criteria", criteria);

    try {
      CRISQueryResponse queryResponse = this.service.process(request, params);
      if (queryResponse == null || !queryResponse.isSuccess() || queryResponse.getData() == null || queryResponse.getData().getResultType() == null) {
        throw new Exception("CRIS response was null or is not successful.");
      }
      storeToSession(queryResponse.getData(), SESSION_KEY_RESULTS, request);
      ModelAndView mv = null;
      switch (queryResponse.getData().getResultType()) {
      case "C":
        // company results
        List<CRISCompany> companies = queryResponse.getData().getCompanies();
        if (companies == null || companies.size() == 0) {
          mv = new ModelAndView("redirect:/window/crisnoresults");
        } else {
          mv = new ModelAndView("redirect:/window/criscomplist");
        }

        break;
      case "E":
        // enterprise results
        List<CRISEstablishment> establishments = queryResponse.getData().getEstablishments();
        if (establishments == null || establishments.size() == 0) {
          mv = new ModelAndView("redirect:/window/crisnoresults");
        } else {
          mv = new ModelAndView("redirect:/window/crisestablist");
        }
        break;
      case "A":
        // account results
        List<CRISAccount> accounts = queryResponse.getData().getAccounts();
        if (accounts == null || accounts.size() == 0) {
          mv = new ModelAndView("redirect:/window/crisnoresults");
        } else {
          mv = new ModelAndView("redirect:/window/crisaccountlist");
        }
        break;
      case "F":
        CRISAccount account = queryResponse.getData().getAccount();
        // full account results
        if (account == null) {
          mv = new ModelAndView("redirect:/window/crisnoresults");
        }
        mv = new ModelAndView("redirect:/crisaccountdet/" + account.getAccountNo());
        break;
      }
      return mv;
    } catch (Exception e) {
      LOG.debug("Error in executing CRIS search.", e);
      ModelAndView mv = new ModelAndView("redirect:/window/crissearch");
      if (e instanceof CmrException) {
        CmrException cmre = (CmrException) e;
        MessageUtil.setErrorMessage(mv, cmre.getCode());
      } else {
        MessageUtil.setErrorMessage(mv, MessageUtil.CRIS_ERROR_QUERY);
      }
      return mv;
    }
  }
}
