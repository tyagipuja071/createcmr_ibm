package com.ibm.cmr.create.batch.util.isuload;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Utility for the ISU Loader application
 * 
 * @author David Partow
 * 
 */
public class ISULoadUtility {

  private static HashMap<Byte, String> EBCDICMap = null;

  public static String extractCMRNumber(byte[] fileData, int startIndex, int endIndex) {

    byte[] arr = new byte[4];
    int arrindex = 0;
    for (int index = startIndex; index <= endIndex; index++) {
      arr[arrindex] = fileData[index];
      arrindex++;
    }
    long num = byteToInt(arr, 4);
    String cmrNumber = Long.toString(num);
    // pad the cmrNumber with leading 0s to get to 7 chars
    if (cmrNumber.length() < 7) {
      for (int index = cmrNumber.length(); index < 7; index++) {
        cmrNumber = "0" + cmrNumber;
      }
    }
    // System.out.println(fileString);
    return cmrNumber;
  }

  public static long byteToInt(byte[] bytes, int length) {
    int val = 0;
    if (length > 4)
      throw new RuntimeException("Too big to fit in int");
    for (int i = 0; i < length; i++) {
      val = val << 8;
      val = val | (bytes[i] & 0xFF);
    }
    return val;
  }

  public static String convertEBCDIC(Byte inbyte) {
    if (EBCDICMap == null)
      InitializeEMap();
    return EBCDICMap.get(inbyte);
  }

  public static byte getEBCDICbyteForString(String instring) {
    if (EBCDICMap == null)
      InitializeEMap();
    Byte akey = getKeyByValue(EBCDICMap, instring);
    if (akey != null)
      return akey.byteValue();
    else
      return 0;
  }

  public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
    for (Entry<T, E> entry : map.entrySet()) {
      if (Objects.equals(value, entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

  private static void InitializeEMap() {
    EBCDICMap = new HashMap<Byte, String>();
    EBCDICMap.put(new Byte((byte) 0x40), " ");
    EBCDICMap.put(new Byte((byte) 0x4A), "¢");
    EBCDICMap.put(new Byte((byte) 0x4B), ".");
    EBCDICMap.put(new Byte((byte) 0x4C), "<");
    EBCDICMap.put(new Byte((byte) 0x4D), "(");
    EBCDICMap.put(new Byte((byte) 0x4E), "+");
    EBCDICMap.put(new Byte((byte) 0x4F), "|");
    EBCDICMap.put(new Byte((byte) 0x50), "&");
    EBCDICMap.put(new Byte((byte) 0x5A), "!");
    EBCDICMap.put(new Byte((byte) 0x5B), "$");
    EBCDICMap.put(new Byte((byte) 0x5C), "*");
    EBCDICMap.put(new Byte((byte) 0x5D), ")");
    EBCDICMap.put(new Byte((byte) 0x5E), ";");
    EBCDICMap.put(new Byte((byte) 0x5F), "?");
    EBCDICMap.put(new Byte((byte) 0x60), "_");
    EBCDICMap.put(new Byte((byte) 0x61), "/");
    EBCDICMap.put(new Byte((byte) 0x6B), "‘");
    EBCDICMap.put(new Byte((byte) 0x6D), "-");
    EBCDICMap.put(new Byte((byte) 0x6E), ">");
    EBCDICMap.put(new Byte((byte) 0x6F), "?");
    EBCDICMap.put(new Byte((byte) 0x7A), ":");
    EBCDICMap.put(new Byte((byte) 0x7B), "#");
    EBCDICMap.put(new Byte((byte) 0x7C), "@");
    EBCDICMap.put(new Byte((byte) 0x7D), "'");
    EBCDICMap.put(new Byte((byte) 0x7E), "=");
    EBCDICMap.put(new Byte((byte) 0x7F), "\"");
    EBCDICMap.put(new Byte((byte) 0x81), "a");
    EBCDICMap.put(new Byte((byte) 0x82), "b");
    EBCDICMap.put(new Byte((byte) 0x83), "c");
    EBCDICMap.put(new Byte((byte) 0x84), "d");
    EBCDICMap.put(new Byte((byte) 0x85), "e");
    EBCDICMap.put(new Byte((byte) 0x86), "f");
    EBCDICMap.put(new Byte((byte) 0x87), "g");
    EBCDICMap.put(new Byte((byte) 0x88), "h");
    EBCDICMap.put(new Byte((byte) 0x89), "i");
    EBCDICMap.put(new Byte((byte) 0x91), "j");
    EBCDICMap.put(new Byte((byte) 0x92), "k");
    EBCDICMap.put(new Byte((byte) 0x93), "l");
    EBCDICMap.put(new Byte((byte) 0x94), "m");
    EBCDICMap.put(new Byte((byte) 0x95), "n");
    EBCDICMap.put(new Byte((byte) 0x96), "o");
    EBCDICMap.put(new Byte((byte) 0x97), "p");
    EBCDICMap.put(new Byte((byte) 0x98), "q");
    EBCDICMap.put(new Byte((byte) 0x99), "r");
    EBCDICMap.put(new Byte((byte) 0xA1), "8");
    EBCDICMap.put(new Byte((byte) 0xA2), "s");
    EBCDICMap.put(new Byte((byte) 0xA3), "t");
    EBCDICMap.put(new Byte((byte) 0xA4), "u");
    EBCDICMap.put(new Byte((byte) 0xA5), "v");
    EBCDICMap.put(new Byte((byte) 0xA6), "w");
    EBCDICMap.put(new Byte((byte) 0xA7), "x");
    EBCDICMap.put(new Byte((byte) 0xA8), "y");
    EBCDICMap.put(new Byte((byte) 0xA9), "z");
    EBCDICMap.put(new Byte((byte) 0xC1), "A");
    EBCDICMap.put(new Byte((byte) 0xC2), "B");
    EBCDICMap.put(new Byte((byte) 0xC3), "C");
    EBCDICMap.put(new Byte((byte) 0xC4), "D");
    EBCDICMap.put(new Byte((byte) 0xC5), "E");
    EBCDICMap.put(new Byte((byte) 0xC6), "F");
    EBCDICMap.put(new Byte((byte) 0xC7), "G");
    EBCDICMap.put(new Byte((byte) 0xC8), "H");
    EBCDICMap.put(new Byte((byte) 0xC9), "I");
    EBCDICMap.put(new Byte((byte) 0xD1), "J");
    EBCDICMap.put(new Byte((byte) 0xD2), "K");
    EBCDICMap.put(new Byte((byte) 0xD3), "L");
    EBCDICMap.put(new Byte((byte) 0xD4), "M");
    EBCDICMap.put(new Byte((byte) 0xD5), "N");
    EBCDICMap.put(new Byte((byte) 0xD6), "O");
    EBCDICMap.put(new Byte((byte) 0xD7), "P");
    EBCDICMap.put(new Byte((byte) 0xD8), "Q");
    EBCDICMap.put(new Byte((byte) 0xD9), "R");
    EBCDICMap.put(new Byte((byte) 0xE0), "\\");
    EBCDICMap.put(new Byte((byte) 0xE2), "S");
    EBCDICMap.put(new Byte((byte) 0xE3), "T");
    EBCDICMap.put(new Byte((byte) 0xE4), "U");
    EBCDICMap.put(new Byte((byte) 0xE5), "V");
    EBCDICMap.put(new Byte((byte) 0xE6), "W");
    EBCDICMap.put(new Byte((byte) 0xE7), "X");
    EBCDICMap.put(new Byte((byte) 0xE8), "Y");
    EBCDICMap.put(new Byte((byte) 0xE9), "Z");
    EBCDICMap.put(new Byte((byte) 0xF0), "0");
    EBCDICMap.put(new Byte((byte) 0xF1), "1");
    EBCDICMap.put(new Byte((byte) 0xF2), "2");
    EBCDICMap.put(new Byte((byte) 0xF3), "3");
    EBCDICMap.put(new Byte((byte) 0xF4), "4");
    EBCDICMap.put(new Byte((byte) 0xF5), "5");
    EBCDICMap.put(new Byte((byte) 0xF6), "6");
    EBCDICMap.put(new Byte((byte) 0xF7), "7");
    EBCDICMap.put(new Byte((byte) 0xF8), "8");
    EBCDICMap.put(new Byte((byte) 0xF9), "9");
  }

}
