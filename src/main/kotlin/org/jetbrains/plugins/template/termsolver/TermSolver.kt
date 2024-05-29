package org.jetbrains.plugins.template.termsolver

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

    private fun getCellReference(input: String, pos: Int): CellReference {
        val letters = input.drop(pos).takeWhile { it.isLetter() }
        val digits = input.drop(pos + letters.length).takeWhile { it.isDigit() }.toInt()
        return CellReference(letters, digits)
    }

    fun inputToTokens(input: String): List<Token> {
        val expression = input.filterNot { it.isWhitespace() }
        val tokens = mutableListOf<Token>()
        var pos = 0
        var prev: Token? = null

        while (pos < expression.length) {
            val currChar = expression[pos]
            when {
                currChar.isDigit() -> {
                    val parsedNumber = expression.drop(pos).takeWhile { it.isDigit() || it == '.' }
                    val operand = Literal(parsedNumber.toDouble())
                    tokens.add(operand)
                    prev = operand
                    pos += parsedNumber.length
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

                currChar.isLetter() && currChar.isUpperCase() -> {
                    val cellReference = getCellReference(expression, pos)
                    tokens.add(cellReference)
                    pos += cellReference.length
                    prev = cellReference
                    continue
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
    fun inputToRpnTokens(input: String, getValueFromCell: (cellReference: CellReference) -> Double): List<Token> {
        val expression = TermParser.inputToTokens(input)
        val rpn = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()
        for (token in expression) {
            when (token) {
                is Literal -> rpn.add(token)
                is CellReference -> rpn.add(Literal(getValueFromCell(token)))

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

    fun evaluate(expression: String, getValueFromCell: (cellReference: CellReference) -> Double) =
        evaluateRpn(inputToRpnTokens(expression, getValueFromCell))
}