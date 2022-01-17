package BattleTowers.powers;

import basemod.interfaces.CloneablePowerInterface;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class TimeStop extends AbstractPower implements CloneablePowerInterface {
    public static final String POWER_ID = makeID(TimeStop.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;
    public static int cardsBlockedThisturn;

    public TimeStop(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = 1;
        this.updateDescription();
        this.type = PowerType.DEBUFF;
        this.loadRegion("time");
    }
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (!card.purgeOnUse && AbstractDungeon.actionManager.cardsPlayedThisTurn.size() - cardsBlockedThisturn <= this.amount) {
            ++cardsBlockedThisturn;
            this.flash();

            AbstractDungeon.actionManager.removeFromQueue(card);
        }

    }
    public void atStartOfTurn() {
        cardsBlockedThisturn = 0;
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new TimeStop(owner);
    }
}