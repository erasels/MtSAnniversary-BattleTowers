package BattleTowers.powers;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.BlurPower;
import com.megacrit.cardcrawl.powers.CurlUpPower;

import static BattleTowers.BattleTowers.makeID;

public class PlayerCurlUpPower extends AbstractPower {
    public static final String POWER_ID = makeID(PlayerCurlUpPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(CurlUpPower.POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    private boolean triggered;

    public PlayerCurlUpPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.type = PowerType.BUFF;
        loadRegion("closeUp");
        this.amount = amount;
        updateDescription();
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        updateDescription();
    }

    public int onAttacked(DamageInfo info, int damageAmount) {
        if ((!this.triggered) && (damageAmount < this.owner.currentHealth) && (damageAmount > 0) && (info.owner != null) && (info.type == com.megacrit.cardcrawl.cards.DamageInfo.DamageType.NORMAL)) {
            flash();
            this.triggered = true;
            addToBot(new GainBlockAction(this.owner, this.owner, this.amount));
            addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
            addToBot(new ApplyPowerAction(owner, this.owner, new BlurPower(owner, 1)));
        }
        return damageAmount;
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount +
                DESCRIPTIONS[1];
    }
}
