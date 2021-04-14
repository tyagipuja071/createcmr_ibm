/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.HandlerEngine;

/**
 * @author JeffZAMORA
 *
 */
public class MassCreateWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(MassCreateWorker.class);
  private static final int MAX_CELL_CONTENTS = 1000;

  private HandlerEngine engine;
  private MassCreateFileRow row;
  private EntityManager entityManager;
  private List<String> errors = new ArrayList<String>();

  private boolean error;
  private String errorMsg;

  private String mode;

  public MassCreateWorker(EntityManager entityManager, HandlerEngine engine, MassCreateFileRow row) {
    this.entityManager = entityManager;
    this.engine = engine;
    this.row = row;
  }

  @Override
  public void run() {
    if (StringUtils.isBlank(this.mode) || "V".equals(this.mode)) {
      validateRow();
    } else {

    }
  }

  private void validateRow() {
    try {
      LOG.debug("Validating row " + this.row.getCmrNo() + "/" + this.row.getSeqNo());
      StringBuilder errorMsg = new StringBuilder();

      this.errors = this.engine.validateRow(this.entityManager, this.row);
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
        this.row.setErrorMessage(errorMsg.toString());
      } else {
        // if no errrors, call transformation
        this.engine.transform(entityManager, row);
      }

    } catch (Exception e) {
      LOG.warn("Error encountered during validations.", e);
      this.error = true;
      this.errorMsg = e.getMessage();
    }
  }

  public boolean isError() {
    return error;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public List<String> getErrors() {
    return errors;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

}
