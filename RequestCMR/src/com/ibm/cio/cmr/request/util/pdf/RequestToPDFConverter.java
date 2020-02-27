package com.ibm.cio.cmr.request.util.pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.listener.CmrContextListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.pdf.impl.DefaultPDFConverter;

/**
 * Generates PDF files for a <strong>single-request</strong> type
 * (Create,Update)
 * 
 * @author Jeffrey Zamora
 * 
 */
public class RequestToPDFConverter {

  private static final Logger LOG = Logger.getLogger(RequestToPDFConverter.class);

  public static void main(String[] args) throws IOException {
    CmrContextListener.startCMRContext();
    System.setProperty("javax.net.ssl.keyStore", "c:/workspace/createcmr/batch/store/cmma.keystore");
    System.setProperty("javax.net.ssl.trustStore", "c:/workspace/createcmr/batch/store/cmma.keystore");
    System.setProperty("javax.net.ssl.keyStorePassword", "cmma123");

    EntityManager em = JpaManager.getEntityManager();
    try {
      try (FileOutputStream fos = new FileOutputStream("C:\\Users\\JeffZAMORA\\Downloads\\test.pdf")) {
        RequestToPDFConverter.exportToPdf(em, 19468, fos);
      }
    } finally {
      em.close();
    }
  }

  private RequestToPDFConverter() {
    // no constructor
  }

  /**
   * Exports the request details into a PDF file written to the supplied
   * outputstream. Only Create and Update types are supported
   * 
   * @param entityManager
   * @param reqId
   * @param os
   * @return
   * @throws IOException
   */
  public static synchronized boolean exportToPdf(EntityManager entityManager, long reqId, OutputStream os) throws IOException {
    LOG.debug("Retrieving Details for Request ID " + reqId);
    CompoundEntity compound = getRequestDetails(entityManager, reqId);
    if (compound == null) {
      LOG.debug("Details for Request ID " + reqId + " cannot be obtained.");
      return false;
    }
    Admin admin = compound.getEntity(Admin.class);
    Data data = compound.getEntity(Data.class);
    String sysLoc = (String) compound.getValue("SYS_LOC");
    if (!Arrays.asList("CREATE", "UPDATE", "REACTIVATE", "DELETE").contains(admin.getReqType().toUpperCase())) {
      LOG.debug("Request Type not supported for PDF generation.");
      return false;
    }
    PDFConverter converter = getConverter(sysLoc);
    return converter.exportToPdf(entityManager, admin, data, os, sysLoc);
  }

  /**
   * Retrieves the main request details
   * 
   * @param entityManager
   * @param reqId
   * @return
   */
  protected static CompoundEntity getRequestDetails(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("PDF.GETDETAILS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("REQ_ID", reqId);
    List<CompoundEntity> results = query.getCompundResults(1, Admin.class, Admin.PDF_MAPPING);
    if (results != null && results.size() > 0) {
      return results.get(0);
    }
    return null;
  }

  private static PDFConverter getConverter(String cmrIssuingCntry) throws IOException {
    try {
      String className = SystemConfiguration.getSystemProperty("pdfconverter." + cmrIssuingCntry);
      Constructor<?> constructor = Class.forName(className).getConstructor(String.class);
      PDFConverter converter = (PDFConverter) constructor.newInstance(cmrIssuingCntry);
      LOG.debug("Using converter class " + converter.getClass().getSimpleName());
      return converter;
    } catch (Exception e) {
      LOG.warn("Converter not defined for " + cmrIssuingCntry + ", using default");
      return new DefaultPDFConverter(cmrIssuingCntry);
    }
  }
}
