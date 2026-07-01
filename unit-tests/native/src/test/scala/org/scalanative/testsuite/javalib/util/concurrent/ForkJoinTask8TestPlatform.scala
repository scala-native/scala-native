package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ForkJoinTask

import org.junit.Assume.assumeTrue

object ForkJoinTask8TestPlatform {
  def assumePollSubmission(): Unit =
    assumeTrue(
      "ForkJoinTask.pollSubmission is tested from require-jdk9 sources",
      false
    )

  def pollSubmission(): ForkJoinTask[_] =
    throw new AssertionError("unreachable")
}
