package scala.scalanative
package libc

import native._

@extern object fenv {
  // @struct case class FEnvT(
  //   control: UShort,
  //   status: UShort,
  //   mxcsr: UInt,
  //   reserved: Array[Char] // TODO: Array[Char, 8] as in https://github.com/scala-native/scala-native/issues/35
  // )

  type FExceptT = UShort

  def feclearexcept(excepts: CInt): CInt = extern
  def fegetexceptflag(flagp: Ptr[FExceptT], excepts: CInt): CInt = extern
  def feraiseexcept(excepts: CInt): CInt = extern
  def fesetexceptflag(flagp: Ptr[FExceptT], excepts: CInt): CInt = extern
  def fetestexcept(excepts: CInt): CInt = extern
  def fegetround(): CInt = extern
  def fesetround(round: CInt): CInt = extern
  // def fegetenv(envp: Ptr[FEnvT]): CInt = extern
  // def feholdexcept(envp: Ptr[FEnvT]): CInt = extern
  // def fesetenv(envp: Ptr[FEnvT]): CInt = extern
  // def feupdateenv(envp: Ptr[FEnvT]): CInt = extern
}
