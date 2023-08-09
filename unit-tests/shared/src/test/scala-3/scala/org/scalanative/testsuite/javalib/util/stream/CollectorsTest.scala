package org.scalanative.testsuite.javalib.util.stream

import java.{lang => jl}
import java.{util => ju}
import java.util._

import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap

import java.util.function.Function
import java.util.function.BinaryOperator

import java.util.stream._
import java.util.stream.Collector.Characteristics

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* This Test suite depends upon a competent Stream implementation.
 * This file focuses on exercising the Collectors.
 * Similar, overlapping, or identical Tests in StreamTest focus on exercising
 * Streams.
 */

/* Design Notes:
 *
 *   1) This file is restricted to running on Scala 3.
 *
 *      It is, by explicit purpose, written to call Collectors
 *      as they are most likely to be used in the field: using brief,
 *      intentional, lambdas and few/no unnecessary type arguments.
 *
 *      As such, they provide reasonable, not perfect, models for how
 *      Collectors may be used with relative ease.
 *
 *      A person with too much time on their hands could write alternate
 *      code for Scala 2.13.*. Such has been done during development.
 *      It can be made to work but changes for the more interesting and
 *      complex uses of Collectors are just too ugly to publish as a use model.
 *
 *      A person with entirely too much time on their hands could try to
 *      write alternate code for Scala 2.12.*.
 *      The changes required for Scala 2.12 are extensive and unlikely to
 *      repay the cost of making them.
 *
 *   2) Someday, after the correctness of both the underlying implementation
 *      and the Tests themselves has been shown, replication of various
 *      data structures and code paths may be collapsed to common code.
 *
 *      Rough edges, partial list
 *        - Testing for the presence or absence of Characteristics is a good
 *          candidate for re-work.
 *
 *        - The various variants & initializations of Employees classes
 *          should be checked for commonalities and possible consolidation.
 */

class CollectorsTest {

  final val epsilon = 0.00001 // tolerance for Floating point comparisons.

  case class Student(name: String, grade: Int, salary: Double)

  private def createStdStudentList(): ArrayList[Student] = {
    val nElements = 8
    val students = new ArrayList[Student](nElements)
    students.add(Student("Student_1", 99, 87.03))
    students.add(Student("Student_2", 0, 16.18))
    students.add(Student("Student_3", 96, 91.94))
    students.add(Student("Student_4", 80, 35.12))
    students.add(Student("Student_5", 81, 7.75))
    students.add(Student("Student_6", 88, 63.69))
    students.add(Student("Student_7", 90, 79.19))
    students.add(Student("Student_8", 70, 49.15))

    students
  }

  case class UpcItem(name: String, upc: Int)
  case class ValueItem(doubleValue: Double, longValue: Long, intValue: Int)

  private def requireEmptyCharacteristics(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals(
      s"unexpected extra characteristics: ${differentia}",
      0,
      differentia.size()
    )
  }

  private def requireIdentityCharacteristicOnly(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals("characteristics set size", 1, differentia.size())

    assertTrue(
      "Characteristics.IDENTITY_FINISH is missing",
      differentia.contains(Characteristics.IDENTITY_FINISH)
    )
  }

  private def requireUnorderedCharacteristicOnly(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals("characteristics set size", 1, differentia.size())

    assertTrue(
      "Characteristics.UNORDERED is missing",
      differentia.contains(Characteristics.UNORDERED)
    )
  }

  private def requireConcurrentUnorderedCharacteristicsOnly(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals("characteristics set size", 2, differentia.size())

    assertTrue(
      "Characteristics.CONCURRENT is missing",
      differentia.contains(Characteristics.CONCURRENT)
    )

    assertTrue(
      "Characteristics.UNORDERED is missing",
      differentia.contains(Characteristics.UNORDERED)
    )
  }

  private def requireIdentityUnorderedCharacteristicOnly(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals("characteristics set size", 2, differentia.size())

    assertTrue(
      "Characteristics.IDENTITY_FINISH is missing",
      differentia.contains(Characteristics.IDENTITY_FINISH)
    )

    assertTrue(
      "Characteristics.UNORDERED is missing",
      differentia.contains(Characteristics.UNORDERED)
    )
  }

  private def requireAll3Characteristics(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertTrue(
      "Characteristics.CONCURRENT is missing",
      differentia.contains(Characteristics.CONCURRENT)
    )

    assertTrue(
      "Characteristics.UNORDERED is missing",
      differentia.contains(Characteristics.UNORDERED)
    )

    assertTrue(
      "Characteristics.IDENTITY_FINISH is missing",
      differentia.contains(Characteristics.IDENTITY_FINISH)
    )
  }

  @Test def collectorsAveragingDouble(): Unit = {

    val expectedAverage = 3.30

    val nElements = 7
    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 3L, 3))
    items.add(ValueItem(2.2, 2L, 2))
    items.add(ValueItem(1.1, 1L, 1))
    items.add(ValueItem(4.4, 4L, 4))
    items.add(ValueItem(0.0, 0L, 0))
    items.add(ValueItem(6.6, 6L, 6))
    items.add(ValueItem(5.5, 5L, 5))

    val s = items.stream()

    val collector =
      Collectors.averagingDouble((e: ValueItem) => e.doubleValue)

    requireEmptyCharacteristics(collector.characteristics())

    val average = s.collect(collector)

    assertEquals("average", expectedAverage, average, epsilon)
  }

  @Test def collectorsAveragingInt(): Unit = {

    val expectedAverage = 46.0

    val nElements = 7
    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 3L, 29))
    items.add(ValueItem(2.2, 2L, 66))
    items.add(ValueItem(1.1, 1L, 54))
    items.add(ValueItem(4.4, 4L, 15))
    items.add(ValueItem(0.0, 0L, 63))
    items.add(ValueItem(6.6, 6L, 82))
    items.add(ValueItem(5.5, 5L, 13))

    val s = items.stream()

    val collector =
      Collectors.averagingInt((e: ValueItem) => e.intValue)

    requireEmptyCharacteristics(collector.characteristics())

    val average = s.collect(collector)

    assertEquals("average", expectedAverage, average, epsilon)
  }

  @Test def collectorsAveragingLong(): Unit = {

    val expectedAverage = 50.4285714

    val nElements = 7
    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 36L, 29))
    items.add(ValueItem(2.2, 32L, 66))
    items.add(ValueItem(1.1, 100L, 54))
    items.add(ValueItem(4.4, 84L, 15))
    items.add(ValueItem(0.0, 22L, 63))
    items.add(ValueItem(6.6, 45L, 82))
    items.add(ValueItem(5.5, 34L, 13))

    val s = items.stream()

    val collector =
      Collectors.averagingLong((e: ValueItem) => e.longValue)

    requireEmptyCharacteristics(collector.characteristics())

    val average = s.collect(collector)

    assertEquals("average", expectedAverage, average, epsilon)
  }

  @Test def collectorsCollectingAndThen(): Unit = {
    val nElements = 20
    val nEvenElements = nElements / 2

    // K. F. Gauss formula for sum of even integers within a range.
    val sum = ((2 + 20) / 2) * nEvenElements
    val expectedSumSquared = sum * sum

    val s = Stream
      .iterate[Int](1, e => e + 1)
      .limit(nElements)

    val collector =
      Collectors.collectingAndThen(
        Collectors.toList(),
        (e: ju.List[Int]) => Collections.unmodifiableList(e)
      )

    requireEmptyCharacteristics(collector.characteristics())

    val adamantine = s.collect(collector)

    assertEquals("list size", nElements, adamantine.size())

    // Unmodifiable
    assertThrows(classOf[UnsupportedOperationException], adamantine.remove(0))
  }

  @Test def collectorsCounting(): Unit = {
    val nElements = 29

    val s = Stream
      .iterate[Int](1775, e => e + 1)
      .limit(nElements)

    val collector = Collectors.counting[Int]()

    requireEmptyCharacteristics(collector.characteristics())

    val count = s.collect(collector)

    assertEquals("unexpected count", nElements.toLong, count)
  }

  @Test def collectorsGroupingBy_1Arg(): Unit = {
    /* This implements one of the examples in the Java 19 description of the
     * java.util.Collectors class:
     *   // Group employees by department
     */

    case class Employee(name: String, department: String)

    val nElements = 16
    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Employee_1", "OGO"))
    employees.add(Employee("Employee_2", "TAY"))
    employees.add(Employee("Employee_3", "LKG"))
    employees.add(Employee("Employee_4", "ZKO"))
    employees.add(Employee("Employee_5", "OGO"))
    employees.add(Employee("Employee_6", "LKG"))
    employees.add(Employee("Employee_7", "LKG"))
    employees.add(Employee("Employee_8", "ZKO"))
    employees.add(Employee("Employee_9", "ZKO"))
    employees.add(Employee("Employee_10", "TAY"))
    employees.add(Employee("Employee_11", "LKG"))
    employees.add(Employee("Employee_12", "ZKO"))
    employees.add(Employee("Employee_13", "OGO"))
    employees.add(Employee("Employee_14", "ZKO"))
    employees.add(Employee("Employee_15", "LKG"))
    employees.add(Employee("Employee_16", "ZKO"))

    val s = employees.stream()

    val collector =
      Collectors.groupingBy((e: Employee) => e.department)

    requireIdentityCharacteristicOnly(collector.characteristics())

    val grouped = s.collect(collector)

    assertEquals("grouped size", 4, grouped.size())

    val ogoEmployees = grouped.get("OGO")
    assertEquals("grouped ogo size", 3, ogoEmployees.size())

    val tayEmployees = grouped.get("TAY")
    assertEquals("grouped tay size", 2, tayEmployees.size())

    val lkgEmployees = grouped.get("LKG")
    assertEquals("grouped lkg size", 5, lkgEmployees.size())

    val zkoEmployees = grouped.get("ZKO")
    assertEquals("grouped zko size", 6, zkoEmployees.size())

    employees.forEach(e =>
      e.department match {
        case "OGO" =>
          assertTrue(
            s"missing OGO employee: ${e.name}",
            grouped.get("OGO").contains(e)
          )

        case "TAY" =>
          assertTrue(
            s"missing TAY employee: ${e.name}",
            grouped.get("TAY").contains(e)
          )

        case "LKG" =>
          assertTrue(
            s"missing LKG employee: ${e.name}",
            grouped.get("LKG").contains(e)
          )

        case "ZKO" =>
          assertTrue(
            s"missing ZKO employee: ${e.name}",
            grouped.get("ZKO").contains(e)
          )
      }
    )
  }

  @Test def collectorsGroupingBy_2Arg(): Unit = {
    /* This implements one of the examples in the Java 19 description of the
     * java.util.Collectors class:
     *   // Compute sum of salaries by department
     */

    case class Employee(name: String, department: String, salary: Int)

    val nElements = 16
    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Employee_1", "OGO", 1606))
    employees.add(Employee("Employee_2", "TAY", 1505))
    employees.add(Employee("Employee_3", "LKG", 1404))
    employees.add(Employee("Employee_4", "ZKO", 1303))
    employees.add(Employee("Employee_5", "OGO", 1202))
    employees.add(Employee("Employee_6", "LKG", 1101))
    employees.add(Employee("Employee_7", "LKG", 1000))
    employees.add(Employee("Employee_8", "ZKO", 909))
    employees.add(Employee("Employee_9", "ZKO", 808))
    employees.add(Employee("Employee_10", "TAY", 707))
    employees.add(Employee("Employee_11", "LKG", 606))
    employees.add(Employee("Employee_12", "ZKO", 505))
    employees.add(Employee("Employee_13", "OGO", 404))
    employees.add(Employee("Employee_14", "ZKO", 303))
    employees.add(Employee("Employee_15", "LKG", 202))
    employees.add(Employee("Employee_16", "ZKO", 101))

    val s = employees.stream()

    val collector =
      Collectors.groupingBy(
        (e: Employee) => e.department,
        Collectors.summingInt((e: Employee) => e.salary)
      )

    requireEmptyCharacteristics(collector.characteristics())

    val grouped = s.collect(collector)

    assertEquals("grouped size", 4, grouped.size())

    val ogoEmployees = grouped.get("OGO")
    assertEquals("ogo salary", 3212, ogoEmployees)

    val tayEmployees = grouped.get("TAY")
    assertEquals("tay salary", 2212, tayEmployees)

    val lkgEmployees = grouped.get("LKG")
    assertEquals("lkg salary", 4313, lkgEmployees)

    val zkoEmployees = grouped.get("ZKO")
    assertEquals("zko salary", 3929, zkoEmployees)
  }

  @Test def collectorsGroupingBy_3Arg(): Unit = {
    /* This implements one of the examples in the Java 19 description of the
     * java.util.Collectors class (using groupingBy with 3 arguments):
     *   // Compute sum of salaries by department
     */

    case class Employee(name: String, department: String, salary: Int)

    val nElements = 16
    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Employee_1", "OGO", 1606))
    employees.add(Employee("Employee_2", "TAY", 1505))
    employees.add(Employee("Employee_3", "LKG", 1404))
    employees.add(Employee("Employee_4", "ZKO", 1303))
    employees.add(Employee("Employee_5", "OGO", 1202))
    employees.add(Employee("Employee_6", "LKG", 1101))
    employees.add(Employee("Employee_7", "LKG", 1000))
    employees.add(Employee("Employee_8", "ZKO", 909))
    employees.add(Employee("Employee_9", "ZKO", 808))
    employees.add(Employee("Employee_10", "TAY", 707))
    employees.add(Employee("Employee_11", "LKG", 606))
    employees.add(Employee("Employee_12", "ZKO", 505))
    employees.add(Employee("Employee_13", "OGO", 404))
    employees.add(Employee("Employee_14", "ZKO", 303))
    employees.add(Employee("Employee_15", "LKG", 202))
    employees.add(Employee("Employee_16", "ZKO", 101))

    val s = employees.stream()

    // Note Well:
    // Collectors.summingInt() returns an Integer, not a primitive Int.

    val collector =
      Collectors.groupingBy(
        (e: Employee) => e.department,
        () => new TreeMap[String, Integer],
        Collectors.summingInt((e: Employee) => e.salary)
      )

    requireEmptyCharacteristics(collector.characteristics())

    val grouped = s.collect(collector)

    assertEquals("grouped size", 4, grouped.size())

    val ogoEmployees = grouped.get("OGO")
    assertEquals("ogo salary", 3212, ogoEmployees)

    val tayEmployees = grouped.get("TAY")
    assertEquals("tay salary", 2212, tayEmployees)

    val lkgEmployees = grouped.get("LKG")
    assertEquals("lkg salary", 4313, lkgEmployees)

    val zkoEmployees = grouped.get("ZKO")
    assertEquals("zko salary", 3929, zkoEmployees)
  }

  @Test def collectorsGroupingByConcurrent_1Arg(): Unit = {
    case class Employee(name: String, department: String)

    val nElements = 16
    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Employee_1", "OGO"))
    employees.add(Employee("Employee_2", "TAY"))
    employees.add(Employee("Employee_3", "LKG"))
    employees.add(Employee("Employee_4", "ZKO"))
    employees.add(Employee("Employee_5", "OGO"))
    employees.add(Employee("Employee_6", "LKG"))
    employees.add(Employee("Employee_7", "LKG"))
    employees.add(Employee("Employee_8", "ZKO"))
    employees.add(Employee("Employee_9", "ZKO"))
    employees.add(Employee("Employee_10", "TAY"))
    employees.add(Employee("Employee_11", "LKG"))
    employees.add(Employee("Employee_12", "ZKO"))
    employees.add(Employee("Employee_13", "OGO"))
    employees.add(Employee("Employee_14", "ZKO"))
    employees.add(Employee("Employee_15", "LKG"))
    employees.add(Employee("Employee_16", "ZKO"))

    val s = employees.stream()

    val collector =
      Collectors.groupingByConcurrent((e: Employee) => e.department)

    requireAll3Characteristics(collector.characteristics())

    val grouped = s.collect(collector)

    assertEquals("grouped size", 4, grouped.size())

    val ogoEmployees = grouped.get("OGO")
    assertEquals("grouped ogo size", 3, ogoEmployees.size())

    val tayEmployees = grouped.get("TAY")
    assertEquals("grouped tay size", 2, tayEmployees.size())

    val lkgEmployees = grouped.get("LKG")
    assertEquals("grouped lkg size", 5, lkgEmployees.size())

    val zkoEmployees = grouped.get("ZKO")
    assertEquals("grouped zko size", 6, zkoEmployees.size())

    employees.forEach(e =>
      e.department match {
        case "OGO" =>
          assertTrue(
            s"missing OGO employee: ${e.name}",
            grouped.get("OGO").contains(e)
          )

        case "TAY" =>
          assertTrue(
            s"missing TAY employee: ${e.name}",
            grouped.get("TAY").contains(e)
          )

        case "LKG" =>
          assertTrue(
            s"missing LKG employee: ${e.name}",
            grouped.get("LKG").contains(e)
          )

        case "ZKO" =>
          assertTrue(
            s"missing ZKO employee: ${e.name}",
            grouped.get("ZKO").contains(e)
          )
      }
    )
  }

  @Test def collectorsGroupingByConcurrent_2Arg(): Unit = {
    case class Employee(name: String, department: String, salary: Int)

    val nElements = 16
    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Employee_1", "OGO", 1606))
    employees.add(Employee("Employee_2", "TAY", 1505))
    employees.add(Employee("Employee_3", "LKG", 1404))
    employees.add(Employee("Employee_4", "ZKO", 1303))
    employees.add(Employee("Employee_5", "OGO", 1202))
    employees.add(Employee("Employee_6", "LKG", 1101))
    employees.add(Employee("Employee_7", "LKG", 1000))
    employees.add(Employee("Employee_8", "ZKO", 909))
    employees.add(Employee("Employee_9", "ZKO", 808))
    employees.add(Employee("Employee_10", "TAY", 707))
    employees.add(Employee("Employee_11", "LKG", 606))
    employees.add(Employee("Employee_12", "ZKO", 505))
    employees.add(Employee("Employee_13", "OGO", 404))
    employees.add(Employee("Employee_14", "ZKO", 303))
    employees.add(Employee("Employee_15", "LKG", 202))
    employees.add(Employee("Employee_16", "ZKO", 101))

    val s = employees.stream()

    val collector =
      Collectors.groupingByConcurrent(
        (e: Employee) => e.department,
        Collectors.summingInt((e: Employee) => e.salary)
      )

    requireConcurrentUnorderedCharacteristicsOnly(collector.characteristics())

    val grouped = s.collect(collector)

    assertEquals("grouped size", 4, grouped.size())

    val ogoEmployees = grouped.get("OGO")
    assertEquals("ogo salary", 3212, ogoEmployees)

    val tayEmployees = grouped.get("TAY")
    assertEquals("tay salary", 2212, tayEmployees)

    val lkgEmployees = grouped.get("LKG")
    assertEquals("lkg salary", 4313, lkgEmployees)

    val zkoEmployees = grouped.get("ZKO")
    assertEquals("zko salary", 3929, zkoEmployees)
  }

  @Test def collectorsGroupingByConcurrent_3Arg(): Unit = {
    case class Employee(name: String, department: String, salary: Int)

    val nElements = 16
    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Employee_1", "OGO", 1606))
    employees.add(Employee("Employee_2", "TAY", 1505))
    employees.add(Employee("Employee_3", "LKG", 1404))
    employees.add(Employee("Employee_4", "ZKO", 1303))
    employees.add(Employee("Employee_5", "OGO", 1202))
    employees.add(Employee("Employee_6", "LKG", 1101))
    employees.add(Employee("Employee_7", "LKG", 1000))
    employees.add(Employee("Employee_8", "ZKO", 909))
    employees.add(Employee("Employee_9", "ZKO", 808))
    employees.add(Employee("Employee_10", "TAY", 707))
    employees.add(Employee("Employee_11", "LKG", 606))
    employees.add(Employee("Employee_12", "ZKO", 505))
    employees.add(Employee("Employee_13", "OGO", 404))
    employees.add(Employee("Employee_14", "ZKO", 303))
    employees.add(Employee("Employee_15", "LKG", 202))
    employees.add(Employee("Employee_16", "ZKO", 101))

    val s = employees.stream()

    // Note Well:
    // Collectors.summingInt() returns an Integer, not a primitive Int.

    val collector =
      Collectors.groupingByConcurrent(
        (e: Employee) => e.department,
        () => new ConcurrentHashMap[String, Integer],
        Collectors.summingInt((e: Employee) => e.salary)
      )

    requireConcurrentUnorderedCharacteristicsOnly(collector.characteristics())

    val grouped = s.collect(collector)

    assertEquals("grouped size", 4, grouped.size())

    val ogoEmployees = grouped.get("OGO")
    assertEquals("ogo salary", 3212, ogoEmployees)

    val tayEmployees = grouped.get("TAY")
    assertEquals("tay salary", 2212, tayEmployees)

    val lkgEmployees = grouped.get("LKG")
    assertEquals("lkg salary", 4313, lkgEmployees)

    val zkoEmployees = grouped.get("ZKO")
    assertEquals("zko salary", 3929, zkoEmployees)
  }

  // Empty stream case handled in collectorsJoining_3Arg_EmptyStream Test

  @Test def collectorsJoining(): Unit = {
    val expected = "Thequickbrownfox"

    val s = Stream.of("The", "quick", "brown", "fox")

    val collector = Collectors.joining()

    requireEmptyCharacteristics(collector.characteristics())

    val joined = s.collect(collector)

    assertEquals("unexpected joined", expected, joined)
  }

  // Empty stream case handled in collectorsJoining_3Arg_EmptyStream Test

  @Test def collectorsJoining_1Arg(): Unit = {
    val expected = "The/quick/brown/fox"

    val s = Stream.of("The", "quick", "brown", "fox")

    val collector = Collectors.joining("/")

    requireEmptyCharacteristics(collector.characteristics())

    val joined = s.collect(collector)

    assertEquals("unexpected joined", expected, joined)
  }

  @Test def collectorsJoining_3Arg_EmptyStream(): Unit = {
    val prefix = "prefix~"
    val suffix = "~suffix"

    val expected = s"${prefix}${suffix}"

    val s = Stream.empty[String]

    val collector = Collectors.joining(" ", prefix, suffix)

    requireEmptyCharacteristics(collector.characteristics())

    val joined = s.collect(collector)

    assertEquals("unexpected joined", expected, joined)
  }

  @Test def collectorsJoining_3Arg(): Unit = {
    val prefix = "Dies irae, dies illa, "
    val body = "Solvetsaeclum in favilla:"
    val suffix = " Teste David cum Sibylla."

    val expected = s"${prefix}${body}${suffix}"

    val s = Stream.of("Solvetsaeclum", "in", "favilla:")

    val collector = Collectors.joining(" ", prefix, suffix)

    requireEmptyCharacteristics(collector.characteristics())

    val joined = s.collect(collector)

    assertEquals("unexpected joined", expected, joined)
  }

  // Issue #3409
  @Test def collectorsJoining_Merge(): Unit = {
    /* The idea is to test that a delimiter is added between the
     * two arguments when Collectors.joining() merge() method is called.
     *
     * One would not normally call merge() directory, but writers of
     * parallel library methods might. So the method should match its
     * JVM description.
     *
     * The complexity comes from not wanting to know the actual implementation
     * type of the accumulator A in <T, ?, R>. combiner() takes two arguments
     * of that exact type. To get the unknown type right, each of the
     * arguments passed to combiner() should come from the same supplier of
     * the same Collector.joining().
     *
     * So far, this type fun & games is true with both JVM and Scala Native.
     *
     * This gets the interior implementation type correct, but also means
     * that both arguments use the same prefix, suffix, & delimiter.
     * Experience with parallel Collectors may show a way around this
     * restriction/feature.
     */

    val left = "Left"
    val right = "Right"
    val delim = "|"

    val expected = s"${left}${delim}${right}"

    val collector = Collectors.joining(delim)

    val supplier = collector.supplier
    val accumulator = collector.accumulator
    val combiner = collector.combiner

    val accLeft = supplier.get()
    accumulator.accept(accLeft, left)

    val accRight = supplier.get()
    accumulator.accept(accRight, right)

    val combined = combiner.apply(accLeft, accRight).toString()

    assertEquals("unexpected combined", expected, combined)
  }

  @Test def collectorsMapping(): Unit = {
    /* This implements one of the examples in the Java 19 description of the
     * java.util.Collectors class:
     *   // Accumulate names into a List
     */
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val expectedSum = 45

    val s = sisters.stream()

    // A demo transformation just for the fun of it.
    val collector = Collectors.mapping(
      (e: String) => e.length(),
      Collectors.summingInt((e: Int) => e)
    )

    requireEmptyCharacteristics(collector.characteristics())

    val sum = s.collect(collector)

    assertEquals("sum", expectedSum, sum)
  }

  @Test def collectorsMapping_PreservesCharacteristics(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val collector1 =
      Collectors.toConcurrentMap(
        (e: Employee) => e.name,
        (e: Employee) => e.badgeNumber
      )

    requireAll3Characteristics(collector1.characteristics())

    // Pick a downstream that is now known to have characteristics.
    val collector2 =
      Collectors.mapping(
        (e: Map.Entry[String, Int]) => Employee(e.getKey(), e.getValue()),
        collector1
      )

    // Are the downstreamCharacteristics inherited correctly? JVM does that.
    requireAll3Characteristics(collector2.characteristics())
  }

  @Test def collectorsMaxBy(): Unit = {
    val itemComparator = new ju.Comparator[UpcItem] {
      def compare(item1: UpcItem, item2: UpcItem): Int =
        item1.upc - item2.upc
    }

    val nElements = 7
    val items = new ArrayList[UpcItem](nElements)
    items.add(UpcItem("Maya", 1))
    items.add(UpcItem("Electra", 2))
    items.add(UpcItem("Taygete", 3))
    items.add(UpcItem("Alcyone", 4))
    items.add(UpcItem("Celaeno", 5))
    items.add(UpcItem("Sterope", 6))
    items.add(UpcItem("Merope", 7))

    val s = items.stream()

    val collector = Collectors.maxBy(itemComparator)

    requireEmptyCharacteristics(collector.characteristics())

    val maxOpt: Optional[UpcItem] = s.collect(collector)

    assertTrue("max not found", maxOpt.isPresent)

    assertEquals(
      "wrong max item found",
      items.get(nElements - 1).name,
      maxOpt.get().name
    )
  }

  @Test def collectorsMinBy(): Unit = {
    val itemComparator = new ju.Comparator[UpcItem] {
      def compare(item1: UpcItem, item2: UpcItem): Int =
        item1.name.compareTo(item2.name)
    }

    val nElements = 7
    val items = new ArrayList[UpcItem](nElements)
    items.add(UpcItem("Maya", 1))
    items.add(UpcItem("Electra", 2))
    items.add(UpcItem("Taygete", 3))
    items.add(UpcItem("Alcyone", 4))
    items.add(UpcItem("Celaeno", 5))
    items.add(UpcItem("Sterope", 6))
    items.add(UpcItem("Merope", 7))

    val expectedMinName = items.get(3).name

    val s = items.stream()

    val collector = Collectors.minBy(itemComparator)

    requireEmptyCharacteristics(collector.characteristics())

    val minOpt: Optional[UpcItem] = s.collect(collector)

    assertTrue("min not found", minOpt.isPresent)

    assertEquals(
      "wrong min item found",
      expectedMinName,
      minOpt.get().name
    )
  }

  @Test def collectorsPartitioningBy_1Arg(): Unit = {
    /* This implements one of the examples in the Java 19 description of the
     * java.util.Collectors class:
     *   // Partition students into passing and failing
     */

    val expectedPassingCount = 6
    val expectedFailingCount = 2

    val passThreshold = 80

    val students = createStdStudentList()
    val s = students.stream()

    val collector =
      Collectors.partitioningBy((s: Student) => s.grade >= passThreshold)

    requireIdentityCharacteristicOnly(collector.characteristics())

    val partitions = s.collect(collector)

    assertEquals("partitions size", 2, partitions.size())

    val passingStudents = partitions.get(true)
    assertEquals(
      "partition passing size",
      expectedPassingCount,
      passingStudents.size()
    )

    val failingStudents = partitions.get(false)
    assertEquals(
      "partition failing size",
      expectedFailingCount,
      failingStudents.size()
    )

    students.forEach(s => {
      if (s.grade >= passThreshold)
        assertTrue(
          s"missing passing student: ${s.name}",
          passingStudents.contains(s)
        )
      else {
        assertTrue(
          s"missing failing student: ${s.name}",
          failingStudents.contains(s)
        )

      }
    })
  }

  @Test def collectorsPartitioningBy_2Arg(): Unit = {
    /* This merges two of the examples in the Java 19 description of the
     * java.util.Collectors class:
     *   // Partition students into passing and failing
     *   // Compute sum of salaries by department
     * The "Compute sum" example uses Int for salary. This Test uses Double.
     */

    val expectedPassingSalary = 364.72
    val expectedFailingSalary = 65.33

    val passThreshold = 80

    val students = createStdStudentList()
    val s = students.stream()

    val collector =
      Collectors.partitioningBy(
        (s: Student) => s.grade >= passThreshold,
        Collectors.summingDouble((s: Student) => s.salary)
      )

    requireEmptyCharacteristics(collector.characteristics())

    val partitions = s.collect(collector)

    assertEquals("partitions size", 2, partitions.size())

    assertEquals(
      "partition passing",
      expectedPassingSalary,
      partitions.get(true),
      epsilon
    )

    assertEquals(
      "partition failing",
      expectedFailingSalary,
      partitions.get(false),
      epsilon
    )

  }

  @Test def collectorsReducing_1Arg(): Unit = {
    val expectedSum = 210
    val nElements = 20
    val s = Stream
      .iterate[Int](1, e => e + 1)
      .limit(nElements)

    val collector = Collectors.reducing((e1: Int, e2: Int) => e1 + e2)

    requireEmptyCharacteristics(collector.characteristics())

    val reducedOpt = s.collect(collector)

    assertTrue("unexpected empty optional", reducedOpt.isPresent())
    assertEquals("reduced sum", expectedSum, reducedOpt.get())
  }

  @Test def collectorsReducing_2Arg(): Unit = {

    val identity = 0

    val s = Stream.empty[Int]()

    val collector =
      Collectors.reducing(
        identity,
        (e1: Int, e2: Int) => -1
      )

    requireEmptyCharacteristics(collector.characteristics())

    val reduced = s.collect(collector)

    assertEquals("reduced sum", identity, reduced)
  }

  @Test def collectorsReducing_3Arg(): Unit = {
    val identity = 0
    val expectedSum = 420

    val nElements = 20
    val s = Stream
      .iterate[Int](1, e => e + 1)
      .limit(nElements)

    val collector =
      Collectors.reducing(
        identity,
        (e: Int) => e * 2,
        (e1: Int, e2: Int) => e1 + e2
      )

    requireEmptyCharacteristics(collector.characteristics())

    val reduced = s.collect(collector)

    assertNotEquals("unexpected identity value", identity, reduced)

    assertEquals("reduced sum", expectedSum, reduced)
  }

  @Test def collectorsSummarizingDouble(): Unit = {

    val nElements = 7
    val expectedSum = 23.1
    val expectedMin = 0.0
    val expectedAverage = expectedSum / nElements
    val expectedMax = 6.6

    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 3L, 3))
    items.add(ValueItem(2.2, 2L, 2))
    items.add(ValueItem(1.1, 1L, 1))
    items.add(ValueItem(4.4, 4L, 4))
    items.add(ValueItem(0.0, 0L, 0))
    items.add(ValueItem(6.6, 6L, 6))
    items.add(ValueItem(5.5, 5L, 5))

    val s = items.stream()

    val collector =
      Collectors.summarizingDouble((e: ValueItem) => e.doubleValue)

    requireIdentityCharacteristicOnly(collector.characteristics())

    val summary = s.collect(collector)

    // Proper stats
    assertEquals("count", nElements, summary.getCount())
    assertEquals("sum", expectedSum, summary.getSum(), epsilon)
    assertEquals("min", expectedMin, summary.getMin(), epsilon)
    assertEquals("average", expectedAverage, summary.getAverage(), epsilon)
    assertEquals("max", expectedMax, summary.getMax(), epsilon)
  }

  @Test def collectorsSummarizingInt(): Unit = {

    val nElements = 7
    val expectedSum = 322
    val expectedMin = 13
    val expectedAverage = expectedSum / (nElements * 1.0)
    val expectedMax = 82

    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 3L, 29))
    items.add(ValueItem(2.2, 2L, 66))
    items.add(ValueItem(1.1, 1L, 54))
    items.add(ValueItem(4.4, 4L, 15))
    items.add(ValueItem(0.0, 0L, 63))
    items.add(ValueItem(6.6, 6L, 82))
    items.add(ValueItem(5.5, 5L, 13))

    val s = items.stream()

    val collector =
      Collectors.summarizingInt((e: ValueItem) => e.intValue)

    requireIdentityCharacteristicOnly(collector.characteristics())

    val summary = s.collect(collector)

    // Proper stats
    assertEquals("count", nElements, summary.getCount())
    assertEquals("sum", expectedSum, summary.getSum())
    assertEquals("min", expectedMin, summary.getMin())
    assertEquals("average", expectedAverage, summary.getAverage(), epsilon)
    assertEquals("max", expectedMax, summary.getMax())
  }

  @Test def collectorsSummarizingLong(): Unit = {

    val nElements = 7
    val expectedSum = 353L
    val expectedMin = 22L
    val expectedAverage = expectedSum / (nElements * 1.0)
    val expectedMax = 100L

    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 36L, 29))
    items.add(ValueItem(2.2, 32L, 66))
    items.add(ValueItem(1.1, 100L, 54))
    items.add(ValueItem(4.4, 84L, 15))
    items.add(ValueItem(0.0, 22L, 63))
    items.add(ValueItem(6.6, 45L, 82))
    items.add(ValueItem(5.5, 34L, 13))

    val s = items.stream()

    val collector =
      Collectors.summarizingLong((e: ValueItem) => e.longValue)

    requireIdentityCharacteristicOnly(collector.characteristics())

    val summary = s.collect(collector)

    // Proper stats
    assertEquals("count", nElements, summary.getCount())
    assertEquals("sum", expectedSum, summary.getSum())
    assertEquals("min", expectedMin, summary.getMin())
    assertEquals("average", expectedAverage, summary.getAverage(), epsilon)
    assertEquals("max", expectedMax, summary.getMax())
  }

  @Test def collectorsSummingDouble(): Unit = {

    val nElements = 7
    val expectedSum = 23.1

    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 3L, 3))
    items.add(ValueItem(2.2, 2L, 2))
    items.add(ValueItem(1.1, 1L, 1))
    items.add(ValueItem(4.4, 4L, 4))
    items.add(ValueItem(0.0, 0L, 0))
    items.add(ValueItem(6.6, 6L, 6))
    items.add(ValueItem(5.5, 5L, 5))

    val s = items.stream()

    val collector =
      Collectors.summingDouble((e: ValueItem) => e.doubleValue)

    requireEmptyCharacteristics(collector.characteristics())

    val sum = s.collect(collector)

    assertEquals("sum", expectedSum, sum, epsilon)
  }

  @Test def collectorsSummingInt(): Unit = {

    val nElements = 7
    val expectedSum = 322

    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 3L, 29))
    items.add(ValueItem(2.2, 2L, 66))
    items.add(ValueItem(1.1, 1L, 54))
    items.add(ValueItem(4.4, 4L, 15))
    items.add(ValueItem(0.0, 0L, 63))
    items.add(ValueItem(6.6, 6L, 82))
    items.add(ValueItem(5.5, 5L, 13))

    val s = items.stream()

    val collector =
      Collectors.summingInt((e: ValueItem) => e.intValue)

    requireEmptyCharacteristics(collector.characteristics())

    val sum = s.collect(collector)

    assertEquals("sum", expectedSum, sum)
  }

  @Test def collectorsSummingLong(): Unit = {

    val nElements = 7
    val expectedSum = 353L

    val items = new ArrayList[ValueItem](nElements)
    items.add(ValueItem(3.3, 36L, 29))
    items.add(ValueItem(2.2, 32L, 66))
    items.add(ValueItem(1.1, 100L, 54))
    items.add(ValueItem(4.4, 84L, 15))
    items.add(ValueItem(0.0, 22L, 63))
    items.add(ValueItem(6.6, 45L, 82))
    items.add(ValueItem(5.5, 34L, 13))

    val s = items.stream()

    val collector =
      Collectors.summingLong((e: ValueItem) => e.longValue)

    requireEmptyCharacteristics(collector.characteristics())

    val sum = s.collect(collector)

    assertEquals("sum", expectedSum, sum)
  }

  @Test def collectorsToMap_2Arg(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val nElements = 7

    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Maya", 0))
    employees.add(Employee("Electra", 1))
    employees.add(Employee("Taygete", 2))
    employees.add(Employee("Alcyone", 3))
    employees.add(Employee("Celaeno", 4))
    employees.add(Employee("Sterope", 5))
    employees.add(Employee("Merope", 6))

    val s = employees.stream()

    val collector =
      Collectors.toMap((e: Employee) => e.name, (e: Employee) => e.badgeNumber)

    requireIdentityCharacteristicOnly(collector.characteristics())

    val map = s.collect(collector)

    assertEquals("count", nElements, map.size())

    map.forEach((k: String, v: Int) =>
      assertEquals(
        s"contents: key: '${k}' value: ${v}",
        employees.get(v).badgeNumber,
        v
      )
    )
  }

  @Test def collectorsToMap_3Arg(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val nElements = 7

    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Maya", 0))
    employees.add(Employee("Electra", 1))
    employees.add(Employee("Taygete", 2))
    employees.add(Employee("Alcyone", 3))
    employees.add(Employee("Merope", -6))
    employees.add(Employee("Sterope", 5))
    employees.add(Employee("Merope", 6))

    val expectedCount = nElements - 1 // One entry, "Merope", will be merged.

    val expectedReplacement = -36

    val s = employees.stream()

    val collector =
      Collectors.toMap(
        (e: Employee) => e.name,
        (e: Employee) => e.badgeNumber,
        (found1: Int, found2: Int) => found1 * found2
      )

    requireIdentityCharacteristicOnly(collector.characteristics())

    val map = s.collect(collector)

    assertEquals("count", expectedCount, map.size())

    map.forEach((k: String, v: Int) =>
      k match {
        case k if (k == "Merope") =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            expectedReplacement,
            v
          )

        case _ =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            employees.get(v).badgeNumber,
            v
          )
      }
    )
  }

  @Test def collectorsToMap_4Arg(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val nElements = 7

    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Maya", 0))
    employees.add(Employee("Electra", 1))
    employees.add(Employee("Taygete", 2))
    employees.add(Employee("Alcyone", 3))
    employees.add(Employee("Merope", -6))
    employees.add(Employee("Sterope", 5))
    employees.add(Employee("Merope", 6))

    val expectedCount = nElements - 1 // One entry, "Merope", will be merged.

    val expectedReplacement = -36

    val s = employees.stream()

    val collector =
      Collectors.toMap(
        (e: Employee) => e.name,
        (e: Employee) => e.badgeNumber,
        (found1: Int, found2: Int) => found1 * found2,
        () => new HashMap[String, Int]
      )

    requireIdentityCharacteristicOnly(collector.characteristics())

    val map = s.collect(collector)

    assertEquals("count", expectedCount, map.size())

    map.forEach((k: String, v: Int) =>
      k match {
        case k if (k == "Merope") =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            expectedReplacement,
            v
          )

        case _ =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            employees.get(v).badgeNumber,
            v
          )
      }
    )
  }

  /* toCollection() use case URL:
   *     https://stackoverflow.com/questions/21697349/
   *         using-streams-to-collect-into-treeset-with-custom-comparator
   */

  @Test def collectorsToCollection(): Unit = {
    /* This implements one of the examples in the Java 19 description of the
     * java.util.Collectors class:
     *   // Accumulate names into a TreeSet
     */

    case class TimeStamp(name: String, stamp: Long, index: Int)

    val nTimeStamps = 7
    val timestamps = new ArrayList[TimeStamp](nTimeStamps)
    // Ensure that the timestamps are not inserted in sorted or reverse order.
    timestamps.add(TimeStamp("Prime", 3, 0))
    timestamps.add(TimeStamp("Matins", 1, 1))
    timestamps.add(TimeStamp("Compline", 7, 2))
    timestamps.add(TimeStamp("Terce", 4, 3))
    timestamps.add(TimeStamp("Lauds", 2, 4))
    timestamps.add(TimeStamp("Nones", 6, 5))
    timestamps.add(TimeStamp("Sext", 5, 6))

    val expectedSet = new TreeSet[TimeStamp]()

    val s = timestamps.stream()

    val collector =
      Collectors.toCollection(() =>
        new TreeSet[TimeStamp](
          Comparator.comparingLong((e) => e.asInstanceOf[TimeStamp].stamp)
        )
      )

    requireIdentityCharacteristicOnly(collector.characteristics())

    val treeSet: TreeSet[TimeStamp] = s.collect(collector)

    assertEquals(
      "TreeSet has wrong number of elements",
      nTimeStamps,
      treeSet.size()
    )

    treeSet
      .spliterator()
      .forEachRemaining((e) =>
        assertEquals(
          "unexpected element",
          timestamps.get(e.index).name,
          e.name
        )
      )
  }

  @Test def collectorsToConcurrentMap_2Arg(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val nElements = 7

    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Maya", 0))
    employees.add(Employee("Electra", 1))
    employees.add(Employee("Taygete", 2))
    employees.add(Employee("Alcyone", 3))
    employees.add(Employee("Celaeno", 4))
    employees.add(Employee("Sterope", 5))
    employees.add(Employee("Merope", 6))

    val s = employees.stream()

    val collector = Collectors.toConcurrentMap(
      (e: Employee) => e.name,
      (e: Employee) => e.badgeNumber
    )

    requireAll3Characteristics(collector.characteristics())

    val map = s.collect(collector)

    assertEquals("count", nElements, map.size())

    map.forEach((k: String, v: Int) =>
      assertEquals(
        s"contents: key: '${k}' value: ${v}",
        employees.get(v).badgeNumber,
        v
      )
    )
  }

  @Test def collectorsToConcurrentMap_3Arg(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val nElements = 7

    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Maya", 0))
    employees.add(Employee("Electra", 1))
    employees.add(Employee("Taygete", 2))
    employees.add(Employee("Alcyone", 3))
    employees.add(Employee("Merope", -6))
    employees.add(Employee("Sterope", 5))
    employees.add(Employee("Merope", 6))

    val expectedCount = nElements - 1 // One entry, "Merope", will be merged.

    val expectedReplacement = -36

    val s = employees.stream()

    val collector = Collectors.toConcurrentMap(
      (e: Employee) => e.name,
      (e: Employee) => e.badgeNumber,
      (found1: Int, found2: Int) => found1 * found2
    )

    requireAll3Characteristics(collector.characteristics())

    val map = s.collect(collector)

    assertEquals("count", expectedCount, map.size())

    map.forEach((k: String, v: Int) =>
      k match {
        case k if (k == "Merope") =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            expectedReplacement,
            v
          )

        case _ =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            employees.get(v).badgeNumber,
            v
          )
      }
    )
  }

  @Test def collectorsToConcurrentMap_4Arg(): Unit = {
    case class Employee(name: String, badgeNumber: Int)

    val nElements = 7

    val employees = new ArrayList[Employee](nElements)
    employees.add(Employee("Maya", 0))
    employees.add(Employee("Electra", 1))
    employees.add(Employee("Taygete", 2))
    employees.add(Employee("Alcyone", 3))
    employees.add(Employee("Merope", -6))
    employees.add(Employee("Sterope", 5))
    employees.add(Employee("Merope", 6))

    val expectedCount = nElements - 1 // One entry, "Merope", will be merged.

    val expectedReplacement = -36

    val s = employees.stream()

    val collector =
      Collectors.toConcurrentMap(
        (e: Employee) => e.name,
        (e: Employee) => e.badgeNumber,
        (found1: Int, found2: Int) => found1 * found2,
        () => new ConcurrentHashMap[String, Int]
      )

    requireAll3Characteristics(collector.characteristics())

    val map = s.collect(collector)

    assertEquals("count", expectedCount, map.size())

    map.forEach((k: String, v: Int) =>
      k match {
        case k if (k == "Merope") =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            expectedReplacement,
            v
          )

        case _ =>
          assertEquals(
            s"contents: key: '${k}' value: ${v}",
            employees.get(v).badgeNumber,
            v
          )
      }
    )
  }

  @Test def collectorsToList(): Unit = {
    /* This implements one of the examples in the Java 19 description of the
     * java.util.Collectors class:
     *   // Accumulate names into a List
     */
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val s = sisters.stream()

    val collector = Collectors.toList[String]()

    requireIdentityCharacteristicOnly(collector.characteristics())

    val collected = s.collect(collector)

    assertEquals("list size", nElements, collected.size())

    // Proper elements, in encounter order
    for (j <- 0 until nElements)
      assertEquals("list element", sisters.get(j), collected.get(j))
  }

  @Test def collectorsToSet(): Unit = {
    val nElements = 7
    val sisters = new ArrayList[String](nElements)
    sisters.add("Maya")
    sisters.add("Electra")
    sisters.add("Taygete")
    sisters.add("Alcyone")
    sisters.add("Celaeno")
    sisters.add("Sterope")
    sisters.add("Merope")

    val s = sisters.stream()

    val collector = Collectors.toSet[String]()

    requireIdentityUnorderedCharacteristicOnly(collector.characteristics())

    val collected = s.collect(collector)

    assertEquals("set size", nElements, collected.size())

    // Proper elements
    for (j <- 0 until nElements) {
      val expected = sisters.get(j)
      assertTrue(
        "set element not in Set: ${expected}",
        collected.contains(expected)
      )
    }
  }

}
