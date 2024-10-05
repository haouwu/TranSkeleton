import java.util.List;
import java.util.Optional;

public class TokenManager {
    List<Token> tokenList;
    int position;

    public TokenManager(List<Token> tokens) {
        tokenList = tokens;
        position = 0;
    }

    public boolean done() {
        return position >= tokenList.size();
    }

    public Optional<Token> matchAndRemove(Token.TokenTypes t) {
        if(t.equals(tokenList.get(position).getType())) {
            Token removedToken = tokenList.get(position);
            tokenList.remove(position);
            return Optional.ofNullable(removedToken);
        }
        return Optional.empty();
    }

    public Optional<Token> peek(int i) {
        return Optional.ofNullable(tokenList.get(position + i));
    }

    public boolean nextTwoTokensMatch(Token.TokenTypes first, Token.TokenTypes second) {
        if(matchAndRemove(first).isPresent()) {
            return matchAndRemove(second).isPresent();
        }
        return false;

    }

    public int getCurrentLine() {
        return tokenList.get(position).getLineNumber();
    }

    public int getCurrentColumnNumber() {
        return tokenList.get(position).getColumnNumber();
    }
}
