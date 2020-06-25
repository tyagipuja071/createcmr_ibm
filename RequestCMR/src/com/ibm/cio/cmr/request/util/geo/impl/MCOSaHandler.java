/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;

/**
 * Handler for MCO South Africa
 * 
 * @author Eduard Bernardo
 * 
 */
public class MCOSaHandler extends MCOHandler {

  protected static final Logger LOG = Logger.getLogger(MCOSaHandler.class);

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    String line1 = getCurrentValue(addressKey, "Address1");
    String line2 = getCurrentValue(addressKey, "Address2");
    String line3 = getCurrentValue(addressKey, "Address3");
    String line4 = getCurrentValue(addressKey, "Address4");
    String line5 = getCurrentValue(addressKey, "Address5");
    String line6 = getCurrentValue(addressKey, "Address6");

    if (StringUtils.isEmpty(line6) || "-/X".equalsIgnoreCase(line6)) {
      // for old format, use existing parser
      handleSOFAddressImportOLD(entityManager, cmrIssuingCntry, address, addressKey);
      return;
    }
    String countryCd = getCountryCode(entityManager, line6);

    boolean crossBorder = !StringUtils.isEmpty(countryCd);
    if (crossBorder) {
      // Cross-border - ZA
      // line2 = Phone + Attention Person (Phone for Shipping & EPL only)
      // line3 = Street + PO BOX
      // line4 = City
      // line5 = Postal Code
      // line6 = State (Country)

      address.setCmrName1Plain(line1);

      handlePhoneAndAttn(line2, cmrIssuingCntry, address, addressKey);

      handleStreetContAndPoBox(line3, cmrIssuingCntry, address, addressKey);
      address.setCmrStreetAddress(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);

      handleCityAndPostCode(line4, cmrIssuingCntry, address, addressKey);

      if (!StringUtils.isEmpty(line5)) {
        if (StringUtils.isEmpty(address.getCmrPostalCode()) && !StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrPostalCode(line5);
        } else if (!StringUtils.isEmpty(address.getCmrPostalCode()) && StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrCity(line5);
        }

      }
      address.setCmrCountryLanded(countryCd);

    } else {
      // Domestic - ZA
      // line2 = Phone + Attention Person (Phone for Shipping & EPL only)
      // line3 = Street
      // line4 = Street Con't + PO BOX
      // line5 = City
      // line6 = Postal Code

      address.setCmrName1Plain(line1);

      handlePhoneAndAttn(line2, cmrIssuingCntry, address, addressKey);

      address.setCmrStreetAddress(line3);

      handleStreetContAndPoBox(line4, cmrIssuingCntry, address, addressKey);

      handleCityAndPostCode(line5, cmrIssuingCntry, address, addressKey);

      if (!StringUtils.isEmpty(line6)) {
        if (StringUtils.isEmpty(address.getCmrPostalCode()) && !StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrPostalCode(line6);
        } else if (!StringUtils.isEmpty(address.getCmrPostalCode()) && StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrCity(line6);
        }

      }

    }

    if (StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
      address.setCmrStreetAddress(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);
    }

    if (StringUtils.isEmpty(address.getCmrCity()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
      address.setCmrCity(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);
    }

    if (!StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddress())
        && isStreet(address.getCmrStreetAddressCont()) && !isStreet(address.getCmrStreetAddress())) {
      // interchange street and street con't based on data
      String cont = address.getCmrStreetAddressCont();
      address.setCmrStreetAddressCont(address.getCmrStreetAddress());
      address.setCmrStreetAddress(cont);
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Attn: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("State: " + address.getCmrState());
    LOG.trace("Country: " + address.getCmrCountryLanded());

  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZP01", "ZS01", "ZD01", "ZI01", "ZS02");
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZD01", "ZI01");
  }
}
