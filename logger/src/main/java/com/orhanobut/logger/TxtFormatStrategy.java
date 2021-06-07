package com.orhanobut.logger;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.orhanobut.logger.Utils.checkNotNull;

/**
 * TXT formatted file logging for Android.
 * Writes to TXT the following data:
 * epoch timestamp, ISO8601 timestamp (human-readable), log level, tag, log message.
 */
public class TxtFormatStrategy implements FormatStrategy {

  /**
   * The minimum stack trace index, starts at this class after two native calls.
   */
  private static final int MIN_STACK_OFFSET = 5;

  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String NEW_LINE_REPLACEMENT = "\r\n";
  private static final String SEPARATOR = " ";

  private final int methodCount;
  private final int methodOffset;
  @NonNull private final Date date;
  @NonNull private final SimpleDateFormat dateFormat;
  @NonNull private final LogStrategy logStrategy;
  @Nullable private final String tag;

  private TxtFormatStrategy(@NonNull Builder builder) {
    checkNotNull(builder);

    methodCount = builder.methodCount;
    methodOffset = builder.methodOffset;
    date = builder.date;
    dateFormat = builder.dateFormat;
    logStrategy = builder.logStrategy;
    tag = builder.tag;
  }

  @NonNull public static Builder newBuilder() {
    return new Builder();
  }

  @Override public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message) {
    checkNotNull(message);
    log(priority, onceOnlyTag, message, methodCount);
  }

  public void log(int priority, @Nullable String onceOnlyTag, @NonNull String message, int methodCount) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    int stackOffset = getStackOffset(trace) + methodOffset;
    //corresponding method count with the current stack may exceeds the stack trace. Trims the count
    if (methodCount + stackOffset > trace.length) {
      methodCount = trace.length - stackOffset - 1;
    }
    for (int i = methodCount; i > 0; i--) {
      int stackIndex = i + stackOffset;
      if (stackIndex >= trace.length) {
        continue;
      }
      String tag = formatTag(onceOnlyTag);
      date.setTime(System.currentTimeMillis());
      StringBuilder builder = new StringBuilder();
      // human-readable date/time
      builder.append(dateFormat.format(date));
      // level
      builder.append(SEPARATOR);
      builder.append(Utils.logLevel(priority));
      //use position
      builder.append(SEPARATOR);
      builder.append("[");
      builder.append(trace[stackIndex].getFileName());
      builder.append("]");
      builder.append("-");
      builder.append("[");
      builder.append(trace[stackIndex].getLineNumber());
      builder.append("]");
      // message
      if (message.contains(NEW_LINE)) {
        // a new line would break the CSV format, so we replace it here
        message = message.replaceAll(NEW_LINE,
            NEW_LINE_REPLACEMENT +
                new String(new char[dateFormat.format(date).length() +
                    6 +
                    Utils.logLevel(priority).length() +
                    trace[stackIndex].getFileName().length() +
                    ("[" + trace[stackIndex].getLineNumber() + "]").length()]).replace("\0", " "));
      }
      builder.append(SEPARATOR);
      builder.append(message);

      // new line
      builder.append(NEW_LINE);
      logStrategy.log(priority, tag, builder.toString());
    }
  }

  /**
   * Determines the starting index of the stack trace, after method calls made by this class.
   *
   * @param trace the stack trace
   * @return the stack offset
   */
  private int getStackOffset(@NonNull StackTraceElement[] trace) {
    checkNotNull(trace);

    for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
      StackTraceElement e = trace[i];
      String name = e.getClassName();
      if (!name.equals(LoggerPrinter.class.getName()) && !name.equals(Logger.class.getName())) {
        return --i;
      }
    }
    return -1;
  }

  @Nullable private String formatTag(@Nullable String tag) {
    if (!Utils.isEmpty(tag) && !Utils.equals(this.tag, tag)) {
      return this.tag + "-" + tag;
    }
    return this.tag;
  }

  public static final class Builder {
    private static final int MAX_BYTES = 500 * 1024; // 500K averages to a 4000 lines per file

    int methodCount = 1;
    int methodOffset = 0;
    Date date;
    SimpleDateFormat dateFormat;
    LogStrategy logStrategy;
    String tag = "PRETTY_LOGGER";

    private Builder() {
    }

    @NonNull public TxtFormatStrategy.Builder methodCount(int val) {
      methodCount = val;
      return this;
    }

    @NonNull public TxtFormatStrategy.Builder methodOffset(int val) {
      methodOffset = val;
      return this;
    }

    @NonNull public Builder date(@Nullable Date val) {
      date = val;
      return this;
    }

    @NonNull public Builder dateFormat(@Nullable SimpleDateFormat val) {
      dateFormat = val;
      return this;
    }

    @NonNull public Builder logStrategy(@Nullable LogStrategy val) {
      logStrategy = val;
      return this;
    }

    @NonNull public Builder tag(@Nullable String tag) {
      this.tag = tag;
      return this;
    }

    @NonNull public TxtFormatStrategy build() {
      if (date == null) {
        date = new Date();
      }
      if (dateFormat == null) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.UK);
      }
      if (logStrategy == null) {
        String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String folder = diskPath + File.separatorChar + "logger";

        HandlerThread ht = new HandlerThread("AndroidFileLogger." + folder);
        ht.start();
        Handler handler = new DiskTxtLogStrategy.WriteHandler(ht.getLooper(), folder, MAX_BYTES);
        logStrategy = new DiskTxtLogStrategy(handler);
      }
      return new TxtFormatStrategy(this);
    }
  }
}
