/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;

/**
 * Handler for {@link MassCreateFileRow} objects. The class validates and
 * transforms values as needed.
 * 
 * @author Jeffrey Zamora
 * 
 */
public interface RowHandler {

  /**
   * Validates the contents of the {@link MassCreateFileRow} object
   * 
   * @param row
   * @return
   * @throws Exception
   */
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception;

  /**
   * Transforms the contents of the {@link MassCreateFileRow} object
   * 
   * @param row
   * @throws Exception
   */
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception;

  /**
   * If true, the error is critical and should stop further validation
   * 
   * @return
   */
  public boolean isCritical();

}
