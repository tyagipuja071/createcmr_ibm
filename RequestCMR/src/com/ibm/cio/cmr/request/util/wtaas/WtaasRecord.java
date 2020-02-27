/**
 * 
 */
package com.ibm.cio.cmr.request.util.wtaas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys.Address;
import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys.Data;
import com.ibm.cmr.services.client.wtaas.WtaasQueryResponse;

/**
 * @author JeffZAMORA
 * 
 */
public class WtaasRecord {

  private String cmrNo;
  private String country;
  private Map<WtaasQueryKeys.Data, String> values = new HashMap<WtaasQueryKeys.Data, String>();

  private List<WtaasAddress> addresses = new ArrayList<WtaasAddress>();

  public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
    WtaasRecord record = dummy();
    ObjectMapper mapper = new ObjectMapper();
    System.out.println(mapper.writeValueAsString(record));
    System.out.println(mapper.writeValueAsString(record.uniqueAddresses()));
  }

  public static WtaasRecord dummy() {
    WtaasRecord record = new WtaasRecord();
    record.setCmrNo("010380");
    record.setCountry("818");

    for (Data a : Data.values()) {
      record.values.put(a, a.toString());
    }

    WtaasAddress address = null;
    for (String no : Arrays.asList("1", "2", "3", "4")) {
      address = new WtaasAddress();
      address.setAddressNo(no.equals("3") ? "2" : "1");
      address.setAddressUse(no);
      for (Address a : Address.values()) {
        address.values.put(a, a.toString());
        address.values.put(Address.AddressNo, no.equals("3") ? "2" : "1");
        address.values.put(Address.AddressUse, no);
      }
      record.addresses.add(address);
    }

    return record;
  }

  /**
   * Creates a represenation of a WTAAS record based on
   * {@link WtaasQueryResponse} from the query service
   * 
   * @param queryResponse
   * @return
   */
  @SuppressWarnings("unchecked")
  public static WtaasRecord createFrom(WtaasQueryResponse queryResponse) {
    Map<String, Object> data = queryResponse.getData();
    if (data == null) {
      return null;
    }

    // parse the data elements
    WtaasRecord record = new WtaasRecord();
    String value = null;
    for (String key : data.keySet()) {
      if (!"addresses".equals(key)) {
        value = (String) data.get(key);
        record.values.put(Data.valueOf(key), value);
      }
    }

    // now parse the addresses
    List<Map<String, Object>> addresses = (List<Map<String, Object>>) data.get("addresses");
    if (addresses == null) {
      return record;
    }

    WtaasAddress wtaasAddress = null;
    for (Map<String, Object> address : addresses) {
      wtaasAddress = new WtaasAddress();
      for (String key : address.keySet()) {
        value = (String) address.get(key);
        wtaasAddress.values.put(Address.valueOf(key), value);
      }
      wtaasAddress.setAddressNo(wtaasAddress.get(Address.AddressNo));
      wtaasAddress.setAddressUse(wtaasAddress.get(Address.AddressUse));
      record.addresses.add(wtaasAddress);
    }

    return record;
  }

  /**
   * Gets the value of the data key stored on the record
   * 
   * @param key
   * @return
   */
  public String get(WtaasQueryKeys.Data key) {
    return this.values.get(key);
  }

  /**
   * Gets the list of raw addresses from the {@link WtaasQueryResponse} without
   * considering duplicate AddressNo
   * 
   * @return
   */
  public List<WtaasAddress> rawAddresses() {
    return this.addresses;
  }

  /**
   * Gets the list of unique addresses that are actually stored in WTAAS based
   * on the {@link WtaasQueryResponse}. The AddressUse values of each unique
   * records have also been computed to compound the values as necessary
   * 
   * @return
   */
  public List<WtaasAddress> uniqueAddresses() {
    List<WtaasAddress> uniqueAddresses = new ArrayList<WtaasAddress>();

    // get the unique address nos. these represent actual address records
    Set<String> addressNos = new HashSet<>();
    for (WtaasAddress address : this.addresses) {
      if (!StringUtils.isEmpty(address.getAddressNo())) {
        addressNos.add(address.getAddressNo());
      }
    }

    // using the unique address record, build the address use and list
    WtaasAddress copy = null;
    boolean assigned = false;
    String addressUse = null;

    for (String addressNo : addressNos) {
      copy = new WtaasAddress();
      assigned = false;
      addressUse = "";

      for (WtaasAddress address : this.addresses) {
        if (addressNo.equals(address.getAddressNo())) {
          if (!assigned) {
            copy.values.putAll(address.values);
            assigned = true;
          }

          if (!addressUse.contains(address.getAddressUse())) {
            addressUse += address.getAddressUse();
          }
        }
      }

      copy.setAddressNo(addressNo);
      copy.setAddressUse(addressUse);
      copy.values.put(Address.AddressUse, addressUse);

      uniqueAddresses.add(copy);
    }
    return uniqueAddresses;
  }

  public boolean hasAddresses() {
    return !this.addresses.isEmpty();
  }

  public String getCmrNo() {
    return cmrNo;
  }

  protected void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getCountry() {
    return country;
  }

  protected void setCountry(String country) {
    this.country = country;
  }

  public Map<WtaasQueryKeys.Data, String> getValues() {
    return values;
  }

  protected void setValues(Map<WtaasQueryKeys.Data, String> values) {
    this.values = values;
  }

  public List<WtaasAddress> getAddresses() {
    return addresses;
  }

  protected void setAddresses(List<WtaasAddress> addresses) {
    this.addresses = addresses;
  }
}
