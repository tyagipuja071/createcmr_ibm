package com.ibm.cio.cmr.request.model.login;

public class LogInUserModel {

  private String username;

  private String password;

  private long r;

  private String c;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public long getR() {
    return r;
  }

  public void setR(long r) {
    this.r = r;
  }

  public String getC() {
    return c;
  }

  public void setC(String c) {
    this.c = c;
  }

}
