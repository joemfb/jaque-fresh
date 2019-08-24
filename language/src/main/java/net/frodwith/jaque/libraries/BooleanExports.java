package net.frodwith.jaque.library;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import net.frodwith.jaque.exception.ExitException;

import net.frodwith.jaque.library.NounLibrary;
import net.frodwith.jaque.runtime.WordAtoms;

@ExportLibrary(value=NounLibrary.class, receiverType=Boolean.class)
final class BooleanExports {
  @ExportMessage static int mug(Boolean a) {
    return a ? 0x79ff04e8 : 0x715c2a60;
  }

  @ExportMessage static boolean isAtom(Boolean a) {
    return true;
  }

  @ExportMessage static boolean fitsInBoolean(Boolean a) {
    return true;
  }

  @ExportMessage static boolean fitsInInt(Boolean a) {
    return true;
  }

  @ExportMessage static boolean fitsInLong(Boolean a) {
    return true;
  }

  @ExportMessage static boolean asBoolean(Boolean a) {
    return a;
  }

  @ExportMessage static int asInt(Boolean a) {
    return a ? 0 : 1;
  }

  @ExportMessage static long asLong(Boolean a) {
    return a ? 0L : 1L;
  }

  @ExportMessage static byte[] asBytes(Boolean a) {
    return a ? new byte[]{} : new byte[]{ (byte)1 };
  }

  @ExportMessage static int[] asWords(Boolean a) {
    return a ? new int[]{} : new int[]{1};
  }

  @ExportMessage static boolean isZero(Boolean a) {
    return a;
  }

  @ExportMessage static Object inc(Boolean a) {
    return a ? false : 2;
  }

  @ExportMessage static boolean dec(Boolean a) throws ExitException {
    if ( a ) {
      throw new ExitException("decrement underflow");
    }
    else {
      return true;
    }
  }

  @ExportMessage static int met(Boolean a, byte bloq) {
    return a ? 0 : 1;
  }

  @ExportMessage static int bex(Boolean a) {
    return a ? 1 : 2;
  }

  @ExportMessage static Object lsh(Boolean a, byte bloq, int count) {
    return a ? 0 : WordAtoms.toAtom(WordAtoms.lsh(bloq, count, asWords(a)));
  }

  @ExportMessage static boolean rsh(Boolean a, byte bloq, int count) {
    return ( 0 == count ) ? a : true;
  }

  @ExportMessage
  static boolean cut(Boolean a, byte bloq, int index, int count) {
    return ( 0 == count ) ? a : true;
  }

  @ExportMessage static boolean con(Boolean a, Object b,
    @CachedLibrary(limit = "3") NounLibrary bs)
  {
    return a && bs.isZero(b);
  }

  @ExportMessage static boolean dis(Boolean a, Object b,
    @CachedLibrary(limit = "3") NounLibrary bs)
  {
    return !a || !bs.isZero(b);
  }

  @ExportMessage static Object add(Boolean a, Object b,
      @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    return a ? b : bs.inc(b);
  }

  @ExportMessage static Object sub(Boolean a, Object b,
      @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    if ( bs.isZero(b) ) {
      return a;
    }
    else if ( !a && bs.isZero(bs.dec(b)) ) {
      return true;
    }
    else {
      throw new ExitException("subtract underflow");
    }
  }

  @ExportMessage static boolean mod(Boolean a, Object b,
      @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    if ( bs.isZero(b) ) {
      throw new ExitException("mod 0");
    }
    else {
      return !( !a && !bs.isZero(bs.dec(b)) );
    }
  }

  @ExportMessage static boolean div(Boolean a, Object b,
      @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    if ( bs.isZero(b) ) {
      throw new ExitException("div 0");
    }
    else {
      return !( !a && bs.isZero(bs.dec(b)) );
    }
  }

  @ExportMessage static Object mul(Boolean a, Object b) {
    return a ? a : b;
  }
}
