import AST.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Parser3Tests
{
    @Test
    public void testDisambiguate() throws Exception {

        var l = new Lexer("class Tran\n" +
                "\thelloWorld() : number a, number b, number avg\n" +
                "\t\ta=2\n" +
                "\t\tb=100\n"+
                "\t\tavg=50\n" +
                "\tnumber z\n" +
                "\tnumber x\n" +
                "\tnumber y\n" );
        var tokens= l.Lex();
        TranNode t = new TranNode();
        Parser p = new Parser(t, tokens);
        p.Tran();
        Assertions.assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        Assertions.assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        Assertions.assertEquals(3, myMethod.statements.size());

        Assertions.assertEquals("b = 100.0 \n", ((AssignmentNode)myMethod.statements.get(1)).toString());
        Assertions.assertEquals("AST.AssignmentNode", myMethod.statements.get(2).getClass().getName());
    }

    @Test
    public void testVariableReference() throws Exception {
        var l = new Lexer("class Tran\n" +
                "\thelloWorld() : number a, number b, number avg\n" +
                "\t\ta=23\n" +
                "\t\tb=100\n"+
                "\t\tavg=23\n" +
                "\tnumber z\n" +
                "\tnumber x\n" +
                "\tnumber y\n" +
                "" );
        var tokens = l.Lex();
        TranNode t = new TranNode();
        Parser p = new Parser(t, tokens);
        p.Tran();
        Assertions.assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        Assertions.assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.get(0);

        Assertions.assertEquals("b", ((VariableReferenceNode)((AssignmentNode)myMethod.statements.get(1)).target).toString());
        Assertions.assertEquals("avg", ((VariableReferenceNode)((AssignmentNode)myMethod.statements.get(2)).target).toString());

        Assertions.assertEquals("[number a, number b, number avg]", ((myMethod.returns)).toString());

    }
    @Test
    public void testassign() throws Exception {


        var l = new Lexer("class Tran\n" +
                "\thelloWorld()\n" +
                "\t\tnumber a\n" +
                "\t\tnumber b\n" +
                "\t\tnumber avg\n" +
                "\t\ta=2\n" +
                "\t\tb=100\n" +
                "\t\tavg=3\n");
        var tokens = l.Lex();
        TranNode t = new TranNode();
        Parser p = new Parser(t, tokens);
        p.Tran();
        Assertions.assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        Assertions.assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        Assertions.assertEquals(3, myMethod.statements.size());
        Assertions.assertEquals("a = 2.0 \n", (((AssignmentNode) myMethod.statements.get(0)).toString()));
        Assertions.assertEquals("b = 100.0 \n", (((AssignmentNode) myMethod.statements.get(1)).toString()));
        Assertions.assertEquals("avg = 3.0 \n", (((AssignmentNode) myMethod.statements.get(2)).toString()));
    }



        @Test
    public void testBooleanExp_term() throws Exception {

        var l = new Lexer("class Tran\n" +
                "\thelloWorld()\n" +
                "\t\tif n>100 && n!=0 || n==1\n" +
                "\t\t\tn = 1");
        var tokens = l.Lex();
        TranNode t = new TranNode();
        Parser p = new Parser(t, tokens);
        p.Tran();
        Assertions.assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        Assertions.assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        Assertions.assertEquals(1, myMethod.statements.size());
        Assertions.assertEquals("AST.IfNode", myMethod.statements.getFirst().getClass().getName());
        Assertions.assertEquals("n > 100.0  and n != 0.0  or n == 1.0 ", ((IfNode) myMethod.statements.getFirst()).condition.toString());

        Assertions.assertTrue(((IfNode) (myMethod.statements.getFirst())).elseStatement.isEmpty());
    }
    @Test
    public void testBooleanTerm_Factor() throws Exception {

        var l = new Lexer("class Tran\n" +
                "\thelloWorld()\n" +
                "\t\tif n>100 && n!=0 || n==1\n" +
                "\t\t\tn = 1");
        var tokens = l.Lex();
        TranNode t = new TranNode();
        Parser p = new Parser(t, tokens);
        p.Tran();
        Assertions.assertEquals(1, t.Classes.size());
        var myClass = t.Classes.getFirst();
        Assertions.assertEquals(1, myClass.methods.size());
        var myMethod = myClass.methods.getFirst();
        Assertions.assertEquals(1, myMethod.statements.size());
        Assertions.assertEquals("AST.IfNode", myMethod.statements.getFirst().getClass().getName());
        Assertions.assertEquals("n > 100.0 ", ((BooleanOpNode) ((BooleanOpNode) ((IfNode) myMethod.statements.getFirst()).condition).left).left.toString());
        Assertions.assertEquals("n != 0.0 ", ((BooleanOpNode) ((BooleanOpNode) ((IfNode) myMethod.statements.getFirst()).condition).left).right.toString());
        Assertions.assertTrue(((IfNode) (myMethod.statements.getFirst())).elseStatement.isEmpty());
    }
}