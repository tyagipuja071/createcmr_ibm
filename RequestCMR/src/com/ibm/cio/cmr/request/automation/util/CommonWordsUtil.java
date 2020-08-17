/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;

/**
 * Utility to handle common words and their replacement
 * 
 * @author JeffZAMORA
 * 
 */
public class CommonWordsUtil {

  private static Map<String, String> equalWords = new HashMap<String, String>();
  private static Map<String, String> oneWayWords = new HashMap<String, String>();
  private static List<String> irrelevant = new ArrayList<>();

  private static List<String> commonPrepositions = new ArrayList<>();

  static {
    addEqual("acad", "academy", "academic");
    addEqual("amer", "america", "american");
    addEqual(true, "apt", "apartment");
    addEqual(true, "assn", "association");
    addEqual("ave", "avenue");
    addEqual("bch", "beach");
    addEqual("bldg", "building");
    addEqual("blvd", "boulevard");
    addEqual("br", "branch");
    addEqual("byp", "bypass");
    addEqual("cir", "circle");
    addEqual(true, "co", "company");
    addEqual("coll", "college");
    addOneWay("comm", "community", "committee", "commission", "communication");
    addEqual(true, "corp", "corporation");
    addEqual("ct", "court");
    addEqual("ctr", "center");
    addEqual("cswy", "causeway");
    addEqual("dept", "department");
    addEqual("dist", "district");
    addEqual("div", "division");
    addEqual("dr", "drive");
    addEqual("e", "east");
    addEqual("east", "eastern");
    addEqual("expy", "expressway");
    addEqual("fwy", "freeway");
    addEqual("govt", "government");
    addEqual("hbr", "harbor");
    addEqual("hosp", "hospital");
    addEqual("hwy", "highway");
    addEqual("i", "interstate");
    addEqual("info", "information");
    addEqual("intl", "international");
    addEqual(true, "inc", "incorporated");
    addEqual("inst", "institute", "institution");
    addEqual("jct", "junction");
    addEqual(true, "ltd", "limited");
    addEqual("mt", "mtn", "mount", "mountain");
    addEqual("n", "north");
    addEqual("natl", "national");
    addEqual("ne", "northeast");
    addEqual("no", "number");
    addEqual("nw", "northwest");
    addEqual("ofc", "office");
    addEqual(true, "org", "organization");
    addEqual("pkwy", "parkway");
    addEqual("pt", "point");
    addEqual("rd", "road");
    addEqual("rt", "rte", "route");
    addEqual("s", "south");
    addEqual("se", "southeast");
    addEqual("sq", "square");
    addOneWay("st", "saint", "street", "state");
    addEqual("ste", "suite");
    addEqual("sw", "southwest");
    addEqual("tpke", "turnpike");
    addEqual("univ", "university");
    addEqual("w", "west");
    addEqual("cyn", " canyon");
    addEqual("ft", "fort");
    addEqual("1st", "first");
    addEqual("2nd", "second");
    addEqual("3rd", "third");
    addEqual("4th", "fourth");
    addEqual("5th", "fifth");
    addEqual("6th", "sixth");
    addEqual("7th", "seventh");
    addEqual("8th", "eighth");
    addEqual("9th", "ninth");
    addEqual("10th", "tenth");
    addEqual("bhd", "berhad");
    addEqual("1", "one");
    addEqual("2", "two");
    addEqual("3", "three");
    addEqual("4", "four");
    addEqual("5", "five");
    addEqual("6", "six");
    addEqual("7", "seven");
    addEqual("8", "eight");
    addEqual("9", "nine");
    addEqual("0", "zero");
    addEqual("mfg", "manufacturing");

    addIrrelevant("llc", "gmbh");

    commonPrepositions.add("OF");
    commonPrepositions.add("WITH");
    commonPrepositions.add("A");
    commonPrepositions.add("FOR");
    commonPrepositions.add("TO");
    commonPrepositions.add("IN");
  }

  /**
   * Adds words of equal weight. These words are interchangeable
   * 
   * @param words
   */
  private static void addEqual(String... words) {
    addEqual(false, words);
  }

  /**
   * Adds irrelevant words, used for the {@link #minimize(String)} function
   * 
   * @param words
   */
  private static void addIrrelevant(String... words) {
    for (String word : words) {
      irrelevant.add(word.toUpperCase());
    }
  }

  /**
   * Adds words of equal weight. These words are interchangeable
   * 
   * @param words
   */
  private static void addEqual(boolean irrelevantWord, String... words) {
    for (String word : words) {
      equalWords.put(word.toUpperCase(), createList(words));
      if (irrelevantWord) {
        irrelevant.add(word.toUpperCase());
      }
    }
  }

  /**
   * Adds word mapped to replacements. The words in the replacement cannot
   * replace each other (left to right only) *
   * 
   * @param word
   * @param replacements
   */
  private static void addOneWay(String word, String... replacements) {
    addOneWay(false, word, replacements);
  }

  /**
   * Adds word mapped to replacements. The words in the replacement cannot
   * replace each other (left to right only) *
   * 
   * @param word
   * @param replacements
   */
  private static void addOneWay(boolean irrelevantWord, String word, String... replacements) {
    oneWayWords.put(word.toUpperCase(), createList(replacements));
    if (irrelevantWord) {
      for (String rword : replacements) {
        irrelevant.add(rword.toUpperCase());
      }
    }
  }

  public static void main(String[] args) {
    String[] words = { "BANK OF AMERICA NATIONAL,LLC", "JP MORGAN CHASE BANK" };
    for (String word : words) {
      List<String> variations = getVariations(word);
      for (String var : variations) {
        System.out.println(" - " + var);
      }

      System.out.println("Relevant: " + minimize(word));
      System.out.println("Like: " + getMaskedForLikeSearch(minimize(word)));
      System.out.println("Min Like: " + getMaskedForLikeSearchFirstWords(minimize(word)));
    }
  }

  /**
   * Returns only relevant key words on this phrase based on the common words
   * registered on this utility. The method removes common words from the phrase
   * and returns a meaningful searching 'key'
   * 
   * @param phrase
   * @return
   */
  public static String minimize(String phrase) {
    String reducedPhrase = phrase.replaceAll("[^A-Za-z0-9-]", " ");
    reducedPhrase = reducedPhrase.replaceAll("  ", " ");
    StringBuilder sb = new StringBuilder();

    boolean hasRelevant = false;
    for (String part : reducedPhrase.toUpperCase().split(" ")) {
      if (irrelevant.contains(part)) {
        if (hasRelevant) {
          break;
        } else {
          // hasRelevant = true;
          // sb.append(sb.length() > 0 ? " " : "");
          // sb.append(part);
        }
      } else {
        hasRelevant = true;
        sb.append(sb.length() > 0 ? " " : "");
        sb.append(part);
      }
    }

    return sb.toString();
  }

  public static String getMaskedForLikeSearch(String phrase) {
    StringBuilder sb = new StringBuilder();

    for (String part : phrase.toUpperCase().split(" ")) {
      if (commonPrepositions.contains(part)) {
        sb.append("%");
      } else {
        sb.append(sb.length() > 0 ? " " : "");
        sb.append(part);
      }
    }
    String masked = sb.toString();
    masked = StringUtils.replace(masked, "%%", "%");
    masked = StringUtils.replace(masked, " % ", "%");
    masked = StringUtils.replace(masked, "% ", "%");
    masked = StringUtils.replace(masked, " %", "%");
    return masked;
  }

  public static String getMaskedForLikeSearchFirstWords(String phrase) {
    String[] parts = phrase.split(" ");
    if (parts.length > 3) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i <= 2; i++) {
        sb.append(sb.length() > 0 ? " " : "");
        sb.append(parts[i]);
      }
      return getMaskedForLikeSearch(sb.toString()).replaceAll(" ", "%");
    }
    return getMaskedForLikeSearch(phrase).replace(" ", "%");
  }

  /**
   * Returns a list of variations of the phrase based on the common words
   * registered on this utility
   * 
   * @param phrase
   * @return
   */
  public static List<String> getVariations(String phrase) {
    String reducedPhrase = phrase.replaceAll("[^A-Za-z0-9-]", " ");
    reducedPhrase = reducedPhrase.replaceAll("  ", " ");
    boolean replaced = false;
    List<String> variations = new ArrayList<String>();

    Queue<String> currentQueue = new LinkedList<>();
    List<String> tempQueue = new ArrayList<>();
    currentQueue.add(reducedPhrase.toUpperCase());

    do {
      replaced = false;
      for (String currPhrase : currentQueue) {
        tempQueue.clear();
        String[] parts = currPhrase.split(" ");
        for (String part : parts) {
          if (equalWords.containsKey(part)) {
            String replacements = equalWords.get(part);
            for (String replacement : replacements.split(",")) {
              if (!part.equals(replacement)) {
                String toReplace = StringUtils.replace(currPhrase, part, replacement);
                if (!variations.contains(toReplace)) {
                  tempQueue.add(toReplace);
                  variations.add(toReplace);
                  replaced = true;
                }
              }
            }
          } else if (oneWayWords.containsKey(part)) {
            String replacements = oneWayWords.get(part);
            for (String replacement : replacements.split(",")) {
              if (!part.equals(replacement)) {
                String toReplace = StringUtils.replace(currPhrase, part, replacement);
                if (!variations.contains(toReplace)) {
                  variations.add(toReplace);
                  replaced = true;
                }
              }
            }
          }
        }
      }
      currentQueue.clear();
      if (!tempQueue.isEmpty()) {
        currentQueue.addAll(tempQueue);
      }

    } while (replaced);

    return variations;
  }

  private static String createList(String... words) {
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      sb.append(sb.length() > 0 ? "," : "");
      sb.append(word.toUpperCase());
    }
    return sb.toString();
  }

}
