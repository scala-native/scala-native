package scala.scalanative.runtime

import java.lang.Runtime

import org.junit.Test
import org.junit.Assert.*
import scala.scalanative.junit.utils.AssumesHelper

object TestModule {
  val slowInitField = {
    assertNotNull(this)
    assertNotNull(this.getClass())
    Thread.sleep(1000)
    42
  }
}

class ModuleInitializationTest {
  @Test def initializeFromMultipleThreads(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()

    val latch = new java.util.concurrent.CountDownLatch(1)
    val threads = Seq.fill(4) {
      new Thread(() => {
        latch.await()
        assertEquals(42, TestModule.slowInitField)
      })
    }
    threads.foreach(_.start())
    Thread.sleep(100)
    latch.countDown()
    threads.foreach(_.join())
    assertEquals(42, TestModule.slowInitField)
  }
}
