import scala.scalanative.libc.stdio._
import scala.scalanative.unsafe._
object Test {
  
  def main(args: Array[String]): Unit = {
    val x = 42
    val y = x
    var z =  args.size
    // z = 0
    while (z > 10){
      z -= 1
    }
    val z2 = z + 1
    printf(c"%d %d %d\n", x, y, z2)
    
    // println(z2)

    // val a = "Foo".toLowerCase()
    // println(a)
    // println(z)
  }
}
