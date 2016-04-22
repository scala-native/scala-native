package scala.scalanative
package compiler
package pass

import compiler.analysis.ClassHierarchy
import nir._

/** Lowers modules into module classes with singleton
 *  instance stored in a global variable that is accessed
 *  through a dedicated accessor function.
 *
 *  For example a dynamic module with members:
 *
 *      module $name : $parent, .. $ifaces {
 *        ..$members
 *      }
 *
 *  Translates to:
 *
 *      class $name : $parent, .. $ifaces {
 *        .. $members
 *      }
 *
 *      var ${name}_instance: class-value $name =
 *        class-value $name { .. zero[$fldty] }
 *
 *      var ${name}_needs_init: bool = true
 *
 *      def ${name}_ensure_init: () => void {
 *        %entry:
 *          %init = load[bool] ${name}_needs_init
 *          if %init then %thenp else %elsep
 *        %thenp:
 *          call ${name}_init(${name}_instance)
 *          store[bool] ${name}_needs_init, false
 *          ret
 *        %elsep:
 *          ret
 *      }
 *
 *  If a module is static (the one without init method, that was either
 *  eliminated by earlier passes or not present in the first place),
 *  the accessor is not emitted.
 *
 *  If a module is external (used for interop with C code) then all
 *  the members are hoisted outside as LLVM declarations, removing
 *  the module prefix in the name.
 *
 *  Eliminates:
 *  - Type.Module
 *  - Op.Module
 *  - Defn.Module
 */
class ModuleLowering(implicit chg: ClassHierarchy.Graph, fresh: Fresh) extends Pass {
  private def stripName(n: Global): Global =
    new Global(n.parts.tail, n.isType)

  private def hoist(defns: Seq[Defn]): (Seq[Defn], Seq[Defn]) = {
    def isExternal(defn: Defn): Boolean =
      defn.attrs.exists(_ == Attr.External)
    val hoisted = defns.collect {
      case defn: Defn.Declare if isExternal(defn) =>
        defn.copy(name = stripName(defn.name),
                  attrs = defn.attrs.filterNot(_ == Attr.External))
      case defn: Defn.Const if isExternal(defn) =>
        defn.copy(name = stripName(defn.name))
      case defn: Defn.Var if isExternal(defn) =>
        defn.copy(name = stripName(defn.name))
    }
    val rest = defns.filterNot(isExternal)

    (hoisted, rest)
  }

  override def preDefn = {
    case Defn.Module(attrs, name, parent, ifaces, members) =>
      val cls             = chg.nodes(name).asInstanceOf[ClassHierarchy.Class]
      val (hoisted, rest) = hoist(members)
      val clsDefn         = Defn.Class(attrs, name, parent, ifaces, rest)

      val instanceVal = Val.ClassValue(name, cls.fields.map(fld => Val.Zero(fld.ty)))
      val instance    = Defn.Var(Seq(), name + "instance", Type.ClassValue(name), instanceVal)
      val instanceRef = Val.Global(name + "instance", Type.Ptr(Type.ClassValue(name)))

      val accessor =
        if (isStaticModule(name))
          Seq()
        else {
          val needsInitName = name ++ Seq("needs", "init")
          val needsInit     = Defn.Var(Seq(), needsInitName, Type.Bool, Val.True)
          val needsInitRef  = Val.Global(needsInitName, Type.Ptr(Type.Bool))

          val ctorSig = Type.Function(Seq(Type.Class(name)), Type.Unit)
          val ctorRef = Val.Global(name + "init", Type.Ptr(ctorSig))

          val entry = fresh()
          val thenp = fresh()
          val elsep = fresh()
          val init  = Val.Local(fresh(), Type.Bool)
          val cast  = Val.Local(fresh(), Type.Class(name))

          val ensureInit =
            Defn.Define(Seq(), name + "ensure" + "init", Type.Function(Seq(), Type.Void), Seq(
              Block(entry, Seq(),
                Seq(
                  Inst(init.name, Op.Load(Type.Bool, needsInitRef))
                ),
                Cf.If(init, Next(thenp), Next(elsep))),
              Block(thenp, Seq(),
                Seq(
                  Inst(cast.name, Op.Conv(Conv.Bitcast, Type.Class(name), instanceRef)),
                  Inst(Op.Call(ctorSig, ctorRef, Seq(cast))),
                  Inst(Op.Store(Type.Bool, needsInitRef, Val.False))
                ),
                Cf.Ret(Val.None)),
              Block(elsep, Seq(),
                Seq(),
                Cf.Ret(Val.None))))

          Seq(needsInit, ensureInit)
        }

      Seq(clsDefn, instance) ++ accessor ++ hoisted
  }

  override def preInst = {
    case Inst(n, Op.Module(name)) =>
      val ensureInit =
        if (isStaticModule(name)) None
        else {
          val ensureInitTy  = Type.Function(Seq(), Type.Void)
          val ensureInitRef = Val.Global(name + "ensure" + "init", Type.Ptr(ensureInitTy))

          Some(Inst(Op.Call(ensureInitTy, ensureInitRef, Seq())))
        }

      val instanceRef = Val.Global(name + "instance", Type.Ptr(Type.ClassValue(name)))

      ensureInit ++: Seq(
        Inst(n, Op.Conv(Conv.Bitcast, Type.Class(name), instanceRef))
      )
  }

  override def preType = {
    case Type.Module(n) => Type.Class(n)
  }

  override def preVal = {
    case Val.Global(n @ ExternalRef(), ty) =>
      Val.Global(stripName(n), ty)
  }

  def isStaticModule(name: Global): Boolean =
    chg.nodes(name).isInstanceOf[ClassHierarchy.Class] &&
    (!chg.nodes.contains(name + "init"))

  def isExternalModule(name: Global): Boolean =
    chg.nodes.get(name) match {
      case Some(cls: ClassHierarchy.Class) if cls.attrs.exists(_ == Attr.External) =>
        true
      case _ =>
        false
    }

  object ExternalRef {
    def unapply(name: Global): Boolean = {
      chg.nodes.get(name) match {
        case Some(node) if node.attrs.exists(_ == Attr.External) =>
          true
        case _ =>
          false
      }
    }
  }
}
