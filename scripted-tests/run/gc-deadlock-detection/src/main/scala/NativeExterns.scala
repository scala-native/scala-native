import scala.scalanative.unsafe._

/** Native extern declarations for GC deadlock tests.
 */

/** Native calls WITHOUT @blocking - causes GC deadlock */
@extern
object NativeBlocking {
  def read(fd: CInt, buf: Ptr[Byte], count: CSize): CSSize = extern
  def pause(): CInt = extern
}

/** Native calls WITH @blocking - GC can proceed */
@extern
object NativeBlockingCorrect {
  @blocking
  def read(fd: CInt, buf: Ptr[Byte], count: CSize): CSSize = extern
  @blocking
  def pause(): CInt = extern
}

/** Zombie thread helper functions from zombie_thread.c */
@extern
object ZombieThread {
  @name("zombie_thread_exit_no_cleanup")
  def exitNoCleanup(): Unit = extern

  @name("zombie_thread_native_sleep_ms")
  def nativeSleepMs(ms: CInt): Unit = extern
}
