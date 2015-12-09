package native
package nir

import native.util.{sh, Show}, Show.{Sequence => s, Indent => i, Unindent => ui,
                                     Repeat => r, Newline => nl}

object Shows {
  implicit val showBlock: Show[Block] = Show { block =>
    import block._
    val header = ui(sh"$name(${r(params, sep = ", ")})")
    val body = r(instrs.map(nl(_)))

    sh"$header: $body"
  }

  implicit val showInstr: Show[Instr] = Show {
    case Instr(Name.None, op, _)    => op
    case Instr(name, op, Type.None) => sh"$name = $op"
    case Instr(name, op, ty)        => sh"$name: $ty = $op"
  }

  implicit val showParam: Show[Param] = Show {
    case Param(Name.None, ty) => ty
    case Param(name, ty)      => sh"name: $ty"
  }

  implicit val showNext: Show[Next] = Show {
    case Next(Val.None, name, args) =>
      sh"label $name(${r(args, sep = ", ")})"
    case Next(value, name, args) =>
      sh"$value, label $name(${r(args, sep = ", ")})"
  }

  implicit val showOp: Show[Op] = Show {
    case Op.Undefined =>
      "undefined"
    case Op.Ret(value) =>
      sh"ret $value"
    case Op.Throw(value) =>
      sh"throw $value"
    case Op.Br(cond, thenp, elsep) =>
      sh"br $cond, $thenp, $elsep"
    case Op.Switch(scrut, default, cases)  =>
      sh"switch $scrut, $default [${r(cases, sep = ", ")}]"
    case Op.Invoke(f, args, succ, fail) =>
      sh"invoke $f(${r(args, sep = ", ")}) to $succ unwind $fail"
    case Op.Call(f, args) =>
      sh"call $f(${r(args, sep = ",")})"
    case Op.Load(ty, ptr) =>
      sh"load[$ty] $ptr"
    case Op.Store(ty, ptr, value) =>
      sh"store[$ty] $ptr, $value"
    case Op.Elem(ptr, indexes) =>
      sh"element $ptr, ${r(indexes, sep = ", ")}"
    case Op.Extract(aggr, index) =>
      sh"extract $aggr, $index"
    case Op.Insert(aggr, value, index) =>
      sh"insert $aggr, $index"
    case Op.Alloc(ty) =>
      sh"alloc[$ty]"
    case Op.Alloca(ty) =>
      sh"alloca[$ty]"
    case Op.Size(ty) =>
      sh"size[$ty]"
    case Op.Builtin(name, targs, args) =>
      sh"$name[${r(targs, sep = ", ")}] ${r(args, sep = ",")}"

    case Op.FieldElem(name, value) =>
      sh"field-elem $name, $value"
    case Op.MethodElem(name, value) =>
      sh"method-elem $name, $value"
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
    case Op.ArrayElem(value, index) =>
      sh"array-elem $value, $index"
    case Op.Box(value, to) =>
      sh"box[$to] $value"
    case Op.Unbox(value, to) =>
      sh"unbox[$to] $value"
  }

  implicit val showBuiltin: Show[Builtin] = Show {
    case Builtin.Add  => "add"
    case Builtin.Sub  => "sub"
    case Builtin.Mul  => "mul"
    case Builtin.Div  => "div"
    case Builtin.Mod  => "mod"
    case Builtin.Shl  => "shl"
    case Builtin.Lshr => "lshr"
    case Builtin.Ashr => "ashr"
    case Builtin.And  => "and"
    case Builtin.Or   => "or"
    case Builtin.Xor  => "xor"
    case Builtin.Eq   => "eq"
    case Builtin.Neq  => "neq"
    case Builtin.Lt   => "lt"
    case Builtin.Lte  => "lte"
    case Builtin.Gt   => "gt"
    case Builtin.Gte  => "gte"

    case Builtin.Trunc    => "trunc"
    case Builtin.Zext     => "zext"
    case Builtin.Sext     => "sext"
    case Builtin.Fptrunc  => "fptrunc"
    case Builtin.Fpext    => "fpext"
    case Builtin.Fptoui   => "fptoui"
    case Builtin.Fptosi   => "fptosi"
    case Builtin.Uitofp   => "uitofp"
    case Builtin.Sitofp   => "sitofp"
    case Builtin.Ptrtoint => "ptrtoint"
    case Builtin.Inttoptr => "inttoptr"
    case Builtin.Bitcast  => "bitcast"
  }

  implicit val showVal: Show[Val] = Show {
    case Val.None           => ""
    case Val.Zero           => "zero"
    case Val.True           => "true"
    case Val.False          => "false"
    case Val.I8(value)      => value
    case Val.I16(value)     => value
    case Val.I32(value)     => value
    case Val.I64(value)     => value
    case Val.F32(value)     => value
    case Val.F64(value)     => value
    case Val.Struct(values) => sh"{${r(values, ", ")}}"
    case Val.Array(values)  => sh"[${r(values, ", ")}]"
    case Val.Name(name)     => name

    case Val.Null           => "null"
    case Val.Unit           => "unit"
  }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Extern(name) =>
      sh"extern $name"
    case Defn.Var(name, ty, rhs) =>
      sh"var $name: $ty = $rhs"
    case Defn.Declare(name, ty) =>
      sh"declare $name: $ty"
    case Defn.Define(name, ty, blocks) =>
      val body = r(blocks.map(i(_)), pre = "{", post = nl("}"))
      sh"define $name: $ty = $body"
    case Defn.Struct(name, fields) =>
      sh"struct $name {${r(fields, sep = ", ")}}"

    case Defn.Interface(name, ifaces, members) =>
      val parents = r(ifaces, sep = ",")
      val body = r(members.map(i(_)), pre = "{", post = nl("}"))
      sh"interface $name: $parents $body"
    case Defn.Class(name, parent, ifaces, members) =>
      val parents = r(parent +: ifaces, sep = ",")
      val body = r(members.map(i(_)), pre = "{", post = nl("}"))
      sh"class $name: $parents $body"
    case Defn.Module(name, parent, ifaces, members) =>
      val parents = r(parent +: ifaces, sep = ",")
      val body = r(members.map(i(_)), pre = "{", post = nl("}"))
      sh"module $name: $parents $body"
  }

  implicit val showType: Show[Type] = Show {
    case Type.None                => ""
    case Type.Void                => "void"
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
    case Name.Fresh(id)             => id
    case Name.Local(id)             => id
    case Name.Extern(id)            => id
    case Name.Nested(owner, member) => sh"$owner::$member"
    case Name.Class(id)             => sh"class.$id"
    case Name.Module(id)            => sh"module.$id"
    case Name.Interface(id)         => sh"interface.$id"
    case Name.Field(id)             => id
    case Name.Constructor(args)     => sh"<${args.mkString(", ")}>"
    case Name.Method(id, args, ret) => sh"$id<${args.mkString(", ")}; $ret>"
    case Name.Accessor(owner)       => sh"$owner.accessor"
    case Name.Data(owner)           => sh"$owner.data"
    case Name.Vtable(owner)         => sh"$owner.vtable"
    case Name.Array(n)              => sh"$n[]"
  }
}
