package com.oceanum.exec

import com.oceanum.client.{JavaTaskProp, PythonTaskProp, ScalaTaskProp, ShellScriptTaskProp, ShellTaskProp, TaskProp, UserAdd}
import com.oceanum.common.{GraphContext, RichTaskMeta}
import com.oceanum.exec.tasks.{JavaTaskConfig, PythonTaskConfig, ScalaTaskConfig, ShellScriptTaskConfig, ShellTaskConfig}

import scala.concurrent.{ExecutionContext, Future}
import StdHandlerFactory.default._
import com.oceanum.exec.tasks.SysTasks.UserAddTaskConfig
import com.oceanum.expr.JavaMap

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
  def from(taskProp: TaskProp, taskMeta: RichTaskMeta): TaskConfig = taskProp match {
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
  }
}
