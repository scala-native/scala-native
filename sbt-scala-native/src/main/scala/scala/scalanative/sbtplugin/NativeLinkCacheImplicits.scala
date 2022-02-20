package scala.scalanative.sbtplugin

import sjsonnew._
import java.nio.file.{Path, Paths}
import scala.scalanative.build
import sbt.util.CacheImplicits._

// Definitions of isomorphic functions used to convert cached values
// from and to json of types not covered by the sjsonnew.BasicJsonProtocol
// package. Used by the sjsonnew library, on which sbt caching also depends.
// Due to the structure of build.Config and build.NativeConfig,
// new values have to be added manually.
// Logger is ignored when caching.
private[sbtplugin] object NativeLinkCacheImplicits {

  // Meant to replace the "Any" when caching LinktimeProperties.
  // Based on the allowed types defined in NativeConfig.
  sealed abstract class LinktimePropertyValue(val any: Any)
  final case class LinktimePropertyBoolean(val value: Boolean)
      extends LinktimePropertyValue(value)
  final case class LinktimePropertyByte(val value: Byte)
      extends LinktimePropertyValue(value)
  final case class LinktimePropertyChar(val value: Char)
      extends LinktimePropertyValue(value)
  final case class LinktimePropertyShort(val value: Short)
      extends LinktimePropertyValue(value)
  final case class LinktimePropertyInt(val value: Int)
      extends LinktimePropertyValue(value)
  final case class LinktimePropertyLong(val value: Long)
      extends LinktimePropertyValue(value)
  final case class LinktimePropertyFloat(val value: Float)
      extends LinktimePropertyValue(value)
  final case class LinktimePropertyDouble(val value: Double)
      extends LinktimePropertyValue(value)
  final case class LinktimePropertyString(val value: String)
      extends LinktimePropertyValue(value)

  private def toLinktimeValue(any: Any): LinktimePropertyValue =
    any match {
      case b: Boolean => LinktimePropertyBoolean(b)
      case b: Byte    => LinktimePropertyByte(b)
      case b: Char    => LinktimePropertyChar(b)
      case b: Short   => LinktimePropertyShort(b)
      case b: Int     => LinktimePropertyInt(b)
      case b: Long    => LinktimePropertyLong(b)
      case b: Float   => LinktimePropertyFloat(b)
      case b: Double  => LinktimePropertyDouble(b)
      case b: String  => LinktimePropertyString(b)
    }

  implicit val ltvBooleanIso =
    LList.iso[LinktimePropertyBoolean, Boolean :*: LNil](
      { ltv: LinktimePropertyBoolean => ("boolean", ltv.value) :*: LNil },
      { case (_, value) :*: LNil => LinktimePropertyBoolean(value) }
    )
  implicit val ltvByteIso = LList.iso[LinktimePropertyByte, Byte :*: LNil](
    { ltv: LinktimePropertyByte => ("byte", ltv.value) :*: LNil },
    { case (_, value) :*: LNil => LinktimePropertyByte(value) }
  )
  implicit val ltvCharIso = LList.iso[LinktimePropertyChar, Char :*: LNil](
    { ltv: LinktimePropertyChar => ("char", ltv.value) :*: LNil },
    { case (_, value) :*: LNil => LinktimePropertyChar(value) }
  )
  implicit val ltvShortIso = LList.iso[LinktimePropertyShort, Short :*: LNil](
    { ltv: LinktimePropertyShort => ("short", ltv.value) :*: LNil },
    { case (_, value) :*: LNil => LinktimePropertyShort(value) }
  )
  implicit val ltvIntIso = LList.iso[LinktimePropertyInt, Int :*: LNil](
    { ltv: LinktimePropertyInt => ("int", ltv.value) :*: LNil },
    { case (_, value) :*: LNil => LinktimePropertyInt(value) }
  )
  implicit val ltvLongIso = LList.iso[LinktimePropertyLong, Long :*: LNil](
    { ltv: LinktimePropertyLong => ("long", ltv.value) :*: LNil },
    { case (_, value) :*: LNil => LinktimePropertyLong(value) }
  )
  implicit val ltvFloatIso = LList.iso[LinktimePropertyFloat, Float :*: LNil](
    { ltv: LinktimePropertyFloat => ("float", ltv.value) :*: LNil },
    { case (_, value) :*: LNil => LinktimePropertyFloat(value) }
  )
  implicit val ltvDoubleIso =
    LList.iso[LinktimePropertyDouble, Double :*: LNil](
      { ltv: LinktimePropertyDouble => ("double", ltv.value) :*: LNil },
      { case (_, ltv) :*: LNil => LinktimePropertyDouble(ltv) }
    )
  implicit val ltvStringIso =
    LList.iso[LinktimePropertyString, String :*: LNil](
      { ltv: LinktimePropertyString => ("string", ltv.value) :*: LNil },
      { case (_, value) :*: LNil => LinktimePropertyString(value) }
    )

  implicit val ltpValueUnion =
    flatUnionFormat9[
      LinktimePropertyValue,
      LinktimePropertyBoolean,
      LinktimePropertyByte,
      LinktimePropertyChar,
      LinktimePropertyShort,
      LinktimePropertyInt,
      LinktimePropertyLong,
      LinktimePropertyFloat,
      LinktimePropertyDouble,
      LinktimePropertyString
    ]("LinktimePropertyValue")

  implicit val pathIso =
    LList.iso[Path, String :*: LNil](
      { path: Path => ("path", path.toString()) :*: LNil },
      { case (_, pathStr) :*: LNil => Paths.get(pathStr) }
    )

  implicit val linktimePropertiesAnyIso =
    LList.iso[Any, LinktimePropertyValue :*: LNil](
      { any: Any => ("any", toLinktimeValue(any)) :*: LNil },
      { case (_, ltv) :*: LNil => ltv.any }
    )

  implicit val gcIso = LList.iso[build.GC, String :*: LNil](
    { gc: build.GC => ("gc", gc.toString()) :*: LNil },
    { case (_, str) :*: LNil => build.GC(str) }
  )

  implicit val modeIso = LList.iso[build.Mode, String :*: LNil](
    { mode: build.Mode => ("mode", mode.toString()) :*: LNil },
    { case (_, mode) :*: LNil => build.Mode(mode) }
  )

  implicit val ltoIso = LList.iso[build.LTO, String :*: LNil](
    { lto: build.LTO => ("lto", lto.toString()) :*: LNil },
    { case (_, str) :*: LNil => build.LTO(str) }
  )

  implicit val nativeConfigIso =
    LList.iso[
      build.NativeConfig,
      build.GC :*: build.Mode :*: Path :*: Path :*: Seq[String] :*: Seq[
        String
      ] :*: Option[
        String
      ] :*: Boolean :*: build.LTO :*: Boolean :*: Boolean :*: Boolean :*: Boolean :*: build.NativeConfig.LinktimeProperites :*: Boolean :*: LNil
    ](
      { c: build.NativeConfig =>
        ("gc", c.gc) :*: ("mode", c.mode) :*: ("clang", c.clang) :*: (
          "clangPP",
          c.clangPP
        ) :*: (
          "linkingOptions",
          c.linkingOptions
        ) :*: ("compileOptions", c.compileOptions) :*: (
          "targetTriple",
          c.targetTriple
        ) :*: ("linkStubs", c.linkStubs) :*: ("lto", c.lto) :*: (
          "check",
          c.check
        ) :*: ("checkFatalWarnings", c.checkFatalWarnings) :*: (
          "dump",
          c.dump
        ) :*: ("optimize", c.optimize) :*: (
          "linktimeProperties",
          c.linktimeProperties
        ) :*: ("embedResources", c.embedResources) :*: LNil
      },
      {
        case (_, gc) :*: (_, mode) :*: (_, clang) :*: (_, clangPP) :*: (
              _,
              linkingOptions
            ) :*: (
              _,
              compileOptions
            ) :*: (_, targetTriple) :*: (_, linkStubs) :*: (_, lto) :*: (
              _,
              check
            ) :*: (_, checkFatalWarnings) :*: (_, dump) :*: (_, optimize) :*: (
              _,
              linktimeProperties
            ) :*: (_, embedResources) :*: LNil =>
          build.NativeConfig.empty
            .withGC(gc)
            .withMode(mode)
            .withClang(clang)
            .withClangPP(clangPP)
            .withLinkingOptions(linkingOptions)
            .withCompileOptions(compileOptions)
            .withTargetTriple(targetTriple)
            .withLinkStubs(linkStubs)
            .withLTO(lto)
            .withCheck(check)
            .withCheckFatalWarnings(checkFatalWarnings)
            .withDump(dump)
            .withOptimize(optimize)
            .withLinktimeProperties(linktimeProperties)
            .withEmbedResources(embedResources)
      }
    )

  implicit val configIso =
    LList.iso[build.Config, Path :*: String :*: Seq[
      Path
    ] :*: build.NativeConfig :*: LNil](
      { c: build.Config =>
        ("workdir", c.workdir) :*: ("mainClass", c.mainClass) :*: (
          "classPath",
          c.classPath
        ) :*: ("compilerConfig", c.compilerConfig) :*: LNil
      },
      {
        case (_, workdir) :*: (_, mainClass) :*: (_, classPath) :*: (
              _,
              compilerConfig
            ) :*: LNil =>
          build.Config.empty
            .withMainClass(mainClass)
            .withClassPath(classPath)
            .withWorkdir(workdir)
            .withCompilerConfig(compilerConfig)
      }
    )
}
