/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;

/**
 * Renders the information bubble
 * 
 * @author Jeffrey Zamora
 * 
 */
public class DeltaTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String text;
  private String oldValue;
  private String id;
  private String code;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    String resources = request.getContextPath() + "/resources";
    RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");

    String textStr = StringUtils.isEmpty(this.text.trim()) ? "-" : this.text.trim();

    boolean show = true;

    if (!StringUtils.isEmpty(this.oldValue)) {
      if (!StringUtils.isEmpty(this.text)) {
        String val = this.text.trim();
        if (this.code != null && "L".equals(this.code)) {
          val = val.substring(0, val.indexOf("-")).trim();
        }
        if (this.code != null && "R".equals(this.code)) {
          val = val.substring(val.indexOf("-") + 1).trim();
        }
        if (this.oldValue.trim().equals(val)) {
          show = false;
        }
      }
    }
    if (StringUtils.isEmpty(this.oldValue) && StringUtils.isEmpty(this.text)) {
      show = false;
    }

    if (reqentry != null && reqentry.getReqId() < 1) {
      show = false;
    }

    if (reqentry != null && !"U".equals(reqentry.getReqType())) {
      show = false;
    }

    if (show) {
      String id = this.id != null ? " id=\"" + this.id + "\"" : "";
      sb.append("<img src=\"" + resources + "/images/change-icon.png\" title=\"Old Value : " + textStr + "\" class=\"cmr-delta-icon\"" + id + " "
          + (this.code != null ? "coded=\"" + this.code + "\"" : "") + ">");

      try {
        this.pageContext.getOut().write(sb.toString());
      } catch (IOException e) {
        throw new JspException(e);
      }
    }
    return SKIP_BODY;
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOldValue() {
    return oldValue;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

}
