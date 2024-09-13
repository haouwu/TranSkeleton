import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

public class Lexer {
    private final TextManager text;
    private final HashMap<String, Token.TokenTypes> keywords;

    public Lexer(String input) {
        text = new TextManager(input);

        keywords = new HashMap<>();
        keywords.put("=", Token.TokenTypes.ASSIGN);
        keywords.put("(", Token.TokenTypes.LPAREN);
        keywords.put(")", Token.TokenTypes.RPAREN);
        keywords.put(":", Token.TokenTypes.COLON);
        keywords.put(".", Token.TokenTypes.DOT);

        keywords.put("+", Token.TokenTypes.PLUS);
        keywords.put("-", Token.TokenTypes.MINUS);
        keywords.put("*", Token.TokenTypes.TIMES);
        keywords.put("/", Token.TokenTypes.DIVIDE);
        keywords.put("%", Token.TokenTypes.MODULO);
        keywords.put(",", Token.TokenTypes.COMMA);

        keywords.put("==", Token.TokenTypes.EQUAL);
        keywords.put("!", Token.TokenTypes.NOT);
        keywords.put("!=", Token.TokenTypes.NOTEQUAL);
        keywords.put("<", Token.TokenTypes.LESSTHAN);
        keywords.put("<=", Token.TokenTypes.LESSTHANEQUAL);
        keywords.put(">", Token.TokenTypes.GREATERTHAN);
        keywords.put(">=", Token.TokenTypes.GREATERTHANEQUAL);
    }

    public List<Token> Lex() throws Exception {
        List<Token> ListOfTokens = new LinkedList<>();

        while(!text.isAtEnd()){
            Character C = text.peekCharacter();
            if (Character.isLetter(C)){
                Token token = parseWord();
                ListOfTokens.add(token);
            }

            if(Character.toString(C).equals(".")){
                text.position++;
                C = text.peekCharacter();
                text.position--;
                if(Character.isDigit(C)){
                    Token token = parseNumber();
                    ListOfTokens.add(token);
                    if(!text.isAtEnd()){
                        text.getCharacter();
                        C = text.peekCharacter();
                    }
                }
            }

            if(keywords.containsKey(Character.toString(C)) && !text.isAtEnd()){
                Token token = parsePunctuation();
                ListOfTokens.add(token);
            }

            if(Character.isDigit(C) && !text.isAtEnd()){
                Token token = parseNumber();
                ListOfTokens.add(token);
            }
            if (Character.isWhitespace(C)){
                text.position++;
            }
        }

        return ListOfTokens;
    }

    public Token parseWord() throws Exception{
        StringBuilder CurrentWord = new StringBuilder();
        Character C = text.peekCharacter();
        while(!text.isAtEnd() && Character.isLetter(C)){
            CurrentWord.append(C);
            C = text.getCharacter();

            if(!text.isAtEnd()){
                C = text.peekCharacter();
            }
        }
        return new Token(Token.TokenTypes.WORD, 0, 0, CurrentWord.toString());
    }

    public Token parseNumber() throws Exception{
        StringBuilder CurrentWord = new StringBuilder();
        Character C = text.peekCharacter();
        if(Character.toString(C).equals(".")){
            CurrentWord.append(C);
            text.getCharacter();
            C = text.peekCharacter();
        }

        while(!text.isAtEnd() && Character.isDigit(C)){
            CurrentWord.append(C);
            C = text.getCharacter();
            if(!text.isAtEnd()){
                C = text.peekCharacter();
            }
        }

        if (keywords.containsKey(C.toString())) {
            CurrentWord.append(C);
            text.position++;
            if(!text.isAtEnd()){
                C = text.peekCharacter();
                while(!text.isAtEnd() && Character.isDigit(C)){
                    CurrentWord.append(C);
                    C = text.getCharacter();
                    if(!text.isAtEnd()){
                        C = text.peekCharacter();
                    }
                }
            }


        }
        return new Token(Token.TokenTypes.NUMBER, 0, 0, CurrentWord.toString());
    }

    public Token parsePunctuation() throws Exception{
        StringBuilder CurrentWord = new StringBuilder();
        Character C = text.peekCharacter();
        while(!text.isAtEnd() && keywords.containsKey(Character.toString(C))){
            CurrentWord.append(C);
            C = text.getCharacter();

            if(!text.isAtEnd()){
                C = text.peekCharacter();
            }
        }
        String Buffer = CurrentWord.toString();
        return new Token(keywords.get(Buffer), 0, 0);
    }
}