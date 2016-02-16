package native
package nir

import native.util.{sh, Show}, Show.{Sequence => s, Indent => i, Unindent => ui,
                                     Repeat => r, Newline => nl}

object Shows {
  def brace(body: Show.Result): Show.Result = {
    val open = "{"
    val close = nl("}")
    sh"$open$body$close"
  }

  implicit val showAdvice: Show[Advice] = Show {
    case Advice.No   => "no"
    case Advice.Hint => "hint"
    case Advice.Must => "must"
  }

  implicit val showAttrs: Show[Seq[Attr]] = Show { attrs =>
    if (attrs.isEmpty) s()
    else r(attrs, sep = " ", post = " ")
  }

  implicit val showAttr: Show[Attr] = Show {
    case Attr.Inline(advice)  => sh"inline($advice)"
    case Attr.Override(name)  => sh"override($name)"
  }

  implicit val showBlock: Show[Block] = Show { block =>
    import block._
    val paramlist =
      if (params.isEmpty) sh""
      else {
        val paramshows = params.map {
          case Val.Local(n, ty) => sh"$n: $ty"
        }
        sh"(${r(paramshows, sep = ", ")})"
      }
    val header = sh"$name$paramlist:"
    val body = i(r(insts, sep = nl("")))
    sh"$header$body"
  }

  implicit val showInst: Show[Inst] = Show {
    case Inst(None, op)       => sh"$op"
    case Inst(Some(name), op) => sh"$name = $op"
  }

  implicit val showNext: Show[Next] = Show {
    case Next(name, Seq()) =>
      sh"$name"
    case Next(name, args) =>
      sh"$name(${r(args, sep = ", ")})"
  }

  implicit val showCase: Show[Case] = Show {
    case Case(value, next) =>
      sh"case $value: $next"
  }

  implicit val showOp: Show[Op] = Show {
    case Op.Unreachable =>
      "unreachable"
    case Op.Ret(Val.None) =>
      sh"ret"
    case Op.Ret(value) =>
      sh"ret $value"
    case Op.Jump(next) =>
      sh"jump $next"
    case Op.If(cond, thenp, elsep) =>
      sh"if $cond then $thenp else $elsep"
    case Op.Switch(scrut, default, cases)  =>
      val body = brace(r(cases.map(i(_)) :+ i(sh"default: $default")))
      sh"switch $scrut $body"
    case Op.Invoke(ty, f, args, succ, fail) =>
      sh"invoke[$ty] $f(${r(args, sep = ", ")}) to $succ unwind $fail"

    case Op.Throw(value) =>
      sh"throw $value"
    case Op.Try(normal, exc) =>
      sh"try $normal catch $exc"

    case Op.Call(ty, f, args) =>
      sh"call[$ty] $f(${r(args, sep = ", ")})"
    case Op.Load(ty, ptr) =>
      sh"load[$ty] $ptr"
    case Op.Store(ty, ptr, value) =>
      sh"store[$ty] $ptr, $value"
    case Op.Elem(ty, ptr, indexes) =>
      sh"elem[$ty] $ptr, ${r(indexes, sep = ", ")}"
    case Op.Extract(ty, aggr, index) =>
      sh"extract[$ty] $aggr, $index"
    case Op.Insert(ty, aggr, value, index) =>
      sh"insert[$ty] $aggr, $value, $index"
    case Op.Alloca(ty) =>
      sh"alloca[$ty]"
    case Op.Bin(name, ty, l, r) =>
      sh"$name[$ty] $l, $r"
    case Op.Comp(name, ty, l, r) =>
      sh"$name[$ty] $l, $r"
    case Op.Conv(name, ty, v) =>
      sh"$name[$ty] $v"

    case Op.Alloc(ty) =>
      sh"alloc[$ty]"
    case Op.Field(ty, value, name) =>
      sh"field[$ty] $value, $name"
    case Op.Method(ty, value, name) =>
      sh"method[$ty] $value, $name"
    case Op.Module(name) =>
      sh"module $name"
    case Op.As(value, ty) =>
      sh"as[$ty] $value"
    case Op.Is(value, ty) =>
      sh"is[$ty] $value"
    case Op.ArrAlloc(ty, length) =>
      sh"arr-alloc[$ty] $length"
    case Op.ArrLength(value) =>
      sh"arr-length $value"
    case Op.ArrElem(ty, value, index) =>
      sh"arr-elem[$ty] $value, $index"
    case Op.Copy(value) =>
      sh"copy $value"
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
    case Val.None                => ""
    case Val.True                => "true"
    case Val.False               => "false"
    case Val.Zero(ty)            => sh"zero $ty"
    case Val.I8(value)           => sh"${value}i8"
    case Val.I16(value)          => sh"${value}i16"
    case Val.I32(value)          => sh"${value}i32"
    case Val.I64(value)          => sh"${value}i64"
    case Val.F32(value)          => sh"${value}f32"
    case Val.F64(value)          => sh"${value}f64"
    case Val.Struct(n, values)   => sh"struct $n {${r(values, ", ")}}"
    case Val.Array(ty, values)   => sh"array $ty {${r(values, ", ")}}"
    case Val.Chars(v)            => s("c\"", v, "\"")
    case Val.Local(name, ty)     => sh"$name"
    case Val.Global(name, ty)    => sh"$name"

    case Val.Unit      => "unit"
    case Val.Null      => "null"
    case Val.String(v) => "\"" + v.replace("\"", "\\\"") + "\""
    case Val.Size(ty)  => sh"sizeof $ty"
    case Val.Type(ty)  => sh"classof $ty"
  }

  implicit val showDefns: Show[Seq[Defn]] = Show { defns =>
    r(defns, sep = nl(""))
  }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Var(attrs, name, ty, v) =>
      sh"${attrs}var $name : $ty = $v"
    case Defn.Declare(attrs, name, ty) =>
      sh"${attrs}def $name : $ty"
    case Defn.Define(attrs, name, ty, blocks) =>
      val body = brace(r(blocks.map(i(_))))
      sh"${attrs}def $name : $ty $body"
    case Defn.Struct(attrs, name, tys) =>
      sh"${attrs}struct $name {${r(tys, sep = ", ")}}"

    case Defn.Interface(attrs, name, ifaces, members) =>
      val parents = r(ifaces, sep = ", ")
      val body = brace(r(members.map(i(_))))
      sh"${attrs}interface $name : $parents $body"
    case Defn.Class(attrs, name, parent, ifaces, members) =>
      val parents = r(parent +: ifaces, sep = ", ")
      val body = brace(r(members.map(i(_))))
      sh"${attrs}class $name : $parents $body"
    case Defn.Module(attrs, name, parent, ifaces, members) =>
      val parents = r(parent +: ifaces, sep = ", ")
      val body = brace(r(members.map(i(_))))
      sh"${attrs}module $name : $parents $body"
  }

  implicit val showType: Show[Type] = Show {
    case Type.None                => ""
    case Type.Void                => "void"
    case Type.Size                => "size"
    case Type.Bool                => "bool"
    case Type.Label               => "label"
    case Type.I8                  => "i8"
    case Type.I16                 => "i16"
    case Type.I32                 => "i32"
    case Type.I64                 => "i64"
    case Type.F32                 => "f32"
    case Type.F64                 => "f64"
    case Type.Array(ty, n)        => sh"[$ty x $n]"
    case Type.Ptr(ty)             => sh"ptr ${ty}"
    case Type.Function(args, ret) => sh"(${r(args, sep = ", ")}) => $ret"
    case Type.Struct(name)        => sh"struct $name"

    case Type.Unit                 => "unit"
    case Type.Nothing              => "nothing"
    case Type.Null                 => "null"
    case Type.Class(name)          => sh"class $name"
    case Type.InterfaceClass(name) => sh"interface $name"
    case Type.ModuleClass(name)    => sh"module $name"
    case Type.ArrayClass(ty)       => sh"${ty}[]"
  }

  implicit val showGlobal: Show[Global] = Show { g =>
    val pre = if (g.isIntrinsic) "#" else "@"
    sh"$pre${r(g.parts, sep = "_")}"
  }

  implicit val showLocal: Show[Local] = Show {
    case Local(scope, id) => sh"%$scope.$id"
  }
}
