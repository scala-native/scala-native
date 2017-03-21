package java.security

trait Principal {
  def getName(): String

  // TODO:
  // def implies(subject: Subject): Boolean = ???
}
