package org.scalanative.testsuite.javalib.lang

import java.lang._

import org.junit.Test
import org.junit.Assert._

class ClassLoaderOnJDK9 {

  @Test def getPlatformClassLoader(): Unit = {
    val cl = ClassLoader.getPlatformClassLoader()
    assertTrue(cl != null)
  }
}