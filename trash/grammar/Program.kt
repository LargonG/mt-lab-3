package grammar

data class Program(
    val headers: MutableMap<String, Header> = mutableMapOf(),
    val bodies: MutableMap<String, MutableList<Body>> = mutableMapOf()) {
    fun header(name: String): Header {
        headers.putIfAbsent(name, Header())
        return headers[name]!!
    }

    fun body(name: String): MutableList<Body> {
        bodies.putIfAbsent(name, mutableListOf())
        return bodies[name]!!
    }
}

data class Header(val types: MutableList<String> = mutableListOf())

data class Body(
    val args: MutableList<String> = mutableListOf(),
    var condition: String = "",
    var expression: String = "")

interface Expression
data class Operation(
    val left: Expression,
    val op: Operator,
    val right: Expression): Expression
data class Value(val inner: String): Expression
data class Apply(
    val value: Expression,
    val args: MutableList<Expression> = mutableListOf()): Expression
data class IfStatement(
    val condition: Expression,
    val ifTrue: Expression,
    val ifFalse: Expression
): Expression

enum class Operator(private val value: String) {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),

    AND("&&"),
    OR("||"),

    LE("<"),
    GR(">"),
    EQ("=="),
    LEQ("<="),
    GEQ(">=")
    ;

    override fun toString(): String = value
}