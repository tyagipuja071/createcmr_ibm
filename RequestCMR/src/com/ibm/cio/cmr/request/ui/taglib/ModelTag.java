/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Jeffrey Zamora
 * 
 */
public class ModelTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String model;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    Object model = this.pageContext.getRequest().getAttribute(this.model);
    try {
      sb.append("<script>\n");
      if (model != null) {
        String json = MAPPER.writeValueAsString(model);
        sb.append(" var _pagemodel = " + json + ";\n");
      } else {
        sb.append(" var _pagemodel = {};\n");
      }
      sb.append("</script>\n");
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

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

}
