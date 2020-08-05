package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

/**
 * @author chenmingkun
 * @date 2020/8/5
 */
@ISerializationMessage("BOOL")
case class BoolValue(value: Boolean) {

}
