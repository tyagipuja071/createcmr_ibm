package com.ibm.cio.cmr.request.entity.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.lang.StringUtils;

/**
 * Listener for entities with values that need to be trimmed. The class checks
 * the annotations {@link AutoTrimStrings} for entities with all string vlues
 * trimmed, or {@link Trim} for per-field trimming.
 * 
 * The trimming will happen before each insert or update via the
 * {@link PrePersist} and {@link PreUpdate} annotations
 * 
 * @author Jeffrey Zamora
 * 
 */
public class TrimListener {

  private final Map<Class<?>, Set<Field>> fieldsToTrim = new HashMap<Class<?>, Set<Field>>();

  /**
   * Trim all the qualified fields for the class
   * 
   * @param entity
   * @throws Exception
   */
  @PrePersist
  @PreUpdate
  public void trimProperties(final Object entity) throws Exception {
    if (entity.getClass().getAnnotation(Entity.class) == null) {
      // not an entity, no trims
      return;
    }

    // gather all fields
    Set<Field> fields = null;
    if (entity.getClass().getAnnotation(AutoTrimStrings.class) != null) {
      fields = getAllQualifiedStringFields(entity.getClass());
    } else {
      fields = getFieldsToTrim(entity.getClass());
    }

    NullValue nvAnnot = null;
    String nullValue = null;
    if (fields != null && fields.size() > 0) {
      for (final Field field : fields) {
        nullValue = null;
        nvAnnot = field.getAnnotation(NullValue.class);
        if (nvAnnot != null) {
          nullValue = nvAnnot.value();
        }
        final String value = removeInvalid((String) field.get(entity));
        try {
          Method set = entity.getClass().getDeclaredMethod("set" + (field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)),
              value != null ? value.getClass() : String.class);
          if (set != null) {
            if (value != null) {
              set.invoke(entity, value.trim().length() == 0 ? null : value.trim());
            } else if (nullValue != null) {
              set.invoke(entity, nullValue);
            }
          }
        } catch (Exception e) {
          System.err.println("Error in set method: " + field.getName());
        }
      }
    }
  }

  /**
   * Removesa set of invalid strings like non-straight quotes
   * 
   * @param value
   * @return
   */
  public static String removeInvalid(String value) {
    if (value == null) {
      return value;
    }
    try {
      String cleaned = StringUtils.replace(value, "�", "'");
      cleaned = StringUtils.replace(cleaned, "�", "'");
      cleaned = StringUtils.replace(cleaned, "�", "\"");
      cleaned = StringUtils.replace(cleaned, "�", "\"");
      return cleaned;
    } catch (Exception e) {
      return value;
    }
  }

  /**
   * Compiles a list of fields that we need to trim
   * 
   * @param entityClass
   * @return
   * @throws Exception
   */
  private Set<Field> getAllQualifiedStringFields(Class<?> entityClass) throws Exception {
    if (Object.class.equals(entityClass))
      return Collections.emptySet();
    Set<Field> fieldsToTrim = this.fieldsToTrim.get(entityClass);
    if (fieldsToTrim == null) {
      fieldsToTrim = new HashSet<Field>();
      for (final Field field : entityClass.getDeclaredFields()) {
        if (field.getType().equals(String.class)) {
          try {
            // check if the property has a getter method, means it's a bean
            if (entityClass.getMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1), (Class[]) null) != null) {
              field.setAccessible(true);
              fieldsToTrim.add(field);
            }
          } catch (NoSuchMethodException e) {
            // no getter, skip the field
          }
        }
      }
      this.fieldsToTrim.put(entityClass, fieldsToTrim);
    }
    return fieldsToTrim;
  }

  /**
   * Compiles a list of fields that we need to trim
   * 
   * @param entityClass
   * @return
   * @throws Exception
   */
  private Set<Field> getFieldsToTrim(Class<?> entityClass) throws Exception {
    if (Object.class.equals(entityClass))
      return Collections.emptySet();
    Set<Field> fieldsToTrim = this.fieldsToTrim.get(entityClass);
    if (fieldsToTrim == null) {
      fieldsToTrim = new HashSet<Field>();
      for (final Field field : entityClass.getDeclaredFields()) {
        // per field checking of the Trim annotation
        if (field.getType().equals(String.class) && field.getAnnotation(Trim.class) != null) {
          field.setAccessible(true);
          fieldsToTrim.add(field);
        }
      }
      this.fieldsToTrim.put(entityClass, fieldsToTrim);
    }
    return fieldsToTrim;
  }

}
