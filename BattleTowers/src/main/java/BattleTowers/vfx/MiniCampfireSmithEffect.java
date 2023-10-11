package BattleTowers.vfx;

import BattleTowers.events.phases.MiniRestPhase;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.campfire.CampfireSmithEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import com.evacipated.cardcrawl.mod.stslib.relics.BetterOnSmithRelic;

public class MiniCampfireSmithEffect extends AbstractGameEffect {
    private static final float DUR = 1.5F;
    private boolean openedScreen = false;
    private Color screenColor;
    
    private MiniRestPhase src;

    public MiniCampfireSmithEffect(MiniRestPhase src) {
        this.src = src;
        this.screenColor = AbstractDungeon.fadeColor.cpy();
        this.duration = DUR;
        this.screenColor.a = 0.0F;
        AbstractDungeon.overlayMenu.proceedButton.hide();
    }

    public void update() {
        if (!AbstractDungeon.isScreenUp) {
            this.duration -= Gdx.graphics.getDeltaTime();
            this.updateBlackScreenColor();
        }

        if (!AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty() && AbstractDungeon.gridSelectScreen.forUpgrade) {
            for (AbstractCard c : AbstractDungeon.gridSelectScreen.selectedCards) {
                AbstractDungeon.effectsQueue.add(new UpgradeShineEffect((float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));
                c.upgrade();
                AbstractDungeon.player.relics.stream().filter(r -> r instanceof BetterOnSmithRelic).forEach(r -> ((BetterOnSmithRelic)r).betterOnSmith(c));
                AbstractDungeon.player.bottledCardUpgradeCheck(c);
                AbstractDungeon.effectsQueue.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy()));
            }

            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            src.fadeIn();
        }

        if (this.duration < 1.0F && !this.openedScreen) {
            this.openedScreen = true;
            AbstractDungeon.gridSelectScreen.open(AbstractDungeon.player.masterDeck.getUpgradableCards(), 1, CampfireSmithEffect.TEXT[0], true, false, true, false);

            for (AbstractRelic r : AbstractDungeon.player.relics) {
                r.onSmith();
            }
        }
        else if (this.duration < 0.0F) {
            this.isDone = true;
            if (MiniRestPhase.MiniCampfireUI.hidden) {
                //Wasn't shown when cancel button was pressed, upgrade happened and is done.
                src.cutFireSound();
                src.finish(false);
            }
        }
    }

    private void updateBlackScreenColor() {
        if (this.duration > 1.0F) {
            this.screenColor.a = Interpolation.fade.apply(1.0F, 0.0F, (this.duration - 1.0F) * 2.0F);
        } else {
            this.screenColor.a = Interpolation.fade.apply(0.0F, 1.0F, this.duration / 1.5F);
        }
    }

    public void render(SpriteBatch sb) {
        sb.setColor(this.screenColor);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, (float)Settings.WIDTH, (float)Settings.HEIGHT);
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID) {
            AbstractDungeon.gridSelectScreen.render(sb);
        }
    }

    public void dispose() {
    }
}
