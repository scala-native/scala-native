package runtime

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

import microbench.*

/** Single-threaded cost of synchronization primitives: uncontended (thin)
 *  monitors, reentrancy, `ReentrantLock`, atomics, `ThreadLocal`, `@volatile`.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class Monitors:
  private val monitor = new Object
  private val lock = new ReentrantLock()
  private val atomic = new AtomicInteger()
  private val threadLocal =
    ThreadLocal.withInitial[Integer](() => Integer.valueOf(1))
  @volatile private var volatileField = 0
  private var counter = 0

  @Benchmark def baseline(): Int =
    counter += 1
    counter

  @Benchmark def synchronizedBlock(): Int =
    monitor.synchronized:
      counter += 1
      counter

  @Benchmark def synchronizedNested(): Int =
    monitor.synchronized:
      monitor.synchronized:
        counter += 1
        counter

  /** Lock a fresh object each time: monitor inflation / first-lock path. */
  @Benchmark def synchronizedFresh(): Int =
    val o = new Object
    o.synchronized:
      counter += 1
      counter

  @Benchmark def reentrantLock(): Int =
    lock.lock()
    try
      counter += 1
      counter
    finally lock.unlock()

  @Benchmark def atomicIncrement(): Int = atomic.incrementAndGet()

  @Benchmark def volatileWrite(): Int =
    counter += 1
    volatileField = counter
    counter

  @Benchmark def volatileRead(): Int = volatileField

  @Benchmark def threadLocalGet(): Integer = threadLocal.get()
