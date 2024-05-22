package org.jetbrains.plugins.template.termsolver

import java.util.*

object TermSolver {
    private fun isLikelyUnary(prev: Any?) =
        prev == null || prev == '('

    private fun getOperator(input: String, pos: Int, prev: Any?): Operators {
        val unaryOperator = UnaryOperators.entries.find { input.drop(pos).startsWith(it.form) }
        println("unaryOperator: $unaryOperator")
        println("prev: $prev")

        if (unaryOperator != null && (unaryOperator.isOnlyUnary() || isLikelyUnary(prev))) {
            return unaryOperator
        }

        return BinaryOperators.operatorByChar[input[pos]] ?: throw Error("Invalid expression")
    }

    private fun strToRpnTokens(input: String): List<Any> {
        val tokens = mutableListOf<Any>()
        val stack = ArrayDeque<Any>()
        var prev: Any? = null

        var pos = 0
        while (pos < input.length) {
            val currChar = input[pos]
            when {
                currChar.isDigit() -> {
                    var posRight = pos + 1
                    while (posRight < input.length && (input[posRight].isDigit() || input[posRight] == '.')) {
                        ++posRight
                    }

                    val operand = input.slice(pos until posRight).toDouble()
                    tokens.add(operand)
                    prev = operand
                    pos = posRight
                    continue
                }

                currChar == '(' -> {
                    stack.addLast('(')
                    prev = '('
                }

                currChar == ')' -> {
                    while (stack.last != '(') {
                        tokens.add(stack.removeLast())
                    }

                    stack.removeLast()
                    prev = ')'
                }

                else -> {
                    val operator = getOperator(input, pos, prev)
                    if (operator is UnaryOperators) {
                        stack.addLast(operator)
                        pos += operator.form.length
                        prev = operator
                        continue
                    }

                    if (operator is BinaryOperators) {
                        while (
                            !stack.isEmpty() &&
                            (stack.last is UnaryOperators ||
                                    (stack.last is BinaryOperators && (stack.last as BinaryOperators).priority >= operator.priority))
                        ) {
                            tokens.add(stack.removeLast())
                        }

                        stack.addLast(operator)
                        prev = operator
                    }
                }
            }

            ++pos
        }

        while (!stack.isEmpty()) {
            tokens.add(stack.removeLast())
        }

        return tokens
    }

    private fun evaluateRpn(tokens: List<Any>): Double {
        val stack = ArrayDeque<Any>()
        for (token in tokens) {
            when (token) {
                is Double -> stack.addLast(token)
                is UnaryOperators -> {
                    stack.addLast(
                        token.compute(stack.removeLast() as Double)
                    )
                }

                is BinaryOperators -> {
                    val right = stack.removeLast()
                    val left = stack.removeLast()
                    stack.addLast(
                        token.compute(left as Double, right as Double)
                    )
                }
            }
        }

        return stack.removeLast() as Double
    }

    fun evaluate(expression: String) = evaluateRpn(strToRpnTokens(expression))
}