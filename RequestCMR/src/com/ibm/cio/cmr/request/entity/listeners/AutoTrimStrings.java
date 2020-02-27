package com.ibm.cio.cmr.request.entity.listeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for entities with string values that will automatically be trimmed
 * 
 * @author Jeffrey Zamora
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoTrimStrings {

  // only a place holder
}
