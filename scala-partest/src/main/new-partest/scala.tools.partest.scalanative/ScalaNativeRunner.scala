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
    super.extraJavaOptions ++ options.javaOptions
  }
}
