package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.powers.HidePower;
import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateHopAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.AddCardToDeckAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.curses.Injury;
import com.megacrit.cardcrawl.cards.green.Blur;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.powers.BarricadePower;
import com.megacrit.cardcrawl.powers.BlurPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.vfx.combat.*;

import java.util.Iterator;

public class Assassin extends CustomMonster {
    public static final String ID = BattleTowers.makeID(Assassin.class.getSimpleName());
    private static final String IMG = BattleTowers.makeImagePath("monsters/Assassin/Assassin.png");
    private static final MonsterStrings monsterStrings;
    public static final String NAME;
    public static final String[] MOVES;
    public static final String[] DIALOG;
    private int backstabDmg = 8;
    private int backstabAmount = 3;
    private int patienceBuff = 2;
    private int smokeBombBlock = 25;
    private int hideBlock = 15;
    private int injuryDmg = 11;
    private static final byte SMOKEBOMB = 1;
    private static final byte HIDE = 2;
    private static final byte BACKSTAB = 3;
    private static final byte STUNNED = 4;
    private static final byte INJURY = 5;

    private int hidingCount = -1;
    private boolean firstTurn = true;

    public Assassin(float x, float y) {
        super(NAME, ID, 60, 0.0F, 0.0F, 120.0F, 150.0F, IMG, x, y);

        if (AbstractDungeon.ascensionLevel >= 7) {
            this.setHp(48, 52);
            smokeBombBlock = 30;
            hideBlock = 20;
        }
        else {
            this.setHp(52, 56);
        }

        if (AbstractDungeon.ascensionLevel >= 2) {
            this.backstabDmg = 8;
            this.injuryDmg = 11;
        } else {
            this.backstabDmg = 7;
            this.injuryDmg = 9;
        }

        firstTurn = true;
        this.damage.add(new DamageInfo(this, this.backstabDmg));
        this.damage.add(new DamageInfo(this, this.injuryDmg));

        this.animation = null;
    }

    protected int decrementBlock(DamageInfo info, int damageAmount) {
        if (info.type != DamageInfo.DamageType.HP_LOSS && this.currentBlock > 0 && damageAmount >= currentBlock) {
            if (this.hasPower(HidePower.POWER_ID)) {
                HidePower h = (HidePower) this.getPower(HidePower.POWER_ID);
                h.removeHide();
                this.changeState("ArmorBreak");
                }
            }
        return super.decrementBlock(info, damageAmount);
    }

    public void usePreBattleAction(){
        if (AbstractDungeon.ascensionLevel >= 17){
            AbstractDungeon.effectsQueue.add(new SmokeBombEffect(this.hb.cX, this.hb.cY));
            AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, smokeBombBlock));
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new HidePower(this, this, 0), 0));
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new BarricadePower(this)));
            hidingCount = 0;
            firstTurn = false;
        }
    }

    public void takeTurn() {
        switch (this.nextMove) {
            case SMOKEBOMB:
                AbstractDungeon.effectsQueue.add(new SmokeBombEffect(this.hb.cX, this.hb.cY));
                AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, smokeBombBlock));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new HidePower(this, this, 0), 0));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new BarricadePower(this)));
                hidingCount++;
                break;
            case HIDE:
                AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, hideBlock));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new StrengthPower(this, patienceBuff), patienceBuff));
                hidingCount++;
                break;
            case BACKSTAB:
                playSfx();
                for (int i = 0; i < backstabAmount; i++) {
                    AbstractDungeon.actionManager.addToBottom(new AnimateHopAction(this));
                    AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo) this.damage.get(0), AttackEffect.SLASH_DIAGONAL));
                }
                hidingCount = 0;
                if (this.hasPower(HidePower.POWER_ID)) {
                    HidePower h = (HidePower) this.getPower(HidePower.POWER_ID);
                    h.removeHide();
                }
                break;
            case STUNNED:
                AbstractDungeon.actionManager.addToBottom(new TextAboveCreatureAction(this, TextAboveCreatureAction.TextType.STUNNED));
                AbstractDungeon.actionManager.addToBottom(new RemoveAllBlockAction(this, this));
                hidingCount = -1;
                break;
            case INJURY:
                playSfx();
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo) this.damage.get(1), AttackEffect.SLASH_DIAGONAL));
                AbstractDungeon.actionManager.addToBottom(new AddCardToDeckAction(new Injury()));
                break;
            case ESCAPE:
                AbstractDungeon.actionManager.addToBottom(new EscapeAction(this));
                break;

        }
        AbstractDungeon.actionManager.addToBottom(new RollMoveAction(this));
    }

    private void playSfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_LOOTER_1A", -1.2f, true));
        } else if (roll == 1) {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_LOOTER_1B", -1.2f, true));
        } else {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_LOOTER_1C", -1.2f, true));
        }

    }

    private void playDeathSfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_LOOTER_2A", -1.2f, true));
        } else if (roll == 1) {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_LOOTER_2B", -1.2f, true));
        } else {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_LOOTER_2C", -1.2f, true));
        }

    }

    protected void getMove(int num) {
        int aliveCount = 0;
        for (AbstractMonster m : AbstractDungeon.getCurrRoom().monsters.monsters){
            if (!m.isDying && !m.isEscaping) {
                ++aliveCount;
            }
        }

        if (aliveCount < 2){
            if (lastMove(INJURY)){
                setMove(ESCAPE, Intent.ESCAPE);
            }
            else if (hidingCount != -1){
                this.setMove(BACKSTAB, Intent.ATTACK, backstabDmg, backstabAmount, true);
            }
            else {
                this.setMove(INJURY, Intent.ATTACK_DEBUFF, injuryDmg);
            }

        }

        else if (firstTurn || currentBlock <= 0) {
            this.setMove(SMOKEBOMB, Intent.DEFEND_BUFF);
            firstTurn = false;
        } else if (hidingCount == 0) {
            this.setMove(HIDE, Intent.DEFEND_BUFF);
        } else if (hidingCount != -1 && this.currentBlock < 30) {
            this.setMove(BACKSTAB, Intent.ATTACK, backstabDmg, backstabAmount, true);
        } else {
            if (hidingCount * 30 <= num) {
                this.setMove(HIDE, Intent.DEFEND_BUFF);
            } else {
                this.setMove(BACKSTAB, Intent.ATTACK, backstabDmg, backstabAmount, true);
            }
        }
    }

    @Override
    public void changeState(String stateName) {
        this.setMove(STUNNED, Intent.STUN);
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


