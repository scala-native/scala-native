package scala.scalanative
package compiler
package analysis

object PerfectHashMap {
  def apply[K, V](hashFunc: (K, Long) => Long, entries: Seq[(K, V)]): PerfectHashMap[K, V] = {
    val hashMapSize = (entries.size).toInt
    val debug = entries match {
      case e: Seq[(String, V)] => e.exists{ case (k, _) => k.contains("foo") }
      case _ => false
    }

    if(debug) println(hashMapSize)

    def createMinimalPerfectHash(): (Map[Int, Int], Map[Int, Option[V]]) = {
      def createBuckets(keys: Seq[K]): List[Seq[K]] = {
        val bucketMap = keys.groupBy(key => mod(hashFunc(key, 0), hashMapSize))
        if(debug) println(bucketMap)
        (0 until hashMapSize).map(i => bucketMap.getOrElse(i, Seq())).toList
      }
      val buckets = createBuckets(entries.map(_._1)).sortBy(_.size)(Ordering[Int].reverse)
      //println(buckets)
      def helper(buckets: List[Seq[K]], keys: Map[Int, Int], values: Map[Int, Option[V]]): (Map[Int, Int], Map[Int, Option[V]]) = buckets match {
        case bucket :: tail if bucket.size > 1 =>
          def findSlots(d: Int, item: Int, slots: List[Int]): (Int, List[Int]) = {
            if(d % 1000000 == 0) println(d+ " "+ buckets+ " "+bucket)
            if(item < bucket.size) {
              val slot = mod(hashFunc(bucket(item), d), hashMapSize)

              if(values.getOrElse(slot, None).isDefined || slots.contains(slot)) {
                findSlots(d + 1, 0, List())
              } else {
                findSlots(d, item + 1, slot :: slots)
              }
            } else {
              (d, slots)
            }
          }
          //println(bucket)

          val (d, slots) = findSlots(1, 0, List())
          //println("slots: " + slots)
          //println("bucket: "+ bucket)
          val newValues = bucket.foldLeft(Map[Int, Option[V]]()){
            case (acc, key) =>
              val value = entries.find{ case (k, v) => k == key}.get._2
              val valueIndex = mod(hashFunc(key, d), hashMapSize)
              acc + (valueIndex -> Some(value))
          }

          //println("newValues (d = "+d+") " +newValues)

          helper(tail, keys + (mod(hashFunc(bucket.head, 0), hashMapSize) -> d), values ++ newValues)
        case _ => (keys, values)
      }

      val (keys, values) = helper(buckets, Map(), Map())
      val valueKeySet = values.keySet
      val freeList = (0 until hashMapSize).filterNot(valueKeySet)

      buckets.filter(bucket => bucket.size == 1).zip(freeList).foldLeft((keys, values)) {
        case ((accKeys, accValues), (Seq(elem), freeValue)) =>
          val keyIndex = mod(hashFunc(elem, 0), hashMapSize)
          val keyValue = - freeValue - 1
          val valueIndex = freeValue
          val valueValue = Some(entries.find{ case (k, v) => k == elem}.get._2)

          (accKeys + (keyIndex -> keyValue), accValues + (valueIndex -> valueValue))
      }
    }

    val (keys, values) = createMinimalPerfectHash()
    if(debug) println(keys, values)


    new PerfectHashMap[K, V](mapToSeq(keys, 0, hashMapSize), mapToSeq(values, None, hashMapSize), hashFunc)
  }

  def mapToSeq[T](map: Map[Int, T], default: T, size: Int): Seq[T] = {
    val mapWithDefault = map withDefaultValue  default
    (0 until size).map(i => mapWithDefault(i))
  }

  def mod(a: Long, b: Int): Int = {
    val m = a % b
    (if(m < 0) m + b else m).toInt
  }
}

class PerfectHashMap[K, V](val keys: Seq[Int], val values: Seq[Option[V]], hashFunc: (K, Long) => Long) {
  //println(keys)
  //println(values)
  lazy val size = keys.length
  //println(size)

  def perfectLookup(key: K): V = {
    val h1 = PerfectHashMap.mod(hashFunc(key, 0), size)
    val d = keys(h1)
    //println(key)
    //println("d = " +d)
    if(d < 0) {
      values(-d-1).get
    } else {
      val h2 = PerfectHashMap.mod(hashFunc(key, d), size).toInt
      //println(h2)
      values(h2).get
    }
  }
}