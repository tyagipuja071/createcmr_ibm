package com.ibm.cio.cmr.request.util.legacy;

import java.util.ArrayList;
import java.util.List;

public class CloningRDCConfiguration {

  private List<String> countriesForKnb1Create = new ArrayList<String>();
  private List<String> countriesForKnvvCreate = new ArrayList<String>();
  private List<String> countriesForKnviCreate = new ArrayList<String>();
  private List<String> countriesForKnvkCreate = new ArrayList<String>();
  private List<String> countriesForKnvpCreate = new ArrayList<String>();
  private List<String> countriesForKnexCreate = new ArrayList<String>();
  private List<String> countriesForAddlCtryDataCreate = new ArrayList<String>();
  private List<String> countriesForKunnrExtCreate = new ArrayList<String>();
  private List<String> countriesForKnbkCreate = new ArrayList<String>();
  private List<String> countriesForKnvaCreate = new ArrayList<String>();
  private List<String> countriesForKnvlCreate = new ArrayList<String>();
  private List<String> countriesForSizeInfoCreate = new ArrayList<String>();
  private List<String> countriesForSadrCreate = new ArrayList<String>();
  private String targetMandt;
  private String katr10;
  private boolean processKnb1;
  private boolean processKnvv;
  private boolean processKnvi;
  private boolean processKnvk;
  private boolean processKnvp;
  private boolean processKnex;
  private boolean processAddlCtryData;
  private boolean processKunnrExt;
  private boolean processKnbk;
  private boolean processKnva;
  private boolean processKnvl;
  private boolean processSizeInfo;
  private boolean processSadr;

  public List<String> getCountriesForKnb1Create() {
    return countriesForKnb1Create;
  }

  public void setCountriesForKnb1Create(List<String> countriesForKnb1Create) {
    this.countriesForKnb1Create = countriesForKnb1Create;
  }

  public List<String> getCountriesForKnvvCreate() {
    return countriesForKnvvCreate;
  }

  public void setCountriesForKnvvCreate(List<String> countriesForKnvvCreate) {
    this.countriesForKnvvCreate = countriesForKnvvCreate;
  }

  public List<String> getCountriesForKnviCreate() {
    return countriesForKnviCreate;
  }

  public void setCountriesForKnviCreate(List<String> countriesForKnviCreate) {
    this.countriesForKnviCreate = countriesForKnviCreate;
  }

  public List<String> getCountriesForKnvkCreate() {
    return countriesForKnvkCreate;
  }

  public void setCountriesForKnvkCreate(List<String> countriesForKnvkCreate) {
    this.countriesForKnvkCreate = countriesForKnvkCreate;
  }

  public List<String> getCountriesForKnvpCreate() {
    return countriesForKnvpCreate;
  }

  public void setCountriesForKnvpCreate(List<String> countriesForKnvpCreate) {
    this.countriesForKnvpCreate = countriesForKnvpCreate;
  }

  public List<String> getCountriesForKnexCreate() {
    return countriesForKnexCreate;
  }

  public void setCountriesForKnexCreate(List<String> countriesForKnexCreate) {
    this.countriesForKnexCreate = countriesForKnexCreate;
  }

  public List<String> getCountriesForAddlCtryDataCreate() {
    return countriesForAddlCtryDataCreate;
  }

  public void setCountriesForAddlCtryDataCreate(List<String> countriesForAddlCtryDataCreate) {
    this.countriesForAddlCtryDataCreate = countriesForAddlCtryDataCreate;
  }

  public List<String> getCountriesForKunnrExtCreate() {
    return countriesForKunnrExtCreate;
  }

  public void setCountriesForKunnrExtCreate(List<String> countriesForKunnrExtCreate) {
    this.countriesForKunnrExtCreate = countriesForKunnrExtCreate;
  }

  public List<String> getCountriesForKnbkCreate() {
    return countriesForKnbkCreate;
  }

  public void setCountriesForKnbkCreate(List<String> countriesForKnbkCreate) {
    this.countriesForKnbkCreate = countriesForKnbkCreate;
  }

  public List<String> getCountriesForKnvaCreate() {
    return countriesForKnvaCreate;
  }

  public void setCountriesForKnvaCreate(List<String> countriesForKnvaCreate) {
    this.countriesForKnvaCreate = countriesForKnvaCreate;
  }

  public List<String> getCountriesForKnvlCreate() {
    return countriesForKnvlCreate;
  }

  public void setCountriesForKnvlCreate(List<String> countriesForKnvlCreate) {
    this.countriesForKnvlCreate = countriesForKnvlCreate;
  }

  public List<String> getCountriesForSizeInfoCreate() {
    return countriesForSizeInfoCreate;
  }

  public void setCountriesForSizeInfoCreate(List<String> countriesForSizeInfoCreate) {
    this.countriesForSizeInfoCreate = countriesForSizeInfoCreate;
  }

  public boolean isProcessKnb1() {
    return processKnb1;
  }

  public void setProcessKnb1(boolean processKnb1) {
    this.processKnb1 = processKnb1;
  }

  public boolean isProcessKnvv() {
    return processKnvv;
  }

  public void setProcessKnvv(boolean processKnvv) {
    this.processKnvv = processKnvv;
  }

  public boolean isProcessKnvi() {
    return processKnvi;
  }

  public void setProcessKnvi(boolean processKnvi) {
    this.processKnvi = processKnvi;
  }

  public boolean isProcessKnvk() {
    return processKnvk;
  }

  public void setProcessKnvk(boolean processKnvk) {
    this.processKnvk = processKnvk;
  }

  public boolean isProcessKnvp() {
    return processKnvp;
  }

  public void setProcessKnvp(boolean processKnvp) {
    this.processKnvp = processKnvp;
  }

  public boolean isProcessKnex() {
    return processKnex;
  }

  public void setProcessKnex(boolean processKnex) {
    this.processKnex = processKnex;
  }

  public boolean isProcessAddlCtryData() {
    return processAddlCtryData;
  }

  public void setProcessAddlCtryData(boolean processAddlCtryData) {
    this.processAddlCtryData = processAddlCtryData;
  }

  public boolean isProcessKunnrExt() {
    return processKunnrExt;
  }

  public void setProcessKunnrExt(boolean processKunnrExt) {
    this.processKunnrExt = processKunnrExt;
  }

  public boolean isProcessKnbk() {
    return processKnbk;
  }

  public void setProcessKnbk(boolean processKnbk) {
    this.processKnbk = processKnbk;
  }

  public boolean isProcessKnva() {
    return processKnva;
  }

  public void setProcessKnva(boolean processKnva) {
    this.processKnva = processKnva;
  }

  public boolean isProcessKnvl() {
    return processKnvl;
  }

  public void setProcessKnvl(boolean processKnvl) {
    this.processKnvl = processKnvl;
  }

  public boolean isProcessSizeInfo() {
    return processSizeInfo;
  }

  public void setProcessSizeInfo(boolean processSizeInfo) {
    this.processSizeInfo = processSizeInfo;
  }

  public List<String> getCountriesForSadrCreate() {
    return countriesForSadrCreate;
  }

  public void setCountriesForSadrCreate(List<String> countriesForSadrCreate) {
    this.countriesForSadrCreate = countriesForSadrCreate;
  }

  public boolean isProcessSadr() {
    return processSadr;
  }

  public void setProcessSadr(boolean processSadr) {
    this.processSadr = processSadr;
  }

  public String getTargetMandt() {
    return targetMandt;
  }

  public void setTargetMandt(String targetMandt) {
    this.targetMandt = targetMandt;
  }

  public String getKatr10() {
    return katr10;
  }

  public void setKatr10(String katr10) {
    this.katr10 = katr10;
  }

}
