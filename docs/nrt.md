# Native Runtime (NRT)

NRT is a C library that provide an implementation of garbage collector and intrinsics.
We assume that LLVM bitcode of the implementation is available to enable
inlining across runtime-application boundary at link time.

## Garbage Collector

* Based on
  ["Efficient On-the-Fly Cycle Collection"](http://dl.acm.org/citation.cfm?id=1255453).
  Delayed reference counting with cycle detection. Optimised for low pause times that
  are required for latency-sensitive applications.

* Precise on heap, conservative on stack.
  This allows us to have smooth handle-free interop with C code and
  not interfere with LLVM optimisations by not having to insert
  additional gc-related instructions into the compiled bitcode.

* Safe points are inserted before return from the function call and on the
  back edges of the loops. The insertion is performed by custom pass which is run
  after LLVM optimisation passes.

## Intrinsics

Intrinsics are implemented as functions with the name of intrinsic prepended by `nrt_`
prefix. Type signatures of the intrinsics are translated according to following
type correspondance rules:

 NIR Type         | C Type                   | C Convenience Alias
------------------|--------------------------|----------------------
 `void`           | `void`                   | `nrt_void`
 `bool`           | `bool`                   | `nrt_bool`
 `i8`, ..., `i64` | `int8_t`, ..., `int64_t` | `nrt_i8`, ..., `nrt_i64`
 `f32`, `f64`     | `float`, `double`        | `nrt_f32`, `nrt_f64`
 `[T x N]`        | `T[N]`                   | n/a
 `ptr T`          | `T*`                     | n/a
 `struct $name`   | `struct $name`           | n/a
 `size`           | `size_t`                 | `nrt_size`
 class types      | `void*`                  | `nrt_obj`

So for example `#int_box` is going to have following C signature:

```C
nrt_obj nrt_int_box(nrt_i32 value);
```
