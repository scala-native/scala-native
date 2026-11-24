import scala.scalanative.unsafe._

@extern
object dispatch {
  type Work = CFuncPtr1[Ptr[Byte], Unit]
  type Apply = CFuncPtr2[Ptr[Byte], CSize, Unit]

  def dispatch_get_global_queue(identifier: CSSize, flags: CSize): Ptr[Byte] =
    extern
  def dispatch_queue_create(label: CString, attr: Ptr[Byte]): Ptr[Byte] =
    extern
  def dispatch_async(queue: Ptr[Byte], block: Ptr[Byte]): Unit = extern
  def dispatch_group_create(): Ptr[Byte] = extern
  def dispatch_group_async_f(
      group: Ptr[Byte],
      queue: Ptr[Byte],
      context: Ptr[Byte],
      work: Work
  ): Unit = extern
  def dispatch_after_f(
      when: CUnsignedLongLong,
      queue: Ptr[Byte],
      context: Ptr[Byte],
      work: Work
  ): Unit = extern
  def dispatch_time(
      when: CUnsignedLongLong,
      delta: CLongLong
  ): CUnsignedLongLong = extern
  def dispatch_semaphore_create(value: CSSize): Ptr[Byte] = extern
  def dispatch_semaphore_signal(semaphore: Ptr[Byte]): CSSize = extern

  // Calls waiting for work done on other threads must not delay their GC.
  @blocking def dispatch_sync_f(
      queue: Ptr[Byte],
      context: Ptr[Byte],
      work: Work
  ): Unit = extern
  @blocking def dispatch_apply_f(
      iterations: CSize,
      queue: Ptr[Byte],
      context: Ptr[Byte],
      work: Apply
  ): Unit = extern
  @blocking def dispatch_group_wait(
      group: Ptr[Byte],
      timeout: CUnsignedLongLong
  ): CSSize = extern
  @blocking def dispatch_semaphore_wait(
      semaphore: Ptr[Byte],
      timeout: CUnsignedLongLong
  ): CSSize = extern
  @blocking def dispatch_main(): Unit = extern

  def _Block_copy(block: Ptr[Byte]): Ptr[Byte] = extern
  def _Block_release(block: Ptr[Byte]): Unit = extern
  def dlsym(handle: Ptr[Byte], name: CString): Ptr[Byte] = extern
}

@extern
object fsevents {
  type Callback = CFuncPtr6[
    Ptr[Byte],
    Ptr[Byte],
    CSize,
    Ptr[CString],
    Ptr[CUnsignedInt],
    Ptr[CUnsignedLongLong],
    Unit
  ]
  def FSEventStreamCreate(
      allocator: Ptr[Byte],
      callback: Callback,
      context: Ptr[Byte],
      pathsToWatch: Ptr[Byte],
      sinceWhen: CUnsignedLongLong,
      latency: CDouble,
      flags: CUnsignedInt
  ): Ptr[Byte] = extern
  def FSEventStreamSetDispatchQueue(stream: Ptr[Byte], queue: Ptr[Byte]): Unit =
    extern
  def FSEventStreamStart(stream: Ptr[Byte]): Boolean = extern
  def FSEventStreamStop(stream: Ptr[Byte]): Unit = extern
  def FSEventStreamInvalidate(stream: Ptr[Byte]): Unit = extern
  def FSEventStreamRelease(stream: Ptr[Byte]): Unit = extern
  def CFStringCreateWithCString(
      allocator: Ptr[Byte],
      string: CString,
      encoding: CUnsignedInt
  ): Ptr[Byte] = extern
  def CFArrayCreate(
      allocator: Ptr[Byte],
      values: Ptr[Ptr[Byte]],
      count: CSSize,
      callbacks: Ptr[Byte]
  ): Ptr[Byte] = extern
  def CFRelease(value: Ptr[Byte]): Unit = extern
}

@extern
object network {
  def nw_parameters_create_secure_tcp(
      configureTls: Ptr[Byte],
      configureTcp: Ptr[Byte]
  ): Ptr[Byte] = extern
  def nw_parameters_set_local_endpoint(
      parameters: Ptr[Byte],
      endpoint: Ptr[Byte]
  ): Unit = extern
  def nw_endpoint_create_host(host: CString, port: CString): Ptr[Byte] =
    extern
  def nw_listener_create(parameters: Ptr[Byte]): Ptr[Byte] = extern
  def nw_listener_set_queue(listener: Ptr[Byte], queue: Ptr[Byte]): Unit =
    extern
  def nw_listener_set_state_changed_handler(
      listener: Ptr[Byte],
      handler: Ptr[Byte]
  ): Unit = extern
  def nw_listener_set_new_connection_handler(
      listener: Ptr[Byte],
      handler: Ptr[Byte]
  ): Unit = extern
  def nw_listener_start(listener: Ptr[Byte]): Unit = extern
  def nw_listener_get_port(listener: Ptr[Byte]): CUnsignedShort = extern
  def nw_listener_cancel(listener: Ptr[Byte]): Unit = extern
  def nw_connection_create(
      endpoint: Ptr[Byte],
      parameters: Ptr[Byte]
  ): Ptr[Byte] = extern
  def nw_connection_set_queue(connection: Ptr[Byte], queue: Ptr[Byte]): Unit =
    extern
  def nw_connection_set_state_changed_handler(
      connection: Ptr[Byte],
      handler: Ptr[Byte]
  ): Unit = extern
  def nw_connection_start(connection: Ptr[Byte]): Unit = extern
  def nw_connection_cancel(connection: Ptr[Byte]): Unit = extern
}
