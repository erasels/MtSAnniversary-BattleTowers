package BattleTowers.powers;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.*;

import java.text.MessageFormat;

import static BattleTowers.BattleTowers.makeID;

public class PetrificationPower extends AbstractPower {
    public static final String POWER_ID = makeID(PetrificationPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    private static final int THRESHOLD = 5;
    private static final int TEMP_STRENGTH_LOSS = 4;
    private static final int TEMP_DEX_LOSS = 4;

    public PetrificationPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.updateDescription();
        this.priority = 50;
        this.type = PowerType.DEBUFF;
        BattleTowers.LoadPowerImage(this);
    }

    @Override
    public void atStartOfTurnPostDraw() {
        if (this.amount >= THRESHOLD) {
            this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, -TEMP_STRENGTH_LOSS)));
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new GainStrengthPower(this.owner, TEMP_STRENGTH_LOSS)));
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new DexterityPower(this.owner, -TEMP_DEX_LOSS)));
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new GainDexterityPower(this.owner, TEMP_DEX_LOSS)));
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new NoDrawPower(this.owner)));
        }
    }

    @Override
    public void updateDescription() {
        this.description = MessageFormat.format(DESCRIPTIONS[0], THRESHOLD, TEMP_STRENGTH_LOSS, TEMP_DEX_LOSS);
    }
}
