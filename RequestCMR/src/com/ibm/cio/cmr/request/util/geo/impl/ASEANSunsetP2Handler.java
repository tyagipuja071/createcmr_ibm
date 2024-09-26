package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
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
      convertFromIerp(entityManager, source, reqEntry, searchModel);
    } else {
      LOG.info("Phase 1 - MQ Logic");
      super.convertFrom(entityManager, source, reqEntry, searchModel);
    }
  }

  private void convertFromIerp(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> converted = new ArrayList<>();
    List<FindCMRRecordModel> records = source.getItems();

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      for (FindCMRRecordModel record : records) {
        if ("ZS01".equals(record.getCmrAddrTypeCode())) {
          record.setCmrAddrSeq(GRP1_SOLD_TO_FIXED_SEQ);
          converted.add(record);
        }
      }
      source.setItems(converted);
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqEntry.getReqType())) {
      for (FindCMRRecordModel record : records) {
        converted.add(record);
      }
      source.setItems(converted);
    }
  }

  @Override
  public void doAfterConvert(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel,
      List<FindCMRRecordModel> converted) {
    // override it so that the parent logic will not be called
  }

  @Override
  protected void handleRDcRecordValues(FindCMRRecordModel record) {
    // override it so that the parent logic will not be called
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    if (currentRecord != null) {
      address.setCustNm1(currentRecord.getCmrName1Plain());
      address.setCustNm2(currentRecord.getCmrName2Plain());
      address.setAddrTxt(currentRecord.getCmrName3());
      address.setAddrTxt2(currentRecord.getCmrStreetAddress());
      address.setDept(currentRecord.getCmrName4());
      address.setCity1(currentRecord.getCmrCity());
      address.setStateProv(currentRecord.getCmrState());
    }
  }
}
