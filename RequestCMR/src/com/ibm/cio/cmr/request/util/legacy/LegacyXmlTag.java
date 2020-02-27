/**
 * 
 */
package com.ibm.cio.cmr.request.util.legacy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote the tag in the current SOF XML Queries for a particular
 * field
 * 
 * @author JeffZAMORA
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LegacyXmlTag {

  /**
   * The TagName denoted by this field in the current SOF Query XMLs
   * 
   * @return
   */
  public String value() default "";

  public String[] tags() default {};
}
