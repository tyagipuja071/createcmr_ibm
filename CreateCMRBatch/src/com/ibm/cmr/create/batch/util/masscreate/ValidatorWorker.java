/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cmr.create.batch.service.MassCreateValidatorService;

/**
 * Worker process for mass create validation
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ValidatorWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(ValidatorWorker.class);

  private MassCreateValidatorService service;
  private CompoundEntity result;
  private EntityManager entityManager;
  private boolean error;
  private String errorMessage;

  public ValidatorWorker(EntityManager entityManager, MassCreateValidatorService service, CompoundEntity result) {
    this.service = service;
    this.result = result;
    this.entityManager = entityManager;
  }

  @Override
  public void run() {
    try {
      LOG.info("Worker starting processing...");
      this.service.processRecord(this.entityManager, this.result);
    } catch (Exception e) {
      this.error = true;
      this.errorMessage = "Unexpected error when processing record. " + e.getMessage();
      LOG.error("Unexpected error when processing record. ", e);
    }
  }

  public boolean isError() {
    return error;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

}
