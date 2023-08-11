/**
 *
 */
package com.ibm.cio.cmr.request.automation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.LicenseModel;
import com.ibm.cio.cmr.request.model.window.RequestSummaryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * Container for changes on a request for update types
 *
 * @author JeffZAMORA
 *
 */
public class RequestChangeContainer {

  private String country;
  private List<UpdatedDataModel> dataUpdates;
  private List<UpdatedNameAddrModel> addressUpdates;
  private List<LicenseModel> newlyAddedLicenses;
  private long reqId;
  private Admin admin;

  private static final Logger LOG = Logger.getLogger(RequestChangeContainer.class);

  private static final List<String> SKIP_RETRIEVE_VALUE_FIELDS_COUNTRIES = Arrays.asList(SystemLocation.UNITED_KINGDOM, SystemLocation.IRELAND,
      SystemLocation.FRANCE, SystemLocation.GERMANY, SystemLocation.AUSTRIA, SystemLocation.SWITZERLAND, SystemLocation.SPAIN);
  private static final List<String> RETRIEVE_VALUE_FIELDS = Arrays.asList("Buying Group ID", "Global Buying Group ID", "BG LDE Rule",
      "Coverage Type/ID", "GEO Location Code");

  public RequestChangeContainer(EntityManager entityManager, String country, Admin admin, long reqId) throws Exception {
    this.country = country;
    this.reqId = reqId;
    this.admin = admin;
    RequestData requestData = new RequestData(entityManager, reqId);
    getAllChanges(entityManager, requestData);
  }

  public RequestChangeContainer(EntityManager entityManager, String country, Admin admin, RequestData requestData) throws Exception {
    this.country = country;
    this.reqId = admin.getId().getReqId();
    this.admin = admin;
    getAllChanges(entityManager, requestData);
  }

  /**
   * Gets all the changes for the request using the
   * {@link RequestSummaryService}
   *
   * @param entityManager
   * @param requestData
   * @throws Exception
   */
  private void getAllChanges(EntityManager entityManager, RequestData requestData) throws Exception {

    RequestSummaryService summaryService = new RequestSummaryService();
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    RequestSummaryModel summary = summaryService.doProcess(entityManager, null, params);
    Data newData = summary.getData();

    if (requestData != null && requestData.getData() != null) {
      PropertyUtils.copyProperties(newData, requestData.getData());
    }

    this.dataUpdates = new ArrayList<>();
    List<UpdatedDataModel> ibmDataList = summaryService.getUpdatedData(newData, reqId, "IBM");
    if (ibmDataList != null) {
      for (UpdatedDataModel data : ibmDataList) {
        if (!StringUtils.isBlank(data.getNewData()) || !StringUtils.isBlank(data.getOldData())) {
          this.dataUpdates.add(data);
          LOG.debug("Changes for RequestId:-" + reqId + "Filed:-" + data.getDataField() + "Old value:-" + data.getOldData() + "New value:-"
              + data.getNewData());
        }
      }
    }
    List<UpdatedDataModel> customerDataList = summaryService.getUpdatedData(newData, reqId, "C");
    if (customerDataList != null) {
      for (UpdatedDataModel data : customerDataList) {
        if (!StringUtils.isBlank(data.getNewData()) || !StringUtils.isBlank(data.getOldData())) {
          this.dataUpdates.add(data);
          LOG.debug("Changes for RequestId:-" + reqId + "Filed:-" + data.getDataField() + "Old value:-" + data.getOldData() + "New value:-"
              + data.getNewData());
        }
      }
    }

    this.addressUpdates = new ArrayList<>();
    List<UpdatedNameAddrModel> addrChangeList = summaryService.getUpdatedNameAddr(entityManager, reqId);
    if (addrChangeList != null) {
      for (UpdatedNameAddrModel addr : addrChangeList) {
        if (!StringUtils.isBlank(addr.getNewData()) || !StringUtils.isBlank(addr.getOldData())
            || (!StringUtils.isBlank(addr.getSapNumber()) && ("[removed]".equals(addr.getSapNumber()) || "[new]".equals(addr.getSapNumber())))) {
          this.addressUpdates.add(addr);
          LOG.debug("Changes for RequestId:-" + reqId + "Address Seq:-" + addr.getAddrSeq() + "Old value:-" + addr.getOldData() + "New value:-"
              + addr.getNewData());
        }
      }
    }

    this.newlyAddedLicenses = new ArrayList<>();
    List<LicenseModel> licensesChangeList = summaryService.getNewLicenses(entityManager, reqId);
    if (licensesChangeList != null) {
      this.newlyAddedLicenses = licensesChangeList;
    }

  }

  /**
   * Checks if the field specified by the fieldId has been updated
   *
   * @param fieldId
   * @return
   */
  public boolean isDataChanged(String fieldId) {
    for (UpdatedDataModel dataChange : this.dataUpdates) {
      if (fieldId.equals(dataChange.getDataField())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the address type has been updated
   *
   * @param fieldId
   * @return
   */
  public boolean isAddressChanged(String addrType) {
    for (UpdatedNameAddrModel addrChange : this.addressUpdates) {
      if (addrType.equals(addrChange.getAddrTypeCode())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the field specified by the fieldId has been updated on the
   * address type
   *
   * @param fieldId
   * @return
   */
  public boolean isAddressFieldChanged(String addrType, String fieldId) {
    for (UpdatedNameAddrModel addrChange : this.addressUpdates) {
      if (addrType.equals(addrChange.getAddrTypeCode()) && fieldId.equals(addrChange.getDataField())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the change connected with the given fieldId
   *
   * @param fieldId
   * @return
   */
  public UpdatedDataModel getDataChange(String fieldId) {
    for (UpdatedDataModel dataChange : this.dataUpdates) {
      if (fieldId.equals(dataChange.getDataField())) {
        return dataChange;
      }
    }
    return null;
  }

  /**
   * Gets the change connected with the specific fieldId on the address type
   *
   * @param fieldId
   * @return
   */
  public UpdatedNameAddrModel getAddressChange(String addrType, String fieldId) {
    for (UpdatedNameAddrModel addrChange : this.addressUpdates) {
      if (addrType.equals(addrChange.getAddrTypeCode()) && fieldId.equals(addrChange.getDataField())) {
        return addrChange;
      }
    }
    return null;
  }

  /**
   * Gets the change connected with the specific fieldId on the address type
   *
   * @param fieldId
   * @return
   */
  public List<UpdatedNameAddrModel> getAddressChanges(String addrType, String addrSeq) {
    List<UpdatedNameAddrModel> changes = new ArrayList<UpdatedNameAddrModel>();
    for (UpdatedNameAddrModel addrChange : this.addressUpdates) {
      if (addrType.equals(addrChange.getAddrTypeCode()) && addrSeq.equals(addrChange.getAddrSeq())) {
        changes.add(addrChange);
      }
    }
    return changes;
  }

  /**
   * Checks if the legal name has been changed for this request
   *
   * @return true if changed
   */
  public boolean isLegalNameChanged() {
    String newName = this.admin.getMainCustNm1().toUpperCase();
    newName += !StringUtils.isBlank(this.admin.getMainCustNm2()) ? " " + this.admin.getMainCustNm2().toUpperCase() : "";

    String oldName = !StringUtils.isBlank(this.admin.getOldCustNm1()) ? this.admin.getOldCustNm1().toUpperCase() : "";
    oldName += !StringUtils.isBlank(this.admin.getOldCustNm2()) ? " " + this.admin.getOldCustNm2().toUpperCase() : "";

    return !newName.equals(oldName);
  }

  /**
   * Returns true if there are data element changes
   *
   * @return
   */
  public boolean hasDataChanges() {
    if (country != null && SKIP_RETRIEVE_VALUE_FIELDS_COUNTRIES.contains(country)) {
      boolean changes = false;
      if (!this.dataUpdates.isEmpty()) {
        for (UpdatedDataModel field : dataUpdates) {
          if (!RETRIEVE_VALUE_FIELDS.contains(field.getDataField())) {
            changes = true;
            break;
          }
        }
        return changes;
      } else {
        return false;
      }
    } else {
      return !this.dataUpdates.isEmpty();
    }
  }

  /**
   * Returns true if there are address changes
   *
   * @return
   */
  public boolean hasAddressChanges() {
    return !this.addressUpdates.isEmpty();
  }

  public boolean hasNewLicenses() {
    return !this.newlyAddedLicenses.isEmpty();
  }

  public String getCountry() {
    return country;
  }

  public long getReqId() {
    return reqId;
  }

  public List<UpdatedDataModel> getDataUpdates() {
    return dataUpdates;
  }

  public List<UpdatedNameAddrModel> getAddressUpdates() {
    return addressUpdates;
  }

}
