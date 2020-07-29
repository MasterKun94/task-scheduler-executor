package com.oceanum.common

import java.util.Properties

object PropertyParser extends StringParser[Properties] {
  override protected def replace(regex: String)(implicit env: Properties): String = {
    val regValue = parse(env.getProperty(regex, System.getenv(regex)))
    env.setProperty(regex, regValue)
    if (regValue == null || regValue.trim.isEmpty) {
      throw new RuntimeException("需要在配置文件或环境变量中设置变量：" + regValue)
    }
    regValue
  }
}
