import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode
import org.largong.HaskellBaseListener
import org.largong.HaskellListener
import org.largong.HaskellParser
import org.largong.HaskellParser.Function_declarationContext

class HaskellValidator: HaskellBaseListener() {
    private val functionsHeaderArgs: MutableMap<String, Int> = mutableMapOf()
    private val functionsBodyArgs: MutableMap<String, MutableList<Int>> = mutableMapOf()
    private var currentFunctionArgs = 0
    private var currentFunction: String = ""

    private fun validate() {
        for (func in functionsBodyArgs) {
            val name = func.key
            val argsShouldBe = functionsHeaderArgs[name] ?: 0
            for (argsCount in func.value) {
                if (argsCount + 1 != argsShouldBe && argsShouldBe != 0) {
                    throw IllegalStateException("function $name body has too " +
                            (if (argsCount + 1 < argsShouldBe) "few" else "many")
                            + " params, expected: ${argsShouldBe - 1}, actual: $argsCount"
                    )
                }
            }
        }
    }

    override fun visitErrorNode(p0: ErrorNode?) {
        throw IllegalStateException("Could not parse program")
    }

    override fun exitProg(ctx: HaskellParser.ProgContext?) {
        validate()
    }

    override fun enterFunction_declaration(ctx: Function_declarationContext?) {
        if (ctx == null) return
        currentFunction = ctx.name().text
    }

    override fun enterFunction_header(ctx: HaskellParser.Function_headerContext?) {
        val before = functionsHeaderArgs.putIfAbsent(currentFunction, 0)
        if (before != null) {
            throw IllegalStateException("Functions should have different names: $currentFunction")
        }
    }

    override fun enterFunction_body_args(ctx: HaskellParser.Function_body_argsContext?) {
        functionsBodyArgs.putIfAbsent(currentFunction, mutableListOf())
        currentFunctionArgs = 0
    }

    override fun exitFunction_body_args(ctx: HaskellParser.Function_body_argsContext?) {
        functionsBodyArgs[currentFunction]!!.add(currentFunctionArgs)
    }

    override fun exitType(ctx: HaskellParser.TypeContext?) {
        if (ctx == null) return
        if (ctx.type() != null) return
        functionsHeaderArgs[currentFunction] = functionsHeaderArgs[currentFunction]!! + 1
    }

    override fun enterArg(ctx: HaskellParser.ArgContext?) {
        currentFunctionArgs++
    }
}