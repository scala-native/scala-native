package scala.scalanative
package linker

import java.nio.file.{Path, Paths}
import scala.annotation.tailrec
import scala.collection.mutable
import scalanative.nir._

class Reach(
    protected val config: build.Config,
    entries: Seq[Global],
    loader: ClassLoader
) extends LinktimeValueResolver {
  import Reach._

  val unavailable = mutable.Set.empty[Global]
  val loaded = mutable.Map.empty[Global, mutable.Map[Global, Defn]]
  val enqueued = mutable.Set.empty[Global]
  var todo = List.empty[Global]
  val done = mutable.Map.empty[Global, Defn]
  var stack = List.empty[Global]
  val links = mutable.Set.empty[Attr.Link]
  val infos = mutable.Map.empty[Global, Info]
  val from = mutable.Map.empty[Global, Global]
  val missing = mutable.Map.empty[Global, Set[NonReachablePosition]]

  val dyncandidates = mutable.Map.empty[Sig, mutable.Set[Global]]
  val dynsigs = mutable.Set.empty[Sig]
  val dynimpls = mutable.Set.empty[Global]

  private case class DelayedMethod(owner: Global.Top, sig: Sig, pos: Position)
  private val delayedMethods = mutable.Set.empty[DelayedMethod]

  entries.foreach(reachEntry)

  // Internal hack used inside linker tests, for more information
  // check out comment in scala.scalanative.linker.ReachabilitySuite
  val reachStaticConstructors = sys.props
    .get("scala.scalanative.linker.reachStaticConstructors")
    .flatMap(v => scala.util.Try(v.toBoolean).toOption)
    .forall(_ == true)

  loader.classesWithEntryPoints.foreach { clsName =>
    if (reachStaticConstructors) reachClinit(clsName)
    config.compilerConfig.buildTarget match {
      case build.BuildTarget.Application => ()
      case _                             => reachExported(clsName)
    }
  }

  def result(): Result = {
    reportMissing()
    cleanup()

    val defns = mutable.UnrolledBuffer.empty[Defn]

    // drop the null values that have been introduced
    // in reachUnavailable
    defns ++= done.valuesIterator.filter(_ != null)

    new Result(
      infos,
      entries,
      unavailable.toSeq,
      from,
      links.toSeq,
      defns.toSeq,
      dynsigs.toSeq,
      dynimpls.toSeq,
      resolvedNirValues
    )
  }

  def cleanup(): Unit = {
    // Remove all unreachable methods from the
    // responds and defaultResponds of every class.
    // Optimizer and codegen may never increase reachability
    // past what's known now, so it's safe to do this.
    infos.values.foreach {
      case cls: Class =>
        val responds = cls.responds.toArray
        responds.foreach {
          case (sig, name) =>
            if (!done.contains(name)) {
              cls.responds -= sig
            }
        }

        val defaultResponds = cls.defaultResponds.toArray
        defaultResponds.foreach {
          case (sig, name) =>
            if (!done.contains(name)) {
              cls.defaultResponds -= sig
            }
        }

      case _ =>
        ()
    }
  }

  def lookup(global: Global): Option[Defn] =
    lookup(global, ignoreIfUnavailable = false)

  private def lookup(
      global: Global,
      ignoreIfUnavailable: Boolean
  ): Option[Defn] = {
    val owner = global.top
    if (!loaded.contains(owner) && !unavailable.contains(owner)) {
      loader
        .load(owner)
        .fold[Unit] {
          if (!ignoreIfUnavailable) {
            unavailable += owner
          }
        } { defns =>
          val scope = mutable.Map.empty[Global, Defn]
          defns.foreach { defn => scope(defn.name) = defn }
          loaded(owner) = scope
        }
    }
    def fallback = global match {
      case Global.Member(owner, sig) =>
        infos
          .get(owner)
          .collect {
            case scope: ScopeInfo =>
              scope.linearized
                .find(_.responds.contains(sig))
                .map(_.responds(sig))
                .flatMap(lookup)
          }
          .flatten

      case _ => None
    }

    loaded
      .get(owner)
      .flatMap(_.get(global))
      .orElse(fallback)
      .orElse {
        if (!ignoreIfUnavailable) {
          val resolvedPosition = for {
            invokedFrom <- from.get(global)
            callerInfo <- infos.get(invokedFrom)
          } yield callerInfo.position
          val pos = resolvedPosition.getOrElse(nir.Position.NoPosition)
          addMissing(global, pos)
        }
        None
      }
  }

  def process(): Unit =
    while (todo.nonEmpty) {
      val name = todo.head
      todo = todo.tail
      if (!done.contains(name)) {
        reachDefn(name)
      }
    }

  @tailrec
  final def processDelayed(): Unit = {
    // Recursively iterate delayed methods - processing delayed method that has existing implementation
    // might result in calling other delayed method. Loop until no more delayedMethods are found
    if (delayedMethods.nonEmpty) {
      /*  Check methods that were marked to not have any defined targets yet when processing loop.
       *  At this stage they should define at least 1 target, or should be marked as a missing symbol.
       */
      delayedMethods.foreach {
        case DelayedMethod(top, sig, position) =>
          def addMissing() = this.addMissing(top.member(sig), position)
          scopeInfo(top).fold(addMissing()) { info =>
            val wasAllocated = info match {
              case value: Trait => value.implementors.exists(_.allocated)
              case clazz: Class => clazz.allocated
            }
            val targets = info.targets(sig)
            if (targets.isEmpty && wasAllocated) {
              addMissing()
            } else {
              todo ++= targets
            }
          }
      }

      delayedMethods.clear()
      process()
      processDelayed()
    }
  }

  def reachDefn(name: Global): Unit = {
    stack ::= name
    lookup(name).fold[Unit] {
      reachUnavailable(name)
    } { defn =>
      if (defn.attrs.isStub && !config.linkStubs) {
        reachUnavailable(name)
      } else {
        val maybeFixedDefn = defn match {
          case defn: Defn.Define =>
            (resolveLinktimeDefine _)
              .andThen(mitigateStaticCalls _)
              .apply(defn)
          case _ => defn
        }
        reachDefn(maybeFixedDefn)
      }
    }
    stack = stack.tail
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
        val Global.Member(_, sig) = defn.name: @unchecked
        if (Rt.arrayAlloc.contains(sig)) {
          classInfo(Rt.arrayAlloc(sig)).foreach(reachAllocation)
        }
        reachDefine(resolveLinktimeDefine(defn))
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
    if (!name.isTop) {
      reachEntry(name.top)
    }
    from(name) = Global.None
    reachGlobalNow(name)
    infos.get(name) match {
      case Some(cls: Class) =>
        if (!cls.attrs.isAbstract) {
          reachAllocation(cls)
          if (cls.isModule) {
            val init = cls.name.member(Sig.Ctor(Seq.empty))
            if (loaded(cls.name).contains(init)) {
              reachGlobal(init)
            }
          }
        }
      case _ =>
        ()
    }
  }

  def reachClinit(name: Global): Unit = {
    reachGlobalNow(name)
    infos.get(name).foreach { cls =>
      val clinit = cls.name.member(Sig.Clinit())
      if (loaded(cls.name).contains(clinit)) {
        reachGlobal(clinit)
      }
    }
  }

  def reachExported(name: Global): Unit = {
    def isExported(defn: Defn) = defn match {
      case Defn.Define(attrs, Global.Member(_, sig), _, _) =>
        attrs.isExtern || sig.isExtern
      case _ => false
    }

    for {
      cls <- infos.get(name)
      defns <- loaded.get(cls.name)
      (name, defn) <- defns
    } if (isExported(defn)) reachGlobal(name)
  }

  def reachGlobal(name: Global): Unit =
    if (!enqueued.contains(name) && name.ne(Global.None)) {
      enqueued += name
      from(name) = if (stack.isEmpty) Global.None else stack.head
      todo ::= name
    }

  def reachGlobalNow(name: Global): Unit =
    if (done.contains(name)) {
      ()
    } else if (!stack.contains(name)) {
      enqueued += name
      reachDefn(name)
    } else {
      val lines = (s"cyclic reference to ${name.show}:" +:
        stack.map(el => s"* ${el.show}"))
      fail(lines.mkString("\n"))
    }

  def newInfo(info: Info): Unit = {
    infos(info.name) = info
    info match {
      case info: MemberInfo =>
        info.owner match {
          case owner: ScopeInfo =>
            owner.members += info
          case _ =>
            ()
        }
      case info: Trait =>
        // Register given trait as a subtrait of
        // all its transitive parent traits.
        def loopTraits(traitInfo: Trait): Unit = {
          traitInfo.subtraits += info
          traitInfo.traits.foreach(loopTraits)
        }
        info.traits.foreach(loopTraits)

        // Initialize default method implementations that
        // can be resolved on a given trait. It includes both
        // all of its parent default methods and any of the
        // non-abstract method declared directly in this trait.
        info.linearized.foreach {
          case parentTraitInfo: Trait =>
            info.responds ++= parentTraitInfo.responds
          case _ =>
            util.unreachable
        }
        loaded(info.name).foreach {
          case (_, defn: Defn.Define) =>
            val Global.Member(_, sig) = defn.name: @unchecked
            info.responds(sig) = defn.name
          case _ =>
            ()
        }
      case info: Class =>
        // Register given class as a subclass of all
        // transitive parents and as an implementation
        // of all transitive traits.
        def loopParent(parentInfo: Class): Unit = {
          parentInfo.implementors += info
          parentInfo.subclasses += info
          parentInfo.parent.foreach(loopParent)
          parentInfo.traits.foreach(loopTraits)
        }
        def loopTraits(traitInfo: Trait): Unit = {
          traitInfo.implementors += info
          traitInfo.traits.foreach(loopTraits)
        }
        info.parent.foreach(loopParent)
        info.traits.foreach(loopTraits)

        // Initialize responds map to keep track of all
        // signatures this class responds to and its
        // corresponding implementation. Some of the entries
        // may end up being not reachable, we remove those
        // in the cleanup right before we return the result.
        info.parent.foreach { parentInfo =>
          info.responds ++= parentInfo.responds
        }
        loaded(info.name).foreach {
          case (_, defn: Defn.Define) =>
            val Global.Member(_, sig) = defn.name: @unchecked
            def update(sig: Sig): Unit = {
              info.responds(sig) = lookup(info, sig)
                .getOrElse(
                  fail(s"Required method ${sig} not found in ${info.name}")
                )
            }
            sig match {
              case Rt.JavaEqualsSig =>
                update(Rt.ScalaEqualsSig)
                update(Rt.JavaEqualsSig)
              case Rt.JavaHashCodeSig =>
                update(Rt.ScalaHashCodeSig)
                update(Rt.JavaHashCodeSig)
              case sig
                  if sig.isMethod || sig.isCtor || sig.isClinit || sig.isGenerated =>
                update(sig)
              case _ =>
                ()
            }
          case _ =>
            ()
        }

        // Initialize the scope of the default methods that can
        // be used as a fallback if no method implementation is given
        // in a given class.
        info.linearized.foreach {
          case traitInfo: Trait =>
            info.defaultResponds ++= traitInfo.responds
          case _ =>
            ()
        }
      case _ =>
        ()
    }
  }

  def reachAllocation(info: Class): Unit =
    if (!info.allocated) {
      info.allocated = true

      // Handle all class and trait virtual calls
      // on this class. This includes virtual calls
      // on the traits that this class implements and
      // calls on all transitive parents.
      val calls = mutable.Set.empty[Sig]
      calls ++= info.calls
      def loopParent(parentInfo: Class): Unit = {
        calls ++= parentInfo.calls
        parentInfo.parent.foreach(loopParent)
        parentInfo.traits.foreach(loopTraits)
      }
      def loopTraits(traitInfo: Trait): Unit = {
        calls ++= traitInfo.calls
        traitInfo.traits.foreach(loopTraits)
      }
      info.parent.foreach(loopParent)
      info.traits.foreach(loopTraits)
      calls.foreach { sig =>
        def respondImpl = info.responds.get(sig)
        def defaultImpl = info.defaultResponds.get(sig)
        respondImpl
          .orElse(defaultImpl)
          .foreach(reachGlobal)
      }

      // 1. Handle all dynamic methods on this class.
      //    Any method that implements a known dynamic
      //    signature becomes reachable. The others are
      //    stashed as dynamic candidates.
      // 2. FuncPtr extern forwarder becomes reachable if
      //    class itself is reachable.
      info.responds.foreach {
        case (sig, impl) if sig.isMethod =>
          val dynsig = sig.toProxy
          if (!dynsigs.contains(dynsig)) {
            val buf =
              dyncandidates.getOrElseUpdate(dynsig, mutable.Set.empty[Global])
            buf += impl
          } else {
            dynimpls += impl
            reachGlobal(impl)
          }
        case (sig, impl)
            if sig.isGenerated
              && sig.unmangled
                .asInstanceOf[Sig.Generated]
                .id == "$extern$forwarder" =>
          reachGlobal(impl)
        case _ =>
          ()
      }
    }

  def scopeInfo(name: Global): Option[ScopeInfo] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: ScopeInfo => Some(info)
      case _               => None
    }
  }

  def scopeInfoOrUnavailable(name: Global): Info = {
    reachGlobalNow(name)
    infos(name) match {
      case info: ScopeInfo   => info
      case info: Unavailable => info
      case _                 => util.unreachable
    }
  }

  def classInfo(name: Global): Option[Class] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Class => Some(info)
      case _           => None
    }
  }

  def classInfoOrObject(name: Global): Class =
    classInfo(name)
      .orElse(classInfo(Rt.Object.name))
      .getOrElse(fail(s"Class info not available for $name"))

  def traitInfo(name: Global): Option[Trait] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Trait => Some(info)
      case _           => None
    }
  }

  def methodInfo(name: Global): Option[Method] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Method => Some(info)
      case _            => None
    }
  }

  def fieldInfo(name: Global): Option[Field] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Field => Some(info)
      case _           => None
    }
  }

  def reachUnavailable(name: Global): Unit = {
    newInfo(new Unavailable(name))
    unavailable += name
    // Put a null definition to indicate that name
    // is effectively done and doesn't need to be
    // visited any more. This saves us the need to
    // check the unavailable set every time we check
    // if something is truly handled.
    done(name) = null
  }

  def reachVar(defn: Defn.Var): Unit = {
    val Defn.Var(attrs, name, ty, rhs) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(
      new Field(
        attrs,
        scopeInfoOrUnavailable(name.top),
        name,
        isConst = false,
        ty,
        rhs
      )
    )
    reachAttrs(attrs)
    reachType(ty)
    reachVal(rhs)
  }

  def reachConst(defn: Defn.Const): Unit = {
    val Defn.Const(attrs, name, ty, rhs) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(
      new Field(
        attrs,
        scopeInfoOrUnavailable(name.top),
        name,
        isConst = true,
        ty,
        rhs
      )
    )
    reachAttrs(attrs)
    reachType(ty)
    reachVal(rhs)
  }

  def reachDeclare(defn: Defn.Declare): Unit = {
    val Defn.Declare(attrs, name, ty) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(
      new Method(attrs, scopeInfoOrUnavailable(name.top), name, ty, Array())
    )
    reachAttrs(attrs)
    reachType(ty)
  }

  // Mitigate static calls to methods compiled with Scala Native older then 0.4.3
  // If given static method in not rechable replace it with call to method with the same
  // name in the companion module
  private def mitigateStaticCalls(defn: Defn.Define): Defn.Define = {
    lazy val fresh = Fresh(defn.insts)
    val newInsts = defn.insts.flatMap {
      case inst @ Inst.Let(
            n,
            Op.Call(
              ty: Type.Function,
              Val.Global(
                methodName @ Global.Member(Global.Top(methodOwner), sig),
                _
              ),
              args
            ),
            unwind
          )
          if sig.isStatic && lookup(
            methodName,
            ignoreIfUnavailable = true
          ).isEmpty =>
        def findRewriteCandidate(inModule: Boolean): Option[List[Inst]] = {
          val owner =
            if (inModule) Global.Top(methodOwner + "$")
            else Global.Top(methodOwner)
          val newMethod = {
            val Sig.Method(id, tps, scope) = sig.unmangled: @unchecked
            val newScope = scope match {
              case Sig.Scope.PublicStatic      => Sig.Scope.Public
              case Sig.Scope.PrivateStatic(in) => Sig.Scope.Private(in)
              case scope                       => scope
            }
            val newSig = Sig.Method(id, tps, newScope)
            Val.Global(owner.member(newSig), Type.Ptr)
          }
          // Make sure that candidate exists
          lookup(newMethod.name, ignoreIfUnavailable = true)
            .map { _ =>
              implicit val pos: nir.Position = defn.pos
              val newType = {
                val newArgsTpe = Type.Ref(owner) +: ty.args
                Type.Function(newArgsTpe, ty.ret)
              }

              if (inModule) {
                val moduleV = Val.Local(fresh(), Type.Ref(owner))
                val newArgs = moduleV +: args
                Inst.Let(moduleV.name, Op.Module(owner), Next.None) ::
                  Inst.Let(n, Op.Call(newType, newMethod, newArgs), unwind) ::
                  Nil
              } else {
                Inst.Let(n, Op.Call(newType, newMethod, args), unwind) :: Nil
              }
            }
        }

        findRewriteCandidate(inModule = true)
          //  special case for lifted methods
          .orElse(findRewriteCandidate(inModule = false))
          .getOrElse {
            config.logger.warn(
              s"Found a call to not defined static method ${methodName}. " +
                "Static methods are generated since Scala Native 0.4.3, " +
                "report this bug in the Scala Native issues. " +
                s"Call defined at ${inst.pos.show}"
            )
            addMissing(methodName, inst.pos)
            inst :: Nil
          }

      case inst =>
        inst :: Nil
    }
    defn.copy(insts = newInsts)(defn.pos)
  }

  def reachDefine(defn: Defn.Define): Unit = {
    val Defn.Define(attrs, name, ty, insts) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(
      new Method(
        attrs,
        scopeInfoOrUnavailable(name.top),
        name,
        ty,
        insts.toArray
      )
    )
    reachAttrs(attrs)
    reachType(ty)
    reachInsts(insts)
  }

  def reachTrait(defn: Defn.Trait): Unit = {
    val Defn.Trait(attrs, name, traits) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(new Trait(attrs, name, traits.flatMap(traitInfo)))
    reachAttrs(attrs)
  }

  def reachClass(defn: Defn.Class): Unit = {
    val Defn.Class(attrs, name, parent, traits) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(
      new Class(
        attrs,
        name,
        parent.map(classInfoOrObject),
        traits.flatMap(traitInfo),
        isModule = false
      )
    )
    reachAttrs(attrs)
  }

  def reachModule(defn: Defn.Module): Unit = {
    val Defn.Module(attrs, name, parent, traits) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(
      new Class(
        attrs,
        name,
        parent.map(classInfoOrObject),
        traits.flatMap(traitInfo),
        isModule = true
      )
    )
    reachAttrs(attrs)
  }

  def reachAttrs(attrs: Attrs): Unit =
    links ++= attrs.links

  def reachType(ty: Type): Unit = ty match {
    case Type.ArrayValue(ty, n) =>
      reachType(ty)
    case Type.StructValue(tys) =>
      tys.foreach(reachType)
    case Type.Function(args, ty) =>
      args.foreach(reachType)
      reachType(ty)
    case Type.Ref(name, _, _) =>
      reachGlobal(name)
    case Type.Var(ty) =>
      reachType(ty)
    case Type.Array(ty, _) =>
      reachType(ty)
    case _ =>
      ()
  }

  def reachVal(value: Val): Unit = value match {
    case Val.Zero(ty) =>
      reachType(ty)
    case Val.StructValue(values) =>
      values.foreach(reachVal)
    case Val.ArrayValue(ty, values) =>
      reachType(ty)
      values.foreach(reachVal)
    case Val.Local(n, ty) =>
      reachType(ty)
    case Val.Global(n, ty) =>
      reachGlobal(n); reachType(ty)
    case Val.Const(v) =>
      reachVal(v)
    case Val.ClassOf(cls) =>
      reachGlobal(cls)
    case _ =>
      ()
  }

  def reachInsts(insts: Seq[Inst]): Unit =
    insts.foreach(reachInst)

  def reachInst(inst: Inst): Unit = inst match {
    case Inst.Label(n, params) =>
      params.foreach(p => reachType(p.ty))
    case Inst.Let(n, op, unwind) =>
      reachOp(op)(inst.pos)
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
    case Inst.Unreachable(unwind) =>
      reachNext(unwind)
    case _: Inst.LinktimeIf =>
      util.unreachable
  }

  def reachOp(op: Op)(implicit pos: Position): Unit = op match {
    case Op.Call(ty, ptrv, argvs) =>
      reachType(ty)
      reachVal(ptrv)
      argvs.foreach(reachVal)
    case Op.Load(ty, ptrv, syncAttrs) =>
      reachType(ty)
      reachVal(ptrv)
    case Op.Store(ty, ptrv, v, syncAttrs) =>
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
      ty match {
        case ref: Type.RefKind =>
          classInfo(ref.className).foreach(reachAllocation)
        case _ => ()
      }
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
    case Op.Fence(attrs) => ()

    case Op.Classalloc(n) =>
      classInfo(n).foreach(reachAllocation)
    case Op.Fieldload(ty, v, n) =>
      reachType(ty)
      reachVal(v)
      reachGlobal(n)
    case Op.Fieldstore(ty, v1, n, v2) =>
      reachType(ty)
      reachVal(v1)
      reachGlobal(n)
      reachVal(v2)
    case Op.Field(obj, name) =>
      reachVal(obj)
      reachGlobal(name)
    case Op.Method(obj, sig) =>
      reachVal(obj)
      reachMethodTargets(obj.ty, sig)
    case Op.Dynmethod(obj, dynsig) =>
      reachVal(obj)
      reachDynamicMethodTargets(dynsig)
    case Op.Module(n) =>
      classInfo(n).foreach(reachAllocation)
      val init = n.member(Sig.Ctor(Seq.empty))
      loaded.get(n).fold(addMissing(n, pos)) { defn =>
        if (defn.contains(init)) {
          reachGlobal(init)
        }
      }
    case Op.As(ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Is(ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Copy(v) =>
      reachVal(v)
    case Op.SizeOf(ty)      => reachType(ty)
    case Op.AlignmentOf(ty) => reachType(ty)
    case Op.Box(code, obj) =>
      reachVal(obj)
    case Op.Unbox(code, obj) =>
      reachVal(obj)
    case Op.Var(ty) =>
      reachType(ty)
    case Op.Varload(slot) =>
      reachVal(slot)
    case Op.Varstore(slot, value) =>
      reachVal(slot)
      reachVal(value)
    case Op.Arrayalloc(ty, init) =>
      classInfo(Type.toArrayClass(ty)).foreach(reachAllocation)
      reachType(ty)
      reachVal(init)
    case Op.Arrayload(ty, arr, idx) =>
      reachType(ty)
      reachVal(arr)
      reachVal(idx)
    case Op.Arraystore(ty, arr, idx, value) =>
      reachType(ty)
      reachVal(arr)
      reachVal(idx)
      reachVal(value)
    case Op.Arraylength(arr) =>
      reachVal(arr)
  }

  def reachNext(next: Next): Unit = next match {
    case Next.Label(_, args) =>
      args.foreach(reachVal)
    case _ =>
      ()
  }

  def reachMethodTargets(ty: Type, sig: Sig)(implicit pos: Position): Unit =
    ty match {
      case Type.Array(ty, _) =>
        reachMethodTargets(Type.Ref(Type.toArrayClass(ty)), sig)
      case Type.Ref(name, _, _) =>
        scopeInfo(name).foreach { scope =>
          if (!scope.calls.contains(sig)) {
            scope.calls += sig
            val targets = scope.targets(sig)
            if (targets.nonEmpty) targets.foreach(reachGlobal)
            else {
              // At this stage we cannot tell if method target is not defined or not yet reached
              // We're delaying resolving targets to the end of Reach phase to check if this method is never defined in NIR
              delayedMethods += DelayedMethod(name.top, sig, pos)
            }
          }
        }
      case _ =>
        ()
    }

  def reachDynamicMethodTargets(dynsig: Sig) = {
    if (!dynsigs.contains(dynsig)) {
      dynsigs += dynsig
      if (dyncandidates.contains(dynsig)) {
        dyncandidates(dynsig).foreach { impl =>
          dynimpls += impl
          reachGlobal(impl)
        }
        dyncandidates -= dynsig
      }
    }
  }

  def lookup(cls: Class, sig: Sig): Option[Global] = {
    assert(loaded.contains(cls.name))

    def lookupSig(cls: Class, sig: Sig): Option[Global] = {
      val tryMember = cls.name.member(sig)
      if (loaded(cls.name).contains(tryMember)) {
        Some(tryMember)
      } else {
        cls.parent.flatMap(lookupSig(_, sig))
      }
    }

    def lookupRequired(sig: Sig) = lookupSig(cls, sig)
      .getOrElse(fail(s"Not found required definition ${cls.name} ${sig}"))

    sig match {
      // We short-circuit scala_== and scala_## to immeditately point to the
      // equals and hashCode implementation for the reference types to avoid
      // double virtual dispatch overhead. This optimization is *not* optional
      // as implementation of scala_== on java.lang.Object assumes it's only
      // called on classes which don't overrider java_==.
      case Rt.ScalaEqualsSig =>
        val scalaImpl = lookupRequired(Rt.ScalaEqualsSig)
        val javaImpl = lookupRequired(Rt.JavaEqualsSig)
        if (javaImpl.top != Rt.Object.name &&
            scalaImpl.top == Rt.Object.name) {
          Some(javaImpl)
        } else {
          Some(scalaImpl)
        }
      case Rt.ScalaHashCodeSig =>
        val scalaImpl = lookupRequired(Rt.ScalaHashCodeSig)
        val javaImpl = lookupRequired(Rt.JavaHashCodeSig)
        if (javaImpl.top != Rt.Object.name &&
            scalaImpl.top == Rt.Object.name) {
          Some(javaImpl)
        } else {
          Some(scalaImpl)
        }
      case _ =>
        lookupSig(cls, sig)
    }
  }

  protected def addMissing(global: Global, pos: Position): Unit = {
    val prev = missing.getOrElseUpdate(global, Set.empty)
    if (pos != nir.Position.NoPosition) {
      val position = NonReachablePosition(
        uri = pos.source,
        line = pos.sourceLine
      )
      missing(global) = prev + position
    }
  }

  private def reportMissing(): Unit = {
    if (missing.nonEmpty) {
      unavailable
        .foreach(missing.getOrElseUpdate(_, Set.empty))
      val log = config.logger
      log.error(s"Found ${missing.size} missing definitions while linking")
      missing.toSeq.sortBy(_._1).foreach {
        case (global, positions) =>
          log.error(s"Not found $global")
          positions.toList
            .sortBy(p => (p.uri, p.line))
            .foreach { pos =>
              log.error(s"\tat ${pos.uri}:${pos.line}")
            }
      }
      fail("Undefined definitions found in reachability phase")
    }
  }

  private def fail(msg: => String): Nothing = {
    throw new LinkingException(msg)
  }
}

object Reach {
  def apply(
      config: build.Config,
      entries: Seq[Global],
      loader: ClassLoader
  ): Result = {
    val reachability = new Reach(config, entries, loader)
    reachability.process()
    reachability.processDelayed()
    reachability.result()
  }

  private[scalanative] case class NonReachablePosition(
      uri: java.net.URI,
      line: Int
  )
}
