package test

class C {
  def newArr = new Array[Int](32)
  def arrApply = Array(1, 2, 3)
  def get = newArr(0)
  def set = newArr(0) = 1
  def length = newArr.length
}
