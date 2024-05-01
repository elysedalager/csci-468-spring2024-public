package edu.montana.csci.csci468.demo;

import edu.montana.csci.csci468.CatscriptTestBase;
import org.junit.jupiter.api.Test;
import static edu.montana.csci.csci468.tokenizer.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartnerTests extends CatscriptTestBase {
    @Test
    public void tokenizeIfWorks(){
        assertTokensAre("if (x = 10) { print(1) }",
                IF, LEFT_PAREN, IDENTIFIER, EQUAL, INTEGER, RIGHT_PAREN, LEFT_BRACE, PRINT,
                LEFT_PAREN, INTEGER, RIGHT_PAREN, RIGHT_BRACE, EOF);
    }

    @Test
    void objectsInListsPrint() {
        assertEquals("true\nfalse\nfalse\n", executeProgram("for(x in [true, false, false]) { print(x) }"));
    }

    @Test
    void reallyGoingToTryMath() {
        assertEquals(55, evaluateExpression("1 * 4 / 2 + 5 * 12 - 7"));
        assertEquals(5, evaluateExpression("1 * (4 / (2 + 2)) * 12 - 7"));
        assertEquals(27, evaluateExpression("1 * 4 / 2 + 5 * (12 - 7)"));
    }
}