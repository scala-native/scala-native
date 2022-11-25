package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir.Type.RefKind
import scalanative.nir.{Type, Val}
import scalanative.util.unsupported
import scalanative.codegen.MemoryLayout.PositionedType
import scala.scalanative.build.Config
import scala.scalanative.linker.ClassRef

final case class MemoryLayout(
    size: Long,
    tys: Seq[MemoryLayout.PositionedType]
) {
  def offsetArray(implicit meta: Metadata): Seq[Val] = {
    val ptrOffsets =
      tys.collect {
        // offset in words without object header
        case MemoryLayout.PositionedType(_: RefKind, offset) =>
          Val.Long(
            offset / MemoryLayout.BYTES_IN_LONG - meta.layouts.ObjectHeader.fields
          )
      }

    ptrOffsets :+ Val.Long(-1)
  }
}

object MemoryLayout {
  final val BITS_IN_BYTE = 8
  final val BYTES_IN_LONG = 8

  final case class PositionedType(ty: Type, offset: Long)

  implicit private def metaToLinkerResult(implicit
      meta: Metadata
  ): linker.Result = meta.linked

  def sizeOf(ty: Type)(implicit platform: PlatformInfo): Long = sizeOf(ty, None)

  def sizeOf(ty: Type, metadata: Option[Metadata])(implicit
      platform: PlatformInfo
  ): Long =
    ty match {
      case primitive: Type.PrimitiveKind =>
        math.max(primitive.width / BITS_IN_BYTE, 1)
      case Type.ArrayValue(ty, n) =>
        sizeOf(ty) * n
      case Type.StructValue(tys) =>
        MemoryLayout(tys).size
      case _: Type.RefKind =>
        val layoutSize = for {
          meta <- metadata
          clsRef <- ClassRef.unapply(ty)(meta.linked)
          layout <- meta.layout.get(clsRef)
        } yield layout.size
        layoutSize.getOrElse(platform.sizeOfPtr)

      case Type.Size | Type.Nothing | Type.Ptr =>
        platform.sizeOfPtr
      case _ =>
        unsupported(s"sizeof $ty")
    }

  def alignmentOf(ty: Type)(implicit platform: PlatformInfo): Long = ty match {
    case Type.Long | Type.Double => platform.sizeOfPtr
    case primitive: Type.PrimitiveKind =>
      math.max(primitive.width / BITS_IN_BYTE, 1)
    case Type.ArrayValue(ty, n) =>
      alignmentOf(ty)
    case Type.StructValue(Seq()) =>
      1
    case Type.StructValue(tys) =>
      tys.map(alignmentOf(_)).max
    case Type.Size | Type.Nothing | Type.Ptr | _: Type.RefKind =>
      platform.sizeOfPtr
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

  def apply(tys: Seq[Type])(implicit platform: PlatformInfo): MemoryLayout = {
    val pos = mutable.UnrolledBuffer.empty[PositionedType]
    var offset = 0L

    tys.foreach { ty =>
      offset = align(offset, alignmentOf(ty))
      pos += PositionedType(ty, offset)
      offset += sizeOf(ty)
    }

    val alignment = {
      if (tys.isEmpty) 1
      else tys.map(alignmentOf(_)).max
    }

    MemoryLayout(align(offset, alignment), pos.toSeq)
  }
}
