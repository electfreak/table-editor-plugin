package org.jetbrains.plugins.template.termsolver

import java.util.*

object TermSolver {
    fun inputToRpnTokens(input: String, getValueFromCell: (cellReference: Cell) -> Double): List<Token> {
        val expression = TermParser.inputToTokens(input)
        val rpn = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()
        for (token in expression) {
            when (token) {
                is Literal -> rpn.add(token)
                is Cell -> rpn.add(Literal(getValueFromCell(token)))

                is UnaryOperator -> stack.addLast(token)
                is BinaryOperator -> {
                    while (
                        !stack.isEmpty() &&
                        (stack.last is UnaryOperator || (stack.last is BinaryOperator && (stack.last as BinaryOperator).priority >= token.priority))
                    ) {
                        rpn.add(stack.removeLast())
                    }

                    stack.addLast(token)
                }

                Brackets.Left -> stack.addLast(Brackets.Left)
                Brackets.Right -> {
                    while (stack.last != Brackets.Left) {
                        rpn.add(stack.removeLast())
                    }

                    stack.removeLast()
                }
            }
        }

        while (!stack.isEmpty()) {
            rpn.add(stack.removeLast())
        }

        return rpn
    }

    private fun evaluateRpn(tokens: List<Token>): Double {
        val stack = ArrayDeque<Double>()
        for (token in tokens) {
            when (token) {
                is Literal -> stack.addLast(token.value)

                is UnaryOperator -> {
                    stack.addLast(
                        token.compute(stack.removeLast())
                    )
                }

                is BinaryOperator -> {
                    val right = stack.removeLast()
                    val left = stack.removeLast()
                    stack.addLast(
                        token.compute(left, right)
                    )
                }
            }
        }

        return stack.removeLast() as Double
    }

    fun evaluate(expression: String, getValueFromCell: (cell: Cell) -> Double) =
        evaluateRpn(inputToRpnTokens(expression, getValueFromCell))
}