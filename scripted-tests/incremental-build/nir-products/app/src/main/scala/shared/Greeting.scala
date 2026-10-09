package shared

import scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

@EnableReflectiveInstantiation
class Greeting() {
  def this(value: String) = this()
}

@EnableReflectiveInstantiation
object Greeting {
  private val greeting: () => String = () => "from deleted app source"
  def text: String = greeting()
}
