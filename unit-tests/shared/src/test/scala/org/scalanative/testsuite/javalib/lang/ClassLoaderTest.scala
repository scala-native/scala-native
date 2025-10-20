package org.scalanative.testsuite.javalib.lang

import java.lang.*

import org.junit.Test
import org.junit.Assert.*

class ClassLoaderTest {

  @Test def getSystemClassLoader(): Unit = {
    val cl = ClassLoader.getSystemClassLoader()
    assertTrue(cl != null)
  }
}
