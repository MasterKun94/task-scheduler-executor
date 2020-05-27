package com.oceanum.exec.multi

import com.oceanum.exec.OperatorProp

/**
 * @author chenmingkun
 * @date 2020/5/10
 */
case class MultiOperatorProp(props: Array[MultiOperatorProp]) extends OperatorProp {
  override def close(): Unit = props.foreach(_.close())
}
