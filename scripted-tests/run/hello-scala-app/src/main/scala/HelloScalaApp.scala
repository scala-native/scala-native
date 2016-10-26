import scala.scalanative.native._, stdio._

object HelloScalaApp extends scala.App {
  val len = args.length

  assert(len == 3)
  assert(args(0).equals("hello"))
  assert(args(1).equals("scala"))
  assert(args(2).equals("app"))
}
