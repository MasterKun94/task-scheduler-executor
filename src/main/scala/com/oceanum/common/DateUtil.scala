package com.oceanum.common

import java.text.SimpleDateFormat

/**
 * @author chenmingkun
 * @date 2020/7/19
 */
object DateUtil {
  def format(str: String): SimpleDateFormat = {
    val format = new SimpleDateFormat(str, Environment.LOCALE)
    format.setTimeZone(Environment.TIME_ZONE)
    format
  }
}
