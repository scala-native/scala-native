package scala.scalanative.linker

/** Exception that is thrown when a Scala Native linking fails. */
final class LinkingException(message: String) extends Exception(message)
