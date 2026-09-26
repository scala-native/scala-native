import scala.scalanative.unsafe._

/** Native extern declarations for GC deadlock tests.
 *
 *  Platform-specific I/O (pipe, read, close) is in PlatformIO.scala. This file
 *  only contains POSIX-specific functions that have no Windows equivalent.
 */

/** Zombie thread helper functions from zombie_thread.c */
@extern
object ZombieThread {
  @name("zombie_thread_exit_no_cleanup")
  def exitNoCleanup(): Unit = extern

  @name("zombie_thread_native_sleep_ms")
  def nativeSleepMs(ms: CInt): Unit = extern
}
