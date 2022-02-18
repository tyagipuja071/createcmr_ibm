package com.ibm.cio.cmr.utils.coverage.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.utils.coverage.JarProperties;

/**
 * Handles the extraction and parsing of raw coverage rule jars files
 * 
 * @author JeffZAMORA
 *
 */
public class RulesArchive {

  private static final Logger LOG = Logger.getLogger(RulesArchive.class);

  /**
   * Unpacks a rule jar archive. The archive can be the main ZIP file or the raw
   * JAR file from ODM
   * 
   * @param zipFileLoc
   * @param targetDir
   * @return The JAR file of thew rules jar, if the input was a ZIP
   * @throws Exception
   */
  public File unpackArchivedRuleset(String zipFileLoc, String targetDir) throws Exception {
    File zipFile = new File(zipFileLoc);
    File target = new File(targetDir);
    return unpackArchivedRuleset(zipFile, target);
  }

  /**
   * Unpacks a rule jar archive. The archive can be the main ZIP file or the raw
   * JAR file from ODM
   * 
   * @param zipFileLoc
   * @param targetDir
   * @return The JAR file of thew rules jar, if the input was a ZIP
   * @throws Exception
   */
  public File unpackArchivedRuleset(File zipFile, File target) throws Exception {
    LOG.trace("Unpacking rules from " + zipFile.getAbsolutePath() + " into " + target.getAbsolutePath());
    if (!zipFile.exists()) {
      throw new FileNotFoundException("File " + zipFile.getAbsolutePath() + " does not exist.");
    }
    if (!zipFile.getName().toUpperCase().endsWith("ZIP") && !zipFile.getName().toUpperCase().endsWith("JAR")) {
      throw new IllegalArgumentException("Oly valid ZIP or JAR archives can be processed.");
    }
    if (!target.exists()) {
      target.mkdirs();
    }
    if (!target.exists() || !target.isDirectory()) {
      throw new IllegalArgumentException("Target directory " + target.getAbsolutePath() + " must be a valid directory");
    }

    // nested extraction of the contents, creating directories as necessary

    byte[] buffer = new byte[1024];
    File rulesJar = null;
    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    try {
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        File newFile = nestFile(target, zipEntry);
        File parentDir = newFile.getParentFile();
        if (!parentDir.exists()) {
          parentDir.mkdirs();
        }
        if (newFile.getName().toUpperCase().endsWith("JAR")) {
          rulesJar = newFile;
        }
        FileOutputStream fos = new FileOutputStream(newFile);
        try {
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        } finally {
          fos.close();
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }

      if (zis != null) {
        zis.closeEntry();
      }
    } finally {
      zis.close();
    }

    return rulesJar;
  }

  /**
   * 
   * @param destinationDir
   * @param zipEntry
   * @return
   * @throws IOException
   */
  private File nestFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }

  /**
   * Extracts the files from the rules JAR file
   * 
   * @param rulesRootDir
   * @return
   */
  public RulesContainer extractRuleFiles(String rulesRootDir) {
    RulesContainer container = new RulesContainer();

    File rootDir = new File(rulesRootDir);
    File dir = lookFor(rootDir, JarProperties.getIntegratedCoverageDir(), true);
    if (dir != null) {
      LOG.trace("Integrated Coverage Dir: " + dir.getName());
      List<File> files = new ArrayList<File>();
      addAllFiles(files, dir);
      for (File file : files) {
        container.addIntegratedCoverageFile(file);
        LOG.trace(" - Integrated Coverage File: " + file.getName());
      }
    } else {
      LOG.warn("Integrated Coverage Dir " + JarProperties.getIntegratedCoverageDir() + " not found.");
    }

    dir = lookFor(rootDir, JarProperties.getDelegationDir(), true);
    if (dir != null) {
      LOG.trace("Delegated Coverage Dir: " + dir.getName());
      List<File> files = new ArrayList<File>();
      addAllFiles(files, dir);
      for (File file : files) {
        container.addDelegationFile(file);
        LOG.trace(" - Delegated Coverage File: " + file.getName());
      }
    } else {
      LOG.warn("Delegated Coverage Dir " + JarProperties.getDelegationDir() + " not found.");
    }

    List<String> countryDirs = JarProperties.getCountryCoverageDirectoryProps();
    for (String countryDir : countryDirs) {
      String desc = JarProperties.getProperty(countryDir + ".desc");
      if (desc == null) {
        desc = countryDir;
      }
      String regExp = JarProperties.getProperty(countryDir);
      LOG.trace("Looking for country dir : " + countryDir + " (" + regExp + ") under " + rootDir + " (Reg Exp: " + regExp + ")");
      dir = lookFor(rootDir, regExp, true);
      if (dir != null) {
        List<File> files = new ArrayList<File>();
        addAllFiles(files, dir);
        LOG.trace("Country Coverage Dir: " + dir.getName() + " [" + files.size() + "]");
        for (File file : files) {
          LOG.trace(" - Country Coverage File: " + file.getName());
          container.addCountryCoverageFile(desc, file);
        }
      } else {
        LOG.warn("Country Dir " + countryDir + " (" + regExp + ") not found.");
      }

    }
    return container;
  }

  private void addAllFiles(List<File> files, File dir) {
    for (File file : dir.listFiles()) {
      if (!file.isDirectory()) {
        files.add(file);
      } else {
        addAllFiles(files, file);
      }
    }
  }

  private File lookFor(File dir, String regExp, boolean searchDir) {
    for (File file : dir.listFiles()) {
      String name = file.getName().toLowerCase();
      if (file.isDirectory() && name.matches(regExp) && searchDir) {
        return file;
      }
      if (!file.isDirectory() && name.matches(regExp) && !searchDir) {
        return file;
      }
      if (file.isDirectory()) {
        File subFile = lookFor(file, regExp, searchDir);
        if (subFile != null) {
          return subFile;
        }
      }
    }
    return null;
  }
}
