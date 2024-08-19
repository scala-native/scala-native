package org.scalanative.testsuite.javalib.lang.management

import java.lang.management._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ManagementFactoryTest {

  @Test def getMemoryMXBean(): Unit = {
    val bean = ManagementFactory.getMemoryMXBean
    val memoryUsage = bean.getHeapMemoryUsage()

    assertTrue(memoryUsage.getInit() >= 0L)
    assertTrue(memoryUsage.getCommitted() >= 0L)
    assertTrue(memoryUsage.getUsed() >= 0L)
    assertTrue(memoryUsage.getMax() >= 0L)
  }

  @Test def getThreadMXBean(): Unit = {
    val bean = ManagementFactory.getThreadMXBean

    assertTrue(bean.getThreadCount() >= 0)
    assertTrue(bean.getDaemonThreadCount() >= 0)
    assertTrue(bean.getAllThreadIds().length >= 0)
    assertTrue(bean.dumpAllThreads(false, false).length >= 0)
    assertTrue(bean.dumpAllThreads(true, true).length >= 0)
  }

  @Test def threadMXBeanFailOnInvalidInput(): Unit = {
    val bean = ManagementFactory.getThreadMXBean

    assertThrows(
      classOf[java.lang.IllegalArgumentException],
      bean.getThreadInfo(-1L)
    )

    assertThrows(
      classOf[java.lang.IllegalArgumentException],
      bean.getThreadInfo(1L, -1)
    )
  }

}
