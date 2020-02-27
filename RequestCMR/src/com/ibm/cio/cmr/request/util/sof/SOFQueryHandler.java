/**
 * 
 */
package com.ibm.cio.cmr.request.util.sof;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.SOFServiceClient;
import com.ibm.cmr.services.client.sof.SOFQueryRequest;
import com.ibm.cmr.services.client.sof.SOFQueryResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SOFQueryHandler extends DefaultHandler {

  private List<SOFRecord> records = new ArrayList<SOFRecord>();
  private Map<String, String> dataElements = new LinkedHashMap<>();

  private Map<String, String> currAddr = null;

  private List<Map<String, String>> addresses = new ArrayList<>();

  private String currentItemName = null;
  private String currentValue = null;

  private boolean addressMode;
  private boolean readingRecord = false;
  private StringBuffer buffer = new StringBuffer();

  public static void main(String[] args) throws Exception {
    SOFServiceClient client = CmrServicesFactory.getInstance().createClient("http://dgadaldecmr01.sl.bluecloud.ibm.com:9443/CMRServicesV2",
        SOFServiceClient.class);

    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry("706");
    request.setCmrNo("001007");
    request.setSiret("00658019500011");

    SOFQueryResponse response = client.executeAndWrap(SOFServiceClient.QUERY_APP_ID, request, SOFQueryResponse.class);
    if (response.isSuccess()) {
      String xmlData = response.getData();
      SOFQueryHandler handler = new SOFQueryHandler();
      List<SOFRecord> resp = handler.extractRecord(xmlData.getBytes());
      for (SOFRecord record : resp) {
        System.out.println("CN: " + record.getCustomerNo());
        for (SOFAttribute att : record.getDataElements().values()) {
          System.out.println("Elem " + att.getName() + " - " + att.getValue());
        }
        for (SOFAddress addr : record.getAddresses()) {
          System.out.println("  - " + addr.getType() + "/" + addr.getSeqNo());
          System.out.println("Address1 " + addr.getAttribute("Address1").getValue());
          for (SOFAttribute att : addr.getAttributes().values()) {
            System.out.println("Elem " + att.getName() + " - " + att.getValue());
          }
        }
      }
    }

  }

  public List<SOFRecord> extractRecord(byte[] xmlData) throws Exception {
    ByteArrayInputStream bis = new ByteArrayInputStream(xmlData);
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      InputSource src = new InputSource(bis);
      src.setEncoding("UTF-8");
      parser.parse(src, this);
    } finally {
      bis.close();
    }

    return this.records;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

    if ("softordc".equals(qName) && attributes.getValue("customerNumber") != null) {
      this.dataElements.clear();
      this.addresses.clear();
      this.readingRecord = true;
    } else if ("softordc".equals(qName)) {
      this.readingRecord = false;
    }

    if (qName.equals("item")) {
      this.buffer.delete(0, this.buffer.length());
      this.currentItemName = attributes.getValue("name");
    }
    if (qName.equals("addresses")) {
      this.addressMode = true;
    }

    if (qName.equals("address")) {
      this.currAddr = new LinkedHashMap<>();
    }

  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if ("softordc".equals(qName) && this.readingRecord) {
      SOFRecord record = new SOFRecord(this.dataElements, this.addresses);
      if (record.isSuccess()) {
        this.records.add(record);
      }
      this.readingRecord = false;
    } else if ("softordc".equals(qName)) {
      this.readingRecord = false;
    }
    if (qName.equals("item")) {
      this.currentItemName = null;
    }
    if (qName.equals("text")) {
      this.currentValue = this.buffer.toString();
      if (this.currentItemName != null) {
        if (this.addressMode && this.currAddr != null) {
          this.currAddr.put(this.currentItemName, this.currentValue != null ? this.currentValue.trim() : "");
        } else if (!this.addressMode) {
          this.dataElements.put(this.currentItemName, this.currentValue != null ? this.currentValue.trim() : "");
        }
      }
    }

    if (qName.equals("address")) {
      if (this.currAddr != null) {
        this.addresses.add(this.currAddr);
      }
      this.currAddr = null;
    }
    if (qName.equals("addresses")) {
      this.addressMode = false;
    }

  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    this.buffer.append(ch, start, length);
  }

}
