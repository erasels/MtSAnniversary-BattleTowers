package BattleTowers.powers;

import BattleTowers.util.UC;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class PlayerEnragePower extends AbstractPower {
    public static final String POWER_ID = makeID(PlayerEnragePower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(PlayerEnragePower.POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    private boolean triggered;

    public PlayerEnragePower(AbstractCreature owner, int amt) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.type = PowerType.BUFF;
        this.amount = amt;
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
        flashWithoutSound();
        UC.reducePower(this);
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}
