/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag for Notes on the UI
 * 
 * @author Jeffrey Zamora
 * 
 */
public class NoteTag extends TagSupport {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String text;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    sb.append("<span class=\"ibm-item-note\">" + this.text + "</span>");
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

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
