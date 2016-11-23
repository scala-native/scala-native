package scala.scalanative
package compiler
package analysis

object PerfectHashMap {
  val MAX_D_VALUE = 1000

  def apply[K, V](hashFunc: (K, Long) => Long,
                  entries: Map[K, V]): PerfectHashMap[K, V] = {

    def createMinimalPerfectHash(
        hashMapSize: Int): Option[(Map[Int, Int], Map[Int, Option[V]])] = {

      /**
       * Creates a list of buckets, grouping them by the hash of the key.
       */
      def createBuckets(keys: Set[K]): List[Seq[K]] = {
        val bucketMap = keys.groupBy(key => mod(hashFunc(key, 0), hashMapSize))
        (0 until hashMapSize)
          .map(i =>
            bucketMap.get(i) match {
              case Some(set) => set.toSeq
              case None      => Seq()
          })
          .toList
      }

      // Sort the buckets in descending order by size
      val buckets =
        createBuckets(entries.keySet).sortBy(_.size)(Ordering[Int].reverse)

      /**
       * find a spot of all buckets with more than 1 element
       */
      def placeBuckets(buckets: List[Seq[K]],
                       keys: Map[Int, Int],
                       values: Map[Int, Option[V]])
        : Option[(Map[Int, Int], Map[Int, Option[V]])] = buckets match {
        case bucket :: tail if bucket.size > 1 =>
          /**
           * Finds slots for all element of a bucket.
           * Returns None, if no placement is found.
           *
           */
          def findSlots(d: Int,
                        item: Int,
                        slots: List[Int]): Option[(Int, List[Int])] = {
            if (d > MAX_D_VALUE) {
              None
            } else {
              if (item < bucket.size) {
                val slot = mod(hashFunc(bucket(item), d), hashMapSize)

                if (values.getOrElse(slot, None).isDefined || slots.contains(
                      slot)) {
                  findSlots(d + 1, 0, List())
                } else {
                  findSlots(d, item + 1, slot :: slots)
                }
              } else {
                Some((d, slots))
              }
            }
          }

          findSlots(1, 0, List()) match {
            case Some((d, slots)) =>
              val newValues = bucket.foldLeft(Map[Int, Option[V]]()) {
                case (acc, key) =>
                  val value      = entries(key)
                  val valueIndex = mod(hashFunc(key, d), hashMapSize)
                  acc + (valueIndex -> Some(value))
              }

              placeBuckets(
                tail,
                keys + (mod(hashFunc(bucket.head, 0), hashMapSize) -> d),
                values ++ newValues)
            case None => None
          }
        case _ => Some((keys, values))
      }

      placeBuckets(buckets, Map(), Map()) match {
        case Some((keys, values)) =>
          val valueKeySet = values.keySet
          val freeList    = (0 until hashMapSize).filterNot(valueKeySet)

          Some(
            buckets
              .filter(bucket => bucket.size == 1)
              .zip(freeList)
              .foldLeft((keys, values)) {
                case ((accKeys, accValues), (Seq(elem), freeValue)) =>
                  val keyIndex   = mod(hashFunc(elem, 0), hashMapSize)
                  val keyValue   = -freeValue - 1
                  val valueIndex = freeValue
                  val valueValue = Some(entries(elem))

                  (accKeys + (keyIndex     -> keyValue),
                   accValues + (valueIndex -> valueValue))
              })
        case None => None
      }
    }

    def helper(size: Int): PerfectHashMap[K, V] =
      createMinimalPerfectHash(size) match {
        case Some((keys, values)) =>
          new PerfectHashMap[K, V](mapToSeq(keys, 0, size),
                                   mapToSeq(values, None, size),
                                   hashFunc)
        case None =>
          helper(size + 1)
      }

    helper(entries.size)

  }

  def mapToSeq[T](map: Map[Int, T], default: T, size: Int): Seq[T] = {
    val mapWithDefault = map withDefaultValue default
    (0 until size).map(i => mapWithDefault(i))
  }

  def mod(a: Long, b: Int): Int = {
    val m = a % b
    (if (m < 0) m + b else m).toInt
  }
}

class PerfectHashMap[K, V](val keys: Seq[Int],
                           val values: Seq[Option[V]],
                           hashFunc: (K, Long) => Long) {

  lazy val size = keys.length

  def perfectLookup(key: K): V = {
    val h1 = PerfectHashMap.mod(hashFunc(key, 0), size)
    val d  = keys(h1)

    if (d < 0) {
      values(-d - 1).get
    } else {
      val h2 = PerfectHashMap.mod(hashFunc(key, d), size)
      values(h2).get
    }
  }
}
