package parsers

import scala.util.control.TailCalls.{TailRec, done, tailcall}

class EsoLongestMatchParser[+A](parser1: => EsoParser[A], parser2: => EsoParser[A]) extends EsoParser[A] {
  private lazy val p = parser1
  private lazy val q = parser2
  
  def apply(inp: String): EsoParseRes[A] = applyByTramp(inp)
  
  override def tramp[AA >: A, B](inp: EsoParserInput, start_ind: Int)(cc: ParserContinuation[AA, B]): TailRec[ParseTrampResult[B]] = {
    tailcall(
      p.tramp(inp, start_ind)(
        pres =>
          tailcall(
            q.tramp(pres.inp, start_ind)(
              qres =>
                if(pres.length >= qres.length && pres.passed) // Gotta be a better way to structure these...
                  tailcall(cc(pres.withInp(qres.inp)) flatMap (res =>
                    if(res.passed) done(res)
                    else tailcall(cc(qres.withInp(res.inp)))))
                else
                  tailcall(cc(qres) flatMap (res =>
                    if(res.passed) done(res)
                    else tailcall(cc(pres.withInp(res.inp)))))))))}
}
object EsoLongestMatchParser{
  def apply[A](p: => EsoParser[A], q: => EsoParser[A]): EsoLongestMatchParser[A] = new EsoLongestMatchParser(p, q)
}