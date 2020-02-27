/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.json.java.JSONObject;

/**
 * @author Jeffrey Zamora
 * 
 */
public class DebugUtil {

  public static void printObjectAsJson(Logger logger, Object object) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = mapper.writeValueAsString(object);
      logger.debug(mapper.defaultPrettyPrintingWriter().writeValueAsString(JSONObject.parse(jsonString)));
    } catch (Exception e) {
      logger.warn("Cannot print object as json.");
    }
  }
}
