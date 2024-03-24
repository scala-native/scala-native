package scala.scalanative

package libc
import scala.scalanative.unsafe._

@extern object fenv extends fenv

@extern
@define("__SCALANATIVE_C_FENV")
private[scalanative] trait fenv {
  type fexcept_t = CStruct0
  type fenv_t = CStruct0

  /** Attempts to clear the floating-point exceptions that are listed in the
   *  bitmask argument excepts, which is a bitwise OR of the floating-point
   *  exception macros.
   *  @param excepts
   *    bitmask listing the exception flags to clear
   *  @return
   *    ​0​ if all indicated exceptions were successfully cleared or if excepts
   *    is zero. A non-zero value on error.
   */
  def feclearexcept(excepts: CInt): CInt = extern

  /** Determines which of the specified subset of the floating-point exceptions
   *  are currently set. The argument excepts is a bitwise OR of the
   *  floating-point exception macros.
   *  @param excepts
   *    bitmask listing the exception flags to test
   *
   *  @return
   *    Bitwise OR of the floating-point exception macros that are both included
   *    in excepts and correspond to floating-point exceptions currently set.
   */
  def fetestexcept(excepts: CInt): CInt = extern

  /** Attempts to raise all floating-point exceptions listed in excepts (a
   *  bitwise OR of the floating-point exception macros). If one of the
   *  exceptions is FE_OVERFLOW or FE_UNDERFLOW, this function may additionally
   *  raise FE_INEXACT. The order in which the exceptions are raised is
   *  unspecified, except that FE_OVERFLOW and FE_UNDERFLOW are always raised
   *  before FE_INEXACT.
   *
   *  @param excepts
   *    bitmask listing the exception flags to raise
   *  @return
   *    ​0​ if all listed exceptions were raised, non-zero value otherwise.
   */
  def feraiseexcept(excepts: CInt): CInt = extern

  /** Attempts to obtain the full contents of the floating-point exception flags
   *  that are listed in the bitmask argument excepts, which is a bitwise OR of
   *  the floating-point exception macros.
   *
   *  @param flagp
   *    pointer to an fexcept_t object where the flags will be stored or read
   *    from
   *  @param excepts
   *    bitmask listing the exception flags to get/set
   *  @return
   *    ​0​ on success, non-zero otherwise.
   */
  def fegetexceptflag(flagp: Ptr[fexcept_t], excepts: CInt): CInt = extern

  /** Attempts to copy the full contents of the floating-point exception flags
   *  that are listed in excepts from flagp into the floating-point environment.
   *  Does not raise any exceptions, only modifies the flags.
   *
   *  @param flagp
   *    pointer to an fexcept_t object where the flags will be stored or read
   *    from
   *  @param excepts
   *    bitmask listing the exception flags to get/set
   *  @return
   *    ​0​ on success, non-zero otherwise.
   */
  def fesetexceptflag(flagp: Ptr[fexcept_t], excepts: CInt): CInt = extern

  /** Attempts to establish the floating-point rounding direction equal to the
   *  argument round, which is expected to be one of the floating-point rounding
   *  macros.
   *
   *  @param round
   *    rounding direction, one of floating-point rounding macros
   *  @return
   *    ​0​ on success, non-zero otherwise.
   */
  def fesetround(round: CInt): CInt = extern

  /** Returns the value of the floating-point rounding macro that corresponds to
   *  the current rounding direction.
   *
   *  @return
   *    the floating-point rounding macro describing the current rounding
   *    direction or a negative value if the direction cannot be determined.
   */
  def fegetround(): CInt = extern

  /** 1) Attempts to store the status of the floating-point environment in the
   *  object pointed to by envp.
   *
   *  @param envp
   *    pointer to the object of type fenv_t which holds the status of the
   *    floating-point environment
   *  @return
   *    ​0​ on success, non-zero otherwise.
   */
  def fegetenv(envp: Ptr[fenv_t]): CInt = extern

  /** 2) Attempts to establish the floating-point environment from the object
   *  pointed to by envp. The value of that object must be previously obtained
   *  by a call to feholdexcept or fegetenv or be a floating-point macro
   *  constant. If any of the floating-point status flags are set in envp, they
   *  become set in the environment (and are then testable with fetestexcept),
   *  but the corresponding floating-point exceptions are not raised (execution
   *  continues uninterrupted)
   *  @param envp
   *    pointer to the object of type fenv_t which holds the status of the
   *    floating-point environment
   *  @return
   *    ​0​ on success, non-zero otherwise.
   */
  def fesetenv(envp: Ptr[fenv_t]): CInt = extern

  /** First, saves the current floating-point environment to the object pointed
   *  to by envp (similar to fegetenv), then clears all floating-point status
   *  flags, and then installs the non-stop mode: future floating-point
   *  exceptions will not interrupt execution (will not trap), until the
   *  floating-point environment is restored by feupdateenv or fesetenv.
   *
   *  This function may be used in the beginning of a subroutine that must hide
   *  the floating-point exceptions it may raise from the caller. If only some
   *  exceptions must be suppressed, while others must be reported, the non-stop
   *  mode is usually ended with a call to feupdateenv after clearing the
   *  unwanted exceptions.
   *
   *  @param envp
   *    pointer to the object of type fenv_t where the floating-point
   *    environment will be stored
   *  @return
   *    ​0​ on success, non-zero otherwise.
   */
  def feholdexcept(envp: Ptr[fenv_t]): CInt = extern

  /** First, remembers the currently raised floating-point exceptions, then
   *  restores the floating-point environment from the object pointed to by envp
   *  (similar to fesetenv), then raises the floating-point exceptions that were
   *  saved.
   *
   *  This function may be used to end the non-stop mode established by an
   *  earlier call to feholdexcept.
   *
   *  @param envp
   *    pointer to the object of type fenv_t set by an earlier call to
   *    feholdexcept or fegetenv or equal to FE_DFL_ENV
   *  @return
   *    ​0​ on success, non-zero otherwise.
   */
  def feupdateenv(envp: Ptr[fenv_t]): CInt = extern

  // floating-point exceptions

  /** pole error occurred in an earlier floating-point operation
   */
  @name("scalanative_fe_divbyzero")
  def FE_DIVBYZERO: CInt = extern

  /** inexact result: rounding was necessary to store the result of an earlier
   *  floating-point operation
   */
  @name("scalanative_fe_inexact")
  def FE_INEXACT: CInt = extern

  /** domain error occurred in an earlier floating-point operation
   */
  @name("scalanative_fe_invalid")
  def FE_INVALID: CInt = extern

  /** the result of an earlier floating-point operation was too large to be
   *  representable
   */
  @name("scalanative_fe_overflow")
  def FE_OVERFLOW: CInt = extern

  /** the result of an earlier floating-point operation was subnormal with a
   *  loss of precision
   */
  @name("scalanative_fe_underflow")
  def FE_UNDERFLOW: CInt = extern

  /** bitwise OR of all supported floating-point exceptions
   */
  @name("scalanative_fe_all_except")
  def FE_ALL_EXCEPT: CInt = extern

  // floating-point rounding direction
  /** rounding towards negative infinity
   */
  @name("scalanative_fe_downward")
  def FE_DOWNWARD: CInt = extern

  /** rounding towards nearest representable value
   */
  @name("scalanative_fe_tonearest")
  def FE_TONEAREST: CInt = extern

  /** rounding towards zero
   */
  @name("scalanative_fe_towardzero")
  def FE_TOWARDZERO: CInt = extern

  /** rounding towards positive infinity
   */
  @name("scalanative_fe_upward")
  def FE_UPWARD: CInt = extern

}
