package scala.scalanative.posix

import scalanative.unsafe._
import scalanative.unsafe.Nat._

import scala.scalanative.posix.sys.types

/** POSIX spawn.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 *
 *  A method with a PS comment indicates it is defined in POSIX extension
 *  "Process Scheduling", not base POSIX.
 */
@extern
@define("__SCALANATIVE_POSIX_SPAWN")
object spawn {

  /* posix_spawnattr_t & posix_spawn_file_actions_t are opaque bulk storage
   * types. They have no user accessible fields, not even the component Bytes.
   * Users of these types should leave them opaque: i.e. no read, no write.
   *
   * The sizes here are from 64 bit Linux 6.0.  Code in spawn.c checks at
   * compile-time that these sizes are greater than or equal to the equivalent
   * OS types.
   *
   * Use CUnsignedLongLong as array elements so that Array is sure to be
   * 64 bit aligned; may be overkill.
   *
   * Maintainers:  If you change sizes, either here or in spawn.c, change
   *               the other file also.
   */

  // Overall required 336 bytes, 8 * 42
  type posix_spawnattr_t = CArray[CUnsignedLongLong, Nat.Digit2[_4, _2]]

  // Overall required 80 bytes, 8 * 10
  type posix_spawn_file_actions_t =
    CArray[CUnsignedLongLong, Nat.Digit2[_1, _0]]

  type mode_t = types.mode_t
  type pid_t = types.pid_t
  type sigset_t = signal.sigset_t
  type sched_param = sched.sched_param

  def posix_spawn(
      pid: Ptr[pid_t],
      path: CString,
      file_actions: Ptr[posix_spawn_file_actions_t],
      attrp: Ptr[posix_spawnattr_t],
      argv: Ptr[CString],
      envp: Ptr[CString]
  ): CInt = extern

  def posix_spawn_file_actions_addclose(
      file_actions: Ptr[posix_spawn_file_actions_t],
      filedes: CInt
  ): CInt = extern

  def posix_spawn_file_actions_adddup2(
      file_actions: Ptr[posix_spawn_file_actions_t],
      filedes: CInt,
      newfiledes: CInt
  ): CInt = extern

  def posix_spawn_file_actions_open(
      file_actions: Ptr[posix_spawn_file_actions_t],
      filedes: CInt,
      path: CString,
      oflag: CInt,
      mode: mode_t
  ): CInt = extern

  def posix_spawn_file_actions_destroy(
      file_actions: Ptr[posix_spawn_file_actions_t]
  ): CInt = extern

  def posix_spawn_file_actions_init(
      file_actions: Ptr[posix_spawn_file_actions_t]
  ): CInt = extern

  def posix_spawnattr_destroy(
      attr: Ptr[posix_spawnattr_t]
  ): CInt = extern

  def posix_spawnattr_getflags(
      attr: Ptr[posix_spawnattr_t],
      flags: Ptr[CShort]
  ): CInt = extern

  def posix_spawnattr_getpgroup(
      attr: Ptr[posix_spawnattr_t],
      pgroup: Ptr[pid_t]
  ): CInt = extern

  /** PS */
  def posix_spawnattr_getschedparam(
      attr: Ptr[posix_spawnattr_t],
      schedparam: Ptr[sched_param]
  ): CInt = extern

  /** PS */
  def posix_spawnattr_getschedpolicy(
      attr: Ptr[posix_spawnattr_t],
      schedpolicy: Ptr[CInt]
  ): CInt = extern

  def posix_spawnattr_getsigdefault(
      attr: Ptr[posix_spawnattr_t],
      sigdefault: Ptr[sigset_t]
  ): CInt = extern

  def posix_spawnattr_getsigmask(
      attr: Ptr[posix_spawnattr_t],
      sigmask: Ptr[sigset_t]
  ): CInt = extern

  def posix_spawnattr_init(
      attr: Ptr[posix_spawnattr_t]
  ): CInt = extern

  def posix_spawnattr_setflags(
      attr: Ptr[posix_spawnattr_t],
      flags: CShort
  ): CInt = extern

  def posix_spawnattr_setpgroup(
      attr: Ptr[posix_spawnattr_t],
      pgroup: pid_t
  ): CInt = extern

  /** PS */
  def posix_spawnattr_setschedparam(
      attr: Ptr[posix_spawnattr_t],
      schedparam: Ptr[sched_param]
  ): CInt = extern

  /** PS */
  def posix_spawnattr_getschedpolicy(
      attr: Ptr[posix_spawnattr_t],
      schedpolicy: CInt
  ): CInt = extern

  def posix_spawnattr_setsigdefault(
      attr: Ptr[posix_spawnattr_t],
      sigdefault: Ptr[sigset_t]
  ): CInt = extern

  def posix_spawnattr_setsigmask(
      attr: Ptr[posix_spawnattr_t],
      sigmask: Ptr[sigset_t]
  ): CInt = extern

  def posix_spawnp(
      pid: Ptr[pid_t],
      file: CString,
      file_actions: Ptr[posix_spawn_file_actions_t],
      attrp: Ptr[posix_spawnattr_t],
      argv: Ptr[CString],
      envp: Ptr[CString]
  ): CInt = extern

// Symbolic constants

  @name("scalanative_posix_spawn_posix_spawn_resetids")
  def POSIX_SPAWN_RESETIDS: CInt = extern

  @name("scalanative_posix_spawn_posix_spawn_setpgroup")
  def POSIX_SPAWN_SETPGROUP: CInt = extern

  /** PS - Unsupported (zero) on Apple */
  @name("scalanative_posix_spawn_setschedparam")
  def POSIX_SPAWN_SETSCHEDPARAM: CInt = extern

  /** PS - Unsupported (zero) on Apple */
  @name("scalanative_posix_spawn_setscheduler")
  def POSIX_SPAWN_SETSCHEDULER: CInt = extern

  @name("scalanative_posix_spawn_setsigdef")
  def POSIX_SPAWN_SETSIGDEF: CInt = extern

  @name("scalanative_posix_spawn_setsigmask")
  def POSIX_SPAWN_SETSIGMASK: CInt = extern

}
