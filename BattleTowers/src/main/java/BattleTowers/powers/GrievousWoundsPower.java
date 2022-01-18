package BattleTowers.powers;

import BattleTowers.actions.DecreaseMaxHPAction;
import BattleTowers.powers.abstracts.AbstractBTPower;
import BattleTowers.util.UC;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;

import static BattleTowers.BattleTowers.makeID;

public class GrievousWoundsPower extends AbstractBTPower {
    public static final String POWER_ID = makeID("GrievousWounds");
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public GrievousWoundsPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        type = PowerType.DEBUFF;
        updateDescription();
        setImage("GrievousWounds_big.png", "GrievousWounds_small.png");
    }

    public void updateDescription() {
        this.description = String.format(DESCRIPTIONS[0], amount);
    }

    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > 0 && info.type != DamageInfo.DamageType.THORNS && info.type != DamageInfo.DamageType.HP_LOSS) {
            flashWithoutSound();
            UC.atb(new DecreaseMaxHPAction(owner, amount));
        }
        return super.onAttacked(info, damageAmount);
    }
}
