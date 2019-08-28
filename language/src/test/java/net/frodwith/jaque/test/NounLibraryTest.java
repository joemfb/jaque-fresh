package net.frodwith.jaque.test;

import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.java.lang.IntegerGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import java.util.Arrays;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;

import org.graalvm.polyglot.Context;
import net.frodwith.jaque.library.NounLibrary;
import net.frodwith.jaque.exception.ExitException;
import net.frodwith.jaque.exception.FailError;
import net.frodwith.jaque.data.BigAtomL;
import net.frodwith.jaque.data.BigAtom;
import net.frodwith.jaque.runtime.Atom;
import net.frodwith.jaque.runtime.Cords;
import net.frodwith.jaque.runtime.HoonMath;
import net.frodwith.jaque.runtime.ByteAtoms;
import net.frodwith.jaque.runtime.WordAtoms;
import net.frodwith.jaque.printer.SimpleAtomPrinter;

import net.frodwith.jaque.test.quickcheck.AtomLGenerator;
import net.frodwith.jaque.test.quickcheck.NounLGenerator;

@RunWith(JUnitQuickcheck.class)
public class NounLibraryTest {
  Context context;
  NounLibrary nouns = NounLibrary.getUncached();

  // XX Move into Cords
  private Object fromString(String s) {
    Object a = Cords.fromString(s);

    if (a instanceof Long) {
      return a;
    }
    else {
      return new BigAtomL(((BigAtom)a).words);
    }
  }

  private String toString(BigAtomL a) throws ExitException {
    try {
      return new String(nouns.asBytes(a), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException();
    }
  }

  // use noun equality
  private void assertNounEquals(Object a, Object b) throws ExitException {
    // XX wtf?
    if ( nouns.isZero(a) && nouns.isZero(b) ) {
      return;
    }

    int[] a_w = nouns.asWords(a);
    int[] b_w = nouns.asWords(b);

    if ( !Arrays.equals(a_w, b_w) ) {
      String message;
      try {
        StringWriter w = new StringWriter();

        w.write("0x");
        SimpleAtomPrinter.raw(w, a_w, 16, 4);
        w.write(" does not equal 0x");
        SimpleAtomPrinter.raw(w, b_w, 16, 4);
        w.flush();
        message = w.toString();
      }
      catch(IOException e) {
        message = "not equal";
        System.out.println(e);
      }

      assertTrue(message, false);
    }
  }

  @Before
  public void init() {
    // libraries should have an active context,
    // otherwise @CachedContext for example will not function
    context = Context.create();
    context.enter();
    context.initialize("nock");
  }

  @After
  public void dispose() {
    context.leave();
    context.close();
  }

  @Test
  public void testBoolMug() {
    assertEquals(0x79ff04e8, nouns.mug(true));
    assertEquals(0x715c2a60, nouns.mug(false));
  }

  @Test
  public void testIntMug() {
    assertEquals(0x79ff04e8, nouns.mug(0));
    assertEquals(0x715c2a60, nouns.mug(1));
    assertEquals(0x643849c6, nouns.mug(42));
  }

  @Test
  public void testLongMug() {
    assertEquals(0x79ff04e8, nouns.mug(0L));
    assertEquals(0x715c2a60, nouns.mug(1L));
    assertEquals(0x643849c6, nouns.mug(42L));
  }

  @Test
  public void testBigMug() throws ExitException {
    Object a = new BigAtomL(new int[] { 0xdeadbeef, 0xbeefdead, 0xfeedbeef });
    assertEquals(0x601265fc, nouns.mug(a));

    a = nouns.lsh(nouns.add(nouns.bex(212L),
                            fromString("abcdefjhijklmnopqrstuvwxyz")), (byte)3, 1);
    assertEquals(0x34d08717, nouns.mug(a));

    assertEquals(0x4d441035, nouns.mug(fromString("Hello, world!")));
    assertEquals(0x64dfda5c,
      nouns.mug(fromString("xxxxxxxxxxxxxxxxxxxxxxxxxxxx")));
  }

  //  XX move to assertions
  // @Property(trials=8192)
  // public void testExcludeAtomCell(@From(NounLGenerator.class) Object noun) {
  //   assertEquals(true, nouns.isAtom(noun) != nouns.isCell(noun));
  // }

  @Property
  public void testWordsRoundtrip(@From(AtomLGenerator.class) Object a)
    throws ExitException
  {
    assertNounEquals(a, WordAtoms.toAtom(nouns.asWords(a)));
  }

  @Property
  public void testBytesRoundtrip(@From(AtomLGenerator.class) Object a)
    throws ExitException
  {
    assertNounEquals(a, ByteAtoms.toAtom(nouns.asBytes(a)));
  }

  // XX test fitsIn* / as* / isZero ?

  @Test
  public void testBoolInc() throws ExitException {
    assertEquals(false, nouns.inc(true));
    assertEquals(2, nouns.inc(false));
  }

  @Test
  public void testIntInc() throws ExitException {
    assertEquals(1, nouns.inc(0));
    assertEquals(2, nouns.inc(1));
    assertEquals(0x100000000L, nouns.inc(0xffffffff));
  }

  @Test
  public void testLongInc() throws ExitException {
    assertEquals(1L, nouns.inc(0L));
    assertEquals(2L, nouns.inc(1L));
    assertEquals(true,
     nouns.inc(0xffffffffffffffffL) instanceof BigAtomL
    );
  }

  @Test
  public void testBigInc() throws ExitException {
    assertEquals(true, "Iello, world!".equals(
      toString((BigAtomL)nouns.inc(fromString("Hello, world!")))
    ));
  }

  @Test(expected = ExitException.class)
  public void testBoolDecUnderflow() throws ExitException {
    nouns.dec(true);
  }

  @Test
  public void testBoolDec() throws ExitException {
    assertEquals(true, nouns.dec(false));
  }

  @Test(expected = ExitException.class)
  public void testIntDecUnderflow() throws ExitException {
    nouns.dec(0);
  }

  @Test
  public void testIntDec() throws ExitException {
    assertEquals(0, nouns.dec(1));
    assertEquals(1, nouns.dec(2));
  }

  @Test(expected = ExitException.class)
  public void testLongDecUnderflow() throws ExitException {
    nouns.dec(0L);
  }

  @Test
  public void testLongDec() throws ExitException {
    assertEquals(0L, nouns.dec(1L));
    assertEquals(1L, nouns.dec(2L));
  }

  @Test
  public void testBigDec() throws ExitException {
    assertNounEquals(0xffffffffffffffffL, nouns.dec(BigAtomL.MINIMUM));

    Object a = new BigAtomL(new int[]{ 0, 0, 32 });
    Object b = new BigAtomL(new int[]{ 0xffffffff, 0xffffffff, 31 });
    assertNounEquals(b, nouns.dec(a));
  }

  @Property
  public void testIncDec(@From(AtomLGenerator.class) Object a)
    throws ExitException
  {
    assertNounEquals(a, nouns.dec(nouns.inc(a)));
  }

  @Test
  public void testBoolMet() throws ExitException {
    assertEquals(0, nouns.met(true, (byte)0));
    assertEquals(0, nouns.met(true, (byte)1));
    assertEquals(0, nouns.met(true, (byte)2));
    assertEquals(1, nouns.met(false, (byte)0));
    assertEquals(1, nouns.met(false, (byte)1));
    assertEquals(1, nouns.met(false, (byte)2));
  }

  @Test
  public void testIntMet() throws ExitException {
    assertEquals(0, nouns.met(0, (byte)0));
    assertEquals(1, nouns.met(1, (byte)0));
    assertEquals(2, nouns.met(16, (byte)2));
  }

  @Test
  public void testLongMet() throws ExitException {
    assertEquals(0, nouns.met(0L, (byte)0));
    assertEquals(1, nouns.met(1L, (byte)0));
    assertEquals(2, nouns.met(16L, (byte)2));
  }

  @Test
  public void testBigMet() throws ExitException {
    assertEquals(13, nouns.met(fromString("Hello, world!"), (byte)3));
  }

  @Test
  public void testBoolBex() throws ExitException {
    assertEquals(1, nouns.bex(true));
    assertEquals(2, nouns.bex(false));
  }

  @Test
  public void testIntBex() throws ExitException {
    assertEquals(1, nouns.bex(0));
    assertEquals(2, nouns.bex(1));
    assertEquals(8, nouns.bex(3));
    assertEquals(0x100000000L, nouns.bex(32));
  }

  @Test
  public void testLongBex() throws ExitException {
    assertEquals(1L, nouns.bex(0L));
    assertEquals(2L, nouns.bex(1L));
    assertEquals(0x8000000000000000L, nouns.bex(63L));
    assertNounEquals(BigAtomL.MINIMUM, nouns.bex(64L));
    assertNounEquals(nouns.mul(32L, BigAtomL.MINIMUM), nouns.bex(69L));
  }

  @Test(expected = FailError.class)
  public void testBigBex() throws ExitException {
    nouns.bex(fromString("Hello, world!"));
  }

  @Property
  public void bexMet(@From(AtomLGenerator.class) Object i)
    throws ExitException
  {
    boolean caught = false;

    try {
      assertNounEquals(i, nouns.met(nouns.dec(nouns.bex(i)), (byte)0));
    }
    catch (FailError e) {
      caught = true;
    }

    assumeFalse(caught);
  }

  @Test
  public void testBoolAdd() throws ExitException {
    assertEquals(true, nouns.add(true, true));
    assertEquals(false, nouns.add(true, false));
    assertEquals(false, nouns.add(false, true));
    assertEquals(2, nouns.add(false, false));
    assertEquals(1, nouns.add(true, 1));
    assertEquals(3, nouns.add(false, 2));
    assertEquals(10L, nouns.add(true, 10L));
    assertEquals(12l, nouns.add(false, 11L));
  }

  @Test
  public void testIntAdd() throws ExitException {
    assertEquals(0, (int)(long)nouns.add(0, 0));
    assertEquals(1, (int)(long)nouns.add(0, 1));
    assertEquals(1, (int)(long)nouns.add(1, 0));
    assertEquals(2, (int)(long)nouns.add(1, 1));
    assertEquals(0x100000000L, nouns.add(1, 0xffffffff));
  }

  @Test
  public void testLongAdd() throws ExitException {
    assertEquals(0L, nouns.add(0L, 0L));
    assertEquals(1L, nouns.add(0L, 1L));
    assertEquals(1L, nouns.add(1L, 0L));
    assertEquals(2L, nouns.add(1L, 1L));
    assertEquals(true,
      nouns.add(1, 0xffffffffffffffffL) instanceof BigAtomL
    );
  }

  @Test
  public void testBigAdd() throws ExitException {
    assertEquals(true, "Yello, world!".equals(
      toString((BigAtomL)nouns.add(fromString("Hello, world!"), 17))
    ));
  }

  @Test(expected = ExitException.class)
  public void testBoolSubUnderflow() throws ExitException {
    nouns.sub(true, false);
  }

  @Property
  public void addInc(@From(AtomLGenerator.class) Object a)
    throws ExitException
  {
    assertNounEquals(nouns.add(a, 1), nouns.inc(a));
  }

  @Test
  public void testBoolSub() throws ExitException {
    assertEquals(true, nouns.sub(true, true));
    assertEquals(false, nouns.sub(false, true));
    assertEquals(true, nouns.sub(false, false));
  }

  @Property
  public void subDec(@From(AtomLGenerator.class) Object a)
    throws ExitException
  {
    assumeTrue( !nouns.isZero(a) );
    assertNounEquals(nouns.sub(a, 1), nouns.dec(a));
  }

  // subtraction reverses addition
  @Property
  public void addSub(
    @From(AtomLGenerator.class) Object a,
    @From(AtomLGenerator.class) Object b
  ) throws ExitException {
    Object c = nouns.add(a, b);
    assertNounEquals(a, nouns.sub(c, b));
    assertNounEquals(b, nouns.sub(c, a));
  }

  // division reverses multiplication
  @Property
  public void mulDiv(@From(AtomLGenerator.class) Object a,
                     @From(AtomLGenerator.class) Object b) throws ExitException {
    assumeTrue( !nouns.isZero(a) && !nouns.isZero(b) );
    Object c = nouns.mul(a, b);
    assertNounEquals(a, nouns.div(c, b));
    assertNounEquals(b, nouns.div(c, a));
  }

  // (5x + 1) % 5 == 1
  // java.lang.AssertionError: Unexpected error in property incMulMod with args [0] and seeds [3932690329607299869]
  @Property
  public void incMulMod(
    @From(AtomLGenerator.class) Object a
  ) throws ExitException {
    assertNounEquals(1L, nouns.mod(nouns.inc(nouns.mul(a, 5L)), 5L));
  }

  @Property
  public void lshRsh(
    // chop ArrayIndexOutOfBounds
    //  with args [false, 32, 8] and seeds [3372070853209715358, 7255190033216352256, -2934053497159410079]

    // with args [-51, 33, 3] and seeds [8009547998480065240, -4930187859339121437, 1423958799303468481]

    //  with args [1437614177, 33, 0] and seeds [7666169638092545818, 1652637183580198143, -4634777146194345582]
    //
    //  [-10, 32, 4]
    // @When(seed = -6337407740872625681L) @From(AtomLGenerator.class)      Object a,
    // @When(seed = 9116334762009861765L) @InRange(min = "0", max = "64")  byte b,
    // @When(seed = -5399559792773723267L) @InRange(min = "0", max = "511") int i
    //
    // [net.frodwith.jaque.data.BigAtomL@15eb5ee5, 64, 8]
    // @When(seed = -7664948684543426089L) @From(AtomLGenerator.class)      Object a,
    // @When(seed = 6157798274796282420L) @InRange(min = "0", max = "64")  byte b,
    // @When(seed = 2519608214495919436L) @InRange(min = "0", max = "511") int i
    //
    @From(AtomLGenerator.class)      Object a,
    @InRange(min = "0", max = "64")  byte b,
    @InRange(min = "0", max = "511") int i
  ) throws ExitException
  {
    boolean caught = false;
    int big = ((i << b) + 31);
    assumeTrue( i == ((big - 31) >>> b) );

    try {
      assertNounEquals(a, nouns.rsh(nouns.lsh(a, b, i), b, i));
    }
    catch (FailError e) {
      caught = true;
    }

    assumeFalse(caught);
  }
}
