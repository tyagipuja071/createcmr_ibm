/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.tags.form.CheckboxTag;
import org.springframework.web.servlet.tags.form.InputTag;
import org.springframework.web.servlet.tags.form.RadioButtonTag;
import org.springframework.web.servlet.tags.form.SelectTag;
import org.springframework.web.servlet.tags.form.TextareaTag;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.model.DropdownItemModel;
import com.ibm.cio.cmr.request.ui.FieldInformation;
import com.ibm.cio.cmr.request.ui.FieldManager;
import com.ibm.cio.cmr.request.ui.PageConfig;
import com.ibm.cio.cmr.request.ui.PageManager;

/**
 * @author Jeffrey Zamora
 * 
 */
public class FieldTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private String fieldId;
  private String path;
  private String size;
  private String id;
  private String tabId;
  private String model;
  private String idPath;
  private String rows;
  private String cols;
  private String placeHolder;
  private String dropdownFieldId;
  private int breakAfter;
  private static final String TEXT = "T";
  private static final String DROPDOWN = "D";
  private static final String READONLY = "O";
  private static final String RADIO = "R";
  private static final String CHECKBOX = "C";
  private static final String MEMO = "M";
  private static final String BLUEPAGES = "B";

  @Override
  public int doEndTag() throws JspException {

    PageManager mgr = PageManager.get();
    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    String cntry = PageManager.getCurrentCountry(request);
    PageConfig config = PageManager.getConfig(request);

    config.getFieldMap().put(this.fieldId, this.id == null ? this.path : this.id);
    if (this.tabId != null) {
      config.getTabs().put(this.fieldId, this.tabId);
    }
    FieldManager fieldMgr = mgr.getManager(this.fieldId);
    try {
      if (fieldMgr == null) {
        this.pageContext.getOut().append("-");
        return EVAL_PAGE;
      }

      String type = fieldMgr.getType();
      List<FieldInformation> cntryInfo = fieldMgr.getFieldInfo(cntry == null ? "all" : "c" + cntry);
      if (cntryInfo != null && cntryInfo.size() > 0) {
        type = cntryInfo.get(0).getType();
      }
      if (TEXT.equals(type)) {
        generateTextField(fieldMgr, request);
      } else if (RADIO.equals(type)) {
        generateRadio(fieldMgr, request);
      } else if (DROPDOWN.equals(type)) {
        generateDropdown(fieldMgr, config, request);
      } else if (READONLY.equals(type)) {
        generateReadOnlyText(fieldMgr);
      } else if (BLUEPAGES.equals(type)) {
        generateBluePages(fieldMgr);
      } else if (MEMO.equals(type)) {
        generateMemo(fieldMgr);
      } else if (CHECKBOX.equals(type)) {
        generateCheckbox(fieldMgr);
      }
    } catch (IOException e) {
      throw new JspException(e);
    }

    return EVAL_PAGE;
  }

  private void generateTextField(FieldManager fieldMgr, HttpServletRequest request) throws JspException {
    InputTag input = new InputTag();
    input.setPageContext(this.pageContext);
    input.setPath(this.path);
    input.setSize(this.size);
    String cntry = PageManager.getCurrentCountry(request);
    List<FieldInformation> info = fieldMgr.getFieldInfo(cntry == null ? "all" : "c" + cntry);
    if (info == null || info.size() == 0) {
      info = fieldMgr.getFieldInfo("all");
    }
    if (info != null && info.size() > 0) {
      int maxLength = info.get(0).getMaxLength();
      if (maxLength == 0) {
        info = fieldMgr.getFieldInfo("all");
        maxLength = info.size() > 0 ? info.get(0).getMaxLength() : 0;
      }
      input.setMaxlength(maxLength > 0 ? maxLength + "" : null);
    }
    input.setId(this.id);
    input.setDynamicAttribute(null, "fieldId", fieldMgr.getFieldId());
    input.setDynamicAttribute(null, "dojoType", "dijit.form.TextBox");
    input.setDynamicAttribute(null, "trim", "true");
    if (this.size != null) {
      input.setCssStyle("width:" + this.size + "px;");
    }
    if (this.placeHolder != null) {
      input.setDynamicAttribute(null, "placeHolder", this.placeHolder);
    }
    input.doStartTag();
    input.doEndTag();
  }

  private void generateDropdown(FieldManager fieldMgr, PageConfig config, HttpServletRequest request) throws JspException {
    SelectTag select = new SelectTag();
    select.setPageContext(this.pageContext);
    select.setPath(this.path);
    select.setId(this.id);
    String label = PageManager.getLabel(request, this.fieldId, "");
    select.setDynamicAttribute(null, "placeHolder", "Select " + label);
    select.setDynamicAttribute(null, "dojoType", "dijit.form.FilteringSelect");
    select.setDynamicAttribute(null, "searchAttr", "name");
    select.setDynamicAttribute(null, "style", "display: block;");
    select.setDynamicAttribute(null, "maxHeight", "200");
    select.setDynamicAttribute(null, "required", "false");
    select.setDynamicAttribute(null, "fieldId", fieldMgr.getFieldId());
    if (this.size != null) {
      select.setCssStyle("width:" + this.size + "px;");
    }
    select.doStartTag();
    select.doEndTag();

    String refTable = "bds";
    String cntry = PageManager.getCurrentCountry(request);
    List<FieldInformation> info = fieldMgr.getFieldInfo(cntry == null ? "all" : "c" + cntry);
    if (info == null) {
      info = fieldMgr.getFieldInfo("all");
    }
    if (info != null && info.size() > 0) {
      String choices = info.get(0).getChoice();
      if ("L".equals(choices)) {
        refTable = "lov";
      } else if ("B".equals(choices)) {
        refTable = "bds";
      }
    }

    if (!StringUtils.isBlank(this.dropdownFieldId)) {
      refTable += ":" + this.dropdownFieldId;
    }
    config.getDropdowns().put(this.fieldId, refTable);
  }

  private void generateRadio(FieldManager fieldMgr, HttpServletRequest request) throws JspException, IOException {
    Map<String, List<DropdownItemModel>> map = fieldMgr.getItemMap();
    String cntry = PageManager.getCurrentCountry(request);
    if (StringUtils.isEmpty(cntry)) {
      cntry = "all";
    }
    List<DropdownItemModel> model = map.get(cntry);
    if (model == null) {
      model = map.get("all");
    }
    if (model == null) {
      return;
    }
    int count = 0;
    for (DropdownItemModel item : model) {

      JspWriter writer = this.pageContext.getOut();

      writer.print("<span id=\"radiocont_" + item.getId() + "\">");
      RadioButtonTag radio = new RadioButtonTag();
      radio.setPageContext(this.pageContext);
      radio.setPath(this.path);
      radio.setValue(item.getId());
      String idLocal = this.id == null ? this.path : this.id;
      radio.setId(idLocal + "_" + item.getId());
      radio.setDynamicAttribute(null, "cmrConfigName", idLocal);
      radio.setDynamicAttribute(null, "checked", true);
      radio.doStartTag();
      radio.doEndTag();

      LabelTag label = new LabelTag();
      label.setPageContext(this.pageContext);
      label.setFieldId(idLocal + "_" + item.getId());
      label.setForRadioOrCheckbox(true);
      label.doStartTag();
      this.pageContext.getOut().append(item.getName());
      label.doEndTag();

      writer.print("</span>");

      count++;

      if (this.breakAfter > 0 && count == this.breakAfter) {
        writer.print("<br>");
      }
    }
  }

  private void generateCheckbox(FieldManager fieldMgr) throws JspException {
    CheckboxTag checkbox = new CheckboxTag();
    checkbox.setPageContext(this.pageContext);
    checkbox.setPath(this.path);
    checkbox.setId(this.id != null ? this.id : this.path);
    checkbox.setDynamicAttribute(null, "fieldId", fieldMgr.getFieldId());
    // checkbox.setDynamicAttribute(null, "dojoType", "dijit.form.CheckBox");
    checkbox.setValue(CmrConstants.YES_NO.Y.toString());
    checkbox.doStartTag();
    checkbox.doEndTag();
  }

  private void generateReadOnlyText(FieldManager fieldMgr) throws JspException {

  }

  private void generateBluePages(FieldManager fieldMgr) throws JspException {
    BluePagesTag bp = new BluePagesTag();
    bp.setPageContext(this.pageContext);
    bp.setModel(this.model);
    bp.setNamePath(this.path);
    bp.setIdPath(this.idPath);
    bp.setShowId(true);
    bp.doStartTag();
    bp.doEndTag();
  }

  private void generateMemo(FieldManager fieldMgr) throws JspException {
    TextareaTag memo = new TextareaTag();
    memo.setPageContext(this.pageContext);
    memo.setId(this.id);
    memo.setPath(this.path);
    memo.setRows(this.rows);
    memo.setCols(this.cols);
    memo.setDynamicAttribute(null, "style", "overflow-y:scroll");
    memo.doStartTag();
    memo.doEndTag();
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public String getTabId() {
    return tabId;
  }

  public void setTabId(String tabId) {
    this.tabId = tabId;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getIdPath() {
    return idPath;
  }

  public void setIdPath(String idPath) {
    this.idPath = idPath;
  }

  public String getRows() {
    return rows;
  }

  public void setRows(String rows) {
    this.rows = rows;
  }

  public String getCols() {
    return cols;
  }

  public void setCols(String cols) {
    this.cols = cols;
  }

  public String getPlaceHolder() {
    return placeHolder;
  }

  public void setPlaceHolder(String placeHolder) {
    this.placeHolder = placeHolder;
  }

  public String getDropdownFieldId() {
    return dropdownFieldId;
  }

  public void setDropdownFieldId(String dropdownFieldId) {
    this.dropdownFieldId = dropdownFieldId;
  }

  public int getBreakAfter() {
    return breakAfter;
  }

  public void setBreakAfter(int breakAfter) {
    this.breakAfter = breakAfter;
  }

}
