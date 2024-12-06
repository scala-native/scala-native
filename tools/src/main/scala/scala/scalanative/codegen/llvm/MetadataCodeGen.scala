package scala.scalanative
package codegen
package llvm

import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.util.ShowBuilder
import scala.collection.mutable
import scala.scalanative.util.unsupported

import scala.language.implicitConversions
import scala.scalanative.codegen.llvm.MetadataCodeGen.Writer.Specialized
import scala.scalanative.util.unreachable
import scala.scalanative.linker.{
  ClassRef,
  ArrayRef,
  FieldRef,
  ScopeRef,
  TraitRef,
  Method
}
import scala.scalanative.linker.ReachabilityAnalysis
import scala.scalanative.util.ScopedVar
import scala.scalanative.codegen.llvm.Metadata.conversions.optionWrapper
import scala.scalanative.nir.SourceFile.Relative

// scalafmt: { maxColumn = 100}
private[codegen] trait MetadataCodeGen { self: AbstractCodeGen =>
  import MetadataCodeGen._
  import Metadata._
  import Writer._
  import self.meta.platform

  final val generateDebugMetadata = self.meta.config.sourceLevelDebuggingConfig.enabled
  final val generateLocalVariables =
    self.meta.config.sourceLevelDebuggingConfig.generateLocalVariables

  def sourceCodeCache: SourceCodeCache

  /* Create a name debug metadata entry and write it on the metadata section */
  def dbg(name: => String)(values: Metadata.Node*)(implicit ctx: Context): Unit =
    if (generateDebugMetadata) {
      // Named metadata is always stored in metadata section
      import ctx.sb._
      values.foreach(Writer.ofNode.intern)
      newline()
      str(s"!$name = ")
      Metadata.Tuple(values).write()
    }

  def dbgUsing[T <: Metadata.Node: InternedWriter](
      v: => T
  )(usingBlock: T => Unit)(implicit ctx: Context, sb: ShowBuilder) = if (generateDebugMetadata) {

    val writer = implicitly[InternedWriter[T]]
    val id = writer.getOrAssignId(v)

    // In reference section
    sb.str(" !dbg ")
    id.write(sb)

    usingBlock(v)

    // In metadata section
    writer.intern(v)
  } else usingBlock(v)

  /* Create a metadata entry by writing metadata reference in the current ShowBuilder,
   * and the metadata node definition in the metadata section
     **/
  def dbg[T <: Metadata.Node: InternedWriter](prefix: => CharSequence, v: => T)(implicit
      ctx: Context,
      sb: ShowBuilder
  ): Unit = if (generateDebugMetadata) {
    // In metadata section
    val id = implicitly[InternedWriter[T]].intern(v)
    // In reference section
    sb.str(prefix)
    sb.str(" !dbg ")
    id.write(sb)
  }

  def dbg[T <: Metadata.Node: InternedWriter](
      v: => T
  )(implicit ctx: Context, sb: ShowBuilder): Unit =
    dbg("", v)

  private def canHaveDebugValue(ty: nir.Type) = ty match {
    case nir.Type.Unit | nir.Type.Nothing => false
    case _                                => true
  }

  def dbgLocalValue(id: nir.Local, ty: nir.Type, argIdx: Option[Int] = None)(
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  )(implicit
      debugInfo: DebugInfo,
      defnScopes: DefnScopes,
      metadataCtx: Context,
      sb: ShowBuilder
  ): Unit = createVarDebugInfo(isVar = false, argIdx = argIdx)(id, ty, srcPosition, scopeId)

  def dbgLocalVariable(id: nir.Local, ty: nir.Type)(
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  )(implicit
      debugInfo: DebugInfo,
      defnScopes: DefnScopes,
      metadataCtx: Context,
      sb: ShowBuilder
  ): Unit = createVarDebugInfo(isVar = true, argIdx = None)(id, ty, srcPosition, scopeId)

  private def createVarDebugInfo(
      isVar: Boolean,
      argIdx: Option[Int]
  )(id: nir.Local, ty: nir.Type, srcPosition: nir.SourcePosition, scopeId: nir.ScopeId)(implicit
      debugInfo: DebugInfo,
      defnScopes: DefnScopes,
      metadataCtx: Context,
      sb: ShowBuilder
  ): Unit = if (generateLocalVariables && canHaveDebugValue(ty)) {
    implicit def _srcPosition: nir.SourcePosition = srcPosition
    implicit def _scopeId: nir.ScopeId = scopeId
    implicit def analysis: linker.ReachabilityAnalysis.Result = meta.analysis
    import Metadata.DIExpression._

    debugInfo.localNames.get(id).foreach { localName =>
      val variableTy = if (isVar) nir.Type.Ptr else ty
      val variableAddress = Metadata.Value(nir.Val.Local(id, variableTy))
      val scope = defnScopes.toDIScope(scopeId)
      val file = toDIFile(srcPosition)
      val line = srcPosition.line.toDILine
      val baseType = toMetadataType(ty)

      def genDbgInfo(
          address: Metadata.Value,
          description: DILocalVariable
      ) = {
        metadataCtx.currentSubprogram.get.retainedNodes += description
        if (isVar) `llvm.dbg.declare`(address, description, DIExpressions())
        else `llvm.dbg.value`(address, description, DIExpressions())
      }

      genDbgInfo(
        address = variableAddress,
        description = Metadata.DILocalVariable(
          name = localName,
          arg = argIdx,
          scope = scope,
          file = file,
          line = line,
          tpe = toMetadataType(ty),
          flags =
            if (localName == "this") DIFlags(DIFlag.DIFlagArtificial)
            else DIFlags()
        )
      )
    }
  }

  private def `llvm.dbg.value`(
      address: Metadata.Value,
      description: DILocalVariable,
      expr: Metadata.DIExpressions
  )(implicit
      ctx: Context,
      sb: ShowBuilder,
      defnScopes: DefnScopes,
      pos: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): Unit = {
    sb.newline()
    sb.str("call void @llvm.dbg.value(metadata ")
    genVal(address.value)
    sb.str(", metadata ")
    description.intern().write(sb)
    sb.str(", metadata ")
    expr.intern().write(sb)
    sb.str(")")
    dbg(",", toDILocation(pos, scopeId))
  }

  private def `llvm.dbg.declare`(
      address: Metadata.Value,
      description: DILocalVariable,
      expr: Metadata.DIExpressions
  )(implicit
      ctx: Context,
      sb: ShowBuilder,
      defnScopes: DefnScopes,
      pos: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): Unit = {
    sb.newline()
    sb.str("call void @llvm.dbg.declare(metadata ")
    genVal(address.value)
    sb.str(", metadata ")
    description.intern().write(sb)
    sb.str(", metadata ")
    expr.intern().write(sb)
    sb.str(")")
    dbg(",", toDILocation(pos, scopeId))
  }

  def compilationUnits(implicit ctx: Context): Seq[DICompileUnit] =
    ctx.writersCache
      .get(classOf[DICompileUnit])
      .map(_.keySet.toSeq.asInstanceOf[Seq[DICompileUnit]])
      .getOrElse(Nil)

  def toDIFile(pos: nir.SourcePosition): DIFile = {
    pos.source
      .flatMap {
        case source: Relative => sourceCodeCache.findSources(source, pos)
        case _                => None
      }
      .map { sourcePath =>
        DIFile(
          filename = sourcePath.getFileName().toString(),
          directory = sourcePath.getParent().toString()
        )
      }
      .getOrElse(DIFile("unknown", "unknown"))
  }

  def toDILocation(
      pos: nir.SourcePosition,
      scopeId: nir.ScopeId
  )(implicit defnScopes: DefnScopes): DILocation = DILocation(
    line = pos.line.toDILine,
    column = pos.column.toDIColumn,
    scope = defnScopes.toDIScope(scopeId)
  )

  class DefnScopes(val defn: nir.Defn.Define)(implicit
      metadataCtx: MetadataCodeGen.Context
  ) {
    private val scopes = mutable.Map.empty[nir.ScopeId, Metadata.Scope]

    lazy val getDISubprogramScope = {
      val pos = defn.pos
      val file = toDIFile(pos)
      val unit = DICompileUnit(
        file = file,
        producer = Constants.PRODUCER,
        isOptimized = defn.attrs.opt == nir.Attr.DidOpt
      )
      val linkageName = mangled(defn.name)
      val ownerName = defn.name.owner.id

      // On Windows if there are no method symbols (LTO enabled) stack traces might return linkage names from found debug symbols
      // Use it to implement stacktraces
      val useFQCName =
        meta.buildConfig.targetsWindows &&
          meta.config.lto != scalanative.build.LTO.None
      def fqcn(methodName: String) = s"$ownerName.$methodName"
      def maybeFQCName(methodName: String) = if (useFQCName) fqcn(methodName) else methodName
      def methodNameInfo(sig: nir.Sig.Unmangled): (String, DIFlags) = sig match {
        case nir.Sig.Extern(id) => id -> DIFlags()
        case nir.Sig.Method(id, _, scope) =>
          maybeFQCName(id) -> DIFlags(sigAccessibilityFlags(scope): _*)
        case nir.Sig.Duplicate(of, _) => methodNameInfo(of.unmangled)
        case nir.Sig.Clinit           => "<clinit>" -> DIFlags(DIFlag.DIFlagPrivate)
        case nir.Sig.Generated(id)    => maybeFQCName(id) -> DIFlags(DIFlag.DIFlagArtificial)
        case nir.Sig.Ctor(_)          => maybeFQCName("<init>") -> DIFlags()
        case nir.Sig.Proxy(id, _)     => maybeFQCName(id) -> DIFlags()
        case _: nir.Sig.Field         => util.unreachable
      }
      val nir.Type.Function(argtys, retty) = defn.ty: @unchecked
      val (name, flags) = methodNameInfo(defn.name.sig.unmangled)
      DISubprogram(
        name = name,
        linkageName = linkageName,
        scope = unit,
        file = file,
        unit = unit,
        line = pos.line.toDILine,
        flags = flags,
        tpe = DISubroutineType(
          DITypes(
            toFunctionMetadataType(retty),
            argtys.map(toMetadataType(_))
          )
        )
      )
    }

    def toDIScope(scopeId: nir.ScopeId): Scope =
      scopes.getOrElseUpdate(
        scopeId,
        if (scopeId.isTopLevel) getDISubprogramScope
        else toDILexicalBlock(scopeId)
      )

    def toDILexicalBlock(scopeId: nir.ScopeId): Metadata.DILexicalBlock = {
      val scope = defn.debugInfo.lexicalScopeOf(scopeId)
      val srcPosition = scope.srcPosition

      Metadata.DILexicalBlock(
        file = toDIFile(srcPosition),
        scope = toDIScope(scope.parent),
        line = srcPosition.line.toDILine,
        column = srcPosition.column.toDIColumn
      )
    }
  }

  private val DIBasicTypes: Map[nir.Type, Metadata.Type] = {
    import nir.Type._
    Seq(Byte, Char, Short, Int, Long, Size, Float, Double, Bool, Ptr).map { tpe =>
      val name = tpe.show
      val nameCapitalize = name.head.toUpper + name.tail
      tpe -> DIBasicType(
        name = nameCapitalize,
        size = MemoryLayout.sizeOf(tpe).toDISize,
        align = MemoryLayout.alignmentOf(tpe).toDISize,
        encoding = tpe match {
          case Bool           => DW_ATE.Boolean
          case Float | Double => DW_ATE.Float
          case Ptr            => DW_ATE.Address
          case Char           => DW_ATE.UTF
          case _              => DW_ATE.Signed
        }
      )
    }.toMap
  }

  protected def toFunctionMetadataType(
      ty: nir.Type
  )(implicit metaCtx: Context): Option[Metadata.Type] = ty match {
    case nir.Type.Unit => None
    case _             => toMetadataType(ty)
  }

  protected def toMetadataType(
      ty: nir.Type,
      underlyingType: Boolean = false
  )(implicit metaCtx: Context): Metadata.Type = {
    import metaCtx.{typeGeneratatorBacktrace => backtrace, diTypesCache => cache}
    val tpe = nir.Type.normalize(ty)
    val metadataType = cache
      .get(tpe)
      .getOrElse {
        if (backtrace.contains(tpe)) Metadata.TypeRef(tpe)
        else {
          backtrace = tpe :: backtrace
          val generated =
            try generateMetadataType(tpe)
            finally backtrace = backtrace.tail
          cache.update(tpe, generated)
          generated
        }
      }
    tpe match {
      case _: nir.Type.RefKind if !underlyingType => pointerTypeOf(metadataType)
      case _                                      => metadataType
    }
  }

  private def pointerTypeOf(ty: Metadata.Type): DIDerivedType =
    DIDerivedType(
      DWTag.Pointer,
      baseType = ty,
      size = platform.sizeOfPtr.toDISize
    )

  private def ObjectMonitorUnionType(implicit metaCtx: MetadataCodeGen.Context) =
    metaCtx.cachedByName[DICompositeType]("scala.scalanative.runtime.ObjectMonitorUnion") { name =>
      DICompositeType(
        DWTag.Union,
        name = name,
        size = platform.sizeOfPtr.toDISize,
        flags = DIFlags(DIFlag.DIFlagArtificial)
      ).withDependentElements { headerRef =>
        Seq(
          DIDerivedType(
            DWTag.Member,
            name = "thinLock",
            baseType = DIBasicTypes(nir.Type.Size),
            size = platform.sizeOfPtr.toDISize
          ),
          DIDerivedType(
            DWTag.Member,
            name = "fatLock",
            baseType = toMetadataType(nir.Rt.RuntimeObjectMonitor),
            size = platform.sizeOfPtr.toDISize
          )
        )
      }
    }

  private def ObjectHeaderType(implicit metaCtx: MetadataCodeGen.Context) =
    metaCtx.cachedByName[DICompositeType]("scala.scalanative.runtime.ObjectHeader") { name =>
      import meta.layouts.ObjectHeader.{layout, size, _}
      DICompositeType(
        DWTag.Structure,
        name = name,
        size = size.toDISize,
        flags = DIFlags(DIFlag.DIFlagArtificial)
      ).withDependentElements { headerRef =>
        MemoryLayout(layout.tys).tys.zipWithIndex.map {
          case (MemoryLayout.PositionedType(ty, offset), idx) =>
            val name = idx match {
              case RttiIdx     => "class"
              case LockWordIdx => "objectMonitor"
            }
            val baseType = idx match {
              case RttiIdx     => toMetadataType(nir.Rt.Class)
              case LockWordIdx => ObjectMonitorUnionType
            }
            DIDerivedType(
              DWTag.Member,
              name = name,
              baseType = baseType,
              size = MemoryLayout.sizeOf(ty).toDISize,
              offset = offset.toDISize,
              scope = headerRef
            )
        }
      }
    }

  private def ArrayHeaderType(implicit metaCtx: MetadataCodeGen.Context) =
    metaCtx.cachedByName[DICompositeType]("scala.scalanative.runtime.ArrayHeader") { name =>
      import meta.layouts.ArrayHeader.{layout, size, _}
      DICompositeType(
        DWTag.Structure,
        name = name,
        size = size.toDISize
      ).withDependentElements { headerRef =>
        val objectHeader = DIDerivedType(
          DWTag.Inheritance,
          baseType = ObjectHeaderType,
          size = ObjectHeaderType.size
        )

        objectHeader +:
          MemoryLayout(layout.tys).tys.zipWithIndex
            .drop(meta.layouts.ObjectHeader.layout.tys.size)
            .map {
              case (MemoryLayout.PositionedType(ty, offset), idx) =>
                val name = idx match {
                  case LengthIdx => "length"
                  case StrideIdx => "stride"
                }
                DIDerivedType(
                  DWTag.Member,
                  name = name,
                  baseType = toMetadataType(ty),
                  size = MemoryLayout.sizeOf(ty).toDISize,
                  offset = offset.toDISize,
                  scope = headerRef
                )
            }
      }
    }

  private def ClassType(implicit metaCtx: MetadataCodeGen.Context) =
    metaCtx.cachedByName[DICompositeType]("java.lang.Class") { name =>
      implicit def analysis: ReachabilityAnalysis.Result = meta.analysis
      val ClassRef(jlClass) = nir.Rt.Class: @unchecked
      import meta.layouts.Rtti.{layout, size, _}

      DICompositeType(
        DWTag.Class,
        name = name,
        size = size.toDISize,
        file = toDIFile(jlClass.position),
        line = jlClass.position.line.toDILine,
        flags = DIFlags(DIFlag.DIFlagArtificial)
      ).withDependentElements { classRef =>
        MemoryLayout(layout.tys).tys.zipWithIndex.map {
          case (MemoryLayout.PositionedType(ty, offset), idx) =>
            val name = idx match {
              case RttiIdx            => "rtti"
              case LockWordIdx        => "lock"
              case ClassIdIdx         => "classId"
              case InterfacesCountIdx => "interfacesCount"
              case InterfacesIdx      => "interfaces"
              case ClassNameIdx       => "className"
            }
            val baseType = idx match {
              case ClassNameIdx => toMetadataType(nir.Rt.String)
              case _            => toMetadataType(ty)
            }
            DIDerivedType(
              DWTag.Member,
              name = name,
              baseType = baseType,
              file = toDIFile(jlClass.position),
              size = MemoryLayout.sizeOf(ty).toDISize,
              offset = offset.toDISize,
              scope = classRef
            )
        }
      }
    }

  private def generateMetadataType(ty: nir.Type)(implicit metaCtx: Context): Metadata.Type = {
    import nir.Type._
    implicit def analysis: ReachabilityAnalysis.Result = metaCtx.codeGen.meta.analysis
    ty match {
      case nir.Type.Unit => toMetadataType(nir.Rt.BoxedUnit)
      case StructValue(tys) =>
        new DICompositeType(
          tag = DWTag.Structure,
          size = MemoryLayout.sizeOf(ty).toDISize
        ).withDependentElements { structRef =>
          MemoryLayout(tys).tys.zipWithIndex.map {
            case (MemoryLayout.PositionedType(ty, offset), idx) =>
              DIDerivedType(
                tag = DWTag.Member,
                name = s"_$idx",
                baseType = toMetadataType(ty),
                scope = structRef,
                size = MemoryLayout.sizeOf(ty).toDISize,
                offset = offset.toDISize
              )
          }
        }

      case ArrayValue(elemTy, n) =>
        new DICompositeType(
          tag = DWTag.Array,
          baseType = toMetadataType(elemTy),
          size = MemoryLayout.sizeOf(ty).toDISize
        ).withElements(DISubrange(count = n.const) :: Nil)

      case ty: nir.Type.ValueKind => DIBasicTypes(ty)

      case ArrayRef(componentCls, _) =>
        val componentName = componentCls match {
          case ref: nir.Type.RefKind => ref.className.id
          case ty                    => ty.show
        }

        DICompositeType(
          DWTag.Class,
          name = s"scala.Array[$componentName]",
          identifier = ty.mangle,
          size = ArrayHeaderType.size,
          flags = DIFlags(
            DIFlag.DIFlagNonTrivial,
            DIFlag.DIFlagArtificial,
            DIFlag.DIFlagTypePassByReference
          )
        ).withDependentElements { structRef =>
          Seq(
            DIDerivedType(
              DWTag.Inheritance,
              baseType = ArrayHeaderType,
              scope = structRef,
              name = "header",
              size = ArrayHeaderType.size
            ),
            DIDerivedType(
              DWTag.Member,
              name = "values",
              offset = ArrayHeaderType.size,
              scope = structRef,
              baseType = DICompositeType(
                DWTag.Array,
                baseType = toMetadataType(componentCls),
                scope = structRef
              )
            )
          )
        }

      case ScopeRef(cls) =>
        val (fieldsLayout, clsParent, traits) = cls.name match {
          case ClassRef(clazz) => (meta.layout.get(clazz), clazz.parent, clazz.traits)
          case TraitRef(clazz) => (None, None, clazz.traits)
          case _               => util.unreachable
        }
        DICompositeType(
          tag = DWTag.Class,
          name = cls.name.id,
          identifier = cls.name.mangle,
          scope = None,
          file = toDIFile(cls.position),
          line = cls.position.line.toDILine,
          size = fieldsLayout.map(_.size.toDISize),
          flags = DIFlags(
            DIFlag.DIFlagObjectPointer,
            DIFlag.DIFlagNonTrivial
          )
        ).withDependentElements { clsRef =>
          val inheritence = {
            val parentType = clsParent
              .map(cls => toMetadataType(cls.ty, underlyingType = true))
              .orElse {
                if (cls.name == nir.Rt.Object.name) ObjectHeaderType
                else None
              }
            val traitTypes = traits.map(cls => toMetadataType(cls.ty, underlyingType = true))
            for (baseType <- (parentType ++ traitTypes).toList)
              yield DIDerivedType(
                DWTag.Inheritance,
                baseType = baseType,
                scope = clsRef,
                flags = DIFlags(DIFlag.DIFlagPublic),
                extraData = Value(nir.Val.Int(0))
              )
          }

          val fields = fieldsLayout.fold(List.empty[DIDerivedType]) { layout =>
            val offsets = layout.layout.fieldOffsets.map(_.toDISize)
            val parentFieldsCount = clsParent.map(meta.layout(_).entries.size)
            layout.entries
              .zip(offsets)
              .drop(parentFieldsCount.getOrElse(0))
              .map {
                case (field, offset) =>
                  val ty = field.ty
                  val (name, flags) = field.name.sig.unmangled match {
                    case nir.Sig.Field(id, scope) => id -> DIFlags(sigAccessibilityFlags(scope): _*)
                    case nir.Sig.Generated(id)    => id -> DIFlags(DIFlag.DIFlagArtificial)
                    case nir.Sig.Extern(id)       => id -> DIFlags()
                    case other                    => scala.scalanative.util.unsupported(other)
                  }

                  DIDerivedType(
                    DWTag.Member,
                    name = name,
                    scope = clsRef,
                    file = toDIFile(field.position),
                    line = field.position.line.toDILine,
                    offset = offset,
                    baseType = toMetadataType(field.ty),
                    size = MemoryLayout.sizeOf(ty).toDISize,
                    flags = flags
                  )
              }
              .toList
          }
          inheritence ::: fields
        }

      case Null | Nothing => DIBasicTypes(Ptr)

      case other =>
        throw new NotImplementedError(s"No idea how to dwarfise ${other.getClass().getName} $other")
    }
  }

  private def sigAccessibilityFlags(scope: nir.Sig.Scope): List[DIFlag] = scope match {
    case nir.Sig.Scope.Public           => DIFlag.DIFlagPublic :: Nil
    case nir.Sig.Scope.PublicStatic     => DIFlag.DIFlagPublic :: DIFlag.DIFlagStaticMember :: Nil
    case _: nir.Sig.Scope.Private       => DIFlag.DIFlagPrivate :: Nil
    case _: nir.Sig.Scope.PrivateStatic => DIFlag.DIFlagPrivate :: DIFlag.DIFlagStaticMember :: Nil
  }
}

private[codegen] object MetadataCodeGen {
  case class Context(codeGen: AbstractCodeGen, sb: ShowBuilder) {
    type WriterCache[T <: Metadata.Node] = mutable.Map[T, Metadata.Id]
    private[MetadataCodeGen] val writersCache
        : mutable.Map[Class[_ <: Metadata.Node], WriterCache[Metadata.Node]] =
      mutable.Map.empty

    private[MetadataCodeGen] val specializedBuilder: Specialized.Builder[_] =
      new Specialized.Builder[Any]()(this)
    private[MetadataCodeGen] val fresh: nir.Fresh = nir.Fresh()
    private[MetadataCodeGen] val diTypesCache = mutable.Map.empty[nir.Type, Metadata.Type]
    private[MetadataCodeGen] var typeGeneratatorBacktrace = List.empty[nir.Type]
    private[MetadataCodeGen] val publishedIds = mutable.Set.empty[Metadata.Id]
    val cachedByNameTypes = mutable.Map.empty[String, Metadata.Type]
    def cachedByName[T <: Metadata.Type](name: String)(create: String => T): T =
      cachedByNameTypes.getOrElseUpdate(name, create(name)).asInstanceOf[T]

    val currentSubprogram = new ScopedVar[Metadata.DISubprogram]()
  }

  implicit class MetadataIdWriter(val id: Metadata.Id) {
    def write(sb: ShowBuilder): Unit = {
      sb.str('!')
      sb.str(id.value)
    }
  }

  trait Writer[T <: Metadata] {
    final def sb(implicit ctx: Context): ShowBuilder = ctx.sb
    final def write(v: T)(implicit ctx: Context): Unit = writeMetadata(v, ctx)
    def writeMetadata(v: T, ctx: Context): Unit
  }

  trait InternedWriter[T <: Metadata.Node] extends Writer[T] {
    import Writer._
    private def asssignedId(v: T)(implicit ctx: Context): Option[Metadata.Id] =
      v.assignedId.orElse(cache(v).get(v))

    private def assignId(v: T)(implicit ctx: Context): Metadata.Id = cache(v).getOrElseUpdate(
      v, {
        val id = Metadata.Id(ctx.fresh().id.toInt)
        v.assignId(id)
        id
      }
    )

    def getOrAssignId(v: T)(implicit ctx: Context): Metadata.Id =
      asssignedId(v).getOrElse(assignId(v))

    final private[MetadataCodeGen] def cache(v: T)(implicit ctx: Context): ctx.WriterCache[T] =
      ctx.writersCache
        .getOrElseUpdate(v.getClass(), mutable.Map.empty)
        .asInstanceOf[ctx.WriterCache[T]]

    protected def internDeps(v: T)(implicit ctx: Context): Unit = {
      def tryIntern(v: Any): Unit = v match {
        case v: Metadata.Node       => v.intern()
        case Some(v: Metadata.Node) => v.intern()
        case _                      => ()
      }
      v match {
        case v: Metadata.Tuple => v.values.foreach(tryIntern)
        case v: Metadata.SpecializedNode =>
          v.productIterator.foreach(tryIntern)
        case _: Metadata.DIExpressions => ()
      }
      v match {
        case v: Metadata.CanBeRecursive => v.recursiveNodes.foreach(tryIntern)
        case _                          => ()
      }
    }

    final def intern(v: T)(implicit ctx: Context): Metadata.Id = v match {
      case v: Metadata.DelayedReference =>
        v match {
          case v: Metadata.TypeRef => ofTypeRef.resolveDelayedId(v)
        }
      case _ =>
        val id = getOrAssignId(v)
        if (ctx.publishedIds.add(id)) {
          internDeps(v)
          sb.newline()
          id.write(sb)
          sb.str(" = ")
          write(v)
        }
        id

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
      ): Metadata.Id = writer.intern(value)

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
      case v: Metadata.DIExpressions   => v.writer
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

    implicit def ofDIExpressions: InternedWriter[DIExpressions] = (v, ctx) => {
      import ctx.sb
      implicit def _ctx: Context = ctx

      sb.str("!DIExpression(")
      sb.rep(v.expressions, sep = ", ")(_.write())
      sb.str(")")
    }

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
          implicit val DISizeField: FieldWriter[DISize] = (ctx: Context, value: DISize) =>
            ctx.sb.str(value.sizeOfBits)
          implicit def MetadataNodeField[T <: Metadata.Node: InternedWriter]: FieldWriter[T] =
            (ctx: Context, value: T) => {
              implicit def _ctx: Context = ctx
              writeInterned(value)
            }
          implicit def MetadataField[T <: Metadata: Writer]: FieldWriter[T] =
            (ctx: Context, value: T) => implicitly[Writer[T]].write(value)(ctx)
          implicit val ofDIFlags: FieldWriter[DIFlags] = (ctx: Context, value: DIFlags) =>
            ctx.sb.str(value.union.mkString(" | "))
          implicit val ofDILine: FieldWriter[DILine] = (ctx: Context, value: DILine) =>
            ctx.sb.str(value.line)
          implicit val ofDIColumn: FieldWriter[DIColumn] = (ctx: Context, value: DIColumn) =>
            ctx.sb.str(value.column)
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
      case v: Metadata.LLVMDebugInformation => v.writer
      case v: Metadata.DISubrange           => v.writer
    }
    implicit lazy val ofDISubrange: Specialized[DISubrange] = {
      case v @ DISubrange(count, lowerBound) =>
        _.field("count", count)
          .field("lowerBound", if (v == DISubrange.empty) None else lowerBound)
    }
    implicit lazy val ofLLVMDebugInformation: Dispatch[Metadata.LLVMDebugInformation] = _ match {
      case v: Metadata.Type   => v.writer
      case v: Metadata.Scope  => v.writer
      case v: DILocation      => v.writer
      case v: DILocalVariable => v.writer
    }
    implicit lazy val ofScope: Dispatch[Metadata.Scope] = _ match {
      case v: DICompileUnit  => v.writer
      case v: DIFile         => v.writer
      case v: DISubprogram   => v.writer
      case v: DILexicalBlock => v.writer
      case v: Metadata.Type  => v.writer
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
      case v: DISubprogram =>
        _.field("name", v.name)
          .field("linkageName", v.linkageName)
          .field("scope", v.scope)
          .field("file", v.file)
          .field("line", v.line)
          .field("type", v.tpe)
          .field("unit", v.unit)
          .field("flags", if (v.flags.nonEmpty) Some(v.flags) else None)
          .field("spFlags", "DISPFlagDefinition".const)
          .field(
            "retainedNodes",
            if (v.retainedNodes.isEmpty) None
            else Some(Tuple(v.retainedNodes.toSeq))
          )
    }

    implicit lazy val ofDILexicalBlock: Specialized[DILexicalBlock] = {
      case DILexicalBlock(scope, file, line, column) =>
        _.field("scope", scope)
          .field("file", file)
          .field("line", line)
          .field("column", column)
    }

    implicit lazy val ofType: Dispatch[Metadata.Type] = _ match {
      case v: DIBasicType      => v.writer
      case v: DIDerivedType    => v.writer
      case v: DISubroutineType => v.writer
      case v: DICompositeType  => v.writer
      case v: TypeRef          => v.writer

    }
    implicit lazy val ofDIBasicType: Specialized[DIBasicType] = {
      case DIBasicType(name, size, align, encoding) =>
        _.field("name", name)
          .field("size", size)
          .field("align", align)
          .field("encoding", encoding)
    }
    implicit lazy val ofDIDerivedType: Specialized[DIDerivedType] = { v =>
      _.field("tag", v.tag)
        .field("name", v.name)
        .field("scope", v.scope)
        .field("file", v.file)
        .field("line", v.line)
        .field("baseType", v.baseType)
        .field("size", v.size)
        .field("offset", v.offset)
        .field("flags", Option(v.flags).filter(_.nonEmpty))
        .field("extraData", v.extraData)
    }
    implicit lazy val ofDISubroutineType: Specialized[DISubroutineType] = {
      case DISubroutineType(types) =>
        _.field("types", types)
    }
    implicit lazy val ofDICompositeType: Specialized[DICompositeType] = { v =>
      _.field("tag", v.tag)
        .field("name", v.name)
        .field("identifier", v.identifier)
        .field("scope", v.scope)
        .field("file", v.file)
        .field("line", v.line)
        .field("size", v.size)
        .field("elements", v.getElements)
        .field("flags", Option(v.flags).filter(_.nonEmpty))
        .field("baseType", v.baseType)
        .field("dataLocation", v.dataLocation)
    }

    implicit object ofTypeRef extends InternedWriter[TypeRef] {
      override protected def internDeps(v: TypeRef)(implicit ctx: Context): Unit = ()

      override def writeMetadata(v: TypeRef, ctx: Context): Unit =
        resolveDelayedId(v)(ctx).write(ctx.sb)

      def resolveDelayedId(v: TypeRef)(implicit ctx: Context): Metadata.Id = {
        val resolved = ctx.diTypesCache(v.ty)
        val cache = ctx.writersCache(resolved.getClass)

        v.assignedId
          .orElse(resolved.assignedId)
          .orElse(cache.get(resolved))
          .getOrElse {
            // Not found actual type, treat it as ObjectHeader. It can happen only in very low level/hacked types, e.g. java.lang.{Object,Array, Cass}
            val composites = ctx.writersCache(classOf[Metadata.DICompositeType])
            val objectHeader = ctx
              .cachedByNameTypes("scala.scalanative.runtime.ObjectHeader")
              .asInstanceOf[DICompositeType]
            composites(objectHeader)
          }
      }
    }

    implicit lazy val ofDILocation: Specialized[DILocation] = {
      case DILocation(line, column, scope) =>
        _.field("line", line)
          .field("column", column)
          .field("scope", scope)
    }
    implicit lazy val ofDILocalVariable: Specialized[DILocalVariable] = {
      case DILocalVariable(name, arg, scope, file, line, tpe, flags) =>
        _.field("name", name)
          .field("arg", arg)
          .field("scope", scope)
          .field("file", file)
          .field("line", line)
          .field("type", tpe)
          .field("flags", if (flags.nonEmpty) Some(flags) else None)
    }
  }

}
