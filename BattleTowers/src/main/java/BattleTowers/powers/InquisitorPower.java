package BattleTowers.powers;

import BattleTowers.powers.abstracts.AbstractBTPower;
import com.evacipated.cardcrawl.mod.stslib.patches.NeutralPowertypePatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;

import static BattleTowers.BattleTowers.makeID;

public class InquisitorPower extends AbstractBTPower {
    public static final String POWER_ID = makeID(InquisitorPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public InquisitorPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        amount = 0;
        this.updateDescription();
        this.type = NeutralPowertypePatch.NEUTRAL;
        this.loadRegion("mantra");
    }

    @Override
    public void updateDescription() {
        this.description = String.format(DESCRIPTIONS[0], 30);
    }
}