package java.lang
package reflect

import scalanative.native.stub

class Constructor[T] extends Executable {

  @stub
  def getParameterTypes(): scala.Array[Object] = ???

  @stub
  def newInstance(
      args: scala.scalanative.runtime.ObjectArray): java.lang.Object = ???
}
