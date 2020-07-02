package com.oceanum.cluster.exec

import com.oceanum.client.Metadata

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
class State {}
object State {
  case class OFFLINE(metaData: Metadata) extends State
  case class PREPARE(metaData: Metadata) extends State
  case class START(metaData: Metadata) extends State
  case class RUNNING(metaData: Metadata) extends State
  case class FAILED(metaData: Metadata) extends State
  case class SUCCESS(metaData: Metadata) extends State
  case class RETRY(metaData: Metadata) extends State
  case class TIMEOUT(metaData: Metadata) extends State
  case class KILL(metaData: Metadata) extends State
}
