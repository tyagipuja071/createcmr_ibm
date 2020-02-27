/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.ISULoaderService;

/**
 * @author Jeffrey Zamora
 * 
 */
public class ISULoaderEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    if (args.length < 2) {
      System.out.println("input and output files (params 1 and 2) must be specified as arguments: exiting..");
      System.exit(-1);
    }
    String infile = args[0];
    String outfile = args[1];

    BatchEntryPoint.initContext("ISULoader");

    ISULoaderService service = new ISULoaderService(infile, outfile);
    service.execute();

  }
}
