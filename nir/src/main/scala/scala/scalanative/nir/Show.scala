package scala.scalanative
package nir

import java.nio.charset.StandardCharsets
import scala.collection.mutable
import scala.scalanative.util.ShowBuilder.InMemoryShowBuilder
import scalanative.util.{ShowBuilder, unreachable}

object Show {
  def newBuilder: NirShowBuilder = new NirShowBuilder(new InMemoryShowBuilder)
  def debug[T](msg: String)(f: => T): T = {
    val value = f
    println("$msg: " + value)
    value
  }

  def apply(v: Attr): String = { val b = newBuilder; b.attr_(v); b.toString }
  def apply(v: Attrs): String = { val b = newBuilder; b.attrs_(v); b.toString }
  def apply(v: Bin): String = { val b = newBuilder; b.bin_(v); b.toString }
  def apply(v: Comp): String = { val b = newBuilder; b.comp_(v); b.toString }
  def apply(v: Conv): String = { val b = newBuilder; b.conv_(v); b.toString }
  def apply(v: Defn): String = { val b = newBuilder; b.defn_(v); b.toString }
  def apply(v: Global): String = {
    val b = newBuilder; b.global_(v); b.toString
  }
  def apply(v: Sig): String = {
    val b = newBuilder; b.sig_(v); b.toString
  }
  def apply(v: Inst): String = { val b = newBuilder; b.inst_(v); b.toString }
  def apply(v: Local): String = { val b = newBuilder; b.local_(v); b.toString }
  def apply(v: Next): String = { val b = newBuilder; b.next_(v); b.toString }
  def apply(v: Op): String = { val b = newBuilder; b.op_(v); b.toString }
  def apply(v: Type): String = { val b = newBuilder; b.type_(v); b.toString }
  def apply(v: Val): String = { val b = newBuilder; b.val_(v); b.toString }
  def apply(v: nir.MemoryOrder): String = {
    val b = newBuilder; b.memoryOrder_(v); b.toString
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

    def attrs_(attrs: Attrs): Unit =
      if (attrs == Attrs.None) {
        ()
      } else {
        attrs_(attrs.toSeq)
      }

    def attrs_(attrs: Seq[Attr]): Unit = {
      rep(attrs, sep = " ")(attr_)
      str(" ")
    }

    def attr_(attr: Attr): Unit = attr match {
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
      case Attr.Abstract =>
        str("abstract")
      case Attr.Volatile =>
        str("volatile")
      case Attr.Final =>
        str("final")
      case Attr.LinktimeResolved =>
        str("linktime")
    }

    def next_(next: Next): Unit = next match {
      case Next.Label(name, Seq()) =>
        local_(name)
      case Next.Unwind(exc, next) =>
        str("unwind ")
        val_(exc)
        str(" to ")
        next_(next)
      case Next.Case(v, next) =>
        str("case ")
        val_(v)
        str(" => ")
        next_(next)
      case Next.Label(name, args) =>
        local_(name)
        str("(")
        rep(args, sep = ", ")(val_)
        str(")")
      case Next.None => ()
    }

    def inst_(inst: Inst): Unit = inst match {
      case Inst.Label(name, params) =>
        local_(name)
        if (params.isEmpty) {
          ()
        } else {
          str("(")
          rep(params, sep = ", ") {
            case Val.Local(n, ty) =>
              local_(n)
              str(" : ")
              type_(ty)
          }
          str(")")
        }
        str(":")
      case Inst.Let(name, op, unwind) =>
        local_(name)
        str(" = ")
        op_(op)
        if (unwind ne Next.None) {
          str(" ")
          next_(unwind)
        }
      case Inst.Ret(value) =>
        str("ret ")
        val_(value)
      case Inst.Jump(next) =>
        str("jump ")
        next_(next)
      case Inst.If(cond, thenp, elsep) =>
        str("if ")
        val_(cond)
        str(" then ")
        next_(thenp)
        str(" else ")
        next_(elsep)
      case Inst.LinktimeIf(cond, thenp, elsep) =>
        str("linktime if ")
        linktimeCondition(cond)
        str(" then ")
        next_(thenp)
        str(" else ")
        next_(elsep)
      case Inst.Switch(scrut, default, cases) =>
        str("switch ")
        val_(scrut)
        str(" {")
        rep(cases) { next =>
          str(" ")
          next_(next)
        }
        str(" default => ")
        next_(default)
        str(" }")
      case Inst.Throw(v, unwind) =>
        str("throw ")
        val_(v)
        if (unwind ne Next.None) {
          str(" ")
          next_(unwind)
        }
      case Inst.Unreachable(unwind) =>
        str("unreachable")
        if (unwind ne Next.None) {
          str(" ")
          next_(unwind)
        }
    }

    def op_(op: Op): Unit = op match {
      case Op.Call(ty, f, args) =>
        str("call[")
        type_(ty)
        str("] ")
        val_(f)
        str("(")
        rep(args, sep = ", ")(val_)
        str(")")
      case Op.Load(ty, ptr, syncAttrs) =>
        val isAtomic = syncAttrs.isDefined
        if (isAtomic) str("atomic ")
        str("load[")
        type_(ty)
        str("] ")
        val_(ptr)
        syncAttrs.foreach {
          str(" ")
          syncAttrs_(_)
        }
      case Op.Store(ty, ptr, value, syncAttrs) =>
        val isAtomic = syncAttrs.isDefined
        if (isAtomic) str("atomic ")
        str("store[")
        type_(ty)
        str("] ")
        val_(ptr)
        str(", ")
        val_(value)
        syncAttrs.foreach {
          str(" ")
          syncAttrs_(_)
        }
      case Op.Elem(ty, ptr, indexes) =>
        str("elem[")
        type_(ty)
        str("] ")
        val_(ptr)
        str(", ")
        rep(indexes, sep = ", ")(val_)
      case Op.Extract(aggr, indexes) =>
        str("extract ")
        val_(aggr)
        str(", ")
        rep(indexes, sep = ", ")(str)
      case Op.Insert(aggr, value, indexes) =>
        str("insert ")
        val_(aggr)
        str(", ")
        val_(value)
        str(", ")
        rep(indexes, sep = ", ")(str)
      case Op.Stackalloc(ty, n) =>
        str("stackalloc[")
        type_(ty)
        str("]")
        str(" ")
        val_(n)
      case Op.Bin(bin, ty, l, r) =>
        bin_(bin)
        str("[")
        type_(ty)
        str("] ")
        val_(l)
        str(", ")
        val_(r)
      case Op.Comp(comp, ty, l, r) =>
        comp_(comp)
        str("[")
        type_(ty)
        str("] ")
        val_(l)
        str(", ")
        val_(r)
      case Op.Conv(conv, ty, v) =>
        conv_(conv)
        str("[")
        type_(ty)
        str("] ")
        val_(v)
      case Op.Fence(syncAttrs) =>
        str("fence ")
        syncAttrs_(syncAttrs)

      case Op.Classalloc(name) =>
        str("classalloc ")
        global_(name)
      case Op.Fieldload(ty, obj, name) =>
        str("fieldload[")
        type_(ty)
        str("] ")
        val_(obj)
        str(", ")
        global_(name)
      case Op.Fieldstore(ty, obj, name, value) =>
        str("fieldstore[")
        type_(ty)
        str("] ")
        val_(obj)
        str(", ")
        global_(name)
        str(", ")
        val_(value)
      case Op.Field(value, name) =>
        str("field ")
        val_(value)
        str(", ")
        global_(name)
      case Op.Method(value, sig) =>
        str("method ")
        val_(value)
        str(", \"")
        str(escapeQuotes(sig.mangle))
        str("\"")
      case Op.Dynmethod(value, sig) =>
        str("dynmethod ")
        val_(value)
        str(", \"")
        str(escapeQuotes(sig.mangle))
        str("\"")
      case Op.Module(name) =>
        str("module ")
        global_(name)
      case Op.As(ty, v) =>
        str("as[")
        type_(ty)
        str("] ")
        val_(v)
      case Op.Is(ty, v) =>
        str("is[")
        type_(ty)
        str("] ")
        val_(v)
      case Op.Copy(value) =>
        str("copy ")
        val_(value)
      case Op.SizeOf(ty) =>
        str("sizeOf[")
        type_(ty)
        str("] ")
      case Op.AlignmentOf(ty) =>
        str("alignmentOf[")
        type_(ty)
        str("] ")
      case Op.Box(ty, v) =>
        str("box[")
        type_(ty)
        str("] ")
        val_(v)
      case Op.Unbox(ty, v) =>
        str("unbox[")
        type_(ty)
        str("] ")
        val_(v)
      case Op.Var(ty) =>
        str("var[")
        type_(ty)
        str("]")
      case Op.Varload(slot) =>
        str("varload ")
        val_(slot)
      case Op.Varstore(slot, value) =>
        str("varstore ")
        val_(slot)
        str(", ")
        val_(value)
      case Op.Arrayalloc(ty, init) =>
        str("arrayalloc[")
        type_(ty)
        str("] ")
        val_(init)
      case Op.Arrayload(ty, arr, idx) =>
        str("arrayload[")
        type_(ty)
        str("] ")
        val_(arr)
        str(", ")
        val_(idx)
      case Op.Arraystore(ty, arr, idx, value) =>
        str("arraystore[")
        type_(ty)
        str("] ")
        val_(arr)
        str(", ")
        val_(idx)
        str(", ")
        val_(value)
      case Op.Arraylength(arr) =>
        str("arraylength ")
        val_(arr)
    }

    def bin_(bin: Bin): Unit = bin match {
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

    def comp_(comp: Comp): Unit = comp match {
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

    def conv_(conv: Conv): Unit = conv match {
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

    def memoryOrder_(v: MemoryOrder): Unit = v match {
      case MemoryOrder.Unordered => str("unordered")
      case MemoryOrder.Monotonic => str("monotonic")
      case MemoryOrder.Acquire   => str("acquire")
      case MemoryOrder.Release   => str("release")
      case MemoryOrder.AcqRel    => str("acq_rel")
      case MemoryOrder.SeqCst    => str("seq_cst")
    }

    def val_(value: Val): Unit = value match {
      case Val.True =>
        str("true")
      case Val.False =>
        str("false")
      case Val.Null =>
        str("null")
      case Val.Zero(ty) =>
        str("zero[")
        type_(ty)
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
      case Val.Float(value) =>
        str("float ")
        str(value)
      case Val.Double(value) =>
        str("double ")
        str(value)
      case Val.StructValue(values) =>
        str("structvalue {")
        rep(values, sep = ", ")(val_)
        str("}")
      case Val.ArrayValue(ty, values) =>
        str("arrayvalue ")
        type_(ty)
        str(" {")
        rep(values, sep = ", ")(val_)
        str("}")
      case v: Val.Chars =>
        str("c\"")
        val stringValue =
          new java.lang.String(v.bytes, StandardCharsets.ISO_8859_1)
        str(escapeNewLine(escapeQuotes(stringValue)))
        str("\"")
      case Val.Local(name, ty) =>
        local_(name)
        str(" : ")
        type_(ty)
      case Val.Global(name, ty) =>
        global_(name)
        str(" : ")
        type_(ty)
      case Val.Unit =>
        str("unit")
      case Val.Const(v) =>
        str("const ")
        val_(v)
      case Val.String(v) =>
        str("\"")
        str(escapeNewLine(escapeQuotes(v)))
        str("\"")
      case Val.Virtual(key) =>
        str("virtual ")
        str(key)
      case Val.ClassOf(cls) =>
        str("classOf[")
        global_(cls)
        str("]")
    }

    def defns_(defns: Seq[Defn]): Unit =
      rep(defns) { defn =>
        newline()
        defn_(defn)
      }

    def defn_(defn: Defn): Unit = defn match {
      case Defn.Var(attrs, name, ty, v) =>
        attrs_(attrs)
        str("var ")
        global_(name)
        str(" : ")
        type_(ty)
        str(" = ")
        val_(v)
      case Defn.Const(attrs, name, ty, v) =>
        attrs_(attrs)
        str("const ")
        global_(name)
        str(" : ")
        type_(ty)
        str(" = ")
        val_(v)
      case Defn.Declare(attrs, name, ty) =>
        attrs_(attrs)
        str("decl ")
        global_(name)
        str(" : ")
        type_(ty)
      case Defn.Define(attrs, name, ty, insts) =>
        attrs_(attrs)
        str("def ")
        global_(name)
        str(" : ")
        type_(ty)
        str(" {")
        rep(insts) {
          case inst: Inst.Label =>
            newline()
            inst_(inst)
          case inst =>
            indent()
            newline()
            inst_(inst)
            unindent()
        }
        newline()
        str("}")
      case Defn.Trait(attrs, name, ifaces) =>
        attrs_(attrs)
        str("trait ")
        global_(name)
        if (ifaces.nonEmpty) {
          str(" : ")
          rep(ifaces, sep = ", ")(global_)
        }
      case Defn.Class(attrs, name, parent, ifaces) =>
        val parents = parent ++: ifaces
        attrs_(attrs)
        str("class ")
        global_(name)
        if (parents.nonEmpty) {
          str(" : ")
          rep(parents, sep = ", ")(global_)
        }
      case Defn.Module(attrs, name, parent, ifaces) =>
        val parents = parent ++: ifaces
        attrs_(attrs)
        str("module ")
        global_(name)
        if (parents.nonEmpty) {
          str(" : ")
          rep(parents, sep = ", ")(global_)
        }
    }

    def type_(ty: Type): Unit = ty match {
      case Type.Vararg => str("...")
      case Type.Bool   => str("bool")
      case Type.Ptr    => str("ptr")
      case Type.Size   => str("size")
      case Type.Char   => str("char")
      case Type.Byte   => str("byte")
      case Type.Short  => str("short")
      case Type.Int    => str("int")
      case Type.Long   => str("long")
      case Type.Float  => str("float")
      case Type.Double => str("double")

      case Type.ArrayValue(ty, n) =>
        str("[")
        type_(ty)
        str(" x ")
        str(n)
        str("]")
      case Type.Function(args, ret) =>
        str("(")
        rep(args, sep = ", ")(type_)
        str(") => ")
        type_(ret)
      case Type.StructValue(tys) =>
        str("{")
        rep(tys, sep = ", ")(type_)
        str("}")

      case Type.Null    => str("null")
      case Type.Nothing => str("nothing")
      case Type.Virtual => str("virtual")
      case Type.Var(ty) => str("var["); type_(ty); str("]")
      case Type.Unit    => str("unit")
      case Type.Array(ty, nullable) =>
        if (!nullable) {
          str("?")
        }
        str("array[")
        type_(ty)
        str("]")
      case Type.Ref(name, exact, nullable) =>
        if (exact) {
          str("!")
        }
        if (!nullable) {
          str("?")
        }
        global_(name)
    }

    def global_(global: Global): Unit = global match {
      case Global.None =>
        unreachable
      case _ =>
        str("@\"")
        str(escapeQuotes(global.mangle))
        str("\"")
    }

    def sig_(sig: Sig): Unit =
      str(sig.mangle)

    def local_(local: Local): Unit = {
      str("%")
      str(local.id)
    }

    def syncAttrs_(attrs: SyncAttrs): Unit = {
      if (attrs.isVolatile) str("volatile ")
      memoryOrder_(attrs.memoryOrder)
    }

    def linktimeCondition(cond: LinktimeCondition): Unit = {
      import LinktimeCondition._
      cond match {
        case SimpleCondition(propertyName, comparison, value) =>
          str(propertyName + " ")
          comp_(comparison)
          str(" ")
          val_(value)
        case ComplexCondition(op, left, right) =>
          linktimeCondition(left)
          str(" ")
          bin_(op)
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
