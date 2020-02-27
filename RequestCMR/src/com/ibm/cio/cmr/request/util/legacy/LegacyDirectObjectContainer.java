/**
 * 
 */
package com.ibm.cio.cmr.request.util.legacy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrLink;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * Container for all the legacy DB entities to be created/updated.
 * 
 * @author JeffZAMORA
 * 
 */
public class LegacyDirectObjectContainer {

  private static final Logger LOG = Logger.getLogger(LegacyDirectObjectContainer.class);

  private String customerNo;
  private String sofCntryCd;
  private CmrtCust customer;
  private CmrtCustExt customerExt;
  private final List<CmrtAddr> addresses = new ArrayList<CmrtAddr>();
  // private List<CmrtAddrUse> uses = new ArrayList<CmrtAddrUse>();
  private final List<CmrtAddrLink> links = new ArrayList<CmrtAddrLink>();
  private String errTxt;

  private static final String ADDRESS_USE_MAILING = "1";
  private static final String ADDRESS_USE_BILLING = "2";
  private static final String ADDRESS_USE_INSTALLING = "3";
  private static final String ADDRESS_USE_SHIPPING = "4";
  private static final String ADDRESS_USE_EPL_MAILING = "5";
  private static final String ADDRESS_USE_LIT_MAILING = "6";
  private static final String ADDRESS_USE_COUNTRY_A = "A";
  private static final String ADDRESS_USE_COUNTRY_B = "B";
  private static final String ADDRESS_USE_COUNTRY_C = "C";
  private static final String ADDRESS_USE_COUNTRY_D = "D";
  private static final String ADDRESS_USE_COUNTRY_E = "E";
  private static final String ADDRESS_USE_COUNTRY_F = "F";
  private static final String ADDRESS_USE_COUNTRY_G = "G";
  private static final String ADDRESS_USE_COUNTRY_H = "H";
  private static final String ADDRESS_USE_EXISTS = "Y";

  private List<CmrtCust> customersIT = new ArrayList<CmrtCust>();
  private List<CmrtCustExt> customersextIT = new ArrayList<CmrtCustExt>();

  /**
   * Finds the address pertaining to the given sequence no
   * 
   * @param seqNo
   * @return
   */
  // Mukesh:Story 1698123
  public CmrtAddr findBySeqNo(String seqNo) {
    String addrNo = "";
    for (CmrtAddr addr : this.addresses) {
      LOG.debug("AddrNo====" + addr.getId().getAddrNo() + "======Length========" + addr.getId().getAddrNo().length());
      if (addr.getId().getAddrNo() != null && addr.getId().getAddrNo().length() > 1) {
        addrNo = addr.getId().getAddrNo().replaceFirst("^0*", "");
        LOG.debug("New AddrNo after removing zero==========" + addrNo);
      } else {
        addrNo = addr.getId().getAddrNo();
        LOG.debug("AddrNo without any change=================" + addrNo);
      }
      LOG.debug("SeqNo====================" + seqNo);
      LOG.debug("SeqNo length==============" + seqNo.length());
      if (seqNo != null && seqNo.length() > 1) {
        seqNo = seqNo.replaceFirst("^0*", "");
        LOG.debug("New SeqNo after removing zero==" + seqNo);
      }
      // CMR-1025
      if (SystemLocation.ITALY.equals(addr.getId().getSofCntryCode())) {
        if (addr.getIsAddrUseBilling() != null && addr.getIsAddrUseBilling().equals("Y") && ("B".equals(seqNo) || "2".equals(seqNo))) {
          if (addr.getId().getAddrNo() != null && (addr.getId().getAddrNo().equals("0000B") || addr.getId().getAddrNo().equals("00002"))) {
            addrNo = seqNo;
          }
        }
      }

      LOG.debug("values of AddrNo==" + addrNo + "== and SeqNo==" + seqNo);
      if (addrNo.trim().equals(seqNo.trim())) {
        LOG.debug("Returning value of Addr==============" + addr.getId().getAddrNo());
        return addr;
      }
    }
    return null;
  }

  public CmrtAddr findByAddressUseFlag(String addressUse) {
    CmrtAddr neededAddr = new CmrtAddr();

    for (CmrtAddr addr : this.addresses) {
      if (addressUse.equals(ADDRESS_USE_BILLING)) {
        switch (addressUse) {
        case "1": // private static final String ADDRESS_USE_MAILING = "1";
          if ("Y".equals(addr.getIsAddrUseMailing())) {
            neededAddr = addr;
          }
        case "2": // private static final String ADDRESS_USE_BILLING = "2";
          if ("Y".equals(addr.getIsAddrUseBilling())) {
            neededAddr = addr;
          }
        case "3": // private static final String ADDRESS_USE_INSTALLING = "3";
          if ("Y".equals(addr.getIsAddrUseInstalling())) {
            neededAddr = addr;
          }
        case "4": // private static final String ADDRESS_USE_SHIPPING = "4";
          if ("Y".equals(addr.getIsAddrUseShipping())) {
            neededAddr = addr;
          }
        default:
          break;
        }
      }
    }
    return neededAddr;
  }

  /**
   * Finds the address pertaining to the given sequence no
   * 
   * DTN: 11152019- Please refrain from using this as this does not do anything
   * that is nearly related to using the address use flags to find an address.
   * Please use findByAddressUseFlag instead.
   * 
   * @param seqNo
   * @return
   */
  @Deprecated
  public List<CmrtAddr> findByAddressUse(String addressUse) {
    List<CmrtAddr> addresses = new ArrayList<CmrtAddr>();
    // Mukesh:Story 1698123
    Set<String> seqNos = new HashSet<String>();
    for (CmrtAddr addr : this.addresses) {
      seqNos.add(addr.getId().getAddrNo());
    }
    /*
     * for (CmrtAddrUse use : this.uses) { if
     * (addressUse.contains(use.getId().getAddrUse())) {
     * seqNos.add(use.getId().getAddrNo()); } }
     */
    CmrtAddr addr = null;
    // Mukesh:Story 1698123
    for (String addrNo : seqNos) {
      addr = findBySeqNo(addrNo);
      if (addr != null) {
        addresses.add(addr);
      }
    }
    return addresses;
  }

  /**
   * Gets the list of address uses for a given address sequence no
   * 
   * @param seqNo
   * @return
   */
  /*
   * public List<String> getUsesBySequenceNo(int seqNo) { List<String> uses =
   * new ArrayList<String>(); for (CmrtAddrUse use : this.uses) { if
   * (use.getId().getAddrNo() == seqNo) { uses.add(use.getId().getAddrUse()); }
   * } return uses; }
   */
  // Mukesh:Story 1698123
  public List<String> getUsesBySequenceNo(String seqNo) {
    List<String> uses = new ArrayList<String>();
    String addrNo = "";

    for (CmrtAddr addr : this.addresses) {
      if (addr.getId().getAddrNo() != null && addr.getId().getAddrNo().length() > 1) {
        addrNo = addr.getId().getAddrNo().replaceFirst("^0*", "");
      } else {
        addrNo = addr.getId().getAddrNo();
      }
      if (seqNo != null && seqNo.length() > 1) {
        seqNo = seqNo.replaceFirst("^0*", "");
      }
      if (addrNo.equals(seqNo)) {
        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddrUseMailing())) {
          uses.add(ADDRESS_USE_MAILING);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddrUseBilling())) {
          uses.add(ADDRESS_USE_BILLING);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddrUseInstalling())) {
          uses.add(ADDRESS_USE_INSTALLING);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddrUseShipping())) {
          uses.add(ADDRESS_USE_SHIPPING);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddrUseEPL())) {
          uses.add(ADDRESS_USE_EPL_MAILING);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddrUseLitMailing())) {
          uses.add(ADDRESS_USE_LIT_MAILING);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddressUseA())) {
          uses.add(ADDRESS_USE_COUNTRY_A);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddressUseB())) {
          uses.add(ADDRESS_USE_COUNTRY_B);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddressUseC())) {
          uses.add(ADDRESS_USE_COUNTRY_C);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddressUseD())) {
          uses.add(ADDRESS_USE_COUNTRY_D);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddressUseE())) {
          uses.add(ADDRESS_USE_COUNTRY_E);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddressUseF())) {
          uses.add(ADDRESS_USE_COUNTRY_F);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddressUseG())) {
          uses.add(ADDRESS_USE_COUNTRY_G);
        }

        if (ADDRESS_USE_EXISTS.equals(addr.getIsAddressUseH())) {
          uses.add(ADDRESS_USE_COUNTRY_H);
        }
      }
    }
    return uses;
  }

  /**
   * Untags the address use on this address
   * 
   * @param seqNo
   * @param addrUse
   */
  /*
   * public void untagUseForSequence(int seqNo, String addrUse) { CmrtAddr
   * address = findBySeqNo(seqNo); if (address != null) { String taggedUse =
   * address.getAddressUse(); LOG.trace("Current Address Use: " + taggedUse +
   * ". Untagging: " + addrUse); List<CmrtAddrUse> uses = new
   * ArrayList<CmrtAddrUse>(); for (String use : addrUse.split("")) { if
   * (!StringUtils.isEmpty(use)) { taggedUse = StringUtils.replace(taggedUse,
   * use, ""); address.setAddressUse(taggedUse); } for (CmrtAddrUse legacyUse :
   * this.uses) { if (addrUse.equals(legacyUse.getId().getAddrUse()) && seqNo ==
   * legacyUse.getId().getAddrNo()) { // do not add here } else {
   * uses.add(legacyUse); } } } this.uses = uses;
   * LOG.trace("Untagged Address Use: " + address.getAddressUse()); } }
   */

  public void addAddress(CmrtAddr addr) {
    this.addresses.add(addr);
  }

  /*
   * public void addAddressUse(CmrtAddrUse addrUse) { this.uses.add(addrUse); }
   */

  public void addLink(CmrtAddrLink link) {
    this.links.add(link);
  }

  public CmrtCust getCustomer() {
    return customer;
  }

  public void setCustomer(CmrtCust customer) {
    this.customer = customer;
  }

  public CmrtCustExt getCustomerExt() {
    return customerExt;
  }

  public void setCustomerExt(CmrtCustExt customerExt) {
    this.customerExt = customerExt;
  }

  public List<CmrtAddr> getAddresses() {
    return addresses;
  }

  /*
   * public List<CmrtAddrUse> getUses() { return uses; }
   */

  public List<CmrtAddrLink> getLinks() {
    return links;
  }

  public String getCustomerNo() {
    return customerNo;
  }

  public void setCustomerNo(String customerNo) {
    this.customerNo = customerNo;
  }

  public String getSofCntryCd() {
    return sofCntryCd;
  }

  public void setSofCntryCd(String sofCntryCd) {
    this.sofCntryCd = sofCntryCd;
  }

  public String getErrTxt() {
    return errTxt;
  }

  public void setErrTxt(String errTxt) {
    this.errTxt = errTxt;
  }

  public List<CmrtCust> getCustomersIT() {
    return customersIT;
  }

  public List<CmrtCustExt> getCustomersextIT() {
    return customersextIT;
  }

  public void setCustomersIT(List<CmrtCust> customersIT) {
    this.customersIT = customersIT;
  }

  public void setCustomersextIT(List<CmrtCustExt> customersextIT) {
    this.customersextIT = customersextIT;
  }

}
