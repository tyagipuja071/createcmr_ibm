/**
 * 
 */
package com.ibm.cio.cmr.request.util.mq;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author JeffZAMORA
 * 
 */
public class WTAASXmlHandler extends MQXmlHandler {

  public WTAASXmlHandler(boolean response) {
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
    for (int i = 0; i < attributes.getLength(); i++) {
      this.currentKey = attributes.getLocalName(i);
      if (StringUtils.isEmpty(this.currentKey)) {
        this.currentKey = attributes.getQName(i);
      }
      this.currentValue = attributes.getValue(this.currentKey);
      xml.addValue(this.currentKey, this.currentValue);
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
