package org.scalanative.testsuite.javalib.lang

import org.junit.Test
import org.junit.Assert._

class SystemWithPosixTest {
  @Test def systemCurrentTimeMillisSecondsShouldApproximatePosixTime(): Unit = {
    // This is a coarse-grain sanity check, primarily to ensure that 64 bit
    // math is being done on 32 bit systems. Only seconds are considered.

    // Taking the two time samples can not be done atomically, so the two
    // times can validly differ by one second. On a mighty slow system the
    // two times might differ by two.
    // Spurious false failures are expensive and annoying to track down,
    // be defensive and add _two_ extra seconds tolerance to expected
    // difference.

    import scalanative.posix.time.time

    val tolerance = 3

    val ctmMillis = System.currentTimeMillis()
    val cSeconds = time(null)

    // Truncate down to keep math simple & reduce number of bits in play.
    val ctmSeconds = ctmMillis / 1000

    assertEquals(cSeconds.toFloat, ctmSeconds.toFloat, tolerance.toFloat)
  }
}
