package scala.util

object PropertiesSuite extends tests.Suite {
  test("Properties.releaseVersion") {
    assert(Properties.releaseVersion != null)
  }

  test("Properties.versionNumberString") {
    assert(Properties.versionNumberString != null)
  }

  test("Properties.copyrightString") {
    assert(Properties.copyrightString != null)
  }
}
