/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;

/**
 * @author 136786PH1
 *
 */
public class FillSapNoService extends TransConnService {

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    try {

      initClient();

      LOG.info("Multi Mode: " + this.multiMode);

      ChangeLogListener.setUser(BATCH_USER_ID);

      monitorUpdateSapNumber(entityManager);

      return true;
    } catch (Exception e) {
      addError(e);
      return false;
    }
  }

}
