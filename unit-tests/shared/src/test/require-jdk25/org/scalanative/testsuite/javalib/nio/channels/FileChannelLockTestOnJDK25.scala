package org.scalanative.testsuite.javalib.nio.channels

import java.nio.channels.OverlappingFileLockException

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

// JDK 25: size 0 locks to the end of any file, as Long.MaxValue does.
class FileChannelLockTestOnJDK25 {
  import FileChannelLockTest._

  @Test def sizeZeroLocksToTheEnd(): Unit = withFile { (_, f) =>
    val ch = rw(f)
    try {
      val l = ch.lock(0, 0, false)
      assertEquals(Long.MaxValue, l.size())
      l.release()
      val t = ch.tryLock(10, 0, false)
      assertEquals(10L, t.position())
      assertEquals(Long.MaxValue - 10, t.size())
      val other = rw(f)
      try {
        assertNotNull(other.tryLock(0, 10, false))
        assertThrows(
          classOf[OverlappingFileLockException],
          other.tryLock(Long.MaxValue - 1, 1, false)
        )
      } finally other.close()
      t.release()
      // Nothing is left past Long.MaxValue.
      val last = ch.lock(Long.MaxValue, 0, false)
      assertEquals(0L, last.size())
      last.release()
    } finally ch.close()
  }
}
