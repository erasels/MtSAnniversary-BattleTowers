package BattleTowers.powers;

import BattleTowers.BattleTowers;
import BattleTowers.util.UC;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;

public class BookGrowthPower extends AbstractPower {
    public static final String POWER_ID = makeID(BookGrowthPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public BookGrowthPower(AbstractMonster owner) {
        name = NAME;
        ID = POWER_ID;
        this.owner = owner;
        this.amount = 1;
        type = PowerType.BUFF;
        BattleTowers.LoadPowerImage(this);
        updateDescription();
    }


    @Override
    public void atEndOfRound() {
        super.atEndOfRound();
        this.amount++;
        if (this.amount == 4){
            UC.doPow(this.owner, new StrengthPower(this.owner, 1));
            this.amount = 1;
        }
    }

    @Override
    public void updateDescription() {
        description = DESCRIPTIONS[0];
    }
}
