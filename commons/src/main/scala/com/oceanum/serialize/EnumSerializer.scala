package com.oceanum.serialize

import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

class EnumSerializer[E <: Enum[E]](implicit ct: Manifest[E]) extends CustomSerializer[E](_ => (
  {
    case JString(name) ⇒ Enum.valueOf(ct.runtimeClass.asInstanceOf[Class[E]], name)
  },
  {
    case dt: E ⇒ JString(dt.name())
  }
))