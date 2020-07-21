package com.oceanum.exec

import com.oceanum.common.Log

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
abstract class TaskRunner(tasks: String*) extends Log {
  private val taskSet: Set[String] = tasks.toSet

  def run(task: ExecutionTask[_ <: TaskConfig]): ExitCode

  def close(): Unit

  def executable(task: ExecutionTask[_ <: TaskConfig]): Boolean = taskSet.contains(task.metadata.taskType)
}
