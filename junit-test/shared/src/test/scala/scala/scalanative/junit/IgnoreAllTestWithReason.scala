package scala.scalanative.junit

import org.junit._

import scala.scalanative.junit.utils.JUnitTest

@Ignore("ignore reason")
class IgnoreAllTestWithReason {
  @Ignore("reason override") @Test def multiTest1(): Unit = ()
  @Ignore @Test def multiTest2(): Unit = ()
  @Test def multiTest3(): Unit = ()

  throw new Error("unreachable")

}

class IgnoreAllTestWithReasonAssertions extends JUnitTest
