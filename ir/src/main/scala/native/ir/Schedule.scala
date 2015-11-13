package native
package ir

import scala.collection.mutable

final case class Schedule(defns: Seq[Schedule.Defn]) {
  override def toString = defns.mkString("\n")

}
object Schedule {
  final case class Op(index: Int, node: Node, var ty: Type, var args: Seq[Value])

  sealed abstract class Defn { def node: Node }
  object Defn {
    final case class Type(node: Node, tys: Seq[Schedule.Type], members: Seq[Defn]) extends Defn
    final case class Func(node: Node, retty: Schedule.Type,
                          paramtys: Seq[Schedule.Type], ops: Seq[Op]) extends Defn
    final case class State(node: Node, init: Value) extends Defn
  }

  sealed abstract class Type
  object Type {
    final case object None extends Type
    final case object Unit extends Type
    final case class Defn(node: Node) extends Type
    final case class Prim(node: Node) extends Type
    final case class Ptr(ty: Type) extends Type
    final case class Array(ty: Type, n: Int) extends Type
    final case class Func(ret: Type, args: Seq[Type]) extends Type
    final case class ArrayClass(ty: Type) extends Type
  }

  sealed abstract class Value { def ty = typedvalue(this) }
  object Value {
    final case class Op(op: Schedule.Op) extends Value
    final case class Struct(tynode: Node, values: Seq[Value]) extends Value
    final case class Const(node: Node) extends Value
    final case class Defn(defn: Node) extends Value
    final case class Param(node: Node) extends Value
  }

  private class CollectDefns extends native.ir.Pass {
    val defns = mutable.ArrayBuffer[Node]()
    def onNode(n: Node): Unit =
      n.desc match {
        case Desc.Empty | _: Desc.Prim =>
          ()
        case _: Desc.Defn =>
          defns += n
        case _ =>
          ()
      }
  }

  private def typedconst(n: Node): Type = n.desc match {
    case Desc.Lit.Zero =>
      val Lit.Zero(of) = n
      toType(of)
    case Desc.Lit.I8(_)  => Type.Prim(Prim.I32)
    case Desc.Lit.I16(_)  => Type.Prim(Prim.I32)
    case Desc.Lit.I32(_)  => Type.Prim(Prim.I32)
    case Desc.Lit.I64(_)  => Type.Prim(Prim.I32)
    case Desc.Lit.Unit    => Type.Prim(Prim.Unit)
    case Desc.Lit.Null    => Type.Ptr(Type.Prim(Prim.I8))
  }

  private def typeddefn(n: Node): Type = n match {
    case _: ir.Prim =>
      Type.Prim(n)
    case ir.Defn.Ptr(ty) =>
      Type.Ptr(toType(ty))
    case ir.Defn.Global(ty, _) =>
      Type.Ptr(toType(ty))
    case ir.Defn.Struct(_)       |
         ir.Defn.Class(_, _)     |
         ir.Defn.Interface(_)    |
         ir.Defn.Module(_, _, _) =>
      Type.Defn(n)
    case ir.Defn.ArrayClass(n) =>
      Type.ArrayClass(toType(n))
    case ir.Defn.Declare(ret, params) =>
      val paramtypes = params.map { case Param(ty) => toType(ty) }
      Type.Ptr(Type.Func(toType(ret), paramtypes))
    case ir.Defn.Define(ret, params, _) =>
      val paramtypes = params.map { case Param(ty) => toType(ty) }
      Type.Ptr(Type.Func(toType(ret), paramtypes))
    case ir.Defn.Method(ret, params, _, _) =>
      val paramtypes = params.map { case Param(ty) => toType(ty) }
      Type.Ptr(Type.Func(toType(ret), paramtypes))
    case ir.Defn.Field(ty, _) =>
      toType(ty)
  }

  private def typedvalue(v: Value): Type = v match {
    case Value.Struct(ty, _)    => typeddefn(ty)
    case Value.Const(const)     => typedconst(const)
    case Value.Defn(defn)       => typeddefn(defn)
    case Value.Op(op)           => op.ty
    case Value.Param(Param(ty)) => typeddefn(ty)
  }

  private def toType(n: Node): Type = n match {
    case _: ir.Prim =>
      Type.Prim(n)
    case ir.Defn.Ptr(to) =>
      Type.Ptr(toType(to))
    case ir.Defn.Function(ret, args) =>
      Type.Func(toType(ret), args.map(toType))
    case ir.Defn.Struct(_)       |
         ir.Defn.Class(_, _)     |
         ir.Defn.Module(_, _, _) =>
      Type.Defn(n)
    case ir.Defn.ArrayClass(n) =>
      Type.ArrayClass(toType(n))
  }

  private def constvalue(n: Node): Value = n match {
    case Lit.Struct(ty, deps) => Value.Struct(ty, deps.map(constvalue))
    case _ if n.desc.isInstanceOf[Desc.Lit] => Value.Const(n)
    case _ if n.desc.isInstanceOf[Desc.Defn] => Value.Defn(n)
  }

  private def scheduleOps(n: Node): Seq[Op] = {
    def opdeps(n: Node): Seq[Node] = n match {
      case Lit.Struct(_, deps)                => deps.flatMap(opdeps)
      case _ if n.desc.isInstanceOf[Desc.Lit] => Seq()
      case _                                  => Seq(n)
    }

    def order(n: Node, done: Seq[Node]): Seq[Node] =
      if (done.contains(n) || n.desc.isInstanceOf[Desc.Defn])
        done
      else {
        val depsbuf = mutable.ArrayBuffer[Node]()
        n.deps.foreach { dep =>
          if (dep.isDefn || (dep eq Empty))
            ()
          else if (dep.isEf)
            depsbuf += dep.dep
          else {
            val deps = opdeps(dep.dep)
            depsbuf ++= deps
          }
        }
        var dn = done
        depsbuf.foreach { n =>
          dn = order(n, dn)
        }
        if ((n.desc eq Desc.End) || (n.desc eq Desc.Param))
          dn
        else
          dn :+ n
      }

    val ordered = order(n, Seq())
    val ops = ordered.zipWithIndex.map { case (node, index) =>
      Op(index, node, Type.None, Seq())
    }

    def argvalue(n: Node): Value = n match {
      case Lit.Struct(ty, deps) => Value.Struct(ty, deps.map(argvalue))
      case _ if n.desc.isInstanceOf[Desc.Lit] => Value.Const(n)
      case _ if n.desc.isInstanceOf[Desc.Defn] => Value.Defn(n)
      case Param(_) => Value.Param(n)
      case _ => Value.Op(ops(ordered.indexOf(n)))
    }

    def typedop(op: Op): Unit = op.node match {
      case Param(ty) =>
        op.ty = toType(ty)
      case Call(_, fun, args) =>
        val funvalue = argvalue(fun)
        val argvalues = args.map(argvalue)
        val Type.Ptr(Type.Func(ret, _)) = typedvalue(funvalue)
        op.ty = ret
        op.args = funvalue +: argvalues
      case StructElem(struct, idx) =>
        val structvalue = argvalue(struct)
        val Desc.Lit.I32(n) = idx.desc
        val Type.Defn(ir.Defn.Struct(elemdefns)) = typedvalue(structvalue)
        op.ty = toType(elemdefns(n))
        op.args = Seq(structvalue, argvalue(idx))
      case Elem(ptr, Seq(idx)) =>
        val ptrvalue = argvalue(ptr)
        op.ty = typedvalue(ptrvalue)
        op.args = Seq(ptrvalue, argvalue(idx))
      case Elem(ptr, Seq(idx1, idx2)) =>
        val ptrvalue = argvalue(ptr)
        val Desc.Lit.I32(n) = idx2.desc
        val Type.Ptr(Type.Defn(ir.Defn.Struct(elemdefns))) = typedvalue(ptrvalue)
        op.ty = Type.Ptr(toType(elemdefns(n)))
        op.args = Seq(ptrvalue, argvalue(idx1), argvalue(idx2))
      case Load(_, ptr) =>
        val ptrvalue = argvalue(ptr)
        val Type.Ptr(ty) = typedvalue(ptrvalue)
        op.ty = ty
        op.args = Seq(ptrvalue)
      case Return(_, _, value) =>
        val retvalue = argvalue(value)
        op.ty = typedvalue(retvalue)
        op.args = Seq(retvalue)
      case Eq(left, right) =>
        val leftvalue = argvalue(left)
        val rightvalue = argvalue(right)
        op.ty = Type.Prim(Prim.Bool)
        op.args = Seq(leftvalue, rightvalue)
      case If(_, cond) =>
        val condvalue = argvalue(cond)
        op.args = Seq(condvalue)
      case CaseTrue(_) | CaseFalse(_) =>
        ()
      case Alloc(ty) =>
        op.ty = Type.Ptr(toType(ty))
      case Store(_, ptr, value) =>
        val ptrvalue = argvalue(ptr)
        val newvalue = argvalue(value)
        op.args = Seq(ptrvalue, newvalue)
      case Bitcast(v, ty) =>
        op.ty = toType(ty)
        op.args = Seq(argvalue(v))
      case Ptrtoint(v, ty) =>
        op.ty = toType(ty)
        op.args = Seq(argvalue(v))
      case MethodElem(_, instance, method) =>
        val ir.Defn.Method(ret, params, _, _) = method
        val paramtys = params.map { case Param(ty) => toType(ty) }
        op.ty   = Type.Ptr(Type.Func(toType(ret), paramtys))
        op.args = Seq(argvalue(method), argvalue(instance))
      case FieldElem(_, instance, field) =>
        val ir.Defn.Field(ty, _) = field
        op.ty = Type.Ptr(toType(ty))
        op.args = Seq(argvalue(field), argvalue(instance))
      case ClassAlloc(_, cls) =>
        op.ty = toType(cls)
        op.args = Seq(argvalue(cls))
    }

    ops.foreach { op =>
      typedop(op)
    }
    ops
  }

  def membersof(n: Node): Seq[Defn] = n.uses.collect {
    case Use(f @ ir.Defn.Field(ty, _)) =>
      Defn.State(f, Value.Const(Lit.Zero(ty)))
    case Use(m @ ir.Defn.Method(ret, params, end, _)) =>
      val retty = toType(ret)
      val argtys = params.map { case Param(ty) => toType(ty) }
      println(s"scheduling ops for $n")
      Defn.Func(m, retty, argtys, scheduleOps(end))
  }.toSeq

  def apply(node: Node): Schedule = {
    val collectDefns = new CollectDefns
    Pass.run(collectDefns, node)
    val defns: Seq[Defn] = collectDefns.defns.collect {
      case n @ ir.Defn.Global(ty, rhs) =>
        Defn.State(n, constvalue(rhs))
      case n @ ir.Defn.Constant(ty, rhs) =>
        Defn.State(n, constvalue(rhs))
      case n @ ir.Defn.Define(ret, params, end) =>
        val retty = toType(ret)
        val argtys = params.map { case Param(ty) => toType(ty) }
        println(s"scheduling ops for $n")
        Defn.Func(n, retty, argtys, scheduleOps(end))
      case n @ ir.Defn.Declare(ret, params) =>
        val retty = toType(ret)
        val argtys = params.map { case Param(ty) => toType(ty) }
        Defn.Func(n, retty, argtys, Seq())
      case n @ ir.Defn.Struct(fields) =>
        val tys = fields.map(toType)
        Defn.Type(n, tys, Seq())
      case n @ ir.Defn.Class(Empty, ifaces) =>
        val tys = ifaces.map(toType)
        Defn.Type(n, tys, membersof(n))
      case n @ ir.Defn.Class(parent, ifaces) =>
        val tys = (parent +: ifaces).map(toType)
        Defn.Type(n, tys, membersof(n))
      case n @ ir.Defn.Interface(ifaces) =>
        val tys = ifaces.map(toType)
        Defn.Type(n, tys, membersof(n))
      case n @ ir.Defn.Module(parent, ifaces, _) =>
        val tys = (parent +: ifaces).map(toType)
        Defn.Type(n, tys, membersof(n))
      case n @ ir.Defn.Extern() =>
        throw new Exception(s"can't schedule graph with unlinked extern ${n.name}")
    }
    Schedule(defns)
  }
}

