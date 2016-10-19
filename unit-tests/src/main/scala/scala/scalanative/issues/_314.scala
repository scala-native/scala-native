package scala.scalanative.issues

object _314 extends tests.Suite {
  test("github.com/scala-native/scala-native/issues/314") {
    // Division by zero is undefined behavior in production mode.
    // Optimizer can assume it never happens and remove unused result.
    assert {
      try {
        5 / 0
        true
      } catch {
        case _: Throwable =>
          false
      }
    }
  }
}
