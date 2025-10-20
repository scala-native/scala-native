package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.util.unsupported
import scalanative.codegen.MemoryLayout.PositionedType
import scala.scalanative.build.Config
import scala.scalanative.linker.ClassRef
import scala.scalanative.linker.Field
import scala.scalanative.nir.Attr.Alignment
import scala.scalanative.build.BuildException
import scala.annotation.tailrec

private[codegen] final case class MemoryLayout(
    size: Long,
    tys: Seq[MemoryLayout.PositionedType]
) {

  /** A list of offsets pointing to inner fields of reference types excluding
   *  object header from the address of memory directly after the object header
   *  end expresed as number of words. Used by the GC for scanning objects.
   *  Terminated with offset=-1
   */
  private[codegen] def referenceFieldsOffsets(implicit
      meta: Metadata
  ): Seq[nir.Val.Int] = {
    import nir.Type.*
    val offsets =
      tys.collect {
        // offset in words without object header
        case MemoryLayout.PositionedType(
              _: RefKind | // normal or alligned reference field
              StructValue((_: RefKind) :: ArrayValue(nir.Type.Byte, _) :: Nil),
              offset
            ) =>
          offset.toInt
      }

    (offsets :+ -1).map(nir.Val.Int(_))
  }

  /** A list of offsets pointing to all inner reference types excluding object
   *  header from the start of the object address.
   */
  def fieldOffsets(implicit meta: Metadata): Seq[Long] =
    tys
      .dropWhile(_.ty == meta.layouts.ObjectHeader.layout)
      .map(_.offset)
}

private[scalanative] object MemoryLayout {
  final val BITS_IN_BYTE = 8
  final val BYTES_IN_LONG = 8

  final case class PositionedType(ty: nir.Type, offset: Long)

  def sizeOf(ty: nir.Type)(implicit platform: PlatformInfo): Long =
    ty match {
      case _: nir.Type.RefKind | nir.Type.Nothing | nir.Type.Ptr =>
        platform.sizeOfPtr
      case nir.Type.Size =>
        platform.sizeOfPtr
      case t: nir.Type.PrimitiveKind =>
        math.max(t.width / BITS_IN_BYTE, 1)
      case nir.Type.ArrayValue(ty, n) =>
        sizeOf(ty) * n
      case nir.Type.StructValue(tys) =>
        MemoryLayout(tys).size
      case _ =>
        unsupported(s"sizeof $ty")
    }

  def alignmentOf(ty: nir.Type)(implicit platform: PlatformInfo): Long =
    ty match {
      case nir.Type.Long | nir.Type.Double | nir.Type.Size =>
        platform.sizeOfPtr
      case nir.Type.Nothing | nir.Type.Ptr | _: nir.Type.RefKind =>
        platform.sizeOfPtr
      case t: nir.Type.PrimitiveKind =>
        math.max(t.width / BITS_IN_BYTE, 1)
      case nir.Type.ArrayValue(ty, n) =>
        alignmentOf(ty)
      case nir.Type.StructValue(Seq()) =>
        1
      case nir.Type.StructValue(tys) =>
        tys.map(alignmentOf).max
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

  def apply(
      tys: Seq[nir.Type]
  )(implicit platform: PlatformInfo): MemoryLayout = {
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
            nir.Type.StructValue(
              Seq(
                last.ty,
                nir.Type.ArrayValue(nir.Type.Byte, remainingPadding.toInt)
              )
            )
          )
        )
        offset += remainingPadding
      }
    }

    def addField(ty: nir.Type, fieldAlignment: Option[Int] = None): Unit = {
      val alignment = fieldAlignment.map(_.toLong).getOrElse(alignmentOf(ty))
      maxAlign = maxAlign.max(alignment)

      offset = align(offset, alignment)
      pos += PositionedType(ty, offset)
      offset += sizeOf(ty)
    }

    lazy val dynamicAlignmentWidth = {
      val propName =
        "scala.scalanative.meta.linktimeinfo.contendedPaddingWidth"
      meta.analysis.resolvedVals
        .get(propName)
        .collectFirst { case nir.Val.Int(value) => value }
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
      case Nil           => ()
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
