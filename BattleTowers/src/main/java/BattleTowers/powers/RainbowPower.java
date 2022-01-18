package BattleTowers.powers;

import basemod.interfaces.CloneablePowerInterface;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class RainbowPower extends AbstractPower implements CloneablePowerInterface {
    public static final String POWER_ID = makeID(RainbowPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;
    private final int timerOffset;

    public RainbowPower(AbstractCreature owner) {
        this(owner, AbstractDungeon.miscRng.random(0, 5000));
    }

    public RainbowPower(AbstractCreature owner, int timerOffset) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.type = PowerType.BUFF;
        this.loadRegion("buffer");
        this.timerOffset = timerOffset;
    }
    public void updateDescription() {
        description = DESCRIPTIONS[0];
    }

    @Override
    public void renderIcons(SpriteBatch sb, float x, float y, Color c) {
        Color backup = new Color(c);
        c.set(
                (MathUtils.cosDeg((float)((System.currentTimeMillis() + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                (MathUtils.cosDeg((float)((System.currentTimeMillis() + 1000L + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                (MathUtils.cosDeg((float)((System.currentTimeMillis() + 2000L + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                c.a);
        super.renderIcons(sb, x, y, c);
        c.set(backup);
    }


    public float atDamageFinalReceive(float damage, DamageInfo.DamageType type) {
        if (damage > 1.0F) {
            damage = 1.0F;
        }
        return damage;
    }

    @Override
    public AbstractPower makeCopy() {
        return new RainbowPower(owner, timerOffset);
    }
}