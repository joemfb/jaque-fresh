package net.frodwith.jaque.test.quickcheck;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.GenerationStatus;

import net.frodwith.jaque.runtime.ByteAtoms;

public final class AtomLGenerator extends Generator<Object> {
  public AtomLGenerator() {
    super(Object.class);
  }

  @Override
  public Object generate(SourceOfRandomness random,
                         GenerationStatus status) {
    if ( random.nextBoolean() ) {
      return random.nextBoolean();
    }
    else if ( random.nextBoolean() ) {
      return (long) random.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
    }
    else if ( random.nextBoolean() ) {
      return random.nextInt();
    }
    else if ( random.nextBoolean() ) {
      return random.nextLong();
    }
    else {
      // 9 bytes of randomness (USUALLY a bigatom)
      return ByteAtoms.toAtom(random.nextBytes(9));
    }
  }
}
