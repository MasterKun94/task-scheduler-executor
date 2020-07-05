package com.oceanum.cluster.exec

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
trait TypedRunner[T <: OperatorTask] extends TaskRunner {

    def run(operatorProp: Operator[_ <: OperatorTask]): ExitCode = {
      if (executable(operatorProp.prop)) {
        operatorProp.eventListener.start()
        typedRun(operatorProp.asInstanceOf[Operator[T]])
      } else {
        ExitCode.UN_SUPPORT(operatorProp.prop.getClass)
      }
    }

    protected def typedRun(operatorProp: Operator[_ <: T]): ExitCode

    def executable(p: OperatorTask): Boolean

}
