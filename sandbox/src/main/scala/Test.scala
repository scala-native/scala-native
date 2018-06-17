object Test {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    println(scala.scalanative.runtime.TargetArchitectureNative.__targetArchitecture)
    println(scala.scalanative.runtime.TargetArchitecture.current)
  }
}
