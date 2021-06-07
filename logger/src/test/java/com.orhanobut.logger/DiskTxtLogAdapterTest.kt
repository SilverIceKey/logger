package com.orhanobut.logger

import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class DiskTxtLogAdapterTest {
  @Mock private lateinit var formatStrategy: FormatStrategy

  @Before fun setup() {
    MockitoAnnotations.initMocks(this)
  }

  @Test fun isLoggableTrue() {
    val logAdapter = DiskTxtLogAdapter(formatStrategy)

    Truth.assertThat(logAdapter.isLoggable(Logger.VERBOSE, "tag")).isTrue()
  }

  @Test fun isLoggableFalse() {
    val logAdapter = object : DiskTxtLogAdapter(formatStrategy) {
      override fun isLoggable(priority: Int, tag: String?): Boolean {
        return false
      }
    }

    Truth.assertThat(logAdapter.isLoggable(Logger.VERBOSE, "tag")).isFalse()
  }

  @Test fun log() {
    val logAdapter = DiskTxtLogAdapter(formatStrategy)

    logAdapter.log(Logger.VERBOSE, "tag", "message")

    Mockito.verify(formatStrategy).log(Logger.VERBOSE, "tag", "message")
  }

}