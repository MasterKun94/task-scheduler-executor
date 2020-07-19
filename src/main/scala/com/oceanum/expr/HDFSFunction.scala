package com.oceanum.expr

import com.googlecode.aviator.runtime.`type`.{AviatorBoolean, AviatorLong, AviatorObject, AviatorString}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.file.HDFSFileClient

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
class HDFSExistFunction extends AbstractFunction {
  override def getName: String = "hdfs.exist"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(HDFSFileClient.exist(str))
  }
}

class HDFSIsDirFunction extends AbstractFunction {
  override def getName: String = "hdfs.isDir"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(HDFSFileClient.isDir(str))
  }
}

class HDFSIsFileFunction extends AbstractFunction {
  override def getName: String = "hdfs.isFile"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(HDFSFileClient.isFile(str))
  }
}

class HDFSSizeFunction extends AbstractFunction {
  override def getName: String = "hdfs.size"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(HDFSFileClient.size(str))
  }
}

class HDFSAccessTimeFunction extends AbstractFunction {
  override def getName: String = "hdfs.accessTime"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(HDFSFileClient.accessTime(str))
  }
}

class HDFSModifiedTimeFunction extends AbstractFunction {
  override def getName: String = "hdfs.modifiedTime"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(HDFSFileClient.modifiedTime(str))
  }
}

class HDFSOwnerTimeFunction extends AbstractFunction {
  override def getName: String = "hdfs.owner"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    new AviatorString(HDFSFileClient.owner(str))
  }
}