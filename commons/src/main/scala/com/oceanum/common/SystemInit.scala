package com.oceanum.common

import java.util.concurrent.atomic.AtomicBoolean

import com.googlecode.aviator.runtime.`type`.AviatorFunction
import com.oceanum.annotation.{IOpFunction, IRepositoryFactory, ISerialization, ISerializationMessage, IStdHandlerFactory, InjectType, Injection}
import com.oceanum.api.RestService
import com.oceanum.exec.StdHandlerFactory
import com.oceanum.expr.Evaluator
import com.oceanum.file.FileSystem
import com.oceanum.persistence.{Catalog, Repository, RepositoryFactory}
import com.oceanum.serialize.{Serialization, WrappedObject}
import org.reflections.Reflections

import scala.collection.JavaConversions.asScalaSet
import scala.collection.mutable

object SystemInit {
  private val annotatedClass = new Reflections("com.oceanum")
    .getTypesAnnotatedWith(classOf[Injection])
    .filterNot(_.isAnnotation)
    .toSet
  private val isInited = new AtomicBoolean(false)
  private val lazyInitSet = mutable.Set[() => Unit]()
  private val serializations: mutable.Map[() => Any, Int] = mutable.Map()
  private val stdHandlerFactories: mutable.Map[() => Any, Int] = mutable.Map()
  private val repositoryFactories: mutable.Map[() => Any, Int] = mutable.Map()
  private val restServices: mutable.Map[() => Any, Int] = mutable.Map()

  lazy val serialization: Serialization[_] = serializations.maxBy(_._2)._1().asInstanceOf[Serialization[_<:WrappedObject]]
  lazy val stdHandlerFactory: StdHandlerFactory = stdHandlerFactories.maxBy(_._2)._1().asInstanceOf[StdHandlerFactory]
  lazy val repositoryFactory: RepositoryFactory = repositoryFactories.maxBy(_._2)._1().asInstanceOf[RepositoryFactory]
  lazy val restService: RestService =  restServices.maxBy(_._2)._1().asInstanceOf[RestService]

  def initAnnotatedClass(): Unit = {
    if (isInited.getAndSet(true)) {
      return
    }
    for (clazz <- annotatedClass) {
      val injection = getInjection(clazz)
      val func: () => Any = () => clazz.getConstructor().newInstance()
      injection.value() match {
        case InjectType.REPOSITORY =>
          Catalog.addRepository(func().asInstanceOf[Repository[_<:AnyRef]])

        case InjectType.FILE_SYSTEM =>
          FileSystem.add(func().asInstanceOf[FileSystem])

        case InjectType.FUNCTION =>
          Evaluator.addFunction(func().asInstanceOf[AviatorFunction])

        case InjectType.OPERATOR_FUNCTION =>
          val operatorFunction = clazz.getAnnotation(classOf[IOpFunction])
          Evaluator.addOpFunction(operatorFunction.value(), func().asInstanceOf[AviatorFunction])

        case InjectType.REPOSITORY_FACTORY =>
          val IRepositoryFactory = clazz.getAnnotation(classOf[IRepositoryFactory])
          repositoryFactories += (func -> IRepositoryFactory.priority())

        case InjectType.STD_HANDLER_FACTORY =>
          val stdHandlerFactory = clazz.getAnnotation(classOf[IStdHandlerFactory])
          stdHandlerFactories += (func -> stdHandlerFactory.priority())

        case InjectType.SERIALIZATION =>
          val iSerialization = clazz.getAnnotation(classOf[ISerialization])
          serializations += (func -> iSerialization.priority())

        case InjectType.SERIALIZATION_MESSAGE =>
          val serializationMessage = clazz.getAnnotation(classOf[ISerializationMessage])
          lazyInitSet += (() => serialization.register(serializationMessage.value(), clazz.asInstanceOf[Class[_<:AnyRef]]))

        case InjectType.CONFIGURATION =>
          initAnnotatedField(func())
          initAnnotatedMethod(func())

        case _ =>
      }
    }
    lazyInitSet.foreach(_.apply())
  }

  private def initAnnotatedField(obj: Any): Unit = {
    val fields = obj.getClass
      .getDeclaredFields
      .filter(f => f.isAnnotationPresent(classOf[Injection]))
    for (field <- fields) {
      val fieldInjection = field.getAnnotation(classOf[Injection])
      val fieldObj = field.get(obj)
      fieldInjection.value() match {
        case InjectType.REPOSITORY =>
          Catalog.addRepository(fieldObj.asInstanceOf[Repository[_<:AnyRef]])

        case InjectType.FILE_SYSTEM =>
          FileSystem.add(fieldObj.asInstanceOf[FileSystem])

        case InjectType.FUNCTION =>
          Evaluator.addFunction(fieldObj.asInstanceOf[AviatorFunction])

        case InjectType.OPERATOR_FUNCTION =>
          val operatorFunction = field.getAnnotation(classOf[IOpFunction])
          Evaluator.addOpFunction(operatorFunction.value(), fieldObj.asInstanceOf[AviatorFunction])

        case InjectType.REPOSITORY_FACTORY =>
          val IRepositoryFactory = field.getAnnotation(classOf[IRepositoryFactory])
          repositoryFactories += ((() => fieldObj) -> IRepositoryFactory.priority())

        case InjectType.STD_HANDLER_FACTORY =>
          val stdHandlerFactory = field.getAnnotation(classOf[IStdHandlerFactory])
          stdHandlerFactories += ((() => fieldObj) -> stdHandlerFactory.priority())

        case InjectType.SERIALIZATION =>
          val iSerialization = field.getAnnotation(classOf[ISerialization])
          serializations += ((() => fieldObj) -> iSerialization.priority())

        case _ =>
      }
    }
  }

  private def initAnnotatedMethod(obj: Any): Unit = {
    val methods = obj.getClass
      .getDeclaredMethods
      .filter(f => f.isAnnotationPresent(classOf[Injection]))
    for (method <- methods) {
      val methodInjection = method.getAnnotation(classOf[Injection])
      val methodObj: () => Any = () => method.invoke(obj)
      methodInjection.value() match {
        case InjectType.REPOSITORY =>
          Catalog.addRepository(methodObj().asInstanceOf[Repository[_<:AnyRef]])

        case InjectType.FILE_SYSTEM =>
          FileSystem.add(methodObj().asInstanceOf[FileSystem])

        case InjectType.FUNCTION =>
          Evaluator.addFunction(methodObj().asInstanceOf[AviatorFunction])

        case InjectType.OPERATOR_FUNCTION =>
          val operatorFunction = method.getAnnotation(classOf[IOpFunction])
          Evaluator.addOpFunction(operatorFunction.value(), methodObj().asInstanceOf[AviatorFunction])

        case InjectType.REPOSITORY_FACTORY =>
          val IRepositoryFactory = method.getAnnotation(classOf[IRepositoryFactory])
          repositoryFactories += (methodObj -> IRepositoryFactory.priority())

        case InjectType.STD_HANDLER_FACTORY =>
          val stdHandlerFactory = method.getAnnotation(classOf[IStdHandlerFactory])
          stdHandlerFactories += (methodObj -> stdHandlerFactory.priority())

        case InjectType.SERIALIZATION =>
          val iSerialization = method.getAnnotation(classOf[ISerialization])
          serializations += (methodObj -> iSerialization.priority())

        case InjectType.INIT =>
          lazyInitSet += (methodObj.apply)

        case _ =>
      }
    }
  }

  @scala.annotation.tailrec
  private def getInjection(c: Class[_]): Injection = {
    val i = c.getAnnotation(classOf[Injection])
    if (i != null) {
      i
    } else {
      c.getAnnotations.find(_.annotationType.isAnnotationPresent(classOf[Injection])) match {
        case Some(a) => getInjection(a.annotationType())
        case None => throw new NullPointerException
      }
    }
  }
}