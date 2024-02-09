package org.example

import HaskellTranslatorJS
import HaskellValidator
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.largong.HaskellLexer
import org.largong.HaskellParser
import java.io.Reader
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

fun getLexer(input: Reader): HaskellLexer = HaskellLexer(CharStreams.fromReader(input))

fun getParser(lexer: HaskellLexer): HaskellParser = HaskellParser(CommonTokenStream(lexer))

fun main(args: Array<String>) {
    assert(args.size == 2)
    val inputFile = Path.of(args[0])
    val outputFile = Path.of(args[1])

    val lexer = getLexer(Files.newBufferedReader(inputFile, StandardCharsets.UTF_8))
    val parser = getParser(lexer)
    parser.buildParseTree = true

    val tree = parser.prog()
    val validator = HaskellValidator()
    ParseTreeWalker.DEFAULT.walk(validator, tree)

    val writer: Writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)
    val translator = HaskellTranslatorJS(writer)
    translator.visit(tree)
    writer.close()
}