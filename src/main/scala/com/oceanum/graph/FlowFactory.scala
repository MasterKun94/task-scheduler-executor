package com.oceanum.graph

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.oceanum.client.{Metadata, Task}

class FlowFactory {
  def create(task: Task): Flow[Metadata, Metadata, NotUsed] = ???
}
