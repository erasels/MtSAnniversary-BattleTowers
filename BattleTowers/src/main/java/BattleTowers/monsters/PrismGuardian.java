package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import MonsterTowerModDayvig.actions.PrismStasisAction;
import MonsterTowerModDayvig.powers.PrismShield;
import MonsterTowerModDayvig.powers.WrathPower;
import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.powers.BarricadePower;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.HeartBuffEffect;
import com.megacrit.cardcrawl.vfx.combat.LaserBeamEffect;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;

public class PrismGuardian extends CustomMonster {
    public static final String ID = BattleTowers.makeID(PrismGuardian.class.getSimpleName());
    private static final MonsterStrings monsterStrings;
    public static final String NAME;
    public static final String[] MOVES;
    public static final String[] DIALOG;
    private static final int HP = 160;
    private static final int LASER_DMG = 12;
    private int laserDmg = LASER_DMG;
    private static final byte STASIS = 1;
    private static final byte LASER = 2;
    private static final byte NULLBEAM = 3;
    private static final int NULLBEAMTURN = 6;
    private int count = 0;
    private boolean firstMove;

    public PrismGuardian(float x, float y) {
        super(NAME, ID, 60, 0.0F, 0.0F, 280.0F, 280.0F, "MonsterTowerModDayvigResources/images/enemies/prismguardian.png", x, y);

        this.setHp(HP);
        this.firstMove = true;

        this.damage.add(new DamageInfo(this, this.laserDmg));
        this.animation = null;
    }

    public void usePreBattleAction() {
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new PrismShield(this, this, 0)));
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new ArtifactPower(this, 1)));
    }

    public void changeLaserDamage(int l){
        this.damage.set(0, new DamageInfo(this, laserDmg + l));
        this.setMove(LASER, Intent.ATTACK, ((DamageInfo) this.damage.get(0)).base);
        this.createIntent();

    }

    public void takeTurn() {
        switch(this.nextMove) {
            case STASIS:
                AbstractDungeon.actionManager.addToBottom(new PrismStasisAction(this));
                break;
            case LASER:
                AbstractDungeon.actionManager.addToBottom(new SFXAction("ATTACK_MAGIC_BEAM_SHORT", 0.5F));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new BorderFlashEffect(Color.SKY)));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new SmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY), 0.3F));
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo)this.damage.get(0), AttackEffect.NONE));
                break;
            case NULLBEAM:
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new BorderFlashEffect(Color.LIME)));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new LaserBeamEffect(this.hb.cX, this.hb.cY + 60.0F * Settings.scale), 1.5F));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player, this, new DexterityPower(AbstractDungeon.player, -3),-3));
                break;
        }



        AbstractDungeon.actionManager.addToBottom(new RollMoveAction(this));
    }

    private void playDeathSfx() {
        if (MathUtils.randomBoolean()) {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("SPHERE_DETECT_VO_1"));
        } else {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("SPHERE_DETECT_VO_2"));
        }
    }

    protected void getMove(int num) {
        if (this.firstMove){
            this.setMove(STASIS, Intent.STRONG_DEBUFF);
            this.firstMove = false;
        }
        else if (this.count == NULLBEAMTURN){
            this.setMove(NULLBEAM, Intent.STRONG_DEBUFF);
            this.count = -1;
        }
        else {
            changeLaserDamage(0);
        }
        this.count++;
        this.createIntent();

    }

    public void die() {
        super.die();
        this.playDeathSfx();
    }

    static {
        monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
        NAME = monsterStrings.NAME;
        MOVES = monsterStrings.MOVES;
        DIALOG = monsterStrings.DIALOG;
    }
}

