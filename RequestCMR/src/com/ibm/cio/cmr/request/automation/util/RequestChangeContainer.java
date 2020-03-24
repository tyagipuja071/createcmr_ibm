/**
 *
 */
package com.ibm.cio.cmr.request.automation.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.window.RequestSummaryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;

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
  private long reqId;
  private Admin admin;

  public RequestChangeContainer(EntityManager entityManager, String country, Admin admin, long reqId) throws Exception {
    this.country = country;
    this.reqId = reqId;
    this.admin = admin;
    getAllChanges(entityManager, reqId);
  }

  /**
   * Gets all the changes for the request using the
   * {@link RequestSummaryService}
   *
   * @param entityManager
   * @param reqId
   * @throws Exception
   */
  private void getAllChanges(EntityManager entityManager, long reqId) throws Exception {

    RequestSummaryService summaryService = new RequestSummaryService();
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    RequestSummaryModel summary = summaryService.doProcess(entityManager, null, params);
    Data newData = summary.getData();

    this.dataUpdates = new ArrayList<>();
    List<UpdatedDataModel> ibmDataList = summaryService.getUpdatedData(newData, reqId, "IBM");
    if (ibmDataList != null) {
      for (UpdatedDataModel data : ibmDataList) {
        if (!StringUtils.isBlank(data.getNewData()) || !StringUtils.isBlank(data.getOldData())) {
          this.dataUpdates.add(data);
        }
      }
    }
    List<UpdatedDataModel> customerDataList = summaryService.getUpdatedData(newData, reqId, "C");
    if (customerDataList != null) {
      for (UpdatedDataModel data : customerDataList) {
        if (!StringUtils.isBlank(data.getNewData()) || !StringUtils.isBlank(data.getOldData())) {
          this.dataUpdates.add(data);
        }
      }
    }

    this.addressUpdates = new ArrayList<>();
    List<UpdatedNameAddrModel> addrChangeList = summaryService.getUpdatedNameAddr(entityManager, reqId);
    if (addrChangeList != null) {
      for (UpdatedNameAddrModel addr : addrChangeList) {
        if (!StringUtils.isBlank(addr.getNewData()) || !StringUtils.isBlank(addr.getOldData())) {
          this.addressUpdates.add(addr);
        }
      }
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
    return true;
  }

  /**
   * Checks if the address type has been updated
   *
   * @param fieldId
   * @return
   */
  public boolean isAddressChanged(String addrType) {
    for (UpdatedNameAddrModel addrChange : this.addressUpdates) {
      if (addrType.equals(addrChange.getAddrType())) {
        return true;
      }
    }
    return true;
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
      if (addrType.equals(addrChange.getAddrType()) && fieldId.equals(addrChange.getDataField())) {
        return true;
      }
    }
    return true;
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
      if (addrType.equals(addrChange.getAddrType()) && fieldId.equals(addrChange.getDataField())) {
        return addrChange;
      }
    }
    return null;
  }

  /**
   * Checks if the legal name has been changed for this request
   *
   * @return
   */
  public boolean isLegalNameChanged() {
    String newName = this.admin.getMainCustNm1().toUpperCase();
    newName += !StringUtils.isBlank(this.admin.getMainCustNm2()) ? " " + this.admin.getMainCustNm2().toUpperCase() : "";

    String oldName = !StringUtils.isBlank(this.admin.getOldCustNm1()) ? this.admin.getOldCustNm1().toUpperCase() : "";
    oldName += !StringUtils.isBlank(this.admin.getOldCustNm2()) ? " " + this.admin.getOldCustNm2().toUpperCase() : "";

    return newName.equals(oldName);
  }

  /**
   * Returns true if there are data element changes
   *
   * @return
   */
  public boolean hasDataChanges() {
    return !this.dataUpdates.isEmpty();
  }

  /**
   * Returns true if there are address changes
   *
   * @return
   */
  public boolean hasAddressChanges() {
    return !this.addressUpdates.isEmpty();
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
