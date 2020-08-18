package com.oceanum.expr

import java.io.File

import com.googlecode.aviator.runtime.`type`.{AviatorBoolean, AviatorLong, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction

/**
 * @author chenmingkun
 * @date 2020/7/17
 */
@IFunction
class FSExistFunction extends AbstractFunction {
  override def getName: String = "fs.exist"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).exists)
  }
}

@IFunction
class FSExistsFunction extends FSExistFunction {
  override def getName: String = "fs.exists"
}

@IFunction
class FSIsDirFunction extends AbstractFunction {
  override def getName: String = "fs.isDir"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).isDirectory)
  }
}

@IFunction
class FSIsFileFunction extends AbstractFunction {
  override def getName: String = "fs.isFile"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).isFile)
  }
}

@IFunction
class FSCanExecuteFunction extends AbstractFunction {
  override def getName: String = "fs.canExecute"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).canExecute)
  }
}

@IFunction
class FSCanReadFunction extends AbstractFunction {
  override def getName: String = "fs.canRead"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).canRead)
  }
}

@IFunction
class FSCanWriteFunction extends AbstractFunction {
  override def getName: String = "fs.canWrite"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorBoolean.valueOf(new File(str).canWrite)
  }
}

@IFunction
class FSAbsoluteFunction extends AbstractFunction {
  override def getName: String = "fs.absolute"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    new AviatorString(new File(str).getAbsolutePath)
  }
}

@IFunction
class FSNameFunction extends AbstractFunction {
  override def getName: String = "fs.name"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    new AviatorString(new File(str).getName)
  }
}

@IFunction
class FSParentFunction extends AbstractFunction {
  override def getName: String = "fs.parent"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    new AviatorString(new File(str).getParent)
  }
}

@IFunction
class FSListFunction extends AbstractFunction {
  override def getName: String = "fs.list"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorRuntimeJavaType.valueOf(new File(str).list)
  }
}

@IFunction
class FSListFilePathsFunction extends AbstractFunction {
  override def getName: String = "fs.listFiles"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorRuntimeJavaType.valueOf(new File(str).listFiles().map(_.getAbsolutePath))
  }
}

@IFunction
class FSSizeFunction extends AbstractFunction {
  override def getName: String = "fs.size"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(new File(str).length())
  }
}

@IFunction
class FSModifiedTimeFunction extends AbstractFunction {
  override def getName: String = "fs.modifiedTime"

  override def call(env: JavaMap[String, AnyRef], path: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(path, env)
    AviatorLong.valueOf(new File(str).lastModified())
  }
}