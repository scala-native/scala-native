package scala.scalanative.linker

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.util.unsupported
import sbt.nio.file.Glob

object LinktimeIntrinsicCallsResolver {
  // scalafmt: { maxColumn = 120}
  final val ServiceLoader = Global.Top("java.util.ServiceLoader")
  final val ServiceLoaderCtor = ServiceLoader.member(Sig.Ctor(Seq(Rt.Class, Type.Array(Rt.Object))))
  final val ServiceLoaderRef = Type.Ref(ServiceLoader)
  final val ClassLoaderRef = Type.Ref(Global.Top("java.lang.ClassLoader"))
  final val ServiceLoaderLoad = ServiceLoader
    .member(Sig.Method("load", Seq(Rt.Class, ServiceLoaderRef), Sig.Scope.PublicStatic))
  final val ServiceLoaderLoadClassLoader = ServiceLoader
    .member(Sig.Method("load", Seq(Rt.Class, ClassLoaderRef, ServiceLoaderRef), Sig.Scope.PublicStatic))
  final val ServiceLoaderLoadInstalled = ServiceLoader
    .member(Sig.Method("loadInstalled", Seq(Rt.Class, ServiceLoaderRef), Sig.Scope.PublicStatic))

  object IntrinsicCall {
    val intrinsicMethods = Set(
      ServiceLoaderLoad,
      ServiceLoaderLoadClassLoader,
      ServiceLoaderLoadInstalled
    )

    def unapply(inst: Inst): Option[(Global.Member, List[Val])] = inst match {
      case Inst.Let(_, Op.Call(_, Val.Global(name: Global.Member, _), args), _) if intrinsicMethods.contains(name) =>
        Some((name, args.toList))
      case _ => None
    }
  }

  object ServiceLoaderLoadCall {
    def unapply(inst: Inst): Option[Val.ClassOf] = inst match {
      case IntrinsicCall(
            ServiceLoaderLoad | ServiceLoaderLoadClassLoader | ServiceLoaderLoadInstalled,
            (cls: Val.ClassOf) :: _
          ) =>
        Some(cls)
      case _ => None
    }
  }
}

trait LinktimeIntrinsicCallsResolver { self: Reach =>
  import self._
  import LinktimeIntrinsicCallsResolver._

  def resolveIntrinsicsCalls(insts: Seq[Inst]): Seq[Inst] = {
    implicit val fresh = Fresh(insts)
    implicit val buffer = new Buffer()
    insts.foreach {
      case inst @ ServiceLoaderLoadCall(cls) => onServiceLoaderLoad(inst, cls)
      case inst                              => buffer += inst
    }
    buffer.toSeq
  }

  private def onServiceLoaderLoad(inst: Inst, cls: Val.ClassOf)(implicit fresh: Fresh, buf: Buffer): Unit = {
    val let @ Inst.Let(_, op: Op.Call, _) = inst
    implicit val pos: Position = let.pos
    implicit val scopeId: ScopeId = let.scopeId

    val providers = loader.definedServicesProviders
      .get(cls.name)
      .toList
      .flatten
      .filter { provider =>
        val exists = lookup(provider, ignoreIfUnavailable = true).isDefined
        val allowed =
          config.compilerConfig.serviceProviders
            .get(cls.name.id)
            .flatMap(_.find(_ == provider.id))
            .isDefined

        val context = s"service providor '${provider.id}' defined for '${cls.name.id}'"
        if (!exists)
          config.logger.warn(s"Not found declared $context")
        else if (!allowed)
          config.logger.debug(s"Ignoring disabled $context")
        else
          config.logger.info(s"Including found $context")
        exists && allowed
      }
      .map { cls =>
        val clsRef = Type.Ref(cls)
        val alloc = buf.classalloc(cls, let.unwind)
        val callCtor = buf.call(
          ty = Type.Function(Seq(clsRef), Type.Unit),
          ptr = Val.Global(cls.member(Sig.Ctor(Nil)), Type.Ptr),
          args = Seq( /*this=*/ Val.Local(alloc.id, clsRef)),
          unwind = let.unwind
        )
        Val.Local(alloc.id, clsRef)
      }

    val alloc = let.copy(op = Op.Classalloc(ServiceLoader, None))
    buf += alloc

    val providersArray = buf.arrayalloc(Rt.Object, Val.ArrayValue(Rt.Object, providers), let.unwind)
    val callCtor = buf.call(
      ty = Type.Function(Seq(ServiceLoaderRef, Rt.Class, Type.Array(Type.Ref(cls.name))), Type.Unit),
      ptr = Val.Global(ServiceLoaderCtor, Type.Ptr),
      args = Seq(
        /*this=*/ Val.Local(alloc.id, ServiceLoaderRef),
        /*runtimeClass=*/ cls,
        /*serviceProviderNames=*/ Val.Local(providersArray.id, providersArray.valty)
      ),
      unwind = let.unwind
    )
  }
}
