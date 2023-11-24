package com.cppcxy.intellij.lua.editor.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.cppcxy.intellij.lua.adaptor.CodeFormat
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class EmmyLuaCodeStyleAnnotator : ExternalAnnotator<Editor, List<LintData>>() {
    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): Editor? {
        return editor
    }

    override fun doAnnotate(editor: Editor?): List<LintData>? {
        var result: List<LintData>? = null
        if (editor != null) {
            val text = editor.document.text
            val ret = CodeFormat.check(null, text)
            if (ret.first) {
                // 将json字符串转化为LintData
                val json = ret.second
                if (json.isNotEmpty()) {
                    result = Json.decodeFromString<List<LintData>>(json)
                }
            }
        }

        return result
    }

    override fun apply(file: PsiFile, annotationResult: List<LintData>?, holder: AnnotationHolder) {
        if (annotationResult != null) {
            for (lintData in annotationResult) {
                val range = lintData.range
                val start = range.start
                val end = range.end
                var startOffset = file.viewProvider.document?.getLineStartOffset(start.line)?.plus(start.character)
                var endOffset = file.viewProvider.document?.getLineStartOffset(end.line)?.plus(end.character + 1)
                val length = file.viewProvider.document?.textLength

                if (startOffset != null && endOffset != null && length != null) {
                    if (endOffset >= length) {
                        endOffset = length - 1
                    }
                    if (startOffset >= endOffset) {
                        startOffset = endOffset - 1
                    }
                    if (startOffset < 0) {
                        continue
                    }

                    val textRange = TextRange(startOffset, endOffset)
                    holder.newAnnotation(HighlightSeverity.WARNING, lintData.message)
                        .range(textRange)
                        .create()
                }
            }
        }
    }
}