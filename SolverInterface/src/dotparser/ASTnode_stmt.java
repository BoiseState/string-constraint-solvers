/* Generated By:JJTree: Do not edit this line. ASTnode_stmt.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package dotparser;

public
class ASTnode_stmt extends SimpleNode {
  public ASTnode_stmt(int id) {
    super(id);
  }

  public ASTnode_stmt(DOTParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(DOTParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=d71adb771dcd1be58df591c9cb014763 (do not edit this line) */
