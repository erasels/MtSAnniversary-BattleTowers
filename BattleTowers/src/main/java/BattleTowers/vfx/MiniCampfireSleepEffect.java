package BattleTowers.vfx;

import BattleTowers.events.phases.MiniRestPhase;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.DreamCatcher;
import com.megacrit.cardcrawl.relics.RegalPillow;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.campfire.CampfireSleepEffect;

import java.util.ArrayList;

public class MiniCampfireSleepEffect extends AbstractGameEffect {
    private static final float HEAL_AMOUNT = 0.3F;
    private static final float DUR = 3.0F;
    private static final float FAST_MODE_DUR = 1.5F;
    private boolean hasHealed = false;
    private int healAmount;
    private Color screenColor;

    private MiniRestPhase src;
    
    public MiniCampfireSleepEffect(MiniRestPhase src) {
        this.src = src;
        this.screenColor = AbstractDungeon.fadeColor.cpy();
        if (Settings.FAST_MODE) {
            this.startingDuration = FAST_MODE_DUR;
        } else {
            this.startingDuration = DUR;
        }

        this.duration = this.startingDuration;
        this.screenColor.a = 0.0F;
        src.cutFireSound();
        AbstractDungeon.overlayMenu.proceedButton.hide();
        if (ModHelper.isModEnabled("Night Terrors")) {
            this.healAmount = AbstractDungeon.player.maxHealth;
            AbstractDungeon.player.decreaseMaxHealth(5);
        } else {
            this.healAmount = (int)(AbstractDungeon.player.maxHealth * HEAL_AMOUNT);
        }

        if (AbstractDungeon.player.hasRelic(RegalPillow.ID)) {
            this.healAmount += 15;
        }
    }


    public void update() {
        this.duration -= Gdx.graphics.getDeltaTime();
        this.updateBlackScreenColor();
        if (this.duration < this.startingDuration - 0.5F && !this.hasHealed) {
            this.playSleepJingle();
            this.hasHealed = true;
            AbstractRelic pillow = AbstractDungeon.player.getRelic(RegalPillow.ID);
            if (pillow != null) {
                pillow.flash();
            }

            AbstractDungeon.player.heal(this.healAmount, false);

            for (AbstractRelic r : AbstractDungeon.player.relics) {
                r.onRest();
            }
        }

        if (this.duration < this.startingDuration / 2.0F) {
            AbstractRelic catcher = AbstractDungeon.player.getRelic(DreamCatcher.ID);
            if (catcher != null) {
                catcher.flash();
                ArrayList<AbstractCard> rewardCards = AbstractDungeon.getRewardCards();
                if (rewardCards != null && !rewardCards.isEmpty()) {
                    AbstractDungeon.cardRewardScreen.open(rewardCards, null, CampfireSleepEffect.TEXT[0]);
                }
            }

            this.isDone = true;
            src.fadeIn();
            src.finish(false);
        }
    }

    private void playSleepJingle() {
        int roll = MathUtils.random(0, 2);
        switch (AbstractDungeon.id) {
            case Exordium.ID:
                if (roll == 0) {
                    CardCrawlGame.sound.play("SLEEP_1-1");
                } else if (roll == 1) {
                    CardCrawlGame.sound.play("SLEEP_1-2");
                } else {
                    CardCrawlGame.sound.play("SLEEP_1-3");
                }
                break;
            case TheBeyond.ID:
                if (roll == 0) {
                    CardCrawlGame.sound.play("SLEEP_3-1");
                } else if (roll == 1) {
                    CardCrawlGame.sound.play("SLEEP_3-2");
                } else {
                    CardCrawlGame.sound.play("SLEEP_3-3");
                }
                break;
            default:
                if (roll == 0) {
                    CardCrawlGame.sound.play("SLEEP_2-1");
                } else if (roll == 1) {
                    CardCrawlGame.sound.play("SLEEP_2-2");
                } else {
                    CardCrawlGame.sound.play("SLEEP_2-3");
                }
                break;
        }
    }

    private void updateBlackScreenColor() {
        if (this.duration > this.startingDuration - 0.5F) {
            this.screenColor.a = Interpolation.fade.apply(1.0F, 0.0F, (this.duration - (this.startingDuration - 0.5F)) * 2.0F);
        } else if (this.duration < 1.0F) {
            this.screenColor.a = Interpolation.fade.apply(0.0F, 1.0F, this.duration);
        } else {
            this.screenColor.a = 1.0F;
        }
    }

    public void render(SpriteBatch sb) {
        sb.setColor(this.screenColor);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, (float)Settings.WIDTH, (float)Settings.HEIGHT);
    }

    public void dispose() {
    }
}
