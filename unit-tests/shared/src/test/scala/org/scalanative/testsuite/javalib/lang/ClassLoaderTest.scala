package org.scalanative.testsuite.javalib.lang

import java.lang._

import org.junit.Test
import org.junit.Assert._

class ClassLoaderTest {

  @Test def getSystemClassLoader(): Unit = {
    val cl = ClassLoader.getSystemClassLoader()
    assertTrue(cl != null)
  }
}
