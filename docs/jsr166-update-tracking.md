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

## Version Comparison

### Files with Explicit Revision Markers

| File | Current Revision | Date | Notes |
|------|:----------------:|------|-------|
| `ForkJoinPool.scala` | **1.411** | - | Major executor framework class |
| `CompletableFuture.scala` | **1.225** | Jan 19, 2021 | Critical async API |
| `Phaser.scala` | **1.97** | 2021-01-31 | Synchronization barrier |
| `CountedCompleter.scala` | **1.72** | - | ForkJoinTask variant |
| `Future.scala` | **1.47** | - | Core future interface |
| `ConcurrentNavigableMap.scala` | **1.20** | - | Concurrent sorted map |
| `DoubleAccumulator.scala` | **1.44** | 2020-11-27 | Atomic accumulation |
| `LongAccumulator.scala` | **1.38** | 2020-11-27 | Atomic accumulation |
| `Striped64.scala` | **1.28** | - | Base for adders/accumulators |
| `LongAdder.scala` | **1.23** | - | High-throughput counter |
| `DoubleAdder.scala` | **1.23** | 2020-11-27 | High-throughput counter |

### Files Without Revision Markers

These files carry the JSR-166 copyright but lack explicit revision numbers:

**java/util/concurrent/** (47 files):
- AbstractExecutorService.scala
- ArrayBlockingQueue.scala
- BlockingDeque.scala
- BlockingQueue.scala
- BrokenBarrierException.scala
- Callable.scala
- CancellationException.scala
- CompletionException.scala
- CompletionService.scala
- CompletionStage.scala
- ConcurrentHashMap.scala
- ConcurrentLinkedDeque.scala
- ConcurrentLinkedQueue.scala
- ConcurrentMap.scala
- ConcurrentSkipListMap.scala
- ConcurrentSkipListSet.scala
- CopyOnWriteArrayList.scala
- CopyOnWriteArraySet.scala
- CountDownLatch.scala
- CyclicBarrier.scala
- Delayed.scala
- DelayQueue.scala
- Exchanger.scala
- ExecutionException.scala
- Executor.scala
- ExecutorCompletionService.scala
- Executors.scala
- ExecutorService.scala
- Flow.scala
- ForkJoinTask.scala
- ForkJoinWorkerThread.scala
- FutureTask.scala
- Helpers.scala
- LinkedBlockingDeque.scala
- LinkedBlockingQueue.scala
- LinkedTransferQueue.scala
- package-info.scala
- PriorityBlockingQueue.scala
- RecursiveAction.scala
- RecursiveTask.scala
- RejectedExecutionException.scala
- RejectedExecutionHandler.scala
- RunnableFuture.scala
- RunnableScheduledFuture.scala
- ScheduledExecutorService.scala
- ScheduledFuture.scala
- ScheduledThreadPoolExecutor.scala
- Semaphore.scala
- SubmissionPublisher.scala
- SynchronousQueue.scala
- ThreadFactory.scala
- ThreadLocalRandom.scala
- ThreadPoolExecutor.scala
- TimeoutException.scala
- TimeUnit.scala
- TransferQueue.scala

**java/util/concurrent/atomic/** (12 files):
- AtomicBoolean.scala
- AtomicInteger.scala
- AtomicIntegerArray.scala
- AtomicIntegerFieldUpdater.scala
- AtomicLong.scala
- AtomicLongArray.scala
- AtomicLongFieldUpdater.scala
- AtomicMarkableReference.scala
- AtomicReference.scala
- AtomicReferenceArray.scala
- AtomicReferenceFieldUpdater.scala
- AtomicStampedReference.scala

**java/util/concurrent/locks/** (9 files):
- AbstractOwnableSynchronizer.scala
- AbstractQueuedLongSynchronizer.scala
- AbstractQueuedSynchronizer.scala
- Condition.scala
- Lock.scala
- LockSupport.scala
- package-info.scala
- ReadWriteLock.scala
- ReentrantLock.scala
- ReentrantReadWriteLock.scala
- StampedLock.scala

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
