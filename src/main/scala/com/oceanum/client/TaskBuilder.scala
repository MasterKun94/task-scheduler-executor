package com.oceanum.client

import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger
import java.util.{Date, UUID}

import com.oceanum.cluster.exec.State

import scala.concurrent.duration.Duration

/**
 * @author chenmingkun
 * @date 2020/7/1
 */
@SerialVersionUID(1L)
abstract class TaskBuilder[T <: TaskBuilder[_, _], P <: TaskProp](task: Task) extends Serializable {
  protected val prop: P = task.prop.asInstanceOf[P]

  def id(id: Int): T = {
    typedBuilder(task.copy(id = id))
  }

  def topic(topic: String): T = {
    typedBuilder(task.copy(topic = topic))
  }

  def user(user: String): T = {
    typedBuilder(task.copy(user = user))
  }

  def retryCount(int: Int):T = {
    typedBuilder(task.copy(retryCount = int))
  }

  def retryInterval(interval: String): T = {
    typedBuilder(task.copy(retryInterval = interval))
  }

  def priority(priority: Int): T = {
    typedBuilder(task.copy(priority = priority))
  }

  def lazyInit(func: T => T): T = {
    val meta = task.metadata.lazyInit = task => func(typedBuilder(task)).build
    typedBuilder(task.copy(meta = meta))
  }

  def checkStateInterval(interval: String): T = {
    typedBuilder(task.copy(checkStateInterval = interval))
  }

  def parallelism(parallelism: Int): T = {
    typedBuilder(task.copy(parallelism = parallelism))
  }

  def build: Task = task

  protected def typedBuilder(task: Task): T = this.getClass.getConstructor(task.getClass).newInstance(task).asInstanceOf[T]

  protected def typedBuilder(prop: TaskProp): T = typedBuilder(task.copy(prop = prop))
}

@SerialVersionUID(1L)
class ShellTaskBuilder(task: Task) extends TaskBuilder[ShellTaskBuilder, ShellTaskProp](task) {

  def command(cmd: Array[String]): ShellTaskBuilder = typedBuilder(prop.copy(cmd = cmd))

  def command(cmd: String): ShellTaskBuilder = command(cmd.split(" "))

  def environment(env: Map[String, String]): ShellTaskBuilder = typedBuilder(prop.copy(env = prop.env ++ env))

  def environment(key: String, value: String): ShellTaskBuilder = typedBuilder(prop.copy(env = prop.env + (key -> value)))

  def directory(dir: String): ShellTaskBuilder = typedBuilder(prop.copy(directory = dir))

  def waitForTimeout(timeout: String): ShellTaskBuilder = typedBuilder(prop.copy(waitForTimeout = Duration(timeout).toMillis))

}

@SerialVersionUID(1L)
class ShellScriptTaskBuilder(task: Task) extends TaskBuilder[ShellScriptTaskBuilder, ShellScriptTaskProp](task) {

  def scriptFile(file: String): ShellScriptTaskBuilder = typedBuilder(prop.copy(scriptFile = file))

  def args(args: String*): ShellScriptTaskBuilder = typedBuilder(prop.copy(args = args.toArray))

  def environment(env: Map[String, String]): ShellScriptTaskBuilder = typedBuilder(prop.copy(env = prop.env ++ env))

  def environment(key: String, value: String): ShellScriptTaskBuilder = typedBuilder(prop.copy(env = prop.env + (key -> value)))

  def directory(dir: String): ShellScriptTaskBuilder = typedBuilder(prop.copy(directory = dir))

  def waitForTimeout(timeout: String): ShellScriptTaskBuilder = typedBuilder(prop.copy(waitForTimeout = Duration(timeout).toMillis))
}

@SerialVersionUID(1L)
class JavaTaskBuilder(task: Task) extends TaskBuilder[JavaTaskBuilder, JavaTaskProp](task) {

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

@SerialVersionUID(1L)
class ScalaTaskBuilder(task: Task) extends TaskBuilder[ScalaTaskBuilder, ScalaTaskProp](task) {
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

@SerialVersionUID(1L)
class PythonTaskBuilder(task: Task) extends TaskBuilder[PythonTaskBuilder, PythonTaskProp](task) {
  def pyFile(file: String): PythonTaskBuilder = typedBuilder(prop.copy(pyFile = file))

  def args(args: String*): PythonTaskBuilder = typedBuilder(prop.copy(args = args.toArray))

  def environment(env: Map[String, String]): PythonTaskBuilder = typedBuilder(prop.copy(env = prop.env ++ env))

  def environment(key: String, value: String): PythonTaskBuilder = typedBuilder(prop.copy(env = prop.env + (key -> value)))

  def directory(dir: String): PythonTaskBuilder = typedBuilder(prop.copy(directory = dir))

  def waitForTimeout(timeout: String): PythonTaskBuilder = typedBuilder(prop.copy(waitForTimeout = Duration(timeout).toMillis))
}

@SerialVersionUID(1L)
class UserAddTaskBuilder(task: Task) extends TaskBuilder[UserAddTaskBuilder, UserAdd](task) {
  override protected def typedBuilder(task: Task): UserAddTaskBuilder = new UserAddTaskBuilder(task)
}

object TaskBuilder {
  private val int = new AtomicInteger(0)
  private def dateFormat: String = new SimpleDateFormat("yyyyMMdd").format(new Date())
  private def getId(prop: TaskProp): Int = int.getAndIncrement()
  val sys: SystemTaskBuilder.type = SystemTaskBuilder

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

  object SystemTaskBuilder {
    def userAdd(): UserAddTaskBuilder = {
      val prop = UserAdd("")
      new UserAddTaskBuilder(Task(prop = prop, id = getId(prop)))
    }
  }
}
