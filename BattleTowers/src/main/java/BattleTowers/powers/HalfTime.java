package BattleTowers.powers;

import basemod.interfaces.CloneablePowerInterface;
import com.evacipated.cardcrawl.mod.stslib.patches.NeutralPowertypePatch;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class HalfTime extends AbstractPower implements CloneablePowerInterface {
    public static final String POWER_ID = makeID(HalfTime.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public HalfTime(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = 1;
        this.updateDescription();
        this.type = NeutralPowertypePatch.NEUTRAL;
        this.loadRegion("time");
    }
    public void onInitialApplication() {
        if (owner instanceof AbstractPlayer) {
            --AbstractDungeon.player.gameHandSize;
        }
    }

    public void atStartOfTurn() {
        if (owner instanceof AbstractPlayer) {
            this.addToBot(new LoseEnergyAction(amount));
            this.flash();
        }
    }
    public void onRemove() {
        if (owner instanceof AbstractPlayer) {
            ++AbstractDungeon.player.gameHandSize;
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new HalfTime(owner);
    }
}