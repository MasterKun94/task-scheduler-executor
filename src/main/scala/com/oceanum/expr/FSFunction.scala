package com.oceanum.expr

import java.io.File

import com.googlecode.aviator.runtime.`type`.{AviatorBoolean, AviatorLong, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}

/**
 * @author chenmingkun
 * @date 2020/7/17
 */
class FSExistFunction extends AbstractFunction {
  override def getName: String = "fs.exist"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).exists)
  }
}

class FSExistsFunction extends FSExistFunction {
  override def getName: String = "fs.exists"
}

class FSIsDirFunction extends AbstractFunction {
  override def getName: String = "fs.isDir"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).isDirectory)
  }
}

class FSIsFileFunction extends AbstractFunction {
  override def getName: String = "fs.isFile"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).isFile)
  }
}

class FSCanExecuteFunction extends AbstractFunction {
  override def getName: String = "fs.canExecute"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).canExecute)
  }
}

class FSCanReadFunction extends AbstractFunction {
  override def getName: String = "fs.canRead"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).canRead)
  }
}

class FSCanWriteFunction extends AbstractFunction {
  override def getName: String = "fs.canWrite"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).canWrite)
  }
}

class FSAbsoluteFunction extends AbstractFunction {
  override def getName: String = "fs.absolute"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    new AviatorString(new File(str).getAbsolutePath)
  }
}

class FSNameFunction extends AbstractFunction {
  override def getName: String = "fs.name"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    new AviatorString(new File(str).getName)
  }
}

class FSParentFunction extends AbstractFunction {
  override def getName: String = "fs.parent"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    new AviatorString(new File(str).getParent)
  }
}

class FSListFunction extends AbstractFunction {
  override def getName: String = "fs.list"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorRuntimeJavaType.valueOf(new File(str).list)
  }
}

class FSListFilePathsFunction extends AbstractFunction {
  override def getName: String = "fs.listFiles"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorRuntimeJavaType.valueOf(new File(str).listFiles().map(_.getAbsolutePath))
  }
}


class FSSizePathsFunction extends AbstractFunction {
  override def getName: String = "fs.size"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(new File(str).getTotalSpace)
  }
}