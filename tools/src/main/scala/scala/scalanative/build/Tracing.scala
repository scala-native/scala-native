package scala.scalanative.build

import com.github.plokhotnyuk.jsoniter_scala.core._

import concurrent.*
import fxprof.tracer.Tracer
import fxprof.{Profile, ProfileMeta}

trait Tracing {
  def use[A](label: String)(f: ScalaNativeTracer => A): A
  def open(label: String): ScalaNativeTracer
  def useAsync[A](label: String)(f: ScalaNativeTracer => Future[A])(implicit
      ec: ExecutionContext
  ): Future[A]
  def close(label: String): Unit
}

class ScalaNativeTracer(
    defaultCategory: String,
    tracer: fxprof.tracer.Tracer
) extends Tracer {

  def symSpan[A](sym: scala.scalanative.nir.Global)(f: => A) =
    tracer.span(sym.mangle, defaultCategory)(f)

  override def span[A](name: String)(f: => A): A =
    tracer.span(name, defaultCategory)(f)

  override def span[A](name: String, category: String)(f: => A): A =
    tracer.span(name, category)(f)

  override def close(): Unit = tracer.close()
}

object ScalaNativeTracer {
  val noop = new ScalaNativeTracer("", Tracer.noop)
}

class RealTracing private[build] (
    tracer: ProfileMeta => fxprof.tracer.RealTracer,
    dest: java.nio.file.Path,
    logger: Logger
) extends Tracing {

  private val opened =
    collection.mutable.Map.empty[String, fxprof.tracer.RealTracer]

  def open(label: String): ScalaNativeTracer = {
    val tracerInstance = tracer(Tracing.meta)
    opened.synchronized {
      opened(label) = tracerInstance
      new ScalaNativeTracer(label, tracerInstance)
    }
  }

  def close(label: String): Unit = {
    val tracerInstance = opened(label)
    opened.synchronized {
      opened.remove(label)
    }
    val tracerDest = dest.resolve(s"fxprof-$label.json")
    val fw = new java.io.FileWriter(tracerDest.toFile)
    fw.write(writeToString(tracerInstance.build()))
    fw.close()
  }

  def useAsync[A](
      category: String
  )(
      f: ScalaNativeTracer => Future[A]
  )(implicit ec: ExecutionContext): Future[A] = {
    val tracerDest = dest.resolve(s"fxprof-$category.json")

    val tracerInstance = tracer(Tracing.meta)
    val result = f(new ScalaNativeTracer(category, tracerInstance))

    result.onComplete {
      case util.Success(value) =>
        val fw = new java.io.FileWriter(tracerDest.toFile)
        fw.write(writeToString(tracerInstance.build()))
        fw.close()

        logger.info(
          s"Firefox Profiler file ($category) was written to $tracerDest"
        )
      case _ =>
    }

    result
  }

  def use[A](category: String)(f: ScalaNativeTracer => A) = {
    val tracerDest = dest.resolve(s"fxprof-$category.json")

    val tracerInstance = tracer(Tracing.meta)
    val result = f(new ScalaNativeTracer(category, tracerInstance))

    val fw = new java.io.FileWriter(tracerDest.toFile)
    fw.write(writeToString(tracerInstance.build()))
    fw.close()

    logger.info(
      s"Firefox Profiler file ($category) was written to $tracerDest"
    )

    result
  }
}

object Tracing {
  def real(destDir: java.nio.file.Path, logger: Logger): Tracing =
    new RealTracing(Tracer(_), destDir, logger)

  val noop: Tracing = new Tracing {
    def open(label: String): ScalaNativeTracer = ScalaNativeTracer.noop
    def close(label: String): Unit = ()

    override def use[A](label: String)(f: ScalaNativeTracer => A): A = f(
      ScalaNativeTracer.noop
    )

    override def useAsync[A](
        label: String
    )(
        f: ScalaNativeTracer => Future[A]
    )(implicit ec: ExecutionContext): Future[A] = f(
      ScalaNativeTracer.noop
    )
  }

  import fxprof.*

  private[build] def meta = ProfileMeta(
    interval = 1.0,
    startTime = System.currentTimeMillis(),
    processType = 1.0,
    product = ProfileMeta_Product.Other("scala-native"),
    stackwalk = ProfileMeta_Stackwalk.False,
    version = 1.0,
    preprocessedProfileVersion = 58
  )
    // Categories defined below can be used to start spans
    // if a span is started with a category that is not defined here, "default" will be used
    // "default" is added implicitly to the profile when it's serialised
    .withCategories(
      Some(
        Vector(
          Category(REACH, CategoryColor.Blue),
          Category(EMIT, CategoryColor.Green),
          Category(LOWER, CategoryColor.Red),
          Category(INTERFLOW, CategoryColor.Yellow),
          Category(INTERFLOW_RELINK, CategoryColor.Blue),
          Category(DEFAULT, CategoryColor.Grey)
        )
      )
    )

  val REACH = "reach"
  val EMIT = "emit"
  val LOWER = "lower"
  val INTERFLOW = "interflow"
  val INTERFLOW_RELINK = "interflow-relink"
  val DEFAULT = "default"

}
