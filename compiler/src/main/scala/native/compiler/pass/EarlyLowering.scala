package native
package compiler
package pass

import analysis.ClassHierarchy
import native.nir._

class EarlyLowering(val entryModule: Global)
  extends Pass
  with IntrinsicLowering
  with MainInjection
  with ClassLowering
  with ModuleLowering
  with ArrayLowering
  with StringLowering
  with ExceptionLowering {

  var cha: ClassHierarchy.Result = _

  override def onCompilationUnit(defns: Seq[Defn]): Seq[Defn] = {
    cha = ClassHierarchy(defns)
    super.onCompilationUnit(defns)
  }
}
