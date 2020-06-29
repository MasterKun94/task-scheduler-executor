package com.oceanum.cluster

import akka.actor.{Actor, ActorLogging}
import com.oceanum.cluster.exec.{ExitCode, Hook, Operator, OperatorTask, RootRunner}
import com.oceanum.client.Implicits.DurationHelper
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/6/29
 */
class RunnerManageActor extends Actor with ActorLogging {
  type Prop = Operator[_ <: OperatorTask]
  private val exec = RootRunner
  import Environment.SCHEDULE_EXECUTION_CONTEXT // TODO

  override def receive: Receive = {
    case operatorProp: Prop =>
      exec.run(operatorProp) match {
        case ExitCode.ERROR =>
          if (operatorProp.retryCount > 1) {
            val newOperatorProp = operatorProp.retry()
            val cancellable = context.system.scheduler.scheduleOnce(fd"${newOperatorProp.retryInterval}") {
              // TODO
            }
            newOperatorProp.receive(Hook(cancellable))
            // do retry
            operatorProp.eventListener.retry()
            log.info("task begin retry: " + operatorProp.name)
          } else {
            // do close
            operatorProp.eventListener.failed()
            log.info("task failed: " + operatorProp.name)
          }

        case ExitCode.OK =>
          // do success
          operatorProp.eventListener.success()
          log.info("task success: " + operatorProp.name)

        case ExitCode.KILL =>
          // do kill
          operatorProp.eventListener.kill()
          log.info("task kill: " + operatorProp.name)

        case code: ExitCode.UN_SUPPORT =>
          log.info(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
          operatorProp.eventListener.failed(code)
      }

  }
}
