package build

import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.core.ProblemFilters._

object BinaryIncompatibilities {
  type Filters = Seq[ProblemFilter]
  final val Util: Filters = Nil
  final val Nir: Filters = Nil
  final val NscPlugin = Nil
  final val JUnitPlugin: Filters = Nil

  final val Tools: Filters = Seq(
    exclude[Problem]("scala.scalanative.codegen.*"),
    exclude[Problem]("scala.scalanative.checker.*"),
    exclude[Problem]("scala.scalanative.interflow.*"),
    exclude[Problem]("scala.scalanative.linker.*"),
    exclude[Problem]("scala.scalanative.build.NativeLib.*"),
    exclude[Problem]("scala.scalanative.build.LLVM.*")
  )

  final val NativeLib = Nil
  final val CLib: Filters = Nil
  final val PosixLib: Filters = Nil
  final val WindowsLib: Filters = Nil

  final val AuxLib, JavaLib, ScalaLib, Scala3Lib: Filters = Nil
  final val TestRunner: Filters = Nil
  final val TestInterface: Filters = Nil
  final val TestInterfaceSbtDefs: Filters = Nil
  final val JUnitRuntime: Filters = Nil

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
