/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.controller.requestentry.RequestEntryController;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.Person;

/**
 * Tag to handle BluePages selection support.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class BluePagesTag extends BodyTagSupport {

  private static final long serialVersionUID = 1L;
  private static Logger LOG = Logger.getLogger(RequestEntryController.class);

  private String namePath;
  private String idPath;
  private String model;
  private String resourcesPath;
  private boolean showId;
  private boolean useUID;
  private boolean useBothIds;
  private boolean editableId;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();
    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    this.resourcesPath = request.getContextPath() + "/resources";

    Object model = request.getAttribute(this.model);
    String nameValue = null;
    String contValue = null;
    String idValue = null;

    if (model != null) {
      nameValue = getModelValue(model, this.namePath);
      if (this.idPath != null) {
        idValue = getModelValue(model, this.idPath);
      }
      if (nameValue != null) {
        contValue = nameValue + ":" + idValue;
      }
    }
    String internalContId = this.namePath + "_bpcont";

    // main name field
    sb.append("<input name=\"" + this.namePath + "\" id=\"" + this.namePath + "\" bpId=\"" + this.idPath + "\" style=\"width:250px\" "
        + (nameValue != null ? "value=\"" + nameValue + "\"" : "") + " onchange=\"bpOnChange(this)\" maxlength=\"120\">\n");

    // w3 image
    sb.append("<img class=\"cmr-w3-icon\" src=\"" + this.resourcesPath + "/images/w3.ico\" title=\"Enter a name to query BluePages\">\n");

    // the hidden item container
    sb.append("<input name=\"" + internalContId + "\" id=\"" + internalContId + "\" type=\"hidden\" "
        + (contValue != null ? "value=\"" + contValue + "\"" : "") + ">\n");

    // optional ID field
    if (this.idPath != null && !this.editableId) {
      sb.append("<input name=\"" + this.idPath + "\" id=\"" + this.idPath + "\" type=\"hidden\" "
          + (idValue != null ? "value=\"" + idValue + "\"" : "") + ">\n");
    }
    if (this.showId) {
      sb.append("<span id=\"" + this.namePath + "_readonly\" class=\"cmr-bluepages-ro\">"
          + (!StringUtils.isEmpty(idValue) ? idValue : "(none selected)") + "</span>");
      if (this.useBothIds) {
        String uid = (String) request.getAttribute(this.idPath + "_UID");
        if (StringUtils.isEmpty(uid)) {
          uid = "";
        } else {
          uid = "(" + uid + ")";
        }
        sb.append("<span style=\"padding-left:5px\" id=\"" + this.namePath + "_uid\" class=\"cmr-bluepages-ro\">" + uid + "</span>");
      }
    }
    sb.append("<script>");
    sb.append("  dojo.addOnLoad(function() {\n");
    sb.append("    cmr.setBluePagesSearch('" + this.namePath + "'" + (this.idPath != null ? ", '" + this.idPath + "'" : "") + ", " + this.useUID
        + ", null, " + this.useBothIds + ");\n");
    if (this.useBothIds && !StringUtils.isEmpty(idValue)) {
      try {
        Person p = BluePagesHelper.getPerson(idValue);
        sb.append("    cmr.getUID('" + p.getEmployeeId().substring(0, p.getEmployeeId().length() - 3) + "', '" + this.namePath + "_uid')");
      } catch (CmrException e) {
        LOG.error("Error in BluePageHelper for BluePageTag", e);
      }
    }
    sb.append("});\n");
    sb.append("</script>\n");
    try {
      this.pageContext.getOut().append(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }

    return SKIP_BODY;
  }

  private String getModelValue(Object model, String property) {
    try {
      Object value = PropertyUtils.getProperty(model, property);
      if (value != null && value instanceof String) {
        return (String) value;
      } else {
        return value != null ? value.toString() : null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public String getResourcesPath() {
    return resourcesPath;
  }

  public void setResourcesPath(String resourcesPath) {
    this.resourcesPath = resourcesPath;
  }

  public String getNamePath() {
    return namePath;
  }

  public void setNamePath(String namePath) {
    this.namePath = namePath;
  }

  public String getIdPath() {
    return idPath;
  }

  public void setIdPath(String idPath) {
    this.idPath = idPath;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public boolean isShowId() {
    return showId;
  }

  public void setShowId(boolean showId) {
    this.showId = showId;
  }

  public boolean isUseUID() {
    return useUID;
  }

  public void setUseUID(boolean useUID) {
    this.useUID = useUID;
  }

  public boolean isEditableId() {
    return editableId;
  }

  public void setEditableId(boolean editableId) {
    this.editableId = editableId;
  }

  public boolean isUseBothIds() {
    return useBothIds;
  }

  public void setUseBothIds(boolean useBothIds) {
    this.useBothIds = useBothIds;
  }

}
