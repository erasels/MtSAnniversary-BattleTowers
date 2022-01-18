package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.cards.AvertYourGaze;
import BattleTowers.powers.PetrifyingGazePower;
import BattleTowers.powers.PetrifyingTouchPower;
import BattleTowers.relics.GorgonHead;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateShakeAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.rewards.RewardItem;

import static BattleTowers.BattleTowers.makeID;

public class Gorgon extends AbstractBTMonster
{
    public static final String ID = makeID(Gorgon.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = Gorgon.monsterStrings.NAME;
    public static final String[] MOVES = Gorgon.monsterStrings.MOVES;
    private static final String IMG = BattleTowers.makeImagePath("monsters/Gorgon/Gorgon.png");
    private boolean firstMove = true;
    private static final byte PETRIFYING_GAZE_BUFF = 1;
    private static final byte BITING_SNAKES_ATTACK = 2;
    private static final byte TAIL_SLAM_ATTACK = 3;
    private static final byte PETRIFY_LIMB_ATTACK = 4;
    private static final int BITING_SNAKES_DAMAGE = 5;
    private static final int A3_BITING_SNAKES_DAMAGE = 6;
    private static final int BITING_SNAKES_HITS = 4;
    private static final int TAIL_SLAM_DAMAGE = 18;
    private static final int A3_TAIL_SLAM_DAMAGE = 20;
    private static final int PETRIFY_LIMB_DAMAGE = 9;
    private static final int A3_PETRIFY_LIMB_DAMAGE = 10;
    private static final int PETRIFY_LIMB_WOUNDS = 1;
    private static final int HP_MIN = 130;
    private static final int HP_MAX = 134;
    private static final int A8_HP_MIN = 143;
    private static final int A8_HP_MAX = 147;
    private final int bitingSnakesDamage;
    private final int tailSlamDamage;
    private final int petrifyLimbDamage;

    public Gorgon() {
        this(0.0f, 0.0f);
    }

    public Gorgon(final float x, final float y) {
        super(Gorgon.NAME, ID, HP_MAX, -5.0F, 0, 450.0f, 355.0f, IMG, x, y);
        this.type = EnemyType.ELITE;
        if (AbstractDungeon.ascensionLevel >= 8) {
            this.setHp(A8_HP_MIN, A8_HP_MAX);
        } else {
            this.setHp(HP_MIN, HP_MAX);
        }

        if (AbstractDungeon.ascensionLevel >= 3) {
            this.bitingSnakesDamage = A3_BITING_SNAKES_DAMAGE;
            this.tailSlamDamage = A3_TAIL_SLAM_DAMAGE;
            this.petrifyLimbDamage = A3_PETRIFY_LIMB_DAMAGE;
        } else {
            this.bitingSnakesDamage = BITING_SNAKES_DAMAGE;
            this.tailSlamDamage = TAIL_SLAM_DAMAGE;
            this.petrifyLimbDamage = PETRIFY_LIMB_DAMAGE;
        }
        this.damage.add(new DamageInfo(this, this.bitingSnakesDamage));
        this.damage.add(new DamageInfo(this, this.tailSlamDamage));
        this.damage.add(new DamageInfo(this, this.petrifyLimbDamage));
    }

    @Override
    public void usePreBattleAction() {
        if (AbstractDungeon.ascensionLevel >= 18) {
            this.givePlayerAvertYourEyes();
            this.addToBot(new ApplyPowerAction(this, this, new PetrifyingGazePower(this)));
            this.addToBot(new ApplyPowerAction(this, this, new PetrifyingTouchPower(this)));
        }
    }

    @Override
    public void takeTurn() {
        if (this.firstMove) {
            this.firstMove = false;
        }
        switch (this.nextMove) {
            case PETRIFYING_GAZE_BUFF:
                this.addToBot(new AnimateShakeAction(this, 0.5f, 0.1f));
                this.addToBot(new ApplyPowerAction(this, this, new PetrifyingGazePower(this)));
                this.addToBot(new ApplyPowerAction(this, this, new PetrifyingTouchPower(this)));
                break;
            case BITING_SNAKES_ATTACK:
                for (int i = 0; i < BITING_SNAKES_HITS; i++) {
                    this.addToBot(new AnimateFastAttackAction(this));
                    this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(0), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                }
                break;
            case TAIL_SLAM_ATTACK:
                this.addToBot(new AnimateSlowAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(1), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                break;
            case PETRIFY_LIMB_ATTACK:
                this.addToBot(new AnimateFastAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(2), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                this.addToBot(new MakeTempCardInDrawPileAction(new Wound(), PETRIFY_LIMB_WOUNDS, true, true));
                break;
        }
        this.givePlayerAvertYourEyes();
        this.addToBot(new RollMoveAction(this));
    }

    private void givePlayerAvertYourEyes() {
        if (AbstractDungeon.player.hand.group.stream().noneMatch(c -> c.cardID.equals(AvertYourGaze.ID))) {
            this.addToBot(new MakeTempCardInHandAction(new AvertYourGaze()));
        }
    }

    @Override
    protected void getMove(final int num) {
        if (this.firstMove && AbstractDungeon.ascensionLevel < 18) {
            this.setMove(MOVES[0], PETRIFYING_GAZE_BUFF, Intent.BUFF);
        }
        else if (this.firstMove
            || (
                (this.lastMove(BITING_SNAKES_ATTACK) || this.lastMove(TAIL_SLAM_ATTACK))
                && (this.lastMoveBefore(BITING_SNAKES_ATTACK) || this.lastMoveBefore(TAIL_SLAM_ATTACK))
            )
        ) {
            this.setMove(MOVES[3], PETRIFY_LIMB_ATTACK, Intent.ATTACK_DEBUFF, this.petrifyLimbDamage);
        }
        else if (this.lastMove(TAIL_SLAM_ATTACK) || (!this.lastMove(BITING_SNAKES_ATTACK) && num > 50)) {
            this.setMove(MOVES[1], BITING_SNAKES_ATTACK, Intent.ATTACK, this.bitingSnakesDamage, BITING_SNAKES_HITS, true);
        }
        else {
            this.setMove(MOVES[2], TAIL_SLAM_ATTACK, Intent.ATTACK, this.tailSlamDamage);
        }
    }
}