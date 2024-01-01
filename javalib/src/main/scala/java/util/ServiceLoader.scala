package java.util

import java.lang.Iterable
import java.{util => ju}

import scala.scalanative.unsafe._
import scala.scalanative.reflect.Reflect
import scala.scalanative.runtime.{UndefinedBehaviorError, RawPtr, Boxes}
import java.{util => ju}

final class ServiceLoader[S <: AnyRef] private[util] (
    serviceClass: Class[S],
    serviceProviders: Array[ServiceLoader.Provider[_ <: S]]
) extends Iterable[S] {
  import ServiceLoader.Provider

  def findFirst(): Optional[S] = stream()
    .map[S](_.get())
    .findFirst()

  def iterator(): ju.Iterator[S] = stream()
    .map[S](_.get())
    .iterator()
    .asInstanceOf[ju.Iterator[S]]

  def stream(): ju.stream.Stream[Provider[S]] = {
    import Spliterator._
    val characteristcs = DISTINCT | NONNULL | IMMUTABLE
    ju.stream.StreamSupport.stream(
      /*supplier*/ () =>
        Spliterators.spliterator[Provider[S]](
          serviceProviders.asInstanceOf[Array[Object]],
          /*characteristcs=*/ characteristcs
        ),
      /*characteristics=*/ characteristcs,
      /*parallel=*/ false
    )
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

  // Used in intrinsic transformation to create an instance of provider
  // based on raw function pointer `loadFn` used to allocate and instanitate Provider lazily
  private[util] def createIntrinsicProvider[S <: AnyRef](
      cls: Class[_ <: S],
      loadFn: RawPtr
  ): Provider[S] = new IntrinsicProvider(cls, Boxes.boxToCFuncPtr0(loadFn))

  private class IntrinsicProvider[S <: AnyRef](
      cls: Class[_ <: S],
      loadFn: CFuncPtr0[S]
  ) extends Provider[S] {
    def get(): S = loadFn()
    def `type`(): Class[_ <: S] = cls
  }

  private def intrinsic = throw new UndefinedBehaviorError(
    "Intrinsic call was not handled by the toolchain"
  )

  def loadInstalled[S <: AnyRef](service: Class[S]): ServiceLoader[S] =
    intrinsic
  def load[S <: AnyRef](service: Class[S]): ServiceLoader[S] = intrinsic
  def load[S <: AnyRef](
      service: Class[S],
      loader: ClassLoader
  ): ServiceLoader[S] = intrinsic
  // def load[S](layer: ModuleLayer, service: Class[S]): ServiceLoader[S] = ???
}
