package org.scalanative.testsuite.javalib.lang

import java.nio.file.Paths

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform._

class SystemTest {
  @Test def systemNanoTimeIsMonotonicallyIncreasing(): Unit = {
    var t0 = 0L
    var t1 = 0L

    // a monotonic function (or monotone function) is a function between ordered sets
    // that preserves or reverses the given order.
    // It shoud never be less, but could be equal,
    // so we want to test if time is correctly increasing during the test as well
    val startTime = System.nanoTime()
    for (_ <- 1 to 100000) {
      t1 = System.nanoTime()
      val diff = t1 - t0
      assertTrue(s"Diff in loop: $diff >= 0L", diff >= 0L)
      t0 = t1
    }
    val endTime = System.nanoTime()
    val elapsed = endTime - startTime
    assertTrue(s"After loop elapsed: $elapsed > 0L", elapsed > 0L)
  }

  @Test def systemGetenvShouldContainKnownEnvVariables(): Unit = {
    assertTrue("home", System.getenv().containsKey("HOME"))
    assertTrue("user", System.getenv().containsKey("USER"))
    assertTrue(System.getenv().containsKey("SCALA_NATIVE_ENV_WITH_EQUALS"))
    assertTrue(System.getenv().containsKey("SCALA_NATIVE_ENV_WITHOUT_VALUE"))
    assertFalse(
      System.getenv().containsKey("SCALA_NATIVE_ENV_THAT_DOESNT_EXIST")
    )
    assertTrue(
      System
        .getenv()
        .containsKey("SCALA_NATIVE_ENV_WITH_UNICODE")
    )
  }

  @Test def systemGetenvKeyShouldReadKnownEnvVariables(): Unit = {
    // assertEquals("scala-native", System.getenv().get("USER"))
    assertEquals("1+1=2", System.getenv("SCALA_NATIVE_ENV_WITH_EQUALS"))
    assertEquals("", System.getenv("SCALA_NATIVE_ENV_WITHOUT_VALUE"))
    assertEquals(null, System.getenv("SCALA_NATIVE_ENV_THAT_DOESNT_EXIST"))
    assertEquals(
      0x2192.toChar.toString,
      System
        .getenv()
        .get("SCALA_NATIVE_ENV_WITH_UNICODE")
    )

  }

  @Test def propertyUserHomeShouldBeSet(): Unit = {
    assertEquals(
      System.getProperty("user.home").toLowerCase(),
      System.getenv("HOME").toLowerCase()
    )
  }

  @Test def propertyUserDirShouldBeSet(): Unit = {
    val expected = {
      val base = System.getenv("SCALA_NATIVE_USER_DIR").toLowerCase()
      if (executingInJVM) Paths.get(base, "unit-tests", "jvm").toString()
      else base
    }

    val userDir = System.getProperty("user.dir").toLowerCase()
    // JVM project can end with Scala binary version suffix directory
    if (executingInJVM) assertTrue(userDir.startsWith(expected))
    else assertEquals(expected, userDir)
  }

}
