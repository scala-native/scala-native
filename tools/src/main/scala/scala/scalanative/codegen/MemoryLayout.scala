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
          Val.Long(offset / MemoryLayout.WORD_SIZE - 1)
      }

    ptrOffsets :+ Val.Long(-1)
  }
}

object MemoryLayout {
  final val WORD_SIZE = 8

  final case class PositionedType(ty: Type, offset: Long)

  def sizeOf(ty: Type): Long = ty match {
    case primitive: Type.PrimitiveKind =>
      math.max(primitive.width / WORD_SIZE, 1)
    case Type.ArrayValue(ty, n) =>
      sizeOf(ty) * n
    case Type.StructValue(tys) =>
      MemoryLayout(tys).size
    case Type.Nothing | Type.Ptr | _: Type.RefKind =>
      8
    case _ =>
      unsupported(s"sizeof $ty")
  }

  def alignmentOf(ty: Type): Long = ty match {
    case primitive: Type.PrimitiveKind =>
      math.max(primitive.width / WORD_SIZE, 1)
    case Type.ArrayValue(ty, n) =>
      alignmentOf(ty)
    case Type.StructValue(Seq()) =>
      1
    case Type.StructValue(tys) =>
      tys.map(alignmentOf).max
    case Type.Nothing | Type.Ptr | _: Type.RefKind =>
      8
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

  def apply(tys: Seq[Type]): MemoryLayout = {
    val pos    = mutable.UnrolledBuffer.empty[PositionedType]
    var offset = 0L

    tys.foreach { ty =>
      offset = align(offset, alignmentOf(ty))
      pos += PositionedType(ty, offset)
      offset += sizeOf(ty)
    }

    val alignment = if (tys.isEmpty) 1 else tys.map(alignmentOf).max

    MemoryLayout(align(offset, alignment), pos)
  }
}
