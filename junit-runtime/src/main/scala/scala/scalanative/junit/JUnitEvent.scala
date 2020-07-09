package scala.scalanative
package junit

// Ported from Scala.js

import sbt.testing._

private[junit] final class JUnitEvent(
    taskDef: TaskDef,
    val status: Status,
    val selector: Selector,
    val throwable: OptionalThrowable = new OptionalThrowable,
    val duration: Long = -1L
) extends Event {
  def fullyQualifiedName(): String = taskDef.fullyQualifiedName()
  def fingerprint(): Fingerprint   = taskDef.fingerprint()
}
