package org.jetbrains.plugins.template.termsolver


import junit.framework.TestCase.assertEquals
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
    }

    @ParameterizedTest
    @MethodSource("expressionToResult")
    fun testEvaluate(expression: String, value: Double) {
        assertEquals(TermSolver.evaluate(expression), value)
    }

    @Test
    fun test() {
        assertEquals(true, true)
    }
}