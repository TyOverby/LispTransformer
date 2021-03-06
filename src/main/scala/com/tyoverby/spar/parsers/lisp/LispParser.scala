package com.tyoverby.spar.parsers.lisp

import com.tyoverby.spar.parsers.lisp.LispTokens._
import util.parsing.input.CharSequenceReader
import scala.util.parsing.combinator.JavaTokenParsers

object LispParser extends JavaTokenParsers {


  override def ident: Parser[String] = """[^0-9()\[\]{}. #":][^()\[\]{} '"\n]*""".r

  private[this] def break: Parser[String] = """[ (){}\[\]]|\z""".r

  private[this] def variable: Parser[Token] = ":[a-zA-Z]+".r ^^ Variable

  private[this] def repeat: Parser[Token] = wrappableExpressions <~ "..." ^^ Repeat

  private[this] def number: Parser[Token] = (floatingPointNumber | decimalNumber | wholeNumber) /*<~ break*/ ^^ TokenTranslations.genNumberLiteral

  private[this] def identifier: Parser[Token] = ident ^^ Identifier

  private[this] def stringLit: Parser[Token] = stringLiteral ^^ StringLiteral

  private[this] def parenGroup: Parser[Token] = "(" ~> rep(exp) <~ ")" ^^ ParenGroup

  private[this] def bracketGroup: Parser[Token] = "[" ~> rep(exp) <~ "]" ^^ BracketGroup

  private[this] def curlyGroup: Parser[Token] = "{" ~> rep(exp) <~ "}" ^^ CurlyGroup

  private[this] def wrappableExpressions: Parser[Token] = parenGroup | bracketGroup | curlyGroup | variable

  private[this] def exp: Parser[Token] = repeat | number | stringLit | identifier | wrappableExpressions

  def parseExpression: Parser[Token] = exp

  def parseProgram: Parser[List[Token]] = phrase(rep1(exp))

  def parseSlurped(s: String): ParseResult[List[Token]] = parseProgram(new CharSequenceReader(s))
}