package com.oceanum.cluster.exec

import com.oceanum.client.TaskMeta

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
@SerialVersionUID(1L)
class State(val name: String) extends Serializable
object State {
  @SerialVersionUID(1L)
  case class OFFLINE(metadata: TaskMeta) extends State("OFFLINE")
  @SerialVersionUID(1L)
  case class PREPARE(metadata: TaskMeta) extends State("PREPARE")
  @SerialVersionUID(1L)
  case class START(metadata: TaskMeta) extends State("START")
  @SerialVersionUID(1L)
  case class RUNNING(metadata: TaskMeta) extends State("RUNNING")
  @SerialVersionUID(1L)
  case class FAILED(metadata: TaskMeta) extends State("FAILED")
  @SerialVersionUID(1L)
  case class SUCCESS(metadata: TaskMeta) extends State("SUCCESS")
  @SerialVersionUID(1L)
  case class RETRY(metadata: TaskMeta) extends State("RETRY")
  @SerialVersionUID(1L)
  case class TIMEOUT(metadata: TaskMeta) extends State("TIMEOUT")
  @SerialVersionUID(1L)
  case class KILL(metadata: TaskMeta) extends State("KILL")
}
