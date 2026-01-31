package scala.scalanative.sbtplugin

object ScalaNativePlatform {

  /** Identifier of Scala Native platform for currently used version, e.g.
   *  "native0.5" Can be used in sbt 2.x build definitions to refer to the
   *  current platform.
   */
  val current: String = ScalaNativeCrossVersion.scalaNativePrefix
}
