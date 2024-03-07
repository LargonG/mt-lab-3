import common.TabString
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode
import org.largong.HaskellBaseVisitor
import org.largong.HaskellParser
import java.io.Writer
import kotlin.math.max

class HaskellTranslatorJS(val output: Writer): HaskellBaseVisitor<Any?>() {

    val program: MutableMap<String, Function> = mutableMapOf()

    var currentFunctionName: String = ""

    override fun visitProg(ctx: HaskellParser.ProgContext?): Any? {
        visitChildren(ctx)
        for (function in program.values) {
            output.write("${function.toString()}\n")
        }
        return null
    }
    override fun visitFunction_declaration(ctx: HaskellParser.Function_declarationContext): Any? {
        val children = getChildren(ctx)
        val name = (children[0] as Name).inner
        currentFunctionName = name
        program.putIfAbsent(name, Function(name))

        if (children[1] is Header) {
            program[name]!!.header = children[1] as Header
        } else {
            program[name]!!.addBody(children[1] as Body)
        }
        return null
    }

    override fun visitFunction_header(ctx: HaskellParser.Function_headerContext): Header =
        getChildren(ctx)[1] as Header

    override fun visitFunction_header_args(ctx: HaskellParser.Function_header_argsContext): Header {
        val children = getChildren(ctx).filterIsInstance<Type>()
        return Header(children)
    }

    override fun visitType(ctx: HaskellParser.TypeContext?): Type {
        val children = getChildren(ctx)
        return if (children.size == 1) {
            Type(children[0] as String)
        } else {
            assert(children.size == 3)
            Type("[${(children[1] as Type).inner}]")
        }
    }

    override fun visitFunction_body(ctx: HaskellParser.Function_bodyContext): Any? {
        val children = getChildren(ctx)
        val pair = children[0] as Pair<*, *>
        val args = pair.first as List<Pair<Int, Name>>
        val pattern = pair.second as List<Pair<Int, Value>>
        val ifCondition: Expression?
        val expr: Expression
        if (children.size == 4) {
            ifCondition = children[1] as Expression
            expr = children[3] as Expression
        } else {
            ifCondition = null
            expr = children[2] as Expression
        }
        return Body(args, pattern, ifCondition, expr)
    }

    override fun visitFunction_body_condition(ctx: HaskellParser.Function_body_conditionContext) =
        getChildren(ctx).filterIsInstance<Expression>()[0]

    override fun visitFunction_body_args(ctx: HaskellParser.Function_body_argsContext): Any {
        val children = getChildren(ctx).mapIndexed {index, any -> Pair(index, any) }
        val args = children.filter { pair -> pair.second is Name }
        val patternMatching = children.filter { pair -> pair.second is Value }
        return Pair(args, patternMatching)
    }

    override fun visitExpression(ctx: HaskellParser.ExpressionContext): Expression {
        val children = getChildren(ctx).filterIsInstance<Expression>()
        return if (children.size == 1) {
            children[0]
        } else {
            Apply(children[0], children.subList(1, children.size))
        }
    }

    override fun visitArith(ctx: HaskellParser.ArithContext) = template(ctx)

    override fun visitLogic(ctx: HaskellParser.LogicContext) = template(ctx)

    override fun visitIf_statement(ctx: HaskellParser.If_statementContext): Expression {
        val children = getChildren(ctx).filterIsInstance<Expression>()
        return IfExpression(children[0], children[1], children[2])
    }

    private fun template(ruleNode: RuleNode?): Expression {
        val children = getChildren(ruleNode)
        return if (children.size == 1) {
            children[0] as Expression
        } else {
            assert(children.size == 3)
            if (children[0] is String) {
                children[1] as Expression
            } else {
                Operation(
                    children[0] as Expression,
                    children[1] as String,
                    children[2] as Expression)
            }

        }
    }

    override fun visitLogic_expression(ctx: HaskellParser.Logic_expressionContext?): Expression {
        val children = getChildren(ctx)
        return if (children.size == 3) {
            children[1] as Expression
        } else {
            children[0] as Expression
        }
    }

    override fun visitLogic_compare_operator(ctx: HaskellParser.Logic_compare_operatorContext?): Any? {
        val value = flat<String>(ctx)
        val convert = mapOf(
            Pair("==", "==="),
            Pair("/=", "!==")
        )
        return if (convert.containsKey(value)) convert[value]!! else value
    }

    override fun visitArg(ctx: HaskellParser.ArgContext): Any? = flat(ctx)

    override fun visitName(ctx: HaskellParser.NameContext) = Name(flat(ctx))

    override fun visitValue(ctx: HaskellParser.ValueContext): Value {
        val value = flat<Any>(ctx)
        return if (value is Value) {
            value
        } else {
            Value(value as String)
        }
    }

    override fun visitNumber(ctx: HaskellParser.NumberContext?) = Value(flat(ctx))

    private inline fun <reified T> flat(ruleNode: RuleNode?) =
        getChildren(ruleNode)[0] as T

    private fun getChildren(ruleNode: RuleNode?) =
        visitChildren(ruleNode) as List<*>

    override fun visitTerminal(node: TerminalNode): String = node.text

    override fun aggregateResult(aggregate: Any?, nextResult: Any?): List<Any?> {
        (aggregate as MutableList<Any?>).add(nextResult)
        return aggregate
    }

    override fun defaultResult(): List<Any?> {
        return mutableListOf()
    }
}

class Function(
    val name: String,
    var header: Header? = null
): TabString {

    private var maximumElements = 0
    private val statements: MutableList<Statement> = mutableListOf()
    private val variables: MutableSet<String> = mutableSetOf()

    fun addBody(body: Body) {
        maximumElements = max(maximumElements, body.args.size)
        for (variable in body.args) {
            assign(variable.second, variable.first)
        }

        val condition =
        if (body.pattern.isNotEmpty() || body.condition != null) {
            createCondition(body.pattern, body.condition)
        } else {
            null
        }

        if (condition != null) {
            statements.add(IfStatement(condition, listOf(Return(body.expression))))
        } else {
            statements.add(Return(body.expression))
        }
    }

    private fun assign(variable: Name, index: Int) {
        statements.add(Let(variable, Name("__$index"), !variables.contains(variable.inner)))
        variables.add(variable.inner)
    }

    private fun createCondition(pattern: List<Pair<Int, Value>>, condition: Expression?): Expression {
        var current: Expression? = condition
        val eq = "==="
        val and = "&&"
        for (it in pattern) {
            val newOperation = Operation(Name("__${it.first}"), eq, it.second)
            if (current == null) {
                current = newOperation
                continue
            }
            current = Operation(current, and, newOperation)
        }

        return current ?: throw IllegalStateException()
    }

    override fun tabString(builder: StringBuilder, tabs: Int) {
        val t = "\t".repeat(tabs)
        builder.append("${t}function $name(")

        if (header == null) {
            for (it in 0 until maximumElements) {
                builder.append("__$it")
                if (it + 1 < maximumElements) {
                    builder.append(", ")
                }
            }
        } else {
            var counter = 0
            builder.append(
                header!!.args.subList(0, header!!.args.size - 1).
                joinToString(", ") { "__${counter++}" })
        }

        builder.append(") {\n")

        for (statement in statements) {
            statement.tabString(builder, tabs + 1)
        }
        builder.append("$t}\n")
    }

    override fun toString(): String = tabString(0)
}

data class Header(val args: List<Type>)

data class Body(
    val args: List<Pair<Int, Name>>,
    val pattern: List<Pair<Int, Value>>,
    val condition: Expression?,
    val expression: Expression)

sealed interface Statement: TabString

data class Return(
    val expr: Expression
): Statement {
    override fun tabString(builder: StringBuilder, tabs: Int) {
        builder
            .append("\t".repeat(tabs))
            .append("return ")
        expr.tabString(builder, tabs)
        builder.append(";\n")
    }

    override fun toString(): String = tabString(0)
}

data class Let(
    val name: Name,
    val expr: Expression,
    val declaration: Boolean
): Statement {
    override fun tabString(builder: StringBuilder, tabs: Int) {
        builder.append("\t".repeat(tabs))
        if (declaration) {
            builder.append("let ")
        }
        builder.append("$name = ")
        expr.tabString(builder, tabs)
        builder.append(";\n")
    }

    override fun toString(): String = tabString(0)
}

data class IfStatement(
    val condition: Expression,
    val ifTrue: List<Statement> = mutableListOf(),
    val ifFalse: List<Statement> = mutableListOf()
): Statement {
    override fun tabString(builder: StringBuilder, tabs: Int) {
        val padding = "\t".repeat(tabs)
        builder.append(padding).append("if (").append(condition.toString()).append(") {\n")
        for (it in ifTrue) {
            it.tabString(builder, tabs + 1)
        }
        builder.append(padding).append("} else {\n")
        for (it in ifFalse) {
            it.tabString(builder, tabs + 1)
        }
        builder.append(padding).append("}\n")
    }

    override fun toString(): String = tabString(0)
}

sealed interface Expression: TabString {
    override fun tabString(builder: StringBuilder, tabs: Int) {
        builder.append(toString())
    }
}

data class Operation(
    val left: Expression,
    val op: String,
    val right: Expression
): Expression {
    override fun toString(): String = "($left $op $right)"
}

data class IfExpression(
    val condition: Expression,
    val ifTrue: Expression,
    val ifFalse: Expression
): Expression {
    override fun toString(): String = "$condition ? $ifTrue : $ifFalse"
}

data class Value(val inner: String): Expression {
    override fun toString(): String = inner
}

data class Name(val inner: String): Expression {
    override fun toString(): String = inner
}

data class Type(val inner: String) {
    override fun toString(): String = inner
}

data class Apply(
    val what: Expression,
    val args: List<Expression>
): Expression {
    override fun toString(): String = "$what(${args.joinToString(", ") { it.toString() }})"
}