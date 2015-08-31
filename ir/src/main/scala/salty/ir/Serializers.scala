package salty.ir

object Serializers {
  import salty.util.Serialize, Serialize.{Sequence => s}

  implicit val serializeType: Serialize[Type] = Serialize {
    case n: Name           => n
    case Type.Unit         => Tags.Type.Unit
    case Type.Null         => Tags.Type.Null
    case Type.Nothing      => Tags.Type.Nothing
    case Type.Bool         => Tags.Type.Bool
    case Type.I8           => Tags.Type.I8
    case Type.I16          => Tags.Type.I16
    case Type.I32          => Tags.Type.I32
    case Type.I64          => Tags.Type.I64
    case Type.F32          => Tags.Type.F32
    case Type.F64          => Tags.Type.F64
    case Type.Ptr(ty)      => s(Tags.Type.Ptr, ty)
    case Type.Array(ty, n) => s(Tags.Type.Array, ty, n)
    case Type.Slice(ty)    => s(Tags.Type.Slice, ty)
  }

  implicit val serializeInstr: Serialize[Instr] = Serialize {
    case e: Expr                  => e
    case Instr.Assign(name, expr) => s(Tags.Instr.Assign, name, expr)
  }

  implicit val serializeTermn: Serialize[Termn] = Serialize {
    case Termn.Out(value)                      => s(Tags.Termn.Out, value)
    case Termn.Return(value)                   => s(Tags.Termn.Return, value)
    case Termn.Throw(value)                    => s(Tags.Termn.Throw, value)
    case Termn.Jump(block)                     => s(Tags.Termn.Jump, block.name)
    case Termn.If(cond, thenp, elsep)          =>
      s(Tags.Termn.If, cond, thenp.name, elsep.name)
    case Termn.Switch(on, default, branches)   =>
      s(Tags.Termn.Switch, on, default.name, branches)
    case Termn.Try(body, catchopt, finallyopt) =>
      s(Tags.Termn.Try, body.name, catchopt.map(_.name), finallyopt.map(_.name))
  }

  implicit val serializeExpr: Serialize[Expr] = Serialize {
    case v: Val                        => v
    case Expr.Bin(op, left, right)     => s(Tags.Expr.Bin, op, left, right)
    case Expr.Conv(op, value, to)      => s(Tags.Expr.Conv, op, value, to)
    case Expr.Is(value, ty)            => s(Tags.Expr.Is, value, ty)
    case Expr.Alloc(name, elements)    => s(Tags.Expr.Alloc, name, elements)
    case Expr.Call(name, args)         => s(Tags.Expr.Call, name, args)
    case Expr.Phi(branches)            => s(Tags.Expr.Phi, branches)
    case Expr.Load(ptr)                => s(Tags.Expr.Load, ptr)
    case Expr.Store(ptr, value)        => s(Tags.Expr.Store, ptr, value)
    case Expr.Box(value, ty)           => s(Tags.Expr.Box, value, ty)
    case Expr.Unbox(value, ty)         => s(Tags.Expr.Unbox, value, ty)
    case Expr.Length(value)            => s(Tags.Expr.Length, value)
    case Expr.Catchpad                 => Tags.Expr.Catchpad
  }

  implicit val serializeBinOp: Serialize[BinOp] = Serialize {
    case BinOp.Add    => Tags.BinOp.Add
    case BinOp.Sub    => Tags.BinOp.Sub
    case BinOp.Mul    => Tags.BinOp.Mul
    case BinOp.Div    => Tags.BinOp.Div
    case BinOp.Mod    => Tags.BinOp.Mod
    case BinOp.Shl    => Tags.BinOp.Shl
    case BinOp.Lshr   => Tags.BinOp.Lshr
    case BinOp.Ashr   => Tags.BinOp.Ashr
    case BinOp.And    => Tags.BinOp.And
    case BinOp.Or     => Tags.BinOp.Or
    case BinOp.Xor    => Tags.BinOp.Xor
    case BinOp.Eq     => Tags.BinOp.Eq
    case BinOp.Equals => Tags.BinOp.Equals
    case BinOp.Neq    => Tags.BinOp.Neq
    case BinOp.Lt     => Tags.BinOp.Lt
    case BinOp.Lte    => Tags.BinOp.Lte
    case BinOp.Gt     => Tags.BinOp.Gt
    case BinOp.Gte    => Tags.BinOp.Gte
  }

  implicit val serializeConvOp: Serialize[ConvOp] = Serialize {
    case ConvOp.Trunc    => Tags.ConvOp.Trunc
    case ConvOp.Zext     => Tags.ConvOp.Zext
    case ConvOp.Sext     => Tags.ConvOp.Sext
    case ConvOp.Fptrunc  => Tags.ConvOp.Fptrunc
    case ConvOp.Fpext    => Tags.ConvOp.Fpext
    case ConvOp.Fptoui   => Tags.ConvOp.Fptoui
    case ConvOp.Fptosi   => Tags.ConvOp.Fptosi
    case ConvOp.Uitofp   => Tags.ConvOp.Uitofp
    case ConvOp.Sitofp   => Tags.ConvOp.Sitofp
    case ConvOp.Ptrtoint => Tags.ConvOp.Ptrtoint
    case ConvOp.Inttoptr => Tags.ConvOp.Inttoptr
    case ConvOp.Bitcast  => Tags.ConvOp.Bitcast
    case ConvOp.Cast     => Tags.ConvOp.Cast
  }

  implicit val serializeVal: Serialize[Val] = Serialize {
    case n: Name              => n
    case Val.Null             => Tags.Val.Null
    case Val.Unit             => Tags.Val.Unit
    case Val.This             => Tags.Val.This
    case Val.Bool(v)          => s(Tags.Val.Bool, v)
    case Val.Number(repr, ty) => s(Tags.Val.Number, repr, ty)
    case Val.Array(vs)        => s(Tags.Val.Array, vs)
    case Val.Slice(len, data) => s(Tags.Val.Slice, len, data)
    case Val.Elem(ptr, value) => s(Tags.Val.Elem, ptr, value)
    case Val.Class(ty)        => s(Tags.Val.Class, ty)
    case Val.Str(str)         => s(Tags.Val.Str, str)
  }

  implicit val serializeStat: Serialize[Stat] = Serialize {
    case Stat.Class(name, p, ifaces, body)  => s(Tags.Stat.Class, name, p, ifaces, body)
    case Stat.Interface(name, ifaces, body) => s(Tags.Stat.Interface, name, ifaces, body)
    case Stat.Module(name, p, ifaces, body) => s(Tags.Stat.Module, name, p, ifaces, body)
    case Stat.Var(name, ty)                 => s(Tags.Stat.Var, name, ty)
    case Stat.Declare(name, args, ty)       => s(Tags.Stat.Declare, name, args, ty)
    case Stat.Define(name, args, ty, block) => s(Tags.Stat.Define, name, args, ty, block)
  }

  implicit val serializeBlock: Serialize[Block] = Serialize { entry =>
    var blocks = List.empty[Block]
    entry.foreach { b =>
      blocks = b :: blocks
    }
    s(blocks.length,
      s(blocks.map { b => serializeName(b.name) }: _*),
      s(blocks.map { b => s(b.instrs, b.termn) }: _*))
  }

  implicit val serializeName: Serialize[Name] = Serialize {
    case Name.Local(id)             => s(Tags.Name.Local, id)
    case Name.Global(id)            => s(Tags.Name.Global, id)
    case Name.Nested(parent, child) => s(Tags.Name.Nested, parent, child)
  }

  implicit val serializeBranch: Serialize[Branch] = Serialize {
    case Branch(v, block) => s(v, block.name)
  }

  implicit val serializeLabeledType: Serialize[LabeledType] = Serialize {
    case LabeledType(name, ty) => s(name, ty)
  }
}
