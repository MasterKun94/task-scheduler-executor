package com.oceanum.common

import java.util.concurrent.atomic.AtomicBoolean

import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.AviatorFunction
import com.oceanum.annotation.{IOpFunction, IRepositoryFactory, IRestService, ISerialization, ISerializationMessage, IStdHandlerFactory, InjectType, Injection}
import com.oceanum.api.RestService
import com.oceanum.exec.StdHandlerFactory
import com.oceanum.expr.{Evaluator, OpFunction}
import com.oceanum.file.FileSystem
import com.oceanum.persistence.{Catalog, Repository, RepositoryFactory}
import com.oceanum.serialize.{Serialization, WrappedObject}
import com.oceanum.trigger.{Trigger, Triggers}
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
  private val opFunctions: mutable.Map[OperatorType, OpFunction] = mutable.Map()

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

        case InjectType.TRIGGER =>
          Triggers.addTrigger(func().asInstanceOf[Trigger])

        case InjectType.OPERATOR_FUNCTION =>
//          val operatorFunction = clazz.getAnnotation(classOf[IOpFunction])
          val opFunc = func().asInstanceOf[OpFunction]
          val opType = opFunc.getOpType
          opFunctions += (opType -> opFunctions.getOrElse(opType, OpFunction.empty(opType)).merge(opFunc))
//          Evaluator.addOpFunction(operatorFunction.value(), func().asInstanceOf[AviatorFunction])

        case InjectType.REPOSITORY_FACTORY =>
          val i = clazz.getAnnotation(classOf[IRepositoryFactory])
          repositoryFactories += (func -> i.priority())

        case InjectType.STD_HANDLER_FACTORY =>
          val i = clazz.getAnnotation(classOf[IStdHandlerFactory])
          stdHandlerFactories += (func -> i.priority())

        case InjectType.SERIALIZATION =>
          val i = clazz.getAnnotation(classOf[ISerialization])
          serializations += (func -> i.priority())

        case InjectType.REST_SERVICE =>
          val i = clazz.getAnnotation(classOf[IRestService])
          restServices += (func -> i.priority())

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
    opFunctions.foreach(kv => Evaluator.addOpFunction(kv._1, kv._2.toAviator))
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
          val opFunc = field.asInstanceOf[OpFunction]
          val opType = opFunc.getOpType
          opFunctions += (opType -> opFunctions.getOrElse(opType, OpFunction.empty(opType)).merge(opFunc))

        case InjectType.REPOSITORY_FACTORY =>
          val IRepositoryFactory = field.getAnnotation(classOf[IRepositoryFactory])
          repositoryFactories += ((() => fieldObj) -> IRepositoryFactory.priority())

        case InjectType.STD_HANDLER_FACTORY =>
          val stdHandlerFactory = field.getAnnotation(classOf[IStdHandlerFactory])
          stdHandlerFactories += ((() => fieldObj) -> stdHandlerFactory.priority())

        case InjectType.REST_SERVICE =>
          val i = field.getAnnotation(classOf[IRestService])
          restServices += ((() => fieldObj) -> i.priority())

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
          val opFunc = methodObj().asInstanceOf[OpFunction]
          val opType = opFunc.getOpType
          opFunctions += (opType -> opFunctions.getOrElse(opType, OpFunction.empty(opType)).merge(opFunc))

        case InjectType.REPOSITORY_FACTORY =>
          val IRepositoryFactory = method.getAnnotation(classOf[IRepositoryFactory])
          repositoryFactories += (methodObj -> IRepositoryFactory.priority())

        case InjectType.STD_HANDLER_FACTORY =>
          val stdHandlerFactory = method.getAnnotation(classOf[IStdHandlerFactory])
          stdHandlerFactories += (methodObj -> stdHandlerFactory.priority())

        case InjectType.SERIALIZATION =>
          val iSerialization = method.getAnnotation(classOf[ISerialization])
          serializations += (methodObj -> iSerialization.priority())

        case InjectType.REST_SERVICE =>
          val i = method.getAnnotation(classOf[IRestService])
          restServices += (methodObj -> i.priority())

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
