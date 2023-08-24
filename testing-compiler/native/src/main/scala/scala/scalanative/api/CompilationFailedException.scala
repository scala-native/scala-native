package scala.scalanative.api

class CompilationFailedException(message: String) extends Exception(message) {
  def this() = this(null)
}

