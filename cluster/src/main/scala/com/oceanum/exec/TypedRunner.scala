package com.oceanum.exec

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
abstract class TypedRunner[T <: TaskConfig](types: String*) extends TaskRunner(types:_*) {

  def run(task: ExecutionTask[_ <: TaskConfig]): ExitCode = {
    if (executable(task)) {
      try {
        task.eventListener.running(task.metadata)
        typedRun(task.asInstanceOf[ExecutionTask[T]])
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          ExitCode.ERROR(e)
      }
    } else {
      ExitCode.UN_SUPPORT(task.metadata.taskType)
    }
  }

  protected def typedRun(task: ExecutionTask[_ <: T]): ExitCode

  protected def sendRunEvent(task: ExecutionTask[_ <: T], hook: ExecutionHook): Unit = {
    task.receive(hook)
    task.eventListener.running(task.metadata)
  }

  protected def kill(task: ExecutionTask[_ <: T]): Boolean = task.hook.kill()

  protected def isKilled(task: ExecutionTask[_ <: T]): Boolean = task.hook.isKilled
}
