package scala.scalanative.build

/** The type of exception that is thrown when a Scala Native build fails. */
final class BuildException(message: String) extends Exception(message)
