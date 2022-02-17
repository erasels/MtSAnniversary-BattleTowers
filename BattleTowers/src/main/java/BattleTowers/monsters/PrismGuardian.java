package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.actions.PrismStasisAction;
import BattleTowers.powers.PrismPower;
import BattleTowers.powers.PrismShield;
import BattleTowers.vfx.ColoredLargeLaserEffect;
import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.LaserBeamEffect;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;

public class PrismGuardian extends CustomMonster {
    public static final String ID = BattleTowers.makeID(PrismGuardian.class.getSimpleName());
    private static final String IMG = BattleTowers.makeImagePath("monsters/PrismGuardian/prismguardian.png");
    private static final MonsterStrings monsterStrings;
    public static final String NAME;
    public static final String[] MOVES;
    public static final String[] DIALOG;
    private static final int HP = 160;
    private static final int HP_ASC7 = 180;
    private static final int LASER_DMG = 12;
    private int laserDmg = LASER_DMG;
    private static final byte STASIS = 1;
    private static final byte LASER = 2;
    private static final byte NULLBEAM = 3;
    private static final int NULLBEAMTURN = 4;
    private int count = 0;
    private boolean firstMove;

    public PrismGuardian(float x, float y) {
        super(NAME, ID, 60, 0.0F, 0.0F, 280.0F, 280.0F, IMG, x, y);

        if (AbstractDungeon.ascensionLevel >= 7) {
            this.setHp(HP_ASC7);
        }
        else {
            this.setHp(HP);
        }
        if (AbstractDungeon.ascensionLevel >= 2) {
            laserDmg = 12;
        }
        else {
            laserDmg = 10;
        }


        this.firstMove = true;

        this.damage.add(new DamageInfo(this, this.laserDmg));
        this.animation = null;
    }

    public void usePreBattleAction() {
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new PrismShield(this, this, 0)));
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new PrismPower(this, this, 0)));
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new ArtifactPower(this, 1)));
    }

    public void changeLaserDamage(int l){
        this.damage.set(0, new DamageInfo(this, laserDmg + l));
        this.setMove(LASER, Intent.ATTACK, ((DamageInfo) this.damage.get(0)).base);
        if (this.hasPower(PrismPower.POWER_ID) && this.hasPower(PrismShield.POWER_ID)){
            if (this.getPower(PrismShield.POWER_ID).amount > 20) {
                this.getPower(PrismPower.POWER_ID).amount = this.getPower(PrismShield.POWER_ID).amount - 20;
                this.getPower(PrismPower.POWER_ID).flash();
                this.getPower(PrismPower.POWER_ID).updateDescription();
            }
        }
        this.createIntent();
    }

    public void takeTurn() {
        switch(this.nextMove) {
            case STASIS:
                AbstractDungeon.actionManager.addToBottom(new PrismStasisAction(this));
                break;
            case LASER:
                AbstractDungeon.actionManager.addToBottom(new SFXAction("ATTACK_MAGIC_BEAM_SHORT", 0.5F, true));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new BorderFlashEffect(Color.SKY)));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new SmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY), 0.3F));
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo)this.damage.get(0), AttackEffect.NONE));
                break;
            case NULLBEAM:
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new BorderFlashEffect(Color.LIME)));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new ColoredLargeLaserEffect(this.hb.cX, this.hb.cY + 60.0F * Settings.scale, Color.LIME.cpy()), 1.5F));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player, this, new DexterityPower(AbstractDungeon.player, -2),-2));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player, this, new FocusPower(AbstractDungeon.player, -2),-2));
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
            if (AbstractDungeon.ascensionLevel >= 17){
                this.setMove(NULLBEAM, Intent.STRONG_DEBUFF);
                this.firstMove = false;
            }
            else {
                changeLaserDamage(0);
                this.createIntent();
                this.count++;
                this.firstMove = false;
            }
        }
        else if (this.count >= NULLBEAMTURN){
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

