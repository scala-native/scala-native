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
    case Instr(Name.None, op) => op
    case Instr(name, op)      => sh"$name = $op"
  }

  implicit val showParam: Show[Param] = Show {
    case Param(Name.None, ty) => ty
    case Param(name, ty)      => sh"$ty name"
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
    case Op.Invoke(func, args, succ, fail) =>
      sh"invoke $func(${r(args, sep = ", ")}) to $succ unwind $fail"
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
    case Val.Str(value)     => ???
  }

  implicit val showDefn: Show[Defn] = Show {
    case Defn.Var(name, ty, rhs)        => sh"$name = global $ty $rhs"
    case Defn.Declare(name, ty)         => ???
    case Defn.Define(name, ty, blocks)  => ???
    case Defn.Extern(name)              => sh"extern $name"
    case Defn.Struct(name, fields)      => sh"$name = type {${r(fields, sep = ", ")}}"

    case Defn.Interface(name, interfaces, members)      => ???
    case Defn.Class(name, parent, interfaces, members)  => ???
    case Defn.Module(name, parent, interfaces, members) => ???
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
    case Type.Function(ret, args) => sh"$ret (${r(args, sep = ", ")})"
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
