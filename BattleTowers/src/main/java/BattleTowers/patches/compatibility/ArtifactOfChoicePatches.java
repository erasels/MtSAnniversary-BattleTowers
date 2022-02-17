package BattleTowers.patches.compatibility;

import BattleTowers.BattleTowers;
import BattleTowers.events.TowerEvent;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import javassist.CtBehavior;

public class ArtifactOfChoicePatches {
    @SpirePatch(
            cls = "artifactOfChoice.CardReplacement",
            method = "receivePostUpdate",
            optional = true
    )
    public static class YouOnlyDoThisIfThereIsAScreenOpen {
        @SpireInsertPatch(
                locator = Locator.class
        )
        public static void soIWillResetIt(Object __instance) {
            if (AbstractDungeon.previousScreen == AbstractDungeon.CurrentScreen.NONE)
                AbstractDungeon.previousScreen = null;

        }

        private static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(GridCardSelectScreen.class, "open");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }


        private static boolean combatReward = false;
        @SpireInsertPatch(
                locator = PreCloseLocator.class
        )
        public static void PleaseDoNot(Object __instance) {
            combatReward = AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD;
        }
        @SpireInsertPatch(
                locator = PostCloseLocator.class
        )
        public static void CloseTheScreen(Object __instance) {
            if (combatReward) {
                AbstractDungeon.combatRewardScreen.reopen();
                AbstractDungeon.overlayMenu.showBlackScreen();
                combatReward = false;
            }
        }

        private static class PreCloseLocator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCard.class, "makeStatEquivalentCopy");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
        private static class PostCloseLocator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
            {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher("artifactOfChoice.CardReplacement", "state");
                return new int[] { LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher)[2] };
            }
        }
    }

    @SpirePatch(
            cls = "artifactOfChoice.PotionReplacement",
            method = "receivePostUpdate",
            optional = true
    )
    @SpirePatch(
            cls = "artifactOfChoice.RelicReplacement",
            method = "receivePostUpdate",
            optional = true
    )
    public static class NoCompletePlz {
        private static AbstractRoom.RoomPhase tempPhase = null;
        private static AbstractRoom.RoomPhase getCurrentPhase() {
            return AbstractDungeon.currMapNode != null ? (AbstractDungeon.getCurrRoom() != null ? AbstractDungeon.getCurrRoom().phase : null) : null;
        }

        @SpirePrefixPatch
        public static void PreUpdate(Object __instance) {
            tempPhase = getCurrentPhase();
        }

        @SpirePostfixPatch
        public static void PostUpdate(Object __instance) {
            if (tempPhase != null && getCurrentPhase() != tempPhase && AbstractDungeon.getCurrRoom().event instanceof TowerEvent) {
                BattleTowers.logger.info("Artifact of Choice changed room phase in update.");
                if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMPLETE) {
                    BattleTowers.logger.info("Heck you, no you are not complete.");
                    AbstractDungeon.getCurrRoom().phase = tempPhase;
                }
            }
        }
    }
}
