package com.oceanum.jdbc.expr

import com.googlecode.aviator.runtime.`type`.{AviatorBoolean, AviatorLong, AviatorObject, AviatorString}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction
import com.oceanum.file.HadoopFileSystem

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
@IFunction
class HDFSExistFunction extends AbstractFunction {
  override def getName: String = "hdfs.exist"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(HadoopFileSystem.exist(str))
  }
}

@IFunction
class HDFSIsDirFunction extends AbstractFunction {
  override def getName: String = "hdfs.isDir"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(HadoopFileSystem.isDir(str))
  }
}

@IFunction
class HDFSIsFileFunction extends AbstractFunction {
  override def getName: String = "hdfs.isFile"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(HadoopFileSystem.isFile(str))
  }
}

@IFunction
class HDFSSizeFunction extends AbstractFunction {
  override def getName: String = "hdfs.size"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(HadoopFileSystem.size(str))
  }
}

@IFunction
class HDFSBlockSizeFunction extends AbstractFunction {
  override def getName: String = "hdfs.blockSize"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(HadoopFileSystem.blockSize(str))
  }
}

@IFunction
class HDFSAccessTimeFunction extends AbstractFunction {
  override def getName: String = "hdfs.accessTime"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(HadoopFileSystem.accessTime(str))
  }
}

@IFunction
class HDFSModifiedTimeFunction extends AbstractFunction {
  override def getName: String = "hdfs.modifiedTime"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(HadoopFileSystem.modifiedTime(str))
  }
}

@IFunction
class HDFSOwnerTimeFunction extends AbstractFunction {
  override def getName: String = "hdfs.owner"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    new AviatorString(HadoopFileSystem.owner(str))
  }
}