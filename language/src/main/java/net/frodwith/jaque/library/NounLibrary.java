package net.frodwith.jaque.library;

import java.util.Iterator;

import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;

import net.frodwith.jaque.util.Path;
import net.frodwith.jaque.exception.ExitException;

@GenerateLibrary(assertions=NounAsserts.class)
@DefaultExport(LongExports.class)
@DefaultExport(IntegerExports.class)
public abstract class NounLibrary extends Library {
  private static final LibraryFactory<NounLibrary> 
    FACTORY = LibraryFactory.resolve(NounLibrary.class);

  public static LibraryFactory<NounLibrary> getFactory() {
    return FACTORY;
  }

  public static NounLibrary getUncached() {
    return FACTORY.getUncached();
  }

  public final Path axisPath(Object receiver)
    throws ExitException {
    final long len = NounLibrary.this.bitLength(receiver);
    if ( 0 == len ) {
      throw new ExitException("0 treated as path");
    }
    return new Path() {
      public Iterator<Boolean> iterator() {
        return new Iterator<Boolean>() {
          long n = len - 1;
          public boolean hasNext() {
            return n > 0;
          }
          public Boolean next() {
            try {
              return NounLibrary.this.testBit(receiver, --n);
            }
            catch ( ExitException e ) {
              CompilerDirectives.transferToInterpreter();
              throw new AssertionError("testBit failed on atom");
            }
          }
        };
      }
    };
  }

  public final boolean axisInHead(Object receiver) throws ExitException {
    return !axisInTail(receiver);
  }

  public final boolean axisInTail(Object receiver) throws ExitException {
    return testBit(receiver, bitLength(receiver) - 2);
  }

  @Abstract(ifExported = {"isAtom", "isCell"})
  public boolean isNoun(Object receiver) {
    return false;
  }

  @Abstract(ifExported = {"fitsInInt", "fitsInLong", "asIntArray"})
  public boolean isAtom(Object receiver) {
    return false;
  }

  @Abstract(ifExported = "isAtom")
  public long bitLength(Object receiver) throws ExitException {
    throw new ExitException("bitLength on non-atom");
  }

  @Abstract(ifExported = "isAtom")
  public boolean testBit(Object receiver, long index) throws ExitException {
    throw new ExitException("testBit on non-atom");
  }

  @Abstract(ifExported = "isAtom")
  public boolean fitsInInt(Object receiver) {
    return false;
  }

  @Abstract(ifExported = "isAtom")
  public boolean fitsInLong(Object receiver) {
    return false;
  }

  @Abstract(ifExported = "fitsInLong")
  public long asLong(Object receiver) {
    throw new AssertionError("treated as long without check");
  }

  @Abstract(ifExported = "isAtom")
  public boolean fitsInBoolean(Object receiver) {
    return false;
  }

  @Abstract(ifExported = "fitsInBoolean")
  public boolean asBoolean(Object receiver) throws ExitException {
    throw new ExitException("not a boolean");
  }

  @Abstract(ifExported = "fitsInInt")
  public int asInt(Object receiver) throws ExitException {
    throw new ExitException("not an int");
  }

  @Abstract(ifExported = "isAtom")
  public int[] asIntArray(Object receiver) throws ExitException {
    // TODO: assert atoms never exit
    throw new ExitException("not an atom");
  }

  @Abstract(ifExported = {"head", "tail"})
  public boolean isCell(Object receiver) {
    return false;
  }

  @Abstract(ifExported = "isCell")
  public Object head(Object receiver) throws ExitException {
    // TODO: assert cells don't actually exit
    CompilerDirectives.transferToInterpreter();
    throw new ExitException("not a cell");
  }

  @Abstract(ifExported = "isCell")
  public Object tail(Object receiver) throws ExitException {
    // TODO: assert cells don't actually exit
    CompilerDirectives.transferToInterpreter();
    throw new ExitException("not a cell");
  }

  public Object cleanup(Object receiver) {
    // optional API to return a "better" version of yourself, so DynamicCells
    // that later learned they were cores can return a Core, etc. This isn't
    // called automatically because it's potentially expensive, but can be
    // called by users of the library at appropriate moments (say, before
    // persisting an arvo kernel)
    return receiver;
  }

  public abstract int mug(Object receiver);

}
