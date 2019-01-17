package net.frodwith.jaque.runtime;

import java.util.function.Function;
import java.util.Map;
import java.util.HashMap;

import org.graalvm.options.OptionValues;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionType;

import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.frodwith.jaque.HoonLanguage;
import net.frodwith.jaque.NockOptions;
import net.frodwith.jaque.jet.JetTree;
import net.frodwith.jaque.jet.RootCore;
import net.frodwith.jaque.data.BigAtom;
import net.frodwith.jaque.data.Cell;
import net.frodwith.jaque.data.AxisMap;
import net.frodwith.jaque.data.NockFunction;
import net.frodwith.jaque.parser.FormulaParser;
import net.frodwith.jaque.exception.ExitException;
import net.frodwith.jaque.dashboard.Location;
import net.frodwith.jaque.dashboard.Registration;
import net.frodwith.jaque.dashboard.Dashboard;
import net.frodwith.jaque.dashboard.BatteryHash;
import net.frodwith.jaque.dashboard.ColdRegistration;

public final class HoonContext {
  private final Env env;
  private final HoonLanguage hoon;
  private final LoadingCache<String,Object> wishes;
  private final TruffleContext innerContext;
  private Object wishGate;

  public HoonContext(HoonLanguage hoon, Env env) {
    OptionValues values = env.getOptions();

    this.hoon = hoon;
    this.env = env;
    this.innerContext = env.newContextBuilder()
      .option("nock.jets", "hoon")
      .build();
    this.wishes = CacheBuilder.newBuilder()
      .maximumSize(values.get(HoonOptions.WISH_CACHE_SIZE))
      .build(new CacheLoader<String,Object>() {
        public Object load(String hoonSrc) {
          return makeWish(hoonSrc);
        }
      });
  }

  public void setWishGate(Object gate) {
    this.wishGate = gate;
  }

  private Object makeWish(String hoonSrc) {
    Object old = innerContext.enter();
    try {
      return hoon.innerSlam(wishGate, Cords.fromString(hoonSrc));
    }
    finally {
      innerContext.leave(old);
    }
  }

  public Object wish(String hoonSrc) {
    return wishes.get(hoonSrc);
  }

  public CellMeta cellMeta(Cell c) {
    Object old = innerContext.enter();
    try {
      return hoon.innerMeta(c);
    }
    finally {
      innerContext.leave(old);
    }
  }
}
