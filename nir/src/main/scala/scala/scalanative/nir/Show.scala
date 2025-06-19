package scala.scalanative
package nir

import java.nio.charset.StandardCharsets
import scala.collection.mutable
import scala.scalanative.util.ShowBuilder.InMemoryShowBuilder
import scalanative.util.{ShowBuilder, unreachable}
import nir.Defn.Define.DebugInfo

object Show {
  def newBuilder: NirShowBuilder = new NirShowBuilder(new InMemoryShowBuilder)
  def debug[T](msg: String)(f: => T): T = {
    val value = f
    println("$msg: " + value)
    value
  }

  def apply(v: Attr): String = { val b = newBuilder; b.onAttr(v); b.toString }
  def apply(v: Attrs): String = { val b = newBuilder; b.onAttrs(v); b.toString }
  def apply(v: Bin): String = { val b = newBuilder; b.onBin(v); b.toString }
  def apply(v: Comp): String = { val b = newBuilder; b.onComp(v); b.toString }
  def apply(v: Conv): String = { val b = newBuilder; b.onConv(v); b.toString }
  def apply(v: Defn): String = { val b = newBuilder; b.onDefn(v); b.toString }
  def apply(v: Global): String = {
    val b = newBuilder; b.onGlobal(v); b.toString
  }
  def apply(v: Sig): String = {
    val b = newBuilder; b.onSig(v); b.toString
  }
  def apply(v: Inst): String = { val b = newBuilder; b.show(v); b.toString }
  def apply(v: Local): String = { val b = newBuilder; b.show(v); b.toString }
  def apply(v: Next): String = { val b = newBuilder; b.show(v); b.toString }
  def apply(v: Op): String = { val b = newBuilder; b.show(v); b.toString }
  def apply(v: Type): String = { val b = newBuilder; b.onType(v); b.toString }
  def apply(v: Val): String = { val b = newBuilder; b.show(v); b.toString }
  def apply(v: nir.MemoryOrder): String = {
    val b = newBuilder; b.onMemoryOrder(v); b.toString
  }

  type DefnString = (Global, String)

  def dump(defns: Seq[Defn], fileName: String): Unit = {
    val pw = new java.io.PrintWriter(fileName)

    try {
      defns
        .filter(_ != null)
        .sortBy(_.name)
        .foreach { defn =>
          pw.write(defn.show)
          pw.write("\n")
        }
    } finally {
      pw.close()
    }
  }

  final class NirShowBuilder(val builder: ShowBuilder) extends AnyVal {
    import builder._

    def onAttrs(attrs: Attrs): Unit =
      if (attrs == Attrs.None) {
        ()
      } else {
        onAttrs(attrs.toSeq)
      }

    def onAttrs(attrs: Seq[Attr]): Unit = {
      rep(attrs, sep = " ")(onAttr)
      str(" ")
    }

    def onAttr(attr: Attr): Unit = attr match {
      case Attr.MayInline =>
        str("mayinline")
      case Attr.InlineHint =>
        str("inlinehint")
      case Attr.NoInline =>
        str("noinline")
      case Attr.AlwaysInline =>
        str("alwaysinline")
      case Attr.MaySpecialize =>
        str("mayspecialize")
      case Attr.NoSpecialize =>
        str("nospecialize")
      case Attr.UnOpt =>
        str("unopt")
      case Attr.NoOpt =>
        str("noopt")
      case Attr.DidOpt =>
        str("didopt")
      case Attr.BailOpt(msg) =>
        str("bailopt(\"")
        str(escapeQuotes(msg))
        str("\")")
      case Attr.Dyn =>
        str("dyn")
      case Attr.Stub =>
        str("stub")
      case Attr.Extern(isBlocking) =>
        str("extern")
        if (isBlocking) str(" blocking")
      case Attr.Link(name) =>
        str("link(\"")
        str(escapeQuotes(name))
        str("\")")
      case Attr.LinkCppRuntime =>
        str("linkCppRuntime")
      case Attr.Define(name) =>
        str("define(\"")
        str(escapeQuotes(name))
        str("\")")
      case Attr.Abstract =>
        str("abstract")
      case Attr.Volatile =>
        str("volatile")
      case Attr.Final =>
        str("final")
      case Attr.SafePublish => str("safe-publish")
      case Attr.LinktimeResolved =>
        str("linktime")
      case Attr.Alignment(size, group) =>
        str("align(")
        str(size)
        group.foreach { v =>
          str(", "); str(escapeQuotes(v))
        }
        str(")")
      case Attr.UsesIntrinsic =>
        str("uses-intrinsics")
    }
    def show(next: Next): Unit = onNext(next)(DebugInfo.empty)
    def onNext(next: Next)(implicit debugInfo: DebugInfo): Unit = next match {
      case Next.Label(name, Seq()) =>
        onLocal(name)
      case Next.Unwind(exc, next) =>
        str("unwind ")
        onVal(exc)
        str(" to ")
        onNext(next)
      case Next.Case(v, next) =>
        str("case ")
        onVal(v)
        str(" => ")
        onNext(next)
      case Next.Label(name, args) =>
        onLocal(name)
        str("(")
        rep(args, sep = ", ")(onVal)
        str(")")
      case Next.None => ()
    }

    def show(inst: Inst): Unit = this.onInst(inst)(DebugInfo.empty)
    def onInst(inst: Inst)(implicit debugInfo: DebugInfo): Unit = inst match {
      case Inst.Label(name, params) =>
        onLocal(name)
        if (params.isEmpty) {
          ()
        } else {
          str("(")
          rep(params, sep = ", ")(onVal)
          str(")")
        }
        str(":")
      case let @ Inst.Let(id, op, unwind) =>
        if (!let.scopeId.isTopLevel) {
          str(let.scopeId.id); str(": ")
        }
        onLocal(id)
        str(" = ")
        onOp(op)
        if (unwind ne Next.None) {
          str(" ")
          onNext(unwind)
        }
      case Inst.Ret(value) =>
        str("ret ")
        onVal(value)
      case Inst.Jump(next) =>
        str("jump ")
        onNext(next)
      case Inst.If(cond, thenp, elsep) =>
        str("if ")
        onVal(cond)
        str(" then ")
        onNext(thenp)
        str(" else ")
        onNext(elsep)
      case Inst.LinktimeIf(cond, thenp, elsep) =>
        str("linktime if ")
        linktimeCondition(cond)
        str(" then ")
        onNext(thenp)
        str(" else ")
        onNext(elsep)
      case Inst.Switch(scrut, default, cases) =>
        str("switch ")
        onVal(scrut)
        str(" {")
        rep(cases) { next =>
          str(" ")
          onNext(next)
        }
        str(" default => ")
        onNext(default)
        str(" }")
      case Inst.Throw(v, unwind) =>
        str("throw ")
        onVal(v)
        if (unwind ne Next.None) {
          str(" ")
          onNext(unwind)
        }
      case Inst.Unreachable(unwind) =>
        str("unreachable")
        if (unwind ne Next.None) {
          str(" ")
          onNext(unwind)
        }
    }

    def show(op: Op): Unit = onOp(op)(DebugInfo.empty)
    def onOp(op: Op)(implicit debugInfo: DebugInfo): Unit = op match {
      case Op.Call(ty, f, args) =>
        str("call[")
        onType(ty)
        str("] ")
        onVal(f)
        str("(")
        rep(args, sep = ", ")(onVal)
        str(")")
      case Op.Load(ty, ptr, memoryOrder) =>
        val isAtomic = memoryOrder.isDefined
        if (isAtomic) str("atomic ")
        str("load[")
        onType(ty)
        str("] ")
        onVal(ptr)
        memoryOrder.foreach {
          str(" ")
          onMemoryOrder(_)
        }
      case Op.Store(ty, ptr, value, memoryOrder) =>
        val isAtomic = memoryOrder.isDefined
        if (isAtomic) str("atomic ")
        str("store[")
        onType(ty)
        str("] ")
        onVal(ptr)
        str(", ")
        onVal(value)
        memoryOrder.foreach {
          str(" ")
          onMemoryOrder(_)
        }
      case Op.Elem(ty, ptr, indexes) =>
        str("elem[")
        onType(ty)
        str("] ")
        onVal(ptr)
        str(", ")
        rep(indexes, sep = ", ")(onVal)
      case Op.Extract(aggr, indexes) =>
        str("extract ")
        onVal(aggr)
        str(", ")
        rep(indexes, sep = ", ")(str)
      case Op.Insert(aggr, value, indexes) =>
        str("insert ")
        onVal(aggr)
        str(", ")
        onVal(value)
        str(", ")
        rep(indexes, sep = ", ")(str)
      case Op.Stackalloc(ty, n) =>
        str("stackalloc[")
        onType(ty)
        str("]")
        str(" ")
        onVal(n)
      case Op.Bin(bin, ty, l, r) =>
        onBin(bin)
        str("[")
        onType(ty)
        str("] ")
        onVal(l)
        str(", ")
        onVal(r)
      case Op.Comp(comp, ty, l, r) =>
        onComp(comp)
        str("[")
        onType(ty)
        str("] ")
        onVal(l)
        str(", ")
        onVal(r)
      case Op.Conv(conv, ty, v) =>
        onConv(conv)
        str("[")
        onType(ty)
        str("] ")
        onVal(v)
      case Op.Fence(memoryOrder) =>
        str("fence ")
        onMemoryOrder(memoryOrder)

      case Op.Classalloc(name, zone) =>
        str("classalloc ")
        onGlobal(name)
        zone.foreach { v =>
          str(" inZone ")
          onVal(v)
        }
      case Op.Fieldload(ty, obj, name) =>
        str("fieldload[")
        onType(ty)
        str("] ")
        onVal(obj)
        str(", ")
        onGlobal(name)
      case Op.Fieldstore(ty, obj, name, value) =>
        str("fieldstore[")
        onType(ty)
        str("] ")
        onVal(obj)
        str(", ")
        onGlobal(name)
        str(", ")
        onVal(value)
      case Op.Field(value, name) =>
        str("field ")
        onVal(value)
        str(", ")
        onGlobal(name)
      case Op.Method(value, sig) =>
        str("method ")
        onVal(value)
        str(", \"")
        str(escapeQuotes(sig.mangle))
        str("\"")
      case Op.Dynmethod(value, sig) =>
        str("dynmethod ")
        onVal(value)
        str(", \"")
        str(escapeQuotes(sig.mangle))
        str("\"")
      case Op.Module(name) =>
        str("module ")
        onGlobal(name)
      case Op.As(ty, v) =>
        str("as[")
        onType(ty)
        str("] ")
        onVal(v)
      case Op.Is(ty, v) =>
        str("is[")
        onType(ty)
        str("] ")
        onVal(v)
      case Op.Copy(value) =>
        str("copy ")
        onVal(value)
      case Op.SizeOf(ty) =>
        str("sizeOf[")
        onType(ty)
        str("] ")
      case Op.AlignmentOf(ty) =>
        str("alignmentOf[")
        onType(ty)
        str("] ")
      case Op.Box(ty, v) =>
        str("box[")
        onType(ty)
        str("] ")
        onVal(v)
      case Op.Unbox(ty, v) =>
        str("unbox[")
        onType(ty)
        str("] ")
        onVal(v)
      case Op.Var(ty) =>
        str("var[")
        onType(ty)
        str("]")
      case Op.Varload(slot) =>
        str("varload ")
        onVal(slot)
      case Op.Varstore(slot, value) =>
        str("varstore ")
        onVal(slot)
        str(", ")
        onVal(value)
      case Op.Arrayalloc(ty, init, zone) =>
        str("arrayalloc[")
        onType(ty)
        str("] ")
        onVal(init)
        zone.foreach { v =>
          str(" inZone ")
          onVal(v)
        }
      case Op.Arrayload(ty, arr, idx) =>
        str("arrayload[")
        onType(ty)
        str("] ")
        onVal(arr)
        str(", ")
        onVal(idx)
      case Op.Arraystore(ty, arr, idx, value) =>
        str("arraystore[")
        onType(ty)
        str("] ")
        onVal(arr)
        str(", ")
        onVal(idx)
        str(", ")
        onVal(value)
      case Op.Arraylength(arr) =>
        str("arraylength ")
        onVal(arr)
    }

    def onBin(bin: Bin): Unit = bin match {
      case Bin.Iadd => str("iadd")
      case Bin.Fadd => str("fadd")
      case Bin.Isub => str("isub")
      case Bin.Fsub => str("fsub")
      case Bin.Imul => str("imul")
      case Bin.Fmul => str("fmul")
      case Bin.Sdiv => str("sdiv")
      case Bin.Udiv => str("udiv")
      case Bin.Fdiv => str("fdiv")
      case Bin.Srem => str("srem")
      case Bin.Urem => str("urem")
      case Bin.Frem => str("frem")
      case Bin.Shl  => str("shl")
      case Bin.Lshr => str("lshr")
      case Bin.Ashr => str("ashr")
      case Bin.And  => str("and")
      case Bin.Or   => str("or")
      case Bin.Xor  => str("xor")
    }

    def onComp(comp: Comp): Unit = comp match {
      case Comp.Ieq => str("ieq")
      case Comp.Ine => str("ine")
      case Comp.Ugt => str("ugt")
      case Comp.Uge => str("uge")
      case Comp.Ult => str("ult")
      case Comp.Ule => str("ule")
      case Comp.Sgt => str("sgt")
      case Comp.Sge => str("sge")
      case Comp.Slt => str("slt")
      case Comp.Sle => str("sle")
      case Comp.Feq => str("feq")
      case Comp.Fne => str("fne")
      case Comp.Fgt => str("fgt")
      case Comp.Fge => str("fge")
      case Comp.Flt => str("flt")
      case Comp.Fle => str("fle")
    }

    def onConv(conv: Conv): Unit = conv match {
      case Conv.SSizeCast => str("ssizecast")
      case Conv.ZSizeCast => str("zsizecast")
      case Conv.Trunc     => str("trunc")
      case Conv.Zext      => str("zext")
      case Conv.Sext      => str("sext")
      case Conv.Fptrunc   => str("fptrunc")
      case Conv.Fpext     => str("fpext")
      case Conv.Fptoui    => str("fptoui")
      case Conv.Fptosi    => str("fptosi")
      case Conv.Uitofp    => str("uitofp")
      case Conv.Sitofp    => str("sitofp")
      case Conv.Ptrtoint  => str("ptrtoint")
      case Conv.Inttoptr  => str("inttoptr")
      case Conv.Bitcast   => str("bitcast")
    }

    def onMemoryOrder(v: MemoryOrder): Unit = v match {
      case MemoryOrder.Unordered => str("unordered")
      case MemoryOrder.Monotonic => str("monotonic")
      case MemoryOrder.Acquire   => str("acquire")
      case MemoryOrder.Release   => str("release")
      case MemoryOrder.AcqRel    => str("onAcqrel")
      case MemoryOrder.SeqCst    => str("onSeqcst")
    }

    def show(value: Val): Unit = onVal(value)(DebugInfo.empty)
    def onVal(value: Val)(implicit debugInfo: DebugInfo): Unit = value match {
      case Val.True =>
        str("true")
      case Val.False =>
        str("false")
      case Val.Null =>
        str("null")
      case Val.Zero(ty) =>
        str("zero[")
        onType(ty)
        str("]")
      case Val.Size(value) =>
        str("size ")
        str(value)
      case Val.Char(value) =>
        str("char ")
        str(value.toInt)
      case Val.Byte(value) =>
        str("byte ")
        str(value)
      case Val.Short(value) =>
        str("short ")
        str(value)
      case Val.Int(value) =>
        str("int ")
        str(value)
      case Val.Long(value) =>
        str("long ")
        str(value)
      case value: Val.Int128 =>
        str("int128 ")
        str(value.bigIntValue)
      case Val.Float(value) =>
        str("float ")
        str(value)
      case Val.Double(value) =>
        str("double ")
        str(value)
      case Val.StructValue(values) =>
        str("structvalue {")
        rep(values, sep = ", ")(onVal)
        str("}")
      case Val.ArrayValue(ty, values) =>
        str("arrayvalue ")
        onType(ty)
        str(" {")
        rep(values, sep = ", ")(onVal)
        str("}")
      case v: Val.ByteString =>
        str("c\"")
        val stringValue = new String(v.bytes, StandardCharsets.ISO_8859_1)
        str(escapeNewLine(escapeQuotes(stringValue)))
        str("\"")
      case Val.Local(id, ty) =>
        onLocal(id)
        str(" : ")
        onType(ty)
      case Val.Global(name, ty) =>
        onGlobal(name)
        str(" : ")
        onType(ty)
      case Val.Unit =>
        str("unit")
      case Val.Const(v) =>
        str("const ")
        onVal(v)
      case Val.String(v) =>
        str("\"")
        str(escapeNewLine(escapeQuotes(v)))
        str("\"")
      case Val.Virtual(key) =>
        str("virtual ")
        str(key)
      case Val.ClassOf(cls) =>
        str("classOf[")
        onGlobal(cls)
        str("]")
    }

    def onDefns(defns: Seq[Defn]): Unit =
      rep(defns) { defn =>
        newline()
        onDefn(defn)
      }

    def onDefn(defn: Defn): Unit = defn match {
      case Defn.Var(attrs, name, ty, v) =>
        onAttrs(attrs)
        str("var ")
        onGlobal(name)
        str(" : ")
        onType(ty)
        str(" = ")
        show(v)
      case Defn.Const(attrs, name, ty, v) =>
        onAttrs(attrs)
        str("const ")
        onGlobal(name)
        str(" : ")
        onType(ty)
        str(" = ")
        show(v)
      case Defn.Declare(attrs, name, ty) =>
        onAttrs(attrs)
        str("decl ")
        onGlobal(name)
        str(" : ")
        onType(ty)
      case Defn.Define(attrs, name, ty, insts, debugInfo) =>
        implicit val _debugInfo: Defn.Define.DebugInfo = debugInfo
        onAttrs(attrs)
        str("def ")
        onGlobal(name)
        str(" : ")
        onType(ty)
        str(" {")
        rep(insts) {
          case inst: Inst.Label =>
            newline()
            onInst(inst)
          case inst =>
            indent()
            newline()
            onInst(inst)
            unindent()
        }
        newline()
        str("}")
      case Defn.Trait(attrs, name, ifaces) =>
        onAttrs(attrs)
        str("trait ")
        onGlobal(name)
        if (ifaces.nonEmpty) {
          str(" : ")
          rep(ifaces, sep = ", ")(onGlobal)
        }
      case Defn.Class(attrs, name, parent, ifaces) =>
        val parents = parent ++: ifaces
        onAttrs(attrs)
        str("class ")
        onGlobal(name)
        if (parents.nonEmpty) {
          str(" : ")
          rep(parents, sep = ", ")(onGlobal)
        }
      case Defn.Module(attrs, name, parent, ifaces) =>
        val parents = parent ++: ifaces
        onAttrs(attrs)
        str("module ")
        onGlobal(name)
        if (parents.nonEmpty) {
          str(" : ")
          rep(parents, sep = ", ")(onGlobal)
        }
    }

    def onType(ty: Type): Unit = ty match {
      case Type.Vararg => str("...")
      case Type.Bool   => str("bool")
      case Type.Ptr    => str("ptr")
      case Type.Size   => str("size")
      case Type.Char   => str("char")
      case Type.Byte   => str("byte")
      case Type.Short  => str("short")
      case Type.Int    => str("int")
      case Type.Int128 => str("int128")
      case Type.Long   => str("long")
      case Type.Float  => str("float")
      case Type.Double => str("double")

      case Type.ArrayValue(ty, n) =>
        str("[")
        onType(ty)
        str(" x ")
        str(n)
        str("]")
      case Type.Function(args, ret) =>
        str("(")
        rep(args, sep = ", ")(onType)
        str(") => ")
        onType(ret)
      case Type.StructValue(tys) =>
        str("{")
        rep(tys, sep = ", ")(onType)
        str("}")

      case Type.Null    => str("null")
      case Type.Nothing => str("nothing")
      case Type.Virtual => str("virtual")
      case Type.Var(ty) => str("var["); onType(ty); str("]")
      case Type.Unit    => str("unit")
      case Type.Array(ty, nullable) =>
        if (!nullable) {
          str("?")
        }
        str("array[")
        onType(ty)
        str("]")
      case Type.Ref(name, exact, nullable) =>
        if (exact) {
          str("!")
        }
        if (!nullable) {
          str("?")
        }
        onGlobal(name)
    }

    def onGlobal(global: Global): Unit = global match {
      case Global.None =>
        unreachable
      case _ =>
        str("@\"")
        str(escapeQuotes(global.mangle))
        str("\"")
    }

    def onSig(sig: Sig): Unit =
      str(sig.mangle)

    def show(local: Local): Unit = onLocal(local)(DebugInfo.empty)
    def onLocal(local: Local)(implicit debugInfo: DebugInfo): Unit = {
      str("%")
      str(local.id)
      debugInfo.localNames.get(local).foreach { name =>
        str(" <"); str(name); str(">")
      }
    }

    def linktimeCondition(cond: LinktimeCondition): Unit = {
      import LinktimeCondition._
      cond match {
        case SimpleCondition(propertyName, comparison, value) =>
          str(propertyName + " ")
          onComp(comparison)
          str(" ")
          show(value)
        case ComplexCondition(op, left, right) =>
          linktimeCondition(left)
          str(" ")
          onBin(op)
          str(" ")
          linktimeCondition(right)
      }
    }

    private def escapeNewLine(s: String): String =
      """([^\\]|^)\n""".r.replaceAllIn(
        s,
        _.matched.toSeq match {
          case Seq(sngl)     => raw"\\n"
          case Seq('$', snd) => raw"\$$\\n"
          case Seq(fst, snd) => raw"\${fst}\\n"
        }
      )

    private def escapeQuotes(s: String): String = {
      val chars = s.toArray
      val out = mutable.UnrolledBuffer.empty[Char]
      var i = 0
      var escaped = false
      while (i < chars.length) {
        val char = chars(i)
        char match {
          case '"' =>
            if (!escaped) out += '\\'
            out += char
          case _ =>
            out += char
        }
        escaped = char == '\\'
        i += 1
      }
      new String(out.toArray)
    }

    override def toString: String = builder.toString
  }
}
