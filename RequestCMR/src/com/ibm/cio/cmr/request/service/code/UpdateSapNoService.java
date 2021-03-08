package com.ibm.cio.cmr.request.service.code;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.cio.cmr.request.model.code.AddressTypeMapping;

public class UpdateSapNoService extends DefaultHandler {
  private UpdateSapNoService updateSapNoService;
  // internals
  private StringBuffer buffer;
  private String countries = null;
  private AddressTypeMapping addrMapping;
  private boolean readTypeMapping;
  private String inputType;
  private String outputType;
  private Map<String, AddressTypeMapping> addressTypeMappings = new HashMap<>();

  @Override
  public void startDocument() throws SAXException {
    this.updateSapNoService = new UpdateSapNoService();
    this.buffer = new StringBuffer();
  }

  // parser starts parsing a specific element inside the document
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if ("typeMappings".equals(qName)) {
      this.readTypeMapping = true;
    }

    if (this.readTypeMapping) {
      if ("mapping".equals(qName)) {
        this.addrMapping = new AddressTypeMapping();
      }
      if ("typeMap".equals(qName)) {
        this.inputType = null;
        this.outputType = null;
      }
    }

    this.buffer.delete(0, this.buffer.length());
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    String value = this.buffer.toString().trim();
    if ("typeMappings".equals(qName)) {
      this.setTypeMapping(this.countries, this.addrMapping);
      this.readTypeMapping = false;
    }

    if (this.readTypeMapping && this.addrMapping != null) {
      if ("countries".equals(qName)) {
        this.countries = value;
        this.addrMapping.setCountries(Arrays.asList(value.split(",")));
      }
      if ("inputType".equals(qName)) {
        this.inputType = value;
      }
      if ("outputType".equals(qName)) {
        this.outputType = value;
      }
      if ("typeMap".equals(qName)) {
        this.addrMapping.setTypeMapping(this.inputType, this.outputType);
      }
    }
    this.buffer.delete(0, this.buffer.length());
  }

  protected void setTypeMapping(String countries, AddressTypeMapping mapping) {
    for (String country : countries.split(",")) {
      this.addressTypeMappings.put(country, mapping);
    }
  }

  public String getKNA1AddressType(String cmrAddrType, String country) {
    String outputType = "";
    AddressTypeMapping mapping = this.addressTypeMappings.get(country);
    if (mapping == null) {
      // no transformation
      return "";
    }
    if (!StringUtils.isBlank(cmrAddrType)) {
      outputType = mapping.getOutputTypeMapping(cmrAddrType);
    }
    return outputType;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    this.buffer.append(ch, start, length);

  }

  @Override
  public void endDocument() throws SAXException {
  }

  /**
   * Initializes
   * 
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public static synchronized void init() throws ParserConfigurationException, SAXException, IOException {
  }
}
