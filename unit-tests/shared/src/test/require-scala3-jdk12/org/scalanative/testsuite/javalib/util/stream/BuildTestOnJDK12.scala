package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._

import java.{util => ju}
import java.util.ArrayList
import java.util.Map

import java.util.stream.Collector.Characteristics

import org.junit.Test
import org.junit.Assert._

class BuildTestOnJDK12 {

  // Since: Java 12
  @Test def executesOnJava12(): Unit = {
    /* Verifying 2023-05-05 build infrastructure change.
     *  This test should execute only when some variant of Scala-3 and
     *  some variant of JDK >= 12 are both active.
     */
  }
}
