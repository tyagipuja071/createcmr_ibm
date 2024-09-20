package com.ibm.cio.cmr.request.util.geo.impl;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.ui.PageManager;

public class ASEANSunsetP2Handler extends ASEANSunsetHandler {
  private static final Logger LOG = Logger.getLogger(ASEANSunsetP2Handler.class);

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    LOG.debug("Issuing Country: " + reqEntry.getCmrIssuingCntry());
    String processingType = PageManager.getProcessingType(reqEntry.getCmrIssuingCntry(), "U");
    LOG.info("ASEANSunsetP2Handler processing type: " + processingType);

    if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) {
      LOG.info("DR - IERP Logic");
      super.convertFrom(entityManager, source, reqEntry, searchModel);
    } else {
      LOG.info("Phase 1 - MQ Logic");
      super.convertFrom(entityManager, source, reqEntry, searchModel);
    }
  }

}
