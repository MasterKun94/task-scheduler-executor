package com.oceanum.exec

import com.oceanum.common.{RichTaskMeta, TaskStatus}

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
@SerialVersionUID(1L)
class State(val name: TaskStatus.value, meta: RichTaskMeta) extends Serializable {
  def metadata: RichTaskMeta = meta.copy(state = name)
}

@SerialVersionUID(1L)
case class OFFLINE(meta: RichTaskMeta) extends State(TaskStatus.OFFLINE, meta)
@SerialVersionUID(1L)
case class PREPARE(meta: RichTaskMeta) extends State(TaskStatus.PREPARE, meta)
@SerialVersionUID(1L)
case class START(meta: RichTaskMeta) extends State(TaskStatus.START, meta)
@SerialVersionUID(1L)
case class RUNNING(meta: RichTaskMeta) extends State(TaskStatus.RUNNING, meta)
@SerialVersionUID(1L)
case class FAILED(meta: RichTaskMeta) extends State(TaskStatus.FAILED, meta)
@SerialVersionUID(1L)
case class SUCCESS(meta: RichTaskMeta) extends State(TaskStatus.SUCCESS, meta)
@SerialVersionUID(1L)
case class RETRY(meta: RichTaskMeta) extends State(TaskStatus.RETRY, meta)
@SerialVersionUID(1L)
case class TIMEOUT(meta: RichTaskMeta) extends State(TaskStatus.TIMEOUT, meta)
@SerialVersionUID(1L)
case class KILL(meta: RichTaskMeta) extends State(TaskStatus.KILL, meta)