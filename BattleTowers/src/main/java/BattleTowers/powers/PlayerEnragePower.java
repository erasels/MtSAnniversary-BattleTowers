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

import static BattleTowers.BattleTowers.makeID;

public class PlayerEnragePower extends AbstractPower {
    public static final String POWER_ID = makeID(PlayerEnragePower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(PlayerEnragePower.POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    private boolean triggered;

    public PlayerEnragePower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.type = PowerType.BUFF;
        this.amount = 3;
        loadRegion("anger");
        updateDescription();
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        updateDescription();
    }

    @Override
    public void onSpecificTrigger() {
        super.onSpecificTrigger();
        this.stackPower(-1);
        if (this.amount <= 0){
            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}
