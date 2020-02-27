package com.ibm.cio.cmr.request.entity.listeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ibm.cio.cmr.request.entity.Admin;

/**
 * Annotation to add for entities that implement the changelog listener
 * 
 * @author Jeffrey Zamora
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ChangeLogDetails {

  public static String ANONYMOUS = "anonymous";

  /**
   * The field name on the entity that contains the User ID field. Should only
   * be used when childTable = false
   * 
   * @return
   */
  public String userId() default ANONYMOUS;

  /**
   * The field name on the entity that contains the Request ID field. Required
   * for all entities
   * 
   * @return
   */
  public String reqId();

  /**
   * The field name on the entity that contains the Request Status field. Should
   * only be used when childTable = false
   * 
   * @return
   */
  public String reqStatus() default "???";

  /**
   * Denotes that the entity is a child of {@link Admin} and should have a
   * Request ID link
   * 
   * @return
   */
  public boolean childTable() default false;

  /**
   * Denotes that this entity is an address table
   * 
   * @return
   */
  public boolean addressTable() default false;

  /**
   * The field name on the entity that contains the Address Sequence field.
   * Should only be used when addressTable = true
   * 
   * @return
   */
  public String addressSeq() default "";

  /**
   * The field name on the entity that contains the Address Type field. Should
   * only be used when addressTable = true
   * 
   * @return
   */
  public String addressType() default "";

  /**
   * Instructs the logger to log insert actions on the entity
   * 
   * @return
   */
  public boolean logInserts() default true;

  /**
   * Instructs the logger to log update actions on the entity
   * 
   * @return
   */
  public boolean logUpdates() default true;

  /**
   * Instructs the logger to log delete actions on the entity
   * 
   * @return
   */
  public boolean logDeletes() default true;

  /**
   * Toggles the logging on the field depending on the parent {@link Admin}
   * record's Request Type
   * 
   * @return
   */
  public String[] logForRequestType() default {};

  /**
   * For insert logs, the field name on the entity to be used when logging
   * insert records. By default insert records have field = empty. Specifying a
   * field here will log the corresponding DB field name and the entity's value
   * as the new value
   * 
   * @return
   */
  public String insertFieldName() default "";

  /**
   * For delete logs, the field name on the entity to be used when logging
   * delete records. By default delete records have field = empty. Specifying a
   * field here will log the corresponding DB field name and the entity's value
   * as the old value
   * 
   * @return
   */
  public String deleteFieldName() default "";
}
