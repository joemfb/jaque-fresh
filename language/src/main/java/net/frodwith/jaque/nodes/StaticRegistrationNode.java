package net.frodwith.jaque.nodes;

import java.util.function.Supplier;

import com.oracle.truffle.api.TruffleLanguage.ContextReference;

import net.frodwith.jaque.data.Cell;
import net.frodwith.jaque.data.NockObject;
import net.frodwith.jaque.data.FastClue;
import net.frodwith.jaque.runtime.Equality;
import net.frodwith.jaque.runtime.NockContext;
import net.frodwith.jaque.dashboard.Dashboard;
import net.frodwith.jaque.exception.ExitException;

// A core we have already registred (noun-equal).
public final class StaticRegistrationNode extends RegistrationNode {
  private final Cell core;
  private final FastClue clue;

  public StaticRegistrationNode(Cell core, FastClue clue,
    ContextReference<NockContext> contextReference) {
    super(contextReference);
    this.core = core;
    this.clue = clue;
  }

  protected Object executeRegister(Object core, Object clue) {
    RegistrationNode replacement;
    if ( Equality.equals(this.clue.noun, clue) ) {
      if ( Equality.equals(this.core, core) ) {
        return this.core;
      }
      else {
        Supplier<Dashboard> supply = getSupplier();
        NockObject object;
        try {
          object = this.core.getMeta().getObject(supply);
          replacement = new FineRegistrationNode(
              this.clue, object.getFine(supply),
              contextReference);
        }
        catch ( ExitException e ) {
          // XX log non-core registration
          return core;
        }
      }
    }
    else {
      replacement = new FullyDynamicRegistrationNode(contextReference);
    }
    replace(replacement);
    return replacement.executeRegister(core, clue);
  }
}