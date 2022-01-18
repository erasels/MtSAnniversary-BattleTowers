package BattleTowers.powers;

import basemod.interfaces.CloneablePowerInterface;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class TemporaryDeEnergizePower extends AbstractPower implements CloneablePowerInterface {
    public static final String POWER_ID = makeID(TemporaryDeEnergizePower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public TemporaryDeEnergizePower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.updateDescription();
        this.type = PowerType.DEBUFF;
        this.loadRegion("energized_blue");
    }
    public void atStartOfTurn() {
        this.addToBot(new LoseEnergyAction(1));
        addToBot(new ReducePowerAction(owner,owner,this,1));
        this.flash();
    }
    @Override
    public void onRemove() {
        flash();
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }

    @Override
    public AbstractPower makeCopy() {
        return new TemporaryDeEnergizePower(owner,amount);
    }
}