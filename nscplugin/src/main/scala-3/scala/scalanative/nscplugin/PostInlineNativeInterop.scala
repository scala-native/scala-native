package scala.scalanative.nscplugin

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools._
import dotc._
import dotc.ast.tpd._
import dotc.transform.SymUtils.setter
import core.Contexts._
import core.Definitions
import core.Names._
import core.Symbols._
import core.Types._
import core.Flags._
import core.StdNames._
import core.Constants.Constant
import NirGenUtil.ContextCached

/** This phase does:
 *    - handle TypeApply -> Apply conversion for intrinsic methods
 */
object PostInlineNativeInterop {
  val name = "scalanative-prepareInterop-postinline"
}

class PostInlineNativeInterop extends PluginPhase with NativeInteropUtil {
  override val runsAfter = Set(transform.Inlining.name, PrepNativeInterop.name)
  override val runsBefore = Set(transform.FirstTransform.name)
  val phaseName = PostInlineNativeInterop.name
  override def description: String = "prepare ASTs for Native interop"

  private def isTopLevelExtern(dd: ValOrDefDef)(using Context) = {
    dd.rhs.symbol == defnNir.UnsafePackage_extern &&
    dd.symbol.isWrappedToplevelDef
  }

  private class DealiasTypeMapper(using Context) extends TypeMap {
    override def apply(tp: Type): Type =
      val sym = tp.typeSymbol
      val dealiased =
        if sym.isOpaqueAlias then sym.opaqueAlias
        else tp
      dealiased.widenDealias match
        case AppliedType(tycon, args) =>
          AppliedType(this(tycon), args.map(this))
        case ty => ty
  }

  override def transformApply(tree: Apply)(using Context): Tree = {
    val defnNir = this.defnNir
    def dealiasTypeMapper = DealiasTypeMapper()

    // Attach exact type information to the AST to preserve the type information
    // during the type erase phase and refer to it in the NIR generation phase.
    tree match
      case app @ Apply(TypeApply(fun, tArgs), _)
          if defnNir.CFuncPtr_fromScalaFunction.contains(fun.symbol) =>
        val tys = tArgs.map(t => dealiasTypeMapper(t.tpe))
        app.withAttachment(NirDefinitions.NonErasedTypes, tys)

      case Apply(fun, args) if defnNir.CFuncPtr_apply.contains(fun.symbol) =>
        val paramTypes =
          args.map(a => dealiasTypeMapper(a.tpe)) :+
            dealiasTypeMapper(tree.tpe.finalResultType)
        fun match {
          case Select(Inlined(_, _, ext), _) =>
            // Apply(Select(Inlined(_,_,_),_),_) would not preserve the attachment, use the receiver as a carrier
            fun.putAttachment(NirDefinitions.NonErasedTypes, paramTypes)
            tree
          case _ => ()
        }
        tree.withAttachment(NirDefinitions.NonErasedTypes, paramTypes)

      case Apply(fun, args)
          if defnNir.Intrinsics_stackallocAlts.contains(fun.symbol) =>
        val tpe = fun match {
          case TypeApply(_, Seq(argTpe)) => dealiasTypeMapper(argTpe.tpe)
        }
        val tpeSym = tpe.typeSymbol
        if (tpe.isAny || tpe.isNothingType || tpe.isNullType ||
            tpeSym.isAbstractType && !tpeSym.isAllOf(DeferredType | TypeParam))
          report.error(
            s"Stackalloc requires concrete type but ${tpe.show} found",
            tree.srcPos
          )
        tree.withAttachment(NirDefinitions.NonErasedType, tpe)

      case _ => tree

  }

  override def transformTypeApply(tree: TypeApply)(using Context): Tree = {
    val TypeApply(fun, tArgs) = tree
    val defnNir = this.defnNir
    def dealiasTypeMapper = DealiasTypeMapper()

    // sizeOf[T] -> sizeOf(classOf[T])
    fun.symbol match
      case defnNir.Intrinsics_sizeOf =>
        val tpe = dealiasTypeMapper(tArgs.head.tpe)
        cpy
          .Apply(tree)(
            ref(defnNir.IntrinsicsInternal_sizeOf),
            List(Literal(Constant(tpe)))
          )
          .withAttachment(NirDefinitions.NonErasedType, tpe)

      // alignmentOf[T] -> alignmentOf(classOf[T])
      case defnNir.Intrinsics_alignmentOf =>
        val tpe = dealiasTypeMapper(tArgs.head.tpe)
        cpy
          .Apply(tree)(
            ref(defnNir.IntrinsicsInternal_alignmentOf),
            List(Literal(Constant(tpe)))
          )
          .withAttachment(NirDefinitions.NonErasedType, tpe)

      case _ => tree
  }

}

// Select(
//   Inlined(
//     Apply(Select(Apply(Apply(TypeApply(Ident(ptrToCStruct),List(TypeTree[TypeVar(TypeParamRef(T) -> AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CStruct2),List(AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr0),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))), AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize))))))])),List(Ident(x))),List(Apply(TypeApply(Ident(materializeCStruct2Tag),List(TypeTree[TypeVar(TypeParamRef(T1) -> AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr0),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))))], TypeTree[TypeVar(TypeParamRef(T2) -> AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize))))])),List(Apply(TypeApply(Ident(materializeCFuncPtr0),List(TypeTree[TypeVar(TypeParamRef(R) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))])),List(Ident(materializeIntTag))), Apply(TypeApply(Ident(materializeCFuncPtr1),List(TypeTree[TypeVar(TypeParamRef(T1) -> AppliedType(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),Ptr),List(TypeRef(TermRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),object package),type CChar))))], TypeTree[TypeVar(TypeParamRef(R) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsigned),USize))])),List(Apply(TypeApply(Ident(materializePtrTag),List(TypeTree[TypeVar(TypeParamRef(T) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Byte))])),List(Ident(materializeByteTag))), Ident(materializeUSizeTag))))))),_2),List(Apply(TypeApply(Ident(materializeCStruct2Tag),List(TypeTree[TypeVar(TypeParamRef(T1) -> AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr0),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))))], TypeTree[TypeVar(TypeParamRef(T2) -> AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize))))])),List(Apply(TypeApply(Ident(materializeCFuncPtr0),List(TypeTree[TypeVar(TypeParamRef(R) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))])),List(Ident(materializeIntTag))), Apply(TypeApply(Ident(materializeCFuncPtr1),List(TypeTree[TypeVar(TypeParamRef(T1) -> AppliedType(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),Ptr),List(TypeRef(TermRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),object package),type CChar))))], TypeTree[TypeVar(TypeParamRef(R) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsigned),USize))])),List(Apply(TypeApply(Ident(materializePtrTag),List(TypeTree[TypeVar(TypeParamRef(T) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Byte))])),List(Ident(materializeByteTag))), Ident(materializeUSizeTag))))))),List(ValDef(CStruct2_this,TypeTree[AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CStruct2),List(AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr0),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))), AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize)))))],Apply(Apply(TypeApply(Ident(ptrToCStruct),List(TypeTree[TypeVar(TypeParamRef(T) -> AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CStruct2),List(AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr0),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))), AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize))))))])),List(Ident(x))),List(Apply(TypeApply(Ident(materializeCStruct2Tag),List(TypeTree[TypeVar(TypeParamRef(T1) -> AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr0),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))))], TypeTree[TypeVar(TypeParamRef(T2) -> AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize))))])),List(Apply(TypeApply(Ident(materializeCFuncPtr0),List(TypeTree[TypeVar(TypeParamRef(R) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))])),List(Ident(materializeIntTag))), Apply(TypeApply(Ident(materializeCFuncPtr1),List(TypeTree[TypeVar(TypeParamRef(T1) -> AppliedType(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),Ptr),List(TypeRef(TermRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),object package),type CChar))))], TypeTree[TypeVar(TypeParamRef(R) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsigned),USize))])),List(Apply(TypeApply(Ident(materializePtrTag),List(TypeTree[TypeVar(TypeParamRef(T) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Byte))])),List(Ident(materializeByteTag))), Ident(materializeUSizeTag)))))))), ValDef(tag$proxy10,TypeTree[AppliedType(TypeRef(TermRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),object Tag),class CStruct2),List(AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr0),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int))), AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize)))))],Apply(TypeApply(Ident(materializeCStruct2Tag),List(TypeTree[AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr0),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int)))], TypeTree[AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize)))])),List(Apply(TypeApply(Ident(materializeCFuncPtr0),List(TypeTree[TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),class Int)])),List(Ident(materializeIntTag))), Apply(TypeApply(Ident(materializeCFuncPtr1),List(TypeTree[AppliedType(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),Ptr),List(TypeRef(TermRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),object package),type CChar)))], TypeTree[TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsigned),USize)])),List(Apply(TypeApply(Ident(materializePtrTag),List(TypeTree[TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Byte)])),List(Ident(materializeByteTag))), Ident(materializeUSizeTag))))))),Typed(Block(List(ValDef(ptr,TypeTree[AppliedType(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),Ptr),List(AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize)))))],Apply(TypeApply(Select(New(AppliedTypeTree(Ident(Ptr),List(Ident(T2)))),<init>),List(TypeTree[AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize)))])),List(Apply(Ident(elemRawPtr),List(Select(Inlined(EmptyTree,List(),Ident(CStruct2_this)),inline$rawptr), Apply(Select(Inlined(EmptyTree,List(),Ident(CStruct2_this)),inline$rawSize$i5),List(Apply(Select(Inlined(EmptyTree,List(),Ident(tag$proxy10)),offset),List(Inlined(Apply(Ident(toUSize),List(Literal(Constant(1)))),List(),Typed(Apply(Ident(unsignedOf),List(Apply(Ident(castIntToRawSizeUnsigned),List(Inlined(EmptyTree,List(),Literal(Constant(1))))))),Ident(USize))))))))))))),Inlined(Apply(Select(Ident(ptr),unary_!),List(Select(Inlined(EmptyTree,List(),Ident(tag$proxy10)),_2))),List(ValDef(Ptr_this,TypeTree[TermRef(NoPrefix,val ptr)],Ident(ptr))),Typed(Inlined(Apply(TypeApply(Select(Ident(Ptr),load),List(Ident(T))),List(Inlined(EmptyTree,List(),Ident(Ptr_this)))),List(),Block(List(ValDef($scrutinee6,TypeTree[TermRef(NoPrefix,val Ptr_this)],Inlined(EmptyTree,List(),Ident(Ptr_this))), ValDef(ptr,TypeTree[AppliedType(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),Ptr),List(AppliedType(TypeRef(ThisType(TypeRef(NoPrefix,module class unsafe)),class CFuncPtr1),List(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CString), TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class unsafe)),object package),type CSize)))))],Ident($scrutinee6))),Apply(Select(Apply(TypeApply(Ident(materializeCFuncPtr1),List(TypeTree[TypeVar(TypeParamRef(T1) -> AppliedType(TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),Ptr),List(TypeRef(TermRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsafe),object package),type CChar))))], TypeTree[TypeVar(TypeParamRef(R) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class scalanative)),object unsigned),USize))])),List(Apply(TypeApply(Ident(materializePtrTag),List(TypeTree[TypeVar(TypeParamRef(T) -> TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Byte))])),List(Ident(materializeByteTag))), Ident(materializeUSizeTag))),load),List(Ident(ptr))))),Ident(T)))),Ident(T2)))
//   ,apply)
