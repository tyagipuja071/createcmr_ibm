/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Outputter to format the XML of max attributes per line
 * 
 * @author Jeffrey Zamora
 * 
 */
public class AttributesPerLineOutputter extends XMLOutputter {
  private int attributesPerLine;
  private boolean omitDeclaration;

  public AttributesPerLineOutputter(int attributesPerLine) {
    this.attributesPerLine = attributesPerLine;
    setFormat(Format.getPrettyFormat());
  }

  private int elementDepth(Element element) {
    int result = 0;
    while (element != null) {
      result++;
      element = element.getParentElement();
    }
    return result;
  }

  public void omitDeclaration(boolean omit) {
    this.omitDeclaration = omit;
  }

  @Override
  protected void printDeclaration(Writer out, Document doc, String encoding) throws IOException {
    if (!this.omitDeclaration) {
      super.printDeclaration(out, doc, encoding);
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected void printAttributes(Writer writer, List attribs, Element parent, NamespaceStack ns) throws IOException {
    // Loop on attributes
    for (Object attribObj : attribs) {

      Attribute attrib = (Attribute) attribObj;

      if (attribs.size() > this.attributesPerLine) {

        writer.append("\n");

        for (int i = 0; i < elementDepth(parent); i++) {
          writer.append(this.getFormat().getIndent());
        }
      }

      List<Attribute> list = new ArrayList<Attribute>();
      list.add(attrib);
      super.printAttributes(writer, list, parent, ns);
    }
    writer.append("\n");
  }

}
