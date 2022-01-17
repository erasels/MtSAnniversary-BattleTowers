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

import java.util.ArrayList;

public class PepperSprayAction extends AbstractGameAction {
    private AbstractMonster m;
    private static final float DURATION = 2F;
    private static final Texture PEPPER_IMAGE_RED =
            new Texture("battleTowersResources/img/vfx/vegetables/BellPepperRed.png");
    private static final Texture PEPPER_IMAGE_ORANGE =
            new Texture("battleTowersResources/img/vfx/vegetables/BellPepperOrange.png");
    private static final Texture PEPPER_IMAGE_YELLOW =
            new Texture("battleTowersResources/img/vfx/vegetables/BellPepperYellow.png");
    private static final Texture PEPPER_IMAGE_GREEN =
            new Texture("battleTowersResources/img/vfx/vegetables/BellPepperGreen.png");
    private static final Texture[] PEPPERS = {PEPPER_IMAGE_RED, PEPPER_IMAGE_ORANGE,
            PEPPER_IMAGE_YELLOW, PEPPER_IMAGE_GREEN};
    private AbstractPlayer p = AbstractDungeon.player;
    private DamageInfo info;
    private int peppersFired = 0;
    private int peppersLanded = 0;

    public PepperSprayAction(AbstractMonster monster, DamageInfo info) {
        this.m = monster;
        this.info = info;
        actionType = ActionType.DAMAGE;
        duration = DURATION;
    }

    public void update() {
        if (m == null) {
            isDone = true;
            return;
        }

        ArrayList<Texture> pepperImages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int pepperIndex = AbstractDungeon.miscRng.random(0, 3);
            pepperImages.add(PEPPERS[pepperIndex]);
        }

        ArrayList<Float> targetX = new ArrayList<>();
        ArrayList<Float> targetY = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            float x = p.hb.cX + AbstractDungeon.miscRng.random(-50.0f*Settings.xScale, 50.0f*Settings.xScale);
            float y = p.hb.cY + AbstractDungeon.miscRng.random(-50.0f*Settings.yScale, 50.0f*Settings.yScale);

            targetX.add(x);
            targetY.add(y);
        }

        if (duration <= DURATION - 0.1f * peppersFired && peppersFired < 5) {
            PepperEffect(pepperImages.get(peppersFired), targetX.get(peppersFired), targetY.get(peppersFired));
            peppersFired++;
        }

        if (duration <= DURATION - 0.5f - 0.1f*peppersLanded && peppersLanded < 5) {
            AbstractDungeon.effectList.add(new FlashAtkImgEffect(targetX.get(peppersLanded), targetY.get(peppersLanded),
                    AttackEffect.BLUNT_LIGHT));
            if (p != null && p.currentHealth > 0) {
                p.damage(info);
                if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                    AbstractDungeon.actionManager.clearPostCombatActions();
                }
            }
            peppersLanded++;
        }

        tickDuration();
        if (peppersLanded >= 5)
            isDone = true;
    }

    private void PepperEffect(Texture pepperImage, float targetX, float targetY) {
        float targetX2 = targetX + AbstractDungeon.miscRng.random(-400.0f*Settings.xScale, 400.0f*Settings.xScale);
        float targetY2 = targetY + AbstractDungeon.miscRng.random(-400.0f*Settings.yScale, 400.0f*Settings.yScale);

        AbstractGameEffect pepperEffect = new VfxBuilder(pepperImage, m.hb.cX, m.hb.cY, 0.5f)
                .moveX(m.hb.cX, targetX, VfxBuilder.Interpolations.LINEAR)
                .moveY(m.hb.cY, targetY, VfxBuilder.Interpolations.LINEAR)
                .rotate(720.0f)
                .andThen(0.5f)
                .moveX(targetX, targetX2, VfxBuilder.Interpolations.LINEAR)
                .moveY(targetY, targetY2, VfxBuilder.Interpolations.LINEAR)
                .rotate(360.0f)
                .fadeOut(0.5f)
                .build();

        AbstractDungeon.topLevelEffects.add(pepperEffect);
        if (peppersFired % 2 == 0)
            CardCrawlGame.sound.play(BattleTowers.PEW_KEY);
    }
}
