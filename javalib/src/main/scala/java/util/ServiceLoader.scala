package java.util

import java.lang.Iterable
import java.{util => ju}

import scala.scalanative.unsafe._
import scala.scalanative.reflect.Reflect
import scala.scalanative.runtime.{UndefinedBehaviorError, RawPtr, fromRawPtr}

final class ServiceLoader[S <: AnyRef] private[util] (
    serviceClass: Class[S],
    serviceProviders: Array[_ <: S]
) extends Iterable[S] {

  def findFirst(): Optional[S] = {
    val it = this.iterator()
    if (it.hasNext()) Optional.of(it.next())
    else Optional.empty()
  }

  def iterator(): ju.Iterator[S] = new ju.Iterator[S] {
    var idx = 0
    override def hasNext(): Boolean = idx < serviceProviders.length
    override def next(): S = {
      val instance = serviceProviders(idx).asInstanceOf[S]
      idx += 1
      instance
    }
  }

  def stream(): ju.stream.Stream[ServiceLoader.Provider[S]] = {
    val builder = ju.stream.Stream.builder[ServiceLoader.Provider[S]]()
    // TODO: the stream shall be lazy, instanization of provider should be delayed until `get()`
    serviceProviders.foreach { provider =>
      builder.accept(new ServiceLoader.Provider[S] {
        override def `type`(): Class[_ <: S] = provider.getClass()
        override def get(): S = provider
      })
    }
    builder.build()
  }

  def reload(): Unit = ()

  override def toString(): String =
    s"${this.getClass().getName()}[${serviceClass.getName()}]"
}

object ServiceLoader {
  trait Provider[S <: AnyRef] {
    def get(): S
    def `type`(): Class[_ <: S]
  }

  // Intrinsics
  def loadInstalled[S <: AnyRef](service: Class[S]): ServiceLoader[S] =
    throw new UndefinedBehaviorError()
  def load[S <: AnyRef](service: Class[S]): ServiceLoader[S] =
    throw new UndefinedBehaviorError()
  def load[S <: AnyRef](
      service: Class[S],
      loader: ClassLoader
  ): ServiceLoader[S] =
    throw new UndefinedBehaviorError()
  // def load[S](layer: ModuleLayer, service: Class[S]): ServiceLoader[S] = ???
}
