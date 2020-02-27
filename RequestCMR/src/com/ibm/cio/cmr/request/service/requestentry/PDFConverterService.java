/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.pdf.RequestToPDFConverter;

/**
 * @author JeffZAMORA
 * 
 */
@Component
public class PDFConverterService extends BaseSimpleService<Boolean> {

  @Override
  protected Boolean doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    Long reqId = (Long) params.getParam("reqId");
    HttpServletResponse response = (HttpServletResponse) params.getParam("response");
    if (reqId == null || reqId <= 0) {
      return false;
    }
    return RequestToPDFConverter.exportToPdf(entityManager, reqId, response.getOutputStream());
  }

}
