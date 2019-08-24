package net.frodwith.jaque.data;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import java.io.StringWriter;
import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;

import net.frodwith.jaque.exception.ExitException;
import net.frodwith.jaque.exception.FailError;
import net.frodwith.jaque.runtime.Mug;
import net.frodwith.jaque.runtime.ByteAtoms;
import net.frodwith.jaque.runtime.WordAtoms;
import net.frodwith.jaque.runtime.Equality;
import net.frodwith.jaque.runtime.HoonMath;
import net.frodwith.jaque.runtime.GrainSilo;
import net.frodwith.jaque.interop.InteropArray;
import net.frodwith.jaque.printer.SimpleAtomPrinter;

import net.frodwith.jaque.library.NounLibrary;

@ExportLibrary(NounLibrary.class)
public final class BigAtomL {
  public static final BigAtomL MINIMUM = new BigAtomL(new int[] {0, 0, 1});

  public int[] words;
  public int mug;

  public BigAtomL(int[] words) {
    // smaller atoms must be represented by longs
    assert(words.length > 2);

    this.words = words;
    this.mug = 0;
  }

  @ExportMessage int mug() {
    if ( 0 == mug ) {
      mug = ByteAtoms.mug(asBytes());
    }
    return mug;
  }

  @ExportMessage boolean isAtom() {
    return true;
  }

  @ExportMessage byte[] asBytes() {
    return ByteAtoms.fromWords(words, met((byte)3));
  }

  @ExportMessage int[] asWords() {
    return words;
  }

  @ExportMessage BigAtomL inc() {
    return new BigAtomL(WordAtoms.inc(words));
  }

  @ExportMessage Object dec() {
    return WordAtoms.toAtom(WordAtoms.dec(words));
  }

  @ExportMessage int met(byte bloq)  {
    return WordAtoms.met(bloq, words);
  }

  @ExportMessage Object bex() {
    CompilerDirectives.transferToInterpreter();
    throw new FailError("bex too big");
  }

  @ExportMessage Object lsh(byte bloq, int count)
  throws ExitException
  {
    return WordAtoms.toAtom(WordAtoms.lsh(bloq, count, words));
  }

  @ExportMessage Object rsh(byte bloq, int count)
  throws ExitException
  {
    return WordAtoms.toAtom(WordAtoms.rsh(bloq, count, words));
  }

  @ExportMessage Object cut(byte bloq, int index, int stride)
  throws ExitException
  {
    return WordAtoms.toAtom(WordAtoms.cut(bloq, index, stride, words));
  }

  @ExportMessage BigAtomL con(Object b,
    @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    return new BigAtomL(WordAtoms.con(words, bs.asWords(b)));
  }

  @ExportMessage Object dis(Object b,
    @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    return WordAtoms.toAtom(WordAtoms.dis(words, bs.asWords(b)));
  }

  @ExportMessage BigAtomL add(Object b,
    @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    return new BigAtomL(WordAtoms.add(words, bs.asWords(b)));
  }

  @ExportMessage Object sub(Object b,
    @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    return WordAtoms.toAtom(WordAtoms.sub(words, bs.asWords(b)));
  }

  @ExportMessage Object mod(Object b,
    @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    return WordAtoms.toAtom(WordAtoms.mod(words, bs.asWords(b)));
  }

  @ExportMessage Object div(Object b,
    @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    return WordAtoms.toAtom(WordAtoms.div(words, bs.asWords(b)));
  }

  @ExportMessage Object mul(Object b,
    @CachedLibrary(limit = "3") NounLibrary bs) throws ExitException
  {
    return WordAtoms.toAtom(WordAtoms.mul(words, bs.asWords(b)));
  }

  /* for debugging
  public String pretty() {
    StringWriter out = new StringWriter();
    try {
      SimpleAtomPrinter.raw(out, words, 16, 0);
      return out.toString();
    }
    catch ( IOException e ) {
      return "noun misprint";
    }
  }
  */
}
