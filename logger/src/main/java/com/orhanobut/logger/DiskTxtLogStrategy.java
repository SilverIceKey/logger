package com.orhanobut.logger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.orhanobut.logger.Utils.checkNotNull;

/**
 * Abstract class that takes care of background threading the file log operation on Android.
 * implementing classes are free to directly perform I/O operations there.
 * <p>
 * Writes all logs to the disk with TXT format.
 */
public class DiskTxtLogStrategy implements LogStrategy {
  @NonNull private final Handler handler;
  public DiskTxtLogStrategy(@NonNull Handler handler) {
    this.handler = checkNotNull(handler);
  }

  @Override public void log(int level, @Nullable String tag, @NonNull String message) {
    checkNotNull(message);

    // do nothing on the calling thread, simply pass the tag/msg to the background thread
    handler.sendMessage(handler.obtainMessage(level, message));
  }

  static class WriteHandler extends Handler {

    @NonNull private final String folder;
    private final int maxFileSize;
    private SimpleDateFormat dateFormat;

    WriteHandler(@NonNull Looper looper, @NonNull String folder, int maxFileSize) {
      super(checkNotNull(looper));
      this.folder = checkNotNull(folder);
      this.maxFileSize = maxFileSize;
    }

    @SuppressWarnings("checkstyle:emptyblock")
    @Override public void handleMessage(@NonNull Message msg) {
      String content = (String) msg.obj;

      FileWriter fileWriter = null;
      File logFile = getLogFile(folder);

      try {
        fileWriter = new FileWriter(logFile, true);

        writeLog(fileWriter, content);

        fileWriter.flush();
        fileWriter.close();
      } catch (IOException e) {
        if (fileWriter != null) {
          try {
            fileWriter.flush();
            fileWriter.close();
          } catch (IOException e1) { /* fail silently */ }
        }
      }
    }

    /**
     * This is always called on a single background thread.
     * Implementing classes must ONLY write to the fileWriter and nothing more.
     * The abstract class takes care of everything else including close the stream and catching IOException
     *
     * @param fileWriter an instance of FileWriter already initialised to the correct file
     */
    private void writeLog(@NonNull FileWriter fileWriter, @NonNull String content) throws IOException {
      checkNotNull(fileWriter);
      checkNotNull(content);

      fileWriter.append(content);
    }

    private File getLogFile(@NonNull String folderName) {
      checkNotNull(folderName);

      File folder = new File(folderName);
      if (!folder.exists()) {
        //TODO: What if folder is not created, what happens then?
        folder.mkdirs();
      }

      int newFileCount = 0;
      File newFile;
      File existingFile = null;
      if (dateFormat == null) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
      }
      newFile = new File(folder, String.format("%s.txt", dateFormat.format(new Date())));
      while (newFile.exists()) {
        existingFile = newFile;
        newFileCount++;
        newFile = new File(folder, String.format("%s-%s.txt", dateFormat.format(new Date()),newFileCount));
      }

      if (existingFile != null) {
        if (existingFile.length() >= maxFileSize) {
          return newFile;
        }
        return existingFile;
      }

      return newFile;
    }
  }
}
