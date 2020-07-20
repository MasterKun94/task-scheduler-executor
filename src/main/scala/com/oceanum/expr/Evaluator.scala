package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.{AviatorEvaluator, Expression}
import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.AviatorFunction
import com.oceanum.common.{Environment, StringParser}
import com.oceanum.common.Implicits.EnvHelper

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object Evaluator {
  def addFunction(function: AviatorFunction): Unit = AviatorEvaluator.addFunction(function)

  def addOpFunction(operatorType: OperatorType, function: AviatorFunction): Unit = AviatorEvaluator.addOpFunction(operatorType, function)

  def execute(expr: String, env: Map[String, Any]): Any = rawExecute(expr, env.toJava)

  def rawExecute(expr: String, env: JavaMap[String, AnyRef]): Any = {
    if (env.isEmpty) AviatorEvaluator.execute(expr)
    else AviatorEvaluator.execute(expr, env, Environment.AVIATOR_CACHE_ENABLED)
  }

  def compile(expr: String, cache: Boolean = Environment.AVIATOR_CACHE_ENABLED): Expression = {
    AviatorEvaluator.compile(expr, cache)
  }

  def init(): Unit = {
    if (Environment.AVIATOR_CACHE_ENABLED) {
      AviatorEvaluator.getInstance().useLRUExpressionCache(Environment.AVIATOR_CACHE_CAPACITY)
    }
    addFunction(new DurationFunction)
    addFunction(new DurationMillisFunction)
    addFunction(new DurationMilliFunction)
    addFunction(new DurationSecondsFunction)
    addFunction(new DurationSecondFunction)
    addFunction(new DurationMinutesFunction)
    addFunction(new DurationMinuteFunction)
    addFunction(new DurationHoursFunction)
    addFunction(new DurationHourFunction)
    addFunction(new DurationDaysFunction)
    addFunction(new DurationDayFunction)

    addFunction(new DateFunction)
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
    addFunction(new FSSizeFunction)
    addFunction(new FSModifiedTimeFunction)

    addFunction(new HDFSExistFunction)
    addFunction(new HDFSIsDirFunction)
    addFunction(new HDFSIsFileFunction)
    addFunction(new HDFSSizeFunction)
    addFunction(new HDFSBlockSizeFunction)
    addFunction(new HDFSModifiedTimeFunction)
    addFunction(new HDFSAccessTimeFunction)

    addFunction(new TaskIdFunction)
    addFunction(new TaskUserFunction)
    addFunction(new TaskCreateTimeFunction)
    addFunction(new TaskStartTimeFunction)
    addFunction(new TaskTypeFunction)
    addFunction(new TaskExecDirFunction)

    addFunction(new GraphIdFunction)
    addFunction(new GraphNameFunction)
    addFunction(new GraphCreateTimeFunction)
    addFunction(new GraphScheduleTimeFunction)
    addFunction(new GraphStartTimeFunction)

    addFunction(new NodeHostFunction)
    addFunction(new NodePortFunction)
    addFunction(new NodeTopicsFunction)
    addFunction(new NodeBaseDirFunction)
    addFunction(new NodeWorkDirFunction)
    addFunction(new NodeLogDirFunction)
    addFunction(new NodeEnvFunction)

    addOpFunction(OperatorType.ADD, new OpAddFunction)
    addOpFunction(OperatorType.SUB, new OpSubFunction)
    addOpFunction(OperatorType.NEG, new OpNegFunction)
    addOpFunction(OperatorType.DIV, new OpDivFunction)
    addOpFunction(OperatorType.MULT, new OpMulFunction)
//    addOpFunction(OperatorType.GT, new OpGtFunction)
//    addOpFunction(OperatorType.GE, new OpGeFunction)
//    addOpFunction(OperatorType.LT, new OpLtFunction)
//    addOpFunction(OperatorType.LE, new OpLeFunction)
//    addOpFunction(OperatorType.EQ, new OpEqFunction)
//    addOpFunction(OperatorType.NEQ, new OpNEqFunction)
  }

  def main(args: Array[String]): Unit = {
    init()
    implicit val env: Map[String, Any] = Map.empty
    println(StringParser.parseExpr("/test/${date.format('yyyy-MM-dd')}/file.txt"))
    println(StringParser.parseExpr("${duration.day(1) == duration.hour(1) * 24}"))
    println(Evaluator.execute("duration.day(1) == duration.hour(1) * 24", env))
    println(Evaluator.execute("date.now()", env))
    println(Evaluator.execute("date.now() - duration('1 day')", env))
    println(Evaluator.execute("duration('1 day') / 24", env))
    println(Evaluator.execute("fs.exists('C:/Users/chenmingkun/work/idea/work/task-scheduler-core/task-scheduler-executor/src/main/scala/com/oceanum/file/ClusterFileServerApi.scala')", env))
    println(Evaluator.execute("fs.size('C:/Users/chenmingkun/work/idea/work/task-scheduler-core/task-scheduler-executor/src/main/scala/com/oceanum/file/ClusterFileServerApi.scala')", env))
  }

}
class Test(val name: String) {
  def andThen(test: Test): Test = new Test(name + ":" + test.name)
}