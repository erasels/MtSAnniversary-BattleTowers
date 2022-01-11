package BattleTowers.patches;

import BattleTowers.events.PhasedEvent;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
import javassist.CtBehavior;

@SpirePatch(
        clz = ProceedButton.class,
        method = "update"
)
public class ShowEventLater {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void ShowLater(ProceedButton __instance) {
        //Before line 140 of ProceedButton
        if (AbstractDungeon.getCurrRoom().event instanceof PhasedEvent) {
            AbstractDungeon.getCurrRoom().event.waitTimer = 0.0f; //waitTimer set to non-0 value for combat phases with a reward screen to prevent instant transition back
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
}
