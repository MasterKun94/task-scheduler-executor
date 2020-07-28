package com.oceanum.exec

import com.oceanum.common.{RichTaskMeta, TaskMeta}
import com.oceanum.pluggable

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
@SerialVersionUID(1L)
class State(val name: State.value, meta: RichTaskMeta) extends Serializable {
  def metadata: TaskMeta = meta.copy(state = name)
}

object State extends Enumeration {
  type value = Value
  val OFFLINE: value = Value(0)
  val PREPARE: value = Value(1)
  val START: value = Value(2)
  val RUNNING: value = Value(3)
  val FAILED: value = Value(4)
  val SUCCESS: value = Value(5)
  val RETRY: value = Value(6)
  val TIMEOUT: value = Value(7)
  val KILL: value = Value(8)

}

@SerialVersionUID(1L)
case class OFFLINE(meta: RichTaskMeta) extends State(State.OFFLINE, meta)
@SerialVersionUID(1L)
case class PREPARE(meta: RichTaskMeta) extends State(State.PREPARE, meta)
@SerialVersionUID(1L)
case class START(meta: RichTaskMeta) extends State(State.START, meta)
@SerialVersionUID(1L)
case class RUNNING(meta: RichTaskMeta) extends State(State.RUNNING, meta)
@SerialVersionUID(1L)
case class FAILED(meta: RichTaskMeta) extends State(State.FAILED, meta)
@SerialVersionUID(1L)
case class SUCCESS(meta: RichTaskMeta) extends State(State.SUCCESS, meta)
@SerialVersionUID(1L)
case class RETRY(meta: RichTaskMeta) extends State(State.RETRY, meta)
@SerialVersionUID(1L)
case class TIMEOUT(meta: RichTaskMeta) extends State(State.TIMEOUT, meta)
@SerialVersionUID(1L)
case class KILL(meta: RichTaskMeta) extends State(State.KILL, meta)