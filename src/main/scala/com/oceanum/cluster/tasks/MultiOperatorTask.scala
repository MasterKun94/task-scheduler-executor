package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.OperatorTask

/**
 * @author chenmingkun
 * @date 2020/5/10
 */
case class MultiOperatorTask(props: Array[MultiOperatorTask]) extends OperatorTask {
  override def close(): Unit = props.foreach(_.close())
}
