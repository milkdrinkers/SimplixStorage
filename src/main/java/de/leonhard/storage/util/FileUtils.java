package de.leonhard.storage.util;

import de.leonhard.storage.internal.provider.LightningProviders;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Class for easier, more convenient & strait interaction with files
 */
@UtilityClass
public class FileUtils {

  // ----------------------------------------------------------------------------------------------------
  // Getting Files
  // ----------------------------------------------------------------------------------------------------

  public List<File> listFiles(@NonNull final File folder) {
    return listFiles(folder, null);
  }

  /**
   * Returns a List of Files in a folder which ends with a specific extension.
   *
   * <p>If there are no files in the folder or the folder is not found, an
   * empty list is returned instead of null.
   *
   * @param folder    Folder to search in.
   * @param extension Extension to search for. Set to null to skip extension validation.
   */
  public List<File> listFiles(
      @NonNull final File folder,
      @NonNull final String extension) {
    final List<File> result = new ArrayList<>();

    final File[] files = folder.listFiles();

    if (files == null) {
      return result;
    }

    for (final File file : files) {
      // if the extension is null we always add the file.
      if (extension == null || file.getName().endsWith(extension)) {
        result.add(file);
      }
    }

    return result;
  }

  public File getAndMake(@NonNull final String name,
      @NonNull final String path) {
    return getAndMake(new File(path, name));
  }

  public File getAndMake(@NonNull final File file) {
    try {
      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      if (!file.exists()) {
        file.createNewFile();
      }

    } catch (final IOException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while creating file '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'"
      );
    }
    return file;
  }

  // ----------------------------------------------------------------------------------------------------
  // Methods for handling the extension of files
  // ----------------------------------------------------------------------------------------------------

  public String getExtension(@NonNull final String path) {
    return path.lastIndexOf(".") > 0
        ? path.substring(path.lastIndexOf(".") + 1)
        : "";
  }

  public String getExtension(@NonNull final File file) {
    return getExtension(file.getName());
  }

  public String replaceExtensions(@NonNull final String fileName) {
    if (!fileName.contains(".")) {
      return fileName;
    }
    return fileName.replace("." + getExtension(fileName), "");
  }

  public String getParentDirPath(@NonNull final File file) {
    return getParentDirPath(file.getAbsolutePath());
  }

  /**
   * Since file.getParentFile can be null we created an extension function to get the path
   * of the parent file
   *
   * @param fileOrDirPath Path to file
   * @return Path to file as String
   */
  public String getParentDirPath(@NonNull final String fileOrDirPath) {
    final boolean endsWithSlash = fileOrDirPath.endsWith(File.separator);
    return fileOrDirPath
        .substring(0, fileOrDirPath.lastIndexOf(File.separatorChar, endsWithSlash
            ? fileOrDirPath.length() - 2
            : fileOrDirPath.length() - 1));
  }

  public boolean hasChanged(final File file, final long timeStamp) {
    if (file == null) {
      return false;
    }
    return timeStamp < file.lastModified();
  }

  // ----------------------------------------------------------------------------------------------------
  // Methods for reading & writing a file
  // ----------------------------------------------------------------------------------------------------

  public InputStream createInputStream(@NonNull final File file) {
    try {
      return Files.newInputStream(file.toPath());
    } catch (final IOException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while creating InputStream from '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'");
    }
  }

  public OutputStream createOutputStream(@NonNull final File file) {
    try {
      return new FileOutputStream(file);
    } catch (final FileNotFoundException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while creating OutputStream from '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'");
    }
  }

  public Reader createReader(@NonNull final File file) {
    try {
      return new FileReader(file);
    } catch (final FileNotFoundException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while creating Reader for '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'");
    }
  }

  public Writer createWriter(@NonNull final File file) {
    try {
      return new FileWriter(file);
    } catch (final IOException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while creating Writer for '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'");
    }
  }

  public void write(
      @NonNull final File file,
      @NonNull final List<String> lines) {
    try {
      Files.write(file.toPath(), lines);
    } catch (final IOException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while writing to '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'");
    }
  }

  public void writeToFile(
      @NonNull final File file,
      @NonNull final InputStream inputStream) {
    try (final FileOutputStream outputStream = new FileOutputStream(file)) {
      if (!file.exists()) {
        Files.copy(inputStream, file.toPath());
      } else {
        final byte[] data = new byte[8192];
        int count;
        while ((count = inputStream.read(data, 0, 8192)) != -1) {
          outputStream.write(data, 0, count);
        }
      }
    } catch (final IOException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while writing InputStream to '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'");
    }
  }

  public byte[] readAllBytes(@NonNull final File file) {
    try {
      return Files.readAllBytes(file.toPath());
    } catch (final IOException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while reading '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'");
    }
  }

  public List<String> readAllLines(final File file) {
    final byte[] fileBytes = readAllBytes(file);
    final String asString = new String(fileBytes);
    return new ArrayList<>(Arrays.asList(asString.split("\n")));
  }

  // ----------------------------------------------------------------------------------------------------
  // Archiving
  // ----------------------------------------------------------------------------------------------------

  @SneakyThrows
  public void zipFile(final String sourceDirectory, final String to) {
    final File fileTo = getAndMake(new File(to + ".zip"));

    @Cleanup final ZipOutputStream zipOutputStream = new ZipOutputStream(
        createOutputStream(fileTo));
    final Path pathFrom = Paths.get(new File(sourceDirectory).toURI());

    Files.walk(pathFrom)
        .filter(path -> !Files.isDirectory(path))
        .forEach(
            path -> {
              final ZipEntry zipEntry = new ZipEntry(
                  pathFrom.relativize(path).toString());

              try {
                zipOutputStream.putNextEntry(zipEntry);

                Files.copy(path, zipOutputStream);
                zipOutputStream.closeEntry();
              } catch (final IOException ex) {
                ex.printStackTrace();
              }
            });
  }

  // ----------------------------------------------------------------------------------------------------
  // Checksums
  // ----------------------------------------------------------------------------------------------------

  public String md5ChecksumAsString(@NonNull final File filename) {
    final byte[] checkSum = md5Checksum(filename);
    final StringBuilder result = new StringBuilder();

    for (final byte b : checkSum) {
      result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
    }

    return result.toString();
  }

  public byte[] md5Checksum(@NonNull final File file) {
    try (final InputStream fileInputStream = new FileInputStream(file)) {
      final byte[] buffer = new byte[1024];
      final MessageDigest complete = MessageDigest.getInstance("MD5");
      int numRead;

      do {
        numRead = fileInputStream.read(buffer);

        if (numRead > 0) {
          complete.update(buffer, 0, numRead);
        }
      } while (numRead != -1);

      return complete.digest();
    } catch (final IOException | NoSuchAlgorithmException ex) {
      throw LightningProviders.exceptionHandler().create(
          ex,
          "Error while creating checksum of '" + file.getName() + "'.",
          "In: '" + getParentDirPath(file) + "'");
    }
  }

  // ----------------------------------------------------------------------------------------------------
  // Misc
  // ----------------------------------------------------------------------------------------------------

  public void copyFolder(@NonNull final File source,
      @NonNull final File destination)
      throws IOException {

    if (source.isDirectory()) {
      if (!destination.exists()) {
        destination.mkdirs();
      }

      final String[] files = source.list();

      if (files == null) {
        return;
      }

      for (final String file : files) {
        final File srcFile = new File(source, file);
        final File destFile = new File(destination, file);

        copyFolder(srcFile, destFile);
      }
    } else {

      @Cleanup final InputStream in = createInputStream(source);
      @Cleanup final OutputStream out = createOutputStream(destination);
      final byte[] buffer = new byte[1024];

      int length;
      while ((length = in.read(buffer)) > 0) {
        out.write(buffer, 0, length);
      }
    }
  }
}
