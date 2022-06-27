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
    exclude[DirectMissingMethodProblem]("scala.scalanative.nir.Rt.*")
  )

  final val NscPlugin = Seq(
    exclude[DirectMissingMethodProblem]("scala.scalanative.nir.Rt.*"),
    exclude[IncompatibleMethTypeProblem](
      "scala.scalanative.nscplugin.NirCompat*"
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
    exclude[Problem]("scala.scalanative.build.NativeConfig*Impl*"),
    exclude[Problem]("scala.scalanative.build.GC.this"),
    exclude[ReversedMissingMethodProblem](
      "scala.scalanative.build.NativeConfig*"
    )
  )

  final val NativeLib = Seq()
  final val CLib: Filters = Nil
  final val PosixLib: Filters = Seq()
  final val WindowsLib: Filters = Nil

  final val AuxLib, JavaLib, ScalaLib, Scala3Lib: Filters = Nil
  final val TestRunner: Filters = Nil
  final val TestInterface: Filters = Nil
  final val TestInterfaceSbtDefs: Filters = Nil
  final val JUnitRuntime: Filters = Seq()

  val moduleFilters = Map(
    "util" -> Util,
    "nir" -> Nir,
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
    "junit-runtime" -> JUnitRuntime
  )
}
