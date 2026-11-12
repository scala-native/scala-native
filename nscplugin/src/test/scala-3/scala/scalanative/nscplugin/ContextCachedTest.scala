package scala.scalanative.nscplugin

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}

import dotty.tools.dotc.core.Contexts.{Context, ContextBase, ctx}

import org.junit.Assert._
import org.junit.Test

class ContextCachedTest {

  // Issue #4950
  @Test def returnsValueOfCallerContextUnderContention(): Unit = {
    val cached = NirGenUtil.ContextCached(ctx)
    val threads = 4
    val iterations = 100000
    val pool = Executors.newFixedThreadPool(threads)
    val start = new CountDownLatch(1)
    val failure = new AtomicReference[String]()
    try {
      for (_ <- 0 until threads) pool.execute { () =>
        given Context = ContextBase().initialCtx
        start.await()
        var i = 0
        while (i < iterations && failure.get() == null) {
          if (cached.get ne summon[Context])
            failure.set("returned a value cached for another context")
          i += 1
        }
      }
      start.countDown()
      pool.shutdown()
      assertTrue(pool.awaitTermination(1, TimeUnit.MINUTES))
      assertNull(failure.get())
    } finally pool.shutdownNow()
  }
}
