package com.oceanum.annotation.util

import com.googlecode.aviator.runtime.`type`.AviatorFunction
import com.oceanum.annotation.{InjectType, Injection, OperatorFunction}
import com.oceanum.expr.Evaluator
import com.oceanum.file.FileClient
import com.oceanum.persistence.{AbstractRepository, Catalog, Repository}
import org.reflections.Reflections

import scala.collection.JavaConversions._
import scala.collection.mutable

object AnnotationUtil {
  private val reflections = new Reflections("com.oceanum")
  private val fileClients = mutable.Set[FileClient]()

  reflections.getTypesAnnotatedWith(classOf[Injection]).foreach { clazz =>
    val injection = clazz.getAnnotation(classOf[Injection])
    val obj = clazz.getConstructor().newInstance()
    injection.value() match {
      case InjectType.REPOSITORY =>
        Catalog.addRepository(obj.asInstanceOf[Repository[_<:AnyRef]])

      case InjectType.FILE_CLIENT =>
        fileClients += obj.asInstanceOf[FileClient]

      case InjectType.FUNCTION =>
        Evaluator.addFunction(obj.asInstanceOf[AviatorFunction])

      case InjectType.OPERATOR_FUNCTION =>
        val operatorFunction = clazz.getAnnotation(classOf[OperatorFunction])
        Evaluator.addOpFunction(operatorFunction.operatorType(), obj.asInstanceOf[AviatorFunction])

      case InjectType.CONFIGURATION =>
        val configuration = obj
        val fields = configuration.getClass
          .getDeclaredFields
          .filter(f => f.isAnnotationPresent(classOf[Injection]))
        fields.foreach { field =>
          val fieldInjection = field.getAnnotation(classOf[Injection])
          val fieldObj = field.get(obj)
          fieldInjection.value() match {
            case InjectType.REPOSITORY =>
              Catalog.addRepository(fieldObj.asInstanceOf[Repository[_<:AnyRef]])

            case InjectType.FILE_CLIENT =>
              fileClients += fieldObj.asInstanceOf[FileClient]

            case InjectType.FUNCTION =>
              Evaluator.addFunction(fieldObj.asInstanceOf[AviatorFunction])

            case InjectType.OPERATOR_FUNCTION =>
              val operatorFunction = field.getAnnotation(classOf[OperatorFunction])
              Evaluator.addOpFunction(operatorFunction.operatorType(), fieldObj.asInstanceOf[AviatorFunction])
          }
        }

        val methods = configuration.getClass
          .getDeclaredMethods
          .filter(f => f.isAnnotationPresent(classOf[Injection]))
        methods.foreach { method =>
          val methodInjection = method.getAnnotation(classOf[Injection])
          val methodObj = method.invoke(obj)
          methodInjection.value() match {
            case InjectType.REPOSITORY =>
              Catalog.addRepository(methodObj.asInstanceOf[Repository[_<:AnyRef]])

            case InjectType.FILE_CLIENT =>
              fileClients += methodObj.asInstanceOf[FileClient]

            case InjectType.FUNCTION =>
              Evaluator.addFunction(methodObj.asInstanceOf[AviatorFunction])

            case InjectType.OPERATOR_FUNCTION =>
              val operatorFunction = method.getAnnotation(classOf[OperatorFunction])
              Evaluator.addOpFunction(operatorFunction.operatorType(), methodObj.asInstanceOf[AviatorFunction])
          }
        }
    }
  }

  def main(args: Array[String]): Unit = {
    println(classOf[Repository[_]].isAssignableFrom(classOf[AbstractRepository[_]]))
  }
}