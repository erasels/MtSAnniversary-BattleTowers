package BattleTowers.patches;

import BattleTowers.interfaces.ModifyCombatCardsRelic;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;

@SpirePatch(
        clz = CardGroup.class,
        method = "initializeDeck"
)
public class ModifyCombatCards {
    @SpireInsertPatch(
            locator = Locator.class,
            localvars = "copy"
    )
    public static void modify(CardGroup __instance, CardGroup masterDeck, CardGroup copy) {
        for (AbstractRelic r : AbstractDungeon.player.relics) {
            if (r instanceof ModifyCombatCardsRelic) {
                ((ModifyCombatCardsRelic) r).modifyCombatDeck(copy);
            }
        }
    }

    private static class Locator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
        {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(CardGroup.class, "shuffle");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}
