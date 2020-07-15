package com.oceanum.cluster.exec

import com.oceanum.client.{RichTaskMeta, TaskMeta}

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
@SerialVersionUID(1L)
class State(val name: State.value, meta: RichTaskMeta) extends Serializable {
  def metadata: TaskMeta[_] = meta.state = name
}

object State extends Enumeration {
  type value = Value
  val OFFLINE, PREPARE, START, RUNNING, FAILED, SUCCESS, RETRY, TIMEOUT, KILL = Value

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