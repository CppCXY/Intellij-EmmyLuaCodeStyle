package com.cppcxy.intellij.lua.editor.annotator

import kotlinx.serialization.Serializable

@Serializable
data class LintPosition(val line: Int, val character: Int)

@Serializable
data class LintRange(val start: LintPosition, val end: LintPosition)

@Serializable
data class LintData(val range: LintRange, val message: String)
