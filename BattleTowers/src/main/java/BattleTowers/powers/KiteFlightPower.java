package BattleTowers.powers;

import BattleTowers.powers.abstracts.AbstractBTPower;
import BattleTowers.powers.abstracts.PowerRemovalNotifier;
import BattleTowers.util.UC;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;

import static BattleTowers.BattleTowers.makeID;

public class KiteFlightPower extends AbstractBTPower {
    public static final String POWER_ID = makeID(KiteFlightPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;
    public static float damageReduction = 0.5f;

    public KiteFlightPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        updateDescription();
        loadRegion("flight");
        this.priority = 50;
    }

    public void playApplyPowerSfx() {
        CardCrawlGame.sound.play("POWER_FLIGHT", 0.05F);
    }

    public void updateDescription() {
        this.description = String.format(DESCRIPTIONS[0], MathUtils.round(damageReduction*100));
    }

    public float atDamageFinalReceive(float damage, DamageInfo.DamageType type) {
        return calculateDamageTakenAmount(damage, type);
    }

    private float calculateDamageTakenAmount(float damage, DamageInfo.DamageType type) {
        if (type != DamageInfo.DamageType.HP_LOSS && type != DamageInfo.DamageType.THORNS)
            return damage / 2.0F;
        return damage;
    }

    public int onAttacked(DamageInfo info, int damageAmount) {
        if (info.owner != null && info.type != DamageInfo.DamageType.HP_LOSS && info.type != DamageInfo.DamageType.THORNS && damageAmount > 0) {
            flash();
            UC.generalPowerLogic(this);
        }
        return damageAmount;
    }

    public void onRemove() {
        if(owner instanceof PowerRemovalNotifier)
            ((PowerRemovalNotifier) owner).onPowerRemoved(this);
    }
}
