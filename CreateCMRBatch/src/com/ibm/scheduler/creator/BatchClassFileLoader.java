package com.ibm.scheduler.creator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BatchClassFileLoader {
  public static List<Class<?>> getClasses(String packageName) {
    String path = packageName.replace('.', '/');

    ClassLoader classLoader = getClassLoader();
    assert classLoader != null;

    Enumeration<URL> resources = getResources(path);
    assert resources != null;

    List<String> dirs = new ArrayList<String>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      dirs.add(resource.getFile());
    }

    TreeSet<String> classes = new TreeSet<String>();
    for (String directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }

    return createClassList(classes);
  }

  private static ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  private static Enumeration<URL> getResources(String path) {
    try {
      return getClassLoader().getResources(path);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static TreeSet<String> findClasses(String directory, String packageName) {
    TreeSet<String> classes = new TreeSet<String>();
    if (directory.startsWith("file:") && directory.contains("!")) {
      String[] split = directory.split("!");
      URL jar = getJarURL(split);
      assert jar != null;
      try (ZipInputStream zip = new ZipInputStream(getOpenStream(jar))) {
        ZipEntry entry = null;
        while ((entry = zip.getNextEntry()) != null) {
          if (entry.getName().endsWith(".class")) {
            String className = entry.getName().replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');
            if (className.startsWith(packageName)) {
              classes.add(className);
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    File dir = new File(directory);
    if (!dir.exists()) {
      return classes;
    }
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          assert !file.getName().contains(".");
          classes.addAll(findClasses(file.getAbsolutePath(), packageName + "." + file.getName()));
        } else if (file.getName().endsWith(".class")) {
          classes.add(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
        }
      }
    }

    return classes;
  }

  public static URL getJarURL(String[] split) {
    try {
      return new URL(split[0]);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public static InputStream getOpenStream(URL jar) {
    try {
      return jar.openStream();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ArrayList<Class<?>> createClassList(TreeSet<String> classes) {
    ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
    for (String clazz : classes) {
      Class<?> classForName = classForName(clazz);
      if (classForName != null) {
        classList.add(classForName);
      }
    }
    return classList;
  }

  public static Class<?> classForName(String clazz) {
    try {
      return Class.forName(clazz);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }
}
