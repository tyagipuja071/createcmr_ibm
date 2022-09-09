/**
 * 
 */
package com.ibm.cio.cmr.request.model.system;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;

/**
 * Processing model for all functions related to Admin -> Corrections functions
 * 
 * @author 136786PH1
 *
 */
@SuppressWarnings("rawtypes")
public class CorrectionsModel {

  private static Map<String, String> fieldMap = new HashMap<>();
  private long reqId;
  private String cmrIssuingCntry;
  private String cmrNo;
  private Admin admin;
  private Data data;
  private List<Addr> addresses;
  private CmrtCust cust;
  private CmrtCustExt custExt;
  private List<CmrtAddr> custAddresses;

  private String processType;
  private String correctionType;

  public static void main(String[] args) {
    System.out.println("ok");
  }

  static {
    // initialize the field mapping
    Class[] classes = new Class[] { Admin.class, Data.class, Addr.class, CmrtCust.class, CmrtCustExt.class, CmrtAddr.class };
    for (Class<?> clazz : classes) {
      for (Field field : clazz.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers()) && !"id".equals(field.getName()) && !Date.class.equals(field.getType())
            && !Timestamp.class.equals(field.getType())) {
          String name = field.getName().toUpperCase();
          Column col = field.getAnnotation(Column.class);
          if (col != null) {
            name = col.name().toUpperCase();
          }
          fieldMap.putIfAbsent(field.getName(), name);
        }
      }
    }
  }

  public static Map<String, String> getFieldMap() {
    return fieldMap;
  }

  public Admin getAdmin() {
    return admin;
  }

  public void setAdmin(Admin admin) {
    this.admin = admin;
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public List<Addr> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Addr> addresses) {
    this.addresses = addresses;
  }

  public CmrtCust getCust() {
    return cust;
  }

  public void setCust(CmrtCust cust) {
    this.cust = cust;
  }

  public CmrtCustExt getCustExt() {
    return custExt;
  }

  public void setCustExt(CmrtCustExt custExt) {
    this.custExt = custExt;
  }

  public List<CmrtAddr> getCustAddresses() {
    return custAddresses;
  }

  public void setCustAddresses(List<CmrtAddr> custAddresses) {
    this.custAddresses = custAddresses;
  }

  public String getCorrectionType() {
    return correctionType;
  }

  public void setCorrectionType(String correctionType) {
    this.correctionType = correctionType;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getProcessType() {
    return processType;
  }

  public void setProcessType(String processType) {
    this.processType = processType;
  }

}
