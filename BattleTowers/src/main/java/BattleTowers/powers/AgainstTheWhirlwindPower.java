package BattleTowers.powers;

import basemod.interfaces.CloneablePowerInterface;
import com.evacipated.cardcrawl.mod.stslib.patches.NeutralPowertypePatch;
import com.evacipated.cardcrawl.mod.stslib.powers.abstracts.TwoAmountPower;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class AgainstTheWhirlwindPower extends TwoAmountPower implements CloneablePowerInterface  {
    public static final String POWER_ID = makeID(AgainstTheWhirlwindPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;
    public boolean Gather = true;

    public AgainstTheWhirlwindPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = 1;
        this.amount2 = 0;
        canGoNegative = true;
        this.updateDescription();
        this.type = NeutralPowertypePatch.NEUTRAL;
        this.loadRegion("storm");
    }

    public void Storm(){
        amount2 = 0;
    }

    public void atStartOfTurn() {
        Gather = true;
    }
    public void atEndOfTurn(boolean isplayer) {
        if (Gather){
            amount2 += amount;
        }
    }
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == AbstractCard.CardType.ATTACK){
            Gather = false;
        }
    }
    @Override
    public void onRemove() {
        flash();
    }

    @Override
    public void updateDescription() {
        if (amount2 > 1){
            this.description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[2];
        } else this.description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }

    @Override
    public AbstractPower makeCopy() {
        return new TemporaryDeEnergizePower(owner,amount);
    }
}