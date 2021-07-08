package scala.scalanative.windows

import scala.scalanative.unsafe._

@extern()
object ProcessEnv {
  def GetEnvironmentStringsW(): CWString = extern
  def FreeEnvironmentStringsW(envBlockPtr: CWString): Boolean = extern
}
