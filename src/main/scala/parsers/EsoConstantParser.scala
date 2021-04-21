package parsers

import scala.util.control.TailCalls.{TailRec, done, tailcall}

class EsoConstantParser[A,+B](parser: => EsoParser[A], v: => B) extends EsoParser[B]{
  private lazy val p = parser
  
  def apply(inp: String): EsoParseRes[B] = applyByTramp(inp)
  
  override def ^^^[C](v2: => C): EsoParser[C] = EsoConstantParser(p, v2)
  
  override def tramp[AA >: B, C](inp: EsoParserInput, start_ind: Int)(cc: ParserContinuation[AA, C]): TailRec[ParseTrampResult[C]] = {
    tailcall(
      p.tramp(inp, start_ind)(
        pres =>
          pres.flatMapAll{
            case (_, pi, ps, pe) =>
              done(EsoParsedTramp(v, pi, ps, pe))})) flatMap cc}
}
object EsoConstantParser{
  def apply[A,B](p: => EsoParser[A], v: => B): EsoConstantParser[A,B] = new EsoConstantParser(p, v)
}