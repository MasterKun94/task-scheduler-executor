package com.oceanum.expr

import com.googlecode.aviator.AviatorEvaluator
import com.oceanum.annotation.{IConfiguration, Init}
import com.oceanum.common.Environment

@IConfiguration
class ExprInitializer {

  @Init
  def init(): Unit = {
    if (Environment.AVIATOR_CACHE_ENABLED) {
      AviatorEvaluator.getInstance().useLRUExpressionCache(Environment.AVIATOR_CACHE_CAPACITY)
    }
  }
}
