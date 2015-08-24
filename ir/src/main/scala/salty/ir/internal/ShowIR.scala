package salty
package ir
package internal

object ShowIR {
  import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                                Repeat => r, Newline => n}

  implicit val showTree: Show[Tree] = Show {
    case t: Type         => t
    case t: Termn        => t
    case i: Instr        => i
    case s: Stat         => s
    case b: Branch       => b
    case b: Block        => b
    case lt: LabeledType => lt
    case lv: LabeledVal  => lv
  }

  implicit val showType: Show[Type] = Show {
    case n: Name        => n
    case Type.Unit      => "unit"
    case Type.Null      => "null"
    case Type.Nothing   => "nothing"
    case Type.Bool      => "bool"
    case Type.I(w)      => s("i", w.toString)
    case Type.F(w)      => s("f", w.toString)
    case Type.Ptr(ty)   => s(ty, "*")
    case Type.Array(ty) => s("[", ty, "]")
  }

  implicit val showInstr: Show[Instr] = Show {
    case e: Expr =>
      e
    case Instr.Assign(name, expr) =>
      s(name, " = ", expr)
  }

  implicit val showTermn: Show[Termn] = Show {
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
  }

  implicit val showExpr: Show[Expr] = Show {
    case v: Val =>
      v
    case Expr.Bin(op, left, right) =>
      s(op.toString.toLowerCase, " ", left, ", ", right)
    case Expr.Conv(op, value, to) =>
      s(op.toString.toLowerCase, " ", value, " to ", to)
    case Expr.Is(value, ty) =>
      s(value, " is ", ty)
    case Expr.Alloc(name) =>
      s("alloc ", name)
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
      s(value, " box ", ty)
    case Expr.Unbox(value, ty) =>
      s(value, " unbox ", ty)
  }

  implicit val showVal: Show[Val] = Show {
    case n: Name              => n
    case Val.Null             => "null"
    case Val.Unit             => "unit"
    case Val.This             => "this"
    case Val.Bool(v)          => v.toString
    case Val.Number(repr, ty) => s(repr, (ty: Type))
    case Val.Array(vs)        => s("[", r(vs, sep = ", "), "]")
  }

  implicit val showStat: Show[Stat] = Show {
    case Stat.Class(name, p, ifaces, body) =>
      s("class ", name, ": ", r(p +: ifaces, sep = ", "), " {",
          r(body.map(i(_))),
        n("}"))
    case Stat.Interface(name, ifaces, body) =>
      s("interface ", name, r(ifaces, pre = ": ", sep = ", "), " {",
          r(body.map(i(_))),
        n("}"))
    case Stat.Module(name, p, ifaces, body) =>
      s("module ", name, ": ", r(p +: ifaces, sep = ", "), " {",
          r(body.map(i(_))),
        n("}"))
    case Stat.Var(name, ty) =>
      s("var ", name, ": ", ty)
    case Stat.Declare(name, args, ty) =>
      s("declare ", name, "(", r(args, sep = ", "), "): ", ty)
    case Stat.Define(name, args, ty, block) =>
      s("define ", name, "(", r(args, sep = ", "), "): ", ty, " = {",
        n(block),
        n("}"))
  }

  implicit val showBlock: Show[Block] = Show { entry =>
    var shows = List.empty[Show.Result]
    entry.foreachBreadthFirst { b =>
      shows = s(b.name, ":", r(b.instrs.map(i(_))), i(b.termn)) :: shows
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
    case LabeledType(name, ty) => s(name, ": ", ty)
  }

  implicit val showLabeledVal: Show[LabeledVal] = Show {
    case LabeledVal(name, value) => s(name, " = ", value)
  }

  implicit val showInt: Show[Int] = Show { _.toString }
}
