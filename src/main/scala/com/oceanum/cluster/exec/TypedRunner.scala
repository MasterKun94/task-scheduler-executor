package com.oceanum.cluster.exec

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
abstract class TypedRunner[T <: OperatorTask](types: String*) extends TaskRunner {
  private val typeSet = types.toSet

    def run(operatorProp: Operator[_ <: OperatorTask]): ExitCode = {
      if (executable(operatorProp)) {
        typedRun(operatorProp.asInstanceOf[Operator[T]])
      } else {
        ExitCode.UN_SUPPORT(operatorProp.metadata.taskType)
      }
    }

    protected def typedRun(operatorProp: Operator[_ <: T]): ExitCode

  override def executable(operator: Operator[_ <: OperatorTask]): Boolean = typeSet.contains(operator.metadata.taskType)
}
