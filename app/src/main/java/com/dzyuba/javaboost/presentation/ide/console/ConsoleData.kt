package com.dzyuba.javaboost.presentation.ide.console

import com.dzyuba.javaboost.domain.entities.lesson.Practice
import java.io.Serializable

data class ConsoleData(
    val filePath: String,
    val className: String?,
    val code: String,
    val practice: Practice
):Serializable