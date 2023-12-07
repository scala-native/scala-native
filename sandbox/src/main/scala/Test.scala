import scala.scalanative.runtime.Intrinsics
import scala.scalanative.unsafe._
import scala.scalanative.runtime.fromRawPtr
class Point(val x: Int, var y: Int, inner: Option[Point] = None){
  def coordinates: String = s"$x:$y"
}

object Test {
  case class InnerPoint(x: Int, y: Int, inner: Option[InnerPoint] = None){
      def coordinates: String = s"$x:$y"
  }
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    val x1 = new Point(1,2)
    val x2 = new Point(-1, -2, Some(x1))

    val x3 = InnerPoint(x1.x, x2.x)
    val x4 = InnerPoint(x2.y, x1.y, Some(x3))

    var ints = Array(1, 2,3,4, 5,6, 7, 8)
    println(fromRawPtr(Intrinsics.castObjectToRawPtr(ints)))
    println(ints.at(0))
    var longs = Array(1, 2,3,4, 5,6, 7, 8).map(_.toLong)
    println(fromRawPtr(Intrinsics.castObjectToRawPtr(longs)))
    println(longs.at(0))
    longs = longs.map(_ * -1L)
    var bytes = Array[Byte](1,2,3,4,5,6,7,8, 9, 10, 11,12,13,14,15,16)
    println(fromRawPtr(Intrinsics.castObjectToRawPtr(bytes)))
    println(bytes.at(0))
    var bytes2 = ('a' to 'z').map(_.toByte).toArray
    ints.foreach(println)
    bytes.foreach(println)
    bytes = bytes.map(v => (-v).toByte)
    bytes2.foreach(println)
    bytes2 = bytes.map(_.toChar.toUpper.toByte)
    Seq(x1, x2, x3, x4).foreach(println)
    println(Seq(x1, x2).map(_.coordinates).mkString(", ") ++ Seq(x3, x4).map(_.coordinates).mkString("|"))
    var points = Array(x1, x2)
    val pointsPtr = fromRawPtr[AnyRef](Intrinsics.castObjectToRawPtr(points))
    println(pointsPtr.toLong)
    println(points.at(0).toLong)

    points.foreach(println)
    if(args.size > 0) points = Array(x1, x2, x1, x2)
    points.foreach(println)
    val points2 = points.map(_ => "foo")
    points2.foreach(println)
    assert(points.mkString == points.mkString)
  }
}
