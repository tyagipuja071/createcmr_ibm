package com.ibm.cio.cmr.request.service.changelog;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

@Component
public class ChangeLogExportFullReportRDcService extends BaseSimpleService<Object> {

  @Override
  protected Object doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String sql = ExternalizedQuery.getSql("CHANGELOG.GET_RDC_BY_KATR6_AND_ZZKV_CUSNO");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KATR6", params.getParam("KATR6"));
    query.setParameter("ZZKV_CUSNO", params.getParam("ZZKV_CUSNO"));
    query.setForReadOnly(true);

    List<Object[]> list = query.getResults();

    return list;
  }

}
