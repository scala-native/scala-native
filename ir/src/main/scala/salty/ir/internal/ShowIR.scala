package salty
package ir
package internal

object ShowIR {
  import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                                Repeat => r, Newline => n}

  implicit val showTree: Show[Tree] = Show {
    case t: Type  => t
    case i: Instr => i
    case s: Stat  => s
    case b: Branch => b
    case lt: LabeledType => lt
    case lv: LabeledVal => lv
  }

  implicit val showType: Show[Type] = Show {
    case n: Name                => n
    case Type.Unit              => "unit"
    case Type.Null              => "null"
    case Type.Nothing           => "nothing"
    case Type.Bool              => "bool"
    case Type.I(w)              => s("i", w.toString)
    case Type.F(w)              => s("f", w.toString)
    case Type.Ptr(ty)           => s(ty, "*")
    case Type.Array(ty)         => s("array { ", ty, "}")
    case Type.FixedArray(ty, n) => s("farray { ", ty, ", ", n, "}")
    case Type.Struct(tys)       => s("struct {", r(tys, sep = ", "), "}")
  }

  implicit val showInstr: Show[Instr] = Show {
    case e: Expr =>
      e
    case Instr.Assign(name, expr) =>
      s(name, " = ", expr)
    case Instr.While(cond, body) =>
      s("while ", cond, " do ", body)
    case Instr.DoWhile(body, cond) =>
      s("do ", body, " while ", cond)
    case Instr.Label(name) =>
      u(s(name, ":"))
    case Instr.Jump(name) =>
      s("jump ", name)
    case Instr.Return(value) =>
      s("return ", value)
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
    case Expr.New(name) =>
      s("new ", name)
    case Expr.Call(name, args) =>
      s("call ", name, "(", r(args, sep = ", "), ")")
    case Expr.Phi(names) =>
      s("phi(", r(names, sep = ", "), ")")
    case Expr.If(cond, thenp, elsep) =>
      s("if ", cond, " then ", thenp, " else ", elsep)
    case Expr.Switch(on, cases, default) =>
      s("switch ", on, " { ",
          r(cases.map(i(_))),
          i(s("case _ => ", default)),
        n(" }"))

    case Expr.Block(instrs, value) =>
      s("{",
          r(instrs.map(i(_))),
          i(value),
        n("}"))
  }

  implicit val showVal: Show[Val] = Show {
    case n: Name               => n
    case Val.Null              => "null"
    case Val.Unit              => "unit"
    case Val.This              => "this"
    case Val.Bool(v)           => v.toString
    case Val.Number(repr, ty)  => s(repr, (ty: Type))
    case Val.Struct(vs)        => s("struct {", r(vs, sep = ", "), "}")
    case Val.Array(vs)         => s("array {", r(vs, sep = ", "), "}")
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
    case Stat.Struct(name, body) =>
      s("struct ", name, " {",
          r(body.map(i(_))),
        n("}"))
    case Stat.Const(name, ty, init) =>
      s("const ", name, ": ", ty, " = ", init)
    case Stat.Var(name, ty, init) =>
      s("var ", name, ": ", ty, " = ", init)
    case Stat.Decl(name, args, ty) =>
      s("decl ", name, "(", r(args, sep = ", "), "): ", ty)
    case Stat.Def(name, args, ty, body) =>
      s("def ", name, "(", r(args, sep = ", "), "): ", ty, " = ", body)
  }

  implicit val showName: Show[Name] = Show {
    case Name.Local(id)             => s("%", id)
    case Name.Global(id)            => s("@", id)
    case Name.Nested(parent, child) => s(parent, "::", child)
  }

  implicit val showBranch: Show[Branch] = Show {
    case Branch(v, expr) => s("case ", v, " => ", expr)
  }

  implicit val showLabeledType: Show[LabeledType] = Show {
    case LabeledType(name, ty) => s(name, ": ", ty)
  }

  implicit val showLabeledVal: Show[LabeledVal] = Show {
    case LabeledVal(name, value) => s(name, " = ", value)
  }
}
