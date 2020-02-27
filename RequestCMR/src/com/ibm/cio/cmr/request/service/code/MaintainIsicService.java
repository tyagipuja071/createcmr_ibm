/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.sql.Timestamp;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.ReftUnsicW;
import com.ibm.cio.cmr.request.entity.ReftUnsicWPK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.IsicModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class MaintainIsicService extends BaseSimpleService<ProcessResultModel> {

  private static final Logger LOG = Logger.getLogger(MaintainIsicService.class);

  @Override
  protected ProcessResultModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    ProcessResultModel result = new ProcessResultModel();
    IsicModel model = (IsicModel) params.getParam("model");

    try {
      int reftUnsicKey = model.getReftUnsicKey();
      if (reftUnsicKey > 0) {
        // update
        ReftUnsicWPK sicPK = new ReftUnsicWPK();
        sicPK.setReftUnsicKey(reftUnsicKey);
        LOG.debug("Retrieving SIC record with key " + reftUnsicKey);
        ReftUnsicW sic = entityManager.find(ReftUnsicW.class, sicPK);
        if (sic != null) {
          sic.setReftUnsicDesc(model.getReftUnsicAbbrevDesc());
          sic.setReftUnsicAbbrevDesc(model.getReftUnsicAbbrevDesc());
          sic.setReftIndclKey(model.getReftIndclKey());
          LOG.debug("Updating SIC " + model.getReftUnsicCd() + " for " + model.getGeoCd() + " under " + model.getReftIndclKey());
          entityManager.merge(sic);
          entityManager.flush();
        }
      } else {
        // create
        Timestamp ts = SystemUtil.getActualTimestamp();
        ReftUnsicWPK sicPK = new ReftUnsicWPK();
        int currMax = getMaxKey(entityManager);
        LOG.debug("Setting KEY value to " + (currMax + 1));
        sicPK.setReftUnsicKey(currMax + 1);
        ReftUnsicW sic = new ReftUnsicW();
        sic.setId(sicPK);
        sic.setActiveDate(ts);
        sic.setComments("Added from admin screen");
        sic.setGeoCd(model.getGeoCd());
        sic.setReftIndclKey(model.getReftIndclKey());
        sic.setReftUnsicCd(model.getReftUnsicCd());
        sic.setReftUnsicAbbrevDesc(model.getReftUnsicAbbrevDesc());
        sic.setReftUnsicDesc(model.getReftUnsicAbbrevDesc());
        sic.setRowCreateTs(ts);
        sic.setRowUpdtTs(ts);
        LOG.debug("Mapping SIC " + model.getReftUnsicCd() + " for " + model.getGeoCd() + " under " + model.getReftIndclKey());
        entityManager.persist(sic);
        entityManager.flush();
      }
      result.setSuccess(true);
    } catch (Exception e) {
      LOG.error("Error in saving SIC", e);
      result.setSuccess(false);
      result.setMessage("An unexpected error occurred during the execution. Please try again later.");
    }
    return result;
  }

  /**
   * Gets the current max REFT_UNSIC_KEY
   * 
   * @param entityManager
   * @return
   */
  private int getMaxKey(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("CODE.ISIC.GET_MAX_KEY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    return query.getSingleResult(Integer.class);
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

}
