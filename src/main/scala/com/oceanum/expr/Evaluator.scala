package com.oceanum.expr

import com.googlecode.aviator.AviatorEvaluator
import com.googlecode.aviator.runtime.`type`.AviatorFunction
import com.oceanum.common.StringParser

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object Evaluator {
  def addFunction(function: AviatorFunction): Unit = {
    AviatorEvaluator.addFunction(function)
  }

  def execute(expr: String, env: Map[String, Any]): Any = {
    init
    val javaEnv = new JavaHashMap[String, AnyRef](env.size)
    for (elem <- env) {
      javaEnv.put(elem._1, elem._2.asInstanceOf[AnyRef])
    }
    AviatorEvaluator.execute(expr, javaEnv)
  }

  private lazy val init: Unit = {
    addFunction(new DurationFunction)
    addFunction(new DurationMillisFunction)
    addFunction(new DurationSecondsFunction)
    addFunction(new DurationMinutesFunction)
    addFunction(new DurationHoursFunction)
    addFunction(new DurationDaysFunction)

    addFunction(new DateFormatFunction)
    addFunction(new DateParseFunction)
    addFunction(new DateShiftFunction)
    addFunction(new DateNowFunction)

    addFunction(new FSExistFunction)
    addFunction(new FSExistsFunction)
    addFunction(new FSIsDirFunction)
    addFunction(new FSIsFileFunction)
    addFunction(new FSCanExecuteFunction)
    addFunction(new FSCanReadFunction)
    addFunction(new FSCanWriteFunction)
    addFunction(new FSAbsoluteFunction)
    addFunction(new FSNameFunction)
    addFunction(new FSParentFunction)
    addFunction(new FSListFunction)
    addFunction(new FSListFilePathsFunction)
    addFunction(new FSSizePathsFunction)

    addFunction(new HDFSExistFunction)
    addFunction(new HDFSIsDirFunction)
    addFunction(new HDFSIsFileFunction)

    addFunction(new TaskIdFunction)
    addFunction(new TaskUserFunction)
    addFunction(new TaskCreateTimeFunction)
    addFunction(new TaskStartTimeFunction)
    addFunction(new TaskTypeFunction)
    addFunction(new TaskExecDirFunction)
  }

  def main(args: Array[String]): Unit = {
    init
    println(Evaluator.execute("duration.days(-1)", Map.empty))
    println(Evaluator.execute("date.format('yyyyMMdd', date.now())", Map.empty))
    println(Evaluator.execute("date.format('yyyyMMdd', '-1 day')", Map.empty))
    println(Evaluator.execute("date.format('yyyyMMdd', date.now(), duration('1 day'))", Map.empty))
    println(Evaluator.execute("fs.listFiles('C:/Users/chenmingkun/work/idea/work/task-scheduler-core/task-scheduler-executor')", Map.empty).asInstanceOf[Array[String]].toSeq)
    println(Evaluator.execute("fs.parent('C:/Users/chenmingkun/work/idea/work/task-scheduler-core/task-scheduler-executor')", Map.empty))
  }
}
