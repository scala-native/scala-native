package native
package compiler
package codegen

import native.nir._
import native.util.unsupported
import native.util.{sh, Show}, Show.{Sequence => s, Indent => i, Unindent => ui,
                                     Repeat => r, Newline => nl}

object GenTextualLLVM extends GenShow {
  implicit def showDefns: Show[Seq[Defn]] = Show { defns =>
    r(defns, sep = nl(""))
  }

  implicit def showDefn: Show[Defn] = Show {
    case Defn.Var(attrs, name, ty, rhs) =>
      sh"@$name = global $ty $rhs"
    case Defn.Declare(attrs, name, Type.Function(argtys, retty)) =>
      sh"declare $retty @$name(${r(argtys, sep = ", ")})"
    case Defn.Define(attrs, name, Type.Function(_, retty), entry +: rest) =>
      sh""
    case Defn.Struct(attrs, name, tys) =>
      sh"type %$name = {${r(tys, sep = ", ")}}"
    case defn =>
      unsupported(defn)
  }

  implicit def showType: Show[Type] = Show {
    case Type.Void                => "void"
    case Type.Bool                => "i1"
    case Type.I8                  => "i8"
    case Type.I16                 => "i16"
    case Type.I32                 => "i32"
    case Type.I64                 => "i64"
    case Type.F32                 => "f32"
    case Type.F64                 => "f64"
    case Type.Array(ty, n)        => sh"[$ty x $n]"
    case Type.Ptr(ty)             => sh"${ty}*"
    case Type.Function(args, ret) => sh"$ret (${r(args, sep = ", ")})"
    case Type.Struct(name)        => sh"%$name"
    case ty                       => unsupported(ty)
  }

  implicit def showVal: Show[Val] = ???

  implicit def showGlobal: Show[Global] = Show {
    case Global.Atom(id)              => id
    case Global.Nested(owner, member) => sh"${owner}__$member"
    case Global.Tagged(owner, tag)    => sh"${owner}.$tag"
  }
}
