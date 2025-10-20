package scala.scalanative
package junit

// Ported from Scala.js

import sbt.testing.*

private[junit] final class JUnitEvent(
    taskDef: TaskDef,
    _status: Status,
    _selector: Selector,
    _throwable: OptionalThrowable = new OptionalThrowable,
    _duration: Long = -1L
) extends Event {
  def status(): Status = _status
  def selector(): Selector = _selector
  def throwable(): OptionalThrowable = _throwable
  def duration(): Long = _duration
  def fullyQualifiedName(): String = taskDef.fullyQualifiedName()
  def fingerprint(): Fingerprint = taskDef.fingerprint()
}
