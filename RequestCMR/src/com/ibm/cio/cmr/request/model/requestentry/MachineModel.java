package com.ibm.cio.cmr.request.model.requestentry;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * Model for Machine Type and Serial Number
 * 
 * @author Rangoli Saxena
 * 
 */
public class MachineModel extends BaseModel {

  private static final long serialVersionUID = 1L;

  private long reqId;
  private String addrType;
  private String addrSeq;
  private String machineSerialNo;
  private String machineTyp;
  private String currentIndc;

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getAddrType() {
    return addrType;
  }

  public void setAddrType(String addrType) {
    this.addrType = addrType;
  }

  public String getAddrSeq() {
    return addrSeq;
  }

  public void setAddrSeq(String addrSeq) {
    this.addrSeq = addrSeq;
  }

  public String getMachineSerialNo() {
    return machineSerialNo;
  }

  public void setMachineSerialNo(String machineSerialNo) {
    this.machineSerialNo = machineSerialNo;
  }

  public String getMachineTyp() {
    return machineTyp;
  }

  public void setMachineTyp(String machineTyp) {
    this.machineTyp = machineTyp;
  }

  @Override
  public boolean allKeysAssigned() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getRecordDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    // TODO Auto-generated method stub
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    // TODO Auto-generated method stub
  }

  public String getCurrentIndc() {
    return currentIndc;
  }

  public void setCurrentIndc(String currentIndc) {
    this.currentIndc = currentIndc;
  }

}