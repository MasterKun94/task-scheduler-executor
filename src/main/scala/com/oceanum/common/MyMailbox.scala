package com.oceanum.common

/**
 * @author chenmingkun
 * @date 2020/6/29
 */
class MyMailbox {

}

object MyMailbox {
  private lazy val innerMailbox = akka.dispatch.SingleConsumerOnlyUnboundedMailbox
}
