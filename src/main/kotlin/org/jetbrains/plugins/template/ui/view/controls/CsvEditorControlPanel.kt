package org.jetbrains.plugins.template.ui.view.controls

import com.intellij.openapi.Disposable
import java.awt.*
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.ScrollPaneConstants
import javax.swing.table.DefaultTableModel


class CsvEditorControlPanel : JPanel(GridLayout()), Disposable {
    private val panel = JPanel()

    init {
        val data = Array(100) {
            arrayOfNulls<Any>(
                30
            )
        }
        for (i in 0..99) {
            for (j in 0..29) {
                data[i][j] = ""
            }
        }

        // Заголовки столбцов (A, B, C, ..., Z)
        val columnNames = arrayOfNulls<String>(30)
//        for (i in 0..29) {
//            columnNames[i] = ('A'.code + i).toChar().toString()
//        }

        // Создаем модель таблицы с данными и заголовками
        val model: DefaultTableModel = object : DefaultTableModel(data, columnNames) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                // Делаем все ячейки редактируемыми
                return true
            }
        }

//        for (i in 0..<model.rowCount) {
//            model.setValueAt((i + 1).toString(), i, 0)
//        }

        // Создаем таблицу с моделью
        val table = JTable(model)

        table.setRowHeight(25)

        // Устанавливаем предпочтительную ширину столбцов
        for (i in 0 until table.columnCount) {
            table.columnModel.getColumn(i).setPreferredWidth(50)
        }


        // Создаем рендерер для заголовков строк (1, 2, 3, ..., 100)
        val rowHeaderTable: JTable = object : JTable(DefaultTableModel(100, 1)) {
            override fun getValueAt(row: Int, column: Int): Any {
                return row + 1
            }
        }
        rowHeaderTable.preferredScrollableViewportSize = Dimension(50, 0)
        rowHeaderTable.setRowHeight(25)
        rowHeaderTable.setEnabled(false)
        rowHeaderTable.tableHeader.setReorderingAllowed(false)

        // Настройка отображения заголовков строк
        for (i in 0 until rowHeaderTable.columnCount) {
            rowHeaderTable.columnModel.getColumn(i).setPreferredWidth(50)
        }

        // Оборачиваем таблицу в JScrollPane для поддержки прокрутки
        val scrollPane = JScrollPane(table)

        // Настраиваем отображение заголовков строк в JScrollPane
        scrollPane.setRowHeaderView(rowHeaderTable)
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaderTable.tableHeader)
//        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
//        scrollPane.horizontalScrollBar.preferredSize = Dimension(scrollPane.width, 20)

        // Добавляем JScrollPane в JPanel
        val panel = JPanel(BorderLayout())
        panel.add(scrollPane, BorderLayout.CENTER)
//        panel.preferredSize = Dimension(Int.MAX_VALUE, 500)
//        panel.layout = FlowLayout(FlowLayout.CENTER, 0, 0)
        add(panel, Component.CENTER_ALIGNMENT)
    }

    override fun dispose() = Unit
}