import scala.scalanative.test

class SampleTest extends test.Test {

  override def test = {
    everythingWorks
  }

  def everythingWorks =
    println("Yay!")
}
