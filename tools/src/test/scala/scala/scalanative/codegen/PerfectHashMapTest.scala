// package scala.scalanative
// package codegen

// import PerfectHashMap._
// import org.scalacheck.Properties
// import org.scalacheck.Prop.forAll

// import org.junit.Test
// import org.junit.Assert._

// object PerfectHashMapTest extends Properties("PerfectHashMap") {

//   property("correctness") = forAll { (map: Map[Int, Int]) =>
//     val perfectHashMap = PerfectHashMap(DynmethodPerfectHashMap.hash, map)

//     map.forall { case (k, v) => perfectHashMap.perfectLookup(k) == v }
//   }

// }
