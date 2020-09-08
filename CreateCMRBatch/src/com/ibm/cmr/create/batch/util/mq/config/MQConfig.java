/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Column;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.MQTransport;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MQConfig {

  private static Map<String, MQConfig> configurations = new HashMap<String, MQConfig>();
  private static Map<String, String> countryMap = new HashMap<String, String>();
  private static final Logger LOG = Logger.getLogger(MQConfig.class);

  private List<String> countries = new ArrayList<String>();
  private String name;
  private String targetSystem;
  private String channel;
  private String host;
  private int port;
  private String userId;
  private String password;
  private String qMqr;
  private String outputQueue;
  private String inputQueue;
  private String cipher;
  private int ccsid = -1;

  public static void main(String[] args) {

    Map<String, String> map = new HashMap<String, String>();
    map.put("RCYAA", "SOF Country code (RCYAA)");
    map.put("RCUXA", "Customer Number (RCUXA)");
    map.put("NORMABB", "Billing Adderss Name (ITALY) (NORMABB)");
    map.put("INDABB", "Billing Adderss Street (ITALY) (INDABB)");
    map.put("CITABB", "Billing AdderssCity (ITALY) (CITABB)");
    map.put("CNCLA", "Nature Client (FRANCE) (CNCLA)");
    map.put("AGIOS", "Penalties De Retard (FRANCE) (AGIOS)");
    map.put("AFFAC", "Affacturage (FRANCE ) (AFFAC)");
    map.put("IAN01", "INACCont (FRANCE) (IAN01)");
    map.put("TACPA", "Code APE (FRANCE) (TACPA)");
    map.put("CTECH", "Type De Facturation (FRANCE) (CTECH)");
    map.put("TLSPE", "Top Liste Speciale (FRANCE) (TLSPE)");
    map.put("TAPAR", "Tarif Particulier (FRANCE) (TAPAR)");
    map.put("IGSNJ", "Nombre De Jours (FRANCE) (IGSNJ)");
    map.put("TADMI", "Forme Juridique (FRANCE) (TADMI)");
    map.put("SIRET", "Siret/Siren (SIRET)");
    map.put("TCOVREP", "Tale Coverage Representative (TCOVREP)");
    map.put("AECISUB", "AECISUBDate CEMA (AECISUB)");
    map.put("CODSSV", "SSV Code (ITALY) (CODSSV)");
    map.put("CODCC", "Billing Custom.Number(ITALY) (CODCC)");
    map.put("CODCP", "Company Customer Number (CODCP)");
    map.put("IDCLI", "Ident Client (IDCLI)");
    map.put("AFFILIATE", "Affiliate Branch (AFFILIATE)");
    map.put("CODFIS", " (CODFIS)");
    map.put("DIC", "DIC (SLOVAKIA) (DIC)");
    map.put("IVA", "IVA (Italy) (IVA)");
    map.put("UPDATE_TS", " (UPDATE_TS)");
    map.put("RACDS", "ACCOUNT ADMINISTRATOR DSC (RACDS)");
    map.put("CODDES", "CODDICE DESTINARIO/UFICIO (CODDES)");
    map.put("TIPOCL", "TIPO CLIENTE (TIPOCL)");
    map.put("INDEMAIL", "PRIVAT EMAIL ADDRESS (INDEMAIL)");
    map.put("PEC", "PEC EMAIL ADDRESS (PEC)");
    int mod = 0;

    for (Field field : CmrtCustExt.class.getDeclaredFields()) {
      String colName = field.getName().toUpperCase();
      Column col = field.getAnnotation(Column.class);
      if (col != null) {
        colName = col.name().toUpperCase();
      }
      if (map.containsKey(colName)) {
        colName = map.get(colName);
      }
      if (mod % 3 == 0) {
        System.out.println("          <tr>");
      }
      System.out.println("            <td class=\"dnb-label\">" + colName + ":</td>");
      System.out.println("            <td ng-bind-html=\"ext." + field.getName() + "\"></td>");
      mod++;
      if (mod % 3 == 0) {
        System.out.println("          </tr>");
      }
    }
  }

  /**
   * Initializes the Configurations. This checks batch-props for configurations
   * and gets the MQ information fro System Parameters
   */
  public static void initConfigurations() {
    configurations.clear();
    List<String> mqKeys = BatchUtil.getKeysWithPrefix("mq.");

    String[] parts = null;
    String name = null;
    String countries = null;
    String targetSys = null;
    MQConfig config = null;
    for (String key : mqKeys) {
      parts = key.split("[.]");
      if (parts.length == 4) {
        name = parts[1];
        targetSys = parts[2];
        countries = BatchUtil.getProperty(key);
        config = new MQConfig();
        config.setName(name);
        config.setCountries(Arrays.asList(countries.split(",")));
        config.setChannel(SystemParameters.getString("MQ." + name + ".CHL"));
        config.setHost(SystemParameters.getString("MQ." + name + ".HOST"));
        config.setInputQueue(SystemParameters.getString("MQ." + name + ".LQ"));
        config.setOutputQueue(SystemParameters.getString("MQ." + name + ".RQ"));
        config.setqMqr(SystemParameters.getString("MQ." + name + ".QMGR"));
        config.setPort(SystemParameters.getInt("MQ." + name + ".PORT"));
        config.setTargetSystem(targetSys);
        if (!StringUtils.isEmpty(SystemParameters.getString("MQ." + name + ".USER"))) {
          config.setUserId(SystemParameters.getString("MQ." + name + ".USER"));
          config.setPassword(SystemParameters.getString("MQ." + name + ".PWD"));
        }
        if (!StringUtils.isEmpty(SystemParameters.getString("MQ." + name + ".CIPHER"))) {
          config.setCipher(SystemParameters.getString("MQ." + name + ".CIPHER"));
        }
        if (!StringUtils.isEmpty(SystemParameters.getString("MQ." + name + ".CCSID"))) {
          int ccsid = SystemParameters.getInt("MQ." + name + ".CCSID");
          if (ccsid > 0) {
            LOG.debug("Setting CCSID " + ccsid + " for " + name);
            config.setCcsid(ccsid);
          }
        }

        if (StringUtils.isEmpty(config.getqMqr()) || StringUtils.isEmpty(config.getChannel()) || StringUtils.isEmpty(config.getInputQueue())
            || StringUtils.isEmpty(config.getOutputQueue()) || config.getPort() <= 0) {
          LOG.warn("MQ configuation " + name + " is defined but with incomplete definitions under System Parameters.");
        } else {
          LOG.debug("MQ configuration " + name + " initialized.");
          configurations.put(name, config);
          for (String country : config.getCountries()) {
            countryMap.put(country, name);
          }
        }
      } else {
        LOG.warn("MQ Config " + key + " cannot be initialized. Improper format");
      }
    }
  }

  /**
   * Converts the properties on this configuration to {@link MQTransport}
   * properties
   * 
   * @return
   */
  public Properties toEnvProperties(boolean receive) {
    Properties props = new Properties();
    props.put(MQMsgConstants.MQ_QUEUE_MANAGER, this.qMqr);
    if (receive) {
      props.put(MQMsgConstants.MQ_QUEUE, this.inputQueue);
    } else {
      props.put(MQMsgConstants.MQ_QUEUE, this.outputQueue);
    }
    props.put(MQMsgConstants.MQ_CHANNEL, this.channel);
    props.put(MQMsgConstants.MQ_HOST_NAME, this.host);
    props.put(MQMsgConstants.MQ_PORT, String.valueOf(this.port));
    if (this.userId != null) {
      props.put(MQMsgConstants.MQ_USER_ID, this.userId);
      props.put(MQMsgConstants.MQ_PASSWORD, this.password);
    }
    if (this.cipher != null) {
      props.put(MQMsgConstants.MQ_CIPHER, this.cipher);
    }
    return props;
  }

  /**
   * Returns the defined configuration names
   * 
   * @return
   */
  public static Set<String> getConfigNames() {
    return configurations.keySet();
  }

  /**
   * Gets a defined MQ configuration for the specified country
   * 
   * @param cmrIssuingCountry
   * @return
   */
  public static String getConfigName(String cmrIssuingCountry) {
    return countryMap.get(cmrIssuingCountry);
  }

  /**
   * Gets a defined MQ configuration for the specified config name
   * 
   * @param cmrIssuingCountry
   * @return
   */
  public static MQConfig getConfig(String name) {
    return configurations.get(name);
  }

  /**
   * Gets a defined MQ configuration for the specified country
   * 
   * @param cmrIssuingCountry
   * @return
   */
  public static MQConfig getConfigForCountry(String cmrIssuingCountry) {
    String name = getConfigName(cmrIssuingCountry);
    if (name != null) {
      return configurations.get(name);
    }
    return null;
  }

  public List<String> getCountries() {
    return countries;
  }

  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getOutputQueue() {
    return outputQueue;
  }

  public void setOutputQueue(String outputQueue) {
    this.outputQueue = outputQueue;
  }

  public String getInputQueue() {
    return inputQueue;
  }

  public void setInputQueue(String inputQueue) {
    this.inputQueue = inputQueue;
  }

  public String getqMqr() {
    return qMqr;
  }

  public void setqMqr(String qMqr) {
    this.qMqr = qMqr;
  }

  public String getTargetSystem() {
    return targetSystem;
  }

  public void setTargetSystem(String targetSystem) {
    this.targetSystem = targetSystem;
  }

  public int getCcsid() {
    return ccsid;
  }

  public void setCcsid(int ccsid) {
    this.ccsid = ccsid;
  }

  public String getCipher() {
    return cipher;
  }

  public void setCipher(String cipher) {
    this.cipher = cipher;
  }
}
