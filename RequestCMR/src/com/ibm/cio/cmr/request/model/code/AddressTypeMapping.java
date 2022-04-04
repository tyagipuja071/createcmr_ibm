package com.ibm.cio.cmr.request.model.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressTypeMapping {

  public static final AddressTypeMapping DEFAULT = new AddressTypeMapping();

  private List<String> countries = new ArrayList<>();
  private List<String> typeToBeSkipped = new ArrayList<>();
  private String usePairedSeqNo;

  private Map<String, String> inTypeMapping = new HashMap<String, String>();

  public AddressTypeMapping() {
    // initialize the defaults
    this.inTypeMapping.put("ZS01", "ZS01");
    this.inTypeMapping.put("ZP01", "ZP01");
    this.inTypeMapping.put("ZI01", "ZI01");
    this.inTypeMapping.put("ZD01", "ZD01");
    this.inTypeMapping.put("ZS02", "ZS02");
    this.inTypeMapping.put("EPL", null);
    this.inTypeMapping.put("MAIL", null);
    this.usePairedSeqNo = "N";

  }

  public List<String> getCountries() {
    return countries;
  }

  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  /**
   * Sets the input and target mapping
   * 
   * @param inputType
   * @param outputType
   */
  public void setTypeMapping(String inputType, String outputType) {
    this.inTypeMapping.put(inputType, outputType);
  }

  /**
   * Gets the CreateCMR code of the given input address type
   * 
   * @param inputType
   * @return
   */
  public String getOutputTypeMapping(String inputType) {
    return this.inTypeMapping.get(inputType);
  }

  /**
   * Gets the input address type for the given CreateCMR code
   * 
   * @param outputType
   * @return
   */
  public String getInputTypeMapping(String outputType) {
    String value = null;
    for (String key : this.inTypeMapping.keySet()) {
      value = this.inTypeMapping.get(key);
      if (outputType.equals(value)) {
        return key;
      }
    }
    return null;
  }

  public List<String> getTypeToBeSkipped() {
    return typeToBeSkipped;
  }

  public void setTypeToBeSkipped(List<String> typeToBeSkipped) {
    this.typeToBeSkipped = typeToBeSkipped;
  }

  public String getUsePairedSeqNo() {
    return usePairedSeqNo;
  }

  public void setUsePairedSeqNo(String usePairedSeqNo) {
    this.usePairedSeqNo = usePairedSeqNo;
  }

}
