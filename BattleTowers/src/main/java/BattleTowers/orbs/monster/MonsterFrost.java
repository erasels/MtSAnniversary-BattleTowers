package BattleTowers.orbs.monster;

import BattleTowers.monsters.AlphabetBoss;
import BattleTowers.monsters.OrbUsingMonster;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;
import com.megacrit.cardcrawl.orbs.Plasma;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FocusPower;

import static BattleTowers.BattleTowers.makeID;

public class MonsterFrost extends Frost
{
    public static final String ORB_ID = makeID(MonsterFrost.class.getSimpleName());
    private static final OrbStrings orbString = CardCrawlGame.languagePack.getOrbString(ORB_ID);
    public static final String[] DESC = orbString.DESCRIPTION;
    private static final int BLOCK = 10;

    private OrbUsingMonster owner;

    public MonsterFrost(OrbUsingMonster owner)
    {
        super();
        ID = ORB_ID;
        this.owner = owner;
        updateDescription();
    }

    @Override
    public void updateDescription()
    {
        applyFocus();
        description = DESC[0] + passiveAmount + DESC[1] + evokeAmount + DESC[2];
    }

    @Override
    public void applyFocus()
    {
        if (owner == null) {
            return;
        }
        AbstractPower power = owner.getPower(FocusPower.POWER_ID);
        if (power != null && !ID.equals(Plasma.ORB_ID)) {
            passiveAmount = Math.max(0, basePassiveAmount + power.amount);
            evokeAmount = Math.max(0, baseEvokeAmount + power.amount);
        }
    }

    @Override
    public void onEvoke()
    {
            if (!owner.isDeadOrEscaped()) {
                AbstractDungeon.actionManager.addToTop(new GainBlockAction(owner, owner, evokeAmount));
            }
    }

    @Override
    public void onEndOfTurn()
    {

        if (!owner.isDeadOrEscaped()) {
            AbstractDungeon.actionManager.addToBottom(new GainBlockAction(owner, owner, passiveAmount));
        }
    }

    @Override
    public AbstractOrb makeCopy()
    {
        return new MonsterFrost(owner);
    }
}
