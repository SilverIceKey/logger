package com.orhanobut.logger

import com.google.common.truth.Truth
import org.junit.Test

class TxtFormatStratehyTest {

  /**
   * log print content add filename and line number
   */
  @Test fun log() {
    val formatStrategy = TxtFormatStrategy.newBuilder()
        .logStrategy { priority, tag, message ->
          Truth.assertThat(tag).isEqualTo("PRETTY_LOGGER-tag")
          Truth.assertThat(priority).isEqualTo(Logger.VERBOSE)
          Truth.assertThat(message).contains("VERBOSE PRETTY_LOGGER-tag [NativeMethodAccessorImpl.java]-[62] message")
        }
        .build()

    formatStrategy.log(Logger.VERBOSE, "tag", "message")
  }

  @Test fun defaultTag() {
    val formatStrategy = TxtFormatStrategy.newBuilder()
        .logStrategy { priority, tag, message -> Truth.assertThat(tag).isEqualTo("PRETTY_LOGGER") }
        .build()

    formatStrategy.log(Logger.VERBOSE, null, "message")
  }

  @Test fun customTag() {
    val formatStrategy = TxtFormatStrategy.newBuilder()
        .tag("custom")
        .logStrategy { priority, tag, message -> Truth.assertThat(tag).isEqualTo("custom") }
        .build()

    formatStrategy.log(Logger.VERBOSE, null, "message")
  }
}