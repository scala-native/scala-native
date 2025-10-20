package org.scalanative.testsuite.javalib.lang.management

import java.lang.management.*

import org.junit.Test
import org.junit.Assert.*

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

  @Test def getOperatingSystemMXBean(): Unit = {
    val bean = ManagementFactory.getOperatingSystemMXBean

    assertEquals(bean.getArch(), System.getProperty("os.arch"))
    assertEquals(bean.getName(), System.getProperty("os.name"))
    assertEquals(bean.getVersion(), System.getProperty("os.version"))
    assertEquals(
      bean.getAvailableProcessors(),
      Runtime.getRuntime().availableProcessors()
    )
  }

  @Test def getRuntimeMXBean(): Unit = {
    val bean = ManagementFactory.getRuntimeMXBean

    assertEquals(bean.getVmName(), sys.props("java.vm.name"))
    assertEquals(bean.getVmVendor(), sys.props("java.vm.vendor"))
    assertEquals(bean.getVmVersion(), sys.props("java.vm.version"))
    assertEquals(bean.getSpecName(), sys.props("java.vm.specification.name"))
    assertEquals(
      bean.getSpecVendor(),
      sys.props("java.vm.specification.vendor")
    )
    assertEquals(
      bean.getSpecVersion(),
      sys.props("java.vm.specification.version")
    )
    assertTrue(bean.getUptime() > 0L)
    assertTrue(bean.getStartTime() > 0L)
    assertTrue(bean.getSystemProperties().size() > 0)
  }

  @Test def getGarbageCollectorMXBeans(): Unit = {
    val beans = ManagementFactory.getGarbageCollectorMXBeans

    assert(beans.size() > 0)
  }

  @Test def getMemoryManagerMXBeans(): Unit = {
    val beans = ManagementFactory.getMemoryManagerMXBeans

    assert(beans.size() > 0)
  }

}
