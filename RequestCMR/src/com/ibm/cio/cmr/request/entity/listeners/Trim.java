package com.ibm.cio.cmr.request.entity.listeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote the value has to be trimmed.
 * 
 * @author Jeffrey Zamora
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Trim {

  // only a placeholder
}
