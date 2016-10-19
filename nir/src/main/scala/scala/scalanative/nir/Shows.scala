package scala.scalanative
package nir

import util.{unreachable, sh, Show}
import Show.{Sequence => s, Indent => i, Unindent => ui, Repeat => r, Newline => nl}

object Shows {
  def brace(body: Show.Result): Show.Result = {
    val open  = "{"
    val close = nl("}")
    sh"$open$body$close"
  }

  implicit val showAttrs: Show[Attrs] = Show { attrs =>
    if (attrs == Attrs.None) s()
    else showAttrSeq(attrs.toSeq)
  }

  implicit val showAttrSeq: Show[Seq[Attr]] = Show { attrs =>
    r(attrs, sep = " ", post = " ")
  }

  implicit val showAttr: Show[Attr] = Show {
    case Attr.MayInline    => "mayinline"
    case Attr.InlineHint   => "inlinehint"
    case Attr.NoInline     => "noinline"
    case Attr.AlwaysInline => "alwaysinline"

    case Attr.Pure           => sh"pure"
    case Attr.Extern         => sh"extern"
    case Attr.Override(name) => sh"override($name)"

    case Attr.Link(name)        => sh"link($name)"
    case Attr.PinAlways(name)   => sh"pin($name)"
    case Attr.PinIf(name, cond) => sh"pin-if($name, $cond)"
  }

  implicit val showNext: Show[Next] = Show {
    case Next.Label(name, Seq()) =>
      sh"$name"
    case Next.Label(name, args) =>
      sh"$name(${r(args, sep = ", ")})"
    case Next.Succ(name) =>
      sh"succ $name"
    case Next.Fail(name) =>
      sh"fail $name"
    case Next.Case(value, name) =>
      sh"case $value => $name"
  }

  implicit val showInst: Show[Inst] = Show {
    case Inst.None =>
      sh"none"
    case Inst.Label(name, params) =>
      val paramlist =
        if (params.isEmpty) sh""
        else {
          val paramshows = params.map {
            case Val.Local(n, ty) => sh"$n: $ty"
          }
          sh"(${r(paramshows, sep = ", ")})"
        }
      sh"$name$paramlist:"
    case Inst.Let(name, op) =>
      sh"$name = $op"

    case Inst.Unreachable =>
      "unreachable"
    case Inst.Ret(Val.None) =>
      sh"ret"
    case Inst.Ret(value) =>
      sh"ret $value"
    case Inst.Jump(next) =>
      sh"jump $next"
    case Inst.If(cond, thenp, elsep) =>
      sh"if $cond then $thenp else $elsep"
    case Inst.Switch(scrut, default, cases) =>
      val body = brace(r(cases.map(i(_)) :+ i(sh"default: $default")))
      sh"switch $scrut $body"
    case Inst.Invoke(ty, f, args, succ, fail) =>
      sh"invoke[$ty] $f(${r(args, sep = ", ")}) to $succ unwind $fail"

    case Inst.Throw(value) =>
      sh"throw $value"
    case Inst.Try(normal, exc) =>
      sh"try $normal catch $exc"
  }

  implicit val showOp: Show[Op] = Show {
    case Op.Call(ty, f, args) =>
      sh"call[$ty] $f(${r(args, sep = ", ")})"
    case Op.Load(ty, ptr) =>
      sh"load[$ty] $ptr"
    case Op.Store(ty, ptr, value) =>
      sh"store[$ty] $ptr, $value"
    case Op.Elem(ty, ptr, indexes) =>
      sh"elem[$ty] $ptr, ${r(indexes, sep = ", ")}"
    case Op.Extract(aggr, indexes) =>
      sh"extract $aggr, ${r(indexes, sep = ", ")}"
    case Op.Insert(aggr, value, indexes) =>
      sh"insert $aggr, $value, ${r(indexes, sep = ", ")}"
    case Op.Stackalloc(ty, Val.None) =>
      sh"stackalloc[$ty]"
    case Op.Stackalloc(ty, n) =>
      sh"stackalloc[$ty] $n"
    case Op.Bin(name, ty, l, r) =>
      sh"$name[$ty] $l, $r"
    case Op.Comp(name, ty, l, r) =>
      sh"$name[$ty] $l, $r"
    case Op.Conv(name, ty, v) =>
      sh"$name[$ty] $v"
    case Op.Select(cond, thenv, elsev) =>
      sh"select $cond, $thenv, $elsev"

    case Op.Classalloc(name) =>
      sh"classalloc $name"
    case Op.Field(ty, value, name) =>
      sh"field[$ty] $value, $name"
    case Op.Method(ty, value, name) =>
      sh"method[$ty] $value, $name"
    case Op.Module(name) =>
      sh"module $name"
    case Op.As(ty, value) =>
      sh"as[$ty] $value"
    case Op.Is(ty, value) =>
      sh"is[$ty] $value"
    case Op.Copy(value) =>
      sh"copy $value"
    case Op.Sizeof(ty) =>
      sh"sizeof[$ty]"
    case Op.Closure(ty, fun, captures) =>
      sh"closure[$ty] ${r(fun +: captures, sep = ", ")}"
  }

  implicit val showBin: Show[Bin] = Show {
    case Bin.Iadd => "iadd"
    case Bin.Fadd => "fadd"
    case Bin.Isub => "isub"
    case Bin.Fsub => "fsub"
    case Bin.Imul => "imul"
    case Bin.Fmul => "fmul"
    case Bin.Sdiv => "sdiv"
    case Bin.Udiv => "udiv"
    case Bin.Fdiv => "fdiv"
    case Bin.Srem => "srem"
    case Bin.Urem => "urem"
    case Bin.Frem => "frem"
    case Bin.Shl  => "shl"
    case Bin.Lshr => "lshr"
    case Bin.Ashr => "ashr"
    case Bin.And  => "and"
    case Bin.Or   => "or"
    case Bin.Xor  => "xor"
  }

  implicit val showComp: Show[Comp] = Show {
    case Comp.Ieq => "ieq"
    case Comp.Ine => "ine"
    case Comp.Ugt => "ugt"
    case Comp.Uge => "uge"
    case Comp.Ult => "ult"
    case Comp.Ule => "ule"
    case Comp.Sgt => "sgt"
    case Comp.Sge => "sge"
    case Comp.Slt => "slt"
    case Comp.Sle => "sle"

    case Comp.Feq => "feq"
    case Comp.Fne => "fne"
    case Comp.Fgt => "fgt"
    case Comp.Fge => "fge"
    case Comp.Flt => "flt"
    case Comp.Fle => "fle"
  }

  implicit val showConv: Show[Conv] = Show {
    case Conv.Trunc    => "trunc"
    case Conv.Zext     => "zext"
    case Conv.Sext     => "sext"
    case Conv.Fptrunc  => "fptrunc"
    case Conv.Fpext    => "fpext"
    case Conv.Fptoui   => "fptoui"
    case Conv.Fptosi   => "fptosi"
    case Conv.Uitofp   => "uitofp"
    case Conv.Sitofp   => "sitofp"
    case Conv.Ptrtoint => "ptrtoint"
    case Conv.Inttoptr => "inttoptr"
    case Conv.Bitcast  => "bitcast"
  }

  implicit val showVal: Show[Val] = Show {
    case Val.None                        => "none"
    case Val.True                        => "true"
    case Val.False                       => "false"
    case Val.Zero(ty)                    => sh"zero[$ty]"
    case Val.Undef(ty)                   => sh"undef[$ty]"
    case Val.I8(value)                   => sh"${value}i8"
    case Val.I16(value)                  => sh"${value}i16"
    case Val.I32(value)                  => sh"${value}i32"
    case Val.I64(value)                  => sh"${value}i64"
    case Val.F32(value)                  => sh"${value}f32"
    case Val.F64(value)                  => sh"${value}f64"
    case Val.Struct(Global.None, values) => sh"struct {${r(values, ", ")}}"
    case Val.Struct(n, values)           => sh"struct $n {${r(values, ", ")}}"
    case Val.Array(ty, values)           => sh"array $ty {${r(values, ", ")}}"
    case Val.Chars(v)                    => s("c\"", v, "\"")
    case Val.Local(name, ty)             => sh"$name: $ty"
    case Val.Global(name, ty)            => sh"$name"

    case Val.Unit      => "unit"
    case Val.Const(v)  => sh"const $v"
    case Val.String(v) => "\"" + v + "\""
  }

  implicit val showDefns: Show[Seq[Defn]] = Show { defns =>
    r(defns, sep = nl(""))
  }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Var(attrs, name, ty, Val.None) =>
      sh"${attrs}var $name : $ty"
    case Defn.Var(attrs, name, ty, v) =>
      sh"${attrs}var $name : $ty = $v"
    case Defn.Const(attrs, name, ty, Val.None) =>
      sh"${attrs}const $name : $ty"
    case Defn.Const(attrs, name, ty, v) =>
      sh"${attrs}const $name : $ty = $v"
    case Defn.Declare(attrs, name, ty) =>
      val defn = sh"def $name : $ty"
      sh"$attrs$defn"
    case Defn.Define(attrs, name, ty, insts) =>
      val body = brace(r(insts.map {
        case inst: Inst.Label => i(showInst(inst))
        case inst             => i(showInst(inst), 2)
      }))
      val defn = sh"def $name : $ty $body"
      sh"$attrs$defn"
    case Defn.Struct(attrs, name, tys) =>
      sh"${attrs}struct $name {${r(tys, sep = ", ")}}"

    case Defn.Trait(attrs, name, ifaces) =>
      val inherits =
        if (ifaces.nonEmpty) r(ifaces, sep = ", ", pre = " : ") else s()
      sh"${attrs}trait $name$inherits"
    case Defn.Class(attrs, name, parent, ifaces) =>
      val parents = parent ++: ifaces
      val inherits =
        if (parents.nonEmpty) r(parents, sep = ", ", pre = " : ") else s()
      sh"${attrs}class $name$inherits"
    case Defn.Module(attrs, name, parent, ifaces) =>
      val parents = parent ++: ifaces
      val inherits =
        if (parents.nonEmpty) r(parents, sep = ", ", pre = " : ") else s()
      sh"${attrs}module $name$inherits"
  }

  implicit val showType: Show[Type] = Show {
    case Type.None                     => "none"
    case Type.Void                     => "void"
    case Type.Vararg                   => "..."
    case Type.Ptr                      => "ptr"
    case Type.Bool                     => "bool"
    case Type.I8                       => "i8"
    case Type.I16                      => "i16"
    case Type.I32                      => "i32"
    case Type.I64                      => "i64"
    case Type.F32                      => "f32"
    case Type.F64                      => "f64"
    case Type.Array(ty, n)             => sh"[$ty x $n]"
    case Type.Function(args, ret)      => sh"(${r(args, sep = ", ")}) => $ret"
    case Type.Struct(Global.None, tys) => sh"{${r(tys, sep = ", ")}}"
    case Type.Struct(name, _)          => sh"struct $name"

    case Type.Unit         => "unit"
    case Type.Nothing      => "nothing"
    case Type.Class(name)  => sh"class $name"
    case Type.Trait(name)  => sh"trait $name"
    case Type.Module(name) => sh"module $name"
  }

  implicit val showArg: Show[Arg] = Show {
    case Arg(ty, None)           => sh"$ty"
    case Arg(ty, Some(passConv)) => sh"$passConv $ty"
  }

  implicit val showPassConvention: Show[PassConv] = Show {
    case PassConv.Byval(ty) => sh"byval[$ty]"
    case PassConv.Sret(ty)  => sh"sret[$ty]"
  }

  implicit val showGlobal: Show[Global] = Show {
    case Global.None          => unreachable
    case Global.Top(id)       => sh"@$id"
    case Global.Member(n, id) => sh"${n: Global}::$id"
  }

  implicit val showLocal: Show[Local] = Show {
    case Local(scope, id) => sh"%$scope.$id"
  }
}
