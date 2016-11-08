package scala.scalanative.issues

object _314Suite extends tests.Suite {
  test("#314") {
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
