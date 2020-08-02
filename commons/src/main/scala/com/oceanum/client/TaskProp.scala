package com.oceanum.client

import com.oceanum.expr.ValidateParser

@SerialVersionUID(1L)
abstract class TaskProp(val taskType: String) extends Serializable {

  def validate(): Unit
}

@SerialVersionUID(1L)
abstract class ProcessTaskProp(taskType: String) extends TaskProp(taskType) with Serializable {}

@SerialVersionUID(1L)
case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: String = "24h") extends ProcessTaskProp("SHELL") {

  override def validate(): Unit = {
    cmd.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
  }
}

@SerialVersionUID(1L)
case class ShellScriptTaskProp(scriptFile: String = "",
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = "",
                               waitForTimeout: String = "24h") extends ProcessTaskProp("SHELL_SCRIPT") {

  override def validate(): Unit = {
    args.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
    ValidateParser.parse(scriptFile)
  }
}

@SerialVersionUID(1L)
case class JavaTaskProp(jars: Array[String] = Array.empty,
                        mainClass: String = "",
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: String = "",
                        waitForTimeout: String = "24h") extends ProcessTaskProp("JAVA") {

  override def validate(): Unit = {
    options.foreach(ValidateParser.parse)
    args.foreach(ValidateParser.parse)
    jars.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
    ValidateParser.parse(mainClass)
  }
}

@SerialVersionUID(1L)
case class ScalaTaskProp(jars: Array[String] = Array.empty,
                         mainClass: String = "",
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: String = "24h") extends ProcessTaskProp("SCALA") {

  override def validate(): Unit = {
    options.foreach(ValidateParser.parse)
    args.foreach(ValidateParser.parse)
    jars.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
    ValidateParser.parse(mainClass)
  }
}

@SerialVersionUID(1L)
case class PythonTaskProp(pyFile: String = "",
                          args: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = "",
                          waitForTimeout: String = "24h") extends ProcessTaskProp("PYTHON") {

  override def validate(): Unit = {
    args.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
    ValidateParser.parse(pyFile)
  }
}

@SerialVersionUID(1L)
case class UserAdd(user: String) extends ProcessTaskProp("USER_ADD") {

  override def validate(): Unit = ValidateParser.parse(user)
}