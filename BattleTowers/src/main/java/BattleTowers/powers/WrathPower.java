package BattleTowers.powers;

import BattleTowers.BattleTowers;
import MonsterTowerModDayvig.particles.WrathParticleEffect2;
import MonsterTowerModDayvig.particles.WrathStanceEffectMonster;
import basemod.interfaces.CloneablePowerInterface;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;


//Gain 1 dex for the turn for each card played.

public class WrathPower extends AbstractPower implements CloneablePowerInterface {
    public AbstractCreature source;
    public float particleTimer;
    public float particleTimer2;

    public static final String POWER_ID = BattleTowers.makeID("WrathPower");
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    // We create 2 new textures *Using This Specific Texture Loader* - an 84x84 image and a 32x32 one.
    // There's a fallback "missing texture" image, so the game shouldn't crash if you accidentally put a non-existent file.
    //private static final Texture tex84 = TextureLoader.getTexture(makePowerPath("honeypower84.png"));
    //private static final Texture tex32 = TextureLoader.getTexture(makePowerPath("honeypower32.png"));

    public WrathPower(final AbstractCreature owner, final AbstractCreature source, final int amount) {
        name = NAME;
        ID = POWER_ID;

        this.owner = owner;
        this.amount = amount;
        this.source = source;

        type = PowerType.BUFF;
        isTurnBased = false;

        // We load those txtures here.
        //this.region128 = new TextureAtlas.AtlasRegion(tex84, 0, 0, 84, 84);
        //this.region48 = new TextureAtlas.AtlasRegion(tex32, 0, 0, 32, 32);
        this.loadRegion("flex");
        updateDescription();
    }

    public float atDamageGive(float damage, DamageInfo.DamageType type) {
        return type == DamageInfo.DamageType.NORMAL ? damage * 2.0F : damage;
    }

    public float atDamageReceive(float damage, DamageInfo.DamageType type) {
        return type == DamageInfo.DamageType.NORMAL ? damage * 2.0F : damage;
    }

    @Override
    public void updateParticles() {
        if (!Settings.DISABLE_EFFECTS) {
            this.particleTimer -= Gdx.graphics.getDeltaTime();
            if (this.particleTimer < 0.0F) {
                this.particleTimer = 0.05F;
                float thisX = this.owner.hb.cX + MathUtils.random(-this.owner.hb.width / 2.0F - 30.0F * Settings.scale, this.owner.hb.width / 2.0F + 30.0F * Settings.scale);
                float thisY = this.owner.hb.cY + MathUtils.random(-this.owner.hb.height / 2.0F - -10.0F * Settings.scale, this.owner.hb.height / 2.0F - 10.0F * Settings.scale);
                AbstractDungeon.effectsQueue.add(new WrathParticleEffect2(thisX, thisY));
            }
        }

        this.particleTimer2 -= Gdx.graphics.getDeltaTime();
        if (this.particleTimer2 < 0.0F) {
            this.particleTimer2 = MathUtils.random(0.3F, 0.4F);
            AbstractDungeon.effectsQueue.add(new WrathStanceEffectMonster("Wrath", this.owner));
        }

    }

    // Update the description when you apply this power. (i.e. add or remove an "s" in keyword(s))
    @Override
    public void updateDescription() {
            description = DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new WrathPower(owner, source, amount);
    }
}
