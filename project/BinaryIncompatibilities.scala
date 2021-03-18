package build

import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.core.ProblemFilters._

object BinaryIncompatibilities {
  final val NativeLib = Seq(
    // Since 0.4.1
    // Internal usage
    exclude[Problem]("scala.scalanative.runtime.package*TypeOps*"),
    exclude[Problem]("scala.scalanative.runtime.ClassInstancesRegistry*"),
    exclude[IncompatibleMethTypeProblem]("scala.scalanative.runtime.GC.alloc*")
  )
}
