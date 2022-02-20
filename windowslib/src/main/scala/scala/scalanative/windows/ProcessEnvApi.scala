package scala.scalanative.windows

import scala.scalanative.unsafe._

@extern()
object ProcessEnvApi {
  def GetEnvironmentStringsW(): CWString = extern
  def FreeEnvironmentStringsW(envBlockPtr: CWString): Boolean = extern
}
