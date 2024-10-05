import AST.*;

import java.lang.reflect.Method;
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
        Optional<Token> nameToken = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(nameToken.isEmpty()){
            throw new SyntaxErrorException("interface requires a name",0,0);}
        String name = nameToken.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("interface requires a indent",0,0);}
        List<MethodHeaderNode> methods = new ArrayList<>();
        Optional<MethodHeaderNode> methodCheck = methodHeaderNode();
        while(methodCheck.isPresent()) {
            methods.add(methodCheck.get());
            methodCheck = methodHeaderNode();
        }
        return Optional.of(new InterfaceNode(name, methods));
    }

    private Optional<MethodHeaderNode> methodHeaderNode() throws SyntaxErrorException {
        ArrayList<VariableDeclarationNode> parameters = new ArrayList<>();
        ArrayList<VariableDeclarationNode> returns = new ArrayList<>();
        Optional<Token> wordCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(wordCheck.isEmpty()) {
            return Optional.empty();}
        String word = wordCheck.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.LPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis",0,0);}
        Optional<VariableDeclarationNode> paramCheck = VariableDeclarationNode();
        paramCheck.ifPresent(parameters::add);
        paramCheck = VariableDeclarationNodes();
        while(paramCheck.isPresent()) {
            parameters.add(paramCheck.get());
            paramCheck = VariableDeclarationNodes();
        }
        if(tokens.matchAndRemove(Token.TokenTypes.RPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis",0,0);}

        if(tokens.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {

            Optional<VariableDeclarationNode> returnCheck = VariableDeclarationNode();
            returnCheck.ifPresent(returns::add);
            returnCheck = VariableDeclarationNodes();
            while(returnCheck.isPresent()) {
                returns.add(returnCheck.get());
                returnCheck = VariableDeclarationNodes();
            }
        }

        return Optional.of(new MethodHeaderNode(word, parameters, returns ));
    }

    private Optional<VariableDeclarationNode> VariableDeclarationNode() throws SyntaxErrorException {
        Optional<Token> Type = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(Type.isEmpty()) {
            return Optional.empty();
        }
        String type = Type.get().getValue();
        Optional<Token> Name = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(Name.isEmpty()) {
            throw new SyntaxErrorException("variable requires a name",0,0);
        }
        String name = Name.get().getValue();
        return Optional.of(new VariableDeclarationNode(type, name));
    }

    private Optional<VariableDeclarationNode> VariableDeclarationNodes() throws SyntaxErrorException {
        if(tokens.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
            return VariableDeclarationNodes();
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