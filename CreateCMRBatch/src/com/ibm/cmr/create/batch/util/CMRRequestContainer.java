/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.util.ArrayList;
import java.util.List;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdt;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;

/**
 * Contains Admin, Data, and Addr records for the request
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CMRRequestContainer {

  private Admin admin;
  private Data data;
  private List<Addr> addresses = new ArrayList<>();
  private List<MassUpdt> massUpdate = new ArrayList<>();
  private List<MassUpdtAddr> massUpdateAddresses = new ArrayList<>();
  private MassUpdtData massUpdateData;

  public void addAddress(Addr addr) {
    this.addresses.add(addr);
  }

  public List<Addr> getAddresses() {
    return this.addresses;
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

  public void addMassUpdateAddresses(MassUpdtAddr massUpdateAddress) {
    this.massUpdateAddresses.add(massUpdateAddress);
  }

  public List<MassUpdtAddr> getMassUpdateAddresses() {
    return massUpdateAddresses;
  }

  private void setAddresses(List<Addr> addresses) {
    this.addresses = addresses;
  }

  private void setMassUpdateAddresses(List<MassUpdtAddr> massUpdateAddresses) {
    this.massUpdateAddresses = massUpdateAddresses;
  }

  public List<MassUpdt> getMassUpdate() {
    return massUpdate;
  }

  private void setMassUpdate(List<MassUpdt> massUpdate) {
    this.massUpdate = massUpdate;
  }

  public void addMassUpdate(MassUpdt massUpdtData) {
    this.massUpdate.add(massUpdtData);
  }

  public MassUpdtData getMassUpdateData() {
    return massUpdateData;
  }

  public void setMassUpdateData(MassUpdtData massUpdateData) {
    this.massUpdateData = massUpdateData;
  }

}
