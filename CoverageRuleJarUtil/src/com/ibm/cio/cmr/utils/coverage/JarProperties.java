/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contains helper methods to read from the property file for Coverage Rules
 * rulejar.properties
 * 
 * @author JeffZAMORA
 *
 */
public class JarProperties {

  private static final ResourceBundle PROPS = ResourceBundle.getBundle("rulejar");

  /**
   * Gets the expected root location in the archive of the integrated coverage
   * rules
   * 
   * @return
   */
  public static final String getIntegratedCoverageDir() {
    return PROPS.getString("jar.dir.integrated");
  }

  /**
   * Gets the expected root location in the archive of the delegation rules
   * 
   * @return
   */
  public static final String getDelegationDir() {
    return PROPS.getString("jar.dir.delegation");
  }

  /**
   * Gets the expected root location in the archive of all the country coverage
   * rules
   * 
   * @return
   */
  public static final String getCountryRootDir() {
    return PROPS.getString("jar.dir.country");
  }

  /**
   * Gets the temporary location where to unpack an archived zip file
   * 
   * @return
   */
  public static final String getZipTempLocation() {
    return PROPS.getString("jar.zip.dir.temp");
  }

  /**
   * Gets the root location where contents of the rules jar will be placed. The
   * root will contain subdirectories for each ruleset, e.g. 2H2018
   * 
   * @return
   */
  public static final String getUnpackRootLocation() {
    return PROPS.getString("jar.unpacked.dir");
  }

  /**
   * Gets the list of root locations in the archive of the country rules, in
   * order of priority
   * 
   * @return
   */
  public static List<String> getCountryCoverageDirectoryProps() {
    String dirList = PROPS.getString("jar.coverage.country.order");
    List<String> directories = new ArrayList<String>();
    if (dirList != null && !dirList.isEmpty()) {
      for (String dirProp : dirList.split(",")) {
        directories.add(dirProp);
      }
    }
    return directories;
  }

  /**
   * Gets a property from the resource
   * 
   * @param key
   * @return
   */
  public static String getProperty(String key) {
    return PROPS.containsKey(key) ? PROPS.getString(key) : null;
  }

}
