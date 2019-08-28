package net.frodwith.jaque.library;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import net.frodwith.jaque.exception.ExitException;

import net.frodwith.jaque.runtime.ByteAtoms;
import net.frodwith.jaque.runtime.WordAtoms;

@ExportLibrary(value=NounLibrary.class, receiverType=Integer.class)
final class IntegerExports {
  @ExportMessage static int mug(Integer a) {
    // XX constants for 0/1/more?
    return ByteAtoms.mug(asBytes(a));
  }

  @ExportMessage static boolean isAtom(Integer a) {
    return true;
  }

  @ExportMessage static boolean fitsInBoolean(Integer a) {
    return ( 0 == a) || ( 1 == a );
  }

  @ExportMessage static boolean fitsInInt(Integer a) {
    return true;
  }

  @ExportMessage static boolean fitsInLong(Integer a) {
    return true;
  }

  @ExportMessage static boolean asBoolean(Integer a) throws ExitException {
    switch (a) {
      case 0: return false;
      case 1: return true;
      default:
        throw new ExitException("not a boolean");
    }
  }

  @ExportMessage static int asInt(Integer a) {
    return a;
  }

  @ExportMessage static long asLong(Integer a) {
    return a & 0xffffffffL;
  }

  @ExportMessage static byte[] asBytes(Integer a) {
    return isZero(a) ? new byte[]{} :
           ByteAtoms.fromWords(asWords(a), met(a, (byte)3));
  }

  @ExportMessage static int[] asWords(Integer a) {
    return isZero(a) ? new int[]{} : new int[]{a};
  }

  @ExportMessage static boolean isZero(Integer a) {
    return 0 == a;
  }

  @ExportMessage static Object inc(Integer a) {
    if ( 0 == ++a ) {
      return 0x100000000L;
    }
    return a;
  }

  @ExportMessage static int dec(Integer a) throws ExitException {
    if ( 0 == a ) {
      throw new ExitException("decrement underflow");
    }
    else {
      return a - 1;
    }
  }

  @ExportMessage static int met(Integer a, byte bloq) {
    int bits = 32 - Integer.numberOfLeadingZeros(a);
    int whole = bits >>> bloq;
    if ( 0 != (bits & (1 << bloq) - 1) ) {
      whole++;
    }
    return whole;
  }

  @ExportMessage static Object bex(Integer a) {
    if ( Integer.compareUnsigned(a, 32) < 0 ) {
      return 1 << a;
    }
    return WordAtoms.toAtom(WordAtoms.bex(a));
  }

  @ExportMessage static Object lsh(Integer a, byte bloq, int count) {
    return ( 0 == a ) ? 0 :
           WordAtoms.toAtom(WordAtoms.lsh(bloq, count, asWords(a)));
  }

  @ExportMessage static int rsh(Integer a, byte bloq, int count) {
    return ( ( Integer.compareUnsigned(bloq, 5) >= 0 &&
               Integer.compareUnsigned(count, 0) > 0 ) ||
             Integer.compareUnsigned(count, 32) >= 0 )
           ? 0
           : (a >>> (count << bloq));
  }

  @ExportMessage
  static int cut(Integer a, byte bloq, int index, int count) {
    if ( ( Integer.compareUnsigned(bloq, 5) >= 0 &&
           Integer.compareUnsigned(count, 0) > 0 ) ||
         Integer.compareUnsigned(count, 32) >= 0 )
    {
      return 0;
    }
    else {
      int shifted = a >>> (index << bloq);
      return ( Integer.compareUnsigned(count, 32) >= 0 )
             ? shifted
             : shifted & (count << bloq) - 1;
    }
  }

  @ExportMessage static class Con {
    @Specialization(guards = "bs.fitsInInt(b)",
                    limit = "3")
    static int conInt(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      return a | bs.asInt(b);
    }

    @Specialization
    static Object conAny(Integer a, Object b) throws ExitException
    {
      return WordAtoms.toAtom(
        WordAtoms.con(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }

  @ExportMessage static class Dis {
    @Specialization(guards = "bs.fitsInInt(b)",
                    limit = "3")
    static int disInt(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      return a & bs.asInt(b);
    }

    @Specialization
    static int disAny(Integer a, Object b) throws ExitException
    {
      // return a & (int)(long)bs.end(b, (byte)32);
      return WordAtoms.dis(asWords(a),
        NounLibrary.getUncached().asWords(b)
      )[0];
    }
  }

  @ExportMessage static class Add {
    @Specialization(guards = "bs.fitsInInt(b)",
                    rewriteOn = ArithmeticException.class,
                    limit = "3")
    static int addInt(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      int c = bs.asInt(b) + a;
      if ( Integer.compareUnsigned(c, a) >= 0 ) {
        return c;
      }
      throw new ArithmeticException();
    }

    @Specialization(guards = "bs.fitsInLong(b)",
                    rewriteOn = ArithmeticException.class,
                    replaces = { "addInt" },
                    limit = "3")
    static long addLong(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      long c = bs.asLong(b) + a;
      if ( Long.compareUnsigned(c, a) >= 0 ) {
        return c;
      }
      throw new ArithmeticException();
    }

    @Specialization(replaces = { "addLong" })
    static Object addAny(Integer a, Object b) throws ExitException
    {
      return WordAtoms.toAtom(
        WordAtoms.add(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }

  @ExportMessage static class Sub {
    @Specialization(guards = "bs.fitsInInt(b)",
                    limit = "3")
    static int subInt(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      int c = bs.asInt(b);
      switch ( Integer.compareUnsigned(a, c) ) {
        case 0:
          return 0;
        case 1:
          return a - c;
        default:
          throw new ExitException("subtract underflow");
      }
    }

    @Specialization
    static Object subAny(Integer a, Object b) throws ExitException
    {
      return WordAtoms.toAtom(
        WordAtoms.sub(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }

  @ExportMessage static class Mod {
    @Specialization(guards = "bs.fitsInInt(b)",
                    limit = "3")
    static int modInt(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      if ( bs.isZero(b) ) {
        throw new ExitException("mod 0");
      }

      return Integer.remainderUnsigned(a, bs.asInt(b));
    }

    @Specialization
    static int modAny(Integer a, Object b) throws ExitException
    {
      NounLibrary nouns = NounLibrary.getUncached();
      if ( nouns.isZero(b) ) {
        throw new ExitException("mod 0");
      }

      return WordAtoms.mod(asWords(a), nouns.asWords(b))[0];
    }
  }

  @ExportMessage static class Div {
    @Specialization(guards = "bs.fitsInInt(b)",
                    limit = "3")
    static int divInt(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      if ( bs.isZero(b) ) {
        throw new ExitException("div 0");
      }

      return Integer.divideUnsigned(a, bs.asInt(b));
    }

    @Specialization
    static int divAny(Integer a, Object b) throws ExitException
    {
      NounLibrary nouns = NounLibrary.getUncached();

      if ( nouns.isZero(b) ) {
        throw new ExitException("div 0");
      }

      return WordAtoms.div(asWords(a), nouns.asWords(b))[0];
    }
  }

  @ExportMessage static class Mul {
    @Specialization(guards = "bs.fitsInInt(b)",
                    rewriteOn = ArithmeticException.class,
                    limit = "3")
    static int mulInt(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      int c = bs.asInt(b);

      if ( (a < 0) || (c < 0) ) {
        // multiplyExact would get cute with negative numbers
        throw new ArithmeticException();
      }

      return Math.multiplyExact(a, c);
    }

    @Specialization(guards = "bs.fitsInLong(b)",
                    rewriteOn = ArithmeticException.class,
                    replaces = { "mulInt" },
                    limit = "3")
    static long mulLong(Integer a, Object b,
      @CachedLibrary("b") NounLibrary bs) throws ExitException
    {
      long c = asLong(a);
      long d = bs.asLong(b);

      if ( (c < 0L) || (d < 0L) ) {
        // multiplyExact would get cute with negative numbers
        throw new ArithmeticException();
      }

      return Math.multiplyExact(c, d);
    }

    @Specialization(replaces = { "mulLong" })
    static Object mulAny(Integer a, Object b) throws ExitException
    {
      return WordAtoms.toAtom(
        WordAtoms.mul(asWords(a), NounLibrary.getUncached().asWords(b))
      );
    }
  }
}
