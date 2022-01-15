package BattleTowers.powers;

import BattleTowers.BattleTowers;
import BattleTowers.actions.PutNonEtherealOnDeckAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class OnusPower extends AbstractPower {
    public static final String POWER_ID = makeID("OnusPower");
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    private AbstractCreature source;

    public OnusPower(AbstractCreature owner, AbstractCreature source) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.source = source;
        this.updateDescription();
        this.type = PowerType.DEBUFF;
        BattleTowers.LoadPowerImage(this);
        this.amount = -1;
        this.priority = 3;
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        addToBot(new PutNonEtherealOnDeckAction());
    }

    @Override
    public void stackPower(int stackAmount) {
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}
