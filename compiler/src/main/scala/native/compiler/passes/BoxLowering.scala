package native
package compiler
package passes

import native.nir._

object BoxLowering extends Pass {
  override def onOp(focus: Focus) = {
    case Op.Box(boxty, v) =>
      val ty = boxty.unboxed
      val alloc = focus withOp Op.Alloc(ty)
      val store = alloc withOp Op.Store(ty, alloc.value, v)
      store withValue alloc.value
    case Op.Unbox(boxty, v) =>
      val ty = boxty.unboxed
      val ptr = focus withOp Op.Conv(Conv.Bitcast, Type.Ptr(ty), v)
      ptr withOp Op.Load(ty, ptr.value)
  }
}
