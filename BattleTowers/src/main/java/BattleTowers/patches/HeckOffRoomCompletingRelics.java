package BattleTowers.patches;

import BattleTowers.BattleTowers;
import BattleTowers.events.TowerEvent;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.OverlayMenu;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CtBehavior;

@SpirePatch(
        clz = OverlayMenu.class,
        method = "update"
)
public class HeckOffRoomCompletingRelics {
    private static AbstractRoom.RoomPhase tempPhase = null;
    private static AbstractRoom.RoomPhase getCurrentPhase() {
        return AbstractDungeon.currMapNode != null ? (AbstractDungeon.getCurrRoom() != null ? AbstractDungeon.getCurrRoom().phase : null) : null;
    }

    @SpireInsertPatch(
            locator = PreLocator.class
    )
    public static void PreUpdate(OverlayMenu __instance) {
        tempPhase = getCurrentPhase();
    }

    @SpireInsertPatch(
            locator = PostLocator.class
    )
    public static void PostUpdate(OverlayMenu __instance) {
        if (tempPhase != null && getCurrentPhase() != tempPhase && AbstractDungeon.getCurrRoom().event instanceof TowerEvent) {
            BattleTowers.logger.info("Relic changed room phase in update.");
            if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMPLETE) {
                BattleTowers.logger.info("Heck you, no you are not complete.");
                AbstractDungeon.getCurrRoom().phase = tempPhase;
            }
        }
    }

    private static class PreLocator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
        {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "relics");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }

    private static class PostLocator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
        {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "blights");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}
