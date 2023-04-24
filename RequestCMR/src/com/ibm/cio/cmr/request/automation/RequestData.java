/**
 * 
 */
package com.ibm.cio.cmr.request.automation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.ScorecardPK;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * Contains the {@link Entity} objects of the request data for use in the
 * {@link AutomationEngine} and corresponding {@link AutomationElement}
 * 
 * @author JeffZAMORA
 * 
 */
public class RequestData {

  private Admin admin;
  private Data data;
  private Scorecard scorecard;
  private List<Addr> addresses = new ArrayList<>();
  private List<MassUpdtAddr> muAddr = new ArrayList<>();

  /**
   * Constructor to wrap the current entities into a {@link RequestData} object
   * 
   * @param admin
   * @param data
   * @param addrs
   * @return
   */
  public static RequestData wrap(Admin admin, Data data, Scorecard scorecard, Addr... addresses) {
    RequestData requestData = new RequestData();
    requestData.admin = admin;
    requestData.data = data;
    requestData.scorecard = scorecard;
    requestData.addresses.addAll(Arrays.asList(addresses));
    return requestData;
  }

  /**
   * Private constructor to avoid blank entities
   */
  private RequestData() {
    // noop
  }

  /**
   * Constructs a {@link RequestData} instance based on the reqId supplied. This
   * connects to the the database to fill the {@link Admin}, {@link Data},
   * {@link Scorecard}, and relevant underlying objects
   * 
   * @param entityManager
   * @param reqId
   */
  public RequestData(EntityManager entityManager, long reqId) {

    AdminPK adminPk = new AdminPK();
    adminPk.setReqId(reqId);
    this.admin = entityManager.find(Admin.class, adminPk);
    if (this.admin != null) {
      entityManager.refresh(this.admin);
    }

    DataPK dataPk = new DataPK();
    dataPk.setReqId(reqId);
    this.data = entityManager.find(Data.class, dataPk);
    if (this.data != null) {
      entityManager.refresh(this.data);
    }

    ScorecardPK scorecardPk = new ScorecardPK();
    scorecardPk.setReqId(reqId);
    this.scorecard = entityManager.find(Scorecard.class, scorecardPk);
    if (this.scorecard != null) {
      entityManager.refresh(this.scorecard);
    }

    switch (admin.getReqType()) {
    case "C":
    case "U":
      this.addresses = extractAddresses(entityManager, reqId);
      break;
    /*
     * case "M": throw new
     * IllegalArgumentException("Mass Update is not supported at the moment.");
     * case "N": throw new
     * IllegalArgumentException("Mass Create is not supported at the moment.");
     */
    }
  }

  /**
   * Constructs a dummy {@link RequestData} instance
   */
  public RequestData(EntityManager entityManager) {
    AdminPK adminPk = new AdminPK();
    entityManager.detach(adminPk);
    this.admin = new Admin();
    this.admin.setId(adminPk);
    entityManager.detach(this.admin);

    DataPK dataPk = new DataPK();
    entityManager.detach(dataPk);
    this.data = new Data();
    this.data.setId(dataPk);
    entityManager.detach(this.admin);

    this.addresses = new ArrayList<>();
  }

  public Addr createDummyAddress(EntityManager entityManager, String addrType, String addrSeq) {
    Addr addr = new Addr();
    AddrPK addrPk = new AddrPK();
    entityManager.detach(addrPk);
    addrPk.setAddrType(addrType);
    addrPk.setAddrSeq(addrSeq);
    addr.setId(addrPk);
    entityManager.detach(addr);
    return addr;
  }

  /**
   * Extracts the ADDR records of a request
   * 
   * @param entityManager
   * @param reqId
   */
  private static List<Addr> extractAddresses(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.GET_ADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Addr> addresses = new ArrayList<>();
    addresses.addAll(query.getResults(Addr.class));
    return addresses;
  }

  /**
   * Creates a {@link RequestEntryModel} object from the container
   * {@link RequestData}
   * 
   * @param requestData
   * @return
   */
  public RequestEntryModel createModelFromRequest() {
    RequestEntryModel model = new RequestEntryModel();
    try {
      PropertyUtils.copyProperties(model, this.admin);
      PropertyUtils.copyProperties(model, this.data);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return model;
  }

  /**
   * Gets an {@link Addr} object registered to this container based on the
   * address type
   * 
   * @param addrType
   * @return
   */
  public Addr getAddress(String addrType) {
    if (this.addresses != null) {
      for (Addr addr : this.addresses) {
        if (addrType.equals(addr.getId().getAddrType())) {
          return addr;
        }
      }
    }
    return null;
  }

  /**
   * Gets all {@link Addr} records with the given address type
   * 
   * @param addrType
   * @return
   */
  public List<Addr> getAddresses(String addrType) {
    List<Addr> addresses = new ArrayList<Addr>();
    if (this.addresses != null) {
      for (Addr addr : this.addresses) {
        if (addrType.equals(addr.getId().getAddrType())) {
          addresses.add(addr);
        }
      }
    }
    return addresses;
  }

  /**
   * Gets an {@link Addr} object registered to this container based on the
   * address type
   * 
   * @param addrType
   * @return
   */
  public Addr getAddress(String addrType, String seqNo) {
    if (this.addresses != null) {
      for (Addr addr : this.addresses) {
        if (addrType.equals(addr.getId().getAddrType()) && seqNo.equals(addr.getId().getAddrSeq())) {
          return addr;
        }
      }
    }
    return null;
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

  public Scorecard getScorecard() {
    return scorecard;
  }

  public void setScorecard(Scorecard scorecard) {
    this.scorecard = scorecard;
  }

  public List<Addr> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Addr> addresses) {
    this.addresses = addresses;
  }

  public List<MassUpdtAddr> getMuAddr() {
    return muAddr;
  }

  public void setMuAddr(List<MassUpdtAddr> muAddr) {
    this.muAddr = muAddr;
  }
}
