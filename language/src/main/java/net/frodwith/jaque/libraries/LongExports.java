package net.frodwith.jaque.library;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import net.frodwith.jaque.exception.ExitException;
import net.frodwith.jaque.exception.FailError;

import net.frodwith.jaque.data.BigAtomL;
import net.frodwith.jaque.runtime.ByteAtoms;
import net.frodwith.jaque.runtime.WordAtoms;

@ExportLibrary(value=NounLibrary.class, receiverType=Long.class)
final class LongExports {
  @ExportMessage static int mug(Long a) {
    // XX constants for 0/1/more?
    return ByteAtoms.mug(asBytes(a));
  }

  @ExportMessage static boolean isAtom(Long a) {
    return true;
  }

  @ExportMessage static boolean fitsInBoolean(Long a) {
    return ( 0L == a) || ( 1L == a );
  }

  @ExportMessage static boolean fitsInInt(Long a) {
    return Long.compareUnsigned(a, 0xFFFFFFFFL) <= 0;
  }

  @ExportMessage static boolean fitsInLong(Long a) {
    return true;
  }

  @ExportMessage static boolean asBoolean(Long a) throws ExitException {
    if ( 0L == a ) {
      return false;
    }
    else if ( 1L == a ) {
      return true;
    }
    else {
      throw new ExitException("not a boolean");
    }
  }

  @ExportMessage static int asInt(Long a) throws ExitException {
    if ( fitsInInt(a) ) {
      return (int)(long)a;
    }
    else {
      throw new ExitException("not an integer");
    }
  }

  @ExportMessage static long asLong(Long a) {
    return a;
  }

  @ExportMessage static byte[] asBytes(Long a) {
    return isZero(a) ? new byte[]{} :
           ByteAtoms.fromWords(asWords(a), met(a, (byte)3));
  }

  @ExportMessage static int[] asWords(Long a) {
    return isZero(a) ? new int[]{} :
           WordAtoms.fromLong(a);
  }

  @ExportMessage static boolean isZero(Long a) {
    return 0L == a;
  }

  @ExportMessage static Object inc(Long a) {
    if ( 0L == ++a ) {
      return BigAtomL.MINIMUM;
    }
    return a;
  }

  @ExportMessage static long dec(Long a) throws ExitException {
    if ( 0L == a ) {
      throw new ExitException("decrement underflow");
    }
    else {
      return a - 1L;
    }
  }

  @ExportMessage static int met(Long a, byte bloq) {
    int bits = 64 - Long.numberOfLeadingZeros(a);
    int whole = bits >>> bloq;
    if ( 0 != (bits & (1 << bloq) - 1) ) {
      whole++;
    }
    return whole;
  }

  @ExportMessage static Object bex(Long a) throws ExitException {
    if ( Long.compareUnsigned(a, 64) < 0 ) {
      return 1L << a;
    }
    else if ( !fitsInInt(a) ) {
      CompilerDirectives.transferToInterpreter();
      throw new FailError("bex too big");
    }

    return WordAtoms.toAtom(WordAtoms.bex(asInt(a)));
  }

  @ExportMessage static Object lsh(Long a, byte bloq, int count) {
    return ( 0 == a ) ? 0 :
           WordAtoms.toAtom(WordAtoms.lsh(bloq, count, asWords(a)));
  }

  @ExportMessage static long rsh(Long a, byte bloq, int count) {
    return ( ( Long.compareUnsigned(bloq, 6) >= 0 &&
               Long.compareUnsigned(count, 0) > 0 ) ||
             Long.compareUnsigned(count, 64) >= 0 )
           ? 0
           : (a >>> (count << bloq));
  }

  @ExportMessage
  static long cut(Long a, byte bloq, int index, int count) {
    if ( ( Long.compareUnsigned(bloq, 6) >= 0 &&
           Long.compareUnsigned(count, 0) > 0 ) ||
         Long.compareUnsigned(count, 64) >= 0 )
    {
      return 0;
    }
    else {
      long shifted = a >>> (index << bloq);
      return ( Long.compareUnsigned(count, 64) >= 0 )
             ? shifted
             : shifted & (count << bloq) - 1;
    }
  }

  @ExportMessage static class Con {
    @Specialization(guards = "bs.fitsInLong(b)",
                    limit = "3")
    static long conLong(Long a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      return a | bs.asLong(b);
    }

    @Specialization
    static Object conAny(Long a, Object b) throws ExitException
    {
      return WordAtoms.toAtom(
        WordAtoms.con(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }

  @ExportMessage static class Dis {
    @Specialization(guards = "bs.fitsInLong(b)",
                    limit = "3")
    static long disLong(Long a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      return a & bs.asLong(b);
    }

    @Specialization
    static long disAny(Long a, Object b) throws ExitException
    {
      // return a & (long)bs.end(b, (byte)64);
      return (long)WordAtoms.toAtom(
        WordAtoms.dis(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }

  @ExportMessage static class Add {
    @Specialization(guards = "bs.fitsInLong(b)",
                    rewriteOn = ArithmeticException.class,
                    limit = "3")
    static long addLong(Long a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      long c = bs.asLong(b) + a;
      if ( Long.compareUnsigned(c, a) >= 0 ) {
        return c;
      }
      throw new ArithmeticException();
    }

    @Specialization(replaces = { "addLong" })
    static Object addAny(Long a, Object b) throws ExitException
    {
      return WordAtoms.toAtom(
        WordAtoms.add(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }

  @ExportMessage static class Sub {
    @Specialization(guards = "bs.fitsInLong(b)",
                    limit = "3")
    static long subLong(Long a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      long c = bs.asLong(b);
      int d = Long.compareUnsigned(a, c);

      if ( 0 == d) {
        return 0L;
      }
      else if ( d > 0 ) {
        return a - c;
      }
      else {
        throw new ExitException("subtract underflow");
      }
    }

    @Specialization
    static long subAny(Long a, Object b) throws ExitException
    {
      return (long)WordAtoms.toAtom(
        WordAtoms.sub(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }

  @ExportMessage static class Mod {
    @Specialization(guards = "bs.fitsInLong(b)",
                    limit = "3")
    static long modLong(Long a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      if ( bs.isZero(b) ) {
        throw new ExitException("mod 0");
      }

      return Long.remainderUnsigned(a, bs.asLong(b));
    }

    @Specialization
    static long modAny(Long a, Object b) throws ExitException
    {
      NounLibrary nouns = NounLibrary.getUncached();
      if ( nouns.isZero(b) ) {
        throw new ExitException("mod 0");
      }

      return (long)WordAtoms.toAtom(
        WordAtoms.mod(asWords(a), nouns.asWords(b))
      );
    }
  }

  @ExportMessage static class Div {
    @Specialization(guards = "bs.fitsInLong(b)",
                    limit = "3")
    static long divLong(Long a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      if ( bs.isZero(b) ) {
        throw new ExitException("div 0");
      }

      return Long.divideUnsigned(a, bs.asLong(b));
    }

    @Specialization
    static long divAny(Long a, Object b) throws ExitException
    {
      NounLibrary nouns = NounLibrary.getUncached();

      if ( nouns.isZero(b) ) {
        throw new ExitException("div 0");
      }

      return (long)WordAtoms.toAtom(
        WordAtoms.div(asWords(a), nouns.asWords(b))
      );
    }
  }

  @ExportMessage static class Mul {
    @Specialization(guards = "bs.fitsInLong(b)",
                    rewriteOn = ArithmeticException.class,
                    limit = "3")
    static long mulLong(Long a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      long c = bs.asLong(b);

      if ( (a < 0L) || (c < 0L) ) {
        // multiplyExact would get cute with negative numbers
        throw new ArithmeticException();
      }

      return Math.multiplyExact(a, c);
    }

    @Specialization(replaces = { "mulLong" })
    static Object mulAny(Long a, Object b) throws ExitException
    {
      return WordAtoms.toAtom(
        WordAtoms.mul(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }
}
