/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.util.external.CreateCMRBPHandler;
import com.ibm.cio.cmr.request.util.external.ExternalSystemHandler;

/**
 * Utility containing functions that deal with requests created from external
 * sources, e.g. GAIA or CreateCMR-BP
 * 
 * @author JeffZAMORA
 * 
 */
public class ExternalSystemUtil {

  private static Map<String, ExternalSystemHandler> handlers = new HashMap<String, ExternalSystemHandler>();

  static {
    handlers.put("createcmr-bp", new CreateCMRBPHandler());
  }

  public static void addExternalMailParams(EntityManager entityManager, java.util.List<Object> params, Admin admin) {
    ExternalSystemHandler handler = handlers.get(admin.getSourceSystId().toLowerCase());
    if (handler != null) {
      handler.addEmailParams(entityManager, params, admin);
    }
  }

}
