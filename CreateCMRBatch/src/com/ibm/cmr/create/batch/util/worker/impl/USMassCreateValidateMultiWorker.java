/**
 * 
 */
package com.ibm.cmr.create.batch.util.worker.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.MultiThreadedWorker;
import com.ibm.cmr.create.batch.util.masscreate.handler.HandlerEngine;

/**
 * @author 136786PH1
 *
 */
public class USMassCreateValidateMultiWorker extends MultiThreadedWorker<MassCreateFileRow> {

  private static final Logger LOG = Logger.getLogger(USMassCreateValidateMultiWorker.class);
  private static final int MAX_CELL_CONTENTS = 1000;

  private HandlerEngine engine;
  private List<String> errors = new ArrayList<String>();
  private long reqId;

  /**
   * @param parentAdmin
   * @param parentEntity
   */
  public USMassCreateValidateMultiWorker(Admin parentAdmin, MassCreateFileRow parentEntity, HandlerEngine engine) {
    super(parentAdmin, parentEntity);
    this.engine = engine;
    this.reqId = parentAdmin.getId().getReqId();
  }

  @Override
  public void executeProcess(EntityManager entityManager) throws Exception {
    LOG.debug("Validating row Request " + this.reqId + " Row Number " + this.parentRow.getSeqNo());
    StringBuilder errorMsg = new StringBuilder();

    this.errors = this.engine.validateRow(entityManager, this.parentRow);
    if (this.errors.size() > 0) {
      errorMsg.delete(0, errorMsg.length());
      for (String error : this.errors) {
        errorMsg.append(errorMsg.length() > 0 ? "\n" : "");
        errorMsg.append(error);
      }
      if (errorMsg.length() > MAX_CELL_CONTENTS) {
        // limit to 200 so as not to exceed excel's limit
        errorMsg.delete(MAX_CELL_CONTENTS, errorMsg.length());
        errorMsg.append("\nToo many errors.");
      }
      this.parentRow.setErrorMessage(errorMsg.toString());
    } else {
      // if no errrors, call transformation
      this.engine.transform(entityManager, this.parentRow);
    }
  }

  @Override
  public boolean flushOnCommitOnly() {
    return true;
  }

  public List<String> getErrors() {
    return errors;
  }
}
