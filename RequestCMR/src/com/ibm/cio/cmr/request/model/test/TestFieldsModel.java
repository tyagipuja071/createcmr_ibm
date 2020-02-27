/**
 * 
 */
package com.ibm.cio.cmr.request.model.test;

/**
 * @author Jeffrey Zamora
 * 
 */
public class TestFieldsModel {

  private String mandatory;
  private String field1;
  private String field2;
  private String singleCheckbox;
  private int number;
  private String bluePagesId;
  private String bluePagesName;
  private String dropdown;
  private String password;

  private String[] checkboxes;
  private String[] radios;

  public String getMandatory() {
    return mandatory;
  }

  public void setMandatory(String mandatory) {
    this.mandatory = mandatory;
  }

  public String getField1() {
    return field1;
  }

  public void setField1(String field1) {
    this.field1 = field1;
  }

  public String getField2() {
    return field2;
  }

  public void setField2(String field2) {
    this.field2 = field2;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public String getDropdown() {
    return dropdown;
  }

  public void setDropdown(String dropdown) {
    this.dropdown = dropdown;
  }

  public String[] getCheckboxes() {
    return checkboxes;
  }

  public void setCheckboxes(String[] checkboxes) {
    this.checkboxes = checkboxes;
  }

  public String[] getRadios() {
    return radios;
  }

  public void setRadios(String[] radios) {
    this.radios = radios;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getBluePagesId() {
    return bluePagesId;
  }

  public void setBluePagesId(String bluePagesId) {
    this.bluePagesId = bluePagesId;
  }

  public String getBluePagesName() {
    return bluePagesName;
  }

  public void setBluePagesName(String bluePagesName) {
    this.bluePagesName = bluePagesName;
  }

  public String getSingleCheckbox() {
    return singleCheckbox;
  }

  public void setSingleCheckbox(String singleCheckbox) {
    this.singleCheckbox = singleCheckbox;
  }

}
