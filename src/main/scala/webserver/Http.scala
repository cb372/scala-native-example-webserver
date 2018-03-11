package webserver

import fastparse.all._
import fastparse.core.Parsed.{Failure, Success}

object Http {

  case class StartLine(httpMethod: String, requestTarget: String, httpVersion: String)
  case class Header(key: String, value: String)
  case class HttpRequest(startLine: StartLine, headers: Seq[Header])

  /*
  Example request:

  GET / HTTP/1.1
  Host: localhost:7000
  User-Agent: curl/7.54.0
  Accept: foo/bar

  */

  /*
  https://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2.2

  "HTTP/1.1 defines the sequence CR LF as the end-of-line marker for all protocol elements except the entity-body"
   */

  private val httpMethod    = P ( CharPred(CharPredicates.isUpper).rep(min = 1).! )
  private val requestTarget = P ( CharsWhile(_ != ' ').! )
  private val httpVersion   = P ( "HTTP/" ~ CharsWhileIn(List('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.')).! )

  private val startLine: Parser[StartLine] =
    P( httpMethod ~ " " ~ requestTarget ~ " " ~ httpVersion ~ "\r\n" ) map StartLine.tupled

  private val header: Parser[Header] =
    P ( CharsWhile(_ != ':').! ~ ": " ~ CharsWhile(_ != '\r').! ~ "\r\n" ) map Header.tupled

  private val startLineAndHeaders = P ( startLine ~ header.rep ) map {
    case (sl, headers) => HttpRequest(sl, headers)
  }

  def parseRequest(raw: String): Either[String, HttpRequest] = startLineAndHeaders.parse(raw) match {
    case Success(parsedRequest, _) => Right(parsedRequest)
    case f: Failure[_, _] => Left(f.toString)
  }

}
