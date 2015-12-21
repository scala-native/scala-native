package native
package nir

import native.util.{sh, Show}, Show.{Sequence => s, Indent => i, Unindent => ui,
                                     Repeat => r, Newline => nl}

object Shows {
  implicit val showBlock: Show[Block] = Show { block =>
    import block._
    val header = sh"block $name(${r(params, sep = ", ")})"
    val body = i(r(instrs, sep = nl("")))
    sh"$header =$body"
  }

  implicit val showInstr: Show[Instr] = Show {
    case Instr(Name.None, op) => op
    case Instr(name, op)      => sh"$name: ${op.resty} = $op"
  }

  implicit val showParam: Show[Param] = Show {
    case Param(Name.None, ty) => ty
    case Param(name, ty)      => sh"$name: $ty"
  }

  implicit val showNext: Show[Next] = Show {
    case Next(name, args) =>
      sh"$name(${r(args, sep = ", ")})"
  }

  implicit val showCase: Show[Case] = Show {
    case Case(value, next) =>
      sh"case $value => $next"
  }

  implicit val showOp: Show[Op] = Show {
    case Op.Undefined =>
      "undefined"
    case Op.Ret(value) =>
      sh"ret $value"
    case Op.Throw(value) =>
      sh"throw $value"
    case Op.Jump(next) =>
      sh"jump $next"
    case Op.If(cond, thenp, elsep) =>
      sh"if $cond then $thenp else $elsep"
    case Op.Switch(scrut, default, cases)  =>
      sh"switch $scrut, $default [${r(cases, sep = "; ")}]"
    case Op.Invoke(f, args, succ, fail) =>
      sh"invoke $f(${r(args, sep = ", ")}) to $succ unwind $fail"

    case Op.Call(ty, f, args) =>
      sh"call[$ty] $f(${r(args, sep = ",")})"
    case Op.Load(ty, ptr) =>
      sh"load[$ty] $ptr"
    case Op.Store(ty, ptr, value) =>
      sh"store[$ty] $ptr, $value"
    case Op.Elem(ty, ptr, indexes) =>
      sh"element[$ty] $ptr, ${r(indexes, sep = ", ")}"
    case Op.Extract(ty, aggr, index) =>
      sh"extract[$ty] $aggr, $index"
    case Op.Insert(ty, aggr, value, index) =>
      sh"insert[$ty] $aggr, $index"
    case Op.Alloc(ty) =>
      sh"alloc[$ty]"
    case Op.Alloca(ty) =>
      sh"alloca[$ty]"
    case Op.Size(ty) =>
      sh"size[$ty]"
    case Op.Bin(name, ty, l, r) =>
      sh"$name[$ty] $l, $r"
    case Op.Comp(name, ty, l, r) =>
      sh"$name[$ty] $l, $r"
    case Op.Conv(name, ty, v) =>
      sh"$name[$ty] $v"

    case Op.FieldElem(ty, name, value) =>
      sh"field-elem[$ty] $name, $value"
    case Op.MethodElem(ty, name, value) =>
      sh"method-elem[$ty] $name, $value"
    case Op.AllocClass(ty) =>
      sh"alloc-class[$ty]"
    case Op.AllocArray(ty, length) =>
      sh"alloc-array[$ty] $length"
    case Op.Equals(left, right) =>
      sh"equals $left, $right"
    case Op.HashCode(value) =>
      sh"hash-code $value"
    case Op.GetClass(value) =>
      sh"get-class $value"
    case Op.ClassOf(ty) =>
      sh"class-of[$ty]"
    case Op.AsInstanceOf(value, ty) =>
      sh"as-instance-of[$ty] $value"
    case Op.IsInstanceOf(value, ty) =>
      sh"is-instance-of[$ty] $value"
    case Op.ArrayLength(value) =>
      sh"array-length $value"
    case Op.ArrayElem(ty, value, index) =>
      sh"array-elem[$ty] $value, $index"
    case Op.Box(value, to) =>
      sh"box[$to] $value"
    case Op.Unbox(value, to) =>
      sh"unbox[$to] $value"
    case Op.MonitorEnter(v) =>
      sh"monitor-enter $v"
    case Op.MonitorExit(v) =>
      sh"monitor-exit $v"
  }

  implicit val showBin: Show[Bin] = Show {
    case Bin.Add  => "add"
    case Bin.Sub  => "sub"
    case Bin.Mul  => "mul"
    case Bin.Div  => "div"
    case Bin.Mod  => "mod"
    case Bin.Shl  => "shl"
    case Bin.Lshr => "lshr"
    case Bin.Ashr => "ashr"
    case Bin.And  => "and"
    case Bin.Or   => "or"
    case Bin.Xor  => "xor"
  }

  implicit val showComp: Show[Comp] = Show {
    case Comp.Eq  => "eq"
    case Comp.Neq => "neq"
    case Comp.Lt  => "lt"
    case Comp.Lte => "lte"
    case Comp.Gt  => "gt"
    case Comp.Gte => "gte"
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
    case Val.None               => ""
    case Val.Zero               => "zero"
    case Val.True               => "true"
    case Val.False              => "false"
    case Val.I8(value)          => sh"${value}i8"
    case Val.I16(value)         => sh"${value}i16"
    case Val.I32(value)         => sh"${value}i32"
    case Val.I64(value)         => sh"${value}i64"
    case Val.F32(value)         => sh"${value}f32"
    case Val.F64(value)         => sh"${value}f64"
    case Val.Struct(ty, values) => sh"struct[${ty: Type}] ${r(values, ", ")}"
    case Val.Array(ty, values)  => sh"array[$ty] ${r(values, ", ")}"
    case Val.Name(name, ty)     => sh"$name: $ty"

    case Val.Null => "null"
    case Val.Unit => "unit"
  }

  implicit val showDefns: Show[Seq[Defn]] = Show { defns => r(defns.map(nl(_))) }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Var(name, ty, rhs) =>
      sh"var $name: $ty = $rhs"
    case Defn.Declare(name, ty) =>
      sh"declare $name: $ty"
    case Defn.Define(name, ty, blocks) =>
      val body = r(blocks.map(i(_)))
      sh"define $name: $ty = $body"
    case Defn.Struct(name, fields) =>
      sh"struct $name {${r(fields, sep = ", ")}}"

    case Defn.Interface(name, ifaces, members) =>
      val parents = r(ifaces, sep = ",")
      val body = r(members.map(i(_)))
      sh"interface $name: $parents =$body"
    case Defn.Class(name, parent, ifaces, members) =>
      val parents = r(parent +: ifaces, sep = ",")
      val body = r(members.map(i(_)))
      sh"class $name: $parents =$body"
    case Defn.Module(name, parent, ifaces, members) =>
      val parents = r(parent +: ifaces, sep = ",")
      val body = r(members.map(i(_)))
      sh"module $name: $parents =$body"
  }

  implicit val showType: Show[Type] = Show {
    case Type.None                => ""
    case Type.Void                => "void"
    case Type.Size                => "size"
    case Type.Bool                => "bool"
    case Type.I8                  => "i8"
    case Type.I16                 => "i16"
    case Type.I32                 => "i32"
    case Type.I64                 => "i64"
    case Type.F32                 => "f32"
    case Type.F64                 => "f64"
    case Type.Array(ty, n)        => sh"[$ty x $n]"
    case Type.Ptr(ty)             => sh"${ty}*"
    case Type.Function(args, ret) => sh"(${r(args, sep = ", ")}) => $ret"
    case Type.Struct(name)        => name

    case Type.Unit                => "unit"
    case Type.Nothing             => "nothing"
    case Type.Null                => "null"
    case Type.Class(name)         => name
    case Type.ArrayClass(ty)      => ty
  }

  implicit val showName: Show[Name] = Show {
    case Name.None                  => ""
    case Name.Fresh(id)             => sh"%$id"
    case Name.Local(id)             => sh"%$id"
    case Name.Prim(id)              => id
    case Name.Foreign(id)           => sh"@$id"
    case Name.Nested(owner, member) => sh"$owner::$member"
    case Name.Class(id)             => sh"@class.$id"
    case Name.Module(id)            => sh"@module.$id"
    case Name.Interface(id)         => sh"@interface.$id"
    case Name.Field(id)             => id
    case Name.Constructor(args)     => sh"<${args.mkString(", ")}>"
    case Name.Method(id, args, ret) => sh"$id<${args.mkString(", ")}; $ret>"
    case Name.Accessor(owner)       => sh"$owner.accessor"
    case Name.Data(owner)           => sh"$owner.data"
    case Name.Vtable(owner)         => sh"$owner.vtable"
    case Name.Array(n)              => sh"$n[]"
  }
}
