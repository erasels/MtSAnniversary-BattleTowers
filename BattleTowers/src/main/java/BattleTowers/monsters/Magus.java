package BattleTowers.monsters;

import BattleTowers.vfx.ColoredSmallLaserEffect;
import BattleTowers.BattleTowers;
import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.animations.*;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.*;

public class Magus extends CustomMonster {
    public static final String ID = BattleTowers.makeID(Magus.class.getSimpleName());
    private static final String IMG = BattleTowers.makeImagePath("monsters/Magus/magus.png");
    private static final MonsterStrings monsterStrings;
    public static final String NAME;
    public static final String[] MOVES;
    public static final String[] DIALOG;
    private int blastDmg = 30;
    private int confuseAmount = 3;
    private int colorSprayAmount = 4;
    private int heatMetalAmount = 3;
    private int heatMetalDamage = 9;
    private static final byte CONFUSE = 1;
    private static final byte COLORSPRAY = 2;
    private static final byte HEATMETAL = 3;
    private static final byte PREPARE = 4;
    private static final byte BLAST = 5;
    private boolean firstTurn = true;
    private boolean appliedConfuse = false;
    private boolean appliedColorSpray = false;
    private boolean appliedHeatMetal = false;

    public Magus(float x, float y) {
        super(NAME, ID, 60, 0.0F, 0.0F, 230.0F, 200.0F, IMG, x, y);

        if (AbstractDungeon.ascensionLevel >= 7) {
            this.setHp(60, 68);
        }
        else {
            this.setHp(58, 62);
        }
        if (AbstractDungeon.ascensionLevel >= 2) {
            this.blastDmg = 30;
            this.heatMetalDamage = 10;
        }
        else {
            this.blastDmg = 28;
            this.heatMetalDamage = 9;
        }
        if (AbstractDungeon.ascensionLevel >= 17) {
            this.heatMetalAmount = 4;
            this.confuseAmount = 4;
            this.colorSprayAmount = 4;
        }
        else {
            this.heatMetalAmount = 3;
            this.confuseAmount = 3;
            this.colorSprayAmount = 3;
        }

        firstTurn = true;
        this.damage.add(new DamageInfo(this, this.heatMetalDamage));
        this.damage.add(new DamageInfo(this, this.blastDmg));
        this.animation = null;
    }

    public void takeTurn() {
        switch (this.nextMove) {
            case CONFUSE:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, confuseAmount + 1, false), confuseAmount + 1));
                appliedConfuse = true;
                break;
            case COLORSPRAY:
                AbstractDungeon.actionManager.addToBottom(new SFXAction("ATTACK_MAGIC_BEAM_SHORT", 0.9F));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new BorderFlashEffect(Color.RED)));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new ColoredSmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY, Color.RED.cpy()), 0.1F));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new ColoredSmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY, Color.BLUE.cpy()), 0.1F));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new ColoredSmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY, Color.YELLOW.cpy()), 0.1F));

                AbstractDungeon.actionManager.addToBottom(new MakeTempCardInDiscardAction(new Dazed(), this.colorSprayAmount));
                appliedColorSpray = true;
                break;
            case HEATMETAL:
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo) this.damage.get(0), AttackEffect.FIRE));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player, this, new FrailPower(AbstractDungeon.player, heatMetalAmount + 1, false), heatMetalAmount + 1));
                appliedHeatMetal = true;
                break;
            case PREPARE:
                playSfx();
                AbstractDungeon.actionManager.addToBottom(new TalkAction(this, DIALOG[0], 1.5F, 3.0F));
                break;
            case BLAST:
                this.addToBot(new VFXAction(this, new FlameBarrierEffect(this.hb.cX, this.hb.cY), 0.1F));
                AbstractDungeon.actionManager.addToBottom(new WaitAction(0.25f));
                AbstractDungeon.actionManager.addToBottom(new AnimateShakeAction(this, 0.4f, 0.6f));
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo) this.damage.get(1), AttackEffect.FIRE));
                break;
        }
        AbstractDungeon.actionManager.addToBottom(new RollMoveAction(this));
    }

    private void playSfx() {
        int roll = MathUtils.random(1);
        if (roll == 0) {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_GREMLINDOPEY_1A"));
        } else {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_GREMLINDOPEY_1B"));
        }

    }

    private void playDeathSfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            CardCrawlGame.sound.play("VO_GREMLINDOPEY_2A");
        } else if (roll == 1) {
            CardCrawlGame.sound.play("VO_GREMLINDOPEY_2B");
        } else {
            CardCrawlGame.sound.play("VO_GREMLINDOPEY_2C");
        }

    }

    protected void getMove(int num) {
        int aliveCount = 0;
        for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters){
            if (!m.isDying && !m.isEscaping) {
                ++aliveCount;
            }
        }
        if (lastMove(PREPARE)){
            this.setMove(BLAST, Intent.ATTACK, blastDmg);
        }
        else if (aliveCount < 2) {
            this.setMove(PREPARE, Intent.UNKNOWN);
        }
        else if (firstTurn) {
            this.setMove(CONFUSE, Intent.DEBUFF);
            firstTurn = false;
        }
        else if (appliedConfuse && appliedColorSpray && appliedHeatMetal){
            this.setMove(PREPARE, Intent.UNKNOWN);
            appliedConfuse = false;
            appliedHeatMetal = false;
            appliedColorSpray = false;
            firstTurn = true;
        }
        else if (num >= 50){
            this.setMove(COLORSPRAY, Intent.DEBUFF);
        }
        else {
            this.setMove(HEATMETAL, Intent.ATTACK_DEBUFF, heatMetalDamage);
        }
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


