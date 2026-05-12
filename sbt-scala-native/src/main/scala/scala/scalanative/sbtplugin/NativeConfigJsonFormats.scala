package scala.scalanative
package sbtplugin

import java.nio.file.{Path, Paths}

import scala.scalanative.build.*
import scala.scalanative.nir

import sjsonnew.BasicJsonProtocol.{
  BooleanJsonFormat, DoubleJsonFormat, FloatJsonFormat, IntJsonFormat,
  LongJsonFormat, StringJsonFormat, optionFormat, seqFormat, vectorFormat
}
import sjsonnew.{Builder, JsonFormat, Unbuilder, deserializationError}

/** sbt 2.x caches task outputs and requires a [[sjsonnew.JsonFormat]] for the
 *  result type. [[NativeConfig]] is encoded structurally: one [[JsonFormat]]
 *  per field type (and nested config), composed here.
 *
 *  [[ScalaNativePlugin.autoImport.nativeConfigJsonFormat]] delegates to
 *  [[NativeConfigJsonFormats.NativeConfigCodec]] so `build.sbt` sees it when
 *  the plugin is enabled.
 */
object NativeConfigJsonFormats {

// scalafmt: { maxColumn = 120 }

  /** Thrown when a JSON string does not match one of the known wire encodings. */
  final class DeserializationError(val value: String, val expected: Set[String], message: String)
      extends RuntimeException(message)

  object DeserializationError {
    def apply(value: String, expected: Set[String]): Nothing = {
      val msg =
        s"invalid value '$value', expected one of: ${expected.toSeq.sorted.mkString(", ")}"
      throw new DeserializationError(value, expected, msg)
    }
  }

  // --- BuildTarget ---------------------------------------------------------------------------

  implicit object BuildTargetJsonFormat extends JsonFormat[BuildTarget] {
    private object Values {
      final val Application = "application"
      final val LibraryDynamic = "libraryDynamic"
      final val LibraryStatic = "libraryStatic"
      val all: Set[String] = Set(Application, LibraryDynamic, LibraryStatic)
    }

    private def toJson(value: BuildTarget): String =
      value match {
        case BuildTarget.Application    => Values.Application
        case BuildTarget.LibraryDynamic => Values.LibraryDynamic
        case BuildTarget.LibraryStatic  => Values.LibraryStatic
      }

    private def fromJson(value: String): BuildTarget =
      value match {
        case Values.Application    => BuildTarget.application
        case Values.LibraryDynamic => BuildTarget.libraryDynamic
        case Values.LibraryStatic  => BuildTarget.libraryStatic
        case other                 => DeserializationError(other, Values.all)
      }

    def write[J](obj: BuildTarget, builder: Builder[J]): Unit =
      StringJsonFormat.write(toJson(obj), builder)

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): BuildTarget =
      fromJson(StringJsonFormat.read(jsOpt, unbuilder))
  }

  // --- JVM memory model -----------------------------------------------------------------------

  implicit object JvmMemoryModelComplianceJsonFormat extends JsonFormat[JVMMemoryModelCompliance] {
    private object Values {
      final val None = "none"
      final val Relaxed = "relaxed"
      final val Strict = "strict"
      val all: Set[String] = Set(None, Relaxed, Strict)
    }

    /** Wire string for JSON (also used by nested formats, e.g. semantics). */
    def toJson(value: JVMMemoryModelCompliance): String =
      value match {
        case JVMMemoryModelCompliance.None    => Values.None
        case JVMMemoryModelCompliance.Relaxed => Values.Relaxed
        case JVMMemoryModelCompliance.Strict  => Values.Strict
      }

    def fromJson(value: String): JVMMemoryModelCompliance =
      value match {
        case Values.None    => JVMMemoryModelCompliance.None
        case Values.Relaxed => JVMMemoryModelCompliance.Relaxed
        case Values.Strict  => JVMMemoryModelCompliance.Strict
        case other          => DeserializationError(other, Values.all)
      }

    def write[J](obj: JVMMemoryModelCompliance, builder: Builder[J]): Unit =
      StringJsonFormat.write(toJson(obj), builder)

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): JVMMemoryModelCompliance =
      fromJson(StringJsonFormat.read(jsOpt, unbuilder))
  }

  // --- GC / LTO / Mode / Sanitizer -------------------------------------------------------------

  implicit object GcJsonFormat extends JsonFormat[GC] {
    private object Values {
      final val None = "none"
      final val Boehm = "boehm"
      final val Immix = "immix"
      final val Commix = "commix"
      final val Experimental = "experimental"
      val all: Set[String] = Set(None, Boehm, Immix, Commix, Experimental)
    }

    def toJson(value: GC): String =
      value match {
        case GC.None         => Values.None
        case GC.Boehm        => Values.Boehm
        case GC.Immix        => Values.Immix
        case GC.Commix       => Values.Commix
        case GC.Experimental => Values.Experimental
      }

    def fromJson(value: String): GC =
      value match {
        case Values.None         => GC.none
        case Values.Boehm        => GC.boehm
        case Values.Immix        => GC.immix
        case Values.Commix       => GC.commix
        case Values.Experimental => GC.experimental
        case other               => DeserializationError(other, Values.all)
      }

    def write[J](obj: GC, builder: Builder[J]): Unit =
      StringJsonFormat.write(toJson(obj), builder)

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): GC =
      fromJson(StringJsonFormat.read(jsOpt, unbuilder))
  }

  implicit object LtoJsonFormat extends JsonFormat[LTO] {
    private object Values {
      final val None = "none"
      final val Thin = "thin"
      final val Full = "full"
      val all: Set[String] = Set(None, Thin, Full)
    }

    def toJson(value: LTO): String =
      value match {
        case LTO.None => Values.None
        case LTO.Thin => Values.Thin
        case LTO.Full => Values.Full
      }

    def fromJson(value: String): LTO =
      value.toLowerCase match {
        case Values.None => LTO.none
        case Values.Thin => LTO.thin
        case Values.Full => LTO.full
        case other       => DeserializationError(other, Values.all)
      }

    def write[J](obj: LTO, builder: Builder[J]): Unit =
      StringJsonFormat.write(toJson(obj), builder)

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): LTO =
      fromJson(StringJsonFormat.read(jsOpt, unbuilder))
  }

  implicit object ModeJsonFormat extends JsonFormat[Mode] {
    private object Values {
      final val Debug = "debug"
      final val Release = "release"
      final val ReleaseFast = "release-fast"
      final val ReleaseSize = "release-size"
      final val ReleaseFull = "release-full"
      val all: Set[String] =
        Set(Debug, Release, ReleaseFast, ReleaseSize, ReleaseFull)
    }

    def toJson(value: Mode): String =
      value match {
        case Mode.Debug       => Values.Debug
        case Mode.ReleaseFast => Values.ReleaseFast
        case Mode.ReleaseSize => Values.ReleaseSize
        case Mode.ReleaseFull => Values.ReleaseFull
      }

    def fromJson(value: String): Mode =
      value match {
        case Values.Debug       => Mode.debug
        case Values.Release     => Mode.release
        case Values.ReleaseFast => Mode.releaseFast
        case Values.ReleaseSize => Mode.releaseSize
        case Values.ReleaseFull => Mode.releaseFull
        case other              => DeserializationError(other, Values.all)
      }

    def write[J](obj: Mode, builder: Builder[J]): Unit =
      StringJsonFormat.write(toJson(obj), builder)

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Mode =
      fromJson(StringJsonFormat.read(jsOpt, unbuilder))
  }

  implicit object SanitizerJsonFormat extends JsonFormat[Sanitizer] {
    private object Values {
      final val Address = "address"
      final val Thread = "thread"
      final val Undefined = "undefined"
      val all: Set[String] = Set(Address, Thread, Undefined)
    }

    private def toJson(value: Sanitizer): String =
      value match {
        case Sanitizer.AddressSanitizer            => Values.Address
        case Sanitizer.ThreadSanitizer             => Values.Thread
        case Sanitizer.UndefinedBehaviourSanitizer => Values.Undefined
      }

    private def fromJson(value: String): Sanitizer =
      value match {
        case Values.Address   => Sanitizer.AddressSanitizer
        case Values.Thread    => Sanitizer.ThreadSanitizer
        case Values.Undefined => Sanitizer.UndefinedBehaviourSanitizer
        case other            => DeserializationError(other, Values.all)
      }

    def write[J](obj: Sanitizer, builder: Builder[J]): Unit =
      StringJsonFormat.write(toJson(obj), builder)

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Sanitizer =
      fromJson(StringJsonFormat.read(jsOpt, unbuilder))
  }

  /** Deterministic key order for stable task-cache hashing. */
  private object MapStringVecStringJsonFormat extends JsonFormat[Map[String, Vector[String]]] {
    def write[J](m: Map[String, Vector[String]], builder: Builder[J]): Unit = {
      builder.beginObject()
      m.toSeq.sortBy(_._1).foreach {
        case (k, vs) =>
          builder.addFieldName(k)
          implicitly[JsonFormat[Vector[String]]].write(vs, builder)
      }
      builder.endObject()
    }

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Map[String, Vector[String]] = {
      val js = jsOpt.getOrElse(deserializationError("expected JSON object for service providers"))
      val n = unbuilder.beginObject(js)
      val buf = scala.collection.mutable.Map.empty[String, Vector[String]]
      var i = 0
      while (i < n) {
        val (k, vOpt) = unbuilder.nextFieldOpt()
        val vs = vOpt
          .map(vjs => implicitly[JsonFormat[Vector[String]]].read(Some(vjs), unbuilder))
          .getOrElse(Vector.empty)
        buf(k) = vs
        i += 1
      }
      unbuilder.endObject()
      buf.toMap
    }
  }

  // --- Service providers (Iterable normalized to Seq) ----------------------------------------

  implicit object ServiceProvidersJsonFormat extends JsonFormat[Map[String, Iterable[String]]] {
    def write[J](obj: Map[String, Iterable[String]], builder: Builder[J]): Unit =
      MapStringVecStringJsonFormat.write(obj.map { case (k, v) => (k, v.toVector) }, builder)

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Map[String, Iterable[String]] =
      MapStringVecStringJsonFormat
        .read(jsOpt, unbuilder)
        .map { case (k, v) => (k, v: Iterable[String]) }
  }

  // --- Optimizer / semantics / source-level debugging ----------------------------------------

  implicit object OptimizerConfigJsonFormat extends JsonFormat[OptimizerConfig] {
    private object Field {
      final val MaxInlineDepth = "maxInlineDepth"
      final val MaxCallerSize = "maxCallerSize"
      final val MaxCalleeSize = "maxCalleeSize"
      final val SmallFunctionSize = "smallFunctionSize"
    }

    def write[J](obj: OptimizerConfig, builder: Builder[J]): Unit = {
      builder.beginObject()
      builder.addField(Field.MaxInlineDepth, obj.maxInlineDepth)
      builder.addField(Field.MaxCallerSize, obj.maxCallerSize)
      builder.addField(Field.MaxCalleeSize, obj.maxCalleeSize)
      builder.addField(Field.SmallFunctionSize, obj.smallFunctionSize)
      builder.endObject()
    }

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): OptimizerConfig = {
      val js = jsOpt.getOrElse(deserializationError("OptimizerConfig: missing JSON"))
      unbuilder.beginObject(js)
      try
        OptimizerConfig.empty
          .withMaxInlineDepth(unbuilder.readField[Int](Field.MaxInlineDepth))
          .withMaxCallerSize(unbuilder.readField[Int](Field.MaxCallerSize))
          .withMaxCalleeSize(unbuilder.readField[Int](Field.MaxCalleeSize))
          .withSmallFunctionSize(unbuilder.readField[Int](Field.SmallFunctionSize))
      finally unbuilder.endObject()
    }
  }

  implicit object SemanticsConfigJsonFormat extends JsonFormat[SemanticsConfig] {
    private object Field {
      final val FinalFields = "finalFields"
      final val StrictExternCallSemantics = "strictExternCallSemantics"
    }

    def write[J](obj: SemanticsConfig, builder: Builder[J]): Unit = {
      builder.beginObject()
      builder.addField(Field.FinalFields, JvmMemoryModelComplianceJsonFormat.toJson(obj.finalFields))
      builder.addField(Field.StrictExternCallSemantics, obj.strictExternCallSemantics)
      builder.endObject()
    }

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): SemanticsConfig = {
      val js = jsOpt.getOrElse(deserializationError("SemanticsConfig: missing JSON"))
      unbuilder.beginObject(js)
      try
        SemanticsConfig.default
          .withFinalFields(
            JvmMemoryModelComplianceJsonFormat.fromJson(
              unbuilder.readField[String](Field.FinalFields)
            )
          )
          .withStrictExternCallSemantics(unbuilder.readField[Boolean](Field.StrictExternCallSemantics))
      finally unbuilder.endObject()
    }
  }

  implicit object SourceLevelDebuggingConfigJsonFormat extends JsonFormat[SourceLevelDebuggingConfig] {
    private object Field {
      final val Enabled = "enabled"
      final val GenerateFunctionSourcePositions = "generateFunctionSourcePositions"
      final val GenerateLocalVariables = "generateLocalVariables"
      final val CustomSourceRoots = "customSourceRoots"
    }

    def write[J](obj: SourceLevelDebuggingConfig, builder: Builder[J]): Unit = {
      builder.beginObject()
      builder.addField(Field.Enabled, obj.enabled)
      builder.addField(Field.GenerateFunctionSourcePositions, obj.generateFunctionSourcePositions)
      builder.addField(Field.GenerateLocalVariables, obj.generateLocalVariables)
      builder.addField(Field.CustomSourceRoots, obj.customSourceRoots.map(_.toString))
      builder.endObject()
    }

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): SourceLevelDebuggingConfig = {
      val js = jsOpt.getOrElse(deserializationError("SourceLevelDebuggingConfig: missing JSON"))
      unbuilder.beginObject(js)
      try {
        val enabled = unbuilder.readField[Boolean](Field.Enabled)
        val genFun = unbuilder.readField[Boolean](Field.GenerateFunctionSourcePositions)
        val genLoc = unbuilder.readField[Boolean](Field.GenerateLocalVariables)
        val roots = unbuilder.readField[Seq[String]](Field.CustomSourceRoots)
        SourceLevelDebuggingConfig.disabled
          .withCustomSourceRoots(roots.map(Paths.get(_)))
          .enabled(enabled)
          .generateFunctionSourcePositions(genFun)
          .generateLocalVariables(genLoc)
      } finally unbuilder.endObject()
    }
  }

  // --- Link-time properties (Map[String, Any]) ------------------------------------------------

  implicit object LinktimePropertiesJsonFormat extends JsonFormat[NativeConfig.LinktimeProperites] {

    private object Field {
      final val Key = "key"
      final val Tag = "tag"
      final val Value = "value"
    }

    /** Wire values for the JSON [[Field.Tag]] discriminator. */
    private object Tag {
      final val Boolean = "boolean"
      final val Byte = "byte"
      final val Char = "char"
      final val Short = "short"
      final val Int = "int"
      final val Long = "long"
      final val Float = "float"
      final val Double = "double"
      final val String = "string"
    }

    private def writePrimitive[J](v: Any, builder: Builder[J]): Unit = v match {
      case b: Boolean =>
        builder.addField(Field.Tag, Tag.Boolean)
        builder.addField(Field.Value, b)
      case b: Byte =>
        builder.addField(Field.Tag, Tag.Byte)
        builder.addField(Field.Value, b.toInt)
      case c: Char =>
        builder.addField(Field.Tag, Tag.Char)
        builder.addField(Field.Value, c.toInt)
      case s: Short =>
        builder.addField(Field.Tag, Tag.Short)
        builder.addField(Field.Value, s.toInt)
      case i: Int =>
        builder.addField(Field.Tag, Tag.Int)
        builder.addField(Field.Value, i)
      case l: Long =>
        builder.addField(Field.Tag, Tag.Long)
        builder.addField(Field.Value, l)
      case f: Float =>
        builder.addField(Field.Tag, Tag.Float)
        builder.addField(Field.Value, f)
      case d: Double =>
        builder.addField(Field.Tag, Tag.Double)
        builder.addField(Field.Value, d)
      case s: String =>
        builder.addField(Field.Tag, Tag.String)
        builder.addField(Field.Value, s)
      case _: nir.Val =>
        throw new IllegalArgumentException(
          "nativeConfig linktimeProperties cannot contain nir.Val entries when using sbt 2.x task caching; remove such entries or disable caching for that task."
        )
      case other =>
        throw new IllegalArgumentException(
          s"Unsupported nativeConfig linktimeProperties value: $other (${other.getClass})"
        )
    }

    def write[J](obj: NativeConfig.LinktimeProperites, builder: Builder[J]): Unit = {
      builder.beginArray()
      obj.toSeq.sortBy(_._1).foreach {
        case (k, v) =>
          builder.beginObject()
          builder.addField(Field.Key, k)
          writePrimitive(v, builder)
          builder.endObject()
      }
      builder.endArray()
    }

    def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): NativeConfig.LinktimeProperites = {
      val js = jsOpt.getOrElse(deserializationError("linktimeProperties: missing JSON"))
      val n = unbuilder.beginArray(js)
      val buf = scala.collection.mutable.ArrayBuffer.empty[(String, Any)]
      var i = 0
      while (i < n) {
        val entryJs = unbuilder.nextElement
        unbuilder.beginObject(entryJs)
        try {
          val key = unbuilder.readField[String](Field.Key)
          val tag = unbuilder.readField[String](Field.Tag)
          val valueJs = unbuilder.lookupField(Field.Value).getOrElse {
            deserializationError(s"""linktime entry missing "${Field.Value}"""")
          }
          val v = tag match {
            case Tag.Boolean => BooleanJsonFormat.read(Some(valueJs), unbuilder)
            case Tag.Byte    => IntJsonFormat.read(Some(valueJs), unbuilder).toByte
            case Tag.Char    => IntJsonFormat.read(Some(valueJs), unbuilder).toChar
            case Tag.Short   => IntJsonFormat.read(Some(valueJs), unbuilder).toShort
            case Tag.Int     => IntJsonFormat.read(Some(valueJs), unbuilder)
            case Tag.Long    => LongJsonFormat.read(Some(valueJs), unbuilder)
            case Tag.Float   => FloatJsonFormat.read(Some(valueJs), unbuilder)
            case Tag.Double  => DoubleJsonFormat.read(Some(valueJs), unbuilder)
            case Tag.String  => StringJsonFormat.read(Some(valueJs), unbuilder)
            case other       => deserializationError(s"Unknown linktime value tag: $other")
          }
          buf += ((key, v))
        } finally unbuilder.endObject()
        i += 1
      }
      unbuilder.endArray()
      buf.toMap
    }
  }

  // --- NativeConfig ----------------------------------------------------------------------------

  private[sbtplugin] implicit object NativeConfigCodec extends JsonFormat[NativeConfig] {

    private object Field {
      final val Clang = "clang"
      final val ClangPP = "clangPP"
      final val LinkingOptions = "linkingOptions"
      final val CompileOptions = "compileOptions"
      final val COptions = "cOptions"
      final val CppOptions = "cppOptions"
      final val TargetTriple = "targetTriple"
      final val Gc = "gc"
      final val Lto = "lto"
      final val Mode = "mode"
      final val BuildTarget = "buildTarget"
      final val Check = "check"
      final val CheckFatalWarnings = "checkFatalWarnings"
      final val CheckFeatures = "checkFeatures"
      final val Dump = "dump"
      final val Sanitizer = "sanitizer"
      final val LinkStubs = "linkStubs"
      final val Optimize = "optimize"
      final val UseIncrementalCompilation = "useIncrementalCompilation"
      final val Multithreading = "multithreading"
      final val LinktimeProperties = "linktimeProperties"
      final val EmbedResources = "embedResources"
      final val ResourceIncludePatterns = "resourceIncludePatterns"
      final val ResourceExcludePatterns = "resourceExcludePatterns"
      final val ServiceProviders = "serviceProviders"
      final val BaseName = "baseName"
      final val OptimizerConfig = "optimizerConfig"
      final val SourceLevelDebuggingConfig = "sourceLevelDebuggingConfig"
      final val SemanticsConfig = "semanticsConfig"
    }

    override def write[J](obj: NativeConfig, builder: Builder[J]): Unit = {
      builder.beginObject()
      builder.addField(Field.Clang, obj.clang.toString)
      builder.addField(Field.ClangPP, obj.clangPP.toString)
      builder.addField(Field.LinkingOptions, obj.linkingOptions)
      builder.addField(Field.CompileOptions, obj.compileOptions)
      builder.addField(Field.COptions, obj.cOptions)
      builder.addField(Field.CppOptions, obj.cppOptions)
      builder.addField(Field.TargetTriple, obj.targetTriple)
      builder.addField(Field.Gc, obj.gc)
      builder.addField(Field.Lto, obj.lto)
      builder.addField(Field.Mode, obj.mode)
      builder.addField(Field.BuildTarget, obj.buildTarget)
      builder.addField(Field.Check, obj.check)
      builder.addField(Field.CheckFatalWarnings, obj.checkFatalWarnings)
      builder.addField(Field.CheckFeatures, obj.checkFeatures)
      builder.addField(Field.Dump, obj.dump)
      builder.addField(Field.Sanitizer, obj.sanitizer)
      builder.addField(Field.LinkStubs, obj.linkStubs)
      builder.addField(Field.Optimize, obj.optimize)
      builder.addField(Field.UseIncrementalCompilation, obj.useIncrementalCompilation)
      builder.addField(Field.Multithreading, obj.multithreading)
      builder.addField(Field.LinktimeProperties, obj.linktimeProperties)
      builder.addField(Field.EmbedResources, obj.embedResources)
      builder.addField(Field.ResourceIncludePatterns, obj.resourceIncludePatterns)
      builder.addField(Field.ResourceExcludePatterns, obj.resourceExcludePatterns)
      builder.addField(Field.ServiceProviders, obj.serviceProviders)
      builder.addField(Field.BaseName, obj.baseName)
      builder.addField(Field.OptimizerConfig, obj.optimizerConfig)
      builder.addField(Field.SourceLevelDebuggingConfig, obj.sourceLevelDebuggingConfig)
      builder.addField(Field.SemanticsConfig, obj.semanticsConfig)
      builder.endObject()
    }

    override def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): NativeConfig = {
      val js = jsOpt.getOrElse(deserializationError("NativeConfig: missing JSON"))
      unbuilder.beginObject(js)
      try
        NativeConfig.empty
          .withClang(Paths.get(unbuilder.readField[String](Field.Clang)))
          .withClangPP(Paths.get(unbuilder.readField[String](Field.ClangPP)))
          .withLinkingOptions(_ => unbuilder.readField[Seq[String]](Field.LinkingOptions))
          .withCompileOptions(_ => unbuilder.readField[Seq[String]](Field.CompileOptions))
          .withCOptions(_ => unbuilder.readField[Seq[String]](Field.COptions))
          .withCppOptions(_ => unbuilder.readField[Seq[String]](Field.CppOptions))
          .withTargetTriple(unbuilder.readField[Option[String]](Field.TargetTriple))
          .withGC(unbuilder.readField[GC](Field.Gc))
          .withLTO(unbuilder.readField[LTO](Field.Lto))
          .withMode(unbuilder.readField[Mode](Field.Mode))
          .withBuildTarget(unbuilder.readField[BuildTarget](Field.BuildTarget))
          .withCheck(unbuilder.readField[Boolean](Field.Check))
          .withCheckFatalWarnings(unbuilder.readField[Boolean](Field.CheckFatalWarnings))
          .withCheckFeatures(unbuilder.readField[Boolean](Field.CheckFeatures))
          .withDump(unbuilder.readField[Boolean](Field.Dump))
          .withSanitizer(unbuilder.readField[Option[Sanitizer]](Field.Sanitizer))
          .withLinkStubs(unbuilder.readField[Boolean](Field.LinkStubs))
          .withOptimize(unbuilder.readField[Boolean](Field.Optimize))
          .withIncrementalCompilation(unbuilder.readField[Boolean](Field.UseIncrementalCompilation))
          .withMultithreading(unbuilder.readField[Option[Boolean]](Field.Multithreading))
          .withLinktimeProperties(_ => unbuilder.readField[NativeConfig.LinktimeProperites](Field.LinktimeProperties))
          .withEmbedResources(unbuilder.readField[Boolean](Field.EmbedResources))
          .withResourceIncludePatterns(unbuilder.readField[Seq[String]](Field.ResourceIncludePatterns))
          .withResourceExcludePatterns(unbuilder.readField[Seq[String]](Field.ResourceExcludePatterns))
          .withServiceProviders(
            ServiceProvidersJsonFormat.read(
              unbuilder.lookupField(Field.ServiceProviders),
              unbuilder
            )
          )
          .withBaseName(unbuilder.readField[String](Field.BaseName))
          .withOptimizerConfig(_ => unbuilder.readField[OptimizerConfig](Field.OptimizerConfig))
          .withSourceLevelDebuggingConfig(_ =>
            unbuilder.readField[SourceLevelDebuggingConfig](Field.SourceLevelDebuggingConfig)
          )
          .withSemanticsConfig(_ => unbuilder.readField[SemanticsConfig](Field.SemanticsConfig))
      finally unbuilder.endObject()
    }
  }

}
