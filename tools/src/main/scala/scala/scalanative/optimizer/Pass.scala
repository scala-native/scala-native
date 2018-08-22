package scala.scalanative
package optimizer

trait Pass {

  def onInsts(insts: Seq[nir.Inst]): Seq[nir.Inst]
}
