import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Lexer2Tests {

    @Test
    public void IndentTest() {
        var l = new Lexer(
                "loop keepGoing\n" +
                        "    if n >= 15\n" +
                        "        keepGoing = false\n" +
                        "    hello welcome\n" +
                        "final the end"
        );
        try {
            var res = l.Lex();
            Assertions.assertEquals(Token.TokenTypes.LOOP, res.get(0).getType());
            Assertions.assertEquals("keepGoing", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(2).getType());
            Assertions.assertEquals(Token.TokenTypes.INDENT, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.IF, res.get(4).getType());
            Assertions.assertEquals("n", res.get(5).getValue());
            Assertions.assertEquals(Token.TokenTypes.GREATERTHANEQUAL, res.get(6).getType());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(7).getType());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(8).getType());
            Assertions.assertEquals(Token.TokenTypes.INDENT, res.get(9).getType());
            Assertions.assertEquals(Token.TokenTypes.INDENT, res.get(10).getType());
            Assertions.assertEquals("keepGoing", res.get(11).getValue());
            Assertions.assertEquals(Token.TokenTypes.ASSIGN, res.get(12).getType());
            Assertions.assertEquals(Token.TokenTypes.FALSE, res.get(13).getType());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(14).getType());
            Assertions.assertEquals(Token.TokenTypes.INDENT, res.get(15).getType());
            Assertions.assertEquals(Token.TokenTypes.DEDENT, res.get(16).getType());
            Assertions.assertEquals("hello", res.get(17).getValue());
            Assertions.assertEquals("welcome", res.get(18).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(19).getType());
            Assertions.assertEquals(Token.TokenTypes.DEDENT, res.get(20).getType());
            Assertions.assertEquals("final", res.get(21).getValue());
            Assertions.assertEquals("the", res.get(22).getValue());
            Assertions.assertEquals("end", res.get(23).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void MultilineLexerTest() {
        var l = new Lexer("ab cd ef gh\nasdjkdsajkl\ndsajkdsa asdjksald dsajhkl \n");
        try {
            var res = l.Lex();
            Assertions.assertEquals(11, res.size());
            Assertions.assertEquals("ab", res.get(0).getValue());
            Assertions.assertEquals("cd", res.get(1).getValue());
            Assertions.assertEquals("ef", res.get(2).getValue());
            Assertions.assertEquals("gh", res.get(3).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(4).getType());
            Assertions.assertEquals("asdjkdsajkl", res.get(5).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(6).getType());
            Assertions.assertEquals("dsajkdsa", res.get(7).getValue());
            Assertions.assertEquals("asdjksald", res.get(8).getValue());
            Assertions.assertEquals("dsajhkl", res.get(9).getValue());
            Assertions.assertEquals(Token.TokenTypes.NEWLINE, res.get(10).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void KeyWordLexerTest() {
        var l = new Lexer("class interface something accessor: mutator: if else loop");
        try {
            var res = l.Lex();
            Assertions.assertEquals(10, res.size());
            Assertions.assertEquals(Token.TokenTypes.CLASS, res.get(0).getType());
            Assertions.assertEquals(Token.TokenTypes.INTERFACE, res.get(1).getType());
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(2).getType());
            Assertions.assertEquals("something", res.get(2).getValue());
            Assertions.assertEquals(Token.TokenTypes.ACCESSOR, res.get(3).getType());
            Assertions.assertEquals(Token.TokenTypes.COLON, res.get(4).getType());
            Assertions.assertEquals(Token.TokenTypes.MUTATOR, res.get(5).getType());
            Assertions.assertEquals(Token.TokenTypes.COLON, res.get(6).getType());
            Assertions.assertEquals(Token.TokenTypes.IF, res.get(7).getType());
            Assertions.assertEquals(Token.TokenTypes.ELSE, res.get(8).getType());
            Assertions.assertEquals(Token.TokenTypes.LOOP, res.get(9).getType());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }

    @Test
    public void QuotedStringLexerTest() {
        var l = new Lexer("test \"hello\" \"there\" 1.2");
        try {
            var res = l.Lex();
            Assertions.assertEquals(4, res.size());
            Assertions.assertEquals(Token.TokenTypes.WORD, res.get(0).getType());
            Assertions.assertEquals("test", res.get(0).getValue());
            Assertions.assertEquals(Token.TokenTypes.QUOTEDSTRING, res.get(1).getType());
            Assertions.assertEquals("hello", res.get(1).getValue());
            Assertions.assertEquals(Token.TokenTypes.QUOTEDSTRING, res.get(2).getType());
            Assertions.assertEquals("there", res.get(2).getValue());
            Assertions.assertEquals(Token.TokenTypes.NUMBER, res.get(3).getType());
            Assertions.assertEquals("1.2", res.get(3).getValue());
        }
        catch (Exception e) {
            Assertions.fail("exception occurred: " +  e.getMessage());
        }
    }
}
