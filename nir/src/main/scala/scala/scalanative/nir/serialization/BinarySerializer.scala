package scala.scalanative
package nir
package serialization

import java.net.URI
import java.io.{DataOutputStream, OutputStream}
import java.nio.charset.StandardCharsets
import scala.collection.mutable
import scala.scalanative.nir.serialization.{Tags => T}

final class BinarySerializer {
  private[this] val bufferUnderyling = new JumpBackByteArrayOutputStream
  private[this] val buffer = new DataOutputStream(bufferUnderyling)

  private[this] var lastPosition: Position = Position.NoPosition
  private[this] val fileIndexMap = mutable.Map.empty[URI, Int]

  // Methods were renamed in order to not pollute git blame history.
  // Original implementation used ByteBuffers
  import buffer.{
    write => put,
    writeDouble => putDouble,
    writeFloat => putFloat,
    writeChar => putChar,
    writeLong => putLong,
    writeShort => putShort
  }
  import bufferUnderyling.currentPosition

  def serialize(defns: Seq[Defn], outputStream: OutputStream): Unit = {
    val names = defns.map(_.name)
    val filenames = initFiles(defns)
    val positions = mutable.UnrolledBuffer.empty[Int]

    Prelude.writeTo(
      buffer,
      Prelude(
        Versions.magic,
        Versions.compat,
        Versions.revision,
        Defn.existsEntryPoint(defns)
      )
    )

    putSeq(filenames)(putUTF8String)

    putSeq(names) { n =>
      putGlobal(n)
      positions += currentPosition()
      putLeb(0)
    }

    defns
      .zip(positions)
      .foreach {
        case (defn, marker) =>
          val offset: Int = currentPosition()
          bufferUnderyling.jumpTo(marker)
          putLeb(offset)
          bufferUnderyling.continue()
          putDefn(defn)
      }

    buffer.flush()
    bufferUnderyling.writeTo(outputStream)
  }

  private def putTag(value: Int): Unit = put(value.toByte)

  private def putLeb(v: Int): Unit = {
    var value     = v
    var remaining = value >> 7
    var hasMore   = true
    var end       = if ((value & java.lang.Integer.MIN_VALUE) == 0) 0 else -1
    while (hasMore) {
      hasMore = (remaining != end) || ((remaining & 1) != ((value >> 6) & 1))
      buffer.put(((value & 0x7f) | (if (hasMore) 0x80 else 0)).toByte)
      value = remaining
      remaining >>= 7
    }
  }
  private def putLebs(ints: Seq[Int]) = putSeq[Int](ints)(putLeb(_))

  private def putSeq[T](seq: Seq[T])(putT: T => Unit) = {
    putLeb(seq.length)
    seq.foreach(putT)
  }

  private def putOpt[T](opt: Option[T])(putT: T => Unit) = opt match {
    case None    => put(0.toByte)
    case Some(t) => put(1.toByte); putT(t)
  }

  private def putUTF8String(v: String) = putBytes {
    v.getBytes(StandardCharsets.UTF_8)
  }

  private def putBytes(bytes: Array[Byte]) = {
    putLeb(bytes.length)
    put(bytes)
  }

  private def putBool(v: Boolean) = put((if (v) 1 else 0).toByte)

  private def putAttrs(attrs: Attrs) = putSeq(attrs.toSeq)(putAttr)
  private def putAttr(attr: Attr) = attr match {
    case Attr.MayInline    => putTag(T.MayInlineAttr)
    case Attr.InlineHint   => putTag(T.InlineHintAttr)
    case Attr.NoInline     => putTag(T.NoInlineAttr)
    case Attr.AlwaysInline => putTag(T.AlwaysInlineAttr)

    case Attr.MaySpecialize => putTag(T.MaySpecialize)
    case Attr.NoSpecialize  => putTag(T.NoSpecialize)

    case Attr.UnOpt        => putTag(T.UnOptAttr)
    case Attr.NoOpt        => putTag(T.NoOptAttr)
    case Attr.DidOpt       => putTag(T.DidOptAttr)
    case Attr.BailOpt(msg) => putTag(T.BailOptAttr); putUTF8String(msg)

    case Attr.Dyn  => putTag(T.DynAttr)
    case Attr.Stub => putTag(T.StubAttr)
    case Attr.Extern(isBlocking) =>
      putTag(T.ExternAttr)
      putBool(isBlocking)
    case Attr.Link(s)  => putTag(T.LinkAttr); putUTF8String(s)
    case Attr.Abstract => putTag(T.AbstractAttr)
    case Attr.Volatile => putTag(T.VolatileAttr)
    case Attr.Final    => putTag(T.FinalAttr)

    case Attr.LinktimeResolved => putTag(T.LinktimeResolvedAttr)
  }

  private def putBin(bin: Bin) = bin match {
    case Bin.Iadd => putTag(T.IaddBin)
    case Bin.Fadd => putTag(T.FaddBin)
    case Bin.Isub => putTag(T.IsubBin)
    case Bin.Fsub => putTag(T.FsubBin)
    case Bin.Imul => putTag(T.ImulBin)
    case Bin.Fmul => putTag(T.FmulBin)
    case Bin.Sdiv => putTag(T.SdivBin)
    case Bin.Udiv => putTag(T.UdivBin)
    case Bin.Fdiv => putTag(T.FdivBin)
    case Bin.Srem => putTag(T.SremBin)
    case Bin.Urem => putTag(T.UremBin)
    case Bin.Frem => putTag(T.FremBin)
    case Bin.Shl  => putTag(T.ShlBin)
    case Bin.Lshr => putTag(T.LshrBin)
    case Bin.Ashr => putTag(T.AshrBin)
    case Bin.And  => putTag(T.AndBin)
    case Bin.Or   => putTag(T.OrBin)
    case Bin.Xor  => putTag(T.XorBin)
  }

  private def putInsts(insts: Seq[Inst]) = putSeq(insts)(putInst)
  private def putInst(cf: Inst) = cf match {
    case Inst.None =>
      putTag(T.NoneInst)

    case Inst.Label(name, params) =>
      putTag(T.LabelInst)
      putLocal(name)
      putParams(params)

      case Inst.Let(name, op, Next.None) =>
        putTag(T.LetInst)
        putLocal(name)
        putOp(op)

      case Inst.Let(name, op, unwind) =>
        putTag(T.LetUnwindInst)
        putLocal(name)
        putOp(op)
        putNext(unwind)

      case Inst.Ret(v) =>
        putTag(T.RetInst)
        putVal(v)

      case Inst.Jump(next) =>
        putTag(T.JumpInst)
        putNext(next)

      case Inst.If(v, thenp, elsep) =>
        putTag(T.IfInst)
        putVal(v)
        putNext(thenp)
        putNext(elsep)

      case Inst.LinktimeIf(v, thenp, elsep) =>
        putTag(T.LinktimeIfInst)
        putLinktimeCondition(v)
        putNext(thenp)
        putNext(elsep)

      case Inst.Switch(v, default, cases) =>
        putTag(T.SwitchInst)
        putVal(v)
        putNext(default)
        putNexts(cases)

      case Inst.Throw(v, unwind) =>
        putTag(T.ThrowInst)
        putVal(v)
        putNext(unwind)

      case Inst.Unreachable(unwind) =>
        putTag(T.UnreachableInst)
        putNext(unwind)
    }
  }

  private def putComp(comp: Comp) = comp match {
    case Comp.Ieq => putTag(T.IeqComp)
    case Comp.Ine => putTag(T.IneComp)
    case Comp.Ugt => putTag(T.UgtComp)
    case Comp.Uge => putTag(T.UgeComp)
    case Comp.Ult => putTag(T.UltComp)
    case Comp.Ule => putTag(T.UleComp)
    case Comp.Sgt => putTag(T.SgtComp)
    case Comp.Sge => putTag(T.SgeComp)
    case Comp.Slt => putTag(T.SltComp)
    case Comp.Sle => putTag(T.SleComp)

    case Comp.Feq => putTag(T.FeqComp)
    case Comp.Fne => putTag(T.FneComp)
    case Comp.Fgt => putTag(T.FgtComp)
    case Comp.Fge => putTag(T.FgeComp)
    case Comp.Flt => putTag(T.FltComp)
    case Comp.Fle => putTag(T.FleComp)
  }

  private def putConv(conv: Conv) = conv match {
    case Conv.Trunc     => putTag(T.TruncConv)
    case Conv.Zext      => putTag(T.ZextConv)
    case Conv.Sext      => putTag(T.SextConv)
    case Conv.Fptrunc   => putTag(T.FptruncConv)
    case Conv.Fpext     => putTag(T.FpextConv)
    case Conv.Fptoui    => putTag(T.FptouiConv)
    case Conv.Fptosi    => putTag(T.FptosiConv)
    case Conv.Uitofp    => putTag(T.UitofpConv)
    case Conv.Sitofp    => putTag(T.SitofpConv)
    case Conv.Ptrtoint  => putTag(T.PtrtointConv)
    case Conv.Inttoptr  => putTag(T.InttoptrConv)
    case Conv.Bitcast   => putTag(T.BitcastConv)
    case Conv.SSizeCast => putTag(T.SSizeCastConv)
    case Conv.ZSizeCast => putTag(T.ZSizeCastConv)
  }

  private def putDefn(value: Defn): Unit = {
    putPosition(value.pos)
    value match {
      case Defn.Var(attrs, name, ty, value) =>
        putTag(T.VarDefn)
        putAttrs(attrs)
        putGlobal(name)
        putType(ty)
        putVal(value)

      case Defn.Const(attrs, name, ty, value) =>
        putTag(T.ConstDefn)
        putAttrs(attrs)
        putGlobal(name)
        putType(ty)
        putVal(value)

      case Defn.Declare(attrs, name, ty) =>
        putTag(T.DeclareDefn)
        putAttrs(attrs)
        putGlobal(name)
        putType(ty)

      case Defn.Define(attrs, name, ty, insts) =>
        putTag(T.DefineDefn)
        putAttrs(attrs)
        putGlobal(name)
        putType(ty)
        putInsts(insts)

    case Defn.Struct(attrs, name, members) =>
      putTag(T.StructDefn)
      putAttrs(attrs)
      putGlobal(name)
      putTypes(members)

    case Defn.Trait(attrs, name, ifaces) =>
      putTag(T.TraitDefn)
      putAttrs(attrs)
      putGlobal(name)
      putGlobals(ifaces)

      case Defn.Class(attrs, name, parent, ifaces) =>
        putTag(T.ClassDefn)
        putAttrs(attrs)
        putGlobal(name)
        putGlobalOpt(parent)
        putGlobals(ifaces)

      case Defn.Module(attrs, name, parent, ifaces) =>
        putTag(T.ModuleDefn)
        putAttrs(attrs)
        putGlobal(name)
        putGlobalOpt(parent)
        putGlobals(ifaces)
    }
  }

  private def putGlobals(globals: Seq[Global]): Unit =
    putSeq(globals)(putGlobal)
  private def putGlobalOpt(globalopt: Option[Global]): Unit =
    putOpt(globalopt)(putGlobal)
  private def putGlobal(global: Global): Unit = global match {
    case Global.None =>
      putTag(T.NoneGlobal)
    case Global.Top(id) =>
      putTag(T.TopGlobal)
      putUTF8String(id)
    case Global.Member(Global.Top(owner), sig) =>
      putTag(T.MemberGlobal)
      putUTF8String(owner)
      putSig(sig)
    case _ =>
      util.unreachable
  }

  private def putLocal(local: Local): Unit = putLeb(local.id)

  private def putNexts(nexts: Seq[Next]) = putSeq(nexts)(putNext)
  private def putNext(next: Next): Unit = next match {
    case Next.None         => putTag(T.NoneNext)
    case Next.Unwind(e, n) => putTag(T.UnwindNext); putParam(e); putNext(n)
    case Next.Case(v, n)   => putTag(T.CaseNext); putVal(v); putNext(n)
    case Next.Label(n, vs) => putTag(T.LabelNext); putLocal(n); putVals(vs)
  }

  private def putOp(op: Op) = op match {
    case Op.Call(ty, v, args) =>
      putTag(T.CallOp)
      putType(ty)
      putVal(v)
      putVals(args)

    case Op.Load(ty, ptr, syncAttrs) =>
      putTag(T.LoadOp)
      putType(ty)
      putVal(ptr)
      putOpt(syncAttrs)(putSyncAttrs(_))

    case Op.Store(ty, value, ptr, syncAttrs) =>
      putTag(T.StoreOp)
      putType(ty)
      putVal(value)
      putVal(ptr)
      putOpt(syncAttrs)(putSyncAttrs(_))

    case Op.Elem(ty, v, indexes) =>
      putTag(T.ElemOp)
      putType(ty)
      putVal(v)
      putVals(indexes)

    case Op.Extract(v, indexes) =>
      putTag(T.ExtractOp)
      putVal(v)
      putLebs(indexes)

    case Op.Insert(v, value, indexes) =>
      putTag(T.InsertOp)
      putVal(v)
      putVal(value)
      putLebs(indexes)

    case Op.Stackalloc(ty, n) =>
      putTag(T.StackallocOp)
      putType(ty)
      putVal(n)

    case Op.Bin(bin, ty, l, r) =>
      putTag(T.BinOp)
      putBin(bin)
      putType(ty)
      putVal(l)
      putVal(r)

    case Op.Comp(comp, ty, l, r) =>
      putTag(T.CompOp)
      putComp(comp)
      putType(ty)
      putVal(l)
      putVal(r)

    case Op.Conv(conv, ty, v) =>
      putTag(T.ConvOp)
      putConv(conv)
      putType(ty)
      putVal(v)

    case Op.Fence(syncAttrs) =>
      putTag(T.FenceOp)
      putSyncAttrs(syncAttrs)

    case Op.Classalloc(n) =>
      putTag(T.ClassallocOp)
      putGlobal(n)

    case Op.Fieldload(ty, obj, name) =>
      putTag(T.FieldloadOp)
      putType(ty)
      putVal(obj)
      putGlobal(name)

    case Op.Fieldstore(ty, obj, name, value) =>
      putTag(T.FieldstoreOp)
      putType(ty)
      putVal(obj)
      putGlobal(name)
      putVal(value)

    case Op.Field(v, name) =>
      putTag(T.FieldOp)
      putVal(v)
      putGlobal(name)

    case Op.Method(v, sig) =>
      putTag(T.MethodOp)
      putVal(v)
      putSig(sig)

    case Op.Dynmethod(obj, sig) =>
      putTag(T.DynmethodOp)
      putVal(obj)
      putSig(sig)

    case Op.Module(name) =>
      putTag(T.ModuleOp)
      putGlobal(name)

    case Op.As(ty, v) =>
      putTag(T.AsOp)
      putType(ty)
      putVal(v)

    case Op.Is(ty, v) =>
      putTag(T.IsOp)
      putType(ty)
      putVal(v)

    case Op.Copy(v) =>
      putTag(T.CopyOp)
      putVal(v)

    case Op.SizeOf(ty) =>
      putTag(T.SizeOfOp)
      putType(ty)

    case Op.AlignmentOf(ty) =>
      putTag(T.AlignmentOfOp)
      putType(ty)

    case Op.Box(ty, obj) =>
      putTag(T.BoxOp)
      putType(ty)
      putVal(obj)

    case Op.Unbox(ty, obj) =>
      putTag(T.UnboxOp)
      putType(ty)
      putVal(obj)

    case Op.Var(ty) =>
      putTag(T.VarOp)
      putType(ty)

    case Op.Varload(slot) =>
      putTag(T.VarloadOp)
      putVal(slot)

    case Op.Varstore(slot, value) =>
      putTag(T.VarstoreOp)
      putVal(slot)
      putVal(value)

    case Op.Arrayalloc(ty, init) =>
      putTag(T.ArrayallocOp)
      putType(ty)
      putVal(init)

    case Op.Arrayload(ty, arr, idx) =>
      putTag(T.ArrayloadOp)
      putType(ty)
      putVal(arr)
      putVal(idx)

    case Op.Arraystore(ty, arr, idx, value) =>
      putTag(T.ArraystoreOp)
      putType(ty)
      putVal(arr)
      putVal(idx)
      putVal(value)

    case Op.Arraylength(arr) =>
      putTag(T.ArraylengthOp)
      putVal(arr)
  }

  private def putParams(params: Seq[Val.Local]) = putSeq(params)(putParam)
  private def putParam(param: Val.Local) = {
    putLocal(param.name)
    putType(param.ty)
  }

  private def putTypes(tys: Seq[Type]): Unit = putSeq(tys)(putType)
  private def putType(ty: Type): Unit = ty match {
    case Type.None         => putTag(T.NoneType)
    case Type.Void         => putTag(T.VoidType)
    case Type.Vararg       => putTag(T.VarargType)
    case Type.Ptr          => putTag(T.PtrType)
    case Type.Bool         => putTag(T.BoolType)
    case Type.Char         => putTag(T.CharType)
    case Type.Byte         => putTag(T.ByteType)
    case Type.UByte        => putTag(T.UByteType)
    case Type.Short        => putTag(T.ShortType)
    case Type.UShort       => putTag(T.UShortType)
    case Type.Int          => putTag(T.IntType)
    case Type.UInt         => putTag(T.UIntType)
    case Type.Long         => putTag(T.LongType)
    case Type.ULong        => putTag(T.ULongType)
    case Type.Float        => putTag(T.FloatType)
    case Type.Double       => putTag(T.DoubleType)
    case Type.Array(ty, n) => putTag(T.ArrayType); putType(ty); putLeb(n)
    case Type.Function(args, ret) =>
      putTag(T.FunctionType); putTypes(args); putType(ret)
    case Type.Struct(n, tys) =>
      putTag(T.StructType); putGlobal(n); putTypes(tys)

    case Type.Unit      => putTag(T.UnitType)
    case Type.Nothing   => putTag(T.NothingType)
    case Type.Class(n)  => putTag(T.ClassType); putGlobal(n)
    case Type.Trait(n)  => putTag(T.TraitType); putGlobal(n)
    case Type.Module(n) => putTag(T.ModuleType); putGlobal(n)
  }

  private def putVals(values: Seq[Val]): Unit = putSeq(values)(putVal)
  private def putVal(value: Val): Unit = value match {
    case Val.None          => putTag(T.NoneVal)
    case Val.True          => putTag(T.TrueVal)
    case Val.False         => putTag(T.FalseVal)
    case Val.Null          => putTag(T.ZeroVal); putType(Type.Ptr)
    case Val.Zero(ty)      => putTag(T.ZeroVal); putType(ty)
    case Val.Undef(ty)     => putTag(T.UndefVal); putType(ty)
    case Val.Byte(v)       => putTag(T.ByteVal); put(v)
    case Val.Short(v)      => putTag(T.ShortVal); putShort(v)
    case Val.Int(v)        => putTag(T.IntVal); putLeb(v)
    case Val.Long(v)       => putTag(T.LongVal); putLong(v)
    case Val.Float(v)      => putTag(T.FloatVal); putFloat(v)
    case Val.Double(v)     => putTag(T.DoubleVal); putDouble(v)
    case Val.Struct(n, vs) => putTag(T.StructVal); putGlobal(n); putVals(vs)
    case Val.Array(ty, vs) => putTag(T.ArrayVal); putType(ty); putVals(vs)
    case Val.Chars(s)      => putTag(T.CharsVal); putString(s)
    case Val.Local(n, ty)  => putTag(T.LocalVal); putLocal(n); putType(ty)
    case Val.Global(n, ty) => putTag(T.GlobalVal); putGlobal(n); putType(ty)

    case Val.Unit     => putTag(T.UnitVal)
    case Val.Const(v) => putTag(T.ConstVal); putVal(v)
    case Val.String(v) =>
      putTag(T.StringVal)
      putTag(v.length)
      v.foreach(putChar(_))
    case Val.Virtual(v)   => putTag(T.VirtualVal); putLong(v)
    case Val.ClassOf(cls) => putTag(T.ClassOfVal); putGlobal(cls)
    case Val.Size(v)      => putTag(T.SizeVal); putLong(v)
  }

  def putSyncAttrs(value: SyncAttrs) = {
    putMemoryOrder(value.memoryOrder)
    putBool(value.isVolatile)
  }

  def putMemoryOrder(value: MemoryOrder): Unit = {
    val tag = value match {
      case MemoryOrder.Unordered => T.Unordered
      case MemoryOrder.Monotonic => T.MonotonicOrder
      case MemoryOrder.Acquire   => T.AcquireOrder
      case MemoryOrder.Release   => T.ReleaseOrder
      case MemoryOrder.AcqRel    => T.AcqRelOrder
      case MemoryOrder.SeqCst    => T.SeqCstOrder
    }
    putTag(tag)
  }

  private def putLinktimeCondition(cond: LinktimeCondition): Unit = cond match {
    case LinktimeCondition.SimpleCondition(propertyName, comparison, value) =>
      putTag(LinktimeCondition.Tag.SimpleCondition)
      putUTF8String(propertyName)
      putComp(comparison)
      putVal(value)
      putPosition(cond.position)

    case LinktimeCondition.ComplexCondition(op, left, right) =>
      putTag(LinktimeCondition.Tag.ComplexCondition)
      putBin(op)
      putLinktimeCondition(left)
      putLinktimeCondition(right)
      putPosition(cond.position)
  }

  // Ported from Scala.js
  def putPosition(pos: Position): Unit = {
    import PositionFormat._
    def writeFull(): Unit = {
      put(FormatFullMaskValue.toByte)
      putTag(fileIndexMap(pos.source))
      putTag(pos.line)
      putTag(pos.column)
    }

    if (pos == Position.NoPosition) {
      put(FormatNoPositionValue.toByte)
    } else if (lastPosition == Position.NoPosition ||
        pos.source != lastPosition.source) {
      writeFull()
      lastPosition = pos
    } else {
      val line = pos.line
      val column = pos.column
      val lineDiff = line - lastPosition.line
      val columnDiff = column - lastPosition.column
      val columnIsByte = column >= 0 && column < 256

      if (lineDiff == 0 && columnDiff >= -64 && columnDiff < 64) {
        put(((columnDiff << Format1Shift) | Format1MaskValue).toByte)
      } else if (lineDiff >= -32 && lineDiff < 32 && columnIsByte) {
        put(((lineDiff << Format2Shift) | Format2MaskValue).toByte)
        put(column.toByte)
      } else if (lineDiff >= Short.MinValue && lineDiff <= Short.MaxValue && columnIsByte) {
        put(Format3MaskValue.toByte)
        putShort(lineDiff.toShort)
        put(column.toByte)
      } else {
        writeFull()
      }

      lastPosition = pos
    }
  }

  private def initFiles(defns: Seq[Defn]): Seq[String] = {
    val filesList = mutable.UnrolledBuffer.empty[String]

    def initFile(pos: Position): Unit = {
      val file = pos.source
      if (pos.isDefined)
        fileIndexMap.getOrElseUpdate(
          file, {
            val idx = filesList.size
            filesList += file.toString
            idx
          }
        )
    }
    defns.foreach {
      case defn @ Defn.Define(_, _, _, insts) =>
        initFile(defn.pos)
        insts.foreach(inst => initFile(inst.pos))
      case defn => initFile(defn.pos)
    }
    filesList.toSeq
  }
}
