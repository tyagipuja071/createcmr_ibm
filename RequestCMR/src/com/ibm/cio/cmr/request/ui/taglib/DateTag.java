/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.servlet.tags.form.InputTag;

import com.ibm.cio.cmr.request.config.SystemConfiguration;

/**
 * Renders a date field
 * 
 * @author Jeffrey Zamora
 * 
 */
public class DateTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String path;
  private String id;
  private String format;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    if (this.id == null) {
      this.id = this.path;
    }

    if (this.format == null) {
      this.format = SystemConfiguration.getValue("DATE_FORMAT");
    }

    InputTag input = new InputTag();
    input.setPageContext(this.pageContext);
    input.setPath(this.path);
    input.setSize("15");
    input.setId(this.id);
    input.doStartTag();
    input.doEndTag();
    HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();
    String resources = req.getContextPath() + "/resources";
    String js = "onclick=\"focusActualField('" + this.id + "')\"";
    sb.append("<img title=\"Choose a date\" src=\"" + resources + "/images/date-icon.png\" class=\"cmr-date-icon\" " + js + ">\n");

    /*
     * String scriptAdded = (String)
     * this.pageContext.getAttribute("_dateScriptsAdded"); if (scriptAdded ==
     * null) { sb.append(
     * "<link rel=\"stylesheet\" href=\"//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css\">\n"
     * );
     * sb.append("<script src=\"//code.jquery.com/jquery-1.10.2.js\"></script>\n"
     * ); sb.append(
     * "<script src=\"//code.jquery.com/ui/1.11.4/jquery-ui.js\"></script>\n");
     * this.pageContext.setAttribute("_dateScriptsAdded", "Y"); }
     */

    sb.append("<script>\n");
    sb.append("  dojo.addOnLoad(function() {\n");
    this.format = this.format.replaceAll("yyyy", "yy");
    this.format = this.format.replaceAll("M", "m");
    sb.append("$( '#" + this.id + "' ).datepicker({ dateFormat: '" + this.format + "', constrainInput: false }); window.jQuery = null;\n");
    sb.append("  });\n");
    sb.append("</script>\n");
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

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
