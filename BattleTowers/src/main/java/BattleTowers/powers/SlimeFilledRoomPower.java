package BattleTowers.powers;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;

public class SlimeFilledRoomPower extends AbstractPower {
    public static final String POWER_ID = makeID(SlimeFilledRoomPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public SlimeFilledRoomPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.priority = 50;
        this.type = PowerType.BUFF;
        BattleTowers.LoadPowerImage(this);
    }

    @Override
    public void onInitialApplication() {
        int slimes = (int)AbstractDungeon.player.masterDeck.group.stream().filter(c -> c.rarity != AbstractCard.CardRarity.CURSE && c.rarity != AbstractCard.CardRarity.BASIC).count();
        int slimesPerBatch = 5;
        while (slimes > 0) {
            this.addToBot(new MakeTempCardInDrawPileAction(new Slimed(), Math.min(slimes, slimesPerBatch), true, true));
            this.addToBot(new WaitAction(0.1f));
            slimes -= slimesPerBatch;
        }
    }

    public void onPlayerExhaust(AbstractCard c) {
        if (c.cardID.equals(Slimed.ID)) {
            this.flash();
            this.addToBot(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, -1)));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}
