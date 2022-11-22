package scala.scalanative.codegen

import scala.scalanative.build.Config

private[scalanative] case class PlatformInfo(
    targetTriple: Option[String],
    is32BitPlatform: Boolean
) {
  val sizeOfPtr = if (is32BitPlatform) 4 else 8
}
object PlatformInfo {
  def apply(config: Config): PlatformInfo = PlatformInfo(
    targetTriple = config.compilerConfig.targetTriple,
    is32BitPlatform = config.compilerConfig.is32BitPlatform
  )
}
