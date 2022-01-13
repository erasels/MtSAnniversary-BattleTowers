package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.VulnerablePower;

import static BattleTowers.BattleTowers.makeID;

public class MinotaurGladiator extends AbstractBTMonster
{
    public static final String ID = makeID(MinotaurGladiator.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = MinotaurGladiator.monsterStrings.NAME;
    public static final String[] MOVES = MinotaurGladiator.monsterStrings.MOVES;
    private static final String IMG = BattleTowers.makeImagePath("monsters/MinotaurGladiator/MinotaurGladiator.png");
    private boolean firstMove = true;
    private static final byte CHOP_ATTACK = 1;
    private static final byte BREAK_ATTACK = 2;
    private static final byte GORE_ATTACK = 3;
    private static final int CHOP_DAMAGE = 10;
    private static final int A2_CHOP_DAMAGE = 11;
    private static final int BREAK_DAMAGE = 8;
    private static final int A2_BREAK_DAMAGE = 9;
    private static final int BREAK_VULNERABLE = 1;
    private static final int A17_BREAK_VULNERABLE = 2;
    private static final int GORE_DAMAGE = 5;
    private static final int A2_GORE_DAMAGE = 6;
    private static final int GORE_HITS = 2;
    private static final int HP_MIN = 47;
    private static final int HP_MAX = 50;
    private static final int A8_HP_MIN = 51;
    private static final int A8_HP_MAX = 54;
    private final int chopDamage;
    private final int breakDamage;
    private final int breakVulnerable;
    private final int goreDamage;

    public MinotaurGladiator() {
        this(0.0f, 0.0f);
    }

    public MinotaurGladiator(final float x, final float y) {
        super(MinotaurGladiator.NAME, ID, HP_MAX, -5.0F, 0, 265.0f, 280.0f, IMG, x, y);
        this.type = EnemyType.NORMAL;
        if (AbstractDungeon.ascensionLevel >= 8) {
            this.setHp(A8_HP_MIN, A8_HP_MAX);
        } else {
            this.setHp(HP_MIN, HP_MAX);
        }

        if (AbstractDungeon.ascensionLevel >= 2) {
            this.chopDamage = A2_CHOP_DAMAGE;
            this.breakDamage = A2_BREAK_DAMAGE;
            this.goreDamage = A2_GORE_DAMAGE;
        } else {
            this.chopDamage = CHOP_DAMAGE;
            this.breakDamage = BREAK_DAMAGE;
            this.goreDamage = GORE_DAMAGE;
        }
        this.damage.add(new DamageInfo(this, this.chopDamage));
        this.damage.add(new DamageInfo(this, this.breakDamage));
        this.damage.add(new DamageInfo(this, this.goreDamage));

        if (AbstractDungeon.ascensionLevel >= 17) {
            this.breakVulnerable = A17_BREAK_VULNERABLE;
        }
        else {
            this.breakVulnerable = BREAK_VULNERABLE;
        }
    }

    @Override
    public void takeTurn() {
        if (this.firstMove) {
            this.firstMove = false;
        }
        switch (this.nextMove) {
            case CHOP_ATTACK:
                this.addToBot(new AnimateFastAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(0), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                break;
            case BREAK_ATTACK:
                this.addToBot(new AnimateFastAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(1), AbstractGameAction.AttackEffect.SLASH_HEAVY));
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new VulnerablePower(AbstractDungeon.player, this.breakVulnerable, true)));
                break;
            case GORE_ATTACK:
                for (int i = 0; i < GORE_HITS; i++) {
                    this.addToBot(new AnimateFastAttackAction(this));
                    this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(2), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                }
                break;
        }
        this.addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        byte move;
        if (this.firstMove) {
            move = AbstractDungeon.ascensionLevel >= 17 ? BREAK_ATTACK : CHOP_ATTACK;
        }
        else if (!lastMove(BREAK_ATTACK) && !lastMoveBefore(BREAK_ATTACK) && (this.moveHistory.size() < 3 || this.moveHistory.get(this.moveHistory.size() - 3) != BREAK_ATTACK)) {
            move = BREAK_ATTACK;
        }
        else if (this.lastMove(GORE_ATTACK) && this.lastMoveBefore(GORE_ATTACK)) {
            move = CHOP_ATTACK;
        }
        else if (this.lastMove(CHOP_ATTACK) && this.lastMoveBefore(CHOP_ATTACK)) {
            move = GORE_ATTACK;
        }
        else {
            move = num < 50 ? CHOP_ATTACK : GORE_ATTACK;
        }

        if (move == CHOP_ATTACK) {
            this.setMove(MOVES[0], CHOP_ATTACK, Intent.ATTACK, this.chopDamage);

        }
        else if (move == BREAK_ATTACK) {
            this.setMove(MOVES[1], BREAK_ATTACK, Intent.ATTACK_DEBUFF, this.breakDamage);
        }
        else {
            this.setMove(MOVES[2], GORE_ATTACK, Intent.ATTACK, this.goreDamage, GORE_HITS, true);
        }
    }
}