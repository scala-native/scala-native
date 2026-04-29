package org.scalanative.testsuite.javalib.util.concurrent

import java.lang.invoke.{MethodHandle, MethodHandles, MethodType}
import java.util.concurrent.ForkJoinTask

import org.junit.Assume.assumeTrue

object ForkJoinTask8TestPlatform {
  private object PollSubmissionBridge extends ForkJoinTask[AnyRef] {
    override def getRawResult(): AnyRef = null
    override def setRawResult(value: AnyRef): Unit = ()
    override def exec(): Boolean = false

    def lookupPollSubmission(): Option[MethodHandle] =
      try {
        Some(
          MethodHandles
            .lookup()
            .findStatic(
              classOf[ForkJoinTask[_]],
              "pollSubmission",
              MethodType.methodType(classOf[ForkJoinTask[_]])
            )
        )
      } catch {
        case _: NoSuchMethodException | _: IllegalAccessException => None
      }
  }

  private val pollSubmissionMethod =
    try PollSubmissionBridge.lookupPollSubmission()
    catch {
      case _: NoClassDefFoundError => None
    }

  def assumePollSubmission(): Unit =
    assumeTrue(
      "ForkJoinTask.pollSubmission requires JDK 9+",
      pollSubmissionMethod.isDefined
    )

  def pollSubmission(): ForkJoinTask[_] =
    pollSubmissionMethod.get.invokeWithArguments().asInstanceOf[ForkJoinTask[_]]
}
