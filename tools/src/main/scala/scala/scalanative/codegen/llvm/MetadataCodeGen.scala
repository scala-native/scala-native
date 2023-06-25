package scala.scalanative.codegen
package llvm

import scala.scalanative.nir
import scala.scalanative.nir.{Position, Global, Fresh}
import scala.scalanative.util.ShowBuilder
import scala.collection.mutable
import scala.scalanative.util.unsupported
// scalafmt: { maxColumn = 100}

trait MetadataCodeGen { self: AbstractCodeGen =>
  import MetadataCodeGen._
  import Metadata._
  import Writer._
  import self.meta.platform

  final val generateDebugMetadata = self.meta.config.debugMetadata

  /* Create a name debug metadata entry and write it on the metadata section */
  def dbg(name: String)(values: Metadata.Node*)(implicit ctx: Context): Unit =
    if (generateDebugMetadata) {
      // Named metadata is always stored in metadata section
      import ctx.sb._
      values.foreach(Writer.intern)
      newline()
      str(s"!$name = ")
      Metadata.Tuple(values).write()
    }

    /* Create a metadata entry by writing metadata reference in the current ShowBuilder,
     * and the metadata node definition in the metadata section
     **/
  def dbg[T <: Metadata.Node: UniqueWriter](prefix: CharSequence, v: T)(implicit
      ctx: Context,
      sb: ShowBuilder
  ): Unit =
    if (generateDebugMetadata) {
      // In metadata section
      val id = implicitly[UniqueWriter[T]].intern(v)
      // In reference section
      sb.str(prefix)
      sb.str(" !dbg ")
      sb.str(id.show)
    }

  def dbg[T <: Metadata.Node: UniqueWriter](v: T)(implicit ctx: Context, sb: ShowBuilder): Unit =
    dbg("", v)

  def compilationUnits(implicit ctx: Context): Seq[DICompileUnit] =
    implicitly[UniqueWriter[DICompileUnit]].cache.view.collect {
      case (cu: DICompileUnit, _) => cu
    }.toSeq

  protected def toDISubprogram(
      attrs: nir.Attrs,
      name: Global,
      rettype: nir.Type,
      argtypes: Seq[nir.Type],
      pos: Position
  )(implicit metaCtx: Context): DISubprogram = {
    val file = toDIFile(pos)
    val unit = DICompileUnit(
      file = file,
      producer = Constants.PRODUCER,
      isOptimized = attrs.opt == nir.Attr.DidOpt
    )
    DISubprogram(
      name = name.top.id,
      linkageName = mangled(name),
      scope = unit,
      file = file,
      unit = unit,
      line = pos.line,
      tpe = DISubroutineType(
        DITypes(
          toMetadataTypeOpt(rettype),
          argtypes.map(toMetadataType(_))
        )
      )
    )
  }

  def toDIFile(pos: Position): DIFile = {
    pos.filename
      .zip(pos.dir)
      .headOption
      .map((DIFile.apply _).tupled)
      .getOrElse(DIFile("unknown", "unknown"))
  }

  def toDILocation(pos: Position): DILocation = DILocation(
    line = pos.line,
    column = pos.column,
    scope = toDIFile(pos)
  )

  def toDILocation(
      pos: Position,
      scope: Scope
  ): DILocation = DILocation(
    line = pos.line,
    column = pos.column,
    scope = scope
  )

  private val DIBasicTypes: Map[nir.Type, Metadata.Type] = {
    import nir.Type._
    Seq(Byte, Char, Short, Int, Long, Size, Float, Double, Bool).map { tpe =>
      val name = tpe match {
        case Byte         => "i8"
        case Short | Char => "i16"
        case Int          => "i32"
        case Long         => "i64"
        case Size         => if (platform.is32Bit) "i32" else "i64"
        case Float        => "float"
        case Double       => "double"
        case Bool         => "i1"
        case _            => unsupported(s"Not a primitive type $tpe")
      }
      tpe -> DIBasicType(
        name,
        MemoryLayout.sizeOf(tpe).toInt * 8 /*bits*/,
        MemoryLayout.alignmentOf(tpe).toInt * 8 /*bits*/
      )
    }.toMap
  }
  private val DIPointerType = DIDerivedType(
    tag = DWTag.Pointer,
    baseType = DIBasicTypes(nir.Type.Byte),
    size = MemoryLayout.sizeOf(nir.Type.Ptr).toInt * 8 /*bits*/
  )

  private def toMetadataType(
      ty: nir.Type
  )(implicit metaCtx: Context): Metadata.Type = toMetadataTypeOpt(ty).getOrElse(
    unsupported(s"Type $ty cannot be converted to DebugInfo Type")
  )
  private def toMetadataTypeOpt(
      ty: nir.Type
  )(implicit metaCtx: Context): Option[Metadata.Type] = {
    import nir.Type._
    DIBasicTypes
      .get(ty)
      .orElse(ty match {
        case nir.Type.Unit                     => None
        case _: RefKind | Ptr | Null | Nothing => Some(DIPointerType)
        case other =>
          throw new NotImplementedError(s"No idea how to dwarfise $other")
      })
  }

}

object MetadataCodeGen {
  case class Context(codeGen: AbstractCodeGen, sb: ShowBuilder) {
    type WriterCache[T <: Metadata.Node] = mutable.Map[T, Int]
    private[MetadataCodeGen] val writersCache
        : mutable.Map[UniqueWriter[_ <: Metadata.Node], WriterCache[Metadata.Node]] =
      mutable.Map.empty
    private[MetadataCodeGen] val fresh: Fresh = Fresh()
  }

  object Constants {
    val PRODUCER = "Scala Native"
    val DWARF_VERSION = 3
    val DEBUG_INFO_VERSION = 3
  }

  class MetadataId(val value: Int) extends AnyVal {
    def show = "!" + value.toString()
  }

  trait Writer[-T <: Metadata] {
    final def sb(implicit ctx: Context): ShowBuilder = ctx.sb
    def write(t: T)(implicit ctx: Context): Unit
  }

  trait UniqueWriter[-T <: Metadata.Node] extends Writer[T] {
    import Writer._
    private[MetadataCodeGen] def cache(implicit ctx: Context): ctx.WriterCache[Metadata.Node] =
      ctx.writersCache
        .getOrElseUpdate(this, mutable.Map.empty)

    protected def internDeps(t: T)(implicit ctx: Context): Unit = {
      def tryIntern(v: Any): Unit = v match {
        case v: Metadata.Tuple           => v.intern()
        case v: Metadata.SpecializedNode => v.intern()
        case _                           => ()
      }
      t match {
        case v: Metadata.Tuple           => v.values.foreach(tryIntern)
        case v: Metadata.SpecializedNode => v.productIterator.foreach(tryIntern)
      }
    }

    def intern(t: T)(implicit ctx: Context): MetadataId = {
      val id = cache.getOrElseUpdate(
        t, {
          val id = ctx.fresh().id.toInt
          internDeps(t)

          sb.newline()
          sb.str("!")
          sb.str(id)
          sb.str(" = ")
          write(t)

          id
        }
      )
      new MetadataId(id)
    }
  }

  object Writer {
    implicit class MetadataWriterOps[T <: Metadata](val value: T) extends AnyVal {
      def write()(implicit
          writer: Writer[T],
          ctx: MetadataCodeGen.Context
      ): Unit = writer.write(value)
    }
    implicit class UniqueMetadataWriterOps[T <: Metadata.Node](val value: T) extends AnyVal {
      def intern()(implicit
          writer: UniqueWriter[T],
          ctx: MetadataCodeGen.Context
      ): MetadataId = writer.intern(value)
    }

    def intern[T <: Metadata](
        value: T
    )(implicit ctx: Context): Option[MetadataId] = value match {
      case node: Metadata.Node =>
        val id = node match {
          case v: Metadata.Tuple           => v.intern()
          case v: Metadata.SpecializedNode => v.intern()
        }
        Some(id)
      case _ => None
    }

    def writeInterned[T <: Metadata](
        value: T
    )(implicit ctx: Context): Unit =
      value match {
        case v: Metadata.Const => v.write()
        case v: Metadata.Str   => v.write()
        case v: Metadata.Value => v.write()
        case node: Metadata.Node =>
          val id = node match {
            case v: Metadata.Tuple           => v.intern()
            case v: Metadata.SpecializedNode => v.intern()
          }
          ctx.sb.str(id.show)
      }

    implicit lazy val constWriter: Writer[Metadata.Const] =
      new Writer[Metadata.Const] {
        override def write(
            t: Metadata.Const
        )(implicit ctx: Context): Unit = {
          sb.str(t.value)
        }
      }

    implicit lazy val stringWriter: Writer[Metadata.Str] =
      new Writer[Metadata.Str] {
        override def write(
            t: Metadata.Str
        )(implicit ctx: Context): Unit = {
          sb.str("!")
          sb.quoted(t.value)
        }
      }

    implicit lazy val valueWriter: Writer[Metadata.Value] =
      new Writer[Metadata.Value] {
        override def write(
            t: Metadata.Value
        )(implicit ctx: Context): Unit = {
          ctx.codeGen.genVal(t.value)(ctx.sb)
        }
      }

    // statefull metadata writers backed with cached
    implicit lazy val tupleWriter: UniqueWriter[Metadata.Tuple] =
      new UniqueWriter[Metadata.Tuple] {
        override def write(
            t: Metadata.Tuple
        )(implicit ctx: Context): Unit = {
          if (t.distinct) sb.str("distinct ")
          sb.str("!{")
          sb.rep(t.values, sep = ", ")(Writer.writeInterned(_))
          sb.str("}")
        }
      }

    implicit lazy val specializedNodeWriter: UniqueWriter[Metadata.SpecializedNode] =
      new UniqueWriter[Metadata.SpecializedNode] {
        override def write(
            t: Metadata.SpecializedNode
        )(implicit ctx: Context): Unit = {
          if (t.distinct) sb.str("distinct ")
          sb.str('!')
          sb.str(t.nodeName)
          sb.str("(")
          writeImpl(t)
          sb.str(")")
        }

        import Metadata._
        private def writeImpl(
            t: Metadata.SpecializedNode
        )(implicit ctx: Context): Unit = {
          var fieldIdx = 0
          def field[T](name: String, value: T)(implicit ctx: Context): Unit = {
            if (fieldIdx > 0) sb.str(", ")
            sb.str(name)
            sb.str(": ")
            value match {
              case v: Metadata  => writeInterned(v)
              case v: String    => sb.quoted(v)
              case v: Number    => sb.str(v.toString)
              case v: Boolean   => sb.str(v.toString)
              case v: Option[_] => v.foreach(field(name, _))
            }
            fieldIdx += 1
          }
          t match {
            case DICompileUnit(file, producer, isOptimized) =>
              field("file", file)
              field("producer", producer)
              field("isOptimized", isOptimized)
              field("emissionKind", Const("FullDebug"))
              // TODO: update once SN has its own DWARF language code
              field("language", Const("DW_LANG_C_plus_plus"))

            case DIFile(filename, directory) =>
              field("filename", filename)
              field("directory", directory)

            case DISubprogram(name, linkageName, scope, file, line, tpe, unit) =>
              field("name", name)
              field("linkageName", linkageName)
              field("scope", scope)
              field("file", file)
              field("line", line)
              field("type", tpe)
              field("unit", unit)
              field("spFlags", Const("DISPFlagDefinition"))

            case DILocation(line, column, scope) =>
              field("line", line)
              field("column", column)
              field("scope", scope)

            case DILocalVariable(name, scope, file, line, tpe) =>
              field("name", name)
              field("scope", scope)
              field("file", file)
              field("line", line)
              field("type", tpe)

            case DIBasicType(name, size, align) =>
              field("name", name)
              field("size", size)
              field("align", align)

            case DIDerivedType(tag, baseType, size) =>
              field("tag", tag)
              field("baseType", baseType)
              field("size", size)

            case DISubroutineType(types) =>
              field("types", types)
          }
        }
      }
  }
}
