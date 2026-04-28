/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.{Ignore, Test}

class ForkJoinPool9Test extends JSR166Test {
  @Ignore("Requires MethodHandles.privateLookupIn and Thread.setContextClassLoader")
  @Test def testCommonPoolThreadContextClassLoader(): Unit = ()
}
