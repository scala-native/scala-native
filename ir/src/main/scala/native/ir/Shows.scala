package native
package ir

import scala.collection.mutable
import native.util.{sh, Show}, Show.{Sequence => s, Indent => i, Repeat => r, Newline => nl}
import native.ir.{Schedule => Sch}

object Shows {
  implicit def showSchedule: Show[Sch] = Show { sch =>
    val types = sch.defns.collect { case d: Sch.Defn.Type  => d }
    val state = sch.defns.collect { case d: Sch.Defn.State => d }
    val funcs = sch.defns.collect { case d: Sch.Defn.Func  => d }
    s(r(types, sep = nl("")), nl(nl("")),
      r(state, sep = nl("")), nl(nl("")),
      r(funcs, sep = nl("")))
  }

  implicit def showDefn: Show[Sch.Defn] = Show {
    case d: Sch.Defn.Type  => d
    case d: Sch.Defn.State => d
    case d: Sch.Defn.Func  => d
  }

  implicit def showDefnState: Show[Sch.Defn.State] = Show {
    case Sch.Defn.State(n, rhs) =>
      n match {
        case Defn.Global(_, _) =>
          sh"@${n.name} = global $rhs"
        case Defn.Constant(_, _) =>
          sh"@${n.name} = constant $rhs"
        case Defn.Field(_, _) =>
          sh"@${n.name} = field $rhs"
      }
  }

  implicit def showDefnType: Show[Sch.Defn.Type] = Show {
    case Sch.Defn.Type(n, tys, members) =>
      def ext =
        if (tys.nonEmpty) sh": ${r(tys, sep = ", ")}"
        else sh""
      def body =
        if (members.nonEmpty) sh" {${r(members.map(i(_)))}${nl("}")}"
        else sh""
      n match {
        case Defn.Struct(elems) =>
          sh"%${n.name} = type { ${r(tys, sep = ", ")} }"
        case Defn.Class(_, _) =>
          sh"class %${n.name}$ext$body"
        case Defn.Interface(_) =>
          sh"interface %${n.name}$ext$body"
        case Defn.Module(_, _, _) =>
          sh"module @${n.name}$ext$body"
      }
  }

  implicit def showDefnFunc: Show[Sch.Defn.Func] = Show {
    case Sch.Defn.Func(n, ret, paramtys, ops) =>
      n match {
        case Defn.Define(_, params, _) =>
          sh"define $ret @${n.name}(${showParams(paramtys, params)}) {${r(ops.map(nl(_)))}${nl("}")}"
        case Defn.Declare(_, params) =>
          sh"declare $ret @${n.name}(${r(paramtys, ", ")})"
        case Defn.Method(_, params, _, _) =>
          sh"method $ret @${n.name}(${showParams(paramtys, params)}) {${r(ops.map(nl(_)))}${nl("}")}"
      }
  }

  implicit def showTy: Show[Sch.Type] = Show {
    case Sch.Type.None            => sh""
    case Sch.Type.Prim(n)         => sh"${n.name}"
    case Sch.Type.Defn(n)         => sh"%${n.name}"
    case Sch.Type.Ptr(ty)         => sh"${ty}*"
    case Sch.Type.Array(ty, n)    => sh"[$ty x n]"
    case Sch.Type.Func(ret, args) => sh"$ret (${r(args, sep = ", ")})"
    case Sch.Type.ArrayClass(ty)  => sh"${ty}[]"
  }

  def showParams(tys: Seq[Sch.Type], params: Seq[Node]) =
    r(tys.zip(params).map {
      case (ty, param @ Param(_)) => sh"$ty %${param.name}"
    }, sep = ", ")

  implicit def showName: Show[Name] = Show {
    case Name.Array(n) =>
      sh"${n}A"
    case Name.Constructor(owner, args) =>
      sh"${owner}__ctor_${r(args)}"
    case Name.Method(owner, name, args, ret) =>
      sh"${owner}__${name}_${r(args)}_$ret"
    case n => n.toString
  }

  implicit def showDesc: Show[Desc] = Show(_.toString.toLowerCase)

  def justvalue(v: Sch.Value) =
    v match {
      case Sch.Value.Op(op) =>
        sh"%${op.name}"
      case Sch.Value.Struct(_, values) =>
        sh"{ ${r(values, ", ")} }"
      case Sch.Value.Const(n) =>
        n.desc match {
          case Desc.Lit.Zero =>
            s("zeroinitializer")
          case lit: Desc.Lit =>
            s(lit.valueString)
        }
      case Sch.Value.Param(n) =>
        sh"%${n.name}"
      case Sch.Value.Defn(n) =>
        sh"@${n.name}"
    }

  implicit def showValue: Show[Sch.Value] = Show { v =>
    def ty = v.ty
    def justv = justvalue(v)
    sh"$ty $justv"
  }

  implicit def showOp: Show[Sch.Op] = Show { operator =>
    import operator._
    def op = node.desc
    def arg = args.head
    def argtail = args.tail
    def label(op: Sch.Op) = sh"label %${op.name}"

    node.desc match {
      case Desc.Label | Desc.CaseTrue | Desc.CaseFalse |
           Desc.CaseConst | Desc.CaseDefault | Desc.CaseException =>
        sh"$name:"
      case Desc.If =>
        sh"  br i1 ${justvalue(arg)}, ${label(cf(0))}, ${label(cf(1))}"
      case Desc.Return =>
        sh"  ret $arg"
      case Desc.StructElem =>
        val arg :: Sch.Value.Const(Lit.I32(n)) :: Nil = args
        sh"  %$name = extractvalue $arg, ${n.toString}"
      case Desc.Elem =>
        sh"  %$name = getelementptr ${r(args, ", ")}"
      case Desc.Load =>
        sh"  %$name = load $arg"
      case Desc.Call =>
        sh"  %$name = call $arg(${r(argtail, ", ")})"
      case Desc.Eq =>
        // TODO: floats
        val Seq(left, right) = args
        sh"  %$name = icmp eq $left, ${justvalue(right)}"
      case Desc.Store =>
        val Seq(ptr, value) = args
        sh"  store $value, $ptr"
      case Desc.Bitcast =>
        sh"  %$name = bitcast $arg to $ty"
      case Desc.Ptrtoint =>
        sh"  %$name = ptrtoint $arg to $ty"
      case desc =>
        sh"  %$name = ${desc.toString.toLowerCase} ${r(args, sep = ", ")}"
    }
  }
}

