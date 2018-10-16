package net.frodwith.jaque.nodes;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;

import net.frodwith.jaque.runtime.NockContext;

@ReportPolymorphism
@TypeSystemReference(NockTypes.class)
@NodeField(name="contextReference", type=ContextReference.class)
public abstract class NockLookupNode extends Node {
  public static final int INLINE_CACHE_SIZE = 2;

  protected abstract ContextReference<NockContext> getContextReference();
}