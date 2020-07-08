package com.oceanum.cluster.exec

import com.oceanum.client.Metadata

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
@SerialVersionUID(1L)
class State extends Serializable
object State {
  @SerialVersionUID(1L)
  case class OFFLINE(metadata: Metadata) extends State
  @SerialVersionUID(1L)
  case class PREPARE(metadata: Metadata) extends State
  @SerialVersionUID(1L)
  case class START(metadata: Metadata) extends State
  @SerialVersionUID(1L)
  case class RUNNING(metadata: Metadata) extends State
  @SerialVersionUID(1L)
  case class FAILED(metadata: Metadata) extends State
  @SerialVersionUID(1L)
  case class SUCCESS(metadata: Metadata) extends State
  @SerialVersionUID(1L)
  case class RETRY(metadata: Metadata) extends State
  @SerialVersionUID(1L)
  case class TIMEOUT(metadata: Metadata) extends State
  @SerialVersionUID(1L)
  case class KILL(metadata: Metadata) extends State
}
