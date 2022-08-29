package scala.scalanative.meta

import scala.scalanative.runtime.Platform

import org.junit.Test
import org.junit.Assert._

class LinktimeInfoTest {

  @Test def testOS(): Unit = {
    assertEquals(Platform.isFreeBSD(), LinktimeInfo.isFreeBSD)
    assertEquals(Platform.isLinux(), LinktimeInfo.isLinux)
    assertEquals(Platform.isMac(), LinktimeInfo.isMac)
    assertEquals(Platform.isWindows(), LinktimeInfo.isWindows)
  }

}
