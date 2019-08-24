package net.frodwith.jaque.library;

import java.util.Iterator;

import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;

// import net.frodwith.jaque.util.Path;
// import net.frodwith.jaque.data.Core;
// import net.frodwith.jaque.data.ConstantCell;
import net.frodwith.jaque.exception.ExitException;

@GenerateLibrary() //assertions=NounAsserts.class
@DefaultExport(BooleanExports.class)
@DefaultExport(IntegerExports.class)
@DefaultExport(LongExports.class)
public abstract class NounLibrary extends Library {
  private static final LibraryFactory<NounLibrary>
    FACTORY = LibraryFactory.resolve(NounLibrary.class);

  public static LibraryFactory<NounLibrary> getFactory() {
    return FACTORY;
  }

  public static NounLibrary getUncached() {
    return FACTORY.getUncached();
  }

  public abstract int mug(Object receiver);

  public boolean isCell(Object receiver) {
    return false;
  }

  public boolean isAtom(Object receiver) {
    return false;
  }

  public boolean fitsInBoolean(Object receiver) {
    return false;
  }

  public boolean fitsInInt(Object receiver) {
    return false;
  }

  public boolean fitsInLong(Object receiver) {
    return false;
  }

  public boolean asBoolean(Object receiver) throws ExitException {
    throw new ExitException("not a boolean");
  }

  public int asInt(Object receiver) throws ExitException {
    throw new ExitException("not an int");
  }

  public long asLong(Object receiver) throws ExitException {
    throw new ExitException("not a long");
  }

  public byte[] asBytes(Object receiver) throws ExitException {
    throw new ExitException("not an atom");
  }

  public int[] asWords(Object receiver) throws ExitException {
    throw new ExitException("not an atom");
  }

  public boolean isZero(Object receiver) {
    return false;
  }

  public Object inc(Object receiver) throws ExitException {
    throw new ExitException("increment a cell");
  }

  public Object dec(Object receiver) throws ExitException {
    throw new ExitException("decrement a cell");
  }

  public int met(Object receiver, byte bloq) throws ExitException {
    throw new ExitException("met on cell");
  }

  public Object bex(Object receiver) throws ExitException {
    throw new ExitException("bex a cell");
  }

  public Object lsh(Object receiver, byte bloq, int count)
  throws ExitException
  {
    throw new ExitException("lsh on cell");
  }

  public Object rsh(Object receiver, byte bloq, int count)
  throws ExitException
  {
    throw new ExitException("rsh on cell");
  }

  public Object cut(Object receiver, byte bloq, int index, int count)
  throws ExitException
  {
    throw new ExitException("cut on cell");
  }

  public Object con(Object a, Object b) throws ExitException {
    throw new ExitException("con cell(s)");
  }

  public Object dis(Object a, Object b) throws ExitException {
    throw new ExitException("dis cell(s)");
  }

  public Object add(Object a, Object b) throws ExitException {
    throw new ExitException("add cell(s)");
  }

  public Object sub(Object a, Object b) throws ExitException {
    throw new ExitException("sub cell(s)");
  }

  public Object mod(Object a, Object b) throws ExitException {
    throw new ExitException("mod cell(s)");
  }

  public Object div(Object a, Object b) throws ExitException {
    throw new ExitException("div cell(s)");
  }

  public Object mul(Object a, Object b) throws ExitException {
    throw new ExitException("mul cell(s)");
  }
}
