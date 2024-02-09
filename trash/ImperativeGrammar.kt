/**
 * statement:
 *
 *  <expression>;
 *
 *  let <name> = <expression>;
 *
 *  if <expression> {
 *      <statement>*
 *  } else {
 *      <statement>*
 *  }
 *
 *  return <expression>;
 *
 * expression:
 *
 *  <value>
 *
 *  <expression> <op> <expression> (можно объединить, так как парсер уже сделал за нас основную работу)
 *
 *  <name>(<args>)
 *
 *
 * value:
 *  true | false
 *  undefined | null
 *  <CHAR>
 *  <STRING>
 *  <NUMBER>
 *
 * name:
 *  <NAME> | <ANYNAME>
 *
 * op:
 *  + | - | * | / | && | '||' | %
 *
 */

sealed interface Statement

data class FunctionDeclaration(
    val header: FunctionHeader,
    val body: List<Statement>
): Statement {
    override fun toString(): String {
        val builder = StringBuilder().append("function $header {\n")
        for (statement in body) {
            builder.append(statement.toString())
        }
        builder.append("}\n")
        return builder.toString()
    }
}

data class FunctionHeader(
    val name: Value,
    val args: List<Value>): Expression {
    override fun toString(): String =
        "$name(${args.joinToString(",") { it.toString() }})"
}

data class OnlyExpression(val inner: Expression): Statement {
    override fun toString(): String = "$inner;\n"
}

data class VarDeclaration(
    val name: String,
    val expression: Expression
): Statement {
    override fun toString(): String = "let $name = $expression;\n"
}

data class IfStatement(
    val condition: Expression,
    val ifTrue: List<Statement>,
    val ifFalse: List<Statement>
): Statement {
    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("if ($condition) {\n")
        for (statement in ifTrue) {
            builder.append(statement.toString())
        }
        builder.append("} else {\n")
        for (statement in ifFalse) {
            builder.append(statement.toString())
        }
        builder.append("}\n")
        return builder.toString()
    }
}

data class Return(
    val expression: Expression
): Statement {
    override fun toString(): String = "return $expression;\n"
}

sealed interface Expression
//data class Operation(
//    val left: Expression,
//    val operator: Operator,
//    val right: Expression
//): Expression {
//    override fun toString(): String = "$left $operator $right"
//}
//
data class Value(private val inner: String): Expression {
    override fun toString(): String = inner
}

