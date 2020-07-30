package com.oceanum.annotation.util

import java.util

import com.oceanum.annotation.Injection
import org.quartz.Job
import org.reflections.Reflections

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * 通过注解获取所有通用Job信息
  *
  * @author BarryWang create at 2018/5/12 10:45
  * @version 0.0.1
  */
object JobEnum {
  /**
    * 获取添加JobInfo注解的类信息
    */
  val jobWithAnnotation: util.Set[Class[_]] = new Reflections("com.oceanum").getTypesAnnotatedWith(classOf[Injection])

  /**
    * 获取所有job枚举值
    * @return
    */
  def values : mutable.Set[Injection] = jobWithAnnotation.asScala.map(getAnnotation)

  /**
    * 根据job class 获取job 信息
    * @param jobClass
    * @return
    */
  def getAnnotation(jobClass : Class[_]): Injection = jobClass.getAnnotation(classOf[Injection])

  /**
    * JobObject与JobEnum映射关系
    * @return
    */
  def jobClassInfoMap: Map[String, Injection] = {
    jobWithAnnotation.asScala.map{sub =>
      Map(sub.getName -> getAnnotation(sub))
    }.fold(Map())((i,j) => i++j)
  }

  def main(args: Array[String]): Unit = {
    println(jobWithAnnotation)
  }
}