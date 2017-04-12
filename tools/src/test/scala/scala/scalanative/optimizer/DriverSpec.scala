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

  "The driver" should "support `passes` and `withPasses`" in {
    val empty = Driver.empty
    empty.passes should have length (0)
    val driver = empty.withPasses(Seq(P0))
    driver.passes should have length (1)
    driver.passes(0) should be(P0)
  }
}
