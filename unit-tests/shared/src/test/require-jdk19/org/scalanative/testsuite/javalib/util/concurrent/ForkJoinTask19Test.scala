/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Test, Ignore}
import JSR166Test._

import java.util.concurrent._

class ForkJoinTask19Test extends JSR166Test {

  /** adaptInterruptible(callable).toString() contains toString of wrapped task
   */
  @Test def testAdaptInterruptible_Callable_toString(): Unit = {
    if (testImplementationDetails) {
      val c: Callable[String] = () => ""
      val task = ForkJoinTask.adaptInterruptible(c)
      assertEquals(
        identityString(task) + "[Wrapped task = " + c.toString() + "]",
        task.toString()
      )
    }
  }
}
