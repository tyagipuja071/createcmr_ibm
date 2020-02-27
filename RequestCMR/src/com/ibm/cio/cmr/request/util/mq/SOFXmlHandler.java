/**
 * 
 */
package com.ibm.cio.cmr.request.util.mq;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * {@link DefaultHandler} for SOF XMLs
 * 
 * @author JeffZAMORA
 * 
 */
public class SOFXmlHandler extends MQXmlHandler {

  public SOFXmlHandler(boolean response) {
    super(response);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (!this.rootRead) {
      this.rootName = localName;
      if (StringUtils.isEmpty(this.rootName)) {
        this.rootName = qName;
      }
      if (StringUtils.isEmpty(this.rootName)) {
        this.rootName = uri;
      }
      this.rootRead = true;
    }
    if (qName.equals("item")) {
      this.currentKey = attributes.getValue("name");
    }
    if (qName.equals("text")) {
      this.buffer.delete(0, this.buffer.length());
      this.currentValue = "";
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals("item")) {
      if (this.currentKey != null) {
        xml.addValue(this.currentKey, this.currentValue);
      }
      this.currentKey = null;
    }
    if (qName.equals("text")) {
      this.currentValue = this.buffer.toString();
    }

  }

}
