package org.jetbrains.plugins.template.ui.view

import org.jetbrains.plugins.template.termsolver.TermSolver
import javax.swing.table.DefaultTableModel

class CsvEditorTableModel(data: Array<Array<Any?>>, columnNames: Array<String>) : DefaultTableModel(data, columnNames) {
    val formulas: MutableMap<Pair<Int, Int>, String> = mutableMapOf()
    private val dependencies: MutableMap<Pair<Int, Int>, MutableSet<Pair<Int, Int>>> = mutableMapOf()

    override fun setValueAt(value: Any?, row: Int, column: Int) {
        if (value is String && value.startsWith("=")) {
            formulas[Pair(row, column)] = value
            updateDependencies(value.substring(1), row, column)
        } else {
            formulas.remove(Pair(row, column))
            super.setValueAt(value, row, column)
        }

        updateFormulas()
    }

    private fun updateFormulas() {
        val cellsOrdered = try {
            TableCellsGraph(dependencies).getOrder()
        } catch (e: CyclicDependencyError) {
            formulas.remove(e.cell)
            dependencies.remove(e.cell)
            super.setValueAt("<was in dependency cycle>", e.cell.first, e.cell.second)
            TableCellsGraph(dependencies).getOrder()
        }

        cellsOrdered.forEach { (row, col) ->
            val formula = formulas[row to col] ?: return@forEach

            val result = try {
                evaluateFormula(formula.substring(1), row, col)
            } catch (e: Exception) {
                formulas.remove(row to col)
                dependencies.remove(row to col)
                super.setValueAt("<could not execute formula>", row, col)
                return@forEach
            }

            super.setValueAt(result, row, col)
        }
    }

    private fun colByHeader(header: String) = header.fold(0) { acc, char -> (char - 'A' + 1) + acc * 26 }
    private fun updateDependencies(formula: String, formulaRow: Int, formulaColumn: Int) {
        val regex = Regex("([A-Z]+)(\\d+)")

        fun processCellAddress(col: Int, row: Int) {
            val dependentCell = Pair(formulaRow, formulaColumn)
            dependencies.getOrPut(Pair(row, col)) { mutableSetOf() }.add(dependentCell)
        }

        val matches = regex.findAll(formula)

        for (match in matches) {
            val (letters, number) = match.destructured
            processCellAddress(colByHeader(letters) - 1, number.toInt() - 1)
        }
    }

    private fun evaluateFormula(formula: String, formulaRow: Int, formulaColumn: Int): Any {
        val regex = Regex("([A-Z]+)(\\d+)")

        fun processCellAddress(col: Int, row: Int): String {
            return getValueAt(row, col)?.toString() ?: ""
        }

        val expression = regex.replace(formula) { matchResult ->
            val (letters, number) = matchResult.destructured
            processCellAddress(colByHeader(letters) - 1, number.toInt() - 1)
        }

        return TermSolver.evaluate(expression)
    }
}