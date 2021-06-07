package com.orhanobut.logger

import android.os.Handler
import org.junit.Test
import org.mockito.Mockito

class DiskTxtLogStrategyTest {
  @Test fun log() {
    val handler = Mockito.mock(Handler::class.java)
    val logStrategy = DiskTxtLogStrategy(handler)

    logStrategy.log(Logger.DEBUG, "tag", "message")

    Mockito.verify(handler).sendMessage(handler.obtainMessage(Logger.DEBUG, "message"))
  }
}