# Embedding Scala Native

Build Scala Native as a static or shared library when another runtime needs to
call Scala code through a C ABI. Export your entry points with `@exported`, then
call them from the host's FFI or a C/C++ bridge. See [exported methods](interop.md#exported-methods)
for declarations and [build targets](sbt.md) for library configuration.

Automatic entry from foreign threads is currently in development. Use a Scala
Native version containing this support. For working integrations, see the
[static-library runtime tests](https://github.com/scala-native/scala-native/tree/main/scripted-tests/external-threads/runtime)
and [shared-library tests](https://github.com/scala-native/scala-native/tree/main/scripted-tests/external-threads/shared-library).
These build Scala libraries and call their exports from C/C++ hosts. See the
[test instructions](https://github.com/scala-native/scala-native/blob/main/scripted-tests/external-threads/README.md) to run them.

When linking a static Scala Native library into a C++ host on Linux, pass the
archive between `-Wl,--whole-archive` and `-Wl,--no-whole-archive`. This includes
the complete unwinder needed for exceptions crossing the library boundary.

## Initialize before calling Scala

For a static library, call `int ScalaNativeInit(void)` exactly once and check
that it returns zero before calling exported methods or Scala function pointers.
Serialize initialization in the host: do not initialize concurrently, repeat a
successful initialization, or enter Scala while initialization is in progress.
If initialization fails, do not call into the library.

Shared libraries initialize automatically when loaded. Do not call
`ScalaNativeInit()` again after that initialization. To initialize explicitly,
set the environment variable `SCALANATIVE_NO_DYLIB_CTOR` before loading the
library, or compile the library with `-DSCALANATIVE_NO_DYLIB_CTOR` in
`nativeConfig.compileOptions`. The variable's presence disables the constructor,
even when its value is `0`. Then initialize once as for a static library.

## Calls from foreign threads

Exported entry points and Scala function-pointer wrappers include
automatic entry guards. After initialization, a host OS thread can call these
entry points directly. The guard establishes the thread's Scala identity and
enters managed execution, where Scala allocation and GC interaction are allowed.
Returning restores the caller's previous execution state. Nested callbacks on
the same OS thread are supported.

Repeated callbacks on a host thread preserve its Scala `Thread` identity and
`ThreadLocal` values until normal thread exit. Ordinary
thread exit cleans up the attachment, provided no managed scopes remain active.

With multithreading enabled, different attached threads can execute Scala
concurrently. In a single-threaded build, only one OS thread can own the runtime
at a time. Automatic entry waits for ownership and releases it when the outermost
callback returns to the host.

Ownership remains held while Scala calls native code with Scala frames still
on the stack, including blocking native calls. Do not wait inside such a call
for a different thread to call back into a single-threaded runtime: that thread
would wait for ownership. Likewise, avoid holding host locks needed by the
current owner while waiting to enter Scala.

## Values, failures, and lifetime

Prefer C-compatible values and explicit ownership rules in the exported API.
Catch failures inside Scala entry points and translate them into status codes
or another host-visible error representation, especially for C and foreign FFIs
that cannot unwind Scala exceptions.

For C++ integration using supported exception unwinding, entry scopes are
restored when a Scala exception escapes a guarded callback. With the C++
exception backend, a retained `std::exception_ptr` keeps the Scala exception
rooted. Arbitrary C++ exceptions must not propagate through Scala frames.

Native storage retaining Scala object pointers is not automatically a GC root.
Register stable storage with `scalanative_GC_add_roots(low, high)` before
publishing a Scala pointer there, and remove it with
`scalanative_GC_remove_roots(low, high)` after the last use. The addresses describe
a half-open range and must remain valid until removal. Access retained Scala
objects through exported Scala methods and synchronize concurrent access.

Retain Scala objects in an established Scala or native root before the callback
returns. For example, expose opaque handles backed by a Scala-owned registry and
a release function. Do not return an unrooted Scala object pointer and attempt to
root it afterward: the object can be collected as soon as the callback returns.

Keep calls on their original OS stack. Native fiber switching, `longjmp` across
Scala frames, and forced thread termination that bypasses cleanup are unsupported.
Exiting a thread with active managed scopes terminates the process.

Keep the library loaded for the process lifetime. There is no public runtime
shutdown API that makes unloading safe, even after external threads detach.
Finish callbacks and join host workers before host shutdown.
