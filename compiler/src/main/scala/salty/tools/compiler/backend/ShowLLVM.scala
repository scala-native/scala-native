package salty.tools.compiler.backend

import scala.collection.mutable
import salty.util.{sh, Show}, Show.{Sequence => s, Indent => i, Repeat => r, Newline => n}
import salty.ir, ir._
import salty.tools.compiler.backend.{Schedule => Sch}

object ShowLLVM {
  implicit def showSchedule: Show[Sch] = Show { sch =>
    r(sch.defns.map(n(_)))
  }

  implicit def showValue: Show[Sch.Value] = Show {
    case Sch.Value.Named(n) =>
      if (n.desc.isInstanceOf[Desc.Defn])
        sh"@${n.name}"
      else
        sh"%${n.name}"
    case Sch.Value.Struct(values) =>
      s("{", r(values, sep = ", "), "}")
    case Sch.Value.Const(n) =>
      n.desc
  }

  implicit def showOp: Show[Sch.Op] = Show { op =>
    import op._
    node.desc match {
      case Desc.Label | Desc.CaseTrue | Desc.CaseFalse |
           Desc.CaseConst | Desc.CaseDefault | Desc.CaseException =>
        sh"${node.name}:"
      case Desc.Return | Desc.Throw | Desc.Undefined |
           Desc.If | Desc.Switch | Desc.Try =>
        sh"  ${node.desc} ${r(args, sep = ", ")}"
      case _ =>
        sh"  %${node.name} = ${node.desc} ${r(args, ", ")}"
    }
  }

  implicit def showDefn: Show[Sch.Defn] = Show { defn =>
    import defn._
    val name = node.name
    node match {
      case Defn.Global(ty, _) =>
        sh"@$name = global @${ty.name}"
      case Defn.Constant(ty, _) =>
        sh"@$name = constant @${ty.name}"
      case Defn.Define(ty, params, _) =>
        sh"define @${ty.name} @$name(${showParams(params)}) { ${r(ops.map(n(_)))} ${n("}")}"
      case Defn.Declare(ty, params) =>
        sh"declare @${ty.name} @$name(${showParams(params)})"
      case Defn.Struct(elems) =>
        sh"@$name = type { ${r(elems.map(e => s("@", e.name)), sep = ", ")} }"
      case Defn.Ptr(ty) =>
        sh"@$name = type @${ty.name} *"
      case Defn.Function(ret, args) =>
        sh"@$name = type @${ret.name} (${r(args.map(a => s("@", a.name)), sep = ", ")})"
      case _ =>
        sh"${node.desc} @$name ${r(ops.map(n(_)))}"
    }
  }

  def showParams(params: Seq[Node]) =
    r(params.map {
      case param @ Param(ty) => sh"@${ty.name} %${param.name}"
    }, sep = ", ")

  implicit def showName: Show[Name] = Show(_.toString)

  implicit def showDesc: Show[Desc] = Show(_.toString.toLowerCase)
}
