package com.oceanum.trigger

import java.util.Date

import com.oceanum.annotation.ITrigger
import com.oceanum.common.CoordStatus

import scala.concurrent.Future

//@ITrigger
class HDFSTrigger extends Trigger {
  private val listener = new HDFSEventListener()
  import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT
  listener.start()

  override def start(name: String, config: Map[String, String], startTime: Option[Date])(action: (Date, Map[String, Any]) => Unit): Future[Unit] = {
    Future {
      val regex = config("regex").r
      listener.add(name, regex, startTime) { event =>
        val time = new Date(event.getCtime)
        val env = Map("path" -> event.getPath)
        action(time, env)
      }
    }
  }

  override def recover(name: String, config: Map[String, String], startTime: Option[Date], state: CoordStatus)(action: (Date, Map[String, Any]) => Unit): Future[Unit] = ???

  /**
   * 根据任务名称停止任务
   *
   * @param name 任务名称
   * @return 是否有任务被停止，false表示没有该名称的任务，true反之
   */
  override def stop(name: String): Future[Boolean] = ???

  /**
   * 根据任务名称暂停任务
   *
   * @param name 任务名称
   * @return 是否有任务被暂停，false表示没有该名称的任务，true反之
   */
  override def suspend(name: String): Future[Boolean] = ???

  /**
   * 根据任务名称继续任务
   *
   * @param name 任务名称
   * @return 是否有任务被继续，false表示没有该名称的任务，true反之
   */
  override def resume(name: String): Future[Boolean] = ???

  /**
   * 触发器的类型名称，自定义的不同触发器类应该有不同的triggerType，系统会根据此来找到对应的触发器
   * 来启动，停止，暂停，继续任务。
   *
   * @return 触发器类型名称
   */
  override def triggerType: String = ???
}
