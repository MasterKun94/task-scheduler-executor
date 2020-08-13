package com.oceanum.exec

import java.util.UUID

import akka.actor.Props
import com.oceanum.client._
import com.oceanum.common.{ActorSystems, Environment, GraphContext, RichTaskMeta}
import com.oceanum.exec.StdHandlerFactory.default._
import com.oceanum.exec.tasks.SysTasks.UserAddTaskConfig
import com.oceanum.exec.tasks._
import com.oceanum.expr.JavaMap
import com.oceanum.pluggable.{PluggablePrimEndpoint, PrimListener}

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
trait TaskConfig {
  def close()

  def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): TaskConfig

  def prepare(env: GraphContext)(implicit ec: ExecutionContext): Future[_<:TaskConfig]
}

object TaskConfig {
  def from(taskProp: TaskProp, taskMeta: RichTaskMeta, listener: EventListener): TaskConfig = taskProp match {
    case prop: ShellTaskProp => ShellTaskConfig(
      prop.cmd,
      prop.env,
      prop.directory,
      prop.waitForTimeout,
      createStdOutHandler(taskMeta),
      createStdErrHandler(taskMeta)
    )
    case prop: ShellScriptTaskProp => ShellScriptTaskConfig(
      prop.scriptFile,
      prop.args,
      prop.env,
      prop.directory,
      prop.waitForTimeout,
      createStdOutHandler(taskMeta),
      createStdErrHandler(taskMeta)
    )
    case prop: JavaTaskProp => JavaTaskConfig(
      prop.jars,
      prop.mainClass,
      prop.args,
      prop.options,
      prop.env,
      prop.directory,
      prop.waitForTimeout,
      createStdOutHandler(taskMeta),
      createStdErrHandler(taskMeta)
    )
    case prop: ScalaTaskProp => ScalaTaskConfig(
      prop.jars,
      prop.mainClass,
      prop.args,
      prop.options,
      prop.env,
      prop.directory,
      prop.waitForTimeout,
      createStdOutHandler(taskMeta),
      createStdErrHandler(taskMeta)
    )
    case prop: PythonTaskProp => PythonTaskConfig(
      prop.pyFile,
      prop.args,
      prop.env,
      prop.directory,
      prop.waitForTimeout,
      createStdOutHandler(taskMeta),
      createStdErrHandler(taskMeta)
    )
    case prop: UserAdd => UserAddTaskConfig(prop.user)

    case prop: PluggableTaskProp =>
      val execListener: PrimListener = new PrimListener {
        override def updateState(info: Map[String, String]): Unit = {
          listener.running(meta => meta.copy(extendedProperties = meta.extendedProperties ++ info))
        }
      }
      val communicator = ActorSystems.SYSTEM.actorOf(Props(classOf[PluggablePrimEndpoint], execListener), UUID.randomUUID().toString)
      PluggableTaskConfig(
        args = prop._args,
        plugJars = Environment.PLUGGABLE_EXECUTOR_JARS,
        plugClass = prop._plugClass,
        mainClass = "com.oceanum.pluggable.Main",
        files = prop._files,
        jars = prop._jars,
        completeDir = taskMeta.execDir,
        options = prop._options,
        env = prop._env,
        directory = prop._directory,
        waitForTimeout = prop._waitForTimeout,
        communicator = communicator,
        stderrHandler = createStdErrHandler(taskMeta),
        stdoutHandler = createStdOutHandler(taskMeta)
      )
  }
}
