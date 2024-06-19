package org.scalanative.testsuite.javalib.lang

import java.lang._

import java.util.concurrent.TimeUnit
import java.io.File

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.junit.utils.AssumesHelper._

import scala.scalanative.meta.LinktimeInfo

import scala.scalanative.posix.unistd.{sysconf, _SC_NPROCESSORS_CONF}

class RuntimeOsTest {

  // See also the major /unit-tests/shared RuntimeTest.scala
  @Test def availableProcessors(): Unit = {
    val available = Runtime.getRuntime().availableProcessors()

    assertTrue(s"${available} < 1", available >= 1)

    /* Do a coherence check of 'available' against an upper bound. Is it
     * in a believable closed range?
     *
     * A tight upper bound is desirable but problematic.
     * 'available' should match the number of online processors,
     * _SC_NPROCESSORS_ONLN exactly. Since the value fetch and comparison
     * are not atomic, there is a race, albeit tortoise slow.
     *
     * Check against the number of configured processors, _SC_NPROCESSORS_CONF.
     * It gives a higher ceiling but fewer chances intermittent errors.
     */

    if (LinktimeInfo.isLinux) {
      val nprocConf = sysconf(_SC_NPROCESSORS_CONF).toInt
      assertTrue(
        "available > sysconf(_SC_NPROCESSORS_CONF): " +
          s"${available} > ${nprocConf}",
        available <= nprocConf
      )
    }
  }
}
