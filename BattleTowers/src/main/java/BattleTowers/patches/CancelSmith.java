package BattleTowers.patches;

import BattleTowers.events.PhasedEvent;
import BattleTowers.events.phases.MiniRestPhase;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.ui.buttons.CancelButton;
import javassist.CtBehavior;
import org.apache.logging.log4j.Logger;

@SpirePatch(
        clz = CancelButton.class,
        method = "update"
)
public class CancelSmith {
    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void reopenThis(CancelButton __instance) {
        if (AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().event instanceof PhasedEvent) {
            if (((PhasedEvent) AbstractDungeon.getCurrRoom().event).currentPhase instanceof MiniRestPhase) {
                ((MiniRestPhase) ((PhasedEvent) AbstractDungeon.getCurrRoom().event).currentPhase).reopen();
            }
        }
    }

    private static class Locator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
        {
            Matcher finalMatcher = new Matcher.InstanceOfMatcher(RestRoom.class);
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}
