package BattleTowers.vfx;

import BattleTowers.BattleTowers;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;

public class LoseMaxHPEffect extends AbstractGameEffect {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(BattleTowers.makeID("LoseMHPEffect"));
    public static final String[] TEXT = uiStrings.TEXT;
    private int mhp = 0;
    private boolean reachedCenter;
    private float x;
    private float y;
    private float destinationY;
    private static final float WAIT_TIME = 1.0F;
    private float waitTimer = WAIT_TIME;
    private float fadeTimer = WAIT_TIME;
    private static final float FADE_Y_SPEED = 100.0F * Settings.scale;
    private static final float TEXT_DURATION = 3.0F;

    public LoseMaxHPEffect(int amount) {
        this.x = AbstractDungeon.player.hb.cX;
        this.y = AbstractDungeon.player.hb.cY;
        this.destinationY = (this.y + 150.0F * Settings.scale);
        this.duration = TEXT_DURATION;
        this.startingDuration = TEXT_DURATION;
        this.reachedCenter = false;
        this.mhp = amount;
        this.color = Color.SALMON.cpy();
    }

    public void update() {
        if (this.waitTimer > 0.0F) {
            if ((!this.reachedCenter) && (y != destinationY)) {
                this.y = MathUtils.lerp(y, destinationY, Gdx.graphics.getDeltaTime() * 9.0F);
                if (Math.abs(y - this.destinationY) < Settings.UI_SNAP_THRESHOLD) {
                    y = destinationY;
                    reachedCenter = true;
                }
            } else {
                this.waitTimer -= Gdx.graphics.getDeltaTime();
            }
        } else {
            this.y += Gdx.graphics.getDeltaTime() * FADE_Y_SPEED;
            this.fadeTimer -= Gdx.graphics.getDeltaTime();
            this.color.a = fadeTimer;
            if (fadeTimer < 0.0F) {
                isDone = true;
            }
        }
    }

    public void render(SpriteBatch sb) {
        if (!this.isDone) {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, "- " + mhp + TEXT[0], x, y, color);
        }
    }

    public void dispose() {
    }
}
