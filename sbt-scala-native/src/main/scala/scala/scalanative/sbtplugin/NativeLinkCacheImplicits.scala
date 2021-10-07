package scala.scalanative.sbtplugin

import sjsonnew._
import java.nio.file.Path
import scala.scalanative.build
import sbt.{pathJsonFormatter => _, _}

// Definitions of isomorphic functions used to covert cached values
// from and to json of types not covered by the sjsonnew.BasicJsonProtocol 
// package. Used by the sjsonnew library, which sbt caching also depends on.
// Due to the structure of build.Config and build.NativeConfig,
// name values have to be added manually.
// Logger is ignored when caching.
private[sbtplugin] object NativeLinkCacheImplicits {
  import sbt.util.CacheImplicits._

  // TypeTag aware pseudo-union. Based on the allowed types
  // specified in NativeConfig.
  // Meant to replace "Any" when caching LinktimeProperties.
  private type LinktimePropertyValue = (
    Option[Boolean],
    Option[Byte],
    Option[Char],
    Option[Short],
    Option[Int],
    Option[Long],
    Option[Float],
    Option[Double],
    Option[String]
  )

  private def toLinktimeValue(a: Any): LinktimePropertyValue = {
    val n = None
    a match {
      case b: Boolean => (Some(b), n, n, n, n, n, n, n, n)
      case b: Byte    => (n, Some(b), n, n, n, n, n, n, n)
      case b: Char    => (n, n, Some(b), n, n, n, n, n, n)
      case b: Short   => (n, n, n, Some(b), n, n, n, n, n)
      case b: Int     => (n, n, n, n, Some(b), n, n, n, n)
      case b: Long    => (n, n, n, n, n, Some(b), n, n, n)
      case b: Float   => (n, n, n, n, n, n, Some(b), n, n)
      case b: Double  => (n, n, n, n, n, n, n, Some(b), n)
      case b: String  => (n, n, n, n, n, n, n, n, Some(b))
      case _          => throw new IllegalArgumentException()
    }
  }

  private def toAny(ltv: LinktimePropertyValue): Any = ltv match {
      case (Some(a), _, _, _, _, _, _, _, _) => a
      case (_, Some(a), _, _, _, _, _, _, _) => a
      case (_, _, Some(a), _, _, _, _, _, _) => a
      case (_, _, _, Some(a), _, _, _, _, _) => a
      case (_, _, _, _, Some(a), _, _, _, _) => a
      case (_, _, _, _, _, Some(a), _, _, _) => a
      case (_, _, _, _, _, _, Some(a), _, _) => a
      case (_, _, _, _, _, _, _, Some(a), _) => a
      case (_, _, _, _, _, _, _, _, Some(a)) => a
      case _ => throw new IllegalArgumentException()
    }

  implicit val linktimePropertiesAnyIso =
    LList.iso[Any, LinktimePropertyValue :*: LNil](
      { any: Any => ("any", toLinktimeValue(any)) :*: LNil },
      { case (_, ltv) :*: LNil => toAny(ltv) }
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
      ] :*: Boolean :*: build.LTO :*: Boolean :*: Boolean :*: Boolean :*: Boolean :*: build.NativeConfig.LinktimeProperites :*: LNil
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
        ) :*: LNil
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
            ) :*: LNil =>
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
