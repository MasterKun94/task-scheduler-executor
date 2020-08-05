package com.oceanum.common

import com.oceanum.annotation.{IConfiguration, Init}
import com.oceanum.persistence.es.EsUtil

/**
 * @author chenmingkun
 * @date 2020/8/4
 */
@IConfiguration
class EsEnvironment {
  @Init
  def init(): Unit = {
    EsUtil.createIndex("graph-meta")
  }

}

object EsEnvironment {
  def init(): Unit = {
    EsUtil.createIndex("graph-meta")
  }


}
