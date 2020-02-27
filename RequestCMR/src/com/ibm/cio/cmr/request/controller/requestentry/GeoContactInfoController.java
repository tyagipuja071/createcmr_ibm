package com.ibm.cio.cmr.request.controller.requestentry;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.BaseController;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.requestentry.GeoContactInfoModel;
import com.ibm.cio.cmr.request.service.requestentry.GeoContactInfoService;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;

/**
 * Addtional Contact Info Controller (LA)
 * 
 * @author Neil Sherwin Espolong
 * */
@Controller
public class GeoContactInfoController extends BaseController {

  @Autowired
  private GeoContactInfoService contactInfoService;

  private static final Logger LOGGER = Logger.getLogger(GeoContactInfoController.class.getSimpleName());

  @RequestMapping(value = "/request/contactinfo/list")
  public ModelMap findAllContactInfoByReqId(HttpServletRequest request, HttpServletResponse response, GeoContactInfoModel model) throws CmrException {

    List<GeoContactInfoModel> contacInfoList = contactInfoService.search(model, request);
    String country = model.getCmrIssuingCntry();
    if (SystemLocation.BRAZIL.equalsIgnoreCase(country)) {
      if (model.getReqType().equalsIgnoreCase("C")) {
        for (Iterator<GeoContactInfoModel> iterator = contacInfoList.iterator(); iterator.hasNext();) {
          GeoContactInfoModel contMod = iterator.next();
          if (contMod.getContactType().equalsIgnoreCase("CF") || contMod.getContactType().equalsIgnoreCase("LE")) {
            iterator.remove();
          }
        }
        return wrapAsSearchResult(contacInfoList);
      }
    } else if (SystemLocation.PERU.equalsIgnoreCase(country)) {
      if (model.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE) || model.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_UPDATE))
        for (Iterator<GeoContactInfoModel> iterator = contacInfoList.iterator(); iterator.hasNext();) {
          GeoContactInfoModel contMod = iterator.next();
          if (contMod.getContactType().equalsIgnoreCase("LE")) {
            iterator.remove();
          }
        }
      return wrapAsSearchResult(contacInfoList);
    }
    return wrapAsSearchResult(contacInfoList);
  }

  @RequestMapping(value = "/request/contactinfo/process", method = { RequestMethod.POST, RequestMethod.GET })
  public ModelMap findContactInfoThenProcess(HttpServletRequest request, HttpServletResponse response, GeoContactInfoModel model) throws CmrException {
    ProcessResultModel result = new ProcessResultModel();
    String currentAction = model.getAction();
    String country = model.getCmrIssuingCntry();
    String reqType = model.getReqType();
    String seqNum = model.getContactSeqNum();
    try {
      if ("ADD_CONTACTINFO".equalsIgnoreCase(currentAction)) {
        if (SystemLocation.BRAZIL.equalsIgnoreCase(country)) { // BR
          if (reqType.equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE) && seqNum.equalsIgnoreCase("001")) { // C
            List<GeoContactInfoModel> modelsToCreate = LAHandler.doContactInfoCreateStyle(model);
            if (modelsToCreate != null && modelsToCreate.size() != 0) {
              for (GeoContactInfoModel tempMod : modelsToCreate) {
                contactInfoService.processTransaction(tempMod, request);
              }
            }
            result.setMessage("Contact informations were created successfully.");
          } else {
            contactInfoService.processTransaction(model, request);
            result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACT_ADD_SUCCESS));
          } // C
        } else if (SystemLocation.PERU.equalsIgnoreCase(country)) {
          if ((reqType.equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE) || reqType.equalsIgnoreCase(CmrConstants.REQ_TYPE_UPDATE))
              && model.getContactSeqNum().equalsIgnoreCase("001")) {
            List<GeoContactInfoModel> modelsToCreate = LAHandler.doContactInfoCreateStyle(model);
            for (GeoContactInfoModel tempMod : modelsToCreate) {
              contactInfoService.processTransaction(tempMod, request);
              result.setMessage("Contact informations were created successfully.");
            }
          }
        } else {
          // OTHER SSAMX
          contactInfoService.processTransaction(model, request);
          result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACT_ADD_SUCCESS));
        }
      } else if ("UPDATE_CONTACTINFO".equalsIgnoreCase(currentAction)) {
        contactInfoService.processTransaction(model, request);
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACT_UPDATE_SUCCESS));
      } else if ("REMOVE_CONTACTINFO".equalsIgnoreCase(currentAction)) {
        contactInfoService.processTransaction(model, request);
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACT_REMOVE_SUCCESS));
      } else if ("REMOVE_CONTACTINFOS".equalsIgnoreCase(currentAction)) {
        contactInfoService.processTransaction(model, request);
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACTS_REMOVE_SUCCESS));
      }
      result.setSuccess(true);
    } catch (Exception ex) {
      if ("ADD_CONTACTINFO".equalsIgnoreCase(currentAction)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACT_ADD_FAILED));
      } else if ("UPDATE_CONTACTINFO".equalsIgnoreCase(currentAction)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACT_UPDATE_FAILED));
      } else if ("REMOVE_CONTACTINFO".equalsIgnoreCase(currentAction)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACT_REMOVE_FAILED));
      } else if ("REMOVE_CONTACTINFOS".equalsIgnoreCase(currentAction)) {
        result.setMessage(MessageUtil.getMessage(MessageUtil.INFO_CONTACTS_REMOVE_FAILED));
      }
      LOGGER.error("/request/contactinfo/process : " + ex.getMessage(), ex);
      result.setSuccess(false);
    }
    return wrapAsProcessResult(result);
  }
}
