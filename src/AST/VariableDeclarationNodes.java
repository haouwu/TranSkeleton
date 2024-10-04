package AST;

import java.util.ArrayList;
import java.util.List;

public class VariableDeclarationNodes implements Node {
    public List<VariableDeclarationNode> variableDeclarationNodes = new ArrayList<VariableDeclarationNode>();

    public VariableDeclarationNodes(List<VariableDeclarationNodes> variableDeclarationNodes) {
    }

    @Override
    public String toString() {
        return ",";
    }
}
