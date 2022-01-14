package BattleTowers.powers;

import BattleTowers.BattleTowers;
import BattleTowers.monsters.Trenchcoat;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class TrenchcoatPower extends AbstractPower {
    public static final String POWER_ID = makeID(TrenchcoatPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public TrenchcoatPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.type = PowerType.BUFF;
        BattleTowers.LoadPowerImage(this);
        this.amount = amount;
        this.updateDescription();
    }

    @Override
    public void wasHPLost(DamageInfo info, int damageAmount) {

       // flash();
        if (damageAmount > 0){
            this.amount = this.amount - damageAmount;
            if (this.amount <= 0){
                ((Trenchcoat)owner).topple();
            }
        }
    }

    @Override
    public void atStartOfTurn() {
        this.amount = 20;
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}
