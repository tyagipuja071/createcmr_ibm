/**
 * 
 */
package com.ibm.cio.cmr.request.util.pdf;

import java.io.IOException;
import java.io.OutputStream;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;

/**
 * @author Jeffrey Zamora
 * 
 */
public interface PDFConverter {

  public boolean exportToPdf(EntityManager entityManager, Admin admin, Data data, OutputStream os, String sysLoc) throws IOException;
}
