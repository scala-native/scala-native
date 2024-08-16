package org.scalanative.testsuite.javalib.lang.management

import java.lang.management._

import org.junit.Test
import org.junit.Assert._

class ManagementFactoryTest {

  @Test def getMemoryMXBean(): Unit = {
    val bean = ManagementFactory.getMemoryMXBean
    val memoryUsage = bean.getHeapMemoryUsage()

    assertTrue(memoryUsage.getInit() >= 0L)
    assertTrue(memoryUsage.getCommitted() >= 0L)
    assertTrue(memoryUsage.getUsed() >= 0L)
    assertTrue(memoryUsage.getMax() >= 0L)
  }

}
