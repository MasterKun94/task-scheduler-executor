package com.oceanum.client

import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import scala.concurrent.duration.Duration

/**
 * @author chenmingkun
 * @date 2020/7/1
 */
abstract class TaskBuilder[T <: TaskBuilder[_]](task: Task) {
  def id(id: String): T = typedBuilder(task.copy(id = id))

  def topic(topic: String): T = typedBuilder(task.copy(topic = topic))

  def user(user: String): T = typedBuilder(task.copy(user = user))

  def retryCount(int: Int):T = typedBuilder(task.copy(retryCount = int))

  def retryInterval(interval: String): T = typedBuilder(task.copy(retryInterval = interval))

  def priority(priority: Int): T = typedBuilder(task.copy(priority = priority))

  def build(suUser: String): Task = task.copy(prop = SuUserTaskProp(suUser, task.prop.asInstanceOf[ProcessTaskProp]))

  def build: Task = task

  protected def typedBuilder(task: Task): T

  protected def typedBuilder(prop: TaskProp): T = typedBuilder(task.copy(prop = prop))
}

class ShellTaskBuilder(task: Task) extends TaskBuilder[ShellTaskBuilder](task) {
  private val prop = task.prop.asInstanceOf[ShellTaskProp]
  override protected def typedBuilder(task: Task): ShellTaskBuilder = new ShellTaskBuilder(task)

  def command(cmd: Array[String]): ShellTaskBuilder = typedBuilder(prop.copy(cmd = cmd))

  def command(cmd: String): ShellTaskBuilder = command(cmd.split(" "))

  def environment(env: Map[String, String]): ShellTaskBuilder = typedBuilder(prop.copy(env = prop.env ++ env))

  def environment(key: String, value: String): ShellTaskBuilder = typedBuilder(prop.copy(env = prop.env + (key -> value)))

  def directory(dir: String): ShellTaskBuilder = typedBuilder(prop.copy(directory = dir))

  def waitForTimeout(timeout: String): ShellTaskBuilder = typedBuilder(prop.copy(waitForTimeout = Duration(timeout).toMillis))
}

class ShellScriptTaskBuilder(task: Task) extends TaskBuilder[ShellScriptTaskBuilder](task) {
  private val prop = task.prop.asInstanceOf[ShellScriptTaskProp]
  override protected def typedBuilder(task: Task): ShellScriptTaskBuilder = new ShellScriptTaskBuilder(task)

  def scriptFile(file: String): ShellScriptTaskBuilder = typedBuilder(prop.copy(scriptFile = file))

  def args(args: String*): ShellScriptTaskBuilder = typedBuilder(prop.copy(args = args.toArray))

  def environment(env: Map[String, String]): ShellScriptTaskBuilder = typedBuilder(prop.copy(env = prop.env ++ env))

  def environment(key: String, value: String): ShellScriptTaskBuilder = typedBuilder(prop.copy(env = prop.env + (key -> value)))

  def directory(dir: String): ShellScriptTaskBuilder = typedBuilder(prop.copy(directory = dir))

  def waitForTimeout(timeout: String): ShellScriptTaskBuilder = typedBuilder(prop.copy(waitForTimeout = Duration(timeout).toMillis))
}

class JavaTaskBuilder(task: Task) extends TaskBuilder[JavaTaskBuilder](task) {
  private val prop = task.prop.asInstanceOf[JavaTaskProp]
  override protected def typedBuilder(task: Task): JavaTaskBuilder = new JavaTaskBuilder(task)

  def jars(jarPaths: String*): JavaTaskBuilder = typedBuilder(prop.copy(jars = prop.jars ++ jarPaths))

  def jar(jarPath: String): JavaTaskBuilder = typedBuilder(prop.copy(jars = prop.jars :+ jarPath))

  def mainClass(clazz: String): JavaTaskBuilder = typedBuilder(prop.copy(mainClass = clazz))

  def args(args: String*): JavaTaskBuilder = typedBuilder(prop.copy(args = args.toArray))

  def options(opts: Array[String]): JavaTaskBuilder = typedBuilder(prop.copy(prop.options ++ opts))

  def option(opt: String): JavaTaskBuilder = typedBuilder(prop.copy(prop.options :+ opt))

  def environment(env: Map[String, String]): JavaTaskBuilder = typedBuilder(prop.copy(env = prop.env ++ env))

  def environment(key: String, value: String): JavaTaskBuilder = typedBuilder(prop.copy(env = prop.env + (key -> value)))

  def directory(dir: String): JavaTaskBuilder = typedBuilder(prop.copy(directory = dir))

  def waitForTimeout(timeout: String): JavaTaskBuilder = typedBuilder(prop.copy(waitForTimeout = Duration(timeout).toMillis))
}

class ScalaTaskBuilder(task: Task) extends TaskBuilder[ScalaTaskBuilder](task) {
  private val prop = task.prop.asInstanceOf[ScalaTaskProp]
  override protected def typedBuilder(task: Task): ScalaTaskBuilder = new ScalaTaskBuilder(task)

  def jars(jarPaths: String*): ScalaTaskBuilder = typedBuilder(prop.copy(jars = prop.jars ++ jarPaths))

  def jar(jarPath: String): ScalaTaskBuilder = typedBuilder(prop.copy(jars = prop.jars :+ jarPath))

  def mainClass(clazz: String): ScalaTaskBuilder = typedBuilder(prop.copy(mainClass = clazz))

  def args(args: String*): ScalaTaskBuilder = typedBuilder(prop.copy(args = args.toArray))

  def options(opts: Array[String]): ScalaTaskBuilder = typedBuilder(prop.copy(prop.options ++ opts))

  def option(opt: String): ScalaTaskBuilder = typedBuilder(prop.copy(prop.options :+ opt))

  def environment(env: Map[String, String]): ScalaTaskBuilder = typedBuilder(prop.copy(env = prop.env ++ env))

  def environment(key: String, value: String): ScalaTaskBuilder = typedBuilder(prop.copy(env = prop.env + (key -> value)))

  def directory(dir: String): ScalaTaskBuilder = typedBuilder(prop.copy(directory = dir))

  def waitForTimeout(timeout: String): ScalaTaskBuilder = typedBuilder(prop.copy(waitForTimeout = Duration(timeout).toMillis))
}

class PythonTaskBuilder(task: Task) extends TaskBuilder[PythonTaskBuilder](task) {
  private val prop = task.prop.asInstanceOf[PythonTaskProp]
  override protected def typedBuilder(task: Task): PythonTaskBuilder = new PythonTaskBuilder(task)

  def pyFile(file: String): PythonTaskBuilder = typedBuilder(prop.copy(pyFile = file))

  def args(args: String*): PythonTaskBuilder = typedBuilder(prop.copy(args = args.toArray))

  def environment(env: Map[String, String]): PythonTaskBuilder = typedBuilder(prop.copy(env = prop.env ++ env))

  def environment(key: String, value: String): PythonTaskBuilder = typedBuilder(prop.copy(env = prop.env + (key -> value)))

  def directory(dir: String): PythonTaskBuilder = typedBuilder(prop.copy(directory = dir))

  def waitForTimeout(timeout: String): PythonTaskBuilder = typedBuilder(prop.copy(waitForTimeout = Duration(timeout).toMillis))
}

object TaskBuilder {
  private def dateFormat: String = new SimpleDateFormat("yyyyMMdd").format(new Date())
  private def getId(prop: TaskProp): String = s"$dateFormat-${prop.taskType}-${UUID.randomUUID().toString}"

  def shell(): ShellTaskBuilder = {
    val prop = ShellTaskProp()
    new ShellTaskBuilder(Task(prop = prop, id = getId(prop)))
  }

  def shellScript(): ShellScriptTaskBuilder = {
    val prop = ShellScriptTaskProp()
    new  ShellScriptTaskBuilder(Task(prop = prop, id = getId(prop)))
  }

  def java(): JavaTaskBuilder = {
    val prop = JavaTaskProp()
    new JavaTaskBuilder(Task(prop = prop, id = getId(prop)))
  }

  def scala(): ScalaTaskBuilder = {
    val prop = ScalaTaskProp()
    new ScalaTaskBuilder(Task(prop = prop, id = getId(prop)))
  }

  def python(): PythonTaskBuilder = {
    val prop = PythonTaskProp()
    new PythonTaskBuilder(Task(prop = prop, id = getId(prop)))
  }

  def main(args: Array[String]): Unit = {

  }
}
