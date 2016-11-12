/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js Benchmarks        **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013, Jonas Fonseca    **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \                               **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */

/* Based on code from: http://norvig.com/sudopy.shtml */

package sudoku

import scala.language.implicitConversions

class SudokuBenchmark
    extends benchmarks.Benchmark[Option[
      scala.collection.mutable.Map[String, String]]] {

  override def run(): Option[Grid] = {
    disableBenchmark()
    solve(grid1)
  }

  override def check(grid: Option[Grid]): Boolean =
    grid match {
      case Some(values) =>
        grid1Solutions.contains(asString(values))
      case None =>
        false
    }

  def cross(as: String, bs: String) =
    for (a <- as.map(_.toString); b <- bs.map(_.toString)) yield a + b

  val digits  = "123456789"
  val rows    = "ABCDEFGHI"
  val cols    = digits
  val squares = cross(rows, cols)

  val unitlist =
    cols.map(_.toString).map(cross(rows, _)) ++
      rows.map(_.toString).map(cross(_, cols)) ++
      (for (rs <- List("ABC", "DEF", "GHI"); cs <- List("123", "456", "789"))
        yield cross(rs, cs))

  val units = squares.map(s => (s, unitlist.filter(_.contains(s)))).toMap
  val peers =
    squares.map(s => (s, units(s).flatten.toSet.filterNot(_ == s))).toMap

  type Grid = scala.collection.mutable.Map[String, String]
  val False                                       = scala.collection.mutable.Map[String, String]()
  implicit def gridToBoolean(grid: Grid): Boolean = grid.nonEmpty

  // ################ Parse a Grid ################

  def parseGrid(grid: String): Grid = {
    val values = scala.collection.mutable.Map[String, String]()
    values ++= squares.map(s => (s, digits)).toMap

    val iter = gridValues(grid).iterator
    while (iter.hasNext) {
      val (s, d) = iter.next
      if (digits.contains(d) && !assign(values, s, d))
        return False
    }

    values
  }

  def gridValues(grid: String) = {
    val chars =
      grid.map(_.toString).filter(c => digits.contains(c) || "0.".contains(c))
    squares.zip(chars).toMap
  }

  // ################ Constraint Propagation ################

  /* Eliminate all the other values (except d) from values[s] and propagate.
   * Return values, except return False if a contradiction is detected. */
  def assign(values: Grid, s: String, d: String): Grid = {
    val otherValues = values(s).replace(d, "")

    if (otherValues.forall(d2 => eliminate(values, s, d2.toString)))
      values
    else
      False
  }

  /* Eliminate d from values[s]; propagate when values or places <= 2.
   * Return values, except return False if a contradiction is detected. */
  def eliminate(values: Grid, s: String, d: String): Grid = {
    if (!values(s).contains(d))
      return values // Already eliminated

    values(s) = values(s).replace(d, "")

    // (1) If a square s is reduced to one value d2, then eliminate d2 from the peers.
    if (values(s).isEmpty) {
      return False // Contradiction: removed last value
    } else if (values(s).length == 1) {
      val d2 = values(s)
      if (!peers(s).forall(s2 => eliminate(values, s2, d2)))
        return False
    }

    // (2) If a unit u is reduced to only one place for a value d, then put it there.
    val iter = units(s).iterator
    while (iter.hasNext) {
      val u       = iter.next
      val dplaces = for (s <- u; if (values(s).contains(d))) yield s
      if (dplaces.isEmpty)
        return False // Contradiction: no place for d
      if (dplaces.size == 1) {
        if (!assign(values, dplaces(0), d))
          return False
      }
    }

    values
  }

  // ################ Unit Tests ################

  val grid1 =
    "003020600900305001001806400008102900700000008006708200002609500800203009005010300"
  val grid2 =
    "4.....8.5.3..........7......2.....6.....8.4......1.......6.3.7.5..2.....1.4......"
  val hard1 =
    ".....6....59.....82....8....45........3........6..3.54...325..6.................."
  val grid1Solutions = List(
    "483921657967345821251876493548132976729564138136798245372689514814253769695417382")
  val grid2Solutions = List(
    "417369825632158947958724316825437169791586432346912758289643571573291684164875293")
  val hard1Solutions = List(
    "874196325359742618261538497145679832783254169926813754417325986598461273632987541",
    "834596217659712438271438569745169382923854671186273954417325896562987143398641725")

  def test() {
    require(squares.length == 81)
    require(unitlist.length == 27)
    require(squares.forall(s => units(s).size == 3))
    require(squares.forall(s => peers(s).size == 20))
    require(
      units("C2") == Vector(
        Vector("A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2", "I2"),
        Vector("C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9"),
        Vector("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3")))
    require(
      peers("C2") == Set("A2",
                         "B2",
                         "D2",
                         "E2",
                         "F2",
                         "G2",
                         "H2",
                         "I2",
                         "C1",
                         "C3",
                         "C4",
                         "C5",
                         "C6",
                         "C7",
                         "C8",
                         "C9",
                         "A1",
                         "A3",
                         "B1",
                         "B3"))
    println("All tests pass")
  }

  // ################ Display as 2-D grid ################

  // Display these values as a 2-D grid.
  def display(values: Grid) = {
    val width = squares.map(values(_).length).max + 1
    val line  = (for (i <- 0 to 2) yield ("-" * width * 3)).mkString("+")
    for (r <- rows.map(_.toString)) {
      val cells = (for (c <- cols) yield center(values(r + c), width))
      println(cells.sliding(3, 3).map(_.mkString).mkString("|"))
      if ("CF".contains(r))
        println(line)
    }
    println
  }

  def asString(values: Grid): String =
    (for (r <- rows; c <- cols) yield values(r.toString + c.toString)).mkString

  // ################ Search ################

  def solve(grid: String) = search(parseGrid(grid))

  // Using depth-first search and propagation, try all possible values.
  def search(values: Grid): Option[Grid] = {
    if (values.isEmpty)
      return None // Failed earlier

    if (squares.forall(s => values(s).length == 1))
      return Some(values) // Solved!

    // Chose the unfilled square s with the fewest possibilities
    val (s, n) = values.filter(_._2.length > 1).minBy(_._2.length)

    values(s).toStream.map { d =>
      val solution = values.clone
      if (assign(solution, s, d.toString))
        search(solution)
      else
        None
    }.find(_.isDefined).flatten
  }

  // ################ Utilities ################

  def center(s: String, max: Int, pad: String = " ") = {
    def repeat(s: String, n: Int) =
      s * n

    val padLen = max - s.length
    if (padLen <= 0)
      s
    else
      repeat(pad, padLen / 2) + s + repeat(pad, (padLen + 1) / 2)
  }

}
