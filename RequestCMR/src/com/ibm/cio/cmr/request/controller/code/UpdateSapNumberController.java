package com.ibm.cio.cmr.request.controller.code;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.BaseEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.system.UserModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.code.UpdateSapNoService;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.service.requestentry.DataService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.JpaManager;

@Controller
public class UpdateSapNumberController extends BaseController {

  private static final Logger LOG = Logger.getLogger(UpdateSapNumberController.class);
  private final DataService dataService = new DataService();

  @Autowired
  AddressService service;

  UpdateSapNoService updateSapNoService;

  public UpdateSapNumberController() {
    try {
      this.updateSapNoService = getParserInstance();
    } catch (Exception e) {
      LOG.debug(e.getMessage());
    }
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   */
  @RequestMapping(value = "/code/updatesapno", method = RequestMethod.GET)
  public @ResponseBody ModelAndView showAttachmentList(HttpServletRequest request) {
    AppUser user = AppUser.getUser(request);
    if (!user.isAdmin()) {
      LOG.warn("User " + user.getIntranetId() + " (" + user.getBluePagesName() + ") tried accessing the update SAP Number.");
      ModelAndView mv = new ModelAndView("noaccess", "users", new UserModel());
      return mv;
    }

    ModelAndView mv = new ModelAndView("updatesapno");
    setPageKeys("ADMIN", "CODE_ADMIN", mv);
    return mv;
  }

  /**
   * Handles the refresh page
   * 
   * @param request
   * @param model
   * @return
   * @throws CmrException
   */
  @RequestMapping(value = "/code/updatesapno/addressList/get", method = RequestMethod.GET)
  public @ResponseBody ModelMap doUpdateSapNumber(HttpServletRequest request) throws CmrException {
    ModelMap map = new ModelMap();
    String reqId = request.getParameter("reqId");
    String cmrNo = "";
    String issuingCntry = "";
    Boolean ifError = false;
    StringBuilder errorText = new StringBuilder();
    if (StringUtils.isBlank(reqId) || !StringUtils.isNumeric(reqId)) {
      map.addAttribute("success", false);
      map.addAttribute("msg", "reqId id invalid.");
      map.addAttribute("data", null);
      return map;
    }
    LOG.debug("Getting addresses for Request " + reqId);
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", Long.parseLong(reqId));
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      Data data = dataService.getCurrentRecordById(Long.parseLong(reqId), entityManager);
      if (data != null) {
        cmrNo = data.getCmrNo();
        issuingCntry = data.getCmrIssuingCntry();
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (StringUtils.isBlank(cmrNo)) {
      map.addAttribute("success", false);
      map.addAttribute("msg", "CMR Number is blank on the request");
      map.addAttribute("data", null);
      return map;
    }

    List<Addr> addressList = getAddressRecords(entityManager, Long.parseLong(reqId));
    List<Kna1> kna1List = getKna1ForCMR(cmrNo, issuingCntry, entityManager);
    if (kna1List == null || kna1List.size() == 0) {
      map.addAttribute("success", false);
      map.addAttribute("msg", "No Addresses Found on KNA1 for CMR No " + cmrNo);
      map.addAttribute("data", null);
      return map;
    }
    String output = "";
    String addrTypeToBeSKipped = null;
    String ifUsePairedSeq = "N";
    String seqnum = null;
    if (addressList != null && !addressList.isEmpty() && kna1List != null && !kna1List.isEmpty()) {
      for (Addr addr : addressList) {
        if (StringUtils.isEmpty(addr.getSapNo())) {
          try {
            output = updateSapNoService.getKNA1AddressType(addr.getId().getAddrType(), issuingCntry);
            addrTypeToBeSKipped = updateSapNoService.getAddressTypeToBeSkipped(issuingCntry);
            ifUsePairedSeq = updateSapNoService.getIfUsePairedSeqNo(issuingCntry);
            LOG.debug("Output AddrType = " + output);
            LOG.debug("Addr to be skipped = " + addrTypeToBeSKipped);
          } catch (Exception e) {
            LOG.debug(e.getMessage());
          }
          String kna1addr = StringUtils.isEmpty(output) ? addr.getId().getAddrType() : output;
          seqnum = "Y".equals(ifUsePairedSeq) ? addr.getPairedAddrSeq() : addr.getId().getAddrSeq();
          String sapNo = getSapNumberForAddrType(kna1List, kna1addr, seqnum);
          if (!StringUtils.isEmpty(sapNo)) {
            addr.setSapNo(sapNo);
            LOG.debug("Updating SAP NO=" + sapNo + "For Address Type =" + addr.getId().getAddrType());
            updateEntity(addr, entityManager);
          } else if (StringUtils.isEmpty(addrTypeToBeSKipped)
              || (!StringUtils.isEmpty(addrTypeToBeSKipped) && !addrTypeToBeSKipped.equals(addr.getId().getAddrType()))) {
            LOG.debug("SAP Number not found in KNA1 for address type=" + output + " Sequence =" + seqnum);
            ifError = true;
            errorText.append("SAP Number not found in KNA1 for address type=" + output + " Sequence =" + seqnum + "\n");
          }
        }
      }
    }
    if (ifError) {
      map.addAttribute("success", false);
      map.addAttribute("msg", errorText);
      map.addAttribute("data", null);
    } else {
      map.addAttribute("success", true);
      map.addAttribute("msg", null);
      map.addAttribute("data", addressList);
    }
    return map;
  }

  private List<Kna1> getKna1ForCMR(String cmr, String cntry, EntityManager entityManager) {
    String knaSql = ExternalizedQuery.getSql("BATCH.GET.KNA1_MANDT_CMRNO");
    PreparedQuery knaQuery = new PreparedQuery(entityManager, knaSql);
    knaQuery.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    knaQuery.setParameter("CMR_NO", cmr);
    knaQuery.setParameter("KATR6", cntry);
    List<Kna1> kna1List = knaQuery.getResults(Kna1.class);
    return kna1List;
  }

  private String getSapNumberForAddrType(List<Kna1> kna1List, String addressType, String seqno) {
    if (kna1List != null && !kna1List.isEmpty()) {
      for (Kna1 addrKna1 : kna1List) {
        if (!StringUtils.isEmpty(addressType) && addressType.equals(addrKna1.getKtokd()) && (seqno.equals(addrKna1.getZzkvSeqno())
            || (seqno.matches("^[0-9]*$") && addrKna1.getZzkvSeqno().matches("^[0-9]*$") && seqno.equals(Integer.parseInt(addrKna1.getZzkvSeqno())))
            || (seqno.matches("^[0-9]*$") && addrKna1.getZzkvSeqno().matches("^[0-9]*$")
                && seqno.equals(StringUtils.leftPad(addrKna1.getZzkvSeqno(), 5, '0'))))) {
          return addrKna1.getId().getKunnr();
        }
      }
    }
    return "";
  }

  private synchronized void updateEntity(BaseEntity<?> entity, EntityManager entityManager) {
    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();
    entityManager.merge(entity);
    entityManager.flush();
    transaction.commit();
  }

  private List<Addr> getAddressRecords(EntityManager entityManager, Long requestId) {
    LOG.debug("Searching for ADDR records for Request " + requestId);
    String sql = ExternalizedQuery.getSql("GET.ADDR.SEARCH_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", requestId);
    query.setForReadOnly(true);
    return query.getResults(Addr.class);
  }

  private UpdateSapNoService getParserInstance() throws ParserConfigurationException, SAXException, IOException {
    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
    UpdateSapNoService handler = new UpdateSapNoService();
    InputStream is = ConfigUtil.getResourceStream("updatesap-addrmap.xml");
    InputSource source = new InputSource(is);
    parser.parse(source, handler);
    return handler;
  }

}
