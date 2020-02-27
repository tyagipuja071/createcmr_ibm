/**
 * 
 */
package com.ibm.cio.cmr.request.util;

/**
 * Represents an IBM person
 * 
 * @author Jeffrey Zamora
 * 
 */
public class Person {

  private String id;
  private String employeeId;
  private String email;
  private String name;
  private String notesEmail;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNotesEmail() {
    return notesEmail;
  }

  public void setNotesEmail(String notesEmail) {
    this.notesEmail = notesEmail;
  }
}
