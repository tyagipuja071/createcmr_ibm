/**
 * 
 */
package com.ibm.cio.cmr.request.automation.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;

/**
 * @author JeffZAMORA
 * 
 */
public class AutomationElementContainer implements Comparable<AutomationElementContainer> {

  private String processCd;
  private String processDesc;
  private String processType;
  private boolean nonImportable;

  private static List<AutomationElementContainer> currentElements;

  /**
   * Gets the list of {@link AutomationElement} processes currently defined
   * 
   * @return
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   */
  @SuppressWarnings("unchecked")
  public static synchronized List<AutomationElementContainer> getDefinedElements() throws NoSuchMethodException, SecurityException,
      InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (currentElements == null || currentElements.isEmpty()) {
      currentElements = new ArrayList<AutomationElementContainer>();
      AutomationElementRegistry registry = AutomationElementRegistry.getInstance();
      for (String key : registry.keySet()) {
        Class<? extends AutomationElement<?>> elementClass = registry.get(key);
        Constructor<AutomationElement<?>> constructor = (Constructor<AutomationElement<?>>) elementClass.getConstructor(String.class, String.class,
            boolean.class, boolean.class);
        AutomationElement<?> elem = constructor.newInstance("CU", null, false, false);

        AutomationElementContainer cont = new AutomationElementContainer();
        cont.processCd = elem.getProcessCode();
        cont.processDesc = elem.getProcessDesc();
        cont.processType = elem.getProcessType().toCode();
        cont.nonImportable = elem.isNonImportable();
        currentElements.add(cont);
        Collections.sort(currentElements);
      }
    }
    return currentElements;
  }

  public static void refresh() {
    if (currentElements != null)
      currentElements.clear();
  }

  @Override
  public int compareTo(AutomationElementContainer o) {
    if (this.processCd.startsWith("GBL") && !o.processCd.startsWith("GBL")) {
      return -1;
    }
    if (!this.processCd.startsWith("GBL") && o.processCd.startsWith("GBL")) {
      return 1;
    }
    return this.processCd.compareTo(o.processCd);
  }

  public String getProcessCd() {
    return processCd;
  }

  public void setProcessCd(String processCd) {
    this.processCd = processCd;
  }

  public String getProcessDesc() {
    return processDesc;
  }

  public void setProcessDesc(String processDesc) {
    this.processDesc = processDesc;
  }

  public String getProcessType() {
    return processType;
  }

  public void setProcessType(String processType) {
    this.processType = processType;
  }

  public boolean isNonImportable() {
    return nonImportable;
  }

  public void setNonImportable(boolean nonImportable) {
    this.nonImportable = nonImportable;
  }

}
