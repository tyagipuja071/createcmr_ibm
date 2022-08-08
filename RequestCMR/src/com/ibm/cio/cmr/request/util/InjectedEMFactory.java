package com.ibm.cio.cmr.request.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.log4j.Logger;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;

/**
 * Creates an {@link EntityManagerFactory} instance with variables and values
 * replaced from system environment values and system properties. This only
 * works with eclipselink's {@link EntityManagerFactory} implementation
 * 
 * @author 136786PH1
 *
 */
public class InjectedEMFactory {

  private static final Logger LOG = Logger.getLogger(InjectedEMFactory.class);
  private static final Map<String, String> ALL_PROPERTIES = new HashMap<>();

  static {
    ALL_PROPERTIES.putAll(System.getenv());
    for (String key : System.getProperties().stringPropertyNames()) {
      ALL_PROPERTIES.put(key, System.getProperty(key));
    }
  }

  /**
   * Creates an {@link EntityManagerFactory} instance with replaced variables
   * from system values
   * 
   * @param persistenceUnitName
   * @param properties
   * @return
   */
  public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map<Object, Object> properties) {
    EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnitName);
    try (ClosableEntityManagerFactoryProperties factoryProps = new ClosableEntityManagerFactoryProperties(factory)) {
      Map<Object, Object> overrideProps = replaceValues(factoryProps.asMap());
      overrideProps.putAll(properties);
      return Persistence.createEntityManagerFactory(persistenceUnitName, overrideProps);
    }
  }

  /**
   * Replaces the current values on the properties with variables found on the
   * system. The variable format is <code>${name}</code>
   * 
   * @param props
   * @return
   */
  private static Map<Object, Object> replaceValues(Map<String, String> props) {
    Map<Object, Object> overrideProps = new HashMap<>();
    for (Entry<String, String> entry : props.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      boolean overridden = false;
      if (containsVariable(key)) {
        LOG.debug("Replacing variable '" + key + "'");
        key = replaceWithValue(key);
        overridden = true;
      }
      if (containsVariable(value)) {
        LOG.debug("Replacing value '" + key + "'");
        value = replaceWithValue(value);
        overridden = true;
      }
      if (overridden) {
        overrideProps.put(key, value);
      }
    }
    return overrideProps;
  }

  /**
   * Checks if the specified input contains a variable in the format
   * <code>${name}</code>
   * 
   * @param input
   * @return
   */
  private static boolean containsVariable(String input) {
    int variableStartIndex = input.indexOf("${");
    int variableEndIndex = input.lastIndexOf('}');
    return variableStartIndex >= 0 && variableStartIndex < variableEndIndex;
  }

  /**
   * Replaces the key with the system value
   * 
   * @param key
   * @return
   */
  private static String replaceWithValue(String key) {
    for (Entry<String, String> entry : ALL_PROPERTIES.entrySet()) {
      key = key.replace("${" + entry.getKey() + "}", entry.getValue());
    }
    return key;
  }

  private static final class ClosableEntityManagerFactoryProperties implements AutoCloseable {

    private final EntityManagerFactory factory;

    public ClosableEntityManagerFactoryProperties(EntityManagerFactory factory) {
      this.factory = factory;
    }

    @Override
    public void close() {
      factory.close();
    }

    public Map<String, String> asMap() {
      EntityManagerFactoryImpl emf = (EntityManagerFactoryImpl) factory;
      EntityManagerFactoryDelegate delegate = emf.unwrap(EntityManagerFactoryDelegate.class);
      PersistenceUnitInfo unit = delegate.getSetupImpl().getPersistenceUnitInfo();
      Properties props = unit.getProperties();
      Map<String, String> map = new HashMap<String, String>();
      for (String key : props.stringPropertyNames()) {
        map.put(key, props.getProperty(key));
      }
      return map;
    }
  }

}
