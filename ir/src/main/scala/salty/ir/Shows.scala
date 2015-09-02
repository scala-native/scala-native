package salty.ir

import salty.ir.Combinators._
import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                              Repeat => r, Newline => n}
import salty.util.sh

object Shows {
  implicit val showType: Show[Type] = Show {
    case Type.Unit         => "unit"
    case Type.Null         => "null"
    case Type.Nothing      => "nothing"
    case Type.Bool         => "bool"
    case Type.I(w)         => s("i", w.toString)
    case Type.F(w)         => s("f", w.toString)
    case Type.Ref(ty)      => s(ty, "!")
    case Type.Slice(ty)    => s("[", ty, "]")
    case Type.Named(n)     => s((n: Name))
  }

  implicit val showInstr: Show[Instr] = Show {
    case e: Expr =>
      e
    case Instr.Assign(name, expr) =>
      s(name, " = ", expr)
  }

  implicit val showTermn: Show[Termn] = Show {
    case Termn.Undefined =>
      "undefined"
    case Termn.Out(value) =>
      s("out ", value)
    case Termn.Return(value) =>
      s("return ", value)
    case Termn.Throw(value) =>
      s("throw ", value)
    case Termn.Jump(name) =>
      s("jump ", name.name)
    case Termn.If(cond, thenp, elsep) =>
      s("if ", cond, " then ", thenp.name, " else ", elsep.name)
    case Termn.Switch(on, default, branches) =>
      s("switch ", on, ", ", default.name, " { ",
          r(branches.map(i(_))),
        n("}"))
    case Termn.Try(body, catchopt, finallyopt) =>
      s("try ", body.name,
        catchopt.map(catchb => s(" catch ", catchb.name)).getOrElse(s()),
        finallyopt.map(finallyb => s(" finally ", finallyb.name)).getOrElse(s()))
  }

  implicit val showExpr: Show[Expr] = Show {
    case v: Val =>
      v
    case Expr.Bin(op, left, right) =>
      s(op.toString.toLowerCase, " ", left, ", ", right)
    case Expr.Conv(op, value, to) =>
      s(op.toString.toLowerCase, " ", value, " to ", to)
    case Expr.Is(value, ty) =>
      s("is ", value, ", ", ty)
    case Expr.Alloc(name, elements) =>
      s("alloc ", name,
        elements.map { v => s(", ", v) }.getOrElse(s()))
    case Expr.Call(name, args) =>
      s("call ", name, "(", r(args, sep = ", "), ")")
    case Expr.Phi(branches) =>
      s("phi { ",
          r(branches.map(i(_))),
        n("}"))
    case Expr.Load(ptr) =>
      s("load ", ptr)
    case Expr.Store(ptr, value) =>
      s("store ", ptr, ", ", value)
    case Expr.Box(value, ty) =>
      s("box ", value, ", ", ty)
    case Expr.Unbox(value, ty) =>
      s("unbox ", value, ", ", ty)
    case Expr.Length(value) =>
      s("length ", value)
    case Expr.Catchpad =>
      s("catchpad")
  }

  implicit val showVal: Show[Val] = Show {
    case n: Name              => n
    case Val.Null             => "null"
    case Val.Unit             => "unit"
    case Val.This             => "this"
    case Val.Bool(v)          => v.toString
    case Val.Number(repr, ty) => s(repr, (ty: Type))
    case Val.Elem(ptr, value) => s("elem(", ptr, ", ", value, ")")
    case Val.Class(ty)        => s("class(", ty, ")")
    case Val.Str(str)         =>
      val esc = str.replace("\"", "\\\"").replace("\n", "\\n")
      s("\"", esc, "\"")
  }

  implicit val showStat: Show[Stat] = Show {
    case Stat.Class(p, ifaces, scope) =>
      s("class", r(p +: ifaces, pre = " (", sep = ", ", post = ")"), " {",
          i(scope),
        n("}"))
    case Stat.Interface(ifaces, scope) =>
      s("interface", r(ifaces, pre = " (", sep = ", ", post = ")"), " {",
          i(scope),
        n("}"))
    case Stat.Module(p, ifaces, scope) =>
      s("module", r(p +: ifaces, pre = " (", sep = ", ", post = ")"), " {",
          i(scope),
        n("}"))
    case Stat.Field(ty) =>
      s("field ", ty)
    case Stat.Declare(ty, args) =>
      s("declare ", ty, " (", r(args, sep = ", "), ")")
    case Stat.Define(ty, args, block) =>
      s("define ", ty, " (", r(args, sep = ", "), ") {",
        i(block),
        n("}"))
  }

  implicit def showScope[S <: Scope]: Show[S] = Show { scope =>
    if (scope.isEmpty) s()
    else {
      val entries = scope.entries.toSeq.sortBy {
        case (n, stat) =>
          (stat.getClass.toString, sh"$n".toString)
      }
      val first +: rest =
        entries.map {
          case (name, stat) =>
            s(name, " = ", stat)
        }
      s(first, r(rest.map(n)))
    }
  }

  implicit val showBlock: Show[Block] = Show { entry =>
    def repr(b: Block) =
      s(b.name, ":", r(b.instrs.map(i(_))), i(b.termn))
    var shows = List.empty[Show.Result]
    entry.foreachBreadthFirst { b =>
      shows = repr(b) :: shows
    }
    val first :: last = shows.reverse
    s(first, r(last.map(n(_))))
  }

  implicit val showName: Show[Name] = Show {
    case Name.Local(id)             => s("%", id)
    case Name.Global(id)            => s("@", id)
    case Name.Nested(parent, child) => s(parent, "::", child)
  }

  implicit val showBranch: Show[Branch] = Show {
    case Branch(v, block) => s(v, ", ", block.name)
  }

  implicit val showLabeledType: Show[LabeledType] = Show {
    case LabeledType(ty, name) => s(ty, " ", name)
  }

  implicit val showInt: Show[Int] = Show { _.toString }
}
