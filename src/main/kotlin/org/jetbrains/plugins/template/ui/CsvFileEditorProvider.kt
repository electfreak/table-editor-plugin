package org.jetbrains.plugins.template.ui

import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileEditor.AsyncFileEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object CsvLanguage : Language("CSV", "application/csv")

object CsvFileType : LanguageFileType(CsvLanguage) {
    override fun getIcon() = AllIcons.FileTypes.Text
    override fun getName() = "CSV"
    override fun getDefaultExtension() = "csv"
    override fun getDescription() = "CSV"
}


class CsvFileEditorProvider : AsyncFileEditorProvider, DumbAware {

    init {
        println("init provider")
    }

    override fun getEditorTypeId() = "CSV"

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileType == CsvFileType
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return createEditorAsync(project, file).build()
    }

    override fun getPolicy() = FileEditorPolicy.HIDE_DEFAULT_EDITOR

    override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder {
        return object : AsyncFileEditorProvider.Builder() {
            override fun build(): FileEditor = CsvFileEditor(project, file)
        }
    }
}
