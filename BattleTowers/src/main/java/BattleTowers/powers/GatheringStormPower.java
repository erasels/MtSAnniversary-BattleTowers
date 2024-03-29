package BattleTowers.powers;

import com.evacipated.cardcrawl.mod.stslib.patches.NeutralPowertypePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class GatheringStormPower extends AbstractPower {
    public static final String POWER_ID = makeID(GatheringStormPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public GatheringStormPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.amount = amount;
        this.type = NeutralPowertypePatch.NEUTRAL;
        this.loadRegion("storm");
    }
    public void atEndOfTurn(boolean isPlayer) {
        boolean found = false;
        for (AbstractCard c : AbstractDungeon.actionManager.cardsPlayedThisTurn){
            if (c.type == AbstractCard.CardType.ATTACK){
                found = true;
                break;
            }
        }
        if (!found){
            stackPower(1);
        }
    }
    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}