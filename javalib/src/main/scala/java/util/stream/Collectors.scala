package java.util.stream

import java.lang.StringBuilder

import java.util._

import java.util.concurrent.{ConcurrentMap, ConcurrentHashMap}

import java.util.function._

import java.util.stream.Collector.Characteristics

/* Design Notes:
 *   * This implementation is complete through Java 12, the
 *     last version with changes to this class. Any missing method is a bug.
 *
 *   * Many methods in this file could have been written entirely using
 *     lambdas for the arguments to the called Collector. This is
 *     idiomatic, concise, and elegant.
 *
 *     By design & intent, this file is implemented with a concern for
 *     corrections and maintenance. In many cases, separate variables are
 *     used where the equivalent lambda would be complex or more than a line
 *     or two.
 *     This makes it easier, for some, to parse the complex call and make
 *     point edits at the intended place.
 *
 *     When the code is stable and proven, it can be converted to the
 *     all-lambda style and submitted to the Obfuscated Scala contest.
 */

object Collectors {

  def averagingDouble[T](
      mapper: ToDoubleFunction[_ >: T]
  ): Collector[T, AnyRef, Double] = {
    type A = DoubleSummaryStatistics

    Collector
      .of[T, A, Double](
        () => new A,
        (stats: A, e: T) => stats.accept(mapper.applyAsDouble(e)),
        (stats1: A, stats2: A) => {
          stats1.combine(stats2)
          stats1
        },
        (stats: A) => stats.getAverage()
      )
      .asInstanceOf[Collector[T, AnyRef, Double]]
  }

  def averagingInt[T](
      mapper: ToIntFunction[_ >: T]
  ): Collector[T, AnyRef, Double] = {
    type A = IntSummaryStatistics

    Collector
      .of[T, A, Double](
        () => new A,
        (stats: A, e: T) => stats.accept(mapper.applyAsInt(e)),
        (stats1: A, stats2: A) => {
          stats1.combine(stats2)
          stats1
        },
        (stats: A) => stats.getAverage()
      )
      .asInstanceOf[Collector[T, AnyRef, Double]]
  }

  def averagingLong[T](
      mapper: ToLongFunction[_ >: T]
  ): Collector[T, AnyRef, Double] = {
    type A = LongSummaryStatistics

    Collector
      .of[T, A, Double](
        () => new A,
        (stats: A, e: T) => stats.accept(mapper.applyAsLong(e)),
        (stats1: A, stats2: A) => {
          stats1.combine(stats2)
          stats1
        },
        (stats: A) => stats.getAverage()
      )
      .asInstanceOf[Collector[T, AnyRef, Double]]
  }

  def collectingAndThen[T, A, R, RR](
      downstream: Collector[T, A, R],
      finisher: Function[R, RR]
  ): Collector[T, A, RR] = {

    val transformingFinisher =
      new Function[A, RR] {
        def apply(accum: A): RR =
          finisher(downstream.finisher()(accum))
      }

    def removeIdentityFinish(
        original: Set[Collector.Characteristics]
    ): Set[Collector.Characteristics] = {
      val hs = new HashSet[Collector.Characteristics]

      original.forEach(c =>
        if (c != Collector.Characteristics.IDENTITY_FINISH)
          hs.add(c)
      )

      hs
    }

    collectorOf[T, A, RR](
      downstream.supplier(),
      downstream.accumulator(),
      downstream.combiner(),
      transformingFinisher,
      removeIdentityFinish(downstream.characteristics())
    )
  }

  def counting[T](): Collector[T, AnyRef, Long] = {
    type A = Array[Long]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new A(1)
        arr(0) = 0L
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        val acc = accum(0)
        accum(0) = accum(0) + 1L
      }
    }

    val combiner = new BinaryOperator[A] {
      def apply(
          count1: A,
          count2: A
      ): A = {
        count1(0) = count1(0) + count2(0)
        count1
      }
    }

    Collector
      .of[T, Array[Long], Long](
        supplier,
        accumulator,
        combiner,
        (counter: Array[Long]) => counter(0)
      )
      .asInstanceOf[Collector[T, AnyRef, Long]]
  }

  // Since: Java 9
  def filtering[T, A, R](
      predicate: Predicate[_ >: T],
      downstream: Collector[_ >: T, A, R]
  ): Collector[T, AnyRef, R] = {

    val dsAccumulator = downstream.accumulator()

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        if (predicate.test(element))
          dsAccumulator.accept(accum, element)
      }
    }

    collectorOf[T, A, R](
      downstream.supplier(),
      accumulator,
      downstream.combiner(),
      downstream.finisher(),
      downstream.characteristics()
    )
      .asInstanceOf[Collector[T, AnyRef, R]]
  }

  // Since: Java 9
  def flatMapping[T, U, A, R](
      mapper: Function[_ >: T, _ <: Stream[U]],
      downstream: Collector[_ >: U, A, R]
  ): Collector[T, AnyRef, R] = {

    val dsAccumulator = downstream.accumulator()

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        mapper(element).forEach(e => dsAccumulator.accept(accum, e))
      }
    }

    collectorOf[T, A, R](
      downstream.supplier(),
      accumulator,
      downstream.combiner(),
      downstream.finisher(),
      downstream.characteristics()
    )
      .asInstanceOf[Collector[T, AnyRef, R]]
  }

  def groupingBy[T, K](
      classifier: Function[_ >: T, _ <: K]
  ): Collector[T, AnyRef, Map[K, List[T]]] = {
    type A = HashMap[K, ArrayList[T]]

    val supplier = new Supplier[A] {
      def get(): A = new A
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        val key = classifier(element)
        accum.compute(
          key,
          (k, oldValue) => {
            val list =
              if (oldValue != null) oldValue
              else new ArrayList[T]()
            list.add(element)
            list
          }
        )
      }
    }

    val combiner = new BinaryOperator[A] {
      def apply(
          map1: A,
          map2: A
      ): A = {
        map1.putAll(map2)
        map1
      }
    }

    Collector
      .of[T, A](
        supplier,
        accumulator,
        combiner
      )
      .asInstanceOf[Collector[T, AnyRef, Map[K, List[T]]]]
  }

  def groupingBy[T, K, D, A, M <: Map[K, D]](
      classifier: Function[_ >: T, _ <: K],
      mapFactory: Supplier[M],
      downstream: Collector[_ >: T, A, D]
  ): Collector[T, AnyRef, M] = {

    // The type of the workspace need not be the type A of downstream container
    val workspace = new Supplier[HashMap[K, ArrayList[T]]] {
      def get(): HashMap[K, ArrayList[T]] = {
        new HashMap[K, ArrayList[T]]
      }
    }

    val accumulator = new BiConsumer[HashMap[K, ArrayList[T]], T] {
      def accept(accum: HashMap[K, ArrayList[T]], element: T): Unit = {
        val key = classifier(element)
        accum.compute(
          key,
          (k, oldValue) => {
            val list =
              if (oldValue != null) oldValue
              else new ArrayList[T]()
            list.add(element)
            list
          }
        )
      }
    }

    val combiner = new BinaryOperator[HashMap[K, ArrayList[T]]] {
      def apply(
          map1: HashMap[K, ArrayList[T]],
          map2: HashMap[K, ArrayList[T]]
      ): HashMap[K, ArrayList[T]] = {
        map1.putAll(map2)
        map1
      }
    }

    val finisher =
      new Function[HashMap[K, ArrayList[T]], M] {
        def apply(accum: HashMap[K, ArrayList[T]]): M = {
          val resultMap = mapFactory.get()

          accum.forEach((k, v) => {
            val reduced = v.stream().collect(downstream)
            resultMap.put(k, reduced)
          })

          resultMap
        }
      }

    Collector
      .of[T, HashMap[K, ArrayList[T]], M](
        workspace,
        accumulator,
        combiner,
        finisher
      )
      .asInstanceOf[Collector[T, AnyRef, M]]
  }

  def groupingBy[T, K, A, D](
      classifier: Function[_ >: T, _ <: K],
      downstream: Collector[_ >: T, A, D]
  ): Collector[T, AnyRef, Map[K, D]] = {

    val supplier = new Supplier[HashMap[K, ArrayList[T]]] {
      def get(): HashMap[K, ArrayList[T]] = {
        new HashMap[K, ArrayList[T]]
      }
    }

    val accumulator = new BiConsumer[HashMap[K, ArrayList[T]], T] {
      def accept(accum: HashMap[K, ArrayList[T]], element: T): Unit = {
        val key = classifier(element)
        accum.compute(
          key,
          (k, oldValue) => {
            val list =
              if (oldValue != null) oldValue
              else new ArrayList[T]()
            list.add(element)
            list
          }
        )
      }
    }

    val combiner = new BinaryOperator[HashMap[K, ArrayList[T]]] {
      def apply(
          map1: HashMap[K, ArrayList[T]],
          map2: HashMap[K, ArrayList[T]]
      ): HashMap[K, ArrayList[T]] = {
        map1.putAll(map2)
        map1
      }
    }

    val finisher =
      new Function[HashMap[K, ArrayList[T]], HashMap[K, D]] {
        def apply(accum: HashMap[K, ArrayList[T]]): HashMap[K, D] = {
          val resultMap = new HashMap[K, D](accum.size())

          accum.forEach((k, v) => {
            val reduced = v.stream().collect(downstream)
            resultMap.put(k, reduced)
          })

          resultMap
        }
      }

    Collector
      .of[T, HashMap[K, ArrayList[T]], HashMap[K, D]](
        supplier,
        accumulator,
        combiner,
        finisher
      )
      .asInstanceOf[Collector[T, AnyRef, Map[K, D]]]
  }

  def groupingByConcurrent[T, K](
      classifier: Function[_ >: T, _ <: K]
  ): Collector[T, AnyRef, ConcurrentMap[K, List[T]]] = {
    type A = ConcurrentHashMap[K, ArrayList[T]]

    val supplier = new Supplier[A] {
      def get(): A = {
        new A
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(
          accum: A,
          element: T
      ): Unit = {
        val key = classifier(element)
        accum.compute(
          key,
          (k, oldValue) => {
            val list =
              if (oldValue != null) oldValue
              else new ArrayList[T]()
            list.add(element)
            list
          }
        )
      }
    }

    val combiner = new BinaryOperator[A] {
      def apply(
          map1: A,
          map2: A
      ): A = {
        map1.putAll(map2)
        map1
      }
    }

    Collector
      .of[T, A](
        supplier,
        accumulator,
        combiner,
        Collector.Characteristics.CONCURRENT,
        Collector.Characteristics.UNORDERED
        // This 4 arg Collector constructor will add IDENTITY_FINISH.
      )
      .asInstanceOf[Collector[T, AnyRef, ConcurrentMap[K, List[T]]]]
  }

  def groupingByConcurrent[T, K, D, A, M <: ConcurrentMap[K, D]](
      classifier: Function[_ >: T, _ <: K],
      mapFactory: Supplier[M],
      downstream: Collector[_ >: T, A, D]
  ): Collector[T, AnyRef, M] = {

    // The type of the workspace need not be the type A of downstream container
    val workspace = new Supplier[ConcurrentHashMap[K, ArrayList[T]]] {
      def get(): ConcurrentHashMap[K, ArrayList[T]] = {
        new ConcurrentHashMap[K, ArrayList[T]]
      }
    }

    val accumulator = new BiConsumer[ConcurrentHashMap[K, ArrayList[T]], T] {
      def accept(
          accum: ConcurrentHashMap[K, ArrayList[T]],
          element: T
      ): Unit = {
        val key = classifier(element)
        accum.compute(
          key,
          (k, oldValue) => {
            val list =
              if (oldValue != null) oldValue
              else new ArrayList[T]()
            list.add(element)
            list
          }
        )
      }
    }

    val combiner = new BinaryOperator[ConcurrentHashMap[K, ArrayList[T]]] {
      def apply(
          map1: ConcurrentHashMap[K, ArrayList[T]],
          map2: ConcurrentHashMap[K, ArrayList[T]]
      ): ConcurrentHashMap[K, ArrayList[T]] = {
        map1.putAll(map2)
        map1
      }
    }

    val finisher =
      new Function[ConcurrentHashMap[K, ArrayList[T]], M] {
        def apply(accum: ConcurrentHashMap[K, ArrayList[T]]): M = {
          val resultMap = mapFactory.get()

          accum.forEach((k, v) => {
            val reduced = v.stream().collect(downstream)
            resultMap.put(k, reduced)
          })

          resultMap
        }
      }

    Collector
      .of[T, ConcurrentHashMap[K, ArrayList[T]], M](
        workspace,
        accumulator,
        combiner,
        finisher,
        Collector.Characteristics.CONCURRENT,
        Collector.Characteristics.UNORDERED
      )
      .asInstanceOf[Collector[T, AnyRef, M]]
  }

  def groupingByConcurrent[T, K, A, D](
      classifier: Function[_ >: T, _ <: K],
      downstream: Collector[_ >: T, A, D]
  ): Collector[T, AnyRef, ConcurrentMap[K, D]] = {

    // The type of the workspace need not be the type A of downstream container

    val supplier = new Supplier[ConcurrentHashMap[K, ArrayList[T]]] {
      def get(): ConcurrentHashMap[K, ArrayList[T]] = {
        new ConcurrentHashMap[K, ArrayList[T]]
      }
    }

    val accumulator = new BiConsumer[ConcurrentHashMap[K, ArrayList[T]], T] {
      def accept(
          accum: ConcurrentHashMap[K, ArrayList[T]],
          element: T
      ): Unit = {
        val key = classifier(element)
        accum.compute(
          key,
          (k, oldValue) => {
            val list =
              if (oldValue != null) oldValue
              else new ArrayList[T]()
            list.add(element)
            list
          }
        )
      }
    }

    val combiner = new BinaryOperator[ConcurrentHashMap[K, ArrayList[T]]] {
      def apply(
          map1: ConcurrentHashMap[K, ArrayList[T]],
          map2: ConcurrentHashMap[K, ArrayList[T]]
      ): ConcurrentHashMap[K, ArrayList[T]] = {
        map1.putAll(map2)
        map1
      }
    }

    val finisher =
      new Function[
        ConcurrentHashMap[K, ArrayList[T]],
        ConcurrentHashMap[K, D]
      ] {
        def apply(
            accum: ConcurrentHashMap[K, ArrayList[T]]
        ): ConcurrentHashMap[K, D] = {
          val resultMap = new ConcurrentHashMap[K, D](accum.size())

          accum.forEach((k, v) => {
            val reduced = v.stream().collect(downstream)
            resultMap.put(k, reduced)
          })

          resultMap
        }
      }

    Collector
      .of[T, ConcurrentHashMap[K, ArrayList[T]], ConcurrentHashMap[K, D]](
        supplier,
        accumulator,
        combiner,
        finisher,
        Collector.Characteristics.CONCURRENT,
        Collector.Characteristics.UNORDERED
      )
      .asInstanceOf[Collector[T, AnyRef, ConcurrentMap[K, D]]]
  }

  def joining(): Collector[CharSequence, AnyRef, String] =
    joining("", "", "")

  def joining(
      delimiter: CharSequence
  ): Collector[CharSequence, AnyRef, String] =
    joining(delimiter, "", "")

  def joining(
      delimiter: CharSequence,
      prefix: CharSequence,
      suffix: CharSequence
  ): Collector[CharSequence, AnyRef, String] = {
    val delimiterLength = delimiter.length()

    val supplier = new Supplier[StringBuilder] {
      def get(): StringBuilder = {
        val sb = new StringBuilder()
        if (prefix != "")
          sb.append(prefix)
        sb
      }
    }

    val accumulator = new BiConsumer[StringBuilder, CharSequence] {
      def accept(accum: StringBuilder, element: CharSequence): Unit = {
        val acc = accum.append(element)
        if (delimiter != "")
          accum.append(delimiter)
      }
    }

    val combiner = new BinaryOperator[StringBuilder] {
      def apply(
          sb1: StringBuilder,
          sb2: StringBuilder
      ): StringBuilder = {
        sb1.append(sb2)
      }
    }

    val finisher =
      new Function[StringBuilder, String] {
        def apply(accum: StringBuilder): String = {

          if ((accum.length() > prefix.length()) && (delimiterLength > 0)) {
            /* This branch means accum has contents beyond a possible prefix.
             * If a delimiter arg was is specified, accumlator() will have
             * appended that delimiter. A delimiter is unwanted after what is
             * now known to be the last item, so trim it off before possibly
             * adding a suffix.
             */
            val lastIndex = accum.length() - delimiterLength
            accum.setLength(lastIndex) // trim off last delimiter sequence.
          }
          // Else empty stream; no token accepted, hence no delimiter to trim.

          if (suffix != "")
            accum.append(suffix)

          accum.toString()
        }
      }

    Collector
      .of[CharSequence, StringBuilder, String](
        supplier,
        accumulator,
        combiner,
        finisher
      )
      .asInstanceOf[Collector[CharSequence, AnyRef, String]]
  }

  def mapping[T, U, A, R](
      mapper: Function[_ >: T, _ <: U],
      downstream: Collector[_ >: U, A, R]
  ): Collector[T, AnyRef, R] = {

    val dsAccumulator = downstream.accumulator()

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        dsAccumulator.accept(accum, mapper(element))
      }
    }

    collectorOf[T, A, R](
      downstream.supplier(),
      accumulator,
      downstream.combiner(),
      downstream.finisher(),
      downstream.characteristics()
    )
      .asInstanceOf[Collector[T, AnyRef, R]]
  }

  def maxBy[T](
      comparator: Comparator[_ >: T]
  ): Collector[T, AnyRef, Optional[T]] = {
    type A = Array[Optional[T]]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new A(1)
        arr(0) = Optional.empty[T]()
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        val acc = accum(0)

        if (acc.isEmpty() || (comparator.compare(acc.get(), element) < 0))
          accum(0) = Optional.of(element)
      }
    }

    Collector
      .of[T, A, Optional[T]](
        supplier,
        accumulator,
        (max1: Array[Optional[T]], max2: Array[Optional[T]]) =>
          if (!max1(0).isPresent()) max2
          else if (!max2(0).isPresent()) max1
          else if (comparator.compare(max1(0).get(), max2(0).get()) < 0) max2
          else max1,
        acc => acc(0)
      )
      .asInstanceOf[Collector[T, AnyRef, Optional[T]]]
  }

  def minBy[T](
      comparator: Comparator[_ >: T]
  ): Collector[T, AnyRef, Optional[T]] = {
    type A = Array[Optional[T]]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new A(1)
        arr(0) = Optional.empty[T]()
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        val acc = accum(0)

        if (acc.isEmpty() || (comparator.compare(acc.get(), element) > 0))
          accum(0) = Optional.of(element)
      }
    }

    Collector
      .of[T, A, Optional[T]](
        supplier,
        accumulator,
        (min1: Array[Optional[T]], min2: Array[Optional[T]]) =>
          if (!min1(0).isPresent()) min2
          else if (!min2(0).isPresent()) min1
          else if (comparator.compare(min1(0).get(), min2(0).get()) > 0) min2
          else min1,
        acc => acc(0)
      )
      .asInstanceOf[Collector[T, AnyRef, Optional[T]]]
  }

  def partitioningBy[T](
      predicate: Predicate[_ >: T]
  ): Collector[T, AnyRef, Map[Boolean, List[T]]] = {
    type A = HashMap[Boolean, ArrayList[T]]

    val supplier = new Supplier[A] {
      def get(): A = {
        val map = new A
        map.put(false, new ArrayList[T])
        map.put(true, new ArrayList[T])
        map
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        val dst = accum.get(predicate.test(element))
        dst.add(element)
      }
    }

    Collector
      .of[T, A](
        supplier,
        accumulator,
        (
            map1: HashMap[Boolean, ArrayList[T]],
            map2: HashMap[Boolean, ArrayList[T]]
        ) => {
          map1.putAll(map2)
          map1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, Map[Boolean, List[T]]]]
  }

  def partitioningBy[T, D, A](
      predicate: Predicate[_ >: T],
      downstream: Collector[_ >: T, A, D]
  ): Collector[T, AnyRef, Map[Boolean, D]] = {

    val supplier = new Supplier[HashMap[Boolean, ArrayList[T]]] {
      def get(): HashMap[Boolean, ArrayList[T]] = {
        val map = new HashMap[Boolean, ArrayList[T]]
        map.put(false, new ArrayList[T])
        map.put(true, new ArrayList[T])
        map
      }
    }

    val accumulator = new BiConsumer[HashMap[Boolean, ArrayList[T]], T] {
      def accept(accum: HashMap[Boolean, ArrayList[T]], element: T): Unit = {
        val dst = accum.get(predicate.test(element))
        dst.add(element)
      }
    }

    val finisher =
      new Function[HashMap[Boolean, ArrayList[T]], Map[Boolean, D]] {
        def apply(accum: HashMap[Boolean, ArrayList[T]]): Map[Boolean, D] = {
          val resultMap = new HashMap[Boolean, D]

          val trueValue = accum.get(true).stream().collect(downstream)
          resultMap.put(true, trueValue)

          val falseValue = accum.get(false).stream().collect(downstream)
          resultMap.put(false, falseValue)

          resultMap.asInstanceOf[Map[Boolean, D]]
        }
      }

    Collector
      .of[T, HashMap[Boolean, ArrayList[T]], Map[Boolean, D]](
        supplier,
        accumulator,
        (
            map1: HashMap[Boolean, ArrayList[T]],
            map2: HashMap[Boolean, ArrayList[T]]
        ) => {
          map1.putAll(map2)
          map1
        },
        finisher
      )
      .asInstanceOf[Collector[T, AnyRef, Map[Boolean, D]]]
  }

  def reducing[T](op: BinaryOperator[T]): Collector[T, AnyRef, Optional[T]] = {
    type A = Array[Optional[T]]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new A(1)
        arr(0) = Optional.empty[T]()
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        val acc = accum(0)

        accum(0) =
          if (acc.isEmpty()) Optional.of(element)
          else Optional.of(op.apply(acc.get(), element))
      }
    }

    Collector
      .of[T, A, Optional[T]](
        supplier,
        accumulator,
        (arr1: Array[Optional[T]], arr2: Array[Optional[T]]) =>
          if (!arr1(0).isPresent()) arr2
          else if (!arr2(0).isPresent()) arr1
          else {
            val result = new Array[Optional[T]](1)
            result(0) = Optional.of(op.apply(arr1(0).get(), arr2(0).get()))
            result
          },
        acc => acc(0)
      )
      .asInstanceOf[Collector[T, AnyRef, Optional[T]]]
  }

  def reducing[T](
      identity: T,
      op: BinaryOperator[T]
  ): Collector[T, AnyRef, T] = {
    type A = Array[T]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new Array[Object](1).asInstanceOf[A]
        arr(0) = identity
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        val acc = accum(0)

        accum(0) = op.apply(acc, element)
      }
    }

    Collector
      .of[T, A, T](
        supplier,
        accumulator,
        (arr1: Array[T], arr2: Array[T]) => {
          val result = new Array[Object](1).asInstanceOf[Array[T]]
          result(0) = op.apply(arr1(0), arr2(0))
          result
        },
        acc => acc(0)
      )
      .asInstanceOf[Collector[T, AnyRef, T]]
  }

  def reducing[T, U](
      identity: U,
      mapper: Function[_ >: T, _ <: U],
      op: BinaryOperator[U]
  ): Collector[T, AnyRef, U] = {
    type A = Array[U]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new Array[Object](1).asInstanceOf[A]
        arr(0) = identity
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        val acc = accum(0)

        accum(0) = op.apply(acc, mapper(element))
      }
    }

    Collector
      .of[T, A, U](
        supplier,
        accumulator,
        (arr1: Array[U], arr2: Array[U]) => {
          val result = new Array[Object](1).asInstanceOf[Array[U]]
          result(0) = op.apply(arr1(0), arr2(0))
          result
        },
        acc => acc(0)
      )
      .asInstanceOf[Collector[T, AnyRef, U]]
  }

  def summarizingDouble[T](
      mapper: ToDoubleFunction[_ >: T]
  ): Collector[T, AnyRef, DoubleSummaryStatistics] = {
    type A = DoubleSummaryStatistics

    Collector
      .of[T, A](
        () => new A,
        (stats: A, e: T) => stats.accept(mapper.applyAsDouble(e)),
        (stats1: A, stats2: A) => {
          stats1.combine(stats2)
          stats1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, DoubleSummaryStatistics]]
  }

  def summarizingInt[T](
      mapper: ToIntFunction[_ >: T]
  ): Collector[T, AnyRef, IntSummaryStatistics] = {
    type A = IntSummaryStatistics

    Collector
      .of[T, A](
        () => new A,
        (stats: A, e: T) => stats.accept(mapper.applyAsInt(e)),
        (stats1: A, stats2: A) => {
          stats1.combine(stats2)
          stats1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, IntSummaryStatistics]]
  }

  def summarizingLong[T](
      mapper: ToLongFunction[_ >: T]
  ): Collector[T, AnyRef, LongSummaryStatistics] = {
    type A = LongSummaryStatistics

    Collector
      .of[T, A](
        () => new A,
        (stats: A, e: T) => stats.accept(mapper.applyAsLong(e)),
        (stats1: A, stats2: A) => {
          stats1.combine(stats2)
          stats1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, LongSummaryStatistics]]
  }

  def summingDouble[T](
      mapper: ToDoubleFunction[_ >: T]
  ): Collector[T, AnyRef, Double] = {
    type A = Array[Double]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new A(1)
        arr(0) = 0.0
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        accum(0) = accum(0) + mapper.applyAsDouble(element)
      }
    }

    val combiner = new BinaryOperator[A] {
      def apply(arr1: A, arr2: A): A = {
        arr1(0) = arr1(0) + arr2(0)
        arr1
      }
    }

    Collector
      .of[T, A, Double](
        supplier,
        accumulator,
        combiner,
        (accum: A) => accum(0)
      )
      .asInstanceOf[Collector[T, AnyRef, Double]]
  }

  def summingInt[T](
      mapper: ToIntFunction[_ >: T]
  ): Collector[T, AnyRef, Int] = {
    type A = Array[Int]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new A(1)
        arr(0) = 0
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        accum(0) = accum(0) + mapper.applyAsInt(element)
      }
    }

    val combiner = new BinaryOperator[A] {
      def apply(arr1: A, arr2: A): A = {
        arr1(0) = arr1(0) + arr2(0)
        arr1
      }
    }

    Collector
      .of[T, A, Int](
        supplier,
        accumulator,
        combiner,
        (accum: A) => accum(0)
      )
      .asInstanceOf[Collector[T, AnyRef, Int]]
  }

  def summingLong[T](
      mapper: ToLongFunction[_ >: T]
  ): Collector[T, AnyRef, Long] = {
    type A = Array[Long]

    val supplier = new Supplier[A] {
      def get(): A = {
        val arr = new A(1)
        arr(0) = 0L
        arr
      }
    }

    val accumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        accum(0) = accum(0) + mapper.applyAsLong(element)
      }
    }

    val combiner = new BinaryOperator[A] {
      def apply(arr1: A, arr2: A): A = {
        arr1(0) = arr1(0) + arr2(0)
        arr1
      }
    }

    Collector
      .of[T, A, Long](
        supplier,
        accumulator,
        combiner,
        (accum: A) => accum(0)
      )
      .asInstanceOf[Collector[T, AnyRef, Long]]
  }

  def teeing[T, R1, R2, R](
      downstream1: Collector[T, AnyRef, R1],
      downstream2: Collector[T, AnyRef, R2],
      merger: BiFunction[_ >: R1, _ >: R2, R]
  ): Collector[T, AnyRef, R] = {
    type A = Tuple2[AnyRef, AnyRef]

    val ds1Accumulator = downstream1.accumulator() // capture type1
    val ds2Accumulator = downstream2.accumulator() // capture type2

    val lclSupplier = new Supplier[A] {
      def get(): A = {
        new A(
          downstream1.supplier().get(),
          downstream2.supplier().get()
        )
      }
    }

    val lclAccumulator = new BiConsumer[A, T] {
      def accept(accum: A, element: T): Unit = {
        ds1Accumulator.accept(accum._1, element)
        ds2Accumulator.accept(accum._2, element)
      }
    }

    def determineCharacteristics(
        set1: Set[Collector.Characteristics],
        set2: Set[Collector.Characteristics]
    ): Set[Collector.Characteristics] = {

      val hs = new HashSet[Collector.Characteristics]

      // The calling method uses a finisher(), so no IDENTITY_FINISH here.

      if (set1.contains(Collector.Characteristics.UNORDERED)
          && set2.contains(Collector.Characteristics.UNORDERED))
        hs.add(Collector.Characteristics.UNORDERED)

      if (set1.contains(Collector.Characteristics.CONCURRENT)
          && set2.contains(Collector.Characteristics.CONCURRENT))
        hs.add(Collector.Characteristics.CONCURRENT)

      hs
    }

    val lclCombiner = new BinaryOperator[A] {
      def apply(accum1: A, accum2: A): A = {
        Tuple2(
          downstream1.combiner()(accum1._1, accum2._1),
          downstream2.combiner()(accum2._1, accum2._2)
        )
      }
    }

    val lclFinisher =
      new Function[A, R] {
        def apply(accum: A): R = {
          merger(
            downstream1.finisher()(accum._1),
            downstream2.finisher()(accum._2)
          )
        }
      }

    collectorOf[T, A, R](
      lclSupplier,
      lclAccumulator,
      lclCombiner,
      lclFinisher,
      determineCharacteristics(
        downstream1.characteristics(),
        downstream2.characteristics()
      )
    )
      .asInstanceOf[Collector[T, AnyRef, R]]
  }

  def toCollection[T, C <: Collection[T]](
      collectionFactory: Supplier[C]
  ): Collector[T, AnyRef, C] = {

    Collector
      .of[T, C](
        collectionFactory,
        (col: C, e: T) => col.add(e),
        (col1: C, col2: C) => {
          col1.addAll(col2)
          col1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, C]]
  }

  def toConcurrentMap[T, K, U](
      keyMapper: Function[_ >: T, _ <: K],
      valueMapper: Function[_ >: T, _ <: U]
  ): Collector[T, AnyRef, ConcurrentMap[K, U]] = {
    type A = ConcurrentHashMap[K, U]

    Collector
      .of[T, A](
        () => new A,
        (map: A, e: T) => map.put(keyMapper(e), valueMapper(e)),
        (map1: A, map2: A) => {
          map1.putAll(map2)
          map1
        },
        Collector.Characteristics.CONCURRENT,
        Collector.Characteristics.UNORDERED
        // This 4 arg Collector constructor will add IDENTITY_FINISH.
      )
      .asInstanceOf[Collector[T, AnyRef, ConcurrentMap[K, U]]]
  }

  def toConcurrentMap[T, K, U](
      keyMapper: Function[_ >: T, _ <: K],
      valueMapper: Function[_ >: T, _ <: U],
      mergeFunction: BinaryOperator[U]
  ): Collector[T, AnyRef, ConcurrentMap[K, U]] = {
    type A = ConcurrentHashMap[K, U]

    Collector
      .of[T, A](
        () => new A,
        (
            map: A,
            e: T
        ) => map.merge(keyMapper(e), valueMapper(e), mergeFunction),
        (map1: A, map2: A) => {
          map1.putAll(map2)
          map1
        },
        Collector.Characteristics.CONCURRENT,
        Collector.Characteristics.UNORDERED
        // This 4 arg Collector constructor will add IDENTITY_FINISH.
      )
      .asInstanceOf[Collector[T, AnyRef, ConcurrentMap[K, U]]]
  }

  def toConcurrentMap[T, K, U, M <: ConcurrentMap[K, U]](
      keyMapper: Function[_ >: T, _ <: K],
      valueMapper: Function[_ >: T, _ <: U],
      mergeFunction: BinaryOperator[U],
      mapFactory: Supplier[M]
  ): Collector[T, AnyRef, M] = {
    Collector
      .of[T, M](
        () => mapFactory.get(),
        (
            map: M,
            e: T
        ) => map.merge(keyMapper(e), valueMapper(e), mergeFunction),
        (map1: M, map2: M) => {
          map1.putAll(map2)
          map1
        },
        Collector.Characteristics.CONCURRENT,
        Collector.Characteristics.UNORDERED
        // This 4 arg Collector constructor will add IDENTITY_FINISH.
      )
      .asInstanceOf[Collector[T, AnyRef, M]]
  }

  def toList[T](): Collector[T, AnyRef, List[T]] = {
    type A = ArrayList[T]

    Collector
      .of[T, A](
        () => new A,
        (list: A, e: T) => list.add(e),
        (list1: A, list2: A) => {
          list1.addAll(list2)
          list1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, List[T]]]
  }

  def toMap[T, K, U](
      keyMapper: Function[_ >: T, _ <: K],
      valueMapper: Function[_ >: T, _ <: U]
  ): Collector[T, AnyRef, Map[K, U]] = {
    type A = HashMap[K, U]

    Collector
      .of[T, A](
        () => new A,
        (map: A, e: T) => map.put(keyMapper(e), valueMapper(e)),
        (map1: A, map2: A) => {
          map1.putAll(map2)
          map1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, Map[K, U]]]
  }

  def toMap[T, K, U](
      keyMapper: Function[_ >: T, _ <: K],
      valueMapper: Function[_ >: T, _ <: U],
      mergeFunction: BinaryOperator[U]
  ): Collector[T, AnyRef, Map[K, U]] = {
    type A = HashMap[K, U]

    Collector
      .of[T, A](
        () => new A,
        (
            map: A,
            e: T
        ) => map.merge(keyMapper(e), valueMapper(e), mergeFunction),
        (map1: A, map2: A) => {
          map1.putAll(map2)
          map1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, Map[K, U]]]
  }

  def toMap[T, K, U, M <: Map[K, U]](
      keyMapper: Function[_ >: T, _ <: K],
      valueMapper: Function[_ >: T, _ <: U],
      mergeFunction: BinaryOperator[U],
      mapFactory: Supplier[M]
  ): Collector[T, AnyRef, M] = {

    Collector
      .of[T, M](
        () => mapFactory.get(),
        (
            map: M,
            e: T
        ) => map.merge(keyMapper(e), valueMapper(e), mergeFunction),
        (map1: M, map2: M) => {
          map1.putAll(map2)
          map1
        }
      )
      .asInstanceOf[Collector[T, AnyRef, M]]
  }

  def toSet[T](): Collector[T, AnyRef, Set[T]] = {
    type A = HashSet[T]

    Collector
      .of[T, A](
        () => new A,
        (set: A, e: T) => set.add(e),
        (set1: A, set2: A) => {
          set1.addAll(set2)
          set1
        },
        Collector.Characteristics.UNORDERED
        // This 4 arg Collector constructor will add IDENTITY_FINISH.
      )
      .asInstanceOf[Collector[T, AnyRef, Set[T]]]
  }

  // Since: Java 10
  def toUnmodifiableList[T](): Collector[T, AnyRef, List[T]] = {
    Collectors.collectingAndThen[T, AnyRef, List[T], List[T]](
      Collectors.toList[T](),
      (e: List[T]) => Collections.unmodifiableList[T](e)
    )
  }

  // Since: Java 10
  def toUnmodifiableMap[T, K, U](
      keyMapper: Function[_ >: T, _ <: K],
      valueMapper: Function[_ >: T, _ <: U]
  ): Collector[T, AnyRef, Map[K, U]] = {
    Collectors.collectingAndThen(
      Collectors.toMap[T, K, U](keyMapper, valueMapper),
      (e: Map[K, U]) => Collections.unmodifiableMap(e)
    )
  }

  // Since: Java 10
  def toUnmodifiableMap[T, K, U](
      keyMapper: Function[_ >: T, _ <: K],
      valueMapper: Function[_ >: T, _ <: U],
      mergeFunction: BinaryOperator[U]
  ): Collector[T, AnyRef, Map[K, U]] = {
    Collectors.collectingAndThen(
      Collectors.toMap[T, K, U](keyMapper, valueMapper, mergeFunction),
      (e: Map[K, U]) => Collections.unmodifiableMap(e)
    )
  }

  // Since: Java 10
  def toUnmodifiableSet[T](): Collector[T, AnyRef, Set[T]] = {
    Collectors.collectingAndThen(
      Collectors.toSet[T](),
      (e: Set[T]) => Collections.unmodifiableSet(e)
    )
  }

  private def collectorOf[T, A, R](
      _supplier: Supplier[A],
      _accumulator: BiConsumer[A, T],
      _combiner: BinaryOperator[A],
      _finisher: Function[A, R],
      _characteristics: Set[Collector.Characteristics]
  ): Collector[T, A, R] = {
    new Collector[T, A, R] {
      def accumulator(): BiConsumer[A, T] = _accumulator

      def characteristics(): Set[Collector.Characteristics] = _characteristics

      def combiner(): BinaryOperator[A] = _combiner

      def finisher(): Function[A, R] = _finisher

      def supplier(): Supplier[A] = _supplier
    }
  }

}
