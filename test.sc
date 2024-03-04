import scala.concurrent.*
import scala.concurrent.duration.*

given ExecutionContext = ExecutionContext.global
private def loop(nextSchedule: Long): Future[Unit] = Future {
  if (System.currentTimeMillis() > nextSchedule) {
    println("GO")
    System.currentTimeMillis() + 100
  } else nextSchedule
  
}.flatMap { next => loop(next) }

Await.result(loop(0), 5.seconds)