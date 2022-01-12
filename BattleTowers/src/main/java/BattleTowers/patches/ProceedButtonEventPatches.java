package BattleTowers.patches;

import BattleTowers.events.PhasedEvent;
import BattleTowers.events.phases.ShopPhase;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

@SpirePatch(
        clz = ProceedButton.class,
        method = "update"
)
public class ProceedButtonEventPatches {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void ShowLater(ProceedButton __instance) {
        //Before line 140 of ProceedButton
        if (AbstractDungeon.getCurrRoom().event instanceof PhasedEvent) {
            AbstractDungeon.getCurrRoom().event.waitTimer = 0.0f;
            //waitTimer set to non-0 value for combat phases with a reward screen to prevent instant transition back
            //Will have no effect on non-combat phases since their waitTimer should be 0 already anyways.
        }
    }

    private static class Locator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
        {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(ProceedButton.class, "hide");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    @SpireInstrumentPatch
    public static ExprEditor forShopPhase() {
        return new ExprEditor() {
            int count = 0;

            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                //143, 206, 216
                if (count < 3 && m.getMethodName().equals("open") && m.getClassName().equals(DungeonMapScreen.class.getName())) {
                    ++count;
                    if (count == 3) {
                        m.replace("{" +
                                    "if (" + ProceedButtonEventPatches.class.getName() + ".openMap(currentRoom)) {" +
                                        "$_ = $proceed($$);" +
                                    "}" +
                                "}");
                    }
                }
            }
        };
    }

    public static boolean openMap(AbstractRoom currentRoom) {
        if (currentRoom != null && currentRoom.event instanceof PhasedEvent && ((PhasedEvent) currentRoom.event).currentPhase instanceof ShopPhase) {
            return ((ShopPhase) ((PhasedEvent) currentRoom.event).currentPhase).finish();
        }
        return true;
    }
}
