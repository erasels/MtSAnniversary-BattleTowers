package BattleTowers.patches;

import BattleTowers.events.PhasedEvent;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

@SpirePatch(
        clz = AbstractRoom.class,
        method = "getCardRarity",
        paramtypez = { int.class }
)
public class DisableRarityAltering {
    @SpireInstrumentPatch
    public static ExprEditor phasedEventControl() {
        return new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getMethodName().equals("getCardRarity")) {
                    m.replace("{" +
                            "$2 = " + DisableRarityAltering.class.getName() + ".allowAltering($0, $2);" +
                            "$_ = $proceed($$);" +
                            "}");
                }
            }
        };
    }

    public static boolean allowAltering(AbstractRoom room, boolean base) {
        if (room.event instanceof PhasedEvent) {
            return ((PhasedEvent)room.event).allowRarityAltering;
        }
        return base;
    }
}
