package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.powers.SlimeFilledRoomPower;
import BattleTowers.relics.SlimeFilledFlask;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateJumpAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.FrailPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;

import static BattleTowers.BattleTowers.makeID;

public class GigaSlime extends AbstractBTMonster
{
    public static final String ID = makeID(GigaSlime.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = GigaSlime.monsterStrings.NAME;
    public static final String[] MOVES = GigaSlime.monsterStrings.MOVES;
    private boolean firstMove = true;
    private static final byte GOOP_SPRAY_DEBUFF = 1;
    private static final byte LESSER_SLAM_ATTACK = 2;
    private static final byte TACKLE_AND_LICK_ATTACK = 3;
    private static final int GOOP_SPRAY_SLIMED = 3;
    private static final int A18_GOOP_SPRAY_SLIMED = 5;
    private static final int GOOP_SPRAY_STRENGTH = 6;
    private static final int A3_GOOP_SPRAY_STRENGTH = 8;
    private static final int A18_GOOP_SPRAY_STRENGTH = 10;
    private static final int LESSER_SLAM_DAMAGE = 18;
    private static final int A3_LESSER_SLAM_DAMAGE = 18;
    private static final int TACKLE_AND_LICK_DAMAGE = 8;
    private static final int A3_TACKLE_AND_LICK_DAMAGE = 8;
    private static final int TACKLE_AND_LICK_WEAK_FRAIL = 1;
    private static final int A18_TACKLE_AND_LICK_WEAK_FRAIL = 2;
    private static final int HP_MIN = 163;
    private static final int HP_MAX = 167;
    private static final int A8_HP_MIN = 178;
    private static final int A8_HP_MAX = 182;
    private final int goopSpraySlimed;
    private final int goopSprayStrength;
    private final int lesserSlamDamage;
    private final int tackleAndLickDamage;
    private final int tackleAndLickWeakFrail;

    public GigaSlime() {
        this(0.0f, 0.0f);
    }

    public GigaSlime(final float x, final float y) {
        super(GigaSlime.NAME, ID, HP_MAX, -5.0F, -30.0f, 400.0f, 350.0f, null, x, y);
        this.type = EnemyType.ELITE;
        if (AbstractDungeon.ascensionLevel >= 8) {
            this.setHp(A8_HP_MIN, A8_HP_MAX);
        } else {
            this.setHp(HP_MIN, HP_MAX);
        }

        if (AbstractDungeon.ascensionLevel >= 3) {
            this.lesserSlamDamage = A3_LESSER_SLAM_DAMAGE;
            this.tackleAndLickDamage = A3_TACKLE_AND_LICK_DAMAGE;
        } else {
            this.lesserSlamDamage = LESSER_SLAM_DAMAGE;
            this.tackleAndLickDamage = TACKLE_AND_LICK_DAMAGE;
        }
        this.damage.add(new DamageInfo(this, this.lesserSlamDamage));
        this.damage.add(new DamageInfo(this, this.tackleAndLickDamage));

        if (AbstractDungeon.ascensionLevel >= 18) {
            this.goopSpraySlimed = A18_GOOP_SPRAY_SLIMED;
            this.tackleAndLickWeakFrail = A18_TACKLE_AND_LICK_WEAK_FRAIL;
        } else {
            this.goopSpraySlimed = GOOP_SPRAY_SLIMED;
            this.tackleAndLickWeakFrail = TACKLE_AND_LICK_WEAK_FRAIL;
        }
        if (AbstractDungeon.ascensionLevel >= 18) {
            this.goopSprayStrength = A18_GOOP_SPRAY_STRENGTH;
        }
        else if (AbstractDungeon.ascensionLevel >= 3) {
            this.goopSprayStrength = A3_GOOP_SPRAY_STRENGTH;
        }
        else {
            this.goopSprayStrength = GOOP_SPRAY_STRENGTH;
        }

        this.loadAnimation(BattleTowers.makeImagePath("monsters/GigaSlime/skeleton.atlas"), BattleTowers.makeImagePath("monsters/GigaSlime/skeleton.json"), 1.0F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
    }

    @Override
    public void usePreBattleAction() {
        this.addToBot(new ApplyPowerAction(this, this, new SlimeFilledRoomPower(this)));
    }

    @Override
    public void takeTurn() {
        if (this.firstMove) {
            this.firstMove = false;
        }
        switch (this.nextMove) {
            case GOOP_SPRAY_DEBUFF:
                this.addToBot(new AnimateSlowAttackAction(this));
                this.addToBot(new SFXAction("MONSTER_SLIME_ATTACK"));
                this.addToBot(new MakeTempCardInDiscardAction(new Slimed(), this.goopSpraySlimed));
                this.addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, this.goopSprayStrength)));
                break;
            case LESSER_SLAM_ATTACK:
                AbstractDungeon.actionManager.addToBottom(new AnimateJumpAction(this));
                AbstractDungeon.actionManager.addToBottom(new VFXAction(new WeightyImpactEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, new Color(1.0F, 0.1F, 0.1F, 0.0F))));
                AbstractDungeon.actionManager.addToBottom(new WaitAction(0.8F));
                AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, this.damage.get(0), AbstractGameAction.AttackEffect.POISON));
                break;
            case TACKLE_AND_LICK_ATTACK:
                if (MathUtils.random(1) == 0) {
                    AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_SLIMEBOSS_1A"));
                } else {
                    AbstractDungeon.actionManager.addToBottom(new SFXAction("VO_SLIMEBOSS_1B"));
                }
                this.addToBot(new AnimateSlowAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(1), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, this.tackleAndLickWeakFrail, true)));
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new FrailPower(AbstractDungeon.player, this.tackleAndLickWeakFrail, true)));
                break;
        }
        this.addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        if (this.firstMove || this.lastMove(LESSER_SLAM_ATTACK)) {
            this.setMove(MOVES[0], GOOP_SPRAY_DEBUFF, Intent.STRONG_DEBUFF);
        }
        else if (this.lastMove(GOOP_SPRAY_DEBUFF)) {
            this.setMove(MOVES[1], TACKLE_AND_LICK_ATTACK, Intent.ATTACK_DEBUFF, this.tackleAndLickDamage);
        }
        else {
            this.setMove(MOVES[2], LESSER_SLAM_ATTACK, Intent.ATTACK, this.lesserSlamDamage);
        }
    }
}