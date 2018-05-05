package scala.scalanative.optimizer.analysis

import scala.scalanative.nir.Type.RefKind
import scala.scalanative.nir.{Type, Val}
import scala.scalanative.optimizer.analysis.MemoryLayout.PositionedType
import scala.scalanative.util.unsupported
import scala.scalanative.build.NativePlatform

final case class MemoryLayout(size: Long, tys: List[PositionedType]) {
  lazy val offsetArray: Seq[Val] = {
    val ptrOffsets =
      tys.collect {
        // offset in words without rtti
        case MemoryLayout.Tpe(_, offset, _: RefKind) =>
          Val.Long(offset / MemoryLayout.WORD_SIZE - 1)
      }

    ptrOffsets :+ Val.Long(-1)
  }
}

object MemoryLayout {

  val WORD_SIZE = 8

  sealed abstract class PositionedType {
    def size: Long
    def offset: Long
  }

  final case class Tpe(size: Long, offset: Long, ty: Type)
      extends PositionedType
  final case class Padding(size: Long, offset: Long) extends PositionedType

  def sizeOf(ty: Type, nativePlatform: NativePlatform): Long = ty match {
    case primitive: Type.Primitive => math.max(primitive.width / WORD_SIZE, 1)
    case Type.Array(arrTy, n)      => sizeOf(arrTy, nativePlatform) * n
    case Type.Struct(_, tys)       => MemoryLayout(tys, nativePlatform).size
    case Type.Nothing | Type.Ptr | _: Type.Trait | _: Type.Module |
        _: Type.Class => math.max((if (nativePlatform.is32) 32 else 64) / WORD_SIZE, 1)
    case _ => unsupported(s"sizeOf $ty")
  }

  def alignmentSize(ty: Type, nativePlatform: NativePlatform) = {
    if (nativePlatform.is32) {
      ty match {
        case Type.Double => 4L
        case Type.Long => 4L
        case o => sizeOf(o, nativePlatform)
      }
    } else {
      sizeOf(ty, nativePlatform)
    }
  }

  def apply(tys: Seq[Type], nativePlatform: NativePlatform): MemoryLayout = {
    val (size, potys) = impl(tys, 0, nativePlatform)

    MemoryLayout(size, potys.reverse)
  }
  private def impl(tys: Seq[Type],
                   offset: Long,
                   nativePlatform: NativePlatform): (Long, List[PositionedType]) = {
    if (tys.isEmpty) {
      return (0, List())
    }

    val sizes = tys.map(o => (sizeOf(o, nativePlatform), alignmentSize(o, nativePlatform)))

    def findMax(tys: Seq[Type]): Long = tys.foldLeft(0L) {
      case (acc, Type.Struct(_, innerTy)) => math.max(acc, findMax(innerTy))
      case (acc, ty)                      => math.max(acc, alignmentSize(ty, nativePlatform))
    }

    val maxSize = findMax(tys)

    val (size, positionedTypes) =
      (tys zip sizes).foldLeft((offset, List[PositionedType]())) {
        case ((index, potys), (ty, (size, alignmentSize))) if size > 0 =>
          ty match {
            case Type.Struct(_, stys) =>
              val innerAlignment = findMax(stys)
              val pad =
                if (index                    % innerAlignment == 0) 0
                else innerAlignment - (index % innerAlignment)
              val (innerSize, innerTys) = impl(stys, index + pad, nativePlatform)

              (index + pad + innerSize,
               innerTys ::: Padding(pad, index) :: potys)

            case _ =>
              val pad = if (index % alignmentSize == 0) 0 else alignmentSize - (index % alignmentSize)
              (index + pad + size,
               Tpe(size, index + pad, ty) :: Padding(pad, index) :: potys)

          }
        case ((index, potys), _) => (index, potys)

      }

    val finalPad = if (size % maxSize == 0) 0 else maxSize - (size % maxSize)
    val potys =
      if (finalPad > 0) {
        Padding(finalPad, size) :: positionedTypes
      } else {
        positionedTypes
      }

    (potys.foldLeft(0L) { case (acc, poty) => acc + poty.size }, potys)
  }
}
