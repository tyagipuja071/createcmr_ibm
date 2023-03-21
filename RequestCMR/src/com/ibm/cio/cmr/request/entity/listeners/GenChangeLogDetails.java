package com.ibm.cio.cmr.request.entity.listeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to add for entities that implement the gen_change_log listener
 * 
 * @author
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GenChangeLogDetails {

  public String mandt();

  public String tab_nm() default "";

  public String tab_key1() default ""; // COMP_NO

  public String tab_key2() default ""; // ENT_NO

  public String field_nm() default "";

  public String action_ind() default "";

  public String change_by() default "";

  public String change_src_typ() default "";

  public String change_src_id() default "";

  // public JpaManage.Sources db();

  public boolean logCreates() default true;

  public boolean logUpdates() default true;

  public boolean logDeletes() default true;

  public String pk();
}
