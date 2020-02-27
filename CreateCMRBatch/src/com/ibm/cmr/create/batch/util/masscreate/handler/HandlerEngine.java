/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;

/**
 * Contains all the handlers for a specific CMR Issuing Country.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class HandlerEngine {

  private List<RowHandler> handlers = new ArrayList<RowHandler>();

  public void addHandler(RowHandler handler) {
    this.handlers.add(handler);
  }

  /**
   * Validates a {@link MassCreateFileRow} row
   * 
   * @param row
   * @return
   * @throws Exception
   */
  public List<String> validateRow(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    List<String> errors = new ArrayList<String>();
    RowResult result = null;
    for (RowHandler validator : this.handlers) {
      result = validator.validate(entityManager, row);
      if (!result.isPassed()) {
        errors.addAll(result.getErrors());
      }
    }
    return errors;
  }

  /**
   * Transforms the values on the {@link MassCreateFileRow}
   * 
   * @param row
   * @throws Exception
   */
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    for (RowHandler validator : this.handlers) {
      validator.transform(entityManager, row);
    }
  }

}
