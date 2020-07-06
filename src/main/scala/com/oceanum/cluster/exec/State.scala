package com.oceanum.cluster.exec

import com.oceanum.client.Metadata

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
@SerialVersionUID(1L)
class State(metadata: Metadata) extends Serializable {
  def getMetadata: Metadata = metadata
}
object State {
  case class OFFLINE(metadata: Metadata) extends State(metadata)
  case class PREPARE(metadata: Metadata) extends State(metadata)
  case class START(metadata: Metadata) extends State(metadata)
  case class RUNNING(metadata: Metadata) extends State(metadata)
  case class FAILED(metadata: Metadata) extends State(metadata)
  case class SUCCESS(metadata: Metadata) extends State(metadata)
  case class RETRY(metadata: Metadata) extends State(metadata)
  case class TIMEOUT(metadata: Metadata) extends State(metadata)
  case class KILL(metadata: Metadata) extends State(metadata)
}
