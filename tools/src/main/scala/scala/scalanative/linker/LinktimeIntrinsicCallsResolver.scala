package scala.scalanative.linker

import scala.collection.mutable
import scala.scalanative.nir._
import scala.scalanative.util.unsupported
import scala.scalanative.build.NativeConfig.{ServiceName, ServiceProviderName}

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

  sealed trait ServiceProviderStatus
  object ServiceProviderStatus {

    /** ServiceProvider enlisted in config and reached by ServiceLoader.load call */
    case object Loaded extends ServiceProviderStatus

    /** ServiceProvider found on classpath but not enabled */
    case object Available extends ServiceProviderStatus

    /** ServiceProvider found in META-INF but not found on classpath */
    case object Missing extends ServiceProviderStatus
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
     *  | foo.bar.baz | my.foo.bar      | Missing   |
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
        import ServiceProviderStatus.{Loaded, Available, Missing}
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
          else Seq(FoundServiceProvider("---", Missing) -> 0)
        statusColor = provider.status match {
          case _ if noColor => ""
          case Loaded       => GREEN
          case Available    => YELLOW
          case Missing      => RED
        }
      } {
        def isNextService = serviceIdx > 0 &&  providerIdx == 0
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
    foundServices.toMap.mapValues(_.values.toSeq)
  )

  def resolveIntrinsicsCalls(defn: Defn.Define): Seq[Inst] = {
    val insts = defn.insts
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

    val serviceName = cls.name.id
    val serviceStats = foundServices.getOrElseUpdate(serviceName, mutable.Map.empty)

    def providerInfo(symbol: Global.Top) = {
      val serviceProviderName = symbol.id
      serviceStats.getOrElseUpdate(
        serviceProviderName, {
          def exists = lookup(symbol, ignoreIfUnavailable = true).isDefined
          def shouldLoad =
            config.compilerConfig.serviceProviders
              .get(serviceName)
              .flatMap(_.find(_ == serviceProviderName))
              .isDefined
          val status =
            if (!exists) ServiceProviderStatus.Missing
            else if (shouldLoad) ServiceProviderStatus.Loaded
            else ServiceProviderStatus.Available
          FoundServiceProvider(serviceProviderName, status)
        }
      )
    }

    def loadProvider(cls: Global.Top): Val.Local = {
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

    val loadedProviders = loader.definedServicesProviders
      .get(cls.name)
      .toList
      .flatten
      .filter(providerInfo(_).status == ServiceProviderStatus.Loaded)
      .map(loadProvider)
    val providersArray = buf.arrayalloc(Rt.Object, Val.ArrayValue(Rt.Object, loadedProviders), let.unwind)

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