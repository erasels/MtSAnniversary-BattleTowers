package BattleTowers.patches;

import BattleTowers.events.phases.WrappedEventPhase;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.AbstractImageEvent;

public class EventWrapping {
    @SpirePatch(
            clz = AbstractEvent.class,
            method = SpirePatch.CLASS
    )
    public static class Field {
        public static SpireField<WrappedEventPhase> wrapper = new SpireField<>(()->null);
    }

    @SpirePatch(
            clz = AbstractEvent.class,
            method = "openMap"
    )
    @SpirePatch(
            clz = AbstractImageEvent.class,
            method = "openMap"
    )
    public static class NoOpen {
        @SpirePrefixPatch
        public static SpireReturn<?> nope(AbstractEvent __instance) {
            WrappedEventPhase wrapper = Field.wrapper.get(__instance);
            if (wrapper != null) {
                if (wrapper.finish()) {
                    return SpireReturn.Return();
                }
            }
            return SpireReturn.Continue();
        }
    }
}
