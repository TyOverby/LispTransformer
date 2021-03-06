package com.tyoverby.spar.pub

import com.tyoverby.spar.parsers.generator.Rule
import com.tyoverby.spar.parsers.generator.GeneratorParser
import com.tyoverby.spar.compiler.Producer
import io.Source
import com.tyoverby.spar.parsers.lisp.LispParser
import com.tyoverby.spar.parsers.lisp.{Token => LispToken}
import java.io.File

case class RuleParsingFailure(msg: String) extends Exception(msg)

case class ProgramCompilationFailure(msg: String, fileName: String) extends Exception(s"Error in $fileName" + msg)

object PublicProducer {
  def parseRule(source: Source): List[Rule] = {
    val slurped = source.getLines().mkString("\n")
    val parsed = GeneratorParser.parseSlurped(slurped)

    parsed match {
      case GeneratorParser.NoSuccess(_, _) => throw RuleParsingFailure(parsed.toString)
      case GeneratorParser.Success(result: List[Rule], _) => result
    }
  }

  def parseRule(file: File): List[Rule] = parseRule(Source.fromFile(file))

  def parseRuleSlurped(str: String): List[Rule] = parseRule(Source.fromString(str))

  def parseRuleSources(sources: Source*): List[Rule] = {
    sources.flatMap(parseRule).toList
  }

  def parseRuleFiles(files: File*): List[Rule] = parseRuleSources(files.map(Source.fromFile): _*)

  def parseSource(source: Source): List[LispToken] = {
    val slurped = source.getLines().foldLeft("")(_ + "\n" + _)
    parseSourceSlurped(slurped, source.descr)
  }

  def parseSource(file: File): List[LispToken] = parseSource(Source.fromFile(file))

  def parseSourceSlurped(slurped: String, fileName: String): List[LispToken] = {
    val parts = LispParser.parseSlurped(slurped)

    parts match {
      case LispParser.Success(result: List[LispToken], _) => result
      case x@LispParser.NoSuccess(_, _) => throw ProgramCompilationFailure(x.toString, fileName)
    }
  }

  def parseSourceSources(sources: Source*): List[LispToken] = sources.map(parseSource).flatten.toList

  def parseSourceFiles(files: File*): List[LispToken] = parseSourceSources(files.map(Source.fromFile): _*)

  def compile(program: List[LispToken], rules: List[Rule]): String = {
    Producer.produceAll(program)(rules)
  }

  def compileSourceRules(program: Source, rules: List[Rule]): String = {
    compile(parseSource(program), rules)
  }

  def compileFileRules(program: File, rules: List[Rule]): String = {
    compile(parseSource(program), rules)
  }

  def compileSourceSources(program: Source, rules: List[Source]): String = {
    compile(parseSource(program), parseRuleSources(rules: _*))
  }

  def compileFileFiles(program: File, rules: List[File]): String = {
    compileSourceRules(Source.fromFile(program), parseRuleFiles(rules: _*))
  }
}
