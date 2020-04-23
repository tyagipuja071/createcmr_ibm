package com.ibm.cio.cmr.request.automation.util.geo.us;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Maps the BP portal handled enterprises with their CE IDs
 * 
 * @author JeffZAMORA
 *
 */
public class USCeIdMapping {

  private static final Logger LOG = Logger.getLogger(USCeIdMapping.class);

  private String ceId;
  private String companyNo;
  private String enterpriseNo;
  private String cmrNo;
  private boolean distributor;
  private String name;

  private static Map<String, USCeIdMapping> configurations = new HashMap<String, USCeIdMapping>();

  public static synchronized void load(boolean force) {
    if (configurations.isEmpty() || force) {
      LOG.debug("Initializing CE ID mappings..");
      Map<String, USCeIdMapping> mappings = new HashMap<String, USCeIdMapping>();
      try {
        ClassLoader loader = USCeIdMapping.class.getClassLoader();
        try (InputStream is = loader.getResourceAsStream("us-ceid-mapping.properties")) {
          try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
              USCeIdMapping mapping = new USCeIdMapping();
              String[] split = line.split("\\|");
              mapping.setCeId(split[0]);
              mapping.setCompanyNo(split[1]);
              mapping.setEnterpriseNo(split[2]);
              mapping.setCmrNo(split[3]);
              mapping.setName(split[4]);
              if (split.length > 5 && "Y".equals(split[5])) {
                LOG.trace(" - Found distributor " + mapping.getName());
                mapping.setDistributor(true);
              }
              mappings.put(split[0], mapping);
            }
          }
        }
        if (mappings.size() > 0) {
          configurations.clear();
          configurations.putAll(mappings);
        }
      } catch (Exception e) {
        LOG.warn("CE ID mappings cannot be initialized.");
      }
    }
  }

  /**
   * Gets {@link USCeIdMapping} based on PPS CEID
   * 
   * @param ceId
   * @return
   */
  public static USCeIdMapping getByCeid(String ceId) {
    load(false);

    return configurations.get(ceId);
  }

  /**
   * Gets {@link USCeIdMapping} based on Enterprise No.
   * 
   * @param enterpriseNo
   * @return
   */
  public static USCeIdMapping getByEnterprise(String enterpriseNo) {
    for (USCeIdMapping mapping : configurations.values()) {
      if (enterpriseNo.equals(mapping.getEnterpriseNo())) {
        return mapping;
      }
    }
    return null;
  }

  public String getCeId() {
    return ceId;
  }

  public void setCeId(String ceId) {
    this.ceId = ceId;
  }

  public String getCompanyNo() {
    return companyNo;
  }

  public void setCompanyNo(String companyNo) {
    this.companyNo = companyNo;
  }

  public String getEnterpriseNo() {
    return enterpriseNo;
  }

  public void setEnterpriseNo(String enterpriseNo) {
    this.enterpriseNo = enterpriseNo;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public boolean isDistributor() {
    return distributor;
  }

  public void setDistributor(boolean distributor) {
    this.distributor = distributor;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
