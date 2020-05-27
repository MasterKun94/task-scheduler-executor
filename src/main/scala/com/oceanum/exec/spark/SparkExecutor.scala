package com.oceanum.exec.spark

import com.oceanum.exec.spark.launcher.SparkLauncher
import com.oceanum.exec.{Executor, ExitCode, Operator, OperatorProp, OutputManager}

/**
 * @author chenmingkun
 * @date 2020/5/10
 */
class SparkExecutor extends Executor[SparkProp] {
  override protected def typedExecute(operatorProp: Operator[_ <: SparkProp]): ExitCode = {
    new SparkLauncher()
    ???
  }

  override def executable(p: OperatorProp): Boolean = {
    p.isInstanceOf[SparkProp]
  }
}
