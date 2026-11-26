// Ported from https://github.com/KieranP/Game-Of-Life-Implementations/tree/master/scala
// MIT License, Copyright (c) 2011-2025 Kieran Pilkington
//
// Kept as close to the original as possible: the String-keyed HashMap,
// `s"$x-$y"` keys, `ArrayBuffer` neighbours and `Option` next state are the
// suspected Scala Native bottlenecks in scala-native/scala-native#4040.
// Changes: package, injected seeded Random (reproducible worlds), public makeKey.

package scala.scalanative.benchmarks.issues

import java.util.concurrent.TimeUnit

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.compiletime.uninitialized
import scala.util.Random

import microbench.*

/** Reproducer of scala-native/scala-native#4040: Game of Life from
 *  KieranP/Game-Of-Life-Implementations (reported in scala/scala3#21432), where
 *  Scala Native was ~2-3x slower than the JVM.
 *
 *  `tick` and `render` are the two phases the original `Play` loop times
 *  (150x40 world). The world keeps evolving across invocations, as in the
 *  original. `makeKey` and `build` isolate the suspects from the issue thread:
 *  `s"$x-$y"` interpolation (Int.toString) and String-keyed HashMap building.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class GameOfLife:
  @Param(Array("150")) var width: Int = 0
  @Param(Array("40")) var height: Int = 0

  private var world: World = uninitialized
  private var x = 0
  private var y = 0

  @Setup(Level.Trial) def prepare(): Unit =
    world = World(width, height, new Random(4040L))

  @Benchmark def tick(): Int =
    world.doTick
    world.tick

  @Benchmark def render(): String = world.render

  @Benchmark @OutputTimeUnit(TimeUnit.NANOSECONDS) def makeKey(): String =
    x += 1
    if x == width then
      x = 0
      y = if y + 1 == height then 0 else y + 1
    world.makeKey(x, y)

  /** Building the world: populate cells and precompute neighbours. */
  @Benchmark def build(blackhole: Blackhole): Unit =
    blackhole.consume(World(width, height, new Random(4040L)))

private class GolCell(
    val x: Int,
    val y: Int,
    var alive: Boolean = false
):
  var nextState: Option[Boolean] = None
  val neighbours = ArrayBuffer[GolCell]()

  def toChar =
    if alive then 'o' else ' '

  def aliveNeighbours =
    var aliveNeighbours = 0
    val count = neighbours.length
    for i <- 0 until count do
      val neighbour = neighbours(i)
      if neighbour.alive then aliveNeighbours += 1
    aliveNeighbours

private object World:
  private val Directions = Array(
    (-1, 1),
    (0, 1),
    (1, 1), // above
    (-1, 0),
    (1, 0), // sides
    (-1, -1),
    (0, -1),
    (1, -1) // below
  )

private class World(
    private val width: Int,
    private val height: Int,
    random: Random
):
  var tick = 0

  private val cells = HashMap[String, GolCell]()

  private class LocationOccupied(x: Int, y: Int)
      extends Exception(s"LocationOccupied($x-$y)")

  populateCells
  prepopulateNeighbours

  def doTick =
    val cellValues = cells.values

    // First determine the action for all cells
    for cell <- cellValues do
      val aliveNeighbours = cell.aliveNeighbours
      if !cell.alive && aliveNeighbours == 3 then cell.nextState = Some(true)
      else if aliveNeighbours < 2 || aliveNeighbours > 3 then
        cell.nextState = Some(false)
      else cell.nextState = Some(cell.alive)

    // Then execute the determined action for all cells
    for cell <- cellValues do cell.alive = cell.nextState.contains(true)

    tick += 1

  def render =
    val renderSize = width * height + height
    val rendering = StringBuilder(renderSize)
    for y <- 0 until height do
      for x <- 0 until width do
        val cell = cellAt(x, y)
        if cell.isDefined then rendering.append(cell.get.toChar)
      rendering.append('\n')
    rendering.toString

  def makeKey(x: Int, y: Int): String = s"$x-$y"

  private def cellAt(x: Int, y: Int) =
    val key = makeKey(x, y)
    cells.get(key)

  private def populateCells =
    for y <- 0 until height do
      for x <- 0 until width do
        val alive = random.nextDouble() <= 0.2
        addCell(x, y, alive)

  private def addCell(x: Int, y: Int, alive: Boolean = false) =
    val existing = cellAt(x, y)
    if existing.isDefined then throw LocationOccupied(x, y)

    val cell = GolCell(x, y, alive)
    val key = makeKey(x, y)
    cells.put(key, cell)
    true

  private def prepopulateNeighbours =
    for cell <- cells.values do
      val x = cell.x
      val y = cell.y

      cell.neighbours.appendAll(
        World.Directions.flatMap { (relX, relY) =>
          val nx = x + relX
          val ny = y + relY
          if nx < 0 || ny < 0 then None // Out of bounds
          else if nx >= width || ny >= height then None // Out of bounds
          else cellAt(nx, ny)
        }
      )
