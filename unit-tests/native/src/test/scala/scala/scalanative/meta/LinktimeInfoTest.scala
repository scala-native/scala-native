package scala.scalanative.meta

import scala.scalanative.buildinfo.ScalaNativeBuildInfo
import scala.scalanative.runtime.Platform

import org.junit.Test
import org.junit.Assert._

class LinktimeInfoTest {

  @Test def testMode(): Unit = {
    assertEquals(LinktimeInfo.debugMode, !LinktimeInfo.releaseMode)
  }

  @Test def testVersion(): Unit = {
    assertEquals(LinktimeInfo.runtimeVersion, ScalaNativeBuildInfo.version)
  }

  @Test def testOS(): Unit = {
    assertEquals("FreeBSD", Platform.isFreeBSD(), LinktimeInfo.isFreeBSD)
    assertEquals("Linux", Platform.isLinux(), LinktimeInfo.isLinux)
    assertEquals("Mac", Platform.isMac(), LinktimeInfo.isMac)
    assertEquals("Windows", Platform.isWindows(), LinktimeInfo.isWindows)
  }

}
