package BattleTowers.powers;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;

public class SuperRitualPower extends AbstractPower {
    public static final String POWER_ID = makeID(SuperRitualPower.class.getSimpleName());
    private static final PowerStrings powerStrings;
    public static final String NAME;
    public static final String[] DESCRIPTIONS;
    private boolean skipFirst = true;
    private boolean onPlayer;

    public SuperRitualPower(AbstractCreature owner, int amount, boolean playerControlled) {
        name = NAME;
        ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        onPlayer = playerControlled;
        updateDescription();
        BattleTowers.LoadPowerImage(this);
    }

    public void updateDescription() {
        description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }

    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            flash();
            addToBot(new ApplyPowerAction(owner, owner, new StrengthPower(owner, amount), amount));
            addToBot(new ApplyPowerAction(owner, owner, new DexterityPower(owner, amount), amount));
        }

    }

    public void atEndOfRound() {
        if (!onPlayer) {
            if (!skipFirst) {
                flash();
                addToBot(new ApplyPowerAction(owner, owner, new StrengthPower(owner, amount), amount));
                addToBot(new ApplyPowerAction(owner, owner, new DexterityPower(owner, amount), amount));
            } else {
                skipFirst = false;
            }
        }

    }

    static {
        powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
        NAME = powerStrings.NAME;
        DESCRIPTIONS = powerStrings.DESCRIPTIONS;
    }
}
