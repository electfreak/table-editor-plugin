package org.jetbrains.plugins.template.ui

import com.intellij.diff.util.FileEditorBase
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.template.ui.view.CsvEditorViewComponent
import javax.swing.JComponent

class CsvFileEditor(project: Project, private val virtualFile: VirtualFile) : FileEditorBase(), DumbAware {
    private val viewComponent = CsvEditorViewComponent(project, virtualFile)
    override fun getName(): String = NAME

    override fun getFile(): VirtualFile = virtualFile

    override fun getComponent(): JComponent = viewComponent

    override fun getPreferredFocusedComponent(): JComponent = viewComponent.controlPanel

    companion object {
        private const val NAME = "CSV Editor"
    }
}
