package java.util.regex

object Pattern {
  def compile(regex: String): Pattern = {
    println(regex)
    new Pattern
  }

  def matches(regex: String, input: CharSequence): Boolean = ???
}

class Pattern {
  def split(seq: java.lang.CharSequence): Array[String] = {
    println(seq.toString())
    ???
  }

  def split(input: CharSequence, limit: Int): Array[String] = ???

  def matcher(seq: CharSequence): Matcher = ???
  def pattern: String                     = ???
}
