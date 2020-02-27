/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * Base {@link MessageTransformer} class that handles the following countries:
 * <ul>
 * <li>744 - India</li>
 * <li>615 - Bangladesh</li>
 * <li>790 - Nepal</li>
 * <li>652 - Sri Lanka</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */

public abstract class ISATransformer extends APTransformer {

  /**
   * @param cmrIssuingCntry
   * @throws Exception
   */
  public ISATransformer(String cmrIssuingCntry) throws Exception {
    super(cmrIssuingCntry);

  }

  @Override
  protected void handleDataDefaults(MQMessageHandler handler) {
    super.handleDataDefaults(handler);
    if ("NA".equalsIgnoreCase(handler.cmrData.getVat())) {
      handler.messageHash.put("CtryText", "");
    } else {
      handler.messageHash.put("CtryText", handler.cmrData.getVat());
    }

    String isu = handler.cmrData.getIsuCd();

    if ("32".equalsIgnoreCase(isu) || "34".equalsIgnoreCase(isu) || "21".equalsIgnoreCase(isu)) {
      handler.messageHash.put("MrktRespCode", "3");
    } else {
      handler.messageHash.put("MrktRespCode", "2");
    }

    if ("0".equalsIgnoreCase(handler.cmrData.getClientTier())) {
      handler.messageHash.put("GB_SegCode", "");
    }
    String abbloc = "";
    abbloc = handler.cmrData.getAbbrevLocn();
    if (StringUtils.isEmpty(handler.cmrData.getAbbrevLocn())) {
      abbloc = "";
    }
    handler.messageHash.put("AbbrLoc", "   " + abbloc);

  }

  @Override
  protected void handleAddressDefaults(MQMessageHandler handler) {
    super.handleAddressDefaults(handler);
    handler.messageHash.put("AddrUseCA", computeAddressUse(handler));
    Addr addrData = handler.addrData;
    String line3 = "";

    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line3 += addrData.getAddrTxt();
    }

    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line4 += addrData.getAddrTxt2();
    }

    String line5 = "";
    if (!StringUtils.isBlank(addrData.getDept())) {
      line5 += addrData.getDept();
    }
    
    handler.messageHash.put("AddrLine3", line3);
    handler.messageHash.put("AddrLine4", line4);
    handler.messageHash.put("AddrLine5", line5);
    
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "AA";
  }

  @Override
  public String[] getAddressOrder() {
    return new String[] { "ZS01", "ZP01", "ZI01", "ZH01" };
  }

  @Override
  protected String getMainAddressUseCA() {
    return "1234567ABCDEFGH";
  }
}
