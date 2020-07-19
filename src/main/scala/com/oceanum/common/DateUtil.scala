package com.oceanum.common

import java.text.SimpleDateFormat

/**
 * @author chenmingkun
 * @date 2020/7/19
 */
object DateUtil {
  def format(format: String): SimpleDateFormat = new SimpleDateFormat(format, Environment.LOCALE)
}
