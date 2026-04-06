# JSR166 Update Tracking

## Overview

This document tracks the JSR166 (Doug Lea's concurrency utilities) versions ported to Scala Native and provides a plan for updating to the latest upstream versions.

## Current State

Scala Native ports JSR166 Java code to Scala rather than bundling the compiled JAR. The ported files live in:
- `javalib/src/main/scala/java/util/concurrent/` (52 files)
- `javalib/src/main/scala/java/util/concurrent/atomic/` (17 files)
- `javalib/src/main/scala/java/util/concurrent/locks/` (9 files)

**Total: 78 Scala files** ported from JSR166

## Latest Upstream JSR166

- **Source**: http://gee.cs.oswego.edu/dl/jsr166/
- **Latest JAR**: http://gee.cs.oswego.edu/dl/jsr166/dist/jsr166.jar (compiled using Java 17)
- **Browsable source**: http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/jsr166/src/main/java/util/
- **TCK tests**: http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/jsr166/src/tests/tck/
- **API docs**: http://gee.cs.oswego.edu/dl/jsr166/dist/docs/
- **OpenJDK Mirror**: https://github.com/openjdk/jdk/tree/master/src/java.base/share/classes/java/util/concurrent

## License & Copyright

**IMPORTANT**: JSR166 code has dual licensing:

1. **Original JSR166** (Doug Lea's code at oswego.edu): 
   - **Public Domain** - http://creativecommons.org/publicdomain/zero/1.0/
   - Can be freely used, modified, and distributed
   - This is what scala-native ports

2. **OpenJDK Packaging**:
   - GPL v2 + Classpath Exception (wrappers around JSR166)
   - The underlying JSR166 code remains public domain

**scala-native License Compatibility**:
- scala-native uses Apache 2.0 License
- Porting public domain JSR166 code is fully compatible
- Must retain attribution: "Written by Doug Lea with assistance from members of JCP JSR-166 Expert Group"

## Version Comparison

### Files with Explicit Revision Markers (scala-native vs OpenJDK Latest)

| Class | scala-native Rev | In OpenJDK | Priority | Notes |
|-------|:----------------:|:----------:|:--------:|-------|
| `ForkJoinPool` | **1.411** | ✓ | 🔴 High | Core executor framework |
| `CompletableFuture` | **1.225** | ✓ | 🔴 High | Critical async API (Jan 2021) |
| `Phaser` | **1.97** | ✓ | 🟡 Medium | Sync barrier (Jan 2021) |
| `CountedCompleter` | **1.72** | ✓ | 🟡 Medium | ForkJoinTask variant |
| `Future` | **1.47** | ✓ | 🟢 Low | Core interface |
| `ConcurrentNavigableMap` | **1.20** | ✓ | 🟢 Low | Concurrent sorted map |
| `DoubleAccumulator` | **1.44** | ✓ | 🟡 Medium | Atomic accumulation (Nov 2020) |
| `LongAccumulator` | **1.38** | ✓ | 🟡 Medium | Atomic accumulation (Nov 2020) |
| `Striped64` | **1.28** | ✓ | 🟡 Medium | Base for adders |
| `LongAdder` | **1.23** | ✓ | 🟡 Medium | High-throughput counter |
| `DoubleAdder` | **1.23** | ✓ | 🟡 Medium | High-throughput counter (Nov 2020) |

### scala-native Files Without Revision Markers (68 files)

These carry JSR-166 copyright but lack explicit revision numbers. Need to be compared against OpenJDK sources:

**java/util/concurrent/** (41 files):
AbstractExecutorService, ArrayBlockingQueue, BlockingDeque, BlockingQueue, BrokenBarrierException, Callable, CancellationException, CompletionException, CompletionService, CompletionStage, **ConcurrentHashMap**, ConcurrentLinkedDeque, ConcurrentLinkedQueue, ConcurrentMap, ConcurrentSkipListSet, CopyOnWriteArrayList, CountDownLatch, CyclicBarrier, Delayed, Executor, ExecutorCompletionService, Executors, ExecutorService, Flow, ForkJoinTask, ForkJoinWorkerThread, FutureTask, LinkedBlockingQueue, LinkedTransferQueue, PriorityBlockingQueue, RecursiveAction, RecursiveTask, RejectedExecutionHandler, RunnableFuture, RunnableScheduledFuture, ScheduledExecutorService, ScheduledFuture, ScheduledThreadPoolExecutor, Semaphore, SynchronousQueue, ThreadFactory, ThreadLocalRandom, ThreadPoolExecutor, TimeUnit, TransferQueue

**java/util/concurrent/atomic/** (12 files):
AtomicBoolean, AtomicInteger, AtomicIntegerArray, AtomicIntegerFieldUpdater, AtomicLong, AtomicLongArray, AtomicLongFieldUpdater, AtomicMarkableReference, AtomicReference, AtomicReferenceArray, AtomicReferenceFieldUpdater, AtomicStampedReference

**java/util/concurrent/locks/** (9 files):
AbstractOwnableSynchronizer, AbstractQueuedLongSynchronizer, AbstractQueuedSynchronizer, Condition, Lock, LockSupport, ReadWriteLock, ReentrantLock, ReentrantReadWriteLock, StampedLock

### Files in OpenJDK but NOT in scala-native (17 JSR166 files)

These exist in OpenJDK with JSR-166 attribution but haven't been ported to scala-native:

| Class | Category | Priority | Notes |
|-------|----------|:--------:|-------|
| **ConcurrentSkipListMap** | concurrent | 🔴 High | Concurrent sorted map implementation |
| **Exchanger** | concurrent | 🟡 Medium | Thread synchronization utility |
| **DelayQueue** | concurrent | 🟡 Medium | Priority queue with delays |
| **LinkedBlockingDeque** | concurrent | 🟡 Medium | Doubly-linked blocking queue |
| **SubmissionPublisher** | concurrent | 🟡 Medium | Reactive Streams publisher |
| **CancellationException** | concurrent | 🟢 Low | Exception class |
| **ExecutionException** | concurrent | 🟢 Low | Exception class |
| **RejectedExecutionException** | concurrent | 🟢 Low | Exception class |
| **TimeoutException** | concurrent | 🟢 Low | Exception class |
| AtomicBoolean | atomic | 🟢 Low | Already in scala-native |
| AtomicInteger | atomic | 🟢 Low | Already in scala-native |
| AtomicIntegerArray | atomic | 🟢 Low | Already in scala-native |
| AtomicLong | atomic | 🟢 Low | Already in scala-native |
| AtomicLongArray | atomic | 🟢 Low | Already in scala-native |
| AtomicReference | atomic | 🟢 Low | Already in scala-native |
| AtomicReferenceArray | atomic | 🟢 Low | Already in scala-native |
| Striped64 | atomic | 🟢 Low | Already in scala-native |

> **Note**: Some atomic classes appear in both repos - scala-native has them but the OpenJDK comparison script flagged them due to directory structure differences.

## Update Strategy

### Phase 1: Documentation (Current)
- [x] Document current JSR166 versions
- [x] Identify files needing updates
- [ ] Add revision markers to files missing them

### Phase 2: High-Priority Updates
These classes are most critical and likely to have significant upstream changes:

1. **ConcurrentHashMap** - Core concurrent data structure, heavily used
2. **CompletableFuture** - Complex async API, frequent improvements
3. **ForkJoinPool** - Thread pool executor backbone
4. **ConcurrentLinkedQueue/Deque** - Lock-free queues
5. **AbstractQueuedSynchronizer** - Foundation for locks

### Phase 3: Systematic Porting

For each class requiring update:

1. **Fetch upstream Java source** from CVS
2. **Diff against current Scala port** to identify changes
3. **Port changes to Scala**, maintaining:
   - Scala Native conventions
   - Existing platform-specific adaptations (stdatomics, Intrinsics)
   - Test coverage
4. **Run tests** to verify correctness
5. **Update revision marker** in file header

### Porting Notes

The Scala Native port has specific adaptations:
- Uses `scala.scalanative.libc.stdatomic` instead of `java.lang.invoke.VarHandle`
- Uses `scala.scalanative.runtime.Intrinsics` for low-level operations
- Some Java idioms are adapted to Scala patterns
- Platform-specific code for native compilation

## References

- **JSR166 Homepage**: http://gee.cs.oswego.edu/dl/jsr166/
- **Concurrency Interest Mailing List**: https://mail.cs.oswego.edu/mailman/listinfo/concurrency-interest/
- **Doug Lea's Publications**: http://gee.cs.oswego.edu/dl/

## Tracking Issues

- [ ] Update ConcurrentHashMap to latest revision
- [ ] Update CompletableFuture to latest revision
- [ ] Update ForkJoinPool to latest revision
- [ ] Add revision markers to all JSR166-ported files
- [ ] Verify TCK test compatibility
- [ ] Update this document after each major class update

## Last Updated

2026-04-07
