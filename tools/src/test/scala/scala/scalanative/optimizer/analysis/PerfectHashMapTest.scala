package scala.scalanative
package optimizer
package analysis

import PerfectHashMap._
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

object PerfectHashMapTest extends Properties("PerfectHashMap") {

  property("correctness") = forAll { map: Map[Int, Int] =>
    val perfectHashMap = PerfectHashMap(DynmethodPerfectHashMap.hash, map)

    map.forall { case (k, v) => perfectHashMap.perfectLookup(k) == v }
  }

}
