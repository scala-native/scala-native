package scala.runtime

object ArrayRuntime {
  def cloneArray(array: Array[scala.Boolean])    = array.clone
  def cloneArray(array: Array[scala.Byte])       = array.clone
  def cloneArray(array: Array[scala.Short])      = array.clone
  def cloneArray(array: Array[scala.Char])       = array.clone
  def cloneArray(array: Array[scala.Int])        = array.clone
  def cloneArray(array: Array[scala.Long])       = array.clone
  def cloneArray(array: Array[scala.Float])      = array.clone
  def cloneArray(array: Array[scala.Double])     = array.clone
  def cloneArray(array: Array[java.lang.Object]) = array.clone
}
