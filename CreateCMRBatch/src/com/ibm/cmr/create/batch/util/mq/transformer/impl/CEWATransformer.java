/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

/**
 * @author Jeffrey Zamora
 * 
 */
public class CEWATransformer extends MCOTransformer {

  public CEWATransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    super.formatDataLines(handler);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    super.formatAddressLines(handler);
  }
}
