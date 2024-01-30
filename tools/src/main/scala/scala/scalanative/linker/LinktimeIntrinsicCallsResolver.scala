package scala.scalanative.linker

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.util.unsupported
import scala.scalanative.build.NativeConfig.{ServiceName, ServiceProviderName}
import scala.scalanative.build.Logger

object LinktimeIntrinsicCallsResolver {
  // scalafmt: { maxColumn = 120}
  final val ServiceLoader = Global.Top("java.util.ServiceLoader")
  final val ServiceLoaderModule = Global.Top("java.util.ServiceLoader$")
  final val ServiceLoaderProvider = Global.Top("java.util.ServiceLoader$Provider")

  final val ServiceLoaderRef = Type.Ref(ServiceLoader)
  final val ServiceLoaderModuleRef = Type.Ref(ServiceLoaderModule)
  final val ServiceLoaderProviderRef = Type.Ref(ServiceLoaderProvider)
  final val ClassLoaderRef = Type.Ref(Global.Top("java.lang.ClassLoader"))

  final val ServiceLoaderCtor = ServiceLoader
    .member(Sig.Ctor(Seq(Rt.Class, Type.Array(ServiceLoaderProviderRef))))

  final val ServiceLoaderLoad = ServiceLoader
    .member(Sig.Method("load", Seq(Rt.Class, ServiceLoaderRef), Sig.Scope.PublicStatic))
  final val ServiceLoaderLoadClassLoader = ServiceLoader
    .member(Sig.Method("load", Seq(Rt.Class, ClassLoaderRef, ServiceLoaderRef), Sig.Scope.PublicStatic))
  final val ServiceLoaderLoadInstalled = ServiceLoader
    .member(Sig.Method("loadInstalled", Seq(Rt.Class, ServiceLoaderRef), Sig.Scope.PublicStatic))

  final val ServiceLoaderCreateProvider = ServiceLoaderModule
    .member(Sig.Method("createIntrinsicProvider", Seq(Rt.Class, Type.Ptr, ServiceLoaderProviderRef)))
  // Registers available ServiceLoader.load* methods
  final val ServiceLoaderLoadMethods = Set(
    ServiceLoaderLoad,
    ServiceLoaderLoadClassLoader,
    ServiceLoaderLoadInstalled
  ).flatMap { member =>
    Set(
      member,
      // Adds their special variants using module for usages within javalib
      member.copy(
        owner = ServiceLoaderModule,
        sig = member.sig.unmangled match {
          case sig @ Sig.Method(_, _, scope) => sig.copy(scope = Sig.Scope.Public)
          case sig                           => sig
        }
      )
    )
  }

  object IntrinsicCall {
    private val intrinsicMethods = ServiceLoaderLoadMethods

    def unapply(inst: Inst): Option[(Global.Member, List[Val])] = inst match {
      case Inst.Let(_, Op.Call(_, Val.Global(name: Global.Member, _), args), _) if intrinsicMethods.contains(name) =>
        Some((name, args.toList))
      case _ => None
    }
  }

  object ServiceLoaderLoadCall {
    def unapply(inst: Inst)(implicit logger: Logger): Option[Val.ClassOf] = inst match {
      case IntrinsicCall(name, args) if ServiceLoaderLoadMethods.contains(name) =>
        args match {
          case (cls: Val.ClassOf) :: _ => Some(cls)
          // Special case for usage within javalib
          case _ :: (cls: Val.ClassOf) :: _ => Some(cls)
          case _ =>
            logger.error(s"Found unsupported variant of ${name.show} function, arguments: ${args.map(_.show)}")
            None
        }
      case _ => None
    }
  }

  sealed trait ServiceProviderStatus
  object ServiceProviderStatus {

    /** ServiceProvider enlisted in config and reached by ServiceLoader.load call */
    case object Loaded extends ServiceProviderStatus

    /** ServiceProvider found on classpath but not enabled */
    case object Available extends ServiceProviderStatus

    /** There is no implementations available for given service */
    case object NoProviders extends ServiceProviderStatus

    /** ServiceProvider found in META-INF but not found on classpath */
    case object NotFoundOnClasspath extends ServiceProviderStatus

    /** ServiceProvider not found in META-INF but defined in config */
    case object UnknownConfigEntry extends ServiceProviderStatus
  }
  case class FoundServiceProvider(name: ServiceProviderName, status: ServiceProviderStatus)
  class FoundServiceProviders(val serviceProviders: Map[ServiceName, Seq[FoundServiceProvider]]) extends AnyVal {
    def nonEmpty = serviceProviders.nonEmpty
    def loaded = serviceProviders.foldLeft(0)(_ + _._2.count(_.status == ServiceProviderStatus.Loaded))

    /* Renders stats as table:
     *  |-------------------------------------------|
     *  | Service Name| Provider Name  | Status    |
     *  |-------------------------------------------|
     *  | x.y.z       | x.y.myImpl      | Loaded    |
     *  |             | x.y.z.otherImpl | Available |
     *  | foo.bar.baz | my.foo.bar      | NotFound  |
     *  |-------------------------------------------|
     */
    def asTable(noColor: Boolean): String = {
      import scala.io.AnsiColor.{RESET, RED, YELLOW, GREEN}
      import ServiceProviderStatus._

      type Entry = (String, String, String)
      val builder = new java.lang.StringBuilder()
      val header: Entry = ("Service", "Service Provider", "Status")
      val entryPadding = 3
      val (serviceNameWidth, provideNameWidth, stateWidth) = serviceProviders
        .foldLeft(header._1.length(), header._2.length(), header._3.length()) {
          case ((maxServiceName, maxProviderName, maxStateName), (serviceName, providers)) =>
            val longestProviderName = providers.foldLeft(0) { _ max _.name.length }
            val longestStateName = providers.foldLeft(0) { _ max _.status.toString().length() }
            (
              maxServiceName max serviceName.length(),
              maxProviderName max longestProviderName,
              maxStateName max longestStateName
            )
        }
      def addLine() = {
        val dashlineLength = serviceNameWidth + provideNameWidth + stateWidth + 8 // extra padding columns
        builder.append("|").append("-" * dashlineLength).append("|\n")
      }
      def addEntry(entry: Entry, statusColor: String, skipServiceName: Boolean) = {
        val (serviceName, providerName, status) = entry
        import ServiceProviderStatus._
        val serviceNameOrBlank = if (skipServiceName) "" else serviceName
        builder
          .append("| ")
          .append(serviceNameOrBlank.padTo(serviceNameWidth, ' '))
          .append(" | ")
          .append(providerName.padTo(provideNameWidth, ' '))
          .append(" | ")
          .append(statusColor)
          .append(status.toString.padTo(stateWidth, ' '))
          .append(if (statusColor.nonEmpty) RESET else "")
          .append(" |\n")
      }

      def addBlankEntry() = addEntry(("", "", ""), "", skipServiceName = false)

      addLine()
      addEntry(header, statusColor = "", skipServiceName = false)
      addLine()
      for {
        ((serviceName, providers), serviceIdx) <- serviceProviders.toSeq.sortBy(_._1).zipWithIndex

        (provider, providerIdx) <-
          if (providers.nonEmpty) providers.sortBy(_.name).zipWithIndex
          else Seq(FoundServiceProvider("---", NoProviders) -> 0)
        statusColor = provider.status match {
          case _ if noColor                             => ""
          case Loaded                                   => GREEN
          case Available | NoProviders                  => YELLOW
          case NotFoundOnClasspath | UnknownConfigEntry => RED
        }
      } {
        def isNextService = serviceIdx > 0 && providerIdx == 0
        if (isNextService) addBlankEntry()
        addEntry(
          (serviceName, provider.name, provider.status.toString()),
          statusColor = statusColor,
          skipServiceName = providerIdx > 0
        )
      }
      addLine()
      builder.toString()
    }
  }
}

trait LinktimeIntrinsicCallsResolver { self: Reach =>
  import self._
  import LinktimeIntrinsicCallsResolver._

  private val foundServices = mutable.Map.empty[ServiceName, mutable.Map[ServiceProviderName, FoundServiceProvider]]
  def foundServiceProviders: FoundServiceProviders = new FoundServiceProviders(
    foundServices.map {
      case (service, providers) =>
        service -> providers.map(_._2).toSeq
    }.toMap
  )
  private val serviceProviderLoaders = mutable.Map.empty[Global.Top, Val.Global]

  def resolveIntrinsicsCalls(defn: Defn.Define): Seq[Inst] = {
    val insts = defn.insts
    implicit def logger: Logger = self.config.logger
    implicit val fresh: Fresh = Fresh(insts)
    implicit val buffer: InstructionBuilder = new InstructionBuilder()
    insts.foreach {
      case inst @ ServiceLoaderLoadCall(cls) =>
        onServiceLoaderLoad(inst, cls)
      case inst =>
        buffer += inst
    }
    buffer.toSeq
  }

  private def onServiceLoaderLoad(inst: Inst, cls: Val.ClassOf)(implicit
      fresh: Fresh,
      buf: InstructionBuilder
  ): Unit = {
    val let @ Inst.Let(_, op: Op.Call, _) = inst: @unchecked
    implicit val pos: Position = let.pos
    implicit val scopeId: ScopeId = let.scopeId

    val serviceName = cls.name.id
    val serviceProvidersStatus = foundServices.getOrElseUpdate(serviceName, mutable.Map.empty)

    def providerInfo(symbol: Global.Top) = {
      val serviceProviderName = symbol.id
      serviceProvidersStatus.getOrElseUpdate(
        serviceProviderName, {
          def exists = lookup(symbol, ignoreIfUnavailable = true).isDefined
          def shouldLoad =
            config.compilerConfig.serviceProviders
              .get(serviceName)
              .flatMap(_.find(_ == serviceProviderName))
              .isDefined
          val status =
            if (!exists) ServiceProviderStatus.NotFoundOnClasspath
            else if (shouldLoad) ServiceProviderStatus.Loaded
            else ServiceProviderStatus.Available
          FoundServiceProvider(serviceProviderName, status)
        }
      )
    }

    def serviceProviderLoader(providerCls: Global.Top): Val.Global = serviceProviderLoaders
      .getOrElseUpdate(
        providerCls, {
          val providerClsRef = Type.Ref(providerCls)
          val loadProviderLambda = {
            new Defn.Define(
              attrs = Attrs.None,
              name = cls.name.member(Sig.Generated(s"loadProvider_${providerCls.id}")),
              ty = Type.Function(Nil, providerClsRef),
              insts = {
                val fresh = Fresh()
                val buf = new InstructionBuilder()(fresh)
                buf.label(fresh(), Nil)
                val alloc = buf.classalloc(providerCls, let.unwind)
                val callCtor = buf.call(
                  ty = Type.Function(Seq(providerClsRef), Type.Unit),
                  ptr = Val.Global(providerCls.member(Sig.Ctor(Nil)), Type.Ptr),
                  args = Seq( /*this=*/ Val.Local(alloc.id, providerClsRef)),
                  unwind = let.unwind
                )
                // Load provider module as it might contain a registration logic
                val moduleName = Global.Top(providerCls.id + "$")
                lookup(moduleName, ignoreIfUnavailable = true).foreach { _ =>
                  buf.module(moduleName, let.unwind)
                }
                buf.ret(alloc)
                buf.toSeq
              }
            )
          }
          reachDefn(loadProviderLambda)
          Val.Global(loadProviderLambda.name, Type.Ptr)
        }
      )

    val serviceLoaderModule = buf.let(Op.Module(ServiceLoaderModule), let.unwind)
    val serviceProviders = loader.definedServicesProviders
      .get(cls.name)
      .toList
      .flatten
      .filter(providerInfo(_).status == ServiceProviderStatus.Loaded)
      .map { providerCls =>
        val loader = serviceProviderLoader(providerCls)
        buf.call(
          ty = Type.Function(Seq(ServiceLoaderModuleRef, Rt.Class, Type.Ptr), ServiceLoaderProviderRef),
          ptr = Val.Global(ServiceLoaderCreateProvider, Type.Ptr),
          args = Seq(serviceLoaderModule, Val.ClassOf(providerCls), loader),
          unwind = let.unwind
        )
      }
    // Mark every service provider found in config, but not found in any META-INF as NotFound
    config.compilerConfig.serviceProviders
      .get(cls.name.id)
      .foreach { providers =>
        providers.foreach { providerName =>
          serviceProvidersStatus.getOrElseUpdate(
            providerName,
            FoundServiceProvider(providerName, ServiceProviderStatus.UnknownConfigEntry)
          )
        }
      }
    val providersArray = buf.arrayalloc(
      ty = ServiceLoaderProviderRef,
      init = Val.ArrayValue(ServiceLoaderProviderRef, serviceProviders),
      unwind = let.unwind
    )

    // Create instance of ServiceLoader and call it's constructor
    val alloc = let.copy(op = Op.Classalloc(ServiceLoader, None))
    buf += alloc
    buf.call(
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
