package com.ibm.cmr.create.batch.util.isuload;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class EBCDICFileReader {

  public static void main(String[] args) throws SQLException, IOException {
    if (args.length < 2) {
      System.out.println("inputfile and sample size must be specified as arguments: exiting..");
      System.exit(-1);
    }
    System.out.println("Starting..");
    String infile = args[0];

    File file = new File(infile);

    // use second ar arg as a loop limit for testing
    int debugMax = 0;
    debugMax = Integer.parseInt(args[1]);
    byte[] fileData = new byte[700];
    DataInputStream dis = null;
    String outString = "";
    int len = 700;
    int off = 0;

    try {
      dis = new DataInputStream(new FileInputStream(file));
      int bytesRead = 0;
      boolean debugflag = true;
      int counter = 0;
      while (bytesRead != -1 && debugflag) {
        bytesRead = dis.read(fileData, off, len);
        outString = convertFromEBCDIC(fileData);
        // cmr number byte data at position 167-170 (array index 166-169)
        String cmrNumber = ISULoadUtility.extractCMRNumber(fileData, 166, 169);

        outString = outString + "cmr:" + cmrNumber + "\n";
        System.out.println(outString);
        counter++;
        if (counter == debugMax)
          debugflag = false;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      dis.close();

    }

    System.out.println("done..");

  }

  private static String convertFromEBCDIC(byte[] fileData) {
    StringBuilder fileString = new StringBuilder();

    String avalue = null;
    for (int index = 0; index < fileData.length; index++) {
      avalue = ISULoadUtility.convertEBCDIC(new Byte(fileData[index]));
      if (avalue != null) {
        fileString.append(avalue.charAt(0));
      } else {
        fileString.append(' ');
      }
    }

    return fileString.toString();
  }

}
