package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._

import java.{util => ju}
import java.util.ArrayList
import java.util.Arrays

import org.junit.Test
import org.junit.Assert._

class CollectorsTestOnJDK12 {

  private def requireEmptyCharacteristics(
      differentia: ju.Set[Collector.Characteristics]
  ): Unit = {
    assertEquals(
      s"unexpected extra characteristics: ${differentia}",
      0,
      differentia.size()
    )
  }

  // Since: Java 12
  @Test def collectorsTeeing(): Unit = {
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

    val collector0 =
      Collectors.teeing(
        Collectors.counting(),
        Collectors.filtering(
          (e: Employee) => e.department == "LKG",
          Collectors.counting()
        ),
        (r1, r2) => Arrays.asList(r1, r2)
      )

    /* The characteristics required of teeing() depends upon the
     * characteristics of the two downstreams. Here, both are simple
     * so expect zero characteristics.
     *
     * The tests for teeing() should be expanded to cover all four
     * combinations of characteristics: None, CONCURRENT-only,
     * UNORDERED-only, both CONCURRENT and UNORDERED.
     */

    requireEmptyCharacteristics(collector0.characteristics())

    val teed =
      s.collect(
        Collectors.teeing(
          Collectors.counting(),
          Collectors.filtering(
            (e: Employee) => e.department == "LKG",
            Collectors.counting()
          ),
          (r1, r2) => Arrays.asList(r1, r2)
        )
      )

    assertEquals("teed size", 2, teed.size())

    assertEquals("total employees", nElements.toLong, teed.get(0))
    assertEquals("LKG employees", 5L, teed.get(1))
  }

}
