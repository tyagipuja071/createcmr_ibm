/**
 * 
 */
package com.ibm.cio.cmr.request.masschange;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;

/**
 * Validates any value
 * 
 * @author JeffZAMORA
 * 
 */
public interface ValueValidator {

  public void validate(EntityManager entityManager, TemplateValidation validation, String country, String cmrNo, String value) throws Exception;
}
