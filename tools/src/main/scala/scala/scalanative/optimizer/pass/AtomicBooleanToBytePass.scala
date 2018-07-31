package scala.scalanative
package optimizer
package pass

import scala.scalanative.nir._
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.optimizer.analysis.ClassHierarchyExtractors.FieldRef

/**
 * We implement Java's 'volatile' fields with LLVM's 'atomic' stores and loads.
 * Since LLVM specification mandates atomic types to be at list 8 bits wide,
 * we need to transform volatile booleans (i1) to volatile bytes (i8).
 *
 * For each volatile boolean field transform:
 * 1. it to a byte field
 * 2. each store to a byte cast followed by an atomic byte store
 * 3. each load to a byte load followed by a boolean cast
 */
class AtomicBooleanToBytePass(implicit top: Top) extends Pass {

  override def onDefn(defn: Defn): Defn = defn match {

    // atomic boolean field
    case field @ Defn.Var(attrs, FieldRef(_, _), Type.Bool, rhs)
        if attrs.isJavaVolatile =>
      if (rhs ne Val.None) {
        sys.error(s"Unexpected non empty rhs of a volatile field: $field")
      } else {
        field.copy(ty = Type.Byte)
      }

    case _ => super.onDefn(defn)

  }

  override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
    val buf = new nir.Buffer
    import buf._

    insts.foreach {

      // atomic boolean load
      case Inst.Let(name,
                    loadOp @ Op.Load(Type.Bool, _, _, _isAtomic @ true)) =>
        val readByte = let(loadOp.copy(ty = Type.Byte))
        let(name, Op.Conv(Conv.Trunc, Type.Bool, readByte))

      // atomic boolean store
      case Inst.Let(
          name,
          store @ Op.Store(Type.Bool, _, value, _, _isAtomic @ true)) =>
        val zextToByte = let(Op.Conv(Conv.Zext, Type.Byte, value))
        let(name, store.copy(ty = Type.Byte, value = zextToByte))

      case other => buf += other

    }

    buf.toSeq
  }

}

object AtomicBooleanToBytePass extends PassCompanion {
  override def apply(config: build.Config, top: Top) =
    new AtomicBooleanToBytePass()(top)
}
