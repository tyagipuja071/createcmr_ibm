package com.ibm.cmr.create.batch.model;

public class SOFResponseCUDQMessage extends RDCLegacyMQMessage {

  private String updatedby;
  private String Country;
  private String UniqueNumber;
  private String XML_DocumentNumber;
  private String CustomerNo;
  private String Status;
  private String Message;
  private String AddressNumber;
  private String addressNo;

  public String getupdatedby() {
    return updatedby;
  }

  public void setupdatedby(String updatedby) {
    this.updatedby = updatedby;
  }

  public String getCountry() {
    return Country;
  }

  public void setCountry(String Country) {
    this.Country = Country;
  }

  public String getUniqueNumber() {
    return UniqueNumber;
  }

  public void setUniqueNumber(String UniqueNumber) {
    this.UniqueNumber = UniqueNumber;
  }

  public String getXML_DocumentNumber() {
    return XML_DocumentNumber;
  }

  public void setXML_DocumentNumber(String XML_DocumentNumber) {
    this.XML_DocumentNumber = XML_DocumentNumber;
  }

  public String getCustomerNo() {
    return CustomerNo;
  }

  public void setCustomerNo(String CustomerNo) {
    this.CustomerNo = CustomerNo;
  }

  public String getStatus() {
    return Status;
  }

  public void setStatus(String status) {
    Status = status;
  }

  public String getMessage() {
    return Message;
  }

  public void setMessage(String message) {
    Message = message;
  }

  public String getUpdatedby() {
    return updatedby;
  }

  public void setUpdatedby(String updatedby) {
    this.updatedby = updatedby;
  }

  public String getAddressNumber() {
    return AddressNumber;
  }

  public void setAddressNumber(String AddressNumber) {
    this.AddressNumber = AddressNumber;
  }

  public String getAddressNo() {
    return addressNo;
  }

  public void setAddressNo(String addressNo) {
    this.addressNo = addressNo;
  }
}
