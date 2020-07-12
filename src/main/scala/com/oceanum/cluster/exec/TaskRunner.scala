package com.oceanum.cluster.exec

import com.oceanum.common.Log

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
trait TaskRunner extends Log {

  def run(task: ExecutionTask[_ <: TaskConfig]): ExitCode

  def close(): Unit

  def executable(task: ExecutionTask[_ <: TaskConfig]): Boolean
}
