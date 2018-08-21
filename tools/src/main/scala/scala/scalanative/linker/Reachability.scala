package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.codegen.Metadata
import scalanative.util.Stats

class Reachability(defns: Seq[Defn]) {
  val env = {
    val out = mutable.Map.empty[Global, Defn]
    defns.foreach { defn =>
      out(defn.name) = defn
    }
    out
  }
  val enqueued = mutable.Set.empty[Global]
  val todo     = mutable.Stack.empty[Global]
  val done     = mutable.Map.empty[Global, Defn]
  val infos    = mutable.Map.empty[Global, Info]
  val stack    = mutable.Stack.empty[Global]
  val links    = mutable.Set.empty[String]
  val dynsigs  = mutable.Set.empty[String]

  def result(): Seq[Defn] =
    done.values.toSeq

  def process(): Unit =
    while (todo.nonEmpty) {
      val name = todo.pop()
      if (!done.contains(name)) {
        stack.push(name)
        reachDefn(env(name))
        stack.pop()
      }
    }

  def reachDefn(defn: Defn): Unit = {
    defn match {
      case defn: Defn.Var =>
        reachVar(defn)
      case defn: Defn.Const =>
        reachConst(defn)
      case defn: Defn.Declare =>
        reachDeclare(defn)
      case defn: Defn.Define =>
        reachDefine(defn)
      case defn: Defn.Struct =>
        reachStruct(defn)
      case defn: Defn.Trait =>
        reachTrait(defn)
      case defn: Defn.Class =>
        reachClass(defn)
      case defn: Defn.Module =>
        reachModule(defn)
    }
    done(defn.name) = defn
  }

  def reachEntry(name: Global): Unit = {
    println(s"entry " + name.show)
    if (!name.isTop) {
      reachEntry(name.top)
    }
    reachGlobalNow(name)
    infos.get(name) match {
      case Some(cls: Class) if cls.isModule =>
        val init = cls.name member "init"
        if (env.contains(init)) {
          reachGlobal(init)
        }
      case _ =>
        ()
    }
  }

  def reachGlobal(name: Global): Unit =
    if (!enqueued.contains(name) && name.ne(Global.None)) {
      enqueued += name
      todo.push(name)
    }

  def reachGlobalNow(name: Global): Unit =
    if (done.contains(name)) {
      ()
    } else if (!stack.contains(name)) {
      enqueued += name
      stack.push(name)
      reachDefn(env(name))
      stack.pop()
    } else {
      val lines = (s"cyclic reference error to ${name.show}:" +:
        stack.map(el => s"* ${el.show}"))
      val msg = lines.mkString("\n")
      throw new Exception(msg)
    }

  def newInfo(info: Info): Unit = {
    infos(info.name) = info
    info match {
      case info: MemberInfo =>
        info.owner.members += info
      case info: Class =>
        val calls = mutable.Set.empty[String]
        def loopParent(parentInfo: Class): Unit = {
          calls ++= parentInfo.calls
          parentInfo.subclasses += info
          parentInfo.parent.foreach(loopParent)
          parentInfo.traits.foreach(loopTraits)
        }
        def loopTraits(traitInfo: Trait): Unit = {
          calls ++= traitInfo.calls
          traitInfo.implementors += info
          traitInfo.traits.foreach(loopTraits)
        }
        info.parent.foreach(loopParent)
        info.traits.foreach(loopTraits)
        calls.foreach { sig =>
          resolve(info, sig).foreach(reachGlobal)
        }
      case _ =>
        ()
    }
  }

  def scopeInfo(name: Global): ScopeInfo = {
    reachGlobalNow(name)
    infos(name).asInstanceOf[ScopeInfo]
  }

  def classInfo(name: Global): Class = {
    reachGlobalNow(name)
    infos(name).asInstanceOf[Class]
  }

  def traitInfo(name: Global): Trait = {
    reachGlobalNow(name)
    infos(name).asInstanceOf[Trait]
  }

  def methodInfo(name: Global): Method = {
    reachGlobalNow(name)
    infos(name).asInstanceOf[Method]
  }

  def fieldInfo(name: Global): Field = {
    reachGlobalNow(name)
    infos(name).asInstanceOf[Field]
  }

  def reachVar(defn: Defn.Var): Unit = {
    val Defn.Var(attrs, name, ty, rhs) = defn
    newInfo(new Field(scopeInfo(name.top), name, isConst = false))
    reachAttrs(attrs)
    reachType(ty)
    reachVal(rhs)
  }

  def reachConst(defn: Defn.Const): Unit = {
    val Defn.Const(attrs, name, ty, rhs) = defn
    newInfo(new Field(scopeInfo(name.top), name, isConst = true))
    reachAttrs(attrs)
    reachType(ty)
    reachVal(rhs)
  }

  def reachDeclare(defn: Defn.Declare): Unit = {
    val Defn.Declare(attrs, name, sig) = defn
    newInfo(new Method(scopeInfo(name.top), name, isConcrete = false))
    reachAttrs(attrs)
    reachType(sig)
  }

  def reachDefine(defn: Defn.Define): Unit = {
    val Defn.Define(attrs, name, sig, insts) = defn
    newInfo(new Method(scopeInfo(name.top), name, isConcrete = true))
    reachAttrs(attrs)
    reachType(sig)
    reachInsts(insts)
  }

  def reachStruct(defn: Defn.Struct): Unit = {
    val Defn.Struct(attrs, _, tys) = defn
    reachAttrs(attrs)
    tys.foreach(reachType)
  }

  def reachTrait(defn: Defn.Trait): Unit = {
    val Defn.Trait(attrs, name, traits) = defn
    newInfo(new Trait(name, traits.map(traitInfo)))
    reachAttrs(attrs)
  }

  def reachClass(defn: Defn.Class): Unit = {
    val Defn.Class(attrs, name, parent, traits) = defn
    newInfo(
      new Class(name,
                parent.map(classInfo),
                traits.map(traitInfo),
                isModule = false))
    reachAttrs(attrs)
  }

  def reachModule(defn: Defn.Module): Unit = {
    val Defn.Module(attrs, name, parent, traits) = defn
    newInfo(
      new Class(name,
                parent.map(classInfo),
                traits.map(traitInfo),
                isModule = true))
    reachAttrs(attrs)
  }

  def reachAttrs(attrs: Attrs): Unit =
    links ++= attrs.links.map(_.name)

  def reachType(ty: Type): Unit = ty match {
    case Type.Array(ty, n) =>
      reachType(ty)
    case Type.Function(args, ty) =>
      args.foreach(reachType)
      reachType(ty)
    case Type.Struct(n, tys) =>
      reachGlobal(n)
      tys.foreach(reachType)
    case ty: Type.Named =>
      reachGlobal(ty.name)
    case _ =>
      ()
  }

  def reachVal(value: Val): Unit = value match {
    case Val.Zero(ty) =>
      reachType(ty)
    case Val.Undef(ty) =>
      reachType(ty)
    case Val.Struct(n, values) =>
      reachGlobal(n)
      values.foreach(reachVal)
    case Val.Array(ty, values) =>
      reachType(ty)
      values.foreach(reachVal)
    case Val.Local(n, ty) =>
      reachType(ty)
    case Val.Global(n, ty) =>
      reachGlobal(n); reachType(ty)
    case Val.Const(v) =>
      reachVal(v)
    case _ =>
      ()
  }

  def reachInsts(insts: Seq[Inst]): Unit =
    insts.foreach(reachInst)

  def reachInst(inst: Inst): Unit = inst match {
    case Inst.None | Inst.Unreachable =>
      ()
    case Inst.Label(n, params) =>
      params.foreach(p => reachType(p.ty))
    case Inst.Let(n, op, unwind) =>
      reachOp(op)
      reachNext(unwind)
    case Inst.Ret(v) =>
      reachVal(v)
    case Inst.Jump(next) =>
      reachNext(next)
    case Inst.If(v, thenp, elsep) =>
      reachVal(v)
      reachNext(thenp)
      reachNext(elsep)
    case Inst.Switch(v, default, cases) =>
      reachVal(v)
      reachNext(default)
      cases.foreach(reachNext)
    case Inst.Throw(v, unwind) =>
      reachVal(v)
      reachNext(unwind)
  }

  def reachOp(op: Op): Unit = op match {
    case Op.Call(ty, ptrv, argvs) =>
      reachType(ty)
      reachVal(ptrv)
      argvs.foreach(reachVal)
    case Op.Load(ty, ptrv, isVolatile) =>
      reachType(ty)
      reachVal(ptrv)
    case Op.Store(ty, ptrv, v, isVolatile) =>
      reachType(ty)
      reachVal(ptrv)
      reachVal(v)
    case Op.Elem(ty, ptrv, indexvs) =>
      reachType(ty)
      reachVal(ptrv)
      indexvs.foreach(reachVal)
    case Op.Extract(aggrv, indexvs) =>
      reachVal(aggrv)
    case Op.Insert(aggrv, v, indexvs) =>
      reachVal(aggrv)
      reachVal(v)
    case Op.Stackalloc(ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Bin(bin, ty, lv, rv) =>
      reachType(ty)
      reachVal(lv)
      reachVal(rv)
    case Op.Comp(comp, ty, lv, rv) =>
      reachType(ty)
      reachVal(lv)
      reachVal(rv)
    case Op.Conv(conv, ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Select(v1, v2, v3) =>
      reachVal(v1)
      reachVal(v2)
      reachVal(v3)

    case Op.Classalloc(n) =>
      reachGlobal(n)
    case Op.Field(v, n) =>
      reachVal(v)
      reachGlobal(n)
    case Op.Method(obj, name) =>
      reachVal(obj)
      reachGlobal(name)
      targets(obj.ty, name).foreach(reachGlobal)
    case Op.Dynmethod(obj, sig) =>
      reachVal(obj)
      dynsigs += sig
      dyntargets(obj.ty, sig).foreach(reachGlobal)
    case Op.Module(n) =>
      reachGlobal(n)
      val init = n member "init"
      if (env.contains(init)) {
        reachGlobal(init)
      }
    case Op.As(ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Is(ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Copy(v) =>
      reachVal(v)
    case Op.Sizeof(ty) =>
      reachType(ty)
    case Op.Closure(ty, fun, captures) =>
      reachType(ty)
      reachVal(fun)
      captures.foreach(reachVal)
    case Op.Box(code, obj) =>
      reachVal(obj)
    case Op.Unbox(code, obj) =>
      reachVal(obj)
  }

  def reachNext(next: Next): Unit = next match {
    case Next.Label(_, args) =>
      args.foreach(reachVal)
    case _ =>
      ()
  }

  def targets(ty: Type, name: Global): mutable.Set[Global] =
    targets(ty, name.id)

  def targets(ty: Type, sig: String): mutable.Set[Global] = {
    val out = mutable.Set.empty[Global]

    def add(cls: Class): Unit =
      resolve(cls, sig).foreach { impl =>
        out += impl
      }

    ty match {
      case Type.Class(name) =>
        val cls = classInfo(name)
        cls.calls += sig
        add(cls)
        cls.subclasses.foreach(add)
      case Type.Trait(name) =>
        val trt = traitInfo(name)
        trt.calls += sig
        trt.implementors.foreach(add)
      case _ =>
        ()
    }

    out
  }

  def resolve(cls: Class, sig: String): Option[Global] = {
    def impl(cls: Class, sig: String): Option[Global] = {
      val tryMember = cls.name member sig
      if (env.contains(tryMember)) {
        Some(tryMember)
      } else {
        cls.parent.flatMap(impl(_, sig))
      }
    }

    sig match {
      // We short-circuit scala_== and scala_## to immeditately point to the
      // equals and hashCode implementation for the reference types to avoid
      // double virtual dispatch overhead.
      case Rt.ScalaEqualsSig =>
        val scalaImpl = impl(cls, Rt.ScalaEqualsSig).get
        val javaImpl  = impl(cls, Rt.JavaEqualsSig).get
        if (javaImpl.top != Rt.Object.name &&
            scalaImpl.top == Rt.Object.name) {
          Some(javaImpl)
        } else {
          Some(scalaImpl)
        }
      case Rt.ScalaHashCodeSig =>
        val scalaImpl = impl(cls, Rt.ScalaHashCodeSig).get
        val javaImpl  = impl(cls, Rt.JavaHashCodeSig).get
        if (javaImpl.top != Rt.Object.name &&
            scalaImpl.top == Rt.Object.name) {
          Some(javaImpl)
        } else {
          Some(scalaImpl)
        }
      case _ =>
        impl(cls, sig)
    }
  }

  def dyntargets(ty: Type, sig: String): Seq[Global] =
    ???
}

object Reachability {
  def apply(entries: Seq[Global],
            defns: Seq[Defn],
            dyns: Seq[String]): Seq[Defn] = Stats.in(Stats.time("reachability") {
    val reachability = new Reachability(defns)
    entries.foreach(reachability.reachEntry)
    reachability.process()
    val res = reachability.result()
    val origClassCount = defns.count {
      case _: nir.Defn.Class | _: nir.Defn.Module | _: nir.Defn.Trait => true
      case _                                                          => false
    }
    val origMemberCount = defns.count {
      case _: nir.Defn.Define | _: nir.Defn.Declare | _: nir.Defn.Var | _: nir.Defn.Const => true
      case _ => false
    }
    val classCount = res.count {
      case _: nir.Defn.Class | _: nir.Defn.Module | _: nir.Defn.Trait => true
      case _                                                          => false
    }
    val memberCount = res.count {
      case _: nir.Defn.Define | _: nir.Defn.Declare | _: nir.Defn.Var | _: nir.Defn.Const => true
      case _ => false
    }
    val resNames = res.map(_.name).toSet
    val excluded = mutable.Set.empty[Global]
    defns.foreach { defn =>
      if (!resNames.contains(defn.name)) {
        excluded += defn.name
      }
    }
    excluded.toList.map(_.show).sorted.foreach(println)
    println(s"Reached ${classCount} classes and ${memberCount} members")
    println(s"(down from ${origClassCount} classes and ${origMemberCount} members)")
    res
  })
}
