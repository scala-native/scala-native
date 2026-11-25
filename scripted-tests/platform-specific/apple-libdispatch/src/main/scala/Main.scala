import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import dispatch._

/** Scala callbacks invoked by libdispatch on threads the runtime did not
 *  create, allocating and collecting concurrently.
 */
object Main {
  final val Forever = -1L
  val completed = new AtomicLong()
  val threads = ConcurrentHashMap.newKeySet[Thread]()
  val threadLocal = new ThreadLocal[Array[Byte]]()
  val fileEvents = new AtomicLong()

  def context(value: Long): Ptr[Byte] =
    fromRawPtr[Byte](Intrinsics.castLongToRawPtr(value))
  def after(millis: Long): CUnsignedLongLong =
    dispatch_time(0L.toULong, millis * 1000000L)
  lazy val globalQueue = dispatch_get_global_queue(0.toCSSize, 0.toCSize)
  lazy val serialQueue = dispatch_queue_create(c"scala-native.serial", null)

  // Scaled down for a collector which never frees memory.
  var allocations = 2000

  def allocate(seed: Long): Long = {
    var total = 0L
    var retained: List[Array[Byte]] = Nil
    var i = 0
    while (i < allocations) {
      retained = new Array[Byte](256 + (i & 255)) :: retained
      if (i % 64 == 0) retained = retained.take(8)
      total += retained.head.length + seed.toString.length
      i += 1
    }
    total
  }

  val work: Work = CFuncPtr1.fromScalaFunction { (ctx: Ptr[Byte]) =>
    threads.add(Thread.currentThread())
    if (threadLocal.get() == null) threadLocal.set(new Array[Byte](4096))
    assert(allocate(ctx.toLong) > 0)
    if (ctx.toLong % 97 == 0) System.gc()
    completed.incrementAndGet()
    ()
  }

  val applyWork: Apply = CFuncPtr2.fromScalaFunction {
    (_: Ptr[Byte], iteration: CSize) =>
      allocate(iteration.toLong)
      completed.incrementAndGet()
      ()
  }

  val onFileEvents: fsevents.Callback = CFuncPtr6.fromScalaFunction {
    (
        _: Ptr[Byte],
        _: Ptr[Byte],
        count: CSize,
        paths: Ptr[CString],
        _: Ptr[CUnsignedInt],
        _: Ptr[CUnsignedLongLong]
    ) =>
      var i = 0
      while (i < count.toInt) {
        assert(fromCString(paths(i)).nonEmpty)
        allocate(i)
        fileEvents.incrementAndGet()
        i += 1
      }
  }

  def asyncBatch(tasks: Int): Unit = {
    completed.set(0)
    val group = dispatch_group_create()
    var i = 1
    while (i <= tasks) {
      dispatch_group_async_f(group, globalQueue, context(i.toLong), work)
      if (i % 500 == 0) allocate(i)
      i += 1
    }
    dispatch_group_wait(group, Forever.toULong)
    assert(completed.get() == tasks, s"${completed.get()} of $tasks tasks")
  }

  def queues(): Unit = {
    asyncBatch(4000)
    assert(threads.size() > 1, "expected multiple worker threads")

    completed.set(0)
    dispatch_apply_f(1000.toCSize, globalQueue, null, applyWork)
    assert(completed.get() == 1000)

    completed.set(0)
    var i = 1
    while (i <= 200) {
      dispatch_sync_f(serialQueue, context(i.toLong), work)
      i += 1
    }
    assert(completed.get() == 200)
  }

  /** Idle worker threads exit after a few seconds, new ones replace them. */
  def idle(): Unit = {
    asyncBatch(2000)
    var round = 0
    while (round < 2) {
      Thread.sleep(8000)
      asyncBatch(2000)
      System.gc()
      round += 1
    }
  }

  /** dispatch_main terminates the main thread and never returns. Only a
   *  single-threaded executable runs this, to check its diagnostic.
   */
  def dispatchMain(): Unit = {
    dispatch_after_f(after(200), globalQueue, context(1), work)
    dispatch_main()
  }

  def blocks(): Unit = {
    val tasks = 20000
    val done = dispatch_semaphore_create(0.toCSSize)
    completed.set(0)
    var i = 0
    while (i < tasks) {
      val payload = new Array[Byte](512)
      val last = i == tasks - 1
      val block = Blocks { () =>
        completed.addAndGet(payload.length / 512)
        if (last) dispatch_semaphore_signal(done)
        ()
      }
      dispatch_async(serialQueue, block)
      _Block_release(block)
      if (i % 1000 == 0) System.gc()
      i += 1
    }
    dispatch_semaphore_wait(done, Forever.toULong)
    assert(completed.get() == tasks)
    // The queue releases the last block shortly after invoking it.
    val deadline = System.nanoTime() + 10e9.toLong
    while ((Blocks.retained != 0 || Blocks.disposed.get() != tasks) &&
        System.nanoTime() < deadline)
      Thread.sleep(10)
    assert(Blocks.retained == 0, s"${Blocks.retained} closures leaked")
    assert(Blocks.disposed.get() == tasks)
  }

  def fileSystemEvents(): Unit = {
    import fsevents._
    val directory = Files.createTempDirectory("scala-native-fsevents")
    Zone.acquire { implicit zone =>
      val path = stackalloc[Ptr[Byte]]()
      !path = CFStringCreateWithCString(
        null,
        toCString(directory.toRealPath().toString),
        0x08000100.toUInt // kCFStringEncodingUTF8
      )
      val paths = CFArrayCreate(null, path, 1.toCSSize, null)
      val stream = FSEventStreamCreate(
        null,
        onFileEvents,
        null,
        paths,
        -1L.toULong, // kFSEventStreamEventIdSinceNow
        0.05,
        0x10.toUInt // kFSEventStreamCreateFlagFileEvents
      )
      FSEventStreamSetDispatchQueue(stream, serialQueue)
      assert(FSEventStreamStart(stream))
      val deadline = System.nanoTime() + 30e9.toLong
      var files = 0
      while (fileEvents.get() == 0 && System.nanoTime() < deadline) {
        Files.write(directory.resolve(s"file$files"), Array[Byte](1, 2))
        if (files % 10 == 0) System.gc()
        files += 1
        Thread.sleep(20)
      }
      FSEventStreamStop(stream)
      FSEventStreamInvalidate(stream)
      FSEventStreamRelease(stream)
      CFRelease(paths)
      CFRelease(!path)
    }
    assert(fileEvents.get() > 0, "no file system events delivered")
  }

  /** Network.framework reports to blocks on a dispatch queue. */
  def networkFramework(): Unit = {
    import network._
    val ListenerReady = 2 // nw_listener_state_ready
    val ConnectionReady = 3 // nw_connection_state_ready
    val accepted = dispatch_semaphore_create(0.toCSSize)
    val connected = dispatch_semaphore_create(0.toCSSize)
    val queue = dispatch_queue_create(c"scala-native.network", null)
    def configuration(name: CString): Ptr[Byte] =
      !Blocks.symbol(name).asInstanceOf[Ptr[Ptr[Byte]]]
    val parameters = nw_parameters_create_secure_tcp(
      configuration(c"_nw_parameters_configure_protocol_disable"),
      configuration(c"_nw_parameters_configure_protocol_default_configuration")
    )
    nw_parameters_set_local_endpoint(
      parameters,
      nw_endpoint_create_host(c"127.0.0.1", c"0")
    )
    val listener = nw_listener_create(parameters)
    nw_listener_set_queue(listener, queue)

    val onConnection = Blocks { (connection: Ptr[Byte]) =>
      allocate(1)
      System.gc()
      nw_connection_set_queue(connection, queue)
      nw_connection_start(connection)
      dispatch_semaphore_signal(accepted)
      ()
    }
    nw_listener_set_new_connection_handler(listener, onConnection)
    _Block_release(onConnection)

    val onListenerState = Blocks { (state: Int, _: Ptr[Byte]) =>
      allocate(2)
      if (state == ListenerReady) Zone.acquire { implicit zone =>
        val port = nw_listener_get_port(listener).toInt.toString
        val connection = nw_connection_create(
          nw_endpoint_create_host(c"127.0.0.1", toCString(port)),
          nw_parameters_create_secure_tcp(
            configuration(c"_nw_parameters_configure_protocol_disable"),
            configuration(
              c"_nw_parameters_configure_protocol_default_configuration"
            )
          )
        )
        nw_connection_set_queue(connection, queue)
        val onConnectionState = Blocks { (state: Int, _: Ptr[Byte]) =>
          allocate(3)
          System.gc()
          if (state == ConnectionReady) dispatch_semaphore_signal(connected)
          ()
        }
        nw_connection_set_state_changed_handler(connection, onConnectionState)
        _Block_release(onConnectionState)
        nw_connection_start(connection)
      }
    }
    nw_listener_set_state_changed_handler(listener, onListenerState)
    _Block_release(onListenerState)
    nw_listener_start(listener)

    assert(dispatch_semaphore_wait(accepted, after(30000)).toLong == 0)
    assert(dispatch_semaphore_wait(connected, after(30000)).toLong == 0)
    nw_listener_cancel(listener)
  }

  def main(args: Array[String]): Unit = {
    if (args.length > 1) allocations = 200 * args(1).toInt
    args.headOption.getOrElse("") match {
      case "queues"   => queues()
      case "idle"     => idle()
      case "main"     => dispatchMain()
      case "blocks"   => blocks()
      case "fsevents" => fileSystemEvents()
      case "network"  => networkFramework()
      case other      =>
        println(s"Unknown scenario: $other")
        System.exit(1)
    }
  }
}
