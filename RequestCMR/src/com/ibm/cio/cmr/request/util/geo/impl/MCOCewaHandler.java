/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Handler for MCO Central, East, West Africa
 * 
 * @author Eduard Bernardo
 * 
 */
public class MCOCewaHandler extends MCOHandler {

  protected static final Logger LOG = Logger.getLogger(MCOCewaHandler.class);

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZS01", "ZP01", "ZI01", "ZD01", "ZS02");
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZD01", "ZI01");
  }

}
