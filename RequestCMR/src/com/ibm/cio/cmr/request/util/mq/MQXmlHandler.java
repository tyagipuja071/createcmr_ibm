/**
 * 
 */
package com.ibm.cio.cmr.request.util.mq;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author JeffZAMORA
 * 
 */
public abstract class MQXmlHandler extends DefaultHandler {

  protected StringBuffer buffer = new StringBuffer();
  protected String currentKey;
  protected String currentValue;
  protected MQXml xml = new MQXml();
  private boolean response;

  protected boolean rootRead;
  protected String rootName;

  protected MQXmlHandler(boolean response) {
    this.response = response;
  }

  public static MQXmlHandler getHandler(String data) {
    if (data.toUpperCase().contains("<CR010CR") || data.toUpperCase().contains("<CR020CR") || data.toUpperCase().contains("<RY")) {
      return new WTAASXmlHandler(data.toUpperCase().contains("<CR020CR") || data.toUpperCase().contains("<RY"));
    }
    if (data.toUpperCase().contains("<RDCTOSOF>") || data.toUpperCase().contains("<SOFTORDC>")) {
      return new SOFXmlHandler(data.toUpperCase().contains("<SOFTORDC>"));
    }
    return new MQXmlHandler(false) {
    };
  }

  public void newXml(String name) {
    this.xml = new MQXml();
    this.xml.setResponse(this.response);
    this.xml.setName(name);
    this.rootRead = false;
  }

  @Override
  public void startDocument() throws SAXException {
    this.currentKey = null;
    this.buffer = new StringBuffer();
    this.currentValue = null;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    this.buffer.append(ch, start, length);
  }

  @Override
  public void endDocument() throws SAXException {
    this.xml.setRootName(this.rootName);
  }

  public MQXml getXml() {
    return xml;
  }

  public void setXml(MQXml xml) {
    this.xml = xml;
  }

}
