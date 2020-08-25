/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.ui.PageManager;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MCOFstHandler extends MCOHandler {

  protected static final Logger LOG = Logger.getLogger(MCOFstHandler.class);

  @Override
  protected void importOtherSOFAddresses(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted) {
    FindCMRRecordModel record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Mailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
    if (record != null) {
      converted.add(record);
    }
    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = "Installing";
    String seqNoFromSOF = null;
    for (FindCMRRecordModel record : records) {
      if (!"ZS01".equals(record.getCmrAddrTypeCode())) {
        return;
      }
      seqNoFromSOF = this.currentImportValues.get(addrKey + "AddressNumber");
      if (!StringUtils.isEmpty(seqNoFromSOF)) {
        LOG.trace("Assigning SOF Sequence " + seqNoFromSOF + " to " + addrKey);
        record.setCmrAddrSeq(seqNoFromSOF);
      }

    }
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);

    String country = currentRecord.getCmrIssuedBy();
    String processingType = PageManager.getProcessingType(country, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
          && "ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
        String seq = address.getId().getAddrSeq();
        seq = StringUtils.leftPad(seq, 5, '0');
        address.getId().setAddrSeq(seq);
      }
    }
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
