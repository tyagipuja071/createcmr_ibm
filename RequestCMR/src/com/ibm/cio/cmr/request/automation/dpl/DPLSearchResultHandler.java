/**
 * 
 */
package com.ibm.cio.cmr.request.automation.dpl;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author JeffZAMORA
 * 
 */
public class DPLSearchResultHandler extends DefaultHandler {

  protected StringBuffer buffer = new StringBuffer();
  private List<DPLSearchItem> results = new ArrayList<DPLSearchItem>();
  private DPLSearchItem current = null;
  private String currType = null;

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    this.buffer.delete(0, this.buffer.length());
    if ("TR".equals(qName)) {
      this.current = new DPLSearchItem();
    } else if ("TD".equals(qName)) {
      this.currType = null;
      String className = atts.getValue("CLASS");
      if (className != null && className.contains("ITEM")) {
        this.currType = "I";
      } else if (className != null && className.contains("DCC")) {
        this.currType = "C";
      } else if (className != null && className.contains("DPN")) {
        this.currType = "P";
      }
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if ("TR".equals(qName) && this.current != null) {
      this.results.add(this.current);
      current = null;
    } else if ("TD".equals(qName)) {
      if (this.currType != null && this.current != null) {
        switch (this.currType) {
        case "I":
          this.current.setItem(this.buffer.toString().trim());
          break;
        case "C":
          this.current.setCountryCode(this.buffer.toString().trim());
          break;
        case "P":
          this.current.setPartyName(this.buffer.toString().trim());
          break;
        }
      }
    }
  }

  public List<DPLSearchItem> getResults() {
    return this.results;
  }

  @Override
  public void characters(char[] buff, int start, int length) throws SAXException {
    this.buffer.append(buff, start, length);
  }
}
