package com.ibm.scheduler.interfaces;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

public interface ProcessStarter {

  public default void runProcess(String command, String processName) {
    try {
      ProcessBuilder pb = new ProcessBuilder(command.split(" "));
      pb.redirectOutput(Redirect.INHERIT);
      pb.redirectError(Redirect.INHERIT);
      pb.start();
      pb.wait();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
