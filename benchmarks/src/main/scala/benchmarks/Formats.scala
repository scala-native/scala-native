package benchmarks

import scala.compat.Platform.EOL

/** Describes hwo to display the benchmarks results */
trait Format {

  /** Shows the given benchmark results. */
  def show(results: Seq[BenchmarkResult]): String

}

object Format {

  /** Returns the format matching `name`, defaults to `TextFormat`. */
  def apply(name: String): Format = formats.getOrElse(name, TextFormat)

  private val formats: Map[String, Format] =
    Map("text" -> TextFormat, "csv" -> CSVFormat)
}

/** A verbose human-friendly format. */
object TextFormat extends Format {

  override def show(results: Seq[BenchmarkResult]): String = {

    val title = new Formattable {
      override def elements =
        Seq("Result",
            "Name",
            "iter",
            "min [ms]",
            "max [ms]",
            "avg [ms]",
            "stddev [ms]",
            "median [ms]",
            "p05 [ms]",
            "p95 [ms]",
            "avg [ms]")
    }

    val rows = results.sortBy {
      case _: BenchmarkCompleted => 0
      case _: BenchmarkDisabled  => 1
      case _: BenchmarkFailed    => 2
    } map {
      case completed: BenchmarkCompleted =>
        CompletedRow(completed)
      case failed: BenchmarkFailed =>
        FailedRow(failed.name, failed.cause)
      case disabled: BenchmarkDisabled =>
        DisabledRow(disabled.name)
    }

    Formattable.format(title +: rows)
  }

}

/** Displays the benchmark results as CSV. */
object CSVFormat extends Format {
  override def show(results: Seq[BenchmarkResult]): String = {
    val rows =
      results.collect {
        case completed: BenchmarkCompleted =>
          CompletedRow(completed)
      }

    rows.map(_.elements.mkString(",")).mkString(EOL)

  }
}

/** Trait for objects that can be formatted into a table */
trait Formattable {
  def elements: Seq[String]

  final def format(widths: Seq[Int]): String =
    elements.zip(widths).map((Formattable.pad _).tupled).mkString(" ")
}

object Formattable {

  /** Adds spaces after `content` to make it as long as `width`. */
  def pad(content: String, width: Int): String = {
    val insert = Math.max(0, width - content.length)
    content + " " * insert
  }

  /** Returns the maximum widths for each column across all `fs`. */
  def getWidths(fs: Seq[Formattable]): Seq[Int] = {
    val rowElements = fs.map(_.elements.length).max

    (0 until rowElements).map { i =>
      fs.flatMap(_.elements.lift(i).map(_.length)).max
    }
  }

  /** Formats the sequence `fs` in a table. */
  def format(fs: Seq[Formattable]): String = {
    val widths = getWidths(fs)
    fs.map(_.format(widths)).mkString(EOL)
  }
}

/** A row in the table for a benchmark that completed. */
case class CompletedRow(name: String,
                        success: String,
                        iterations: Int,
                        min: String,
                        max: String,
                        avg: String,
                        stddev: String,
                        median: String,
                        p95: String,
                        p05: String,
                        avgBetweenP05AndP95: String)
    extends Formattable {
  override def elements: Seq[String] =
    Seq(success,
        name,
        iterations.toString,
        min,
        max,
        avg,
        stddev,
        median,
        p95,
        p05,
        avgBetweenP05AndP95)
}

object CompletedRow {
  def apply(completed: BenchmarkCompleted): CompletedRow = {
    import StatUtils._
    import completed._

    val successStr = if (success) "[OK]" else "[FAIL]"
    val timesMs    = timesNs map (_ / 1e6)
    val sortedMs   = timesMs.sorted
    val minMs      = timesMs.min
    val maxMs      = timesMs.max
    val avgMs      = average(timesMs)
    val medianMs   = percentile(50, sortedMs)
    val p95Ms      = percentile(95, sortedMs)
    val p05Ms      = percentile(5, sortedMs)
    val iterations = timesNs.length
    val stddevMs =
      Math.sqrt(timesMs.map(t => Math.pow(t - avgMs, 2) / iterations).sum)
    val avgBetweenP05AndP95 =
      average(timesMs filter (t => t >= p05Ms && t <= p95Ms))

    CompletedRow(
      name,
      successStr,
      iterations,
      format(minMs),
      format(maxMs),
      format(avgMs),
      format(stddevMs),
      format(medianMs),
      format(p95Ms),
      format(p05Ms),
      format(avgBetweenP05AndP95)
    )
  }
}

/** A row in the table for a benchmark that is disabled. */
case class DisabledRow(name: String) extends Formattable {
  override def elements: Seq[String] =
    Seq("[???]", s"$name is disabled.")
}

/** A row in the table for a benchmark that failed during execution. */
case class FailedRow(name: String, cause: Throwable) extends Formattable {
  override def elements: Seq[String] =
    Seq("[FAIL]",
        s"""$name has failed:
           |${cause.getMessage}
           |${cause.getStackTrace.mkString(EOL)}""".stripMargin)
}

/** Collection of functions useful to display the benchmark results. */
private object StatUtils {

  /** Truncates `n` at `decimals` decimals. */
  def format(n: Double, decimals: Int = 3): String = {
    val s = n.toString
    s.substring(0, s.indexOf('.') + decimals + 1)
  }

  /** Computes the `n`th percentile of `sortedData`. */
  def percentile[T](n: Int, sortedData: Seq[T]): T = {
    val index = Math.ceil(n * sortedData.length / 100.0).toInt - 1
    sortedData(index)
  }

  /** Computes the average of `data`. */
  def average(data: Seq[Double]): Double = {
    val count = data.length.toDouble
    data.foldLeft(0.0) { _ + _ / count }
  }
}
