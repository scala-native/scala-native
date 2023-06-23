package scala.scalanative
package nir
package serialization

import java.nio.ByteBuffer
import java.nio.channels.WritableByteChannel
import scala.collection.mutable
import java.nio.charset.StandardCharsets
import serialization.{Tags => T}

// scalafmt: { maxColumn = 120}
final class BinarySerializer(channel: WritableByteChannel) {
  def serialize(defns: Seq[Defn]) = {
    // Write to in-memory buffers
    for (defn <- defns) {
      val offset = Defns.position()
      Defns.put(defn)
      Offsets.put(defn.name, offset)
    }
    // Mark the end of offsets
    Offsets.put(Global.None, -1)
    // Fill header info with buffer positions
    Header.put()
    // Write prepared data to final output channel
    sections.foreach(_.commit(channel))
  }

  private val sections = Seq(Header, Offsets, Strings, Positions, Globals, Types, Defns, Vals, Insts)
  private var hasEntryPoints: Boolean = false

  private object Header extends NIRSectionWriter(Prelude.length) {
    def put(): Unit = {
      putInt(Versions.magic)
      putInt(Versions.compat)
      putInt(Versions.revision)
      // All sections without Header (requires fixes size) and last section (Insts)
      sections.tail
        .foldLeft(Prelude.length) {
          case (sectionStart, section) =>
            putInt(sectionStart)
            sectionStart + section.position()
        }
      putBool(hasEntryPoints)
    }
  }

  private trait Common { self: NIRSectionWriter =>
    def putVal(value: Val): Unit = putLebUnsignedInt(Vals.intern(value))
    def putVals(values: Seq[Val]): Unit = putSeq(values)(putVal)
    def putLocal(local: Local): Unit = putLebUnsignedLong(local.id)
    def putGlobal(g: Global): Unit = putLebUnsignedInt(Globals.intern(g))
    def putGlobals(gs: Seq[Global]): Unit = putSeq(gs)(putGlobal)
    def putGlobalOpt(gopt: Option[Global]): Unit = putOpt(gopt)(putGlobal)
    def putSig(sig: Sig): Unit = putString(sig.mangle)
    def putType(ty: Type): Unit = putLebUnsignedInt(Types.intern(ty))
    def putTypes(tys: Seq[Type]): Unit = putSeq(tys)(putType)
    def putString(s: String): Unit = putLebUnsignedInt(Strings.intern(s))
    def putPosition(pos: Position): Unit = putLebUnsignedInt(Positions.intern(pos))
  }

  private object Offsets extends NIRSectionWriter with Common {
    def put(global: Global, defnOffset: Int): Unit = {
      putGlobal(global)
      putLebSignedInt(defnOffset) // signed due to -1 in the last offset
    }
  }

  private object Strings extends InternedBinarySectionWriter[String] with Common {
    override def internDeps(v: String): Unit = ()
    override def put(v: String) = {
      putSeq(v)(putLebChar)
    }
  }

  private object Positions extends InternedBinarySectionWriter[Position] with Common {
    override def internDeps(pos: Position): Unit = ()

    override def put(pos: Position): Unit = {
        putString(pos.source.toString) // interned
        putLebUnsignedInt(pos.line)
        putLebUnsignedInt(pos.column)
// 23 748 455 // baseline
// 24 018 968 // always-write full
// 23 973 474 // always write-full no tag 
// 23 909 912
    }
  }

  private object Globals extends InternedBinarySectionWriter[Global] with Common {
    override def internDeps(value: Global): Unit = value match {
      case Global.Member(n, _) => intern(n)
      case _                   => ()
    }

    override def put(value: Global): Unit = value match {
      case Global.Member(n, sig) => putTag(T.MemberGlobal); putGlobal(n); putSig(sig)
      case Global.Top(id)        => putTag(T.TopGlobal); putString(id)
      case Global.None           => putTag(T.NoneGlobal)
    }

  }

  private object Types extends InternedBinarySectionWriter[Type] with Common {
    override def internDeps(ty: Type): Unit = ty match {
      case Type.Function(tys, ty) => tys.foreach(intern); intern(ty)
      case Type.Array(ty, _)      => intern(ty)
      case Type.StructValue(tys)  => tys.foreach(intern)
      case Type.ArrayValue(ty, _) => intern(ty)
      case Type.Var(ty)           => intern(ty)
      case _                      => ()
    }

    override def put(ty: Type): Unit = ty match {
      case Type.Function(args, ret)     => putTag(T.FunctionType); putTypes(args); putType(ret)
      case Type.Ref(n, exact, nullable) => putTag(T.RefType); putGlobal(n); putBool(exact); putBool(nullable)
      case Type.Ptr                     => putTag(T.PtrType)
      case Type.Unit                    => putTag(T.UnitType)
      case Type.Array(ty, nullable)     => putTag(T.ArrayType); putType(ty); putBool(nullable)
      case Type.Bool                    => putTag(T.BoolType)
      case Type.Char                    => putTag(T.CharType)
      case Type.Byte                    => putTag(T.ByteType)
      case Type.Short                   => putTag(T.ShortType)
      case Type.Int                     => putTag(T.IntType)
      case Type.Long                    => putTag(T.LongType)
      case Type.Float                   => putTag(T.FloatType)
      case Type.Double                  => putTag(T.DoubleType)
      case Type.Size                    => putTag(T.SizeType)
      case Type.Null                    => putTag(T.NullType)
      case Type.Nothing                 => putTag(T.NothingType)
      case Type.ArrayValue(ty, n)       => putTag(T.ArrayValueType); putType(ty); putLebUnsignedInt(n)
      case Type.StructValue(tys)        => putTag(T.StructValueType); putTypes(tys)
      case Type.Vararg                  => putTag(T.VarargType)
      case Type.Var(ty)                 => putTag(T.VarType); putType(ty)
      case Type.Virtual                 => putTag(T.VirtualType)
    }
  }

  private object Vals extends InternedBinarySectionWriter[Val] with Common {
    override def internDeps(value: Val): Unit = value match {
      case Val.Const(v)          => intern(v)
      case Val.ArrayValue(_, vs) => vs.foreach(intern)
      case Val.StructValue(vs)   => vs.foreach(intern)
      case _                     => ()
    }

    override def put(value: Val): Unit = value match {
      case Val.Local(n, ty)       => putTag(T.LocalVal); putLocal(n); putType(ty)
      case Val.Global(n, ty)      => putTag(T.GlobalVal); putGlobal(n); putType(ty)
      case Val.Unit               => putTag(T.UnitVal)
      case Val.Null               => putTag(T.NullVal)
      case Val.True               => putTag(T.TrueVal)
      case Val.False              => putTag(T.FalseVal)
      case Val.Byte(v)            => putTag(T.ByteVal); put(v)
      case Val.Char(v)            => putTag(T.CharVal); putLebChar(v)
      case Val.Short(v)           => putTag(T.ShortVal); putLebSignedInt(v)
      case Val.Int(v)             => putTag(T.IntVal); putLebSignedInt(v)
      case Val.Long(v)            => putTag(T.LongVal); putLebSignedLong(v)
      case Val.Float(v)           => putTag(T.FloatVal); putFloat(v)
      case Val.Double(v)          => putTag(T.DoubleVal); putDouble(v)
      case Val.String(v)          => putTag(T.StringVal); putString(v)
      case Val.ByteString(v)      => putTag(T.ByteStringVal); putLebUnsignedInt(v.length); put(v)
      case Val.Const(v)           => putTag(T.ConstVal); putVal(v)
      case Val.Size(v)            => putTag(T.SizeVal); putLebUnsignedLong(v)
      case Val.ClassOf(cls)       => putTag(T.ClassOfVal); putGlobal(cls)
      case Val.Zero(ty)           => putTag(T.ZeroVal); putType(ty)
      case Val.ArrayValue(ty, vs) => putTag(T.ArrayValueVal); putType(ty); putVals(vs)
      case Val.StructValue(vs)    => putTag(T.StructValueVal); putVals(vs)
      case Val.Virtual(v)         => putTag(T.VirtualVal); putLebUnsignedLong(v)
    }
  }

  private object Defns extends NIRSectionWriter with Common {
    private def putAttrs(attrs: Attrs) =
      putSeq(attrs.toSeq)(putAttr)

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
      case Attr.BailOpt(msg) => putTag(T.BailOptAttr); putString(msg)

      case Attr.Dyn                => putTag(T.DynAttr)
      case Attr.Stub               => putTag(T.StubAttr)
      case Attr.Extern(isBlocking) => putTag(T.ExternAttr); putBool(isBlocking)
      case Attr.Link(s)            => putTag(T.LinkAttr); putString(s)
      case Attr.Abstract           => putTag(T.AbstractAttr)
      case Attr.Volatile           => putTag(T.VolatileAttr)
      case Attr.Final              => putTag(T.FinalAttr)

      case Attr.LinktimeResolved => putTag(T.LinktimeResolvedAttr)
    }

    private def putInsts(insts: Seq[Inst]): Unit = {
      putLebUnsignedInt(Insts.position())
      Insts.put(insts)
    }

    def put(defn: Defn): Unit = {
      def putHeader(tag: Byte): Unit = {
        putTag(tag)
        putGlobal(defn.name)
        putAttrs(defn.attrs)
        putPosition(defn.pos)
      }

      hasEntryPoints ||= defn.isEntryPoint
      defn match {
        case defn: Defn.Define =>
          putHeader(T.DefineDefn)
          putType(defn.ty)
          putInsts(defn.insts)

        case defn: Defn.Var =>
          putHeader(T.VarDefn)
          putType(defn.ty)
          putVal(defn.rhs)

        case defn: Defn.Class =>
          putHeader(T.ClassDefn)
          putGlobalOpt(defn.parent)
          putGlobals(defn.traits)

        case defn: Defn.Trait =>
          putHeader(T.TraitDefn)
          putGlobals(defn.traits)

        case defn: Defn.Module =>
          putHeader(T.ModuleDefn)
          putGlobalOpt(defn.parent)
          putGlobals(defn.traits)

        case defn: Defn.Declare =>
          putHeader(T.DeclareDefn)
          putType(defn.ty)

        case defn: Defn.Const =>
          putHeader(T.ConstDefn)
          putType(defn.ty)
          putVal(defn.rhs)
      }
    }
  }

  private object Insts extends NIRSectionWriter with Common {
    private def putBin(bin: Bin) = bin match {
      case Bin.Iadd => putTag(T.IaddBin)
      case Bin.Isub => putTag(T.IsubBin)
      case Bin.Xor  => putTag(T.XorBin)
      case Bin.And  => putTag(T.AndBin)
      case Bin.Or   => putTag(T.OrBin)
      case Bin.Fadd => putTag(T.FaddBin)
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
      case Conv.Bitcast   => putTag(T.BitcastConv)
      case Conv.SSizeCast => putTag(T.SSizeCastConv)
      case Conv.ZSizeCast => putTag(T.ZSizeCastConv)
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
    }

    private def putNexts(nexts: Seq[Next]) =
      putSeq(nexts)(putNext)

    private def putNext(next: Next): Unit = next match {
      case Next.Label(n, vs) => putTag(T.LabelNext); putLocal(n); putVals(vs)
      case Next.Unwind(e, n) => putTag(T.UnwindNext); putParam(e); putNext(n)
      case Next.Case(v, n)   => putTag(T.CaseNext); putVal(v); putNext(n)
      case Next.None         => putTag(T.NoneNext)
    }

    private def putOptSyncAttrs(attrs: Option[SyncAttrs]): Unit = putOpt(attrs)(putSyncAttrs(_))
    private def putSyncAttrs(value: SyncAttrs) = {
      putMemoryOrder(value.memoryOrder)
      putBool(value.isVolatile)
    }

    private def putMemoryOrder(value: MemoryOrder): Unit = value match {
      case MemoryOrder.Unordered => putTag(T.Unordered)
      case MemoryOrder.Monotonic => putTag(T.MonotonicOrder)
      case MemoryOrder.Acquire   => putTag(T.AcquireOrder)
      case MemoryOrder.Release   => putTag(T.ReleaseOrder)
      case MemoryOrder.AcqRel    => putTag(T.AcqRelOrder)
      case MemoryOrder.SeqCst    => putTag(T.SeqCstOrder)
    }

    private def putLinktimeCondition(cond: LinktimeCondition): Unit = cond match {
      case LinktimeCondition.SimpleCondition(propertyName, comparison, value) =>
        putTag(LinktimeCondition.Tag.SimpleCondition)
        putString(propertyName)
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

    private def putOp(op: Op) = op match {
      case Op.Call(ty, v, args) =>
        putTag(T.CallOp);
        putType(ty);
        putVal(v);
        putVals(args);

      case Op.Module(name) =>
        putTag(T.ModuleOp)
        putGlobal(name)

      case Op.Classalloc(n, None) =>
        putTag(T.ClassallocOp)
        putGlobal(n)

      case Op.Classalloc(n, Some(zone)) =>
        putTag(T.ClassallocZoneOp)
        putGlobal(n)
        putVal(zone)

      case Op.Field(v, name) =>
        putTag(T.FieldOp)
        putVal(v)
        putGlobal(name)

      case Op.Method(v, sig) =>
        putTag(T.MethodOp)
        putVal(v)
        putSig(sig)

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

      case Op.Bin(bin, ty, l, r) =>
        putTag(T.BinOp)
        putBin(bin)
        putType(ty)
        putVal(l)
        putVal(r)

      case Op.Load(ty, ptr, None) =>
        putTag(T.LoadOp)
        putType(ty)
        putVal(ptr)

      case Op.Load(ty, ptr, Some(syncAttrs)) =>
        putTag(T.LoadSyncOp)
        putType(ty)
        putVal(ptr)
        putSyncAttrs(syncAttrs)

      case Op.Store(ty, value, ptr, None) =>
        putTag(T.StoreOp)
        putType(ty)
        putVal(value)
        putVal(ptr)

      case Op.Store(ty, value, ptr, Some(syncAttrs)) =>
        putTag(T.StoreOp)
        putType(ty)
        putVal(value)
        putVal(ptr)
        putSyncAttrs(syncAttrs)

      case Op.Box(ty, obj) =>
        putTag(T.BoxOp)
        putType(ty)
        putVal(obj)

      case Op.Unbox(ty, obj) =>
        putTag(T.UnboxOp)
        putType(ty)
        putVal(obj)

      case Op.Elem(ty, v, indexes) =>
        putTag(T.ElemOp)
        putType(ty)
        putVal(v)
        putVals(indexes)

      case Op.Extract(v, indexes) =>
        putTag(T.ExtractOp)
        putVal(v)
        putSeq(indexes)(putLebSignedInt)

      case Op.Insert(v, value, indexes) =>
        putTag(T.InsertOp)
        putVal(v)
        putVal(value)
        putSeq(indexes)(putLebSignedInt)

      case Op.Copy(v) =>
        putTag(T.CopyOp)
        putVal(v)

      case Op.Stackalloc(ty, n) =>
        putTag(T.StackallocOp)
        putType(ty)
        putVal(n)

      case Op.Arrayalloc(ty, init, None) =>
        putTag(T.ArrayallocOp)
        putType(ty)
        putVal(init)

      case Op.Arrayalloc(ty, init, Some(zone)) =>
        putTag(T.ArrayallocZoneOp)
        putType(ty)
        putVal(init)
        putVal(zone)

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

      case Op.Dynmethod(obj, sig) =>
        putTag(T.DynmethodOp)
        putVal(obj)
        putSig(sig)

      case Op.As(ty, v) =>
        putTag(T.AsOp)
        putType(ty)
        putVal(v)

      case Op.Is(ty, v) =>
        putTag(T.IsOp)
        putType(ty)
        putVal(v)

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

      case Op.SizeOf(ty) =>
        putTag(T.SizeOfOp)
        putType(ty)

      case Op.AlignmentOf(ty) =>
        putTag(T.AlignmentOfOp)
        putType(ty)

      case Op.Fence(syncAttrs) =>
        putTag(T.FenceOp)
        putSyncAttrs(syncAttrs)
    }

    private def putParams(params: Seq[Val.Local]) = putSeq(params)(putParam)
    private def putParam(param: Val.Local) = {
      putLebUnsignedLong(param.name.id)
      putType(param.ty)
    }

    private def putInst(cf: Inst) = {
      def putTagAndPosition(tag: Byte) = {
        putTag(tag)
        putPosition(cf.pos)
      }
      cf match {
        case Inst.Label(name, params) =>
          putTagAndPosition(T.LabelInst)
          putLocal(name)
          putParams(params)

        case Inst.Let(name, op, Next.None) =>
          putTagAndPosition(T.LetInst)
          putLocal(name)
          putOp(op)

        case Inst.Let(name, op, unwind) =>
          putTagAndPosition(T.LetUnwindInst)
          putLocal(name)
          putOp(op)
          putNext(unwind)

        case Inst.Ret(v) =>
          putTagAndPosition(T.RetInst)
          putVal(v)

        case Inst.Jump(next) =>
          putTagAndPosition(T.JumpInst)
          putNext(next)

        case Inst.If(v, thenp, elsep) =>
          putTagAndPosition(T.IfInst)
          putVal(v)
          putNext(thenp)
          putNext(elsep)

        case Inst.LinktimeIf(v, thenp, elsep) =>
          putTagAndPosition(T.LinktimeIfInst)
          putLinktimeCondition(v)
          putNext(thenp)
          putNext(elsep)

        case Inst.Switch(v, default, cases) =>
          putTagAndPosition(T.SwitchInst)
          putVal(v)
          putNext(default)
          putNexts(cases)

        case Inst.Throw(v, unwind) =>
          putTagAndPosition(T.ThrowInst)
          putVal(v)
          putNext(unwind)

        case Inst.Unreachable(unwind) =>
          putTagAndPosition(T.UnreachableInst)
          putNext(unwind)
      }

    }

    def put(insts: Seq[Inst]) = putSeq(insts)(putInst)
  }

}

sealed abstract class NIRSectionWriter(init: Int = 1024 * 1024) {
  protected val buffer = ByteBuffer.allocate(init)

  def position(): Int = buffer.position()
  def put(values: Array[Byte]): Unit = buffer.put(values)
  def put(value: Byte): Unit = buffer.put(value)
  def putShort(value: Short): Unit = buffer.putShort(value)
  def putInt(value: Int): Unit = buffer.putInt(value)
  def putFloat(value: Float): Unit = buffer.putFloat(value)
  def putDouble(value: Double): Unit = buffer.putDouble(value)
  def putBool(v: Boolean) = put((if (v) 1 else 0).toByte)
  // Leb128 encoders
  def putLebShort(value: Short): Unit = putLebSignedInt(value)
  def putLebChar(value: Char): Unit = putLebUnsignedInt(value)
  def putLebUnsignedInt(v: Int): Unit = {
    require(v >= 0, s"Unsigned integer expected, got $v")
    var remaining = v
    while ({
      val byte = (remaining & 0x7f).toByte
      remaining >>= 7
      val hasMore = remaining != 0
      buffer.put(if (hasMore) (byte | 0x80).toByte else byte)
      hasMore
    }) ()
  }
  def putLebUnsignedLong(v: Long): Unit = {
    require(v >= 0L, s"Unsigned integer expected, got $v")
    var remaining = v
    while ({
      val byte = (remaining & 0x7f).toByte
      remaining >>= 7
      val hasMore = remaining != 0
      buffer.put(if (hasMore) (byte | 0x80).toByte else byte)
      hasMore
    }) ()
  }
  def putLebSignedInt(v: Int): Unit = {
    var value = v
    var remaining = value >> 7
    var hasMore = true
    val end = if ((value & java.lang.Integer.MIN_VALUE) == 0) 0 else -1
    while (hasMore) {
      hasMore = (remaining != end) || ((remaining & 1) != ((value >> 6) & 1))
      buffer.put(((value & 0x7f) | (if (hasMore) 0x80 else 0)).toByte)
      value = remaining
      remaining >>= 7
    }
  }
  def putLebSignedLong(v: Long): Unit = {
    var value = v
    var remaining = value >> 7
    var hasMore = true
    val end = if ((value & java.lang.Long.MIN_VALUE) == 0) 0L else -1L
    while (hasMore) {
      hasMore = (remaining != end) || ((remaining & 1) != ((value >> 6) & 1))
      buffer.put(((value & 0x7f) | (if (hasMore) 0x80 else 0)).toByte)
      value = remaining
      remaining >>= 7
    }
  }

  def putSeq[T](seq: Seq[T])(putT: T => Unit): Unit = {
    putLebUnsignedInt(seq.length)
    seq.foreach(putT)
  }
  def putOpt[T](opt: Option[T])(putT: T => Unit): Unit = opt match {
    case None    => put(0.toByte)
    case Some(t) => put(1.toByte); putT(t)
  }
  def putTag(value: Byte): Unit = put(value)

  def commit(channel: WritableByteChannel): Unit = {
    buffer.flip()
    channel.write(buffer)
  }
}

sealed abstract class InternedBinarySectionWriter[T] extends NIRSectionWriter {
  protected val entries = mutable.Map.empty[T, Int]
  def put(value: T): Unit
  def internDeps(value: T): Unit
  def intern(value: T): Int =
    entries
      .get(value)
      .getOrElse {
        internDeps(value)
        val offset = position()
        entries(value) = offset
        put(value)
        offset
      }
}
