/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.tags.form.FormTag;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * @author 136786PH1
 *
 */
public class CmrFormTag extends FormTag {

  private static final long serialVersionUID = 1L;

  @Override
  protected void writeDefaultAttributes(TagWriter tagWriter) throws JspException {
    tagWriter.writeOptionalAttributeValue("_csrf", UUID.randomUUID().toString());
    super.writeDefaultAttributes(tagWriter);
  }

  @Override
  public int doEndTag() throws JspException {
    try {
      this.pageContext.getOut().append("<input type=\"hidden\" name=\"_csrf\" id=\"_csrf\" value=\"" + UUID.randomUUID().toString() + "\">");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return super.doEndTag();
  }
}
