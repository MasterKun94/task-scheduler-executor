package com.oceanum.client

import akka.actor.ActorRef
import com.oceanum.actors.StateHandler
import com.oceanum.exec.process.{JavaProp, ProcessProp, PythonProp, ScalaProp, ShellProp, ShellScriptProp, SuUserProp}
import com.oceanum.exec.{EventListener, Operator, OperatorProp}

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
case class AvailableExecutorResponse(executor: AvailableExecutor)
case class AvailableExecutorsResponse(executors: Array[AvailableExecutor])
case class AvailableExecutor(actor: ActorRef, queueSize: Int, topics: Array[String])
case class ExecuteOperatorRequest(operatorMessage: OperatorMessage, checkStateScheduled: CheckStateScheduled)
case class ExecuteOperatorResponse(operatorMessage: OperatorMessage, checkStateScheduled: CheckStateScheduled)
case class HandleState(handler: StateHandler)
case class HandleOnComplete(handler: StateHandler)
case class ExecutorState(isSuccess: Boolean, iterator: Iterator[EventListener.State])

case class OperatorMessage(name: String, retryCount: Int, retryInterval: Int, priority: Int, prop: PropMessage) {
  def toOperator(listener: EventListener): Operator[_ <: OperatorProp] = Operator(name, retryCount, retryInterval, priority, prop.toOperatorProp, listener)
}

trait PropMessage {
  def toOperatorProp: OperatorProp
}

abstract class ProcessPropMessage extends PropMessage {
  override def toOperatorProp: ProcessProp
}

case class ShellPropMessage(cmd: Array[String] = Array.empty,
                            env: Map[String, String] = Map.empty,
                            directory: Option[String] = None,
                            waitForTimeout: Long = -1,
                            stdoutLineHandler: LineHandlerProducer,
                            stderrLineHandler: LineHandlerProducer) extends ProcessPropMessage {
  override def toOperatorProp: ProcessProp = ShellProp(
    cmd, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler.produce, stderrLineHandler.produce)
}

case class ShellScriptPropMessage(scriptFile: String,
                                  args: Array[String] = Array.empty,
                                  env: Map[String, String] = Map.empty,
                                  directory: Option[String] = None,
                                  waitForTimeout: Long = -1,
                                  stdoutLineHandler: LineHandlerProducer,
                                  stderrLineHandler: LineHandlerProducer) extends ProcessPropMessage {
  override def toOperatorProp: ProcessProp = ShellScriptProp(
    scriptFile, args, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler.produce, stderrLineHandler.produce)
}

case class JavaPropMessage(jars: Array[String],
                           mainClass: String,
                           args: Array[String] = Array.empty,
                           options: Array[String] = Array.empty,
                           env: Map[String, String] = Map.empty,
                           directory: Option[String] = None,
                           waitForTimeout: Long = -1,
                           stdoutLineHandler: LineHandlerProducer,
                           stderrLineHandler: LineHandlerProducer) extends ProcessPropMessage {
  override def toOperatorProp: ProcessProp = JavaProp(
    jars, mainClass, args, options, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler.produce, stderrLineHandler.produce)
}

case class ScalaPropMessage(jars: Array[String],
                            mainClass: String,
                            args: Array[String] = Array.empty,
                            options: Array[String] = Array.empty,
                            env: Map[String, String] = Map.empty,
                            directory: Option[String] = None,
                            waitForTimeout: Long = -1,
                            stdoutLineHandler: LineHandlerProducer,
                            stderrLineHandler: LineHandlerProducer) extends ProcessPropMessage {
  override def toOperatorProp: ProcessProp = ScalaProp(
    jars, mainClass, args, options, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler.produce, stderrLineHandler.produce)
}
case class PythonPropMessage(pyFile: String,
                             args: Array[String] = Array.empty,
                             options: Array[String] = Array.empty,
                             env: Map[String, String] = Map.empty,
                             directory: String = ".",
                             waitForTimeout: Long = -1,
                             stdoutLineHandler: LineHandlerProducer,
                             stderrLineHandler: LineHandlerProducer) extends ProcessPropMessage {
  override def toOperatorProp: ProcessProp = PythonProp(
    pyFile, args, options, env, directory, waitForTimeout, stdoutLineHandler.produce, stderrLineHandler.produce)
}

case class SuUserPropMessage(user: String, prop: ProcessPropMessage) extends PropMessage {
  override def toOperatorProp: ProcessProp = SuUserProp(user, prop.toOperatorProp)
}