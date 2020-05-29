package com.oceanum.client

import akka.actor.ActorRef
import com.oceanum.actors.StateHandler
import com.oceanum.exec.process.{JavaProp, ProcessProp, PythonProp, ScalaProp, ShellProp, ShellScriptProp, SuUserProp}
import com.oceanum.exec.{EventListener, InputStreamHandler, LineHandler, Operator, OperatorProp}

import scala.concurrent.duration.FiniteDuration

case class PrepareMessage(message: Any)
case class RunningMessage(message: Any)
case class FailedMessage(message: Any)
case class SuccessMessage(message: Any)
case class RetryMessage(message: Any)
case class KillMessage(message: Any)
case class TimeoutMessage(message: Any)
case class StartMessage(message: Any)

case object KillAction
case object TerminateAction
case object CheckStateOnce
case class CheckStateScheduled(duration: FiniteDuration, handler: StateHandler = StateHandler.empty())

case class AvailableExecutorRequest(topic: String)
case class AvailableExecutorsRequest(topic: String, maxWait: FiniteDuration)
case class AvailableExecutorResponse(executor: TraversableOnce[AvailableExecutor])
case class AvailableExecutor(actor: ActorRef, queueSize: Int, topics: Array[String])
case class ExecuteOperatorRequest(operatorMessage: Task, checkStateScheduled: CheckStateScheduled)
case class ExecuteOperatorResponse(operatorMessage: Task, checkStateScheduled: CheckStateScheduled)
case class HandleState(handler: StateHandler)
case class HandleOnComplete(handler: StateHandler)
case class ExecutorState(isSuccess: Boolean, iterator: Iterator[EventListener.State])

case class Task(name: String, retryCount: Int, retryInterval: Int, priority: Int, prop: TaskProp) {
  def toOperator(listener: EventListener): Operator[_ <: OperatorProp] = Operator(name, retryCount, retryInterval, priority, prop.toOperatorProp, listener)
}

trait TaskProp {
  def toOperatorProp: OperatorProp
}

abstract class ProcessTaskProp extends TaskProp {
  override def toOperatorProp: ProcessProp
}

case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: Option[String] = None,
                         waitForTimeout: Long = -1,
                         stdoutLineHandler: () => InputStreamHandler,
                         stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toOperatorProp: ProcessProp = ShellProp(
    cmd, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}

case class ShellScriptTaskProp(scriptFile: String,
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: Option[String] = None,
                               waitForTimeout: Long = -1,
                               stdoutLineHandler: () => InputStreamHandler,
                               stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toOperatorProp: ProcessProp = ShellScriptProp(
    scriptFile, args, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}

case class JavaTaskProp(jars: Array[String],
                        mainClass: String,
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: Option[String] = None,
                        waitForTimeout: Long = -1,
                        stdoutLineHandler: () => InputStreamHandler,
                        stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toOperatorProp: ProcessProp = JavaProp(
    jars, mainClass, args, options, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}

case class ScalaTaskProp(jars: Array[String],
                         mainClass: String,
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: Option[String] = None,
                         waitForTimeout: Long = -1,
                         stdoutLineHandler: () => InputStreamHandler,
                         stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toOperatorProp: ProcessProp = ScalaProp(
    jars, mainClass, args, options, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}
case class PythonTaskProp(pyFile: String,
                          args: Array[String] = Array.empty,
                          options: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = ".",
                          waitForTimeout: Long = -1,
                          stdoutLineHandler: () => InputStreamHandler,
                          stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toOperatorProp: ProcessProp = PythonProp(
    pyFile, args, options, env, directory, waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}

case class SuUserTaskProp(user: String, prop: ProcessTaskProp) extends TaskProp {
  override def toOperatorProp: ProcessProp = SuUserProp(user, prop.toOperatorProp)
}