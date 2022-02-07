/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.archive;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for the files and directories where the rules will be read from
 * 
 * @author JeffZAMORA
 *
 */
public class RulesContainer {

  private List<File> integratedCoverageFiles = new ArrayList<File>();
  private List<File> delegationFiles = new ArrayList<File>();
  private Map<String, List<File>> countryCoverageFiles = new LinkedHashMap<String, List<File>>();

  /**
   * Adds a directory location containing integrated coverage rules
   * 
   * @param file
   */
  public void addIntegratedCoverageFile(File file) {
    this.integratedCoverageFiles.add(file);
  }

  /**
   * Adds a directory location containing delegation rules
   * 
   * @param file
   */
  public void addDelegationFile(File file) {
    this.delegationFiles.add(file);
  }

  /**
   * Adds a directory location containing country coverage rules
   * 
   * @param covGroup
   * @param file
   */
  public void addCountryCoverageFile(String covGroup, File file) {
    if (!this.countryCoverageFiles.containsKey(covGroup)) {
      this.countryCoverageFiles.put(covGroup, new ArrayList<File>());
    }
    this.countryCoverageFiles.get(covGroup).add(file);
  }

  /**
   * Checks if all relevant directories are accounted for
   * 
   * @return
   */
  public boolean isValid() {
    return !this.integratedCoverageFiles.isEmpty() || !this.delegationFiles.isEmpty() || !this.countryCoverageFiles.isEmpty();
  }

  public List<File> getIntegratedCoverageFiles() {
    return integratedCoverageFiles;
  }

  public List<File> getDelegationFiles() {
    return delegationFiles;
  }

  public Map<String, List<File>> getCountryCoverageFiles() {
    return countryCoverageFiles;
  }

}
