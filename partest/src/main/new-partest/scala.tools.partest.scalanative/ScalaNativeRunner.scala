package scala.tools.partest.scalanative

import scala.tools.partest.nest
import scala.tools.partest.nest.{AbstractRunner, DirectCompiler, TestInfo}

class ScalaNativeRunner(testInfo: TestInfo,
                        suiteRunner: AbstractRunner,
                        options: ScalaNativePartestOptions)
    extends nest.Runner(testInfo, suiteRunner) {

  override def newCompiler = {
    new DirectCompiler(this) with ScalaNativeDirectCompiler
  }

  override def extraJavaOptions = {
    super.extraJavaOptions ++ Seq(
      s"-Dscalantive.partest.optimize=${options.optimize}",
      s"-Dscalanative.partest.mode=${options.buildMode.name}",
      s"-Dscalanative.partest.gc=${options.gc.name}",
      s"-Dscalanative.partest.lto=${options.lto.name}",
      s"-Dscalanative.partest.nativeCp=${options.nativeClasspath.mkString(":")}"
    )
  }
}
