package salty.tools.compiler.backend

import scala.collection.mutable
import salty.util.{sh, Show}, Show.{Sequence => s, Indent => i, Repeat => r, Newline => n}
import salty.ir, ir._
import salty.tools.compiler.backend.{Schedule => Sch}

object ShowLLVM {
  implicit def showSchedule: Show[Sch] = Show { sch =>
    val structs = sch.defns.collect { case defn @ Sch.Defn(Defn.Struct(_), _, _) => defn }.sortBy(_.node.name.toString)
    val state = sch.defns.collect { case defn @ Sch.Defn(Defn.Global(_, _) | Defn.Constant(_, _), _, _) => defn }.sortBy(_.node.name.toString)
    val funcs = sch.defns.collect { case defn @ Sch.Defn(Defn.Define(_, _, _) | Defn.Declare(_, _), _, _) => defn }.sortBy(_.node.name.toString)
    r(Seq(r(structs.map(n(_))), r(state.map(n(_))), r(funcs.map(n(_)))).map(n(_)))
  }

  implicit def showDefn: Show[Sch.Defn] = Show { defn =>
    import defn._
    val name = node.name
    def ty = tys.head
    def tytail = tys.tail
    node match {
      case Defn.Global(_, _) =>
        val Seq(Sch.Op(_, _, Seq(rhs))) = ops
        sh"@$name = global $rhs"
      case Defn.Constant(_, _) =>
        val Seq(Sch.Op(_, _, Seq(rhs))) = ops
        sh"@$name = constant $rhs"
      case Defn.Define(_, params, _) =>
        sh"define $ty @$name(${showParams(tytail, params)}) { ${r(ops.map(n(_)))} ${n("}") }"
      case Defn.Declare(_, params) =>
        sh"declare $ty @$name(${showParams(tytail, params)})"
      case Defn.Struct(elems) =>
        sh"%$name = type { ${r(tys, sep = ", ")} }"
      case _ =>
        sh"${node.desc} @$name ${r(ops.map(n(_)))}"
    }
  }

  implicit def showTy: Show[Sch.Type] = Show {
    case Sch.Type.None            => sh""
    case Sch.Type.Prim(n)         => sh"${n.name}"
    case Sch.Type.Struct(n)       => sh"%${n.name}"
    case Sch.Type.Ptr(ty)         => sh"${ty}*"
    case Sch.Type.Func(ret, args) => sh"$ret (${r(args, sep = ", ")})"
  }

  def showParams(tys: Seq[Sch.Type], params: Seq[Node]) =
    r(tys.zip(params).map {
      case (ty, param @ Param(_)) => sh"$ty %${param.name}"
    }, sep = ", ")

  implicit def showName: Show[Name] = Show {
    case Name.Slice(n) =>
      sh"${n}S"
    case Name.Constructor(owner, args) =>
      sh"${owner}__ctor_${r(args)}"
    case Name.Method(owner, name, args, ret) =>
      sh"${owner}__${name}_${r(args)}_$ret"
    case n => n.toString
  }

  implicit def showDesc: Show[Desc] = Show(_.toString.toLowerCase)

  def justvalue(v: Sch.Value) =
    v match {
      case Sch.Value.Op(Sch.Op(n, _, _)) =>
        sh"%${n.name}"
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
    def name = node.name
    def arg = args.head
    def argtail = args.tail

    node.desc match {
      case Desc.Label | Desc.CaseTrue | Desc.CaseFalse |
           Desc.CaseConst | Desc.CaseDefault | Desc.CaseException =>
        sh"$op$name:"
      case Desc.If =>
        val casetrue = node.uses.collectFirst { case Use(n @ CaseTrue(_)) => n }.get
        val casefalse = node.uses.collectFirst { case Use(n @ CaseFalse(_)) => n }.get
        sh"  br i1 ${justvalue(arg)}, label %casetrue${casetrue.name}, label %casefalse${casefalse.name}"
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
        val Seq(left, right) = args
        sh"  %$name = icmp eq $left, ${justvalue(right)}"
      case Desc.Alloc =>
        // TODO: proper allocation here
        val Sch.Type.Ptr(inner) = ty
        sh"  %$name = alloca $inner"
      case Desc.Store =>
        val Seq(ptr, value) = args
        sh"  store $value, $ptr"
      case Desc.Bitcast =>
        sh"  %$name = bitcast $arg to $ty"
      case Desc.Ptrtoint =>
        sh"  %$name = ptrtoint $arg to $ty"
    }
  }
}

