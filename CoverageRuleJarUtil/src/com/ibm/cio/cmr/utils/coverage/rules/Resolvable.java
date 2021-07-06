/**
 * 
 */
package com.ibm.cio.cmr.utils.coverage.rules;

import com.ibm.cio.cmr.utils.coverage.objects.CoverageInput;

/**
 * Any instance that can be computed to a match
 * 
 * @author JeffZAMORA
 *
 */
public interface Resolvable {

  /**
   * Checks the given input if it matches the conditions on this resolvable
   * 
   * @param input
   * @return
   */
  public boolean resolve(CoverageInput input, String integratedCoverageId);

  /**
   * Checks whether the instance is a valid resolvable instance
   * 
   * @return
   */
  public boolean isValid();

  /**
   * Prints the matches resolvable conditions
   */
  public void display(int nestLevel, StringBuilder displayBuilder);
}
