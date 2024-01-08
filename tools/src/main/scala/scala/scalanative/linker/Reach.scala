package scala.scalanative
package linker

import java.nio.file.{Path, Paths}
import scala.annotation.tailrec
import scala.collection.mutable

/** The algorithm gathering definitions reachable from a set of root symbols. */
class Reach(
    protected val config: build.Config,
    entries: Seq[nir.Global],
    protected val loader: ClassLoader
) extends LinktimeValueResolver
    with LinktimeIntrinsicCallsResolver {
  import Reach._

  val loaded =
    mutable.Map.empty[nir.Global.Top, mutable.Map[nir.Global, nir.Defn]]
  val unreachable = mutable.Map.empty[nir.Global, UnreachableSymbol]
  val unsupported = mutable.Map.empty[nir.Global, UnsupportedFeature]
  val enqueued = mutable.Set.empty[nir.Global]
  var todo = List.empty[nir.Global]
  val done = mutable.Map.empty[nir.Global, nir.Defn]
  var stack = List.empty[nir.Global]
  val links = mutable.Set.empty[nir.Attr.Link]
  val preprocessorDefinitions = mutable.Set.empty[nir.Attr.Define]
  val infos = mutable.Map.empty[nir.Global, Info]
  val from = mutable.Map.empty[nir.Global, ReferencedFrom]

  val dyncandidates = mutable.Map.empty[nir.Sig, mutable.Set[nir.Global.Member]]
  val dynsigs = mutable.Set.empty[nir.Sig]
  val dynimpls = mutable.Set.empty[nir.Global.Member]

  private case class DelayedMethod(
      owner: nir.Global.Top,
      sig: nir.Sig,
      pos: nir.Position
  )
  private val delayedMethods = mutable.Set.empty[DelayedMethod]

  if (injects.nonEmpty) {
    injects.groupBy(_.name.top).foreach {
      case (owner, defns) =>
        val buf = mutable.Map.empty[nir.Global, nir.Defn]
        loaded.update(owner, buf)
        defns.foreach(defn => buf.update(defn.name, defn))
    }
    injects.foreach(reachDefn)
  }

  entries.foreach(reachEntry(_)(nir.Position.NoPosition))

  // Internal hack used inside linker tests, for more information
  // check out comment in scala.scalanative.linker.ReachabilitySuite
  val reachStaticConstructors = sys.props
    .get("scala.scalanative.linker.reachStaticConstructors")
    .flatMap(v => scala.util.Try(v.toBoolean).toOption)
    .forall(_ == true)

  loader.classesWithEntryPoints.foreach { clsName =>
    if (reachStaticConstructors) reachClinit(clsName)(nir.Position.NoPosition)
    config.compilerConfig.buildTarget match {
      case build.BuildTarget.Application => ()
      case _                             => reachExported(clsName)
    }
  }

  def result(): ReachabilityAnalysis = {
    cleanup()

    val defns = mutable.UnrolledBuffer.empty[nir.Defn]
    defns.sizeHint(done.size)
    // drop the null values that have been introduced
    // in reachUnavailable
    done.valuesIterator.filter(_ != null).foreach(defns += _)

    if (unreachable.isEmpty && unsupported.isEmpty)
      new ReachabilityAnalysis.Result(
        infos = infos,
        entries = entries,
        links = links.toSeq,
        preprocessorDefinitions = preprocessorDefinitions.toSeq,
        defns = defns.toSeq,
        dynsigs = dynsigs.toSeq,
        dynimpls = dynimpls.toSeq,
        resolvedVals = resolvedNirValues,
        foundServiceProviders = foundServiceProviders
      )
    else
      new ReachabilityAnalysis.Failure(
        defns = defns.toSeq,
        unreachable = unreachable.values.toSeq,
        unsupportedFeatures = unsupported.values.toSeq,
        foundServiceProviders = foundServiceProviders
      )
  }

  def cleanup(): Unit = {
    // Remove all unreachable methods from the
    // responds and defaultResponds of every class.
    // Optimizer and codegen may never increase reachability
    // past what's known now, so it's safe to do this.
    infos.foreach {
      case (_, cls: Class) =>
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

      case _ => ()
    }
  }

  def lookup(global: nir.Global): Option[nir.Defn] =
    lookup(global, ignoreIfUnavailable = false)

  protected def lookup(
      global: nir.Global,
      ignoreIfUnavailable: Boolean
  ): Option[nir.Defn] = {
    val owner = global.top
    if (!loaded.contains(owner) && !unreachable.contains(owner)) {
      loader
        .load(owner)
        .fold[Unit] {
          if (!ignoreIfUnavailable) addMissing(global)
        } { defns =>
          val scope = mutable.Map.empty[nir.Global, nir.Defn]
          defns.foreach { defn => scope(defn.name) = defn }
          loaded(owner) = scope
        }
    }

    loaded
      .get(owner)
      .flatMap(_.get(global))
      .orElse {
        if (!ignoreIfUnavailable) addMissing(global)
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
          def addMissing() = this.addMissing(top.member(sig))
          scopeInfo(top)(position).fold(addMissing()) { info =>
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

  def reachDefn(name: nir.Global): Unit = {
    stack ::= name
    lookup(name).fold[Unit] {
      reachUnavailable(name)
    } { defn =>
      if (defn.attrs.isStub && !config.linkStubs) {
        reachUnavailable(name)
      } else {
        reachDefn(defn)
      }
    }
    stack = stack.tail
  }

  def reachDefn(defninition: nir.Defn): Unit = {
    val defn = preprocessDefn(defninition)
    implicit val srcPosition = defn.pos
    defn match {
      case defn: nir.Defn.Var =>
        reachVar(defn)
      case defn: nir.Defn.Const =>
        reachConst(defn)
      case defn: nir.Defn.Declare =>
        reachDeclare(defn)
      case defn: nir.Defn.Define =>
        val nir.Global.Member(_, sig) = defn.name
        if (nir.Rt.arrayAlloc.contains(sig)) {
          classInfo(nir.Rt.arrayAlloc(sig)).foreach(reachAllocation)
        }
        reachDefine(defn)
      case defn: nir.Defn.Trait =>
        reachTrait(defn)
      case defn: nir.Defn.Class =>
        reachClass(defn)
      case defn: nir.Defn.Module =>
        reachModule(defn)
    }
    done(defn.name) = defn
  }

  private def preprocessDefn(defn: nir.Defn): nir.Defn = {
    defn match {
      case defn: nir.Defn.Define =>
        (resolveLinktimeDefine _)
          .andThen(resolveDefineIntrinsics)
          .apply(defn)

      case _ => defn
    }
  }

  private def resolveDefineIntrinsics(
      defn: nir.Defn.Define
  ): nir.Defn.Define = {
    if (defn.attrs.isUsingIntrinsics)
      defn.copy(insts = resolveIntrinsicsCalls(defn))(defn.pos)
    else defn
  }

  def reachEntry(name: nir.Global)(implicit srcPosition: nir.Position): Unit = {
    if (!name.isTop) {
      reachEntry(name.top)
    }
    from.getOrElseUpdate(name, ReferencedFrom.Root)
    reachGlobalNow(name)
    infos.get(name) match {
      case Some(cls: Class) =>
        if (!cls.attrs.isAbstract) {
          reachAllocation(cls)(cls.position)
          if (cls.isModule) {
            val init = cls.name.member(nir.Sig.Ctor(Seq.empty))
            if (loaded(cls.name).contains(init)) {
              reachGlobal(init)(cls.position)
            }
          }
        }
      case _ =>
        ()
    }
  }

  def reachClinit(
      clsName: nir.Global.Top
  )(implicit srcPosition: nir.Position): Unit = {
    reachGlobalNow(clsName)
    infos.get(clsName).foreach { cls =>
      val clinit = clsName.member(nir.Sig.Clinit)
      if (loaded(clsName).contains(clinit)) {
        reachGlobal(clinit)(cls.position)
      }
    }
  }

  def reachExported(name: nir.Global.Top): Unit = {
    def isExported(defn: nir.Defn) = defn match {
      case nir.Defn.Define(attrs, nir.Global.Member(_, sig), _, _, _) =>
        attrs.isExtern || sig.isExtern
      case _ => false
    }

    for {
      cls <- infos.get(name).collect { case info: ScopeInfo => info }
      defns <- loaded.get(cls.name)
      (name, defn) <- defns
    } if (isExported(defn)) reachGlobal(name)(defn.pos)
  }

  def reachGlobal(name: nir.Global)(implicit srcPosition: nir.Position): Unit =
    if (!enqueued.contains(name) && name.ne(nir.Global.None)) {
      enqueued += name
      track(name)
      todo ::= name
    }

  def reachGlobalNow(
      name: nir.Global
  )(implicit srcPosition: nir.Position): Unit =
    if (done.contains(name)) {
      ()
    } else if (!stack.contains(name)) {
      enqueued += name
      track(name)
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
          case (_, defn: nir.Defn.Define) =>
            val nir.Global.Member(_, sig) = defn.name
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
          case (_, defn: nir.Defn.Define) =>
            val nir.Global.Member(_, sig) = defn.name
            def update(sig: nir.Sig): Unit = {
              info.responds(sig) = lookup(info, sig)
                .getOrElse(
                  fail(s"Required method ${sig} not found in ${info.name}")
                )
            }

            if (sig.isMethod || sig.isCtor || sig.isClinit || sig.isGenerated) {
              update(sig)
            }
          case _ => ()
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

  def reachAllocation(info: Class)(implicit srcPosition: nir.Position): Unit =
    if (!info.allocated) {
      info.allocated = true

      // Handle all class and trait virtual calls
      // on this class. This includes virtual calls
      // on the traits that this class implements and
      // calls on all transitive parents.
      val calls = mutable.Set.empty[nir.Sig]
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
              dyncandidates.getOrElseUpdate(
                dynsig,
                mutable.Set.empty[nir.Global.Member]
              )
            buf += impl
          } else {
            dynimpls += impl
            reachGlobal(impl)
          }
        case (sig, impl)
            if sig.isGenerated
              && sig.unmangled
                .asInstanceOf[nir.Sig.Generated]
                .id == "$extern$forwarder" =>
          reachGlobal(impl)
        case _ =>
          ()
      }
    }

  def scopeInfo(
      name: nir.Global.Top
  )(implicit srcPosition: nir.Position): Option[ScopeInfo] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: ScopeInfo => Some(info)
      case _               => None
    }
  }

  def scopeInfoOrUnavailable(
      name: nir.Global.Top
  )(implicit srcPosition: nir.Position): Info = {
    reachGlobalNow(name)
    infos(name) match {
      case info: ScopeInfo   => info
      case info: Unavailable => info
      case _                 => util.unreachable
    }
  }

  def classInfo(
      name: nir.Global.Top
  )(implicit srcPosition: nir.Position): Option[Class] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Class => Some(info)
      case _           => None
    }
  }

  def classInfoOrObject(
      name: nir.Global.Top
  )(implicit srcPosition: nir.Position): Class =
    classInfo(name)
      .orElse(classInfo(nir.Rt.Object.name))
      .getOrElse(fail(s"Class info not available for $name"))

  def traitInfo(
      name: nir.Global.Top
  )(implicit srcPosition: nir.Position): Option[Trait] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Trait => Some(info)
      case _           => None
    }
  }

  def methodInfo(
      name: nir.Global
  )(implicit srcPosition: nir.Position): Option[Method] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Method => Some(info)
      case _            => None
    }
  }

  def fieldInfo(
      name: nir.Global
  )(implicit srcPosition: nir.Position): Option[Field] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Field => Some(info)
      case _           => None
    }
  }

  def reachUnavailable(name: nir.Global): Unit = {
    newInfo(new Unavailable(name))
    addMissing(name)
    // Put a null definition to indicate that name
    // is effectively done and doesn't need to be
    // visited any more. This saves us the need to
    // check the unreachable set every time we check
    // if something is truly handled.
    done(name) = null
  }

  def reachVar(defn: nir.Defn.Var): Unit = {
    val nir.Defn.Var(attrs, name, ty, rhs) = defn
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

  def reachConst(defn: nir.Defn.Const): Unit = {
    val nir.Defn.Const(attrs, name, ty, rhs) = defn
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

  def reachDeclare(defn: nir.Defn.Declare): Unit = {
    val nir.Defn.Declare(attrs, name, ty) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(
      new Method(
        attrs,
        scopeInfoOrUnavailable(name.top),
        name,
        ty,
        insts = Array(),
        debugInfo = nir.Defn.Define.DebugInfo.empty
      )
    )
    reachAttrs(attrs)
    reachType(ty)
  }

  def reachDefine(defn: nir.Defn.Define): Unit = {
    val nir.Defn.Define(attrs, name, ty, insts, debugInfo) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(
      new Method(
        attrs,
        scopeInfoOrUnavailable(name.top),
        name,
        ty,
        insts.toArray,
        debugInfo
      )
    )
    reachAttrs(attrs)
    reachType(ty)
    reachInsts(insts)
  }

  def reachTrait(defn: nir.Defn.Trait): Unit = {
    val nir.Defn.Trait(attrs, name, traits) = defn
    implicit val pos: nir.Position = defn.pos
    newInfo(new Trait(attrs, name, traits.flatMap(traitInfo)))
    reachAttrs(attrs)
  }

  def reachClass(defn: nir.Defn.Class): Unit = {
    val nir.Defn.Class(attrs, name, parent, traits) = defn
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

  def reachModule(defn: nir.Defn.Module): Unit = {
    val nir.Defn.Module(attrs, name, parent, traits) = defn
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

  def reachAttrs(attrs: nir.Attrs): Unit = {
    links ++= attrs.links
    preprocessorDefinitions ++= attrs.preprocessorDefinitions
  }

  def reachType(ty: nir.Type)(implicit srcPosition: nir.Position): Unit =
    ty match {
      case nir.Type.ArrayValue(ty, n) =>
        reachType(ty)
      case nir.Type.StructValue(tys) =>
        tys.foreach(reachType)
      case nir.Type.Function(args, ty) =>
        args.foreach(reachType)
        reachType(ty)
      case nir.Type.Ref(name, _, _) =>
        reachGlobal(name)
      case nir.Type.Var(ty) =>
        reachType(ty)
      case nir.Type.Array(ty, _) =>
        reachType(ty)
      case _ =>
        ()
    }

  def reachVal(value: nir.Val)(implicit srcPosition: nir.Position): Unit =
    value match {
      case nir.Val.Zero(ty) =>
        reachType(ty)
      case nir.Val.StructValue(values) =>
        values.foreach(reachVal)
      case nir.Val.ArrayValue(ty, values) =>
        reachType(ty)
        values.foreach(reachVal)
      case nir.Val.Local(_, ty) =>
        reachType(ty)
      case nir.Val.Global(n, ty) =>
        reachGlobal(n)
        reachType(ty)
      case nir.Val.Const(v) =>
        reachVal(v)
      case nir.Val.ClassOf(cls) =>
        reachGlobal(cls)
      case _ =>
        ()
    }

  def reachInsts(insts: Seq[nir.Inst]): Unit =
    insts.foreach(reachInst)

  def reachInst(inst: nir.Inst): Unit = {
    implicit val srcPosition: nir.Position = inst.pos
    inst match {
      case nir.Inst.Label(n, params) =>
        params.foreach(p => reachType(p.ty))
      case nir.Inst.Let(_, op, unwind) =>
        reachOp(op)(inst.pos)
        reachNext(unwind)
      case nir.Inst.Ret(v) =>
        reachVal(v)
      case nir.Inst.Jump(next) =>
        reachNext(next)
      case nir.Inst.If(v, thenp, elsep) =>
        reachVal(v)
        reachNext(thenp)
        reachNext(elsep)
      case nir.Inst.Switch(v, default, cases) =>
        reachVal(v)
        reachNext(default)
        cases.foreach(reachNext)
      case nir.Inst.Throw(v, unwind) =>
        reachVal(v)
        reachNext(unwind)
      case nir.Inst.Unreachable(unwind) =>
        reachNext(unwind)
      case _: nir.Inst.LinktimeIf =>
        util.unreachable
    }
  }

  def reachOp(op: nir.Op)(implicit pos: nir.Position): Unit = op match {
    case nir.Op.Call(ty, ptrv, argvs) =>
      reachType(ty)
      reachVal(ptrv)
      argvs.foreach(reachVal)
    case nir.Op.Load(ty, ptrv, _) =>
      reachType(ty)
      reachVal(ptrv)
    case nir.Op.Store(ty, ptrv, v, _) =>
      reachType(ty)
      reachVal(ptrv)
      reachVal(v)
    case nir.Op.Elem(ty, ptrv, indexvs) =>
      reachType(ty)
      reachVal(ptrv)
      indexvs.foreach(reachVal)
    case nir.Op.Extract(aggrv, indexvs) =>
      reachVal(aggrv)
    case nir.Op.Insert(aggrv, v, indexvs) =>
      reachVal(aggrv)
      reachVal(v)
    case nir.Op.Stackalloc(ty, v) =>
      reachType(ty)
      reachVal(v)
      ty match {
        case ref: nir.Type.RefKind =>
          classInfo(ref.className).foreach(reachAllocation)
        case _ => ()
      }
    case nir.Op.Bin(bin, ty, lv, rv) =>
      reachType(ty)
      reachVal(lv)
      reachVal(rv)
    case nir.Op.Comp(comp, ty, lv, rv) =>
      reachType(ty)
      reachVal(lv)
      reachVal(rv)
    case nir.Op.Conv(conv, ty, v) =>
      reachType(ty)
      reachVal(v)
    case nir.Op.Fence(attrs) => ()

    case nir.Op.Classalloc(n, zoneHandle) =>
      classInfo(n).foreach(reachAllocation)
      zoneHandle.foreach(reachVal)
    case nir.Op.Fieldload(ty, v, n) =>
      reachType(ty)
      reachVal(v)
      reachGlobal(n)
    case nir.Op.Fieldstore(ty, v1, n, v2) =>
      reachType(ty)
      reachVal(v1)
      reachGlobal(n)
      reachVal(v2)
    case nir.Op.Field(obj, name) =>
      reachVal(obj)
      reachGlobal(name)
    case nir.Op.Method(obj, sig) =>
      reachVal(obj)
      reachMethodTargets(obj.ty, sig)
    case nir.Op.Dynmethod(obj, dynsig) =>
      reachVal(obj)
      reachDynamicMethodTargets(dynsig)
    case nir.Op.Module(n) =>
      classInfo(n).foreach(reachAllocation)
      val init = n.member(nir.Sig.Ctor(Seq.empty))
      loaded.get(n).fold(addMissing(n)) { defn =>
        if (defn.contains(init)) {
          reachGlobal(init)
        }
      }
    case nir.Op.As(ty, v) =>
      reachType(ty)
      reachVal(v)
    case nir.Op.Is(ty, v) =>
      reachType(ty)
      reachVal(v)
    case nir.Op.Copy(v) =>
      reachVal(v)
    case nir.Op.SizeOf(ty)      => reachType(ty)
    case nir.Op.AlignmentOf(ty) => reachType(ty)
    case nir.Op.Box(code, obj) =>
      reachVal(obj)
    case nir.Op.Unbox(code, obj) =>
      reachVal(obj)
    case nir.Op.Var(ty) =>
      reachType(ty)
    case nir.Op.Varload(slot) =>
      reachVal(slot)
    case nir.Op.Varstore(slot, value) =>
      reachVal(slot)
      reachVal(value)
    case nir.Op.Arrayalloc(ty, init, zoneHandle) =>
      classInfo(nir.Type.toArrayClass(ty)).foreach(reachAllocation)
      reachType(ty)
      reachVal(init)
      zoneHandle.foreach(reachVal)
    case nir.Op.Arrayload(ty, arr, idx) =>
      reachType(ty)
      reachVal(arr)
      reachVal(idx)
    case nir.Op.Arraystore(ty, arr, idx, value) =>
      reachType(ty)
      reachVal(arr)
      reachVal(idx)
      reachVal(value)
    case nir.Op.Arraylength(arr) =>
      reachVal(arr)
  }

  def reachNext(next: nir.Next)(implicit srcPosition: nir.Position): Unit =
    next match {
      case nir.Next.Label(_, args) =>
        args.foreach(reachVal)
      case _ =>
        ()
    }

  def reachMethodTargets(ty: nir.Type, sig: nir.Sig)(implicit
      srcPosition: nir.Position
  ): Unit =
    ty match {
      case nir.Type.Array(ty, _) =>
        reachMethodTargets(nir.Type.Ref(nir.Type.toArrayClass(ty)), sig)
      case nir.Type.Ref(name, _, _) =>
        scopeInfo(name).foreach { scope =>
          if (!scope.calls.contains(sig)) {
            scope.calls += sig
            val targets = scope.targets(sig)
            if (targets.nonEmpty) targets.foreach(reachGlobal)
            else {
              // At this stage we cannot tell if method target is not defined or not yet reached
              // We're delaying resolving targets to the end of Reach phase to check if this method is never defined in NIR
              track(name.member(sig))
              delayedMethods += DelayedMethod(name, sig, srcPosition)
            }
          }
        }
      case _ =>
        ()
    }

  def reachDynamicMethodTargets(
      dynsig: nir.Sig
  )(implicit srcPosition: nir.Position) = {
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

  def lookup(cls: Class, sig: nir.Sig): Option[nir.Global.Member] = {
    assert(loaded.contains(cls.name))

    val tryMember = cls.name.member(sig)
    if (loaded(cls.name).contains(tryMember)) {
      Some(tryMember)
    } else {
      cls.parent.flatMap(lookup(_, sig))
    }
  }

  protected def addMissing(global: nir.Global): Unit =
    global match {
      case UnsupportedFeatureExtractor(details) =>
        unsupported.getOrElseUpdate(global, details)
      case _ =>
        unreachable.getOrElseUpdate(
          global,
          UnreachableSymbol(
            name = global,
            symbol = parseSymbol(global),
            backtrace = getBackTrace(global)
          )
        )
    }

  private def parseSymbol(name: nir.Global): SymbolDescriptor = {
    def renderType(tpe: nir.Type): String = tpe match {
      case arr: nir.Type.Array   => s"${renderType(arr.ty)}[]"
      case ref: nir.Type.RefKind => ref.className.id
      case ty                    => ty.show
    }
    def parseArgTypes(
        types: Seq[nir.Type],
        isCtor: Boolean = false
    ): Some[Seq[String]] = Some {
      val args = types match {
        case _ if isCtor   => types
        case args :+ retty => args
        case _             => Nil
      }
      args.map(renderType)
    }

    val Private = "private"
    val Static = "static"

    def parseResultType(types: Seq[nir.Type]): Option[String] =
      types.lastOption.map(renderType)

    def parseModifiers(scope: nir.Sig.Scope): List[String] = scope match {
      case nir.Sig.Scope.Public           => Nil
      case nir.Sig.Scope.Private(_)       => List(Private)
      case nir.Sig.Scope.PublicStatic     => List(Static)
      case nir.Sig.Scope.PrivateStatic(_) => List(Static, Private)
    }

    def parseSig(owner: String, sig: nir.Sig): SymbolDescriptor =
      sig.unmangled match {
        case nir.Sig.Method(name, types, scope) =>
          SymbolDescriptor(
            "method",
            s"$owner.$name",
            parseArgTypes(types),
            parseResultType(types),
            parseModifiers(scope)
          )
        case nir.Sig.Ctor(types) =>
          SymbolDescriptor(
            "constructor",
            owner,
            parseArgTypes(types, isCtor = true)
          )
        case nir.Sig.Clinit =>
          SymbolDescriptor(
            "constructor",
            owner,
            modifiers = List(Static)
          )
        case nir.Sig.Field(name, scope) =>
          SymbolDescriptor(
            "field",
            owner,
            modifiers = parseModifiers(scope)
          )
        case nir.Sig.Generated(name) =>
          SymbolDescriptor(
            "symbol",
            s"$owner.$name",
            modifiers = List("generated")
          )
        case nir.Sig.Proxy(name, types) =>
          SymbolDescriptor(
            "method",
            s"$owner.$name",
            parseArgTypes(types),
            parseResultType(types),
            modifiers = List("proxy")
          )
        case nir.Sig.Duplicate(sig, types) =>
          val original = parseSig(owner, sig)
          original.copy(
            argTypes = parseArgTypes(types),
            resultType = parseResultType(types),
            modifiers = List("duplicate") ++ original.modifiers
          )
          SymbolDescriptor(
            "method",
            s"$owner.$name",
            parseArgTypes(types),
            parseResultType(types),
            modifiers = List("duplicate")
          )
        case nir.Sig.Extern(name) =>
          SymbolDescriptor(
            "symbol",
            s"$owner.$name",
            modifiers = List("extern")
          )
      }

    name match {
      case nir.Global.Member(owner, sig) =>
        parseSig(owner.id, sig)
      case nir.Global.Top(id) =>
        SymbolDescriptor("type", id)
      case _ =>
        util.unreachable
    }
  }

  private def getBackTrace(
      referencedFrom: nir.Global
  ): List[BackTraceElement] = {
    val buf = List.newBuilder[BackTraceElement]
    def loop(name: nir.Global): List[BackTraceElement] = {
      // orElse just in case if we messed something up and failed to correctly track references
      // Accept possibly empty backtrace instead of crashing
      val current = from.getOrElse(name, ReferencedFrom.Root)
      if (current == ReferencedFrom.Root) buf.result()
      else {
        val file = current.srcPosition.source.filename.getOrElse("unknown")
        val line = current.srcPosition.line
        buf += BackTraceElement(
          name = current.referencedBy,
          symbol = parseSymbol(current.referencedBy),
          filename = file,
          line = line + 1
        )
        loop(current.referencedBy)
      }
    }
    loop(referencedFrom)
  }

  protected object UnsupportedFeatureExtractor {
    import UnsupportedFeature._
    val UnsupportedSymbol =
      nir.Global.Top("scala.scalanative.runtime.UnsupportedFeature")

    // Add stubs for NIR when checkFeatures is disabled
    val injects: Seq[nir.Defn] =
      if (config.compilerConfig.checkFeatures) Nil
      else {
        implicit val srcPosition: nir.Position = nir.Position.NoPosition
        val stubMethods = for {
          methodName <- Seq("threads", "virtualThreads", "continuations")
        } yield {
          import scala.scalanative.codegen.Lower.{
            throwUndefined,
            throwUndefinedTy,
            throwUndefinedVal
          }
          implicit val scopeId: nir.ScopeId = nir.ScopeId.TopLevel
          nir.Defn.Define(
            attrs = nir.Attrs.None,
            name = UnsupportedSymbol.member(
              nir.Sig.Method(
                methodName,
                Seq(nir.Type.Unit),
                nir.Sig.Scope.PublicStatic
              )
            ),
            ty = nir.Type.Function(Nil, nir.Type.Unit),
            insts = {
              implicit val fresh: nir.Fresh = nir.Fresh()
              val buf = new nir.Buffer()
              buf.label(fresh(), Nil)
              buf.call(
                throwUndefinedTy,
                throwUndefinedVal,
                Seq(nir.Val.Null),
                nir.Next.None
              )
              buf.unreachable(nir.Next.None)
              buf.toSeq
            }
          )
        }
        val stubType =
          nir.Defn.Class(
            nir.Attrs.None,
            UnsupportedSymbol,
            Some(nir.Rt.Object.name),
            Nil
          )
        stubType +: stubMethods
      }

    private def details(sig: nir.Sig): UnsupportedFeature.Kind = {
      sig.unmangled match {
        case nir.Sig.Method("threads", _, _) =>
          SystemThreads
        case nir.Sig.Method("virtualThreads", _, _) =>
          VirtualThreads
        case nir.Sig.Method("continuations", _, _) =>
          Continuations
        case _ => Other
      }
    }

    def unapply(name: nir.Global): Option[UnsupportedFeature] = name match {
      case nir.Global.Member(UnsupportedSymbol, sig) =>
        unsupported
          .get(name)
          .orElse(
            Some(
              UnsupportedFeature(
                kind = details(sig),
                backtrace = getBackTrace(name)
              )
            )
          )
      case _ => None
    }
  }

  private def fail(msg: => String): Nothing = {
    throw new LinkingException(msg)
  }

  protected def track(name: nir.Global)(implicit srcPosition: nir.Position) =
    from.getOrElseUpdate(
      name,
      if (stack.isEmpty) ReferencedFrom.Root
      else ReferencedFrom(stack.head, srcPosition)
    )

  lazy val injects: Seq[nir.Defn] = UnsupportedFeatureExtractor.injects
}

object Reach {

  /** Returns the definitions reachable from `entries`. */
  def apply(
      config: build.Config,
      entries: Seq[nir.Global],
      loader: ClassLoader
  ): ReachabilityAnalysis = {
    val reachability = new Reach(config, entries, loader)
    reachability.process()
    reachability.processDelayed()
    reachability.result()
  }

  private[scalanative] case class ReferencedFrom(
      referencedBy: nir.Global,
      srcPosition: nir.Position
  )
  object ReferencedFrom {
    final val Root = ReferencedFrom(nir.Global.None, nir.Position.NoPosition)
  }
  case class SymbolDescriptor(
      kind: String,
      name: String,
      argTypes: Option[Seq[String]] = None,
      resultType: Option[String] = None,
      modifiers: Seq[String] = Nil
  ) {
    override def toString(): String = {
      val mods =
        if (modifiers.isEmpty) "" else modifiers.distinct.mkString("", " ", " ")
      val argsList = argTypes.fold("")(_.mkString("(", ", ", ")"))
      val resType = resultType.fold("")(tpe => s": $tpe")
      s"$mods$kind $name$argsList$resType"
    }
  }
  case class BackTraceElement(
      name: nir.Global,
      symbol: SymbolDescriptor,
      filename: String,
      line: Int
  )
  case class UnreachableSymbol(
      name: nir.Global,
      symbol: SymbolDescriptor,
      backtrace: List[BackTraceElement]
  )

  case class UnsupportedFeature(
      kind: UnsupportedFeature.Kind,
      backtrace: List[BackTraceElement]
  )
  object UnsupportedFeature {
    sealed abstract class Kind(val details: String)
    case object SystemThreads
        extends Kind(
          "Application linked with disabled multithreading support. Adjust nativeConfig and try again"
        )
    case object VirtualThreads
        extends Kind("VirtualThreads are not supported yet on this platform")
    case object Continuations
        extends Kind("Continuations are not supported yet on this platform")
    case object Other extends Kind("Other unsupported feature")
  }
}
