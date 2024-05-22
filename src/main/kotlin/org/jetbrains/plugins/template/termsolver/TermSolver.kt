package org.jetbrains.plugins.template.termsolver

import ai.grazie.nlp.utils.dropWhitespaces
import java.util.*

object TermParser {
    private fun isLikelyUnary(previous: Token?) =
        previous == null || previous == Brackets.Left

    private fun getOperator(input: String, pos: Int, prev: Token?): Operator {
        val unaryOperator = UnaryOperator.values().find { input.drop(pos).startsWith(it.form) }

        if (unaryOperator != null && (unaryOperator.withBrackets || isLikelyUnary(prev))) {
            return unaryOperator
        }

        return BinaryOperator.operatorByChar[input[pos]] ?: error("Invalid expression")
    }

    fun inputToTokens(input: String): MutableList<Token> {
        val expression = input.dropWhitespaces()
        val tokens = mutableListOf<Token>()
        var pos = 0
        var prev: Token? = null

        while (pos < expression.length) {
            val currChar = expression[pos]
            when {
                currChar.isDigit() -> {
                    var posRight = pos + 1
                    while (posRight < expression.length && (expression[posRight].isDigit() || expression[posRight] == '.')) {
                        ++posRight
                    }

                    val operand = Operand(expression.slice(pos until posRight).toDouble())
                    tokens.add(operand)
                    prev = operand
                    pos = posRight
                    continue
                }

                currChar == '(' -> {
                    tokens.add(Brackets.Left)
                    prev = Brackets.Left
                }

                currChar == ')' -> {
                    tokens.add(Brackets.Right)
                    prev = Brackets.Right
                }

                else -> {
                    val operator = getOperator(expression, pos, prev)
                    if (operator is UnaryOperator) {
                        tokens.add(operator)
                        pos += operator.form.length
                        prev = operator
                        continue
                    }

                    if (operator is BinaryOperator) {
                        tokens.add(operator)
                        prev = operator
                    }
                }
            }

            ++pos
        }

        return tokens
    }
}

object TermSolver {
    fun inputToRpnTokens(input: String): List<Token> {
        val expression = TermParser.inputToTokens(input)
        val rpn = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()
        for (token in expression) {
            when (token) {
                is Operand -> {
                    rpn.add(token)
                }

                is BinaryOperator -> {
                    while (
                        !stack.isEmpty() &&
                        (stack.last is UnaryOperator || (stack.last is BinaryOperator && (stack.last as BinaryOperator).priority >= token.priority))
                    ) {
                        rpn.add(stack.removeLast())
                    }

                    stack.addLast(token)
                }

                is UnaryOperator -> {
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
                is Operand -> stack.addLast(token.value)

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

    fun evaluate(expression: String) = evaluateRpn(inputToRpnTokens(expression))
}