package scala.scalanative.reflect

import scala.collection.mutable
import java.lang as jl

final class LoadableModuleClass private[reflect] (
    val runtimeClass: Class[?],
    loadModuleFun: Function0[Any]
) {

  /** Loads the module instance and returns it. */
  def loadModule(): Any = loadModuleFun()
}

final class InstantiatableClass private[reflect] (
    val runtimeClass: Class[?],
    val declaredConstructors: List[InvokableConstructor]
) {

  /** Instantiates a new instance of this class using the zero-argument
   *  constructor.
   *
   *  @throws java.lang.InstantiationException
   *    (caused by a `NoSuchMethodException`) If this class does not have a
   *    public zero-argument constructor.
   */
  def newInstance(): Any = {
    getConstructor().fold[Any] {
      throw new InstantiationException(runtimeClass.getName).initCause(
        new NoSuchMethodException(runtimeClass.getName + ".<init>()")
      )
    } { ctor => ctor.newInstance() }
  }

  /** Looks up a public constructor identified by the types of its formal
   *  parameters.
   *
   *  If no such public constructor exists, returns `None`.
   */
  def getConstructor(parameterTypes: Class[?]*): Option[InvokableConstructor] =
    declaredConstructors.find(_.parameterTypes.sameElements(parameterTypes))
}

final class InvokableConstructor private[reflect] (
    val parameterTypes: Array[Class[?]],
    newInstanceFun: Function1[Array[Any], Any]
) {
  def newInstance(args: Any*): Any = {
    /* Check the number of actual arguments. We let the casts and unbox
     * operations inside `newInstanceFun` take care of the rest.
     */
    require(
      args.size == parameterTypes.size,
      "Reflect: wrong number of arguments for InvokableConstructor"
    )
    val adaptedArgs = (args zip parameterTypes).map {
      case (arg, tpe) => wideningPrimConversionIfRequired(arg, tpe)
    }
    newInstanceFun.apply(adaptedArgs.toArray)
  }

  /** Perform a widening primitive conversion if required.
   *
   *  According to
   *  https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2
   */
  private def wideningPrimConversionIfRequired(
      arg: Any,
      paramType: Class[?]
  ): Any = {
    paramType match {
      case java.lang.Short.TYPE =>
        arg match {
          case arg: Byte => arg.toShort
          case _         => arg
        }
      case java.lang.Integer.TYPE =>
        arg match {
          case arg: Byte  => arg.toInt
          case arg: Short => arg.toInt
          case arg: Char  => arg.toInt
          case _          => arg
        }
      case java.lang.Long.TYPE =>
        arg match {
          case arg: Byte  => arg.toLong
          case arg: Short => arg.toLong
          case arg: Int   => arg.toLong
          case arg: Char  => arg.toLong
          case _          => arg
        }
      case java.lang.Float.TYPE =>
        arg match {
          case arg: Byte  => arg.toFloat
          case arg: Short => arg.toFloat
          case arg: Int   => arg.toFloat
          case arg: Long  => arg.toFloat
          case arg: Char  => arg.toFloat
          case _          => arg
        }
      case java.lang.Double.TYPE =>
        arg match {
          case arg: Byte  => arg.toDouble
          case arg: Short => arg.toDouble
          case arg: Int   => arg.toDouble
          case arg: Long  => arg.toDouble
          case arg: Float => arg.toDouble
          case arg: Char  => arg.toDouble
          case _          => arg
        }
      case _ =>
        arg
    }
  }

  override def toString: String = {
    val builder = new jl.StringBuilder("InvokableContructor")
    builder.append("(")
    for (tpe <- parameterTypes) {
      builder.append(tpe.getName)
      builder.append(", ")
    }
    if (parameterTypes.length > 0) {
      builder.setLength(builder.length - 2)
    }
    builder.append(")")
    builder.toString
  }
}

object Reflect {
  private val loadableModuleClasses =
    mutable.Map.empty[String, LoadableModuleClass]

  private val instantiatableClasses =
    mutable.Map.empty[String, InstantiatableClass]

  // `protected[reflect]` makes it public in the IR
  protected[reflect] def registerLoadableModuleClass[T](
      fqcn: String,
      runtimeClass: Class[T],
      loadModuleFun: Function0[T]
  ): Unit = {
    loadableModuleClasses(fqcn) =
      new LoadableModuleClass(runtimeClass, loadModuleFun)
  }

  protected[reflect] def registerInstantiatableClass[T](
      fqcn: String,
      runtimeClass: Class[T],
      constructors: Array[(Array[Class[?]], Function1[Array[Any], Any])]
  ): Unit = {
    val invokableConstructors = constructors.map { c =>
      new InvokableConstructor(c._1, c._2)
    }
    instantiatableClasses(fqcn) =
      new InstantiatableClass(runtimeClass, invokableConstructors.toList)
  }

  /** Reflectively looks up a loadable module class.
   *
   *  A module class is the technical term referring to the class of a Scala
   *  `object`. The object or one of its super types (classes or traits) must be
   *  annotated with
   *  [[scala.scalanative.reflect.annotation.EnableReflectiveInstantiation @EnableReflectiveInstantiation]].
   *  Moreover, the object must be "static", i.e., declared at the top-level of
   *  a package or inside a static object.
   *
   *  If the module class cannot be found, either because it does not exist, was
   *  not `@EnableReflectiveInstantiation` or was not static, this method
   *  returns `None`.
   *
   *  @param fqcn
   *    Fully-qualified name of the module class, including its trailing `$`
   */
  def lookupLoadableModuleClass(fqcn: String): Option[LoadableModuleClass] =
    loadableModuleClasses.get(fqcn)

  /** Reflectively looks up an instantiable class.
   *
   *  The class or one of its super types (classes or traits) must be annotated
   *  with
   *  [[scala.scalanative.reflect.annotation.EnableReflectiveInstantiation @EnableReflectiveInstantiation]].
   *  Moreover, the class must not be abstract, nor be a local class (i.e., a
   *  class defined inside a `def`). Inner classes (defined inside another
   *  class) are supported.
   *
   *  If the class cannot be found, either because it does not exist, was not
   *  `@EnableReflectiveInstantiation` or was abstract or local, this method
   *  returns `None`.
   *
   *  @param fqcn
   *    Fully-qualified name of the class
   */
  def lookupInstantiatableClass(fqcn: String): Option[InstantiatableClass] =
    instantiatableClasses.get(fqcn)
}
