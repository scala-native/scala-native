package build

import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.core.ProblemFilters._

object BinaryIncompatibilities {
  type Filters = Seq[ProblemFilter]
  final val Util: Filters = Seq(
    // JDK distribution dependent methods, derived from java.lang.Throwable
    exclude[DirectMissingMethodProblem](
      "scala.scalanative.util.UnreachableException.getStackTraceDepth"
    ),
    exclude[DirectMissingMethodProblem](
      "scala.scalanative.util.UnreachableException.getStackTraceElement"
    )
  )
  final val Nir: Filters = Seq(
    exclude[DirectMissingMethodProblem]("scala.scalanative.nir.Rt.*"),
    // sealed trait replaced with sealed abstract class, used internally
    exclude[Problem]("scala.scalanative.nir.Sig$Scope*"),
    // artifact due to non-cleaned environment while publishing
    exclude[MissingClassProblem]("scala.scalanative.nir.Attr$Volatile$"),
    // Scala 2.11 is no longer supported
    exclude[DirectMissingMethodProblem]("scala.scalanative.nir.Sig.isImplCtor")
  )

  final val NscPlugin = Seq(
    exclude[DirectMissingMethodProblem]("scala.scalanative.nir.Rt.*"),
    exclude[IncompatibleMethTypeProblem](
      "scala.scalanative.nscplugin.NirCompat*"
    ),
    exclude[ReversedMissingMethodProblem](
      "scala.scalanative.nscplugin.NirGenStat.LinktimeProperty"
    )
  )
  final val JUnitPlugin: Filters = Nil

  final val Tools: Filters = Seq(
    exclude[Problem]("scala.scalanative.codegen.*"),
    exclude[Problem]("scala.scalanative.checker.*"),
    exclude[Problem]("scala.scalanative.interflow.*"),
    exclude[Problem]("scala.scalanative.linker.*"),
    exclude[Problem]("scala.scalanative.build.NativeLib.*"),
    exclude[Problem]("scala.scalanative.build.LLVM.*"),
    exclude[Problem]("scala.scalanative.build.Config*Impl*"),
    exclude[Problem]("scala.scalanative.build.NativeConfig*Impl*"),
    exclude[Problem]("scala.scalanative.build.GC.this"),
    exclude[ReversedMissingMethodProblem](
      "scala.scalanative.build.NativeConfig*"
    ),
    exclude[ReversedMissingMethodProblem]("scala.scalanative.build.Config.*"),
    // package private, moved to build.core
    exclude[MissingClassProblem]("scala.scalanative.build.Filter*"),
    exclude[MissingClassProblem]("scala.scalanative.build.IO*"),
    exclude[MissingClassProblem]("scala.scalanative.build.NativeLib*"),
    exclude[MissingClassProblem]("scala.scalanative.build.ScalaNative*")
  )

  final val NativeLib = Seq(
    // Internal usage
    exclude[DirectMissingMethodProblem]("scala.scalanative.regex.*"),
    exclude[DirectMissingMethodProblem]("java.lang._Class.rawty"),
    exclude[DirectMissingMethodProblem]("java.lang._Class.this"),
    exclude[MissingClassProblem]("scala.scalanative.unsafe.Zone$ZoneImpl*"),
    exclude[MissingClassProblem]("scala.scalanative.unsafe.package$MacroImpl$"),
    // Moved to unsafe package, source compatible change
    exclude[MissingClassProblem]("scala.scalanative.unsafe.extern"),
    // moved to auxlib
    exclude[MissingClassProblem]("scala.runtime.BoxesRunTime*"),
    // moved to javalib
    exclude[MissingClassProblem]("scala.scalanative.runtime.DeleteOnExit*"),
    // package-private
    exclude[MissingClassProblem]("scala.scalanative.runtime.*Shutdown*"),
    exclude[Problem]("scala.scalanative.runtime.ClassInstancesRegistry*"),
    exclude[Problem]("scala.scalanative.runtime.package*TypeOps*"),
    // Stub with incorrect signature
    exclude[Problem]("java.lang._Class.getConstructor"),
    // This package is not actually part of Java's stdlib, it only contains private classes
    // to handle embedded resources.
    exclude[Problem]("java.lang.resource.*"),
    // Forward compatible
    exclude[ReversedMissingMethodProblem](
      "scala.scalanative.runtime.Array.atRawUnsafe"
    )
  )
  final val CLib: Filters = Nil
  final val PosixLib: Filters = Seq(
    exclude[IncompatibleResultTypeProblem](
      "scala.scalanative.posix.limits.PATH_MAX"
    ),
    // Moved to javalib, used internally and in scripted-tests
    exclude[MissingClassProblem]("scala.scalanative.runtime.SocketHelpers*"),
    exclude[Problem]("scala.scalanative.posix.monetaryExtern*")
  )
  final val WindowsLib: Filters = Nil

  final val AuxLib, JavaLib, ScalaLib, Scala3Lib: Filters = Nil
  final val TestRunner: Filters = Seq(
    // exclude[Problem]("scala.scalanative.testinterface.ComRunner.*")
  )
  final val TestInterface: Filters = Seq(
    // internals
    exclude[Problem]("scala.scalanative.testinterface.TestMain.*")
    // exclude[Problem]("scala.scalanative.testinterface.NativeRPC.*")
  )
  final val TestInterfaceSbtDefs: Filters = Nil
  final val JUnitRuntime: Filters = Seq(
    // Internal method, package-private
    exclude[IncompatibleMethTypeProblem]("scala.scalanative.junit.Reporter.*")
  )

  val moduleFilters = Map(
    "util" -> Util,
    "nir" -> Nir,
    "nscplugin" -> NscPlugin,
    "tools" -> Tools,
    "clib" -> CLib,
    "posixlib" -> PosixLib,
    "windowslib" -> WindowsLib,
    "nativelib" -> NativeLib,
    "auxlib" -> AuxLib,
    "javalib" -> JavaLib,
    "scalalib" -> ScalaLib,
    "scala3lib" -> Scala3Lib,
    "test-runner" -> TestRunner,
    "test-interface" -> TestInterface,
    "test-interface-sbt-defs" -> TestInterfaceSbtDefs,
    "junit-plugin" -> JUnitPlugin,
    "junit-runtime" -> JUnitRuntime
  )
}
