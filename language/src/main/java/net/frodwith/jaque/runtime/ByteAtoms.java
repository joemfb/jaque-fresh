package net.frodwith.jaque.runtime;

import de.greenrobot.common.hash.Murmur3A;

import gnu.math.MPN;

import java.util.Arrays;

import net.frodwith.jaque.data.BigAtomL;
import net.frodwith.jaque.exception.ExitException;

public final class ByteAtoms {
  public static final boolean BIG_ENDIAN = true, LITTLE_ENDIAN = false;

  public static Object toAtom(byte[] b) {
    return WordAtoms.toAtom(toWords(b, LITTLE_ENDIAN));
  }

  public static int mug(byte[] b) {
    Murmur3A murmur;
    int  seed = 0xcafebabe;
    long hash = 0L;

    while ( true ) {
      murmur = new Murmur3A(seed);
      murmur.update(b);
      hash = murmur.getValue();
      hash = (hash >>> 31) ^ (hash & 0x7fffffffL);

      if ( 0L == hash ) {
        ++seed;
      }
      else {
        return (int)hash;
      }
    }
  }

  /* IN-PLACE */
  private static byte[] reverse(byte[] a) {
    int i, j;
    byte b;
    for (i = 0, j = a.length - 1; j > i; ++i, --j) {
      b = a[i];
      a[i] = a[j];
      a[j] = b;
    }
    return a;
  }

  public static int[] toWords(byte[] pill, boolean endian) {
    int len  = pill.length;
    int trim = len % 4;

    if (endian == BIG_ENDIAN) {
      pill = Arrays.copyOf(pill, len);
      reverse(pill);
    }

    if (trim > 0) {
      int    nlen = len + (4-trim);
      byte[] npil = new byte[nlen];
      System.arraycopy(pill, 0, npil, 0, len);
      pill = npil;
      len = nlen;
    }

    int   size  = len / 4;
    int[] words = new int[size];
    int i, b, w;
    for (i = 0, b = 0; i < size; ++i) {
      w =  (pill[b++] << 0)  & 0x000000FF;
      w ^= (pill[b++] << 8)  & 0x0000FF00;
      w ^= (pill[b++] << 16) & 0x00FF0000;
      w ^= (pill[b++] << 24) & 0xFF000000;
      words[i] = w;
    }

    return WordAtoms.malt(words);
  }

  public static byte[] fromWords(int[] words, int byteLength) {
    return fromWords(words, byteLength, LITTLE_ENDIAN);
  }

  public static byte[] fromWords(int[] wor, int bel, boolean endian) {
    int    w, i, b;
    byte[] buf = new byte[bel];
    for (i = 0, b = 0;;) {
      w = wor[i++];

      buf[b++] = (byte) ((w & 0x000000FF) >>> 0);
      if (b >= bel) break;

      buf[b++] = (byte) ((w & 0x0000FF00) >>> 8);
      if (b >= bel) break;

      buf[b++] = (byte) ((w & 0x00FF0000) >>> 16);
      if (b >= bel) break;

      buf[b++] = (byte) ((w & 0xFF000000) >>> 24);
      if (b >= bel) break;
    }
    if (endian == BIG_ENDIAN) {
      reverse(buf);
    }
    return buf;
  }
}
