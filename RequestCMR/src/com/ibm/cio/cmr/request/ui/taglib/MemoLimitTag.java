/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MemoLimitTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String fieldId;
  private int maxLength;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();
    sb.append("<span class=\"cmr-txtarea-limit\">(<span id=\"" + this.fieldId + "_charind\">" + this.maxLength + "</span> chars remaining)</span>");
    sb.append("<script>\n");
    sb.append("dojo.addOnLoad(function() {\n");
    sb.append("dojo.byId('" + this.fieldId + "').onkeyup = function() {\n");
    sb.append("  var txtArea = dojo.byId('" + this.fieldId + "');\n");
    sb.append("  if (!txtArea){\n");
    sb.append("    return;\n");
    sb.append("   }\n");
    sb.append("   var chars = txtArea.value.length;\n");
    sb.append("  document.getElementById('" + this.fieldId + "_charind').innerHTML = " + this.maxLength + " - chars;\n");
    sb.append("  if (chars > " + this.maxLength + ") {\n");
    sb.append("    txtArea.value = txtArea.value.substring(0, " + this.maxLength + ");\n");
    sb.append("  }\n");
    sb.append("};\n");
    sb.append("document.getElementById('" + this.fieldId + "_charind').innerHTML = " + this.maxLength + " - dojo.byId('" + this.fieldId
        + "').value.length;\n");
    sb.append("});\n");
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

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

}
