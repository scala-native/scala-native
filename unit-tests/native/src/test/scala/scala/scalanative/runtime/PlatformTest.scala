package scala.scalanative.runtime

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils

import scala.scalanative.runtime

class PlatformTest {
  @Test def isFreeBSD(): Unit = {
    val buildEnviron = utils.Platform.isFreeBSD // Test environment
    val runtimeEnviron = runtime.Platform.isFreeBSD() // Unit under test

    assertTrue(
      s"build: '${buildEnviron}' != runtime: '${runtimeEnviron}'",
      buildEnviron == runtimeEnviron
    )
  }
}
