package javalib.lang

import org.junit.Test
import org.junit.Assert._

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
      assert(t0 - t1 <= 0L)
      t0 = t1
    }
    val endTime = System.nanoTime()
    assert(startTime - endTime < 0L)
  }

  @Test def systemGetenvShouldContainKnownEnvVariables(): Unit = {
    assert(System.getenv().containsKey("HOME"))
    assert(System.getenv().get("USER") == "scala-native")
    assert(System.getenv().get("SCALA_NATIVE_ENV_WITH_EQUALS") == "1+1=2")
    assert(System.getenv().get("SCALA_NATIVE_ENV_WITHOUT_VALUE") == "")
    assert(System.getenv().get("SCALA_NATIVE_ENV_THAT_DOESNT_EXIST") == null)
    assert(
      System
        .getenv()
        .get("SCALA_NATIVE_ENV_WITH_UNICODE") == 0x2192.toChar.toString
    )
  }

  @Test def systemGetenvKeyShouldReadKnownEnvVariables(): Unit = {
    assert(System.getenv("USER") == "scala-native")
    assert(System.getenv("SCALA_NATIVE_ENV_WITH_EQUALS") == "1+1=2")
    assert(System.getenv("SCALA_NATIVE_ENV_WITHOUT_VALUE") == "")
    assert(System.getenv("SCALA_NATIVE_ENV_THAT_DOESNT_EXIST") == null)
  }

  @Test def propertyUserHomeShouldBeSet(): Unit = {
    assertEquals(System.getProperty("user.home"), System.getenv("HOME"))
  }

  @Test def propertyUserDirShouldBeSet(): Unit = {
    assertEquals(
      System.getProperty("user.dir"),
      System.getenv("SCALA_NATIVE_USER_DIR")
    )
  }

}
