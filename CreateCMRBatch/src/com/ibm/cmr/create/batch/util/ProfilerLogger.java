/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Logging to profile the CMR batches
 * 
 * @author clint
 *
 */
public class ProfilerLogger {

  public static final Logger LOG = LogManager.getLogger("batch-profiler");

  public static void logProcessTime(long duration, String log) {
    StringBuilder sb = new StringBuilder();

    LOG.debug(sb.toString());
  }
}
