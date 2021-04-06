package com.ibm.cio.cmr.request.service.code;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
  private String typeToBeSkipped = null;
  private String usePairedSeqNo = null;
  private AddressTypeMapping addrMapping;
  private boolean readTypeMapping;
  private String inputType;
  private String outputType;
  private Map<String, AddressTypeMapping> addressTypeMappings = new HashMap<>();
  private Map<String, String> typesToBeSkipped = new HashMap<>();
  private Map<String, String> usePairedSeqNoMap = new HashMap<>();

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
      setAddrTypeMapping(this.countries, this.addrMapping, this.typeToBeSkipped, this.usePairedSeqNo);
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
      if ("typeToBeSkipped".equals(qName)) {
        this.typeToBeSkipped = value;
        this.addrMapping.setTypeToBeSkipped(Arrays.asList(value.split(",")));
      }
      if ("usePairedSeqNo".equals(qName)) {
        this.usePairedSeqNo = value;
        this.addrMapping.setUsePairedSeqNo(value);
      }
    }
    this.buffer.delete(0, this.buffer.length());
  }

  protected void setAddrTypeMapping(String countries, AddressTypeMapping mapping, String addrTypesToBeSkipped, String ifUsePairedSeq) {
    for (String country : countries.split(",")) {
      this.addressTypeMappings.put(country, mapping);
      this.typesToBeSkipped.put(country, addrTypesToBeSkipped);
      this.usePairedSeqNoMap.put(country, ifUsePairedSeq == null ? "N" : ifUsePairedSeq);
    }
  }

  public String getKNA1AddressType(String cmrAddrType, String country) {
    String outputType = "";
    AddressTypeMapping mapping = getAddressTypeMapping(country);
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
   * Gets the {@link AddressTypeMapping} registered for the given country. Falls
   * back to ZS01 and ZP01 if no mapping is registered for the country
   * 
   * @param country
   * @return
   */
  public AddressTypeMapping getAddressTypeMapping(String country) {
    if (this.addressTypeMappings.containsKey(country)) {
      return this.addressTypeMappings.get(country);
    }
    return AddressTypeMapping.DEFAULT;
  }

  public String getAddressTypeToBeSkipped(String country) {
    if (this.typesToBeSkipped.containsKey(country)) {
      return this.typesToBeSkipped.get(country);
    }
    return null;
  }

  public String getIfUsePairedSeqNo(String country) {
    if (this.usePairedSeqNoMap.containsKey(country)) {
      return this.usePairedSeqNoMap.get(country);
    }
    return AddressTypeMapping.DEFAULT.getUsePairedSeqNo();
  }
}
