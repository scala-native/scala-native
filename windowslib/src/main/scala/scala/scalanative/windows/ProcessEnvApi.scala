package scala.scalanative.windows

import scala.scalanative.unsafe.*

@extern()
object ProcessEnvApi {
  def GetEnvironmentStringsW(): CWString = extern
  def FreeEnvironmentStringsW(envBlockPtr: CWString): Boolean = extern
}
