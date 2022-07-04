/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.io.IOException;
import java.io.Writer;

import org.jdom2.Attribute;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SingleQuoteAttributeOutputtter extends AbstractXMLOutputProcessor {

  public SingleQuoteAttributeOutputtter() {
    super();

  }

  @Override
  protected void printAttribute(Writer writer, FormatStack fstack, Attribute attrib) throws IOException {
    // Loop on attributes
    int count = 0;
    writer.append(" ");

    writer.append(attrib.getName() + "='" + attrib.getValue() + "'");

  }
}
