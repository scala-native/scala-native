package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ForkJoinTask

object ForkJoinTask8TestPlatform {
  private object PollSubmissionBridge extends ForkJoinTask[AnyRef] {
    override def getRawResult(): AnyRef = null
    override def setRawResult(value: AnyRef): Unit = ()
    override def exec(): Boolean = false

    def pollSubmission(): ForkJoinTask[_] =
      ForkJoinTask.pollSubmission()
  }

  def assumePollSubmission(): Unit = ()

  def pollSubmission(): ForkJoinTask[_] =
    PollSubmissionBridge.pollSubmission()
}
