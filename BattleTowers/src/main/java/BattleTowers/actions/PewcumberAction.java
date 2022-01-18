package BattleTowers.actions;

import BattleTowers.BattleTowers;
import basemod.helpers.VfxBuilder;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;


public class PewcumberAction extends AbstractGameAction {
    private AbstractMonster m;
    private static final float DURATION = 1F;
    private static final Texture CUCUMBER_IMAGE =
            new Texture("battleTowersResources/img/vfx/vegetables/Cucumber.png");
    private AbstractPlayer p = AbstractDungeon.player;
    private DamageInfo info;
    private boolean thunkEffect;

    public PewcumberAction(AbstractMonster monster, DamageInfo info) {
        this.m = monster;
        this.info = info;
        actionType = ActionType.DAMAGE;
        duration = DURATION;
        thunkEffect = false;
    }

    public void update() {
        if (m == null) {
            isDone = true;
            return;
        }

        float targetX = 0f;
        float targetY = 0f;
        if (duration == DURATION) {
            targetX = p.hb.cX + AbstractDungeon.miscRng.random(-25.0f*Settings.xScale, 25.0f*Settings.xScale);
            targetY = p.hb.cY + AbstractDungeon.miscRng.random(-25.0f*Settings.yScale, 25.0f*Settings.yScale);
            float targetX2 = targetX + AbstractDungeon.miscRng.random(-400.0f*Settings.xScale, 400.0f*Settings.xScale);
            float targetY2 = targetY + AbstractDungeon.miscRng.random(-400.0f*Settings.yScale, 400.0f*Settings.yScale);
            AbstractGameEffect cucumberEffect = new VfxBuilder(CUCUMBER_IMAGE, m.hb.cX, m.hb.cY, 0.5f)
                    .moveX(m.hb.cX, targetX, VfxBuilder.Interpolations.LINEAR)
                    .moveY(m.hb.cY, targetY, VfxBuilder.Interpolations.LINEAR)
                    .rotate(720.0f)
                    .andThen(0.5f)
                    .moveX(targetX, targetX2, VfxBuilder.Interpolations.LINEAR)
                    .moveY(targetY, targetY2, VfxBuilder.Interpolations.LINEAR)
                    .rotate(360.0f)
                    .fadeOut(0.5f)
                    .build();

            AbstractDungeon.topLevelEffects.add(cucumberEffect);
            CardCrawlGame.sound.play(BattleTowers.PEW_KEY);
        }

        if (duration <= DURATION - 0.5f && !thunkEffect) {
            thunkEffect = true;
            AbstractDungeon.effectList.add(new FlashAtkImgEffect(targetX, targetY, AttackEffect.BLUNT_HEAVY));
            if (p != null && p.currentHealth > 0) {
                p.damage(info);
                if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                    AbstractDungeon.actionManager.clearPostCombatActions();
                }
            }
        }

        tickDuration();
    }
}
