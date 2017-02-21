package scala.scalanative
package optimizer
package analysis
import scala.scalanative.nir.{Global, Type, Val}
import scala.scalanative.optimizer.analysis.ClassHierarchy.Method

/**
 *
 * Implementation based on the article:
 * 'Throw away the keys: Easy, Minimal Perfect Hashing' by Steve Hanov
 * (http://stevehanov.ca/blog/index.php?id=119)
 *
 */
object PerfectHashMap {
  val MAX_D_VALUE = 10000

  def apply[K, V](hashFunc: (K, Int) => Int,
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
           * Returns None, if no placement is found and MAX_D_VALUE is reached
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

  def mod(a: Int, b: Int): Int = {
    val m = a % b
    if (m < 0) m + b else m
  }
}

class PerfectHashMap[K, V](val keys: Seq[Int],
                           val values: Seq[Option[V]],
                           hashFunc: (K, Int) => Int) {

  lazy val size: Int = keys.length

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

object DynmethodPerfectHashMap {
  def apply(dynmethods: Seq[Method], allSignatures: Seq[String]): Val.Struct = {

    val signaturesWithIndex =
      allSignatures.zipWithIndex.foldLeft(Map[String, Int]()) {
        case (acc, (signature, index)) => acc + (signature -> index)
      }

    val entries = dynmethods.foldLeft(Map[Int, (Int, Val)]()) {
      case (acc, m) =>
        val index = signaturesWithIndex(Global.genSignature(m.name))
        acc + (index -> (index, m.value))
    }

    val perfectHashMap = PerfectHashMap[Int, (Int, Val)](hash, entries)

    val (keys, values) = perfectHashMap.values.map {
      case Some((k, v)) => (Val.Int(k), v)
      case None         => (Val.Int(-1), Val.Null)
    }.unzip

    Val.Struct(
      Global.None,
      Val.Int(perfectHashMap.size) ::
        (perfectHashMap.size match {
          case 0 =>
            List(Val.Null, Val.Null, Val.Null)
          case _ =>
            List(
              Val.Const(Val.Array(Type.Int, perfectHashMap.keys.map(Val.Int))),
              Val.Const(Val.Array(Type.Int, keys)),
              Val.Const(Val.Array(Type.Ptr, values))
            )
        })
    )
  }

  def hash(key: Int, salt: Int): Int = {
    (key + (salt * 31)) ^ salt
  }
}
