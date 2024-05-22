package org.jetbrains.plugins.template.ui.view

import org.jetbrains.plugins.template.termsolver.TermSolver
import javax.swing.table.DefaultTableModel

class TableCellsGraph(dependencies: MutableMap<Pair<Int, Int>, MutableSet<Pair<Int, Int>>>) {
    private data class Node(
        val id: Pair<Int, Int>,
        val children: MutableList<Node> = mutableListOf(),
        var state: State = State.NotVisited
    ) {
        enum class State { NotVisited, InProcess, Visited }
    }

    private val nodes: MutableSet<Node> = mutableSetOf()

    init {
        for ((fromCell, toCells) in dependencies) {
            for (toCell in toCells) {
                addEdgeFromDependency(fromCell, toCell)
            }
        }
    }

    private fun addEdgeFromDependency(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        if (from == to) {
            throw Error("Recursive dependency")
        }

        val fromNode = nodes.find { it.id == from } ?: Node(from).also { nodes.add(it) }
        val toNode = nodes.find { it.id == to } ?: Node(to).also { nodes.add(it) }
        fromNode.children.add(toNode)
    }

    private fun dfs(node: Node, topSort: MutableList<Pair<Int, Int>>) {
        node.state = Node.State.InProcess

        for (child in node.children) {
            when (child.state) {
                Node.State.NotVisited -> dfs(child, topSort)
                Node.State.InProcess -> throw Error("cycle")
                Node.State.Visited -> continue
            }
        }

        node.state = Node.State.Visited
        topSort.add(node.id)
    }

    private fun topSort(): List<Pair<Int, Int>> {
        val sorted = mutableListOf<Pair<Int, Int>>()
        try {
            for (node in nodes) {
                if (node.state == Node.State.NotVisited)
                    dfs(node, sorted)
            }
        } catch (e: Error) {
            throw e
        }

        return sorted.reversed()
    }

    fun getOrder(): List<Pair<Int, Int>> {
        return topSort()
    }
}

class CsvEditorTableModel(data: Array<Array<Any?>>, columnNames: Array<String>) : DefaultTableModel(data, columnNames) {
    val formulas: MutableMap<Pair<Int, Int>, String> = mutableMapOf()
    private val dependencies: MutableMap<Pair<Int, Int>, MutableSet<Pair<Int, Int>>> = mutableMapOf()
    private val formulasOrder
        get() = TableCellsGraph(dependencies).getOrder()

    override fun setValueAt(value: Any?, row: Int, column: Int) {
        if (value is String && value.startsWith("=")) {
            formulas[Pair(row, column)] = value
            updateDependencies(value.substring(1), row, column)
        } else {
            formulas.remove(Pair(row, column))
            super.setValueAt(value, row, column)
        }
        println("dependencies: $dependencies")
        updateFormulas()
    }

    private fun updateFormulas() {
        formulasOrder.forEach { (row, col) ->
            val formula = formulas[row to col]
            if (formula != null) {
                val result = evaluateFormula(formula.substring(1), row, col)
                super.setValueAt(result, row, col)
            }
        }
    }

    private fun updateDependentCells(row: Int, column: Int) {
        val cell = Pair(row, column)
        dependencies[cell]?.forEach { dependentCell ->
            val (depRow, depCol) = dependentCell
            val formula = formulas[dependentCell]
            if (formula != null) {
                val result = evaluateFormula(formula.substring(1), depRow, depCol)
                super.setValueAt(result, depRow, depCol)
            }
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