package BattleTowers.orbs.monster;

import BattleTowers.actions.monsterOrbs.MonsterLightningOrbEvokeAction;
import BattleTowers.monsters.OrbUsingMonster;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.orbs.Plasma;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.FocusPower;

import static BattleTowers.BattleTowers.makeID;

public class MonsterLightning extends Lightning
{
    public static final String ORB_ID = makeID(MonsterLightning.class.getSimpleName());
    private static final OrbStrings orbString = CardCrawlGame.languagePack.getOrbString(ORB_ID);
    public static final String[] DESC = orbString.DESCRIPTION;

    private OrbUsingMonster owner;

    public MonsterLightning(OrbUsingMonster owner)
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
        AbstractDungeon.actionManager.addToTop(new MonsterLightningOrbEvokeAction(owner, new DamageInfo(owner, this.evokeAmount, DamageInfo.DamageType.THORNS)));
    }

    @Override
    public void onEndOfTurn()
    {
        AbstractDungeon.actionManager.addToBottom(new MonsterLightningOrbEvokeAction(owner, new DamageInfo(owner, this.passiveAmount, DamageInfo.DamageType.THORNS)));
    }

    @Override
    public AbstractOrb makeCopy()
    {
        return new MonsterLightning(owner);
    }
}
