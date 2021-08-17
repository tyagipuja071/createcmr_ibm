/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.parsers;

import com.ibm.cio.cmr.utils.coverage.rules.Coverage;

/**
 * Parses class for the Coverage assignment lines
 * 
 * @author JeffZAMORA
 *
 */
public class CoverageParser {

  /**
   * Extracts the coverage type and ID based on the
   * <strong>coverageOutput.addCoverage</strong> call
   * 
   * @param irlCoverageLine
   * @return
   */
  public static Coverage extract(String irlCoverageLine) {
    if (irlCoverageLine.contains("coverageInput.delegationAuthorized =")) {
      Coverage coverage = new Coverage();
      coverage.setDelegationAuthorized(true);
      return coverage;
    } else if (irlCoverageLine.contains("coverageOutput.addCoverage") || irlCoverageLine.contains("coverageOutput.addUSCoverage")) {
      String line = irlCoverageLine;
      line = line.replaceAll(
          "new ilog\\.rules\\.xml\\.types\\.IlrDate\\(ilog\\.rules\\.brl\\.IlrDateUtil\\.getLocalTime\\(9999, 11, 31, 0, 0, 0, 0, 0\\)\\)", "\"\"");
      line = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
      String[] parts = line.split(",");
      Coverage coverage = new Coverage();
      if ("coverageInput.countryCode".equals(parts[0].trim())) {
        coverage.setCountry(null);
      } else {
        coverage.setCountry(parts[0].trim());
      }
      coverage.setType(parts[2]);
      coverage.setLevel(parts[1]);
      coverage.setId(parts[3]);
      coverage.setId(coverage.getId().replace("\\t", "").trim());
      if (parts.length > 7) {
        coverage.setClientTier(parts[7]);
      }
      if (parts.length > 8) {
        coverage.setIsu(parts[8]);
      }

      return coverage;
    } else {
      throw new IllegalArgumentException("Coverage line " + irlCoverageLine + " cannot be parsed.");
    }
  }
}
