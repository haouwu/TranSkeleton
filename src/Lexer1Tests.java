import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class Lexer1Tests {

    @Test
    public void SimpleLexerTest() {
        var l = new Lexer("ab cd ef gh");
        try {
            var res = l.Lex();
            Assertions.assertEquals(4, res.size());
            Assertions.assertEquals("ab", res.get(0).getValue());
            Assertions.assertEquals("cd", res.get(1).getValue());
            Assertions.assertEquals("ef", res.get(2).getValue());
            Assertions.assertEquals("gh", res.get(3).getValue());
            for (var result : res)
                Assertions.assertEquals(Token.TokenTypes.WORD, result.getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }


    @Test
    public void NotEqualsTest() {
        var l = new Lexer("=");
        try {
            var res = l.Lex();
            Assertions.assertEquals(1, res.size());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }


    @Test
    public void NotEqualsTest1() {
        var l = new Lexer("2.3");
        try {
            var res = l.Lex();
            Assertions.assertEquals("2.3", res.get(0).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void TwoCharacterTest() {
        var l = new Lexer(">= > <= < = == !=");
        try {
            var res = l.Lex();
            Assertions.assertEquals(7, res.size());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHANEQUAL, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHAN, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.LESSTHANEQUAL, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.LESSTHAN, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.ASSIGN, res.get(4).getType());
            Assertions.assertEquals(Token.TokenTypes.EQUAL, res.get(5).getType());
            Assertions.assertEquals(Token.TokenTypes.NOTEQUAL, res.get(6).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest() {
        var l = new Lexer("word 1.2 : ( )");
        try {
            var res = l.Lex();
            Assertions.assertEquals(5, res.size());
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(0).getType());
            Assertions.assertEquals("word", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());
            Assertions.assertEquals("1.2", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.COLON, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.LPAREN, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.RPAREN, res.get(4).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest2() {
        var l = new Lexer("word 1.222 : ( )");
        try {
            var res = l.Lex();
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(0).getType());
            Assertions.assertEquals("word", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(1).getType());
            Assertions.assertEquals("1.222", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.COLON, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.LPAREN, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.RPAREN, res.get(4).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest3() {
        var l = new Lexer(".2.2");
        try {
            var res = l.Lex();
            Assertions.assertEquals(".2", res.get(0).getValue());
            Assertions.assertEquals(".2", res.get(1).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest4() {
        var l = new Lexer("<= ==.");
        try {
            var res = l.Lex();
            Assertions.assertEquals(Token.TokenTypes.LESSTHANEQUAL, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.EQUAL, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(2).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest5() {
        var l = new Lexer("x=1");
        try {
            var res = l.Lex();
            Assertions.assertEquals("x", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.ASSIGN, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(2).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest6() {
        var l = new Lexer("xxx \n xxx");
        try {
            var res = l.Lex();
            Assertions.assertEquals("xxx", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(1).getType());
            Assertions.assertEquals("xxx", res.get(2).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest7() {
        var l = new Lexer("line. \n This is another line.");
        try {
            var res = l.Lex();
            Assertions.assertEquals("line", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(2).getType());
            Assertions.assertEquals("This", res.get(3).getValue());
            Assertions.assertEquals("is", res.get(4).getValue());
            Assertions.assertEquals("another", res.get(5).getValue());
            Assertions.assertEquals("line", res.get(6).getValue());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(7).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MixedTest8() {
        var l = new Lexer("line. \n This is another line. {ABC} DFDFDFDFDF {DSDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD}");
        try {
            var res = l.Lex();
            Assertions.assertEquals("line", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(2).getType());
            Assertions.assertEquals("This", res.get(3).getValue());
            Assertions.assertEquals("is", res.get(4).getValue());
            Assertions.assertEquals("another", res.get(5).getValue());
            Assertions.assertEquals("line", res.get(6).getValue());
            Assertions.assertEquals(Token.TokenTypes.DOT, res.get(7).getType());
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(8).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }


}
