/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SingleQuoteAttributeOutputtter extends XMLOutputter {

  public SingleQuoteAttributeOutputtter() {
    super();

  }

  public SingleQuoteAttributeOutputtter(Format format) {
    super(format);

  }

  public SingleQuoteAttributeOutputtter(XMLOutputter that) {
    super(that);

  }

  @SuppressWarnings("rawtypes")
  @Override
  protected void printAttributes(Writer writer, List attribs, Element parent, NamespaceStack namespaces) throws IOException {
    // Loop on attributes
    int count = 0;
    if (attribs != null && !attribs.isEmpty()) {
      writer.append(" ");
    }
    for (Object attribObj : attribs) {

      Attribute attrib = (Attribute) attribObj;

      writer.append(count > 0 ? " " : "");
      writer.append(attrib.getName() + "='" + attrib.getValue() + "'");

      count++;
    }
  }
}
