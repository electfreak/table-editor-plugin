package org.jetbrains.plugins.template.ui.view

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import net.miginfocom.swing.MigLayout
import org.jetbrains.plugins.template.ui.view.controls.CsvEditorControlPanel
import java.awt.GridLayout
import javax.swing.BoxLayout
import javax.swing.JPanel

class CsvEditorViewComponent(project: Project, private val virtualFile: VirtualFile) : JPanel(), Disposable {
    val controlPanel = CsvEditorControlPanel()

    private val wrapperPanel = JPanel(MigLayout("flowy, fill, ins 0, gap 0, hidemode 3")).apply {
        add(controlPanel, "grow, push")
    }

    init {
        Disposer.register(this, controlPanel)
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(JPanel(GridLayout(1, 1)).apply {
            add(wrapperPanel)
        })
    }

    override fun dispose() = Unit

    companion object {
//        private val logger = logger<PdfEditorViewComponent>()
    }
}