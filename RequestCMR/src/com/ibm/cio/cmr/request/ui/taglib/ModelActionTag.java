/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.servlet.tags.form.HiddenInputTag;

/**
 * Generates the hidden items for the form
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ModelActionTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String formName;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    HiddenInputTag hidden = new HiddenInputTag();
    hidden.setPageContext(this.pageContext);
    hidden.setPath("state");
    hidden.setId(this.formName + "_modelState");
    hidden.doStartTag();
    hidden.doEndTag();

    hidden = new HiddenInputTag();
    hidden.setPageContext(this.pageContext);
    hidden.setPath("action");
    hidden.setId(this.formName + "_modelAction");
    hidden.doStartTag();
    hidden.doEndTag();

    hidden = new HiddenInputTag();
    hidden.setPageContext(this.pageContext);
    hidden.setPath("massAction");
    hidden.setId(this.formName + "_modelMassAction");
    hidden.doStartTag();
    hidden.doEndTag();

    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public String getFormName() {
    return formName;
  }

  public void setFormName(String formName) {
    this.formName = formName;
  }
}
