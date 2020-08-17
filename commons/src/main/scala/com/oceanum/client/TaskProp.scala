package com.oceanum.client

import com.oceanum.jdbc.expr.ValidateParser

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

abstract class PluggableTaskProp
extends TaskProp("PLUGGABLE") {
  override def validate(): Unit = {
    _args.foreach(ValidateParser.parse)
    _files.foreach(ValidateParser.parse)
    _jars.foreach(ValidateParser.parse)
    _options.foreach(ValidateParser.parse)
    _env.mapValues(ValidateParser.parse)
    ValidateParser.parse(_plugClass)
  }

  def _args: Array[String] = Array.empty
  def _plugClass: String
  def _files: Array[String] = Array.empty
  def _jars: Array[String] = Array.empty
  def _options: Array[String] = Array.empty
  def _env: Map[String, String] = Map.empty
  def _directory: String = ""
  def _waitForTimeout: String = "24h"
}

case class SparkArgs(appName: Option[String],
                     appResource: String,
                     mainClass: String,
                     appArgs: Array[String] = Array.empty,
                     master: String = "local[2]",
                     deployMode: Option[String] = None,
                     conf: Map[String, String] = Map.empty,
                     jars: Array[String] = Array.empty,
                     files: Array[String] = Array.empty,
                     pyFiles: Array[String] = Array.empty,
                     propertiesFile: Option[String] = None,
                     options: Array[String] = Array.empty,
                     env: Map[String, String] = Map.empty,
                     directory: String = "",
                     waitForTimeout: String = "24h") extends PluggableTaskProp {
  override def _args: Array[String] = ???
  override def _plugClass: String = ???
  override def _files: Array[String] = ???
  override def _jars: Array[String] = ???
  override def _options: Array[String] = options
  override def _env: Map[String, String] = env
  override def _directory: String = directory
  override def _waitForTimeout: String = waitForTimeout
}