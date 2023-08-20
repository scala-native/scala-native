package scala.scalanative.codegen
package llvm

import scala.scalanative.nir
import scala.scalanative.nir.{Position, Global, Fresh, Val, Local}
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.util.ShowBuilder
import scala.collection.mutable
import scala.scalanative.util.unsupported

import scala.language.implicitConversions
import scala.scalanative.codegen.llvm.MetadataCodeGen.Writer.Specialized
import scala.scalanative.nir.Unmangle
import scala.scalanative.nir.Global.Member
import scala.scalanative.nir.Sig.Method
import scala.scalanative.util.unreachable

// scalafmt: { maxColumn = 100}
trait MetadataCodeGen { self: AbstractCodeGen =>
  import MetadataCodeGen._
  import Metadata._
  import Writer._
  import self.meta.platform

  final val generateDebugMetadata = self.meta.config.debugMetadata
  final val FirstLineOffset = 1

  /* Create a name debug metadata entry and write it on the metadata section */
  def dbg(name: String)(values: Metadata.Node*)(implicit ctx: Context): Unit =
    if (generateDebugMetadata) {
      // Named metadata is always stored in metadata section
      import ctx.sb._
      values.foreach(Writer.ofNode.intern)
      newline()
      str(s"!$name = ")
      Metadata.Tuple(values).write()
    }

    /* Create a metadata entry by writing metadata reference in the current ShowBuilder,
     * and the metadata node definition in the metadata section
     **/
  def dbg[T <: Metadata.Node: InternedWriter](prefix: CharSequence, v: T)(implicit
      ctx: Context,
      sb: ShowBuilder
  ): Unit =
    if (generateDebugMetadata) {
      // In metadata section
      val id = implicitly[InternedWriter[T]].intern(v)
      // In reference section
      sb.str(prefix)
      sb.str(" !dbg ")
      id.write(sb)
    }

  def dbg[T <: Metadata.Node: InternedWriter](v: T)(implicit ctx: Context, sb: ShowBuilder): Unit =
    dbg("", v)

  def dbgLocalValue(id: Local, ty: nir.Type, argIdx: Option[Int] = None)(
      srcPosition: nir.Position,
      scope: Scope
  )(implicit debugInfo: DebugInfo, metadataCtx: Context, sb: ShowBuilder): Unit =
    if (generateDebugMetadata) {
      debugInfo.localNames.get(id).foreach { localName =>
        `llvm.dbg.value`(
          address = Metadata.Value(Val.Local(id, ty)),
          description = Metadata.DILocalVariable(
            name = localName,
            arg = argIdx,
            scope = scope,
            file = toDIFile(srcPosition),
            line = srcPosition.line + FirstLineOffset,
            tpe = toMetadataType(ty)
          ),
          expr = Metadata.DIExpression()
        )(srcPosition, scope)
      }
    }

  private def `llvm.dbg.value`(
      address: Metadata.Value,
      description: DILocalVariable,
      expr: Metadata.DIExpression
  )(pos: Position, scope: Metadata.Scope)(implicit
      ctx: Context,
      sb: ShowBuilder
  ): Unit = {
    sb.newline()
    sb.str("call void @llvm.dbg.value(metadata ")
    genVal(address.value)
    sb.str(", metadata ")
    description.intern().write(sb)
    sb.str(", metadata ")
    expr.intern().write(sb)
    sb.str(")")
    dbg(",", toDILocation(pos, scope))
  }

  def compilationUnits(implicit ctx: Context): Seq[DICompileUnit] =
    ctx.writersCache
      .get(classOf[DICompileUnit])
      .map(_.keySet.toSeq.asInstanceOf[Seq[DICompileUnit]])
      .getOrElse(Nil)

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
    // unmangle method name
    // see: https://scala-native.org/en/stable/contrib/mangling.html
    val linkageName = mangled(name)
    val defnName = if (linkageName.startsWith("_S")) linkageName.drop(2) else linkageName
    val unmangledName = if (defnName.startsWith("M")) { // subprogram should be a member
      Unmangle.unmangleGlobal(defnName) match {
        case Member(owner, sig) =>
          Unmangle.unmangleSig(sig.mangle) match {
            case Method(id, _, _) => Some(id)
            case _                => None
          }
        case _ => None
      }
    } else None

    DISubprogram(
      name = unmangledName.getOrElse(linkageName),
      linkageName = linkageName,
      scope = unit,
      file = file,
      unit = unit,
      line = pos.line + FirstLineOffset,
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
    line = pos.line + FirstLineOffset,
    column = pos.column,
    scope = toDIFile(pos)
  )

  def toDILocation(
      pos: Position,
      scope: Scope
  ): DILocation = DILocation(
    line = pos.line + FirstLineOffset,
    column = pos.column,
    scope = scope
  )

  private val DIBasicTypes: Map[nir.Type, Metadata.Type] = {
    import nir.Type._
    Seq(Byte, Char, Short, Int, Long, Size, Float, Double, Bool, Ptr).map { tpe =>
      tpe -> DIBasicType(
        name = tpe.show,
        size = MemoryLayout.sizeOf(tpe).toInt * 8 /*bits*/,
        align = MemoryLayout.alignmentOf(tpe).toInt * 8 /*bits*/,
        encoding = tpe match {
          case Bool           => DW_ATE.Boolean
          case Float | Double => DW_ATE.Float
          case Ptr            => DW_ATE.Address
          case Char           => DW_ATE.Unsigned
          case _              => DW_ATE.Signed
        }
      )
    }.toMap
  }

  protected def toMetadataType(
      ty: nir.Type
  )(implicit metaCtx: Context): Metadata.Type = toMetadataTypeOpt(ty).getOrElse(
    unsupported(s"Type $ty cannot be converted to DebugInfo Type")
  )
  protected def toMetadataTypeOpt(
      ty: nir.Type
  )(implicit metaCtx: Context): Option[Metadata.Type] = {
    import nir.Type._
    DIBasicTypes
      .get(ty)
      .orElse(ty match {
        case nir.Type.Unit => None
        // TODO: describe RefKinds using DICompositeType
        case _: RefKind | Null | Nothing => DIBasicTypes.get(Ptr)
        case StructValue(tys)            =>
          // TODO: DICompositeType and DIDerivedType should have `name` attribute, but we need to modify
          // the way of traversing NIR during codegen or add type name info into NIR.
          val elements = tys.map { ty =>
            DIDerivedType(
              tag = DWTag.Member,
              baseType = toMetadataType(ty),
              size = (MemoryLayout.sizeOf(ty) * 8).toInt /* bits */
            )
          }
          val size = MemoryLayout.sizeOf(ty).toInt * 8 /* bits */
          Some(
            DICompositeType(
              tag = DWTag.StructureType,
              size = size,
              elements = Tuple(elements)
            )
          )
        case other =>
          throw new NotImplementedError(s"No idea how to dwarfise $other")
      })
  }
}

object MetadataCodeGen {
  case class Context(codeGen: AbstractCodeGen, sb: ShowBuilder) {
    type WriterCache[T <: Metadata.Node] = mutable.Map[T, Int]
    private[MetadataCodeGen] val writersCache
        : mutable.Map[Class[_ <: Metadata.Node], WriterCache[Metadata.Node]] =
      mutable.Map.empty

    private[MetadataCodeGen] val specializedBuilder: Specialized.Builder[_] =
      new Specialized.Builder[Any]()(this)
    private[MetadataCodeGen] val fresh: Fresh = Fresh()
  }

  class MetadataId(val value: Int) extends AnyVal {
    def show = "!" + value.toString()
    def write(sb: ShowBuilder): Unit = {
      sb.str('!')
      sb.str(value.toString())
    }
  }

  trait Writer[T <: Metadata] {
    final def sb(implicit ctx: Context): ShowBuilder = ctx.sb
    final def write(v: T)(implicit ctx: Context): Unit = writeMetadata(v, ctx)
    def writeMetadata(v: T, ctx: Context): Unit
  }

  trait InternedWriter[T <: Metadata.Node] extends Writer[T] {
    import Writer._
    final private[MetadataCodeGen] def cache(v: T)(implicit ctx: Context): ctx.WriterCache[T] =
      ctx.writersCache
        .getOrElseUpdate(v.getClass(), mutable.Map.empty)
        .asInstanceOf[ctx.WriterCache[T]]

    protected def internDeps(v: T)(implicit ctx: Context): Unit = {
      def tryIntern(v: Any): Unit = v match {
        case v: Metadata.Node => v.intern()
        case _                => ()
      }
      v match {
        case v: Metadata.Tuple           => v.values.foreach(tryIntern)
        case v: Metadata.SpecializedNode => v.productIterator.foreach(tryIntern)
      }
    }

    final def intern(v: T)(implicit ctx: Context): MetadataId = {
      val id = cache(v).getOrElseUpdate(
        v, {
          internDeps(v)
          val id = ctx.fresh().id.toInt

          sb.newline()
          sb.str("!")
          sb.str(id)
          sb.str(" = ")
          write(v)

          id
        }
      )
      new MetadataId(id)
    }
  }

  trait Dispatch[T <: Metadata.Node] extends InternedWriter[T] {
    import Writer.MetadataInternedWriterOps
    final override def writeMetadata(v: T, ctx: Context): Unit = delegate(v).writeMetadata(v, ctx)

    private def delegate(v: T): InternedWriter[T] = dispatch(v).asInstanceOf[InternedWriter[T]]

    def dispatch(v: T): InternedWriter[_ <: T]
  }

  object Writer {
    import Metadata._

    implicit class MetadataWriterOps[T <: Metadata](val value: T) extends AnyVal {
      def write()(implicit
          writer: Writer[T],
          ctx: MetadataCodeGen.Context
      ): Unit = writer.write(value)
    }
    implicit class MetadataInternedWriterOps[T <: Metadata.Node](val value: T) extends AnyVal {
      def intern()(implicit
          writer: InternedWriter[T],
          ctx: MetadataCodeGen.Context
      ): MetadataId = writer.intern(value)

      def writeInterned()(implicit
          writer: InternedWriter[T],
          ctx: MetadataCodeGen.Context
      ): Unit = value.intern().write(ctx.sb)

      def writer(implicit writer: InternedWriter[T]): InternedWriter[T] = writer
    }

    def writeInterned[T <: Metadata.Node: InternedWriter](value: T)(implicit ctx: Context): Unit = {
      val id = value.intern()
      id.write(ctx.sb)
    }

    implicit lazy val ofMetadata: Writer[Metadata] = (v, ctx) => {
      implicit def _ctx: Context = ctx
      v match {
        case v: Metadata.Str   => v.write()
        case v: Metadata.Const => v.write()
        case v: Metadata.Value => v.write()
        case v: Metadata.Node  => writeInterned(v)
      }
    }

    implicit lazy val ofConst: Writer[Metadata.Const] = (v, ctx) => ctx.sb.str(v.value)
    implicit def ofSubConst[T <: Metadata.Const]: Writer[T] = ofConst.asInstanceOf[Writer[T]]

    implicit lazy val ofString: Writer[Metadata.Str] = (v, ctx) => {
      import ctx.sb
      sb.str("!")
      sb.quoted(v.value)
    }
    implicit def ofSubString[T <: Metadata.Str]: Writer[T] =
      ofString.asInstanceOf[Writer[T]]

    implicit lazy val ofValue: Writer[Metadata.Value] = (v, ctx) =>
      ctx.codeGen.genVal(v.value)(ctx.sb)
    implicit def ofSubValue[T <: Metadata.Value]: Writer[T] =
      ofValue.asInstanceOf[Writer[T]]

    implicit def ofNode: Dispatch[Metadata.Node] = _ match {
      case v: Metadata.Tuple           => v.writer
      case v: Metadata.SpecializedNode => v.writer
    }

    // statefull metadata writers backed with cached

    implicit lazy val ofTuple: InternedWriter[Metadata.Tuple] = (v, ctx) => {
      implicit def _ctx: Context = ctx
      v.values.foreach {
        case v: Metadata.Node => v.intern()
        case _                => ()
      }
      import ctx.sb
      if (v.distinct) sb.str("distinct ")
      sb.str("!{")
      sb.rep(v.values, sep = ", ")({
        case v: Metadata.Node => writeInterned(v)
        case v: Metadata      => v.write()
      })
      sb.str("}")
    }
    implicit def ofSubTuple[T <: Metadata.Tuple]: InternedWriter[T] =
      ofTuple.asInstanceOf[InternedWriter[T]]

    object Specialized {
      object Builder {
        def use[T](ctx: Context)(fn: Builder[T] => Unit) = {
          // Use cached instance of Builder, it's always used by single ctx/thread
          val builder = ctx.specializedBuilder
            .asInstanceOf[Builder[T]]
            .reset()
          fn(builder)
        }

        trait FieldWriter[T] { def write(ctx: Context, value: T): Unit }
        object FieldWriter {
          implicit val IntField: FieldWriter[Int] = (ctx: Context, value: Int) =>
            ctx.sb.str(value.toString())
          implicit val BooleanField: FieldWriter[Boolean] = (ctx: Context, value: Boolean) =>
            ctx.sb.str(value.toString())
          implicit val StringField: FieldWriter[String] = (ctx: Context, value: String) =>
            ctx.sb.quoted(value)
          implicit def MetadataNodeField[T <: Metadata.Node: InternedWriter]: FieldWriter[T] =
            (ctx: Context, value: T) => writeInterned(value)(implicitly, ctx)
          implicit def MetadataField[T <: Metadata: Writer]: FieldWriter[T] =
            (ctx: Context, value: T) => implicitly[Writer[T]].write(value)(ctx)
        }

      }
      class Builder[T](implicit ctx: Context) {
        import Builder._
        private var isEmpty = true

        private def reset(): this.type = {
          isEmpty = true
          this
        }

        // The fields of literal types differ from typical Metadata.Str // no '!' prefix
        // Also Boolean and numeric literals don't contain type prefix
        def field[T: FieldWriter](name: String, value: T): this.type =
          fieldImpl[Int](name)(implicitly[FieldWriter[T]].write(ctx, value))

        def field[T: FieldWriter](name: String, value: Option[T]): this.type =
          value.fold[this.type](this)(field(name, _))

        private def fieldImpl[T](name: String)(doWrite: => Unit): this.type = {
          def sb = ctx.sb
          if (!isEmpty) sb.str(", ")
          sb.str(name)
          sb.str(": ")
          doWrite
          isEmpty = false
          this
        }
      }
    }
    trait Specialized[T <: Metadata.SpecializedNode] extends InternedWriter[T] {
      def writeFields(v: T): Specialized.Builder[T] => Unit
      override def writeMetadata(v: T, ctx: Context): Unit = {
        implicit def _ctx: Context = ctx
        if (v.distinct) sb.str("distinct ")
        sb.str('!')
        sb.str(v.nodeName)
        sb.str("(")
        Specialized.Builder.use[T](ctx) { builder =>
          writeFields(v)(builder)
        }
        sb.str(")")
      }
    }

    implicit lazy val ofSpecializedNode: Dispatch[Metadata.SpecializedNode] = _ match {
      case v: Metadata.LLVMDebugInformation => ofLLVMDebugInformation
    }
    implicit lazy val ofLLVMDebugInformation: Dispatch[Metadata.LLVMDebugInformation] = _ match {
      case v: Metadata.Scope  => v.writer
      case v: Metadata.Type   => v.writer
      case v: DILocation      => v.writer
      case v: DILocalVariable => v.writer
      case v: DIExpression    => v.writer
    }
    implicit lazy val ofScope: Dispatch[Metadata.Scope] = _ match {
      case v: DICompileUnit => v.writer
      case v: DIFile        => v.writer
      case v: DISubprogram  => v.writer
      // Dummy scope, not possible to obtain, becouse call to dbg would never actually need call it
      case Scope.NoScope => unreachable
    }

    import Metadata.conversions.StringOps
    implicit lazy val ofDICompileUnit: Specialized[DICompileUnit] = {
      case DICompileUnit(file, producer, isOptimized) =>
        _.field("file", file)
          .field("producer", producer)
          .field("isOptimized", isOptimized)
          .field("emissionKind", "FullDebug".const)
          // TODO: update once SN has its own DWARF language code
          .field("language", "DW_LANG_C_plus_plus".const)
    }

    implicit lazy val ofDIFile: Specialized[DIFile] = {
      case DIFile(filename, directory) =>
        _.field("filename", filename)
          .field("directory", directory)
    }

    implicit lazy val ofDISubprogram: Specialized[DISubprogram] = {
      case DISubprogram(name, linkageName, scope, file, line, tpe, unit) =>
        _.field("name", name)
          .field("linkageName", linkageName)
          .field("scope", scope)
          .field("file", file)
          .field("line", line)
          .field("type", tpe)
          .field("unit", unit)
          .field("spFlags", "DISPFlagDefinition".const)
    }

    implicit lazy val ofType: Dispatch[Metadata.Type] = _ match {
      case v: DIBasicType      => v.writer
      case v: DIDerivedType    => v.writer
      case v: DISubroutineType => v.writer
      case v: DICompositeType  => v.writer

    }
    implicit lazy val ofDIBasicType: Specialized[DIBasicType] = {
      case DIBasicType(name, size, align, encoding) =>
        _.field("name", name)
          .field("size", size)
          .field("align", align)
          .field("encoding", encoding)
    }
    implicit lazy val ofDIDerivedType: Specialized[DIDerivedType] = {
      case DIDerivedType(tag, baseType, size) =>
        _.field("tag", tag)
          .field("baseType", baseType)
          .field("size", size)
    }
    implicit lazy val ofDISubroutineType: Specialized[DISubroutineType] = {
      case DISubroutineType(types) =>
        _.field("types", types)
    }
    implicit lazy val ofDICompositeType: Specialized[DICompositeType] = {
      case DICompositeType(tag, size, elements) =>
        _.field("tag", tag)
          .field("size", size)
          .field("elements", elements)
    }

    implicit lazy val ofDILocation: Specialized[DILocation] = {
      case DILocation(line, column, scope) =>
        _.field("line", line)
          .field("column", column)
          .field("scope", scope)
    }
    implicit lazy val ofDILocalVariable: Specialized[DILocalVariable] = {
      case DILocalVariable(name, arg, scope, file, line, tpe) =>
        _.field("name", name)
          .field("arg", arg)
          .field("scope", scope)
          .field("file", file)
          .field("line", line)
          .field("type", tpe)
    }
    implicit lazy val ofDIExpression: Specialized[DIExpression] = {
      case DIExpression() => identity
    }
  }

}
