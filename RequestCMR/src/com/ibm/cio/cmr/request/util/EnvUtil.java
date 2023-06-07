package com.ibm.cio.cmr.request.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author 136786PH1
 *
 */
public class EnvUtil {

  private static final Logger LOG = Logger.getLogger(EnvUtil.class);

  /**
   * Injects values retrieved from Environment Variables in the format
   * <code>${variable}</code>. For example, if the value of the given field is
   * in the variable format, the value is replaced with the System.env or
   * System.getProperty equivalent. This function only replaces
   * <code>String</code> values.
   * 
   * @param target
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public static void injectEnvVariables(Object target) throws IllegalArgumentException, IllegalAccessException {
    for (Field field : target.getClass().getDeclaredFields()) {
      if (String.class.equals(field.getType()) && !Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
        field.setAccessible(true);
        String value = (String) field.get(target);
        if (!StringUtils.isBlank(value) && value.startsWith("${")) {
          // found in the ${} format
          String key = value.replaceAll("[${}]", "");
          String prop = System.getProperty(key);
          if (StringUtils.isBlank(prop)) {
            prop = System.getenv(key);
          }
          if (!StringUtils.isBlank(prop)) {
            LOG.debug("System value found for " + key + " = " + prop);
            System.out.println("System value found for " + key + " = " + prop);
            field.set(target, prop);
          }
        }
      }
    }
  }
}