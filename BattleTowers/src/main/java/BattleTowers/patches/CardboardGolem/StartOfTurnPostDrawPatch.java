package BattleTowers.patches.CardboardGolem;

import BattleTowers.monsters.CardboardGolem.powers.CardEaterPower;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

@SpirePatch(
        clz = AbstractPlayer.class,
        method = "applyStartOfTurnPostDrawRelics"
)
public class StartOfTurnPostDrawPatch {
    public static void Prefix(AbstractPlayer __instance) {
        for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (m.hasPower(CardEaterPower.POWER_ID)) {
                m.getPower(CardEaterPower.POWER_ID).atStartOfTurnPostDraw();
            }
        }
    }
}