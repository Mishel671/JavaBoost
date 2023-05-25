package com.dzyuba.javaboost.domain.entities.lesson


enum class Type(val value: String)  {
    HEADER("header"),
    TEXT("text"),
    IMAGE("image"),
    CODE("code"),
    DIVIDER("divider"),
    TEST("test"),
    PRACTICE("practice");

    companion object {
        fun String.toType(): Type? {
            for (type in Type.values()) {
                if (type.value.equals(this, ignoreCase = true)) {
                    return type
                }
            }
            return null
        }
    }
}