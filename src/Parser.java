import AST.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private TranNode tran;
    private final TokenManager tokens;

    public Parser(TranNode top, List<Token> Tokens) {
        tokens = new TokenManager(Tokens);
        tran = top;
    }

    public void RequireNewLine(){
        while(!tokens.done()){
            tokens.matchAndRemove(Token.TokenTypes.NEWLINE);
            tokens.position++;
        }
        tokens.position =0;
    }

    private Optional<InterfaceNode> interfaceNode() throws SyntaxErrorException {
        Optional<InterfaceNode> interface1 = Optional.of(new InterfaceNode());

        Optional<Token> nameCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(nameCheck.isEmpty()){
            throw new SyntaxErrorException("interface requires a name",0,0);}
        interface1.get().name = nameCheck.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("interface requires a indent",0,0);}
        Optional<MethodHeaderNode> methodCheck = methodHeaderNode();
        while(methodCheck.isPresent()) {
            interface1.get().methods.add(methodCheck.get());
            methodCheck = methodHeaderNode();
        }
        return interface1;
    }

    private Optional<MethodHeaderNode> methodHeaderNode() throws SyntaxErrorException {
        Optional<MethodHeaderNode> method = Optional.of(new MethodHeaderNode());

        Optional<Token> wordCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(wordCheck.isEmpty()) {
            return Optional.empty();}
        method.get().name = wordCheck.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.LPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis",0,0);}
        Optional<VariableDeclarationNode> paramCheck = VariableDeclarationNode();
        paramCheck.ifPresent(variableDeclarationNode -> method.get().parameters.add(variableDeclarationNode));
        paramCheck = VariableDeclarationNodes();
        while(paramCheck.isPresent()) {
            method.get().parameters.add(paramCheck.get());
            paramCheck = VariableDeclarationNodes();
        }
        if(tokens.matchAndRemove(Token.TokenTypes.RPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis",0,0);}

        if(tokens.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {
            Optional<VariableDeclarationNode> returnCheck = VariableDeclarationNode();
            returnCheck.ifPresent(variableDeclarationNode -> method.get().returns.add(variableDeclarationNode));
            returnCheck = VariableDeclarationNodes();
            while(returnCheck.isPresent()) {
                method.get().returns.add(returnCheck.get());
                returnCheck = VariableDeclarationNodes();
            }
        }

        return method;
    }

    private Optional<VariableDeclarationNode> VariableDeclarationNode() throws SyntaxErrorException {
        Optional<VariableDeclarationNode> variable = Optional.of(new VariableDeclarationNode());
        Optional<Token> TypeCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(TypeCheck.isEmpty()) {
            return Optional.empty();
        }
        variable.get().type = TypeCheck.get().getValue();

        Optional<Token> NameCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(NameCheck.isEmpty()) {
            throw new SyntaxErrorException("variable requires a name",0,0);
        }
        variable.get().name = NameCheck.get().getValue();
        return variable;
    }

    private Optional<VariableDeclarationNode> VariableDeclarationNodes() throws SyntaxErrorException {
        if(tokens.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
            return VariableDeclarationNode();
        }
        return Optional.empty();
    }


    // Tran = { Class | Interface }
    public void Tran() throws SyntaxErrorException {
        RequireNewLine();
        if(tokens.matchAndRemove(Token.TokenTypes.INTERFACE).isPresent()) {
            tran.Interfaces.add(interfaceNode().get());
        }
    }
}