package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.{AviatorEvaluator, Expression}
import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.AviatorFunction
import com.oceanum.common.{Environment, GraphContext}

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object Evaluator {
  def addFunction(function: AviatorFunction): Unit = AviatorEvaluator.addFunction(function)

  def addOpFunction(operatorType: OperatorType, function: AviatorFunction): Unit = AviatorEvaluator.addOpFunction(operatorType, function)

  def execute(expr: String, env: GraphContext): Any = rawExecute(expr, env.javaExprEnv)

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
    addFunction(new TaskEndTimeFunction)
    addFunction(new TaskTypeFunction)
    addFunction(new TaskExecDirFunction)
    addFunction(new TaskNameFunction)

    addFunction(new GraphIdFunction)
    addFunction(new GraphNameFunction)
    addFunction(new GraphCreateTimeFunction)
    addFunction(new GraphScheduleTimeFunction)
    addFunction(new GraphStartTimeFunction)
    addFunction(new GraphFindTaskFunction)
    addFunction(new GraphLatestTaskStateFunction)

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
}