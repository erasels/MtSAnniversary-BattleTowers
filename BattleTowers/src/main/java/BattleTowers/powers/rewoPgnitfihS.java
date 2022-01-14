package BattleTowers.powers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;

public class rewoPgnitfihS extends AbstractPower {
    public static final String POWER_ID = makeID(rewoPgnitfihS.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public rewoPgnitfihS(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = -1;
        this.isTurnBased = false;
        this.updateDescription();
        this.type = PowerType.BUFF;
        this.isPostActionPower = true;
        this.loadRegion("shift");

        this.region48 = new TextureAtlas.AtlasRegion(this.region48);
        this.region128 = new TextureAtlas.AtlasRegion(this.region128);

        this.region48.flip(true, false);
        this.region128.flip(true, false);
    }

    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > 0) {
            this.addToTop(new ApplyPowerAction(this.owner, this.owner, new StrengthPower(this.owner, damageAmount), damageAmount));
            this.flash();
        }

        return damageAmount;
    }

    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}
