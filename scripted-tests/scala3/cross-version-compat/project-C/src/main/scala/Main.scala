package app

object Main {
  def main(args: Array[String]): Unit = {
    println(testlib.ADT.SingletonCase) // #2983
    println(testlib.ADT.ClassCase("foo"))
  }
}
