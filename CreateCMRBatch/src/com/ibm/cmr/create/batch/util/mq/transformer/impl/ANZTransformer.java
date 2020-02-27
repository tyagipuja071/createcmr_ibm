/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * Base {@link MessageTransformer} class that handles the following countries:
 * <ul>
 * <li>616 - Australia</li>
 * <li>796 - New Zealand</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class ANZTransformer extends APTransformer {

  public ANZTransformer(String cmrIssuingCntry) throws Exception {
    super(cmrIssuingCntry);
  }

  @Override
  protected void handleDataDefaults(MQMessageHandler handler) {
    super.handleDataDefaults(handler);

    handler.messageHash.put("SellBrnchOff", "000");
    handler.messageHash.put("SellDept", "0000");
    handler.messageHash.put("InstBrnchOff", "000");
    handler.messageHash.put("InstDept", "");
    
    String isu = handler.cmrData.getIsuCd();
    if ("32".equalsIgnoreCase(isu) || "34".equalsIgnoreCase(isu) || "21".equalsIgnoreCase(isu)) {
      handler.messageHash.put("MrktRespCode", "3");
    } else {
      handler.messageHash.put("MrktRespCode", "2");
    }
    
  }

  @Override
  protected void handleAddressDefaults(MQMessageHandler handler) {
    super.handleAddressDefaults(handler);
    
    /*Addr addrData = handler.addrData;

    String line3 = "";
    if (!StringUtils.isBlank(addrData.getDept()) && StringUtils.isBlank(addrData.getAddrTxt2())) {
      line3 += addrData.getDept();
    }
    if (StringUtils.isBlank(addrData.getDept()) && !StringUtils.isBlank(addrData.getAddrTxt2())) {
      line3 += addrData.getAddrTxt2();
    }
    if (!StringUtils.isBlank(addrData.getDept()) && !StringUtils.isBlank(addrData.getAddrTxt2())) {
      line3 += addrData.getDept();
    }

    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 += addrData.getAddrTxt();
    }

    handler.messageHash.put("AddrLine3", line3);
    handler.messageHash.put("AddrLine4", line4);*/
  }
}
