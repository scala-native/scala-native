import scala.concurrent._
import scala.concurrent.duration._

// Issue https://github.com/scala-native/scala-native/issues/3859
object Test {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    Await.result(Future.successful(1) map (_ + 1), Duration.Inf)
  }
}
