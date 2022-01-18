package BattleTowers.powers;

import BattleTowers.BattleTowers;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.List;

import static BattleTowers.BattleTowers.makeID;

public class CursedTotemPower extends AbstractPower {
    public static final String POWER_ID = makeID(CursedTotemPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public CursedTotemPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.type = PowerType.BUFF;
        BattleTowers.LoadPowerImage(this);
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }

    @Override
    public float atDamageFinalReceive(float damage, DamageInfo.DamageType type)
    {
        if (damage > 1) {
            damage = 1;
        }
        return damage;
    }

    @SpirePatch2(
            clz = AbstractCreature.class,
            method = "renderRedHealthBar"
    )
    public static class PoisonDisplayPatch
    {
        @SpireInsertPatch(
                locator = Locator.class,
                localvars = {"poisonAmt"}
        )
        public static void Insert(AbstractCreature __instance, @ByRef int[] poisonAmt)
        {
            if (poisonAmt[0] > 1 && __instance.hasPower(CursedTotemPower.POWER_ID)) {
                poisonAmt[0] = 1;
            }
        }

        private static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
            {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractCreature.class, "currentHealth");
                List<Matcher> matchers = new ArrayList<>();
                matchers.add(finalMatcher);
                return LineFinder.findInOrder(ctMethodToPatch, matchers, finalMatcher);
            }
        }
    }
}
