package com.orhanobut.logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.orhanobut.logger.Utils.checkNotNull;

/**
 * This is used to saves log messages to the disk.
 * By default it uses {@link TxtFormatStrategy} to translates text message into Txt format.
 */
public class DiskTxtLogAdapter implements LogAdapter {

  @NonNull private final FormatStrategy formatStrategy;

  public DiskTxtLogAdapter() {
    formatStrategy = TxtFormatStrategy.newBuilder().build();
  }

  public DiskTxtLogAdapter(@NonNull FormatStrategy formatStrategy) {
    this.formatStrategy = checkNotNull(formatStrategy);
  }

  @Override public boolean isLoggable(int priority, @Nullable String tag) {
    return true;
  }

  @Override public void log(int priority, @Nullable String tag, @NonNull String message) {
    formatStrategy.log(priority, tag, message);
  }
}
