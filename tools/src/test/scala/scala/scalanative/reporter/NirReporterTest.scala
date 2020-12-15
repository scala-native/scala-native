package scala.scalanative.reporter

class NirReporterTest extends NirErrorReporterSpec {
  it should "verify the application of primitive functions" in {
    allows {
      s"""|object test {
          |  def foo(): Unit = {
          |    val x = List(1)
          |    new PlusOps[List, Int]
          |  }
          |
          |  class PlusOps[A[_], B]() {
          |  }
          |}""".stripMargin
    }
  }

}
