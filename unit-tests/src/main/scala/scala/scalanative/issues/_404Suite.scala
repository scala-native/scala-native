package scala.scalanative.issues

object _404Suite extends tests.Suite {
  test("#404") {
    // this must not throw an exception
    this.getClass.##
  }
}
