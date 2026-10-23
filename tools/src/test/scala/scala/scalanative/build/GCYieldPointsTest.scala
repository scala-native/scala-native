package scala.scalanative.build

import org.junit.Assert._
import org.junit.Assume.assumeFalse
import org.junit.Test

class GCYieldPointsTest {
  @Test def environmentOverride(): Unit = {
    sys.env.get("SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS").foreach { value =>
      for (configured <- Seq(None, Some(false), Some(true))) {
        val config = NativeConfig.empty
          .withGC(GC.Commix)
          .withMultithreading(true)
          .withTrapBasedGCYieldPoints(configured)
        assertEquals(
          value == "1",
          Config.empty.withCompilerConfig(config).useTrapBasedGCYieldPoints
        )
      }
    }
  }
  @Test def configuration(): Unit = {
    assumeFalse(sys.env.contains("SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS"))
    def traps(native: NativeConfig): Boolean =
      Config.empty.withCompilerConfig(native).useTrapBasedGCYieldPoints
    val config = NativeConfig.empty.withGC(GC.Immix).withMultithreading(true)
    assertFalse(traps(config.withMode(Mode.debug)))
    assertTrue(traps(config.withMode(Mode.releaseFast)))
    assertFalse(
      traps(config.withMode(Mode.releaseFast).withTrapBasedGCYieldPoints(false))
    )
    assertTrue(
      traps(config.withMode(Mode.debug).withTrapBasedGCYieldPoints(true))
    )
    assertFalse(
      traps(config.withTrapBasedGCYieldPoints(true).withMultithreading(false))
    )
    assertFalse(traps(config.withTrapBasedGCYieldPoints(true).withGC(GC.None)))
    assertEquals(
      None,
      config
        .withTrapBasedGCYieldPoints(true)
        .withTrapBasedGCYieldPoints(None)
        .trapBasedGCYieldPoints
    )
  }
}
