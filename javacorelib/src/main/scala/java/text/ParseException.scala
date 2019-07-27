package java.text

class ParseException(s: String, errorOffset: Int) extends Exception(s) {
  def getErrorOffset(): Int = errorOffset
}
