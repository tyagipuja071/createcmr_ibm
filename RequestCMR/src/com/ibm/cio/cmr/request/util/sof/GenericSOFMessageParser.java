/**
 * 
 */
package com.ibm.cio.cmr.request.util.sof;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.SOFServiceClient;
import com.ibm.cmr.services.client.sof.SOFQueryRequest;
import com.ibm.cmr.services.client.sof.SOFQueryResponse;

/**
 * Parses SOF XML messages
 * 
 * @author Jeffrey Zamora
 * 
 */
public class GenericSOFMessageParser extends DefaultHandler {

  private StringBuffer buffer = new StringBuffer();
  private String currentItemName;
  private String currentAddressSeq;
  private List<String> shippingSequences = new ArrayList<>();
  private List<String> countryCSequences = new ArrayList<>();
  private List<String> installingSequences = new ArrayList<>();
  private String currentValue = null;
  private Map<String, String> valueMap = new HashMap<String, String>();
  private boolean addressTagFound = false;

  private static final String SHIPPING_KEY = "Shipping";
  private static final String COUNTRY_USE_C_KEY = "CtryUseC";
  private static final String INSTALLING_KEY = "Installing";

  public static void main(String[] args) throws Exception {
    SOFServiceClient client = CmrServicesFactory.getInstance().createClient("http://dgadaldecmr01.sl.bluecloud.ibm.com:9443/CMRServicesV2",
        SOFServiceClient.class);

    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry("756");
    request.setCmrNo("371500");

    try {
      SOFQueryResponse response = client.executeAndWrap(SOFServiceClient.QUERY_APP_ID, request, SOFQueryResponse.class);
      if (response.isSuccess()) {
        String xmlData = response.getData();

        GenericSOFMessageParser handler = new GenericSOFMessageParser();
        ByteArrayInputStream bis = new ByteArrayInputStream(xmlData.getBytes());
        try {
          SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
          parser.parse(new InputSource(bis), handler);
        } finally {
          bis.close();
        }

        Map<String, String> map = handler.getValues();
        if (map != null) {
          Set<String> keys = map.keySet();
          List<String> x = new ArrayList<String>(keys);
          Collections.sort(x);
          for (String key : x) {
            System.out.println(key + " = " + map.get(key));
          }
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equals("item")) {
      this.currentItemName = attributes.getValue("name");

      if (this.addressTagFound && this.currentItemName.endsWith("Name") && this.valueMap.containsKey("AddressNumber")) {
        String val = this.valueMap.remove("AddressNumber");
        String addrType = this.currentItemName.substring(0, this.currentItemName.indexOf("Name"));
        this.currentAddressSeq = val;
        this.valueMap.put(addrType + "AddressNumber", val);

        if (SHIPPING_KEY.equals(addrType)) {
          this.valueMap.put(addrType + "_" + val + "_" + "AddressNumber", val);
          this.shippingSequences.add(val);
        }
        if (COUNTRY_USE_C_KEY.equals(addrType)) {
          this.valueMap.put(addrType + "_" + val + "_" + "AddressNumber", val);
          this.countryCSequences.add(val);
        }
        if (INSTALLING_KEY.equals(addrType)) {
          this.valueMap.put(addrType + "_" + val + "_" + "AddressNumber", val);
          this.installingSequences.add(val);
        }

      }
    }
    if (qName.equals("text")) {
      this.buffer.delete(0, this.buffer.length());
    }
    if (qName.equals("address")) {
      this.addressTagFound = true;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals("item")) {
      this.currentItemName = null;
    }
    if (qName.equals("text")) {
      this.currentValue = this.buffer.toString();
      if (this.currentItemName != null && !StringUtils.isBlank(this.currentValue)) {

        this.valueMap.put(this.currentItemName, this.currentValue);

        if (this.currentItemName.startsWith(SHIPPING_KEY) && !StringUtils.isEmpty(this.currentAddressSeq)) {
          String name = this.currentItemName.substring(8);
          this.valueMap.put(SHIPPING_KEY + "_" + currentAddressSeq + "_" + name, this.currentValue);
        }

        if (this.currentItemName.startsWith(COUNTRY_USE_C_KEY) && !StringUtils.isEmpty(this.currentAddressSeq)) {
          String name = this.currentItemName.substring(8);
          this.valueMap.put(COUNTRY_USE_C_KEY + "_" + currentAddressSeq + "_" + name, this.currentValue);
        }
        if (this.currentItemName.startsWith(INSTALLING_KEY) && !StringUtils.isEmpty(this.currentAddressSeq)) {
          String name = this.currentItemName.substring(10);
          this.valueMap.put(INSTALLING_KEY + "_" + currentAddressSeq + "_" + name, this.currentValue);
        }

      }
    }
    if (qName.equals("address")) {
      this.addressTagFound = false;
      this.currentAddressSeq = null;
    }
  }

  public void setValues(Map<String, String> values) {
    this.valueMap = values;
  }

  public Map<String, String> getValues() {
    return this.valueMap;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    this.buffer.append(ch, start, length);
  }

  public List<String> getShippingSequences() {
    return shippingSequences;
  }

  public List<String> getCountryCSequences() {
    return countryCSequences;
  }

  public void setCountryCSequences(List<String> countryCSequences) {
    this.countryCSequences = countryCSequences;
  }

  public List<String> getInstallingSequences() {
    return installingSequences;
  }

  public void setInstallingSequences(List<String> installingSequences) {
    this.installingSequences = installingSequences;
  }

  public void setShippingSequences(List<String> shippingSequences) {
    this.shippingSequences = shippingSequences;
  }
}
