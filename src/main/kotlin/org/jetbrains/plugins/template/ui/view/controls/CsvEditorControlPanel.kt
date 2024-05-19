package org.jetbrains.plugins.template.ui.view.controls

import com.intellij.openapi.Disposable
import org.jetbrains.plugins.template.ui.view.CsvEditorTableComponent
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JPanel

class CsvEditorControlPanel : JPanel(GridLayout()), Disposable {
    init {
        add(CsvEditorTableComponent(100, 100).scrollPane, BorderLayout.CENTER)
    }

    override fun dispose() = Unit
}