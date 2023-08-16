package com.ibm.scheduler.interfaces;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import org.apache.logging.log4j.ThreadContext;

public interface ProcessStarter {

  public default void runProcess(String command, String processName) {
    ThreadContext.put("PROCESS_NAME", processName);
    try {
      ProcessBuilder pb = new ProcessBuilder(command.split(" "));
      pb.redirectOutput(Redirect.INHERIT);
      pb.redirectError(Redirect.INHERIT);
      Process start = pb.start();
      start.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
