package org.apache.commons.compress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.AfterClass;

public abstract class BaseTestCase {

  private static boolean isAndroid;

  private static final List<File> tempCopies = new ArrayList<>();

  static {
    try {
      Class.forName("android.app.Activity");
      isAndroid = true;
    } catch (ClassNotFoundException e) {
      isAndroid = false;
    }
  }

  public static File getFile(final String path) throws IOException {
    if (isAndroid) {
      return getFileAndroid(path);
    }

    final URL url = AbstractTestCase.class.getClassLoader().getResource(path);
    if (url == null) {
      throw new FileNotFoundException("couldn't find " + path);
    }
    URI uri = null;
    try {
      uri = url.toURI();
    } catch (final java.net.URISyntaxException ex) {
      throw new IOException(ex);
    }
    return new File(uri);
  }

  private static File getFileAndroid(final String path) throws IOException {
    File dir = getDirAndroid(path);
    if (dir != null) {
      return dir;
    }

    File of = File.createTempFile("copyFile", new File(path).getName());

    try (InputStream is = AbstractTestCase.class.getClassLoader().getResourceAsStream(path);
        OutputStream os = new FileOutputStream(of)) {
      IOUtils.copy(is, os);
    }

    synchronized (tempCopies) {
      tempCopies.add(of);
    }

    return of;
  }

  private static File getDirAndroid(final String path) throws IOException {
    InputStream is = AbstractTestCase.class.getClassLoader().getResourceAsStream(path + "/contents.txt");
    if (is == null) {
      return null;
    }

    File dir = null;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      dir = File.createTempFile("copyFile", new File(path).getName());
      dir.delete();
      dir.mkdirs();

      String line;
      while ((line = reader.readLine()) != null) {
        File file = new File(dir, line);
        try (InputStream i = AbstractTestCase.class.getClassLoader().getResourceAsStream(path + "/" + line);
            OutputStream o = new FileOutputStream(file)) {
          IOUtils.copy(i, o);
        }
      }
    }

    synchronized (tempCopies) {
      tempCopies.add(dir);
    }

    return dir;
  }

  @AfterClass
  public static void clearTempCopies() {
    synchronized (tempCopies) {
      for (File f : tempCopies) {
        delete(f);
      }
      tempCopies.clear();
    }
  }

  private static void delete(File file) {
    if (file == null) {
      return;
    }

    File[] files = file.listFiles();
    if (files != null) {
      for (File f: files) {
        delete(f);
      }
    }

    file.delete();
  }
}
