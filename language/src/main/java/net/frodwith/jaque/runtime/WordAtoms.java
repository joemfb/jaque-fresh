package net.frodwith.jaque.runtime;

import com.oracle.truffle.api.CompilerDirectives;

import gnu.math.MPN;

import java.util.Arrays;

import net.frodwith.jaque.data.BigAtomL;
import net.frodwith.jaque.exception.ExitException;
import net.frodwith.jaque.exception.FailError;

public final class WordAtoms {
  public static final boolean BIG_ENDIAN = true, LITTLE_ENDIAN = false;

  public static Object toAtom(int[] words) {
    if ( 0 == words.length ) {
      return 0L;
    }
    else if ( words != null && words.length > 2 ) {
      return new BigAtomL(words);
    }
    else {
      return toLong(words);
    }
  }

  public static long toLong(int[] words) {
    return (words.length == 1)
      ? words[0] & 0xffffffffL
      : ((words[1] & 0xffffffffL) << 32) | (words[0] & 0xffffffffL);
  }

  public static int[] fromLong(long l) {
    int low  = (int)l;
    int high = (int)(l >>> 32);

    return ( 0 == high ) ? new int[] { low } : new int[] { low, high };
  }

	public static int[] malt(int[] words) {
    int bad = 0;

    for ( int i = words.length - 1; i >= 0; --i) {
      if ( words[i] == 0 ) {
        ++bad;
      }
      else {
        break;
      }
    }

    if ( bad > 0 ) {
      words = Arrays.copyOfRange(words, 0, words.length - bad);
    }

    return words;
	}

	public static int[] slaq(byte bloq, int len) {
    int big = (len << bloq) + 31;

    if ( len != ((big - 31) >>> bloq) ) {
      CompilerDirectives.transferToInterpreter();
      throw new FailError("slaq overflow");
    }

		return new int[(big >>> 5)];
	}

	public static void chop(byte met, int fum, int wid, int tou, int[] dst, int[] src) {
		int len = src.length, i;

		if (met < 5) {
			int san = 1 << met,
					mek = ((1 << san) - 1),
					baf = fum << met,
					bat = tou << met;

      {
        int max_f = (fum << met) + (san * wid);
        int max_t = (tou << met) + (san * wid);

        if ( max_f < 0 ||
             max_t < 0 ||
             fum != ((max_f - (san * wid)) >>> met) ||
             tou != ((max_t - (san * wid)) >>> met) )
        {
          CompilerDirectives.transferToInterpreter();
          throw new FailError("chop overflow");
        }
      }

			for (i = 0; i < wid; ++i) {
				int waf = baf >>> 5,
						raf = baf & 31,
						wat = bat >>> 5,
						rat = bat & 31,
						hop;

				hop = (waf >= len) ? 0 : src[waf];
				hop = (hop >>> raf) & mek;
				dst[wat] ^= hop << rat;
				baf += san;
				bat += san;
			}
		}
		else {
			int hut = met - 5,
					san = 1 << hut,
					j;

      {
        int max_f = ((fum + wid) << hut) + san;
        int max_t = ((tou + wid) << hut) + san;

        if ( max_f < 0 ||
             max_t < 0 ||
             (fum + wid) != ((max_f - san) >>> hut) ||
             (tou + wid) != ((max_t - san) >>> hut) )
        {
          CompilerDirectives.transferToInterpreter();
          throw new FailError("chop overflow");
        }
      }

			for (i = 0; i < wid; ++i) {
				int wuf = (fum + i) << hut,
						wut = (tou + i) << hut;

				for (j = 0; j < san; ++j) {
					dst[wut + j] ^= ((wuf + j) >= len)
						? 0
						: src[wuf + j];
				}
			}
		}
	}

  public static int compare(int[] a, int[] b) {
    return MPN.cmp(a, a.length, b, b.length);
  }

  public static int[] incrementInPlace(int[] vol) {
    for ( int i = 0; i < vol.length; i++ ) {
      if ( 0 != ++vol[i] ) {
        return vol;
      }
    }
    int[] bigger = new int[vol.length + 1];
    bigger[bigger.length] = 1;
    return bigger;
  }

  public static int[] inc(int[] a) {
    int[] w = Arrays.copyOf(a, a.length);
    w = incrementInPlace(w);
    return w;
  }

  // note: you can decrement a word array under BigAtom.MINIMUM with this.
  public static int[] decrementInPlace(int[] vol) {
    boolean carry = true;

    for ( int i = 0; carry && (i < vol.length); i++ ) {
      carry = 0 == vol[i];
      vol[i] -= 1;
    }

    return malt(vol);

    // for ( int i = 0; i < vol.length; i++ ) {
    //   if ( 0 == vol[i] ) {
    //     int[] smaller = new int[vol.length - 1];
    //     Arrays.fill(smaller, 0xFFFFFFFF);
    //     return smaller;
    //   }
    //   else {
    //     vol[i] -= 1;
    //   }
    // }
    // return vol;
  }

  public static int[] dec(int[] a) {
    int[] w = Arrays.copyOf(a, a.length);
    w = decrementInPlace(w);
    return w;
  }

  public static int met(byte bloq, int[] w)  {
    int gal = w.length - 1;
    int daz = w[gal];

    switch (bloq) {
      case 0:
      case 1:
      case 2:
        int col = 32 - Integer.numberOfLeadingZeros(daz),
            bif = col + (gal << 5);

        return (bif + ((1 << bloq) - 1) >>> bloq);

      case 3:
        return (gal << 2)
          + ((daz >>> 24 != 0)
            ? 4
            : (daz >>> 16 != 0)
            ? 3
            : (daz >>> 8 != 0)
            ? 2
            : 1);

      case 4:
        return (gal << 1) + ((daz >>> 16 != 0) ? 2 : 1);

      default: {
        int gow = bloq - 5;
        return ((gal + 1) + ((1 << gow) - 1)) >>> gow;
      }
    }
  }

  public static int[] bex(int a) {
    int whole = a >>> 5;
    int parts = a & 31;

    int[] words = new int[whole+1];
    words[whole] = 1 << parts;
    return words;
  }

  public static int[] add(int[] a, int[] b) {
    MPNSquare s = new MPNSquare(a, b);
    int[] dst   = new int[s.len+1];
    dst[s.len]  = MPN.add_n(dst, s.x, s.y, s.len);
    return malt(dst);
  }

  public static int[] sub(int[] a, int[] b) throws ExitException {
    MPNSquare s = new MPNSquare(a, b);
    int[] dst = new int[s.len];
    int bor = MPN.sub_n(dst, s.x, s.y, s.len);
    if ( bor != 0 ) {
      throw new ExitException("subtract underflow");
    }
    return malt(dst);
  }

  // lsh fails because we don't really have infinitely sized atoms
  public static int[] lsh(byte bloq, int count, int[] words) {
    int len = met(bloq, words),
        big;

    if ( 0 == len ) {
      return new int[]{};
    }
    try {
      big = Math.addExact(count, len);
    }
    catch (ArithmeticException e) {
      CompilerDirectives.transferToInterpreter();
      throw new FailError("slaq count doesn't fit in int");
    }

    int[] sal = slaq(bloq, big);
    chop(bloq, 0, len, count, sal, words);

    return malt(sal);
  }

  public static int[] rsh(byte bloq, int count, int[] words) {
    int len = met(bloq, words);

    if ( count >= len ) {
      return new int[]{};
    }
    else {
      int[] sal = slaq(bloq, len - count);
      chop(bloq, count, len - count, 0, sal, words);

      return malt(sal);
    }
  }

  public static int[] cut(byte bloq, int index, int count, int[] words)
    throws ExitException
  {
    int len = met(bloq, words);

    if ( (0 == count) || (index >= len) ) {
      return new int[]{};
    }

    if ( (index + count) > len ) {
      count = len - index;
    }

    if ( (index == 0) && (count == len) ) {
      return words;
    }
    else {
      int[] sal = slaq(bloq, count);
      chop(bloq, index, count, 0, sal, words);
      return malt(sal);
    }
  }

  // public static int[] max(int[] a, int[] b) {
  //   return ( 0 > compare(a, b) ) ? b : a;
  // }

  public static int[] con(int[] a, int[] b) {
    byte w   = 5;
    int  lna = met(w, a);
    int  lnb = met(w, b);

    if ( (0 == lna) && (0 == lnb) ) {
      return new int[]{};
    }
    else {
      int i, len = Math.max(lna, lnb);
      int[] sal  = new int[len];

      chop(w, 0, lna, 0, sal, a);

      for ( i = 0; i < lnb; i++ ) {
        sal[i] |= b[i];
      }

      return malt(sal);
    }
  }

  public static int[] dis(int[] a, int[] b) {
    byte w   = 5;
    int  lna = met(w, a);
    int  lnb = met(w, b);

    if ( (0 == lna) || (0 == lnb) ) {
      return new int[]{};
    }
    else {
      int i, len = Math.min(lna, lnb);
      int[] sal  = new int[len];

      chop(w, 0, len, 0, sal, a);

      for ( i = 0; i < len; i++ ) {
        sal[i] &= b[i];
      }

      return malt(sal);
    }
  }

  /* This code is substantially adapted from Kawa's IntNum.java -- see the note at
   * the top of gnu.math.MPN */
  private static int[] divmod_x(int[] x, int[] y) {
    int xlen = x.length,
        ylen = y.length,
        rlen, qlen;
    int[] xwords = Arrays.copyOf(x, xlen+2),
          ywords = Arrays.copyOf(y, ylen);

    int nshift = MPN.count_leading_zeros(ywords[ylen-1]);
    if (nshift != 0) {
      MPN.lshift(ywords, 0, ywords, ylen, nshift);
      int x_high = MPN.lshift(xwords, 0, xwords, xlen, nshift);
      xwords[xlen++] = x_high;
    }

    if (xlen == ylen) {
      xwords[xlen++] = 0;
    }

    MPN.divide(xwords, xlen, ywords, ylen);
    rlen = ylen;
    MPN.rshift0(ywords, xwords, 0, rlen, nshift);
    qlen = xlen + 1 - ylen;
    xwords = Arrays.copyOfRange(xwords, ylen, ylen+qlen);
    // while ( rlen > 1 && 0 == ywords[rlen - 1] ) {
    //   --rlen;
    // }
    // if ( ywords[rlen-1] < 0 ) {
    //   ywords[rlen++] = 0;
    // }

    return xwords;
  }

  private static int[] divmod_y(int[] x, int[] y) {
    int xlen = x.length,
        ylen = y.length,
        rlen, qlen;
    int[] xwords = Arrays.copyOf(x, xlen+2),
          ywords = Arrays.copyOf(y, ylen);

    int nshift = MPN.count_leading_zeros(ywords[ylen-1]);
    if (nshift != 0) {
      MPN.lshift(ywords, 0, ywords, ylen, nshift);
      int x_high = MPN.lshift(xwords, 0, xwords, xlen, nshift);
      xwords[xlen++] = x_high;
    }

    if (xlen == ylen) {
      xwords[xlen++] = 0;
    }

    MPN.divide(xwords, xlen, ywords, ylen);
    rlen = ylen;
    MPN.rshift0(ywords, xwords, 0, rlen, nshift);
    qlen = xlen + 1 - ylen;
    // xwords = Arrays.copyOfRange(xwords, ylen, ylen+qlen);
    while ( rlen > 1 && 0 == ywords[rlen - 1] ) {
      --rlen;
    }
    if ( ywords[rlen-1] < 0 ) {
      ywords[rlen++] = 0;
    }

    return ywords;
  }

  // NO ZERO CHECK
  public static int[] mod(int[] x, int[] y) {
    int cmp = compare(x, y);
    if ( cmp < 0 ) {
      return y;
    }
    else if ( 0 == cmp ) {
      return new int[]{};
    }
    else if ( 1 == y.length ) {
      int[] q = new int[x.length];
      return new int[]{ MPN.divmod_1(q, x, x.length, y[0]) };
    }
    else {
      return malt(divmod_y(x, y));
    }
  }

  // NO ZERO CHECK
  public static int[] div(int[] x, int[] y) {
    int cmp = compare(x, y);
    if ( cmp < 0 ) {
      return new int[]{};
    }
    else if ( 0 == cmp ) {
      return new int[]{1};
    }
    else if ( 1 == y.length ) {
      int[] q = new int[x.length];
      MPN.divmod_1(q, x, x.length, y[0]);
      return malt(q);
    }
    else {
      return malt(divmod_x(x, y));
    }
  }

  public static int[] mul(int[] x, int[] y) {
    int xlen = x.length,
        ylen = y.length;
    int[] dest = new int[xlen + ylen];

    if ( xlen < ylen ) {
      int zlen = xlen;
      int[] z = x;

      x = y;
      y = z;
      xlen = ylen;
      ylen = zlen;
    }

    MPN.mul(dest, x, xlen, y, ylen);
    return malt(dest);
  }
}
