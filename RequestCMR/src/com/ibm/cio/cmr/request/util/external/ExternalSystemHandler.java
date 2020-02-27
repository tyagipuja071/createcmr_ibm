package com.ibm.cio.cmr.request.util.external;

import java.util.List;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.Admin;

/**
 * Interface for External System Handlers
 * 
 * @author JeffZAMORA
 * 
 */
public interface ExternalSystemHandler {

  /**
   * Adds extra email parameters to the mails sent by CreateCMR
   * 
   * @param entityManager
   * @param admin
   */
  public void addEmailParams(EntityManager entityManager, List<Object> params, Admin admin);
}
