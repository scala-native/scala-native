// format: off
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
    // Conversion case-class -> class
    exclude[DirectMissingMethodProblem]("scala.scalanative.nir.Attrs._*"),
    exclude[DirectMissingMethodProblem]("scala.scalanative.nir.Attrs.fromProduct"),
    exclude[IncompatibleResultTypeProblem]("scala.scalanative.nir.Attrs.unapply"),
    exclude[MissingTypesProblem]("scala.scalanative.nir.Attrs$"),
  )

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
    exclude[Problem]("scala.scalanative.build.Config*Impl*"),
    // Should have never been public in the first place - contains local classpaths
    exclude[MissingClassProblem]("scala.scalanative.buildinfo.ScalaNativeBuildInfo*"),
  )

  final val NativeLib = Seq(
    exclude[MissingClassProblem]("scala.scalanative.runtime.rtti*"),
    exclude[Problem]("scala.scalanative.runtime.Backtrace*"),
    exclude[Problem]("scala.scalanative.runtime.dwarf.DWARF*"),
    exclude[Problem]("scala.scalanative.runtime.StackTraceElement*"),
    exclude[ReversedMissingMethodProblem]("scala.scalanative.runtime.NativeThread.companion"),
    exclude[ReversedMissingMethodProblem]("scala.scalanative.runtime.NativeThread.stackSize"),
    exclude[ReversedMissingMethodProblem]("scala.scalanative.runtime.NativeThread#Companion.defaultOSStackSize"),
    exclude[Problem]("scala.scalanative.runtime._Class.*"),
    exclude[Problem]("scala.scalanative.runtime.unwind.*"),
  )
  final val CLib: Filters = Nil

  final val PosixLib: Filters = Seq(
    exclude[DirectMissingMethodProblem]("scala.scalanative.posix.spawn.posix_spawn_file_actions_open"), // wrong name
    exclude[Problem]("scala.scalanative.posix.string.stroll"), // remove typo 
    exclude[Problem]("scala.scalanative.posix.string.stroll_l"), // remove typo
    exclude[Problem]("scala.scalanative.posix.string.strcpy") // libc not CX
  )

  final val WindowsLib: Filters = Nil

  final val TestRunner: Filters = Nil
  final val TestInterface: Filters = Nil
  final val TestInterfaceSbtDefs: Filters = Nil
  final val JUnitRuntime: Filters = Seq.empty

  val moduleFilters = Map(
    "util" -> Util,
    "nir" -> Nir,
    "tools" -> Tools,
    "clib" -> CLib,
    "posixlib" -> PosixLib,
    "windowslib" -> WindowsLib,
    "nativelib" -> NativeLib,
    "test-runner" -> TestRunner,
    "test-interface" -> TestInterface,
    "test-interface-sbt-defs" -> TestInterfaceSbtDefs,
    "junit-runtime" -> JUnitRuntime
  )
}
