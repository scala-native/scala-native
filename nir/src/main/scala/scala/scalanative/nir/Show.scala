package scala.scalanative
package nir

import scala.util.matching.Regex

import util.{unreachable, ShowBuilder}

object Show {
  def newBuilder: NirShowBuilder = new NirShowBuilder(new ShowBuilder)
  def debug[T](msg: String)(f: => T): T = {
    val value = f
    println("$msg: " + value)
    value
  }

  def apply(v: Attr): String  = { val b = newBuilder; b.attr_(v); b.toString }
  def apply(v: Attrs): String = { val b = newBuilder; b.attrs_(v); b.toString }
  def apply(v: Bin): String   = { val b = newBuilder; b.bin_(v); b.toString }
  def apply(v: Comp): String  = { val b = newBuilder; b.comp_(v); b.toString }
  def apply(v: Conv): String  = { val b = newBuilder; b.conv_(v); b.toString }
  def apply(v: Defn): String  = { val b = newBuilder; b.defn_(v); b.toString }
  def apply(v: Global): String = {
    val b = newBuilder; b.global_(v); b.toString
  }
  def apply(v: Inst): String  = { val b = newBuilder; b.inst_(v); b.toString }
  def apply(v: Local): String = { val b = newBuilder; b.local_(v); b.toString }
  def apply(v: Next): String  = { val b = newBuilder; b.next_(v); b.toString }
  def apply(v: Op): String    = { val b = newBuilder; b.op_(v); b.toString }
  def apply(v: Type): String  = { val b = newBuilder; b.type_(v); b.toString }
  def apply(v: Val): String   = { val b = newBuilder; b.val_(v); b.toString }

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
      case Attr.Dyn =>
        str("dyn")
      case Attr.Align(value) =>
        str("align(")
        str(value)
        str(")")
      case Attr.Pure =>
        str("pure")
      case Attr.Extern =>
        str("extern")
      case Attr.Override(name) =>
        str("override(")
        global_(name)
        str(")")
      case Attr.Link(name) =>
        str("link(")
        str(name)
        str(")")
      case Attr.PinAlways(name) =>
        str("pin(")
        global_(name)
        str(")")
      case Attr.PinIf(name, cond) =>
        str("pin-if(")
        global_(name)
        str(", ")
        global_(cond)
        str(")")
      case Attr.PinWeak(name) =>
        str("pin-weak(")
        global_(name)
        str(")")
    }

    def next_(next: Next): Unit = next match {
      case Next.Label(name, Seq()) =>
        local_(name)
      case Next.Label(name, args) =>
        local_(name)
        str("(")
        rep(args, sep = ", ")(val_)
        str(")")
      case Next.Unwind(name) =>
        str("unwind ")
        local_(name)
      case Next.Case(v, name) =>
        str("case ")
        val_(v)
        str(" => ")
        local_(name)
    }

    def inst_(inst: Inst): Unit = inst match {
      case Inst.None =>
        str("none")
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
      case Inst.Let(name, op) =>
        local_(name)
        str(" = ")
        op_(op)
      case Inst.Unreachable =>
        str("unreachable")
      case Inst.Ret(Val.None) =>
        str("ret")
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
      case Inst.Switch(scrut, default, cases) =>
        str("switch ")
        val_(scrut)
        str(" {")
        indent()
        rep(cases) { next =>
          newline()
          next_(next)
        }
        newline()
        str("default => ")
        next_(default)
        unindent()
        newline()
        str("}")
      case Inst.Throw(v, unwind) =>
        str("throw ")
        val_(v)
        if (unwind ne Next.None) {
          str(" ")
          next_(unwind)
        }
    }

    def op_(op: Op): Unit = op match {
      case Op.Call(ty, f, args, unwind) =>
        str("call[")
        type_(ty)
        str("] ")
        val_(f)
        str("(")
        rep(args, sep = ", ")(val_)
        str(")")
        if (unwind ne Next.None) {
          str(" ")
          next_(unwind)
        }
      case Op.Load(ty, ptr, isVolatile) =>
        str(if (isVolatile) "volatile load[" else "load[")
        type_(ty)
        str("] ")
        val_(ptr)
      case Op.Store(ty, ptr, value, isVolatile) =>
        str(if (isVolatile) "volatile store[" else "store[")
        type_(ty)
        str("] ")
        val_(ptr)
        str(", ")
        val_(value)
      case Op.Elem(ty, ptr, indexes) =>
        str("elem[")
        type_(ty)
        str("] ")
        val_(ptr)
        str(", ")
        rep(indexes, sep = ", ")(str)
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
        if (n ne Val.None) {
          str(" ")
          val_(n)
        }
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
      case Op.Select(cond, thenv, elsev) =>
        str("select ")
        val_(cond)
        str(", ")
        val_(thenv)
        str(", ")
        val_(elsev)

      case Op.Classalloc(name) =>
        str("classalloc ")
        global_(name)
      case Op.Field(value, name) =>
        str("field ")
        val_(value)
        str(", ")
        global_(name)
      case Op.Method(value, name) =>
        str("method ")
        val_(value)
        str(", ")
        global_(name)
      case Op.Dynmethod(value, signature) =>
        str("dynmethod ")
        val_(value)
        str(", \"")
        str(escapeQuotes(signature))
        str("\"")
      case Op.Module(name, unwind) =>
        str("module ")
        global_(name)
        if (unwind ne Next.None) {
          str(" ")
          next_(unwind)
        }
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
      case Op.Sizeof(ty) =>
        str("sizeof[")
        type_(ty)
        str("] ")
      case Op.Closure(ty, fun, captures) =>
        str("closure[")
        type_(ty)
        str("] ")
        rep(fun +: captures, sep = ", ")(val_)
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
      case Conv.Trunc    => str("trunc")
      case Conv.Zext     => str("zext")
      case Conv.Sext     => str("sext")
      case Conv.Fptrunc  => str("fptrunc")
      case Conv.Fpext    => str("fpext")
      case Conv.Fptoui   => str("fptoui")
      case Conv.Fptosi   => str("fptosi")
      case Conv.Uitofp   => str("uitofp")
      case Conv.Sitofp   => str("sitofp")
      case Conv.Ptrtoint => str("ptrtoint")
      case Conv.Inttoptr => str("inttoptr")
      case Conv.Bitcast  => str("bitcast")
    }

    def val_(value: Val): Unit = value match {
      case Val.None =>
        str("none")
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
      case Val.Undef(ty) =>
        str("undef[")
        type_(ty)
        str("]")
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
      case Val.Struct(n, values) =>
        str("struct ")
        if (n ne Global.None) {
          global_(n)
          str(" ")
        }
        str("{")
        rep(values, sep = ", ")(val_)
        str("}")
      case Val.Array(ty, values) =>
        str("array ")
        type_(ty)
        str(" {")
        rep(values, sep = ", ")(val_)
        str("}")
      case Val.Chars(v) =>
        str("c\"")
        str(escapeNewLine(escapeQuotes(v)))
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
        if (v ne Val.None) {
          str(" = ")
          val_(v)
        }
      case Defn.Const(attrs, name, ty, v) =>
        attrs_(attrs)
        str("const ")
        global_(name)
        str(" : ")
        type_(ty)
        if (v ne Val.None) {
          str(" = ")
          val_(v)
        }
      case Defn.Declare(attrs, name, ty) =>
        attrs_(attrs)
        str("def ")
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
      case Defn.Struct(attrs, name, tys) =>
        attrs_(attrs)
        str("struct ")
        global_(name)
        str(" {")
        rep(tys, sep = ", ")(type_)
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
      case Type.None   => str("none")
      case Type.Void   => str("void")
      case Type.Vararg => str("...")
      case Type.Bool   => str("bool")
      case Type.Ptr    => str("ptr")
      case Type.Char   => str("char")
      case Type.Byte   => str("byte")
      case Type.UByte  => str("ubyte")
      case Type.Short  => str("short")
      case Type.UShort => str("ushort")
      case Type.Int    => str("int")
      case Type.UInt   => str("uint")
      case Type.Long   => str("long")
      case Type.ULong  => str("ulong")
      case Type.Float  => str("float")
      case Type.Double => str("double")

      case Type.Array(ty, n) =>
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
      case Type.Struct(Global.None, tys) =>
        str("{")
        rep(tys, sep = ", ")(type_)
        str("}")
      case Type.Struct(name, _) =>
        str("struct ")
        global_(name)

      case Type.Unit    => str("unit")
      case Type.Nothing => str("nothing")
      case Type.Class(name) =>
        str("class ")
        global_(name)
      case Type.Trait(name) =>
        str("trait ")
        global_(name)
      case Type.Module(name) =>
        str("module ")
        global_(name)
    }

    def global_(global: Global): Unit = global match {
      case Global.None =>
        unreachable
      case Global.Top(id) =>
        str("@")
        str(id)
      case Global.Member(n, id) =>
        global_(n)
        str("::")
        str(id)
    }

    def local_(local: Local): Unit = local match {
      case Local(scope, id) =>
        str("%")
        str(scope)
        str(".")
        str(id)
    }

    private def escapeNewLine(s: String): String =
      """([^\\]|^)\n""".r.replaceAllIn(s, _.matched.toSeq match {
        case Seq(sngl)     => s"""\\\\n"""
        case Seq(fst, snd) => s"""${fst}\\\\n"""
      })

    private def escapeQuotes(s: String): String =
      """([^\\]|^)"""".r.replaceAllIn(s, _.matched.toSeq match {
        case Seq(sngl)     => s"\\\\$sngl"
        case Seq(fst, snd) => s"$fst\\\\$snd"
      })

    override def toString: String = builder.toString
  }
}
