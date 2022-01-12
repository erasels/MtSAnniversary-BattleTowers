package BattleTowers.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class FeelMyPainPower extends AbstractPower {
    public static final String POWER_ID = makeID(FeelMyPainPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    private boolean triggeredThisTurn = false;

    public FeelMyPainPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.type = PowerType.BUFF;
        this.loadRegion("noPain");
    }

    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (!triggeredThisTurn) {
            this.flash();
            triggeredThisTurn = true;
            int returnDamage = Math.min(owner.currentHealth, damageAmount);
            addToTop(new DamageAction(AbstractDungeon.player, new DamageInfo(this.owner, returnDamage, DamageInfo.DamageType.THORNS), AbstractGameAction.AttackEffect.POISON, true));
        }
        return damageAmount;
    }

    @Override
    public void atEndOfRound() {
        triggeredThisTurn = false;
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}
