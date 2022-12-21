/**
 * 
 * 
 */
package com.ibm.cio.cmr.request.util.approval;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.ProlifChecklist;

/**
 * @author Jeffrey Zamora
 * 
 */
public class ChecklistUtil {

  private static final Logger LOG = Logger.getLogger(ChecklistUtil.class);

  public static enum ChecklistResponse {
    Yes, No, Any, Responded
  }

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("checklist");

  public static List<ChecklistItem> getItems(ProlifChecklist checklist, String sysLoc, ChecklistResponse response)
      throws IllegalArgumentException, IllegalAccessException {
    List<ChecklistItem> items = new ArrayList<ChecklistItem>();

    String answer = checklist.getUsDplSanctioned();
    ChecklistItem item = null;
    if (shouldAdd(response, answer)) {
      item = getItem(sysLoc, "dpl");
      item.setAnswer(answer);
      items.add(item);
    }
    answer = checklist.getPotentialMatch();
    if (shouldAdd(response, answer)) {
      item = getItem(sysLoc, "match");
      item.setAnswer(answer);
      items.add(item);
    }

    String section = null;
    for (Field field : ProlifChecklist.class.getDeclaredFields()) {
      if (field.getName().startsWith("section")) {
        field.setAccessible(true);
        answer = (String) field.get(checklist);
        section = field.getName().substring(7);
        if (shouldAdd(response, answer)) {
          item = getItem(sysLoc, section);
          if (item != null) {
            item.setLabel("Section " + section + ": " + item.getLabel());
            item.setAnswer(answer);
            items.add(item);
          }

        }
      }
    }
    return items;
  }

  private static boolean shouldAdd(ChecklistResponse response, String answer) {
    switch (response) {
    case Any:
      return true;
    case Yes:
      return "Y".equals(answer);
    case No:
      return "N".equals(answer);
    case Responded:
      return !StringUtils.isEmpty(answer);
    default:
      return false;
    }
  }

  private static ChecklistItem getItem(String sysLoc, String section) {
    ChecklistItem item = new ChecklistItem();
    try {
      if (BUNDLE.containsKey("chk." + sysLoc + "." + section)) {
        item.setLabel(BUNDLE.getString("chk." + sysLoc + "." + section));
      } else if (BUNDLE.containsKey("chk.CEMEA." + section)) {
        if (!"641".equals(sysLoc) && !"736".equals(sysLoc) && !"738".equals(sysLoc)) {
          LOG.warn("Warning: Element for " + sysLoc + " missing in the " + BUNDLE + ".properties file. Using CEMEA" + section);
          // TODO fix this later
          item.setLabel(BUNDLE.getString("chk.CEMEA." + section));
        }
      }
      return item;
    } catch (MissingResourceException e) {
      LOG.error("Element for " + sysLoc + " missing in the " + BUNDLE + ".properties file.");
      e.printStackTrace();
      return null;
    }
  }
}
