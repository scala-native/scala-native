package org.scalanative.testsuite.javalib.lang

import java.lang.*

import org.junit.Test
import org.junit.Assert.*

class ClassLoaderOnJDK9 {

  @Test def getPlatformClassLoader(): Unit = {
    val cl = ClassLoader.getPlatformClassLoader()
    assertTrue(cl != null)
  }
}
