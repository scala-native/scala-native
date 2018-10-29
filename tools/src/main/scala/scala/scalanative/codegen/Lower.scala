package scala.scalanative
package codegen

import scala.collection.mutable
import scalanative.util.ScopedVar
import scalanative.nir._
import scalanative.linker.{
  Class,
  Trait,
  Ref,
  ScopeRef,
  ClassRef,
  TraitRef,
  FieldRef,
  MethodRef
}

object Lower {

  def apply(defns: Seq[Defn])(implicit meta: Metadata): Seq[Defn] =
    (new Impl).onDefns(defns)

  private final class Impl(implicit meta: Metadata) extends Transform {
    import meta._

    implicit val linked = meta.linked

    val Object = linked.infos(Rt.Object.name).asInstanceOf[Class]

    // Type of the bare runtime type information struct.
    private val classRttiType =
      rtti(linked.infos(Global.Top("java.lang.Object"))).struct

    // Names of the fields of the java.lang.String in the memory layout order.
    private val stringFieldNames = {
      val node  = ClassRef.unapply(StringName).get
      val names = layout(node).entries.map(_.name)
      assert(names.length == 4, "java.lang.String is expected to have 4 fields")
      names
    }

    private val retty = new util.ScopedVar[Type]
    private val fresh = new util.ScopedVar[Fresh]

    override def onDefns(defns: Seq[Defn]): Seq[Defn] = {
      val buf = mutable.UnrolledBuffer.empty[Defn]

      defns.foreach {
        case _: Defn.Class | _: Defn.Module | _: Defn.Trait =>
          ()
        case Defn.Declare(attrs, MethodRef(_: Class | _: Trait, _), _)
            if !attrs.isExtern =>
          ()
        case Defn.Var(attrs, FieldRef(_: Class, _), _, _) if !attrs.isExtern =>
          ()
        case defn =>
          buf += onDefn(defn)
      }

      buf
    }

    override def onDefn(defn: Defn): Defn = defn match {
      case defn: Defn.Define =>
        val Type.Function(_, ty) = defn.ty
        ScopedVar.scoped(
          retty := ty,
          fresh := Fresh(defn.insts)
        )(super.onDefn(defn))
      case _ =>
        super.onDefn(defn)
    }

    override def onInsts(insts: Seq[Inst]): Seq[Inst] = {
      val buf = new nir.Buffer()(fresh)
      import buf._

      buf += insts.head

      insts.foreach {
        case Inst.Let(n, Op.Var(ty), unwind) =>
          buf.let(n, Op.Stackalloc(ty, Val.None), unwind)
        case _ =>
          ()
      }

      insts.tail.foreach {
        case inst @ Inst.Let(n, op, unwind) =>
          op.resty match {
            case Type.Unit =>
              genOp(buf, fresh(), op, unwind)
              let(n, Op.Copy(unit), unwind)
            case Type.Nothing =>
              genOp(buf, fresh(), op, unwind)
              unreachable
              label(fresh(), Seq(Val.Local(n, op.resty)))
            case _ =>
              genOp(buf, n, op, unwind)
          }

        case Inst.Throw(v, unwind) =>
          genThrow(buf, v, unwind)

        case Inst.Ret(_) if retty.get == Type.Unit =>
          ret(Val.None)

        case inst =>
          buf += inst
      }

      optimizer.pass.DeadCodeElimination(buf.toSeq.map(super.onInst))
    }

    override def onVal(value: Val): Val = value match {
      case Val.Global(ScopeRef(node), _) =>
        Val.Global(rtti(node).name, Type.Ptr)
      case Val.String(v) =>
        genStringVal(v)
      case Val.Unit =>
        unit
      case _ =>
        super.onVal(value)
    }

    override def onType(ty: Type): Type = ty match {
      case _: Type.RefKind | Type.Nothing =>
        Type.Ptr
      case Type.Function(params, Type.Unit | Type.Nothing) =>
        Type.Function(params.map(onType), Type.Void)
      case _ =>
        super.onType(ty)
    }

    def genThrow(buf: Buffer, exc: Val, unwind: Next) = {
      genOp(buf, fresh(), Op.Call(throwSig, throw_, Seq(exc)), unwind)
      buf.unreachable
    }

    def genOp(buf: Buffer, n: Local, op: Op, unwind: Next): Unit = op match {
      case op: Op.Fieldload =>
        genFieldloadOp(buf, n, op, unwind)
      case op: Op.Fieldstore =>
        genFieldstoreOp(buf, n, op, unwind)
      case op: Op.Method =>
        genMethodOp(buf, n, op, unwind)
      case op: Op.Dynmethod =>
        genDynmethodOp(buf, n, op, unwind)
      case op: Op.Is =>
        genIsOp(buf, n, op, unwind)
      case op: Op.As =>
        genAsOp(buf, n, op, unwind)
      case op: Op.Sizeof =>
        genSizeofOp(buf, n, op, unwind)
      case op: Op.Classalloc =>
        genClassallocOp(buf, n, op, unwind)
      case op: Op.Bin =>
        genBinOp(buf, n, op, unwind)
      case op: Op.Box =>
        genBoxOp(buf, n, op, unwind)
      case op: Op.Unbox =>
        genUnboxOp(buf, n, op, unwind)
      case op: Op.Module =>
        genModuleOp(buf, n, op, unwind)
      case op: Op.Var =>
        ()
      case Op.Varload(Val.Local(slot, Type.Var(ty))) =>
        buf.let(n, Op.Load(ty, Val.Local(slot, Type.Ptr)), unwind)
      case Op.Varstore(Val.Local(slot, Type.Var(ty)), value) =>
        buf.let(n, Op.Store(ty, Val.Local(slot, Type.Ptr), value), unwind)
      case op: Op.Arrayalloc =>
        genArrayallocOp(buf, n, op, unwind)
      case op: Op.Arrayload =>
        genArrayloadOp(buf, n, op, unwind)
      case op: Op.Arraystore =>
        genArraystoreOp(buf, n, op, unwind)
      case op: Op.Arraylength =>
        genArraylengthOp(buf, n, op, unwind)
      case _ =>
        buf.let(n, op, unwind)
    }

    def genFieldElemOp(buf: Buffer, obj: Val, name: Global, unwind: Next) = {
      val FieldRef(cls: Class, fld) = name

      val layout = meta.layout(cls)
      val ty     = layout.struct
      val index  = layout.index(fld)

      buf.elem(ty, obj, Seq(Val.Int(0), Val.Int(index)), unwind)
    }

    def genFieldloadOp(buf: Buffer,
                       n: Local,
                       op: Op.Fieldload,
                       unwind: Next) = {
      val Op.Fieldload(ty, obj, name) = op

      val elem = genFieldElemOp(buf, obj, name, unwind)
      buf.let(n, Op.Load(ty, elem), unwind)
    }

    def genFieldstoreOp(buf: Buffer,
                        n: Local,
                        op: Op.Fieldstore,
                        unwind: Next) = {
      val Op.Fieldstore(ty, obj, name, value) = op

      val elem = genFieldElemOp(buf, obj, name, unwind)
      buf.let(n, Op.Store(ty, elem, value), unwind)
    }

    def genMethodOp(buf: Buffer, n: Local, op: Op.Method, unwind: Next) = {
      import buf._

      val Op.Method(obj, sig) = op

      def genClassVirtual(cls: Class): Unit = {
        val vindex  = vtable(cls).index(sig)
        val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
        val methptrptr = let(
          Op.Elem(rtti(cls).struct,
                  typeptr,
                  Seq(Val.Int(0),
                      Val.Int(5), // index of vtable in type struct
                      Val.Int(vindex))),
          unwind)

        let(n, Op.Load(Type.Ptr, methptrptr), unwind)
      }

      def genTraitVirtual(trt: Trait): Unit = {
        val sigid   = dispatchTable.traitSigIds(sig)
        val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
        val idptr =
          let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(1))), unwind)
        val id = let(Op.Load(Type.Int, idptr), unwind)
        val rowptr = let(
          Op.Elem(Type.Ptr,
                  dispatchTable.dispatchVal,
                  Seq(Val.Int(dispatchTable.dispatchOffset(sigid)))),
          unwind)
        val methptrptr =
          let(Op.Elem(Type.Ptr, rowptr, Seq(id)), unwind)
        let(n, Op.Load(Type.Ptr, methptrptr), unwind)
      }

      val targets = obj.ty match {
        case ScopeRef(scope) =>
          scope.targets(sig).toSeq
        case _ =>
          Seq()
      }

      targets match {
        case Seq() =>
          let(n, Op.Copy(Val.Null), unwind)
        case Seq(impl) =>
          let(n, Op.Copy(Val.Global(impl, Type.Ptr)), unwind)
        case _ =>
          obj.ty match {
            case ClassRef(cls) =>
              genClassVirtual(cls)
            case TraitRef(_) if Object.calls.contains(sig) =>
              genClassVirtual(Object)
            case TraitRef(trt) =>
              genTraitVirtual(trt)
          }
      }
    }

    def genDynmethodOp(buf: Buffer,
                       n: Local,
                       op: Op.Dynmethod,
                       unwind: Next): Unit = {
      import buf._

      val Op.Dynmethod(obj, sig) = op

      def throwInstrs(): Unit = {
        val exc = Val.Local(fresh(), Type.Ptr)
        genClassallocOp(buf, exc.name, Op.Classalloc(excptnGlobal), unwind)
        let(Op.Call(excInitSig, excInit, Seq(exc, Val.String(sig.mangle))),
            unwind)
        genThrow(buf, exc, Next.None)
      }

      def throwIfCond(cond: Op.Comp): Unit = {
        val labelIsNull, labelEndNull = Next(fresh())

        val condNull = let(cond, unwind)
        branch(condNull, labelIsNull, labelEndNull)
        label(labelIsNull.name)
        throwInstrs()
        label(labelEndNull.name)
      }

      def throwIfNull(value: Val) =
        throwIfCond(Op.Comp(Comp.Ieq, Type.Ptr, value, Val.Null))

      val methodIndex =
        meta.linked.dynsigs.zipWithIndex.find(_._1 == sig).get._2

      // Load the type information pointer
      val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
      // Load the pointer of the table size
      val methodCountPtr = let(Op.Elem(classRttiType,
                                       typeptr,
                                       Seq(Val.Int(0), Val.Int(3), Val.Int(0))),
                               unwind)
      // Load the table size
      val methodCount = let(Op.Load(Type.Int, methodCountPtr), unwind)
      throwIfCond(Op.Comp(Comp.Ieq, Type.Int, methodCount, Val.Int(0)))
      // If the size is greater than 0, call the dyndispatch runtime function
      val dyndispatchTablePtr = let(
        Op.Elem(classRttiType,
                typeptr,
                Seq(Val.Int(0), Val.Int(3), Val.Int(0))),
        unwind)
      val methptrptr = let(
        Op.Call(dyndispatchSig,
                dyndispatch,
                Seq(dyndispatchTablePtr, Val.Int(methodIndex))),
        unwind)
      throwIfNull(methptrptr)
      let(n, Op.Load(Type.Ptr, methptrptr), unwind)
    }

    def genIsOp(buf: Buffer, n: Local, op: Op.Is, unwind: Next): Unit = {
      import buf._

      op match {
        case Op.Is(_, Val.Null | Val.Zero(_)) =>
          let(n, Op.Copy(Val.False), unwind)

        case Op.Is(ty, obj) =>
          val result = Val.Local(fresh(), Type.Bool)

          val thenL, elseL, contL = fresh()

          // check if obj is null
          val isnull = let(Op.Comp(Comp.Ieq, Type.Ptr, obj, Val.Null), unwind)
          branch(isnull, Next(thenL), Next(elseL))
          // in case it's null, result is always false
          label(thenL)
          val res1 = let(Op.Copy(Val.False), unwind)
          jump(contL, Seq(res1))
          // otherwise, do an actual instance check
          label(elseL)
          val res2 = genIsOp(buf, ty, obj, unwind)
          jump(contL, Seq(res2))
          // merge the result of two branches
          label(contL, Seq(result))
          let(n, Op.Copy(result), unwind)
      }
    }

    def genIsOp(buf: Buffer, ty: Type, obj: Val, unwind: Next): Val = {
      import buf._

      ty match {
        case ClassRef(cls) if meta.ranges(cls).length == 1 =>
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          let(Op.Comp(Comp.Ieq, Type.Ptr, typeptr, rtti(cls).const), unwind)

        case ClassRef(cls) =>
          val range   = meta.ranges(cls)
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          val idptr =
            let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(0))), unwind)
          val id = let(Op.Load(Type.Int, idptr), unwind)
          val ge =
            let(Op.Comp(Comp.Sle, Type.Int, Val.Int(range.start), id), unwind)
          val le =
            let(Op.Comp(Comp.Sle, Type.Int, id, Val.Int(range.end)), unwind)
          let(Op.Bin(Bin.And, Type.Bool, ge, le), unwind)

        case TraitRef(trt) =>
          val typeptr = let(Op.Load(Type.Ptr, obj), unwind)
          val idptr =
            let(Op.Elem(Rt.Type, typeptr, Seq(Val.Int(0), Val.Int(0))), unwind)
          val id = let(Op.Load(Type.Int, idptr), unwind)
          val boolptr = let(
            Op.Elem(hasTraitTables.classHasTraitTy,
                    hasTraitTables.classHasTraitVal,
                    Seq(Val.Int(0), id, Val.Int(meta.ids(trt)))),
            unwind)
          let(Op.Load(Type.Bool, boolptr), unwind)

        case _ =>
          util.unsupported(s"is[$ty] $obj")
      }
    }

    def genAsOp(buf: Buffer, n: Local, op: Op.As, unwind: Next): Unit =
      op match {
        case Op.As(_: Type.RefKind, v) if v.ty.isInstanceOf[Type.RefKind] =>
          buf.let(n, Op.Copy(v), unwind)
        case Op.As(to, v) =>
          util.unsupported(s"can't cast from ${v.ty} to $to")
      }

    def genSizeofOp(buf: Buffer,
                    n: Local,
                    op: Op.Sizeof,
                    unwind: Next): Unit = {
      val Op.Sizeof(ty, retType) = op

      if (targetArchitecture.is32) {
        assert(retType == Type.Int)
        buf.let(
          n,
          Op.Copy(Val.Int(MemoryLayout.sizeOf(ty, targetArchitecture).toInt)),
          unwind)
      } else {
        assert(retType == Type.Long)
        buf.let(n,
                Op.Copy(Val.Long(MemoryLayout.sizeOf(ty, targetArchitecture))),
                unwind)
      }
    }

    def genClassallocOp(buf: Buffer,
                        n: Local,
                        op: Op.Classalloc,
                        unwind: Next): Unit = {
      val Op.Classalloc(ClassRef(cls)) = op

      val size = MemoryLayout.sizeOf(layout(cls).struct, targetArchitecture)
      val allocMethod =
        if (size < LARGE_OBJECT_MIN_SIZE) alloc else largeAlloc

      buf.let(n,
              Op.Call(allocSig,
                      allocMethod,
                      Seq(rtti(cls).const, Val.Int(size.toInt))),
              unwind)
    }

    def genBinOp(buf: Buffer, n: Local, op: Op.Bin, unwind: Next): Unit = {
      import buf._

      op match {
        // Detects taking remainder for division by -1 and replaces
        // it by division by 1 which can't overflow.
        //
        // We implement '%' (remainder) with LLVM's 'srem' and it
        // can overflow for cases:
        //
        // - Int.MinValue % -1
        // - Long.MinValue % -1
        //
        // E.g. On x86_64 'srem' might get translated to 'idiv'
        // which computes both quotient and remainder at once
        // and quotient can overflow.
        case sremBin @ Op.Bin(Bin.Srem, intType: Type.I, _, divisor)
            if intType.width == 32 || intType.width == 64 =>
          val safeDivisor         = Val.Local(fresh(), intType)
          val thenL, elseL, contL = fresh()

          val isPossibleOverflow =
            let(Op.Comp(Comp.Ieq, intType, divisor, Val.Int(-1)), unwind)
          branch(isPossibleOverflow, Next(thenL), Next(elseL))

          label(thenL)
          jump(contL, Seq(Val.Int(1)))

          label(elseL)
          jump(contL, Seq(divisor))

          label(contL, Seq(safeDivisor))
          let(n, sremBin.copy(r = safeDivisor), unwind)

        case op =>
          let(n, op, unwind)
      }
    }

    def genBoxOp(buf: Buffer, n: Local, op: Op.Box, unwind: Next): Unit = {
      val Op.Box(ty, from) = op

      val methodName = BoxTo(ty)
      val moduleName = methodName.top

      val boxTy =
        Type.Function(Seq(Type.Ref(moduleName), Type.unbox(ty)), ty)

      buf.let(
        n,
        Op.Call(boxTy, Val.Global(methodName, Type.Ptr), Seq(Val.Null, from)),
        unwind)
    }

    def genUnboxOp(buf: Buffer, n: Local, op: Op.Unbox, unwind: Next): Unit = {
      val Op.Unbox(ty, from) = op

      val methodName = UnboxTo(ty)
      val moduleName = methodName.top

      val unboxTy =
        Type.Function(Seq(Type.Ref(moduleName), ty), Type.unbox(ty))

      buf.let(
        n,
        Op.Call(unboxTy, Val.Global(methodName, Type.Ptr), Seq(Val.Null, from)),
        unwind)
    }

    def genModuleOp(buf: Buffer, n: Local, op: Op.Module, unwind: Next) = {
      val Op.Module(name) = op

      val loadSig = Type.Function(Seq(), Type.Ref(name))
      val load    = Val.Global(name.member(Sig.Generated("load")), Type.Ptr)

      buf.let(n, Op.Call(loadSig, load, Seq()), unwind)
    }

    def genArrayallocOp(buf: Buffer,
                        n: Local,
                        op: Op.Arrayalloc,
                        unwind: Next): Unit = {
      val Op.Arrayalloc(ty, init) = op
      init match {
        case len if len.ty == Type.Int =>
          val sig  = arrayAllocSig.getOrElse(ty, arrayAllocSig(Rt.Object))
          val func = arrayAlloc.getOrElse(ty, arrayAlloc(Rt.Object))
          buf.let(n,
                  Op.Call(sig, Val.Global(func, Type.Ptr), Seq(Val.Null, len)),
                  unwind)
        case arrval: Val.ArrayValue =>
          val sig  = arraySnapshotSig.getOrElse(ty, arrayAllocSig(Rt.Object))
          val func = arraySnapshot.getOrElse(ty, arrayAlloc(Rt.Object))
          val len  = Val.Int(arrval.values.length)
          val init = Val.Const(arrval)
          buf.let(
            n,
            Op.Call(sig, Val.Global(func, Type.Ptr), Seq(Val.Null, len, init)),
            unwind)
      }
    }

    def genArrayloadOp(buf: Buffer,
                       n: Local,
                       op: Op.Arrayload,
                       unwind: Next): Unit = {
      val Op.Arrayload(ty, arr, idx) = op

      val sig  = arrayApplySig.getOrElse(ty, arrayApplySig(Rt.Object))
      val func = arrayApply.getOrElse(ty, arrayApply(Rt.Object))
      buf.let(n,
              Op.Call(sig, Val.Global(func, Type.Ptr), Seq(arr, idx)),
              unwind)
    }

    def genArraystoreOp(buf: Buffer,
                        n: Local,
                        op: Op.Arraystore,
                        unwind: Next): Unit = {
      val Op.Arraystore(ty, arr, idx, value) = op

      val sig  = arrayUpdateSig.getOrElse(ty, arrayUpdateSig(Rt.Object))
      val func = arrayUpdate.getOrElse(ty, arrayUpdate(Rt.Object))
      buf.let(n,
              Op.Call(sig, Val.Global(func, Type.Ptr), Seq(arr, idx, value)),
              unwind)
    }

    def genArraylengthOp(buf: Buffer,
                         n: Local,
                         op: Op.Arraylength,
                         unwind: Next): Unit = {
      val Op.Arraylength(arr) = op

      val sig  = arrayLengthSig
      val func = arrayLength
      buf.let(n, Op.Call(sig, Val.Global(func, Type.Ptr), Seq(arr)), unwind)
    }

    def genStringVal(value: String): Val = {
      val StringCls    = ClassRef.unapply(StringName).get
      val CharArrayCls = ClassRef.unapply(CharArrayName).get

      val chars       = value.toCharArray
      val charsLength = Val.Int(chars.length)
      val charsConst = Val.Const(
        Val.StructValue(
          Seq(
            rtti(CharArrayCls).const,
            charsLength,
            Val.Int(0), // padding to get next field aligned properly
            Val.ArrayValue(Type.Short, chars.map(c => Val.Short(c.toShort)))
          )
        ))

      val fieldValues = stringFieldNames.map {
        case StringValueName          => charsConst
        case StringOffsetName         => Val.Int(0)
        case StringCountName          => charsLength
        case StringCachedHashCodeName => Val.Int(stringHashCode(value))
        case _                        => util.unreachable
      }

      Val.Const(Val.StructValue(rtti(StringCls).const +: fieldValues))
    }

    // Update java.lang.String::hashCode whenever you change this method.
    def stringHashCode(s: String): Int =
      if (s.length == 0) {
        0
      } else {
        val value = s.toCharArray
        var hash  = 0
        var i     = 0
        while (i < value.length) {
          hash = value(i) + ((hash << 5) - hash)
          i += 1
        }
        hash
      }
  }

  val LARGE_OBJECT_MIN_SIZE = 8192

  val allocSig = Type.Function(Seq(Type.Ptr, Type.Int), Type.Ptr)

  val allocSmallName = extern("scalanative_alloc_small")
  val alloc          = Val.Global(allocSmallName, allocSig)

  val largeAllocName = extern("scalanative_alloc_large")
  val largeAlloc     = Val.Global(largeAllocName, allocSig)

  val dyndispatchName = extern("scalanative_dyndispatch")
  val dyndispatchSig =
    Type.Function(Seq(Type.Ptr, Type.Int), Type.Ptr)
  val dyndispatch = Val.Global(dyndispatchName, dyndispatchSig)

  val excptnGlobal = Global.Top("java.lang.NoSuchMethodException")
  val excptnInitGlobal =
    Global.Member(excptnGlobal, Sig.Ctor(Seq(nir.Rt.String)))

  val excInitSig = Type.Function(
    Seq(Type.Ref(excptnGlobal), Type.Ref(Global.Top("java.lang.String"))),
    Type.Unit)
  val excInit = Val.Global(excptnInitGlobal, Type.Ptr)

  val StringName       = Rt.String.name
  val StringValueName  = StringName.member(Sig.Field("value"))
  val StringOffsetName = StringName.member(Sig.Field("offset"))
  val StringCountName  = StringName.member(Sig.Field("count"))
  val StringCachedHashCodeName =
    StringName.member(Sig.Field("cachedHashCode"))

  val CharArrayName =
    Global.Top("scala.scalanative.runtime.CharArray")

  val BoxesRunTime = Global.Top("scala.runtime.BoxesRunTime$")
  val RuntimeBoxes = Global.Top("scala.scalanative.runtime.Boxes$")

  val BoxTo: Map[Type, Global] = Seq(
    "java.lang.Boolean",
    "java.lang.Character",
    "scala.scalanative.native.UByte",
    "java.lang.Byte",
    "scala.scalanative.native.UShort",
    "java.lang.Short",
    "scala.scalanative.native.UInt",
    "java.lang.Integer",
    "scala.scalanative.native.ULong",
    "java.lang.Long",
    "java.lang.Float",
    "java.lang.Double"
  ).map { name =>
    val boxty = Type.Ref(Global.Top(name))
    val module =
      if (name.startsWith("java.lang")) BoxesRunTime else RuntimeBoxes
    val id    = "boxTo" + name.split("\\.").last
    val retty = if (name.startsWith("java.lang")) boxty else nir.Rt.Object
    val tys   = Seq(toSigned(nir.Type.unbox(boxty)), retty)
    val meth  = module.member(Sig.Method(id, tys))

    boxty -> meth
  }.toMap

  val UnboxTo: Map[Type, Global] = Seq(
    "java.lang.Boolean",
    "java.lang.Character",
    "scala.scalanative.native.UByte",
    "java.lang.Byte",
    "scala.scalanative.native.UShort",
    "java.lang.Short",
    "scala.scalanative.native.UInt",
    "java.lang.Integer",
    "scala.scalanative.native.ULong",
    "java.lang.Long",
    "java.lang.Float",
    "java.lang.Double"
  ).map { name =>
    val boxty = Type.Ref(Global.Top(name))
    val module =
      if (name.startsWith("java.lang")) BoxesRunTime else RuntimeBoxes
    val id = {
      val last = name.split("\\.").last
      val suffix =
        if (last == "Integer") "Int"
        else if (last == "Character") "Char"
        else last
      "unboxTo" + suffix
    }
    val tys  = Seq(nir.Rt.Object, toSigned(nir.Type.unbox(boxty)))
    val meth = module.member(Sig.Method(id, tys))

    boxty -> meth
  }.toMap

  private def extern(id: String): Global =
    Global.Member(Global.Top("__"), Sig.Extern(id))

  private def toSigned(ty: Type): Type = ty match {
    case Type.UByte  => Type.Byte
    case Type.UShort => Type.Short
    case Type.UInt   => Type.Int
    case Type.ULong  => Type.Long
    case _           => ty
  }

  val unitName = Global.Top("scala.scalanative.runtime.BoxedUnit$")
  val unit     = Val.Global(unitName, Type.Ptr)
  val unitTy =
    Type.StructValue(Seq(Type.Ptr))
  val unitConst =
    Val.Global(unitName.member(Sig.Generated("type")), Type.Ptr)
  val unitValue = Val.StructValue(Seq(unitConst))

  val throwName = extern("scalanative_throw")
  val throwSig  = Type.Function(Seq(Type.Ptr), Type.Void)
  val throw_    = Val.Global(throwName, Type.Ptr)

  val arrayAlloc = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname
      val arrcls         = Type.Ref(arrname)
      ty -> Global.Member(Global.Top(id + "$"),
                          Sig.Method("alloc", Seq(Type.Int, arrcls)))
  }.toMap
  val arrayAllocSig = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname
      ty -> Type.Function(Seq(Type.Ref(Global.Top(id + "$")), Type.Int),
                          Type.Ref(arrname))
  }.toMap
  val arraySnapshot = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname
      val arrcls         = Type.Ref(arrname)
      ty -> Global.Member(
        Global.Top(id + "$"),
        Sig.Method("snapshot", Seq(Type.Int, Type.Ptr, arrcls)))
  }.toMap
  val arraySnapshotSig = Type.typeToArray.map {
    case (ty, arrname) =>
      val Global.Top(id) = arrname
      ty -> Type.Function(
        Seq(Type.Ref(Global.Top(id + "$")), Type.Int, Type.Ptr),
        Type.Ref(arrname))
  }.toMap
  val arrayApplyGeneric = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(arrname,
                          Sig.Method("apply", Seq(Type.Int, nir.Rt.Object)))
  }
  val arrayApply = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(arrname, Sig.Method("apply", Seq(Type.Int, ty)))
  }.toMap
  val arrayApplySig = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Type.Function(Seq(Type.Ref(arrname), Type.Int), ty)
  }.toMap
  val arrayUpdateGeneric = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Global.Member(
        arrname,
        Sig.Method("update", Seq(Type.Int, nir.Rt.Object, Type.Unit)))
  }
  val arrayUpdate = Type.typeToArray.map {
    case (ty @ Type.Unit, arrname) =>
      ty -> Global.Member(
        arrname,
        Sig.Method("update",
                   Seq(Type.Int,
                       Type.Ref(Global.Top("scala.runtime.BoxedUnit")),
                       Type.Unit)))
    case (ty, arrname) =>
      ty -> Global.Member(arrname,
                          Sig.Method("update", Seq(Type.Int, ty, Type.Unit)))
  }.toMap
  val arrayUpdateSig = Type.typeToArray.map {
    case (ty, arrname) =>
      ty -> Type.Function(Seq(Type.Ref(arrname), Type.Int, ty), Type.Unit)
  }.toMap
  val arrayLength =
    Global.Member(Global.Top("scala.scalanative.runtime.Array"),
                  Sig.Method("length", Seq(Type.Int)))
  val arrayLengthSig =
    Type.Function(Seq(Type.Ref(Global.Top("scala.scalanative.runtime.Array"))),
                  Type.Int)

  val injects: Seq[Defn] = {
    val buf = mutable.UnrolledBuffer.empty[Defn]
    buf += Defn.Declare(Attrs.None, allocSmallName, allocSig)
    buf += Defn.Declare(Attrs.None, largeAllocName, allocSig)
    buf += Defn.Declare(Attrs.None, dyndispatchName, dyndispatchSig)
    buf += Defn.Const(Attrs.None, unitName, unitTy, unitValue)
    buf += Defn.Declare(Attrs.None, throwName, throwSig)
    buf
  }

  val depends: Seq[Global] = {
    val buf = mutable.UnrolledBuffer.empty[Global]
    buf += excptnGlobal
    buf += excptnInitGlobal
    buf += StringName
    buf += StringValueName
    buf += StringOffsetName
    buf += StringCountName
    buf += StringCachedHashCodeName
    buf += CharArrayName
    buf += BoxesRunTime
    buf += RuntimeBoxes
    buf += unitName
    buf ++= BoxTo.values
    buf ++= UnboxTo.values
    buf += arrayLength
    buf ++= arrayAlloc.values
    buf ++= arraySnapshot.values
    buf ++= arrayApplyGeneric.values
    buf ++= arrayApply.values
    buf ++= arrayUpdateGeneric.values
    buf ++= arrayUpdate.values
    buf
  }
}
