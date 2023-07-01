package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.nir.Type.RefKind
import scalanative.nir.{Type, Val}
import scalanative.util.unsupported
import scalanative.codegen.MemoryLayout.PositionedType
import scala.scalanative.build.Config
import scala.scalanative.linker.ClassRef
import scala.scalanative.linker.Field
import scala.scalanative.nir.Attr.Alignment
import scala.scalanative.build.BuildException
import scala.annotation.tailrec

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

  def sizeOf(ty: Type)(implicit platform: PlatformInfo): Long =
    ty match {
      case _: Type.RefKind | Type.Nothing | Type.Ptr => platform.sizeOfPtr
      case Type.Size                                 => platform.sizeOfPtr
      case t: Type.PrimitiveKind  => math.max(t.width / BITS_IN_BYTE, 1)
      case Type.ArrayValue(ty, n) => sizeOf(ty) * n
      case Type.StructValue(tys)  => MemoryLayout(tys).size
      case _                      => unsupported(s"sizeof $ty")
    }

  def alignmentOf(ty: Type)(implicit platform: PlatformInfo): Long = ty match {
    case Type.Long | Type.Double | Type.Size       => platform.sizeOfPtr
    case Type.Nothing | Type.Ptr | _: Type.RefKind => platform.sizeOfPtr
    case t: Type.PrimitiveKind   => math.max(t.width / BITS_IN_BYTE, 1)
    case Type.ArrayValue(ty, n)  => alignmentOf(ty)
    case Type.StructValue(Seq()) => 1
    case Type.StructValue(tys)   => tys.map(alignmentOf).max
    case _                       => unsupported(s"alignment $ty")
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

    val maxAlign = tys.foldLeft(1L) {
      case (maxAlign, ty) =>
        val align = alignmentOf(ty)

        offset = this.align(offset, align)
        pos += PositionedType(ty, offset)
        offset += sizeOf(ty)

        align.max(maxAlign)
    }

    MemoryLayout(align(offset, maxAlign), pos.toSeq)
  }

  def ofAlignedFields(
      fields: Seq[Field]
  )(implicit platform: PlatformInfo, meta: Metadata): MemoryLayout = {
    import meta.layouts.ObjectHeader

    val pos = mutable.UnrolledBuffer.empty[PositionedType]
    var offset = 0L
    var maxAlign = 1L

    def addPadding(alignment: Int): Unit = {
      val remainingPadding = align(offset, alignment) - offset
      if (remainingPadding > 0) {
        val last = pos.last
        // Update last postion to set correct padding by replaceing the type with struct{ty, array[byte]}
        pos.update(
          pos.indexOf(last),
          last.copy(ty =
            Type.StructValue(
              Seq(last.ty, Type.ArrayValue(Type.Byte, remainingPadding.toInt))
            )
          )
        )
        offset += remainingPadding
      }
    }

    def addField(ty: Type, fieldAlignment: Option[Int] = None): Unit = {
      val alignment = fieldAlignment.map(_.toLong).getOrElse(alignmentOf(ty))
      maxAlign = maxAlign.max(alignment)

      offset = align(offset, alignment)
      println(offset)
      pos += PositionedType(ty, offset)
      offset += sizeOf(ty)
    }

    lazy val dynamicAlignmentWidth = {
      val propName =
        "scala.scalanative.meta.linktimeinfo.contendedPaddingWidth"
      meta.linked.resolvedVals
        .get(propName)
        .collectFirst { case Val.Int(value) => value }
        .getOrElse(
          throw new BuildException(
            s"Unable to resolve size of dynamic field alignment, linktime property not found: $propName"
          )
        )
    }
    def resolveAlignWidth(align: Alignment): Int = align.size match {
      case nir.Attr.Alignment.linktimeResolved => dynamicAlignmentWidth
      case fixedWidth                          => fixedWidth
    }

    def isGroupAligned(field: Field) =
      field.attrs.align.flatMap(_.group).isDefined

    // fields should be already ordered by group names
    @tailrec def loop(fields: List[Field]): Unit = fields match {
      case Nil => ()
      case field :: tail =>
        val alignInfo = field.attrs.align
        val groupName = alignInfo.flatMap(_.group)
        val groupTail =
          if (isGroupAligned(field))
            tail.takeWhile(_.attrs.align.flatMap(_.group) == groupName)
          else Nil
        val headAlignSize = alignInfo.map(resolveAlignWidth)
        // Align size is equal to maximal alignment of all fields in the group
        val alignSize = headAlignSize.map {
          groupTail.foldLeft(_) {
            case (maxSize, field) =>
              field.attrs.align
                .map(resolveAlignWidth)
                .map(_.max(maxSize))
                .getOrElse(maxSize)
          }
        }

        alignSize.foreach(addPadding)
        addField(field.ty, alignSize)
        groupTail.foreach(field => addField(field.ty))
        alignSize.foreach(addPadding)

        loop(tail.drop(groupTail.size))
    }

    addField(ObjectHeader.layout)
    loop(fields.toList)
    MemoryLayout(align(offset, maxAlign), pos.toSeq)
  }
}
