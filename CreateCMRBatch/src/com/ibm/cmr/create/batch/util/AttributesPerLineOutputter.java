/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.io.IOException;
import java.io.Writer;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;

/**
 * Outputter to format the XML of max attributes per line
 * 
 * @author Jeffrey Zamora
 * 
 */
public class AttributesPerLineOutputter extends AbstractXMLOutputProcessor {
  protected int attributesPerLine;
  private boolean omitDeclaration = true;

  public AttributesPerLineOutputter(int attributesPerLine) {
    this.attributesPerLine = attributesPerLine;
  }

  protected int elementDepth(Element element) {
    int result = 0;
    while (element != null) {
      result++;
      if (element.getParent() instanceof Element) {
        element = (Element) element.getParent();
      } else {
        element = null;
      }
    }
    return result;
  }

  public void omitDeclaration(boolean omit) {
    this.omitDeclaration = omit;
  }

  @Override
  protected void printDeclaration(Writer out, FormatStack stack) throws IOException {
    if (!this.omitDeclaration) {
      super.printDeclaration(out, stack);
    }
  }

  @Override
  protected void printAttribute(Writer writer, FormatStack fstack, Attribute attrib) throws IOException {
    writer.append("\n");
    super.printAttribute(writer, fstack, attrib);
  }

}
