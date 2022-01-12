package BattleTowers.powers;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class PetrifyingGazePower extends AbstractPower {
    public static final String POWER_ID = makeID(PetrifyingGazePower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public PetrifyingGazePower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.priority = 50;
        this.type = PowerType.BUFF;
        BattleTowers.LoadPowerImage(this);
    }

    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (!info.owner.hasPower(AvertYourGazePower.POWER_ID)) {
            if (damageAmount < this.owner.currentHealth && damageAmount > 0 && info.owner != null && info.type == DamageInfo.DamageType.NORMAL) {
                this.trigger(info.owner);
            }
        }

        return damageAmount;
    }

    public void onPowerApplied(AbstractPower power, AbstractCreature source) {
        if (power.type == AbstractPower.PowerType.DEBUFF) {
            this.trigger(source);
        }
    }

    private void trigger(AbstractCreature c) {
        this.flash();
        this.addToBot(new ApplyPowerAction(c, this.owner, new PetrificationPower(c, 1)));

    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}
