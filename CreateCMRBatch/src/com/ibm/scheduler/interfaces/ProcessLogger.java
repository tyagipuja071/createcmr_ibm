package com.ibm.scheduler.interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public interface ProcessLogger {

  public default void logProcessOutputToConsole(Process process, String className) throws IOException {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      while (true) {
        String line = in.readLine();
        if (line == null)
          break;
        System.out.println(line);
      }
    }
    try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      while (true) {
        String line = in.readLine();
        if (line == null)
          break;
        System.err.println(line);
      }
    }
  }
}
