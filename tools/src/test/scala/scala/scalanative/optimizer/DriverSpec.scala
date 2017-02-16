package scala.scalanative
package optimizer

import tools.Config
import analysis.ClassHierarchy.Top

import org.scalatest._

class DriverSpec extends FlatSpec with Matchers {

  private def makeCompanion: PassCompanion =
    new PassCompanion {
      override def apply(config: Config, top: Top): Pass =
        new Pass {}
    }

  private val P0 = makeCompanion
  private val P1 = makeCompanion
  private val P2 = makeCompanion

  "The driver" should "support `append`" in {
    val driver = Driver.empty.append(P0)
    driver.passes should have length (1)
    driver.passes(0) should be(P0)
  }

  it should "support `takeUpTo`" in {
    val driver = Driver.empty.append(P0).append(P1).append(P2)
    driver.passes should have length (3)
    driver.passes should contain theSameElementsInOrderAs Seq(P0, P1, P2)

    val newDriver = driver.takeUpTo(P1)
    newDriver.passes should have length (2)
    newDriver.passes should contain theSameElementsInOrderAs Seq(P0, P1)
  }

  it should "support `takeBefore`" in {
    val driver = Driver.empty.append(P0).append(P1).append(P2)
    driver.passes should have length (3)
    driver.passes should contain theSameElementsInOrderAs Seq(P0, P1, P2)

    val newDriver = driver.takeBefore(P2)
    newDriver.passes should have length (2)
    newDriver.passes should contain theSameElementsInOrderAs Seq(P0, P1)
  }

}
