package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

@ISerializationMessage("ELEMENTS")
case class Elements[T](elements: Seq[T]) {
  def size: Int = elements.size
}
