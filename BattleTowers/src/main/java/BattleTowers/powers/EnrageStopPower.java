package BattleTowers.powers;

import BattleTowers.BattleTowers;
import BattleTowers.util.UC;
import basemod.cardmods.RetainMod;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.AngerPower;
import com.megacrit.cardcrawl.powers.TimeWarpPower;

import static BattleTowers.BattleTowers.makeID;

public class EnrageStopPower extends AbstractPower {
    public static final String POWER_ID = makeID(EnrageStopPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public EnrageStopPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.updateDescription();
        BattleTowers.LoadPowerImage(this);
        this.type = PowerType.BUFF;
    }


    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }

    public void onAfterUseCard(AbstractCard card, UseCardAction action) {

        if (card.type == AbstractCard.CardType.SKILL) {
            this.flashWithoutSound();
            --this.amount;
            if (this.amount == 0) {
                UC.atb(new RemoveSpecificPowerAction(this.owner, this.owner, this));
                UC.atb(new RemoveSpecificPowerAction(this.owner, this.owner, AngerPower.POWER_ID));
            }


            this.updateDescription();
        }
    }

}
