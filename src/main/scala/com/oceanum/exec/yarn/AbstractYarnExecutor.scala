package com.oceanum.exec.yarn

import com.oceanum.common.Log
import com.oceanum.exec.process.ProcessExecutor
import com.oceanum.exec.{OperatorProp, _}

/**
 * @author chenmingkun
 * @date 2020/5/9
 */
class AbstractYarnExecutor extends Executor[BasicYarnProp] with Log {
  private val processExecutor = new ProcessExecutor(OutputManager.global) {
    override def executable(p: OperatorProp): Boolean = true
  }

  override protected def typedExecute(operatorProp: Operator[_ <: BasicYarnProp]): ExitCode = {
    val yarnProp = operatorProp.prop
    val extractor = new YarnAppIdExtractor()
    val newProp = operatorProp.copy(prop = yarnProp.copy(stdoutHandler = LineHandler.multiHandler(yarnProp.stdoutHandler, extractor)))
    val pre = System.currentTimeMillis()
    val exitCode = processExecutor.execute(newProp)
    val after = System.currentTimeMillis()
    if (exitCode == ExitCode.OK) {
      val appTask = extractor.getAppTask

      if (operatorProp.hook.isKilled) {
        ExitCode.KILL
      } else {
        operatorProp.receive(new YarnExecutorHook(appTask))
        ExitCode(waitFor(appTask, yarnProp.waitForTimeout + pre - after))
      }

    } else {
      exitCode
    }
  }

  override def executable(p: OperatorProp): Boolean = p.isInstanceOf[BasicYarnProp]

  private def waitFor(appTask: YarnAppTask, timeout: Long): Int = {
    0
  }

  class YarnAppIdExtractor extends LineHandler {
    override def handle(line: String): Unit = ???

    override def close(): Unit = ???

    def getAppTask: YarnAppTask = ???
  }

  class YarnAppTask {
    def appId: String = ???

    def appUrl: String = ???

    def resourceManagerUrl: String = ???

    def state: String = ???

    def kill: Unit = ???
  }

  class YarnExecutorHook(appTask: YarnAppTask) extends ExecutorHook {
    override def kill(): Boolean = ???

    override def isKilled: Boolean = ???
  }
}
