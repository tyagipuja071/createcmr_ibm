package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.MachinesToInstall;
import com.ibm.cio.cmr.request.entity.MachinesToInstallPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.MachineModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Rangoli Saxena
 * 
 */
@Component
public class MachineService extends BaseService<MachineModel, MachinesToInstall> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(MachineService.class);
  }

  @Override
  protected void performTransaction(MachineModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {

  }

  @Override
  protected List<MachineModel> doSearch(MachineModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    List<MachineModel> results = new ArrayList<MachineModel>();

    String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_MACHINES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", model.getReqId());
    query.setParameter("ADDR_TYPE", model.getAddrType());
    query.setParameter("ADDR_SEQ", model.getAddrSeq());
    query.setForReadOnly(true);

    List<MachinesToInstall> machines = query.getResults(MachinesToInstall.class);

    MachineModel machineModel = null;
    for (MachinesToInstall machine : machines) {
      machineModel = new MachineModel();
      copyValuesFromEntity(machine, machineModel);
      machineModel.setState(BaseModel.STATE_EXISTING);
      results.add(machineModel);
    }
    return results;
  }

  @Override
  protected MachinesToInstall getCurrentRecord(MachineModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected MachinesToInstall createFromModel(MachineModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    MachinesToInstall machine = new MachinesToInstall();
    MachinesToInstallPK pk = new MachinesToInstallPK();
    machine.setId(pk);
    copyValuesToEntity(model, machine);
    return machine;
  }

}