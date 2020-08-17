package com.oceanum.jdbc.expr

import com.googlecode.aviator.runtime.`type`.{AviatorLong, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/7/19
 */
@IFunction
class NodeHostFunction extends AbstractFunction {
  override def getName: String = "node.host"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = new AviatorString(Environment.HOST)
}

@IFunction
class NodePortFunction extends AbstractFunction {
  override def getName: String = "node.port"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = AviatorLong.valueOf(Environment.CLUSTER_NODE_PORT)
}

@IFunction
class NodeTopicsFunction extends AbstractFunction {
  override def getName: String = "node.topics"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = AviatorRuntimeJavaType.valueOf(Environment.CLUSTER_NODE_TOPICS.toArray)
}

@IFunction
class NodeBaseDirFunction extends AbstractFunction {
  override def getName: String = "node.baseDir"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = new AviatorString(Environment.BASE_PATH)
}

@IFunction
class NodeWorkDirFunction extends AbstractFunction {
  override def getName: String = "node.workDir"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = new AviatorString(Environment.EXEC_WORK_DIR)
}

@IFunction
class NodeLogDirFunction extends AbstractFunction {
  override def getName: String = "node.logDir"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = new AviatorString(Environment.LOG_FILE_DIR)
}

@IFunction
class NodeEnvFunction extends AbstractFunction {
  override def getName: String = "node.getEnv"

  override def call(env: JavaMap[String, AnyRef], key: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(key, env)
    new AviatorString(Environment.getProperty(str))
  }

  override def call(env: JavaMap[String, AnyRef], key: AviatorObject, orElse: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(key, env)
    val els = FunctionUtils.getStringValue(orElse, env)
    new AviatorString(Environment.getProperty(str, els))
  }
}