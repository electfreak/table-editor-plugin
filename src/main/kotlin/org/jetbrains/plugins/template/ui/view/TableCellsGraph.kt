package org.jetbrains.plugins.template.ui.view

import org.jetbrains.plugins.template.termsolver.Cell

class CyclicDependencyError(s: String, val cell: Cell) : Exception(s)

class TableCellsGraph(val dependencies: MutableMap<Cell, Set<Cell>>) {
    private data class Node(
        val id: Cell,
        val children: MutableList<Node> = mutableListOf(),
        var state: State = State.NotVisited
    ) {
        enum class State { NotVisited, InProcess, Visited }
    }

    private val nodes: MutableSet<Node> = mutableSetOf()

    init {
        for ((formulaCell, cellsInFormula) in dependencies) {
            for (cell in cellsInFormula) {
                addEdgeFromDependency(cell, formulaCell)
            }
        }
    }

    private fun addEdgeFromDependency(from: Cell, to: Cell) {
        if (from == to) {
            println(dependencies)
            throw CyclicDependencyError("Recursive dependency", from)
        }

        val fromNode = nodes.find { it.id == from } ?: Node(from).also { nodes.add(it) }
        val toNode = nodes.find { it.id == to } ?: Node(to).also { nodes.add(it) }
        fromNode.children.add(toNode)
    }

    private fun dfs(node: Node, topSort: MutableList<Cell>) {
        node.state = Node.State.InProcess

        for (child in node.children) {
            when (child.state) {
                Node.State.NotVisited -> dfs(child, topSort)
                Node.State.InProcess -> throw CyclicDependencyError("Cycle", node.id)
                Node.State.Visited -> continue
            }
        }

        node.state = Node.State.Visited
        topSort.add(node.id)
    }

    private fun topSort(): List<Cell> {
        val sorted = mutableListOf<Cell>()
        for (node in nodes) {
            if (node.state == Node.State.NotVisited)
                dfs(node, sorted)
        }

        return sorted.reversed()
    }

    fun getOrder(): List<Cell> {
        return topSort()
    }
}