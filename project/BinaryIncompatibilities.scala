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
    exclude[Problem]("scala.scalanative.build.Config*Impl*"),
    exclude[Problem]("scala.scalanative.build.NativeConfig*Impl*"),
    exclude[Problem]("scala.scalanative.build.GC.this"),
    exclude[ReversedMissingMethodProblem](
      "scala.scalanative.build.NativeConfig*"
    ),
    exclude[ReversedMissingMethodProblem]("scala.scalanative.build.Config*"),
    exclude[Problem]("scala.scalanative.build.Config*Impl*")
  )

  final val NativeLib = Seq(
    exclude[MissingClassProblem]("scala.scalanative.runtime.rtti*"),
    exclude[Problem]("scala.scalanative.runtime.Backtrace*"),
    exclude[Problem]("scala.scalanative.runtime.dwarf.DWARF*")
  )
  final val CLib: Filters = Nil
  final val PosixLib: Filters = Seq.empty
  final val WindowsLib: Filters = Nil

  final val TestRunner: Filters = Nil
  final val TestInterface: Filters = Nil
  final val TestInterfaceSbtDefs: Filters = Nil
  final val JUnitRuntime: Filters = Seq.empty

  val moduleFilters = Map(
    "util" -> Util,
    "nir" -> Nir,
    "nscplugin" -> NscPlugin,
    "tools" -> Tools,
    "clib" -> CLib,
    "posixlib" -> PosixLib,
    "windowslib" -> WindowsLib,
    "nativelib" -> NativeLib,
    "test-runner" -> TestRunner,
    "test-interface" -> TestInterface,
    "test-interface-sbt-defs" -> TestInterfaceSbtDefs,
    "junit-plugin" -> JUnitPlugin,
    "junit-runtime" -> JUnitRuntime
  )
}
