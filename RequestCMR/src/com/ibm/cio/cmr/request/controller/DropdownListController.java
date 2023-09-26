/**
 * 
 */
package com.ibm.cio.cmr.request.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.DropdownItemModel;
import com.ibm.cio.cmr.request.model.DropdownModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.DropDownService;

/**
 * @author Rangoli Saxena
 * 
 */
@Controller
public class DropdownListController extends BaseController {

  private static Map<String, DropdownModel> cachedFixedItems = new HashMap<String, DropdownModel>();
  private static List<String> noCacheFields = Arrays.asList("RequestReason");

  @Autowired
  private DropDownService service;

  @RequestMapping(
      value = "/dropdown/{queryId}/list")
  public ModelMap getDropdownValues(@PathVariable String queryId, HttpServletRequest request, HttpServletResponse response) throws Exception {

    ParamContainer params = new ParamContainer();
    int paramCount = 0;
    boolean nocache = false;
    for (String paramName : request.getParameterMap().keySet()) {
      params.addParam(paramName, request.getParameter(paramName));
      if (!"queryId".equals(paramName) && !"nocache".equals(paramName)) {
        paramCount++;
      }
      if ("nocache".equals(paramName)) {
        nocache = true;
      }
    }
    String fieldId = (String) params.getParam("fieldId");
    if (noCacheFields.contains(fieldId)) {
      nocache = true;
    }
    params.addParam("queryId", queryId);

    DropdownModel items = null;
    boolean addToCache = false;
    boolean bdsOrLov = false;
    String cmrIssuingCntry = (String) params.getParam("cmrIssuingCntry");
    if (paramCount == 0) {
      // fixed queries, cache them
      items = cachedFixedItems.get(queryId.toUpperCase());
      addToCache = items == null;
    }

    boolean lovForCntry = false;
    if ("BDS".equalsIgnoreCase(queryId)) {
      items = paramCount == 1 ? cachedFixedItems.get(fieldId) : null;
      addToCache = items == null && paramCount == 1; // fieldID not counted
      bdsOrLov = true;
    } else if ("LOV".equalsIgnoreCase(queryId)) {
      items = paramCount <= 2 ? cachedFixedItems.get(fieldId) : null;
      if (!StringUtils.isEmpty(cmrIssuingCntry)) {
        items = cachedFixedItems.get(fieldId + "_" + cmrIssuingCntry);
        addToCache = items == null && paramCount == 2; // fieldID and
                                                       // cmrIssuingCntry not
                                                       // counted
        lovForCntry = true;
      } else {
        addToCache = items == null && paramCount == 1; // fieldID not counted
      }
      bdsOrLov = true;
    }
    if (nocache) {
      // explicitly set items to null for nocache calls
      items = null;
    }

    if (items == null) {
      items = service.process(request, params);
    }
    if (items != null && items.getItems() != null && items.getItems().size() == 2) {
      if (StringUtils.isBlank(items.getItems().get(0).getId()) && StringUtils.isBlank(items.getItems().get(0).getName())) {
        items.setSelectedItem(items.getItems().get(1).getId());
      }
    }

    if (addToCache && !nocache) {
      if (bdsOrLov) {
        if (lovForCntry) {
          cachedFixedItems.put(fieldId + "_" + cmrIssuingCntry, items);
        } else {
          cachedFixedItems.put(fieldId, items);
        }
      } else {
        cachedFixedItems.put(queryId.toUpperCase(), items);
      }
    }

    ModelMap model = new ModelMap();
    model.addAttribute("listItems", items);
    return model;
  }

  public static void refresh() throws Exception {
    cachedFixedItems.clear();
  }

  /**
   * Tries to get the description of a code field based on the stored items
   * 
   * @param fieldId
   * @param code
   * @return
   */
  public static String getDescription(String fieldId, String code, String cmrIssuingCntry, boolean caseSensitive) {
    if (code == null) {
      return null;
    }
    DropdownModel items = cachedFixedItems.get(fieldId);
    if (items == null && !StringUtils.isEmpty(cmrIssuingCntry)) {
      items = cachedFixedItems.get(fieldId + "_" + cmrIssuingCntry);
    }
    if (items != null) {
      List<DropdownItemModel> list = items.getItems();
      for (DropdownItemModel item : list) {
        if (caseSensitive) {
          if (code.trim().equals(item.getId())) {
            return item.getName();
          }
        } else {
          if (code.trim().equalsIgnoreCase(item.getId())) {
            return item.getName();
          }
        }
      }
    }
    return code;
  }

  /**
   * Tries to get the description of a code field based on the stored items
   * 
   * @param fieldId
   * @param code
   * @return
   */
  public static String getDescription(String fieldId, String code, String cmrIssuingCntry) {
    return getDescription(fieldId, code, cmrIssuingCntry, false);
  }

  public static DropdownModel getCachedModel(String fieldId, String cmrIssuingCntry) {
    DropdownModel cached = cachedFixedItems.get(fieldId);
    if (cached != null) {
      return cached;
    }
    return cachedFixedItems.get(fieldId + "_" + cmrIssuingCntry);
  }

  public void initList(String queryId, String fieldId) throws CmrException {
    if (cachedFixedItems.containsKey(fieldId)) {
      return;
    }
    ParamContainer params = new ParamContainer();
    params.addParam("queryId", queryId);
    params.addParam("fieldId", fieldId);
    DropdownModel model = service.process(null, params);
    cachedFixedItems.put(fieldId, model);
  }

}
