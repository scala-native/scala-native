package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir.Type.RefKind
import scalanative.nir.{Type, Val}
import scalanative.util.unsupported
import scalanative.codegen.MemoryLayout.PositionedType

final case class MemoryLayout(size: Long,
                              tys: Seq[MemoryLayout.PositionedType]) {
  lazy val offsetArray: Seq[Val] = {
    val ptrOffsets =
      tys.collect {
        // offset in words without rtti
        case MemoryLayout.PositionedType(_: RefKind, offset) =>
          Val.Long(offset / MemoryLayout.BITS_IN_BYTE - 1)
      }

    ptrOffsets :+ Val.Long(-1)
  }
}

object MemoryLayout {
  final val BITS_IN_BYTE = 8

  final case class PositionedType(ty: Type, offset: Long)

  def sizeOf(ty: Type, is32: Boolean): Long = ty match {
    case primitive: Type.PrimitiveKind =>
      math.max(primitive.width / BITS_IN_BYTE, 1)
    case Type.ArrayValue(ty, n) =>
      sizeOf(ty, is32) * n
    case Type.StructValue(tys) =>
      MemoryLayout(tys, is32).size
    case Type.Word | Type.Nothing | Type.Ptr | _: Type.RefKind =>
      if (is32) 4 else 8
    case _ =>
      unsupported(s"sizeof $ty")
  }

  def alignmentOf(ty: Type, is32: Boolean): Long = ty match {
    case Type.Long | Type.Double =>
      if (is32) 4 else 8
    case primitive: Type.PrimitiveKind =>
      math.max(primitive.width / BITS_IN_BYTE, 1)
    case Type.ArrayValue(ty, n) =>
      alignmentOf(ty, is32)
    case Type.StructValue(Seq()) =>
      1
    case Type.StructValue(tys) =>
      tys.map(alignmentOf(_, is32)).max
    case Type.Word | Type.Nothing | Type.Ptr | _: Type.RefKind =>
      if (is32) 4 else 8
    case _ =>
      unsupported(s"alignment $ty")
  }

  def align(offset: Long, alignment: Long): Long = {
    val alignmentMask = alignment - 1L
    val padding =
      if ((offset & alignmentMask) == 0L) 0L
      else alignment - (offset & alignmentMask)
    offset + padding
  }

  def apply(tys: Seq[Type], is32: Boolean): MemoryLayout = {
    val pos    = mutable.UnrolledBuffer.empty[PositionedType]
    var offset = 0L

    tys.foreach { ty =>
      offset = align(offset, alignmentOf(ty, is32))
      pos += PositionedType(ty, offset)
      offset += sizeOf(ty, is32)
    }

    val alignment = if (tys.isEmpty) 1 else tys.map(alignmentOf(_, is32)).max

    MemoryLayout(align(offset, alignment), pos)
  }
}
