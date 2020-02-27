/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cmr.create.batch.service.MQInterfaceService;
import com.ibm.cmr.create.batch.service.MQInterfaceServiceDev;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MQServiceEntryPoint extends BatchEntryPoint {

  public static void main(String[] args) throws CmrException {
    if (args.length < 1) {
      System.out.println("MQ type(params) must be specified as arguments (put or get): exiting..");
      System.exit(-1);
    }
    String type = args[0]; // get or put

    String mode = null;
    if (type != null && type.trim().equalsIgnoreCase("put")) {
      BatchEntryPoint.initContext("MQPublish");
      mode = MQInterfaceService.PUBLISH;
    } else if (type != null && type.trim().equalsIgnoreCase("get")) {
      BatchEntryPoint.initContext("MQSubscribe");
      mode = MQInterfaceService.SUBSCRIBE;
    }

    MQInterfaceService service = null;
    if (args.length > 1 && "DEV".equals(args[1])) {
      service = new MQInterfaceServiceDev();
      if (args.length > 2 && StringUtils.isNumeric(args[2])) {
        ((MQInterfaceServiceDev) service).setReqId(Long.parseLong(args[2]));
      }
    } else {
      service = new MQInterfaceService();
    }
    service.setMode(mode);
    service.execute();
  }
}
