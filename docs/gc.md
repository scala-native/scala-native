# Garbage Collector

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