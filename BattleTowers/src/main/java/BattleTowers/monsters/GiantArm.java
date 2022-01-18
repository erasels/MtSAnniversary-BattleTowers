package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.powers.MakeAWishPower;
import BattleTowers.powers.WrathPower;
import BattleTowers.relics.DijinnLamp;
import BattleTowers.relics.SweatyArmband;
import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
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
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.rewards.RewardItem;

public class GiantArm extends CustomMonster {
    public static final String ID = BattleTowers.makeID(GiantArm.class.getSimpleName());
    private static final String IMG = BattleTowers.makeImagePath("monsters/GiantArm/GiantArm.png");
    private static final MonsterStrings monsterStrings;
    public static final String NAME;
    public static final String[] MOVES;
    public static final String[] DIALOG;
    private static final int HP_MIN = 46;
    private static final int HP_MAX = 50;
    private static final int A_2_HP_MIN = 48;
    private static final int A_2_HP_MAX = 52;
    private static final int STAB_DMG = 12;
    private static final int A_2_STAB_DMG = 13;
    private static final int RAKE_DMG = 7;
    private static final int A_2_RAKE_DMG = 8;
    private int pummelDmg = 2;
    private int pummelAmount = 4;
    private int flexAmount = 2;
    private int smashDmg = 20;
    private int wrathTurn = 4;
    private static final byte FLEX = 1;
    private static final byte SMASH = 2;
    private static final byte PUMMEL = 3;
    private static final byte WRATH = 4;
    private int count = wrathTurn;

    public GiantArm(float x, float y) {
        super(NAME, ID, 60, 0.0F, 0.0F, 240.0F, 325.0F, IMG, x, y);

        if (AbstractDungeon.ascensionLevel >= 7) {
            this.setHp(300);
        }
        else {
            this.setHp(275);
        }
        if (AbstractDungeon.ascensionLevel >= 2){
            pummelDmg = 3;
            smashDmg = 22;
        }
        else {
            pummelDmg = 2;
            smashDmg = 20;
        }

        if (AbstractDungeon.ascensionLevel >= 18){
            this.wrathTurn = 2;
            this.count = wrathTurn;
        }

        this.dialogX = (this.hb_x - 70.0F) * Settings.scale;
        this.dialogY -= (this.hb_y - 55.0F) * Settings.scale;

        this.damage.add(new DamageInfo(this, this.pummelDmg));
        this.damage.add(new DamageInfo(this, this.smashDmg));
        this.animation = null;
    }

    @Override
    public void usePreBattleAction() {
        AbstractDungeon.actionManager.addToBottom(new TalkAction(this, DIALOG[0], 1.2f, 1.6f));
        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(new SweatyArmband()));
        playSfx();
    }

        public void takeTurn() {
        switch(this.nextMove) {
            case PUMMEL:
                for (int i = 0;i<pummelAmount; i++) {
                    AbstractDungeon.actionManager.addToBottom(new AnimateFastAttackAction(this));
                    AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo) this.damage.get(0), AttackEffect.BLUNT_HEAVY));
                }
                break;
            case FLEX:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new StrengthPower(this, flexAmount), flexAmount));
                break;
            case SMASH:
                AbstractDungeon.actionManager.addToBottom(new AnimateSlowAttackAction(this));
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, (DamageInfo) this.damage.get(1), AttackEffect.SMASH));
                break;
            case WRATH:
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new WrathPower(this, this, 1), 1));
                AbstractDungeon.actionManager.addToBottom(new SFXAction("STANCE_ENTER_WRATH"));
                AbstractDungeon.actionManager.addToBottom(new TalkAction(this, DIALOG[2], 1.2f, 1.6f));
                playSfx();
                break;


        }



        AbstractDungeon.actionManager.addToBottom(new RollMoveAction(this));
    }

    @Override
    public void damage(DamageInfo var1){
        int num = (int)(Math.random() * 100);
        if (num <= 10){
            if (num <= 5) {
                AbstractDungeon.actionManager.addToBottom(new TalkAction(this, DIALOG[1], 1.2f, 1.6f));
            }
            else {
                AbstractDungeon.actionManager.addToBottom(new TalkAction(this, DIALOG[3], 1.2f, 1.6f));
            }
            playSfx();
        }
        super.damage(var1);
    }

    private void playSfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_GIANTHEAD_1A", 1.0f, true));
        } else if (roll == 1) {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_GIANTHEAD_1B", 1.0f, true));
        } else {
            AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_GIANTHEAD_1C", 1.0f, true));
        }

    }

    private void playDeathSfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            CardCrawlGame.sound.play("VO_GIANTHEAD_2A");
        } else if (roll == 1) {
            CardCrawlGame.sound.play("VO_GIANTHEAD_2B");
        } else {
            CardCrawlGame.sound.play("VO_GIANTHEAD_2C");
        }

    }

    protected void getMove(int num) {
        count--;
        if (this.count == 0){
            this.setMove(WRATH, Intent.UNKNOWN);
        }
        else {
            if (this.lastMove(FLEX)){
                if (num < 50){
                    this.setMove(PUMMEL, Intent.ATTACK, ((DamageInfo) this.damage.get(0)).base, pummelAmount, true);
                }
                else {
                    this.setMove(SMASH, Intent.ATTACK, ((DamageInfo) this.damage.get(1)).base);
                }
            }
            else if (this.lastMove(PUMMEL)){
                    this.setMove(SMASH, Intent.ATTACK, ((DamageInfo) this.damage.get(1)).base);
            }
            else {
                if (num <= 33){
                    this.setMove(PUMMEL, Intent.ATTACK, ((DamageInfo) this.damage.get(0)).base, pummelAmount, true);
                }
                else if (num > 33 && num < 66){
                    this.setMove(FLEX, Intent.BUFF);
                }
                else {
                    this.setMove(SMASH, Intent.ATTACK, ((DamageInfo) this.damage.get(1)).base);
                }
            }
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

