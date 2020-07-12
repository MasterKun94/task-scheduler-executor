package com.oceanum.cluster.exec

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
abstract class TypedRunner[T <: TaskConfig](types: String*) extends TaskRunner {
  private val typeSet = types.toSet

    def run(task: ExecutionTask[_ <: TaskConfig]): ExitCode = {
      if (executable(task)) {
        typedRun(task.asInstanceOf[ExecutionTask[T]])
      } else {
        ExitCode.UN_SUPPORT(task.metadata.taskType)
      }
    }

    protected def typedRun(task: ExecutionTask[_ <: T]): ExitCode

  override def executable(task: ExecutionTask[_ <: TaskConfig]): Boolean = typeSet.contains(task.metadata.taskType)
}
