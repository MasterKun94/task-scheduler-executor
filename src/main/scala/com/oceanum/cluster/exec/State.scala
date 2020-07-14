package com.oceanum.cluster.exec

import com.oceanum.client.TaskMeta

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
@SerialVersionUID(1L)
class State(val name: State.value, meta: TaskMeta) extends Serializable {
  def metadata: TaskMeta = meta.state = name
}

object State extends Enumeration {
  type value = Value
  val OFFLINE, PREPARE, START, RUNNING, FAILED, SUCCESS, RETRY, TIMEOUT, KILL = Value

}

@SerialVersionUID(1L)
case class OFFLINE(meta: TaskMeta) extends State(State.OFFLINE, meta)
@SerialVersionUID(1L)
case class PREPARE(meta: TaskMeta) extends State(State.PREPARE, meta)
@SerialVersionUID(1L)
case class START(meta: TaskMeta) extends State(State.START, meta)
@SerialVersionUID(1L)
case class RUNNING(meta: TaskMeta) extends State(State.RUNNING, meta)
@SerialVersionUID(1L)
case class FAILED(meta: TaskMeta) extends State(State.FAILED, meta)
@SerialVersionUID(1L)
case class SUCCESS(meta: TaskMeta) extends State(State.SUCCESS, meta)
@SerialVersionUID(1L)
case class RETRY(meta: TaskMeta) extends State(State.RETRY, meta)
@SerialVersionUID(1L)
case class TIMEOUT(meta: TaskMeta) extends State(State.TIMEOUT, meta)
@SerialVersionUID(1L)
case class KILL(meta: TaskMeta) extends State(State.KILL, meta)