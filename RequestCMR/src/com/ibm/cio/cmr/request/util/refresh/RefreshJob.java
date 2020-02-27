/**
 * 
 */
package com.ibm.cio.cmr.request.util.refresh;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.ui.UIMgr;
import com.ibm.cio.cmr.request.ui.template.TemplateManager;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemParameters;

/**
 * @author JeffZAMORA
 * 
 */
public class RefreshJob implements Runnable {

  private static final Logger LOG = Logger.getLogger(RefreshJob.class);

  @Override
  public void run() {
    while (true) {

      try {
        Thread.sleep(1000 * 60 * 5);
      } catch (InterruptedException e) {
      }

      String lastRefresh = checkRefreshTime();
      if (lastRefresh != null && !lastRefresh.equals(SystemConfiguration.LAST_REFRESH_TIME)) {
        LOG.info("Application will be refreshed due to external refreshes..");
        try {

          ExternalizedQuery.refresh();
          MessageUtil.refresh();
          UIMgr.refresh();

          DropdownListController.refresh();
          RequestUtils.refresh();
          PageManager.init();
          TemplateManager.refresh();
          SystemParameters.refresh();

          SystemConfiguration.LAST_REFRESH_TIME = lastRefresh;
        } catch (Exception e) {
          LOG.error("Error in refresh thread.", e);
        }

      }

    }
  }

  private String checkRefreshTime() {
    try {
      EntityManager entityManager = JpaManager.getEntityManager();
      try {

        String sql = ExternalizedQuery.getSql("SYSPAR.CHECK_REFRESH");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        return query.getSingleResult(String.class);
      } finally {
        // empty the manager
        entityManager.clear();
        entityManager.close();
      }
    } catch (Exception e) {
      LOG.error("Error when trying to update refresh time.", e);
      return "";
    }
  }

}
