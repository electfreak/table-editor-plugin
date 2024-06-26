package org.jetbrains.plugins.template.termsolver


import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ParserTests {
    companion object {
        @JvmStatic
        fun expressionToResult(): List<Arguments> = listOf(
            Arguments.of("1+2", 3.0),
            Arguments.of("0+0", 0.0),
            Arguments.of("-0", -0.0),
            Arguments.of("-1", -1.0),
            Arguments.of("1+2*3", 7.0),
            Arguments.of("1*2+3", 5.0),
            Arguments.of("1+(-2+2)", 1.0),
            Arguments.of("sqrt(4)+2+sqrt(16)+(-2)", 6.0),
        )

        @JvmStatic
        fun expressionToTokens(): List<Arguments> = listOf(
            Arguments.of("", listOf<Token>()),
            Arguments.of("A1", listOf(Cell("A", 1))),
            Arguments.of("ABC123", listOf(Cell("ABC", 123))),
            Arguments.of(
                "1 + 2", listOf(Literal(1.0), BinaryOperator.PLUS, Literal(2.0))
            ),
            Arguments.of(
                "-1", listOf(UnaryOperator.NEGATION, Literal(1.0))
            ),
            Arguments.of(
                "-6.23 * (A1 + B3)",
                listOf(
                    UnaryOperator.NEGATION,
                    Literal(6.23),
                    BinaryOperator.MULTIPLY,
                    Brackets.Left,
                    Cell("A", 1),
                    BinaryOperator.PLUS,
                    Cell("B", 3),
                    Brackets.Right
                )
            )
        )
    }

    private val dummyGetValueFromCell = { (row, col): Cell -> (row + col).toDouble() }

    @ParameterizedTest
    @MethodSource("expressionToResult")
    fun testEvaluate(expression: String, value: Double) {
        assertEquals(value, TermSolver.evaluate(expression, dummyGetValueFromCell))
    }

    @ParameterizedTest
    @MethodSource("expressionToTokens")
    fun testParse(expression: String, value: List<Token>) {
        println(TermParser.inputToTokens(expression))
        assertArrayEquals(value.toTypedArray(), TermParser.inputToTokens(expression).toTypedArray())
    }
}