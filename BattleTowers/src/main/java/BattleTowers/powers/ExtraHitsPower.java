package BattleTowers.powers;

import basemod.interfaces.CloneablePowerInterface;
import com.badlogic.gdx.utils.StringBuilder;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class ExtraHitsPower extends AbstractPower implements CloneablePowerInterface {
    public static final String POWER_ID = makeID(ExtraHitsPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public ExtraHitsPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.updateDescription();
        this.type = PowerType.BUFF;
        this.loadRegion("anger");
    }

    public void updateDescription() {
        StringBuilder sb = new StringBuilder();
        if (this.amount == 1) {
            sb.append(DESCRIPTIONS[0]).append(this.amount).append(DESCRIPTIONS[1]);
        } else if (this.amount > 1){
            sb.append(DESCRIPTIONS[0]).append(this.amount).append(DESCRIPTIONS[2]);
        }
        description = sb.toString();
    }

    @Override
    public AbstractPower makeCopy() {
        return new ExtraHitsPower(owner, amount);
    }
}