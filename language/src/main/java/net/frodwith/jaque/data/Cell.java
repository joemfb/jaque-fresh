package net.frodwith.jaque.data;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.IOException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.ForeignAccess;

import net.frodwith.jaque.runtime.Mug;
import net.frodwith.jaque.runtime.Equality;
import net.frodwith.jaque.runtime.NockContext;
import net.frodwith.jaque.dashboard.Location;
import net.frodwith.jaque.printer.MappedNounPrinter;

import net.frodwith.jaque.exception.ExitException;

/* Because we must use Object fields for the head and the tail to accomodate the atom
 * types that we are using, it is unfortunately possible to construct a cell of any
 * arbitrary Java objects (including, sometimes frustratingly, cells of ints instead of
 * longs etc.). In particular, suffix literal atoms with L (1L, etc) religiously to avoid
 * this. No real checking is done at runtime.
 */

public final class Cell implements TruffleObject, Serializable {
  // head and tail are not final because we set them during unifying equals
  public Object head, tail;
  private Object meta;
  
  public Cell(Object head, Object tail) {
    this.head = head;
    this.tail = tail;
    this.meta = null;
  }

  public static Cell require(Object o) throws ExitException {
    if ( o instanceof Cell ) {
      return (Cell) o;
    }
    else {
      CompilerDirectives.transferToInterpreter();
      throw new ExitException("cell required");
    }
  }

  public int mug() {
    int mug;
    if ( null == meta ) {
      meta = mug = Mug.calculate(this);
    }
    else if ( meta instanceof Integer ) {
      mug = (int) meta;
    }
    else {
      mug = ((CellMeta)meta).mug();
    }
    return mug;
  }

  @Override
  public int hashCode() {
    return mug();
  }

  public void unifyMeta(Cell other) {
    Object om = other.meta;
    if ( null == meta ) {
      if ( null != om ) {
        meta = om;
      }
    }
    else if ( null == om ) {
      if ( null != meta ) {
        other.meta = meta;
      }
    }
    else if ( meta instanceof Integer ) {
      if ( !(om instanceof Integer) ) {
        ((CellMeta)om).setMug((int) meta);
        meta = om;
      }
    }
    else if ( om instanceof Integer ) {
      if ( !(meta instanceof Integer) ) {
        ((CellMeta)meta).setMug((int) om);
        other.meta = meta;
      }
    }
    else {
      CellMeta mine = (CellMeta) meta;
      CellMeta theirs = (CellMeta) om;
      mine.unify(theirs);
      other.meta = mine;
    }
  }

  public int cachedMug() {
    if ( null == meta ) {
      return 0;
    }
    else if ( meta instanceof Integer ) {
      return (int) meta;
    }
    else {
      return ((CellMeta)meta).cachedMug();
    }
  }

  public void unifyHeads(Cell other) {
    if ( head instanceof Cell ) {
      ((Cell)head).unifyMeta((Cell)other.head);
    }
    else if ( head instanceof BigAtom ) {
      ((BigAtom)other.head).words = ((BigAtom)head).words;
    }
    other.head = head;
  }

  public void unifyTails(Cell other) {
    if ( tail instanceof Cell ) {
      ((Cell)tail).unifyMeta((Cell)other.tail);
    }
    else if ( tail instanceof BigAtom ) {
      ((BigAtom)other.tail).words = ((BigAtom)tail).words;
    }
    other.tail = tail;
  }

  public static boolean unequalMugs(Cell a, Cell b) {
    int am, bm;
    if ( 0 == (am = a.cachedMug()) ) {
      return false;
    }
    if ( 0 == (bm = b.cachedMug()) ) {
      return false;
    }
    return am != bm;
  }

  public CellMeta getMeta(NockContext context) {
    CellMeta cm;
    if ( null == this.meta ) {
      cm = new CellMeta(context, this, 0);
      this.meta = cm;
    }
    else if ( meta instanceof Integer ) {
      cm = new CellMeta(context, this, (int) meta);
      this.meta = cm;
    }
    else {
      cm = (CellMeta) meta;
      /* it is possible that the same cell will be run in different contexts. If
       * two threads with different contexts are running on the same shared
       * cell, there is potential for unproductive clobbering. */
      if ( !cm.forContext(context) ) {
        cm = new CellMeta(context, this, cm.cachedMug());
        this.meta = cm;
      }
    }
    return cm;
  }

  public boolean knownAt(Location location) {
    // XX pass me a context, i need to check if they match
    return (meta instanceof CellMeta) && ((CellMeta)meta).knownAt(location);
  }

  public void copyObject(Cell from, Axis written) {
    if ( null != from.meta && from.meta instanceof CellMeta ) {
      CellMeta fromMeta = (CellMeta) from.meta;
      fromMeta.writeObject(this, written);
    }
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof Cell) && Equality.equals(this, (Cell) o);
  }

  public ForeignAccess getForeignAccess() {
    return CellMessageResolutionForeign.ACCESS;
  }

/* for debugging */
  public String pretty() {
    StringWriter w = new StringWriter();
    try {
      MappedNounPrinter.print(w, this);
    }
    catch ( ExitException | IOException e ) {
    }
    return w.toString();
  }
}
