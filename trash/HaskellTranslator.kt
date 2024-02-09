import grammar.Body
import grammar.Header
import grammar.Program
import org.antlr.runtime.tree.TreeWizard.Visitor
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.*
import org.largong.HaskellBaseListener
import org.largong.HaskellListener
import org.largong.HaskellParser
import java.io.Writer

class HaskellTranslator(private val output: Writer): HaskellBaseListener() {
    private var program = Program()

    private var currentFunctionName: String = ""
    private var typeRecursive: Boolean = false

    private var currentBody: Body = Body()
    private var currentHeader: Header = Header()

    private val map: MutableMap<String, String> = mutableMapOf()

    fun translate() {
        for (it in program.headers) {
            output.write("$it\n")
        }
        output.write("\n")
        for (it in program.bodies) {
            output.write("$it\n")
        }
    }

    override fun visitTerminal(p0: TerminalNode?) {}

    override fun visitErrorNode(p0: ErrorNode?) {
        throw IllegalStateException("Program cannot be parsed: $p0")
    }

    override fun enterEveryRule(p0: ParserRuleContext?) {}

    override fun exitEveryRule(p0: ParserRuleContext?) {}

    override fun enterProg(ctx: HaskellParser.ProgContext?) {}

    override fun exitProg(ctx: HaskellParser.ProgContext?) {}

    override fun enterFunction_declaration(ctx: HaskellParser.Function_declarationContext?) {
        if (ctx == null) return
        currentFunctionName = ctx.name().text
    }

    override fun exitFunction_declaration(ctx: HaskellParser.Function_declarationContext?) {}

    override fun enterFunction_header(ctx: HaskellParser.Function_headerContext?) {
        currentHeader = Header()
    }

    override fun exitFunction_header(ctx: HaskellParser.Function_headerContext?) {
        program.headers[currentFunctionName] = currentHeader
    }

    override fun enterFunction_header_args(ctx: HaskellParser.Function_header_argsContext?) {}

    override fun exitFunction_header_args(ctx: HaskellParser.Function_header_argsContext?) {}

    override fun enterFunction_body(ctx: HaskellParser.Function_bodyContext?) {
        currentBody = Body()
    }

    override fun exitFunction_body(ctx: HaskellParser.Function_bodyContext?) {
        if (ctx == null) return
        if (ctx.expression() != null) {
            currentBody.expression = ctx.expression().text
        }
        program.body(currentFunctionName).add(currentBody)
    }

    override fun enterFunction_body_args(ctx: HaskellParser.Function_body_argsContext?) {}

    override fun exitFunction_body_args(ctx: HaskellParser.Function_body_argsContext?) {}
    override fun enterFunction_body_condition(ctx: HaskellParser.Function_body_conditionContext?) {
        if (ctx == null) return
        currentBody.condition = ctx.expression().text
    }

    override fun exitFunction_body_condition(ctx: HaskellParser.Function_body_conditionContext?) {}

    override fun enterType(ctx: HaskellParser.TypeContext?) {
        if (ctx == null) return
        if (typeRecursive) return
        currentHeader.types.add(ctx.text)
        if (ctx.type() != null) {
            typeRecursive = true
        }
    }

    override fun exitType(ctx: HaskellParser.TypeContext?) {
        typeRecursive = false
    }

    override fun enterName(ctx: HaskellParser.NameContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitName(ctx: HaskellParser.NameContext?) {
//        TODO("Not yet implemented")
    }

    override fun enterValue(ctx: HaskellParser.ValueContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitValue(ctx: HaskellParser.ValueContext?) {
//        TODO("Not yet implemented")
    }

    override fun enterExpression(ctx: HaskellParser.ExpressionContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitExpression(ctx: HaskellParser.ExpressionContext?) {
//        TODO("Not yet implemented")
        if (ctx == null) return
        val builder = StringBuilder()
        if (ctx.expression() != null) {
            var counter = 0
            for (it in ctx.expression()) {
                if (counter == 0) {
                    builder.append(map[it.text]).append("(")
                    counter++
                    continue
                }
                builder.append(map[it.text])
            }
            builder.append(")")
        } else {
            ctx.
        }
        map[ctx.text] = builder.toString()
    }

    override fun enterArg(ctx: HaskellParser.ArgContext?) {
        if (ctx == null) return
        currentBody.args.add(ctx.text)
    }

    override fun exitArg(ctx: HaskellParser.ArgContext?) {
//        TODO("Not yet implemented")
    }

    override fun enterArith(ctx: HaskellParser.ArithContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitArith(ctx: HaskellParser.ArithContext?) {
//        TODO("Not yet implemented")
    }

    override fun enterVal(ctx: HaskellParser.ValContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitVal(ctx: HaskellParser.ValContext?) {
//        TODO("Not yet implemented")
    }

    override fun enterLogic(ctx: HaskellParser.LogicContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitLogic(ctx: HaskellParser.LogicContext?) {
//        TODO("Not yet implemented")
    }

    override fun enterLogic_compare_operator(ctx: HaskellParser.Logic_compare_operatorContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitLogic_compare_operator(ctx: HaskellParser.Logic_compare_operatorContext?) {
//        TODO("Not yet implemented")
    }

    override fun enterLogic_expression(ctx: HaskellParser.Logic_expressionContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitLogic_expression(ctx: HaskellParser.Logic_expressionContext?) {
//        TODO("Not yet implemented")
    }

    override fun enterIf_statement(ctx: HaskellParser.If_statementContext?) {
//        TODO("Not yet implemented")
    }

    override fun exitIf_statement(ctx: HaskellParser.If_statementContext?) {
//        TODO("Not yet implemented")
        if (ctx == null) return
        val condition = ctx.expression(0)
        val ifTrue = ctx.expression(1)
        val ifFalse = ctx.expression(2)

    }
}