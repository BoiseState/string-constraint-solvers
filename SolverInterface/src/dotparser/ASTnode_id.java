/* Generated By:JJTree: Do not edit this line. ASTnode_id.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package dotparser;

public
class ASTnode_id extends SimpleNode {
  public ASTnode_id(int id) {
    super(id);
  }

  public ASTnode_id(DOTParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(DOTParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=219e26d81863a5c89f9bb816a39f699d (do not edit this line) */
