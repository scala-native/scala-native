package build

import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.core.ProblemFilters._

object BinaryIncompatibilities {
  type Filters = Seq[ProblemFilter]
  final val Util: Filters = Nil
  final val Nir: Filters = Seq(
    exclude[DirectMissingMethodProblem]("scala.scalanative.nir.Rt.*")
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
    exclude[Problem]("scala.scalanative.build.NativeConfig*Impl*"),
    exclude[Problem]("scala.scalanative.build.GC.this"),
    exclude[ReversedMissingMethodProblem](
      "scala.scalanative.build.NativeConfig*"
    )
  )

  final val NativeLib = Seq(
    // Internal usage
    exclude[DirectMissingMethodProblem]("java.lang._Class.rawty"),
    exclude[DirectMissingMethodProblem]("java.lang._Class.this"),
    // moved to auxlib
    exclude[MissingClassProblem]("scala.runtime.BoxesRunTime*"),
    // moved to javalib
    exclude[MissingClassProblem]("scala.scalanative.runtime.DeleteOnExit*"),
    // package-private
    exclude[MissingClassProblem]("scala.scalanative.runtime.*Shutdown*"),
    exclude[Problem]("scala.scalanative.runtime.ClassInstancesRegistry*"),
    exclude[Problem]("scala.scalanative.runtime.package*TypeOps*")
  )
  final val CLib: Filters = Nil
  final val PosixLib: Filters = Seq(
    exclude[IncompatibleResultTypeProblem](
      "scala.scalanative.posix.limits.PATH_MAX"
    )
  )
  final val WindowsLib: Filters = Nil

  final val AuxLib, JavaLib, ScalaLib: Filters = Nil
  final val TestRunner: Filters = Nil
  final val TestInterface: Filters = Nil
  final val TestInterfaceSbtDefs: Filters = Nil
  final val JUnitRuntime: Filters = Seq(
    // Internal method, package-private
    exclude[IncompatibleMethTypeProblem]("scala.scalanative.junit.Reporter.*")
  )

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
    "test-runner" -> TestRunner,
    "test-interface" -> TestInterface,
    "test-interface-sbt-defs" -> TestInterfaceSbtDefs,
    "junit-runtime" -> JUnitRuntime
  )
}
