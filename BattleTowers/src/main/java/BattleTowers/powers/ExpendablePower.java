package BattleTowers.powers;

import BattleTowers.BattleTowers;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.HealthBarRenderPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class ExpendablePower extends AbstractPower implements HealthBarRenderPower {
    public static final String POWER_ID = makeID("ExpendablePower");
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    private AbstractCreature source;
    private Color hpColor = new Color(0.7F, 0.55F, 0.8F, 0.75F);

    private boolean skip;

    public ExpendablePower(AbstractCreature owner, AbstractCreature source, int amount, boolean skip) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.source = source;
        this.type = PowerType.DEBUFF;
        this.isTurnBased = false;
        BattleTowers.LoadPowerImage(this);
        this.amount = amount;
        this.skip = skip;

        this.updateDescription();
    }

    public void atEndOfTurn(boolean isPlayer) {
        if (skip) {
            skip = false;
        }
        else {
            this.flashWithoutSound();
            this.addToBot(new DamageAction(this.owner, new DamageInfo(this.source, this.amount, DamageInfo.DamageType.HP_LOSS), AbstractGameAction.AttackEffect.POISON));
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }

    @Override
    public int getHealthBarAmount() {
        return skip ? 0 : this.amount;
    }

    @Override
    public Color getColor() {
        return hpColor;
    }
}
