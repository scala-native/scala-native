package tests

import sbt.testing._

final case class NativeEvent(override val fullyQualifiedName: String,
                             testName: String,
                             override val fingerprint: Fingerprint,
                             override val status: Status)
    extends Event {

  override def selector(): Selector =
    new NestedTestSelector(fullyQualifiedName, testName)
  override def duration(): Long =
    -1L
  override def throwable(): OptionalThrowable =
    new OptionalThrowable()
}
