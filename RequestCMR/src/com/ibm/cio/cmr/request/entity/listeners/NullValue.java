/**
 * 
 */
package com.ibm.cio.cmr.request.entity.listeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields that need a value when the value being set is NULL by
 * the system
 * 
 * @author Jeffrey Zamora
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NullValue {

  public String value();
}
