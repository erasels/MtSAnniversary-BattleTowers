package BattleTowers.powers;

import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class CopyCatPower extends AbstractPower {
    public static final String POWER_ID = makeID(CopyCatPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    private boolean justApplied = false;
    private AbstractCard card;
    
    public CopyCatPower(AbstractCreature owner, int amount, AbstractCard card) {
        this.name = powerStrings.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.loadRegion("vigor");
        this.type = PowerType.BUFF;
        this.isTurnBased = false;
        this.justApplied = true;
        this.card = card;
        updateDescription();
    }
    
    public void updateDescription() {
        this.description = powerStrings.DESCRIPTIONS[0] + CardCrawlGame.languagePack.getCardStrings(card.cardID).NAME + powerStrings.DESCRIPTIONS[1] + this.amount + powerStrings.DESCRIPTIONS[2];
    }
    
    public float atDamageGive(float damage, DamageInfo.DamageType type) {
        return type == DamageInfo.DamageType.NORMAL ? damage + (float) this.amount : damage;
    }
    
    public void atEndOfRound() {
        if (!justApplied) {
            this.addToBot(new RemoveSpecificPowerAction(this.owner, this.owner, this));
        }
        justApplied = false;
    }
}
