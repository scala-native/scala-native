package org.scalanative.testsuite.javalib.invoke

import java.lang.invoke.VarHandle
import org.junit.Test

/** Availability smoke tests, not a proof of inter-thread ordering. */
class VarHandleFenceTest {
  @Test def fullFence(): Unit = VarHandle.fullFence()
  @Test def acquireFence(): Unit = VarHandle.acquireFence()
  @Test def releaseFence(): Unit = VarHandle.releaseFence()
  @Test def loadLoadFence(): Unit = VarHandle.loadLoadFence()
  @Test def storeStoreFence(): Unit = VarHandle.storeStoreFence()
}
