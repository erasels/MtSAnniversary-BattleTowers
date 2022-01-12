package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.powers.GreedWrathEnvyPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.FrailPower;

import static BattleTowers.BattleTowers.makeID;

public class DoomedSoul extends AbstractBTMonster
{
    public static final String ID = makeID(DoomedSoul.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = DoomedSoul.monsterStrings.NAME;
    public static final String[] MOVES = DoomedSoul.monsterStrings.MOVES;
    private static final String IMG = BattleTowers.makeImagePath("monsters/DoomedSoul/DoomedSoul.png");
    private boolean firstMove = true;
    private static final byte GREED_ATTACK = 1;
    private static final byte WRATH_ATTACK = 2;
    private static final byte ENVY_ATTACK = 3;
    private static final int GREED_DAMAGE = 7;
    private static final int A2_GREED_DAMAGE = 8;
    private static final int WRATH_DAMAGE = 0;
    private static final int A2_WRATH_DAMAGE = 1;
    private static final int WRATH_HITS = 2;
    private static final int ENVY_DAMAGE = 2;
    private static final int A2_ENVY_DAMAGE = 3;
    private static final int ENVY_FRAIL = 1;
    private static final int A17_ENVY_FRAIL = 1;
    private static final int HP_MIN = 90;
    private static final int HP_MAX = 94;
    private static final int A8_HP_MIN = 95;
    private static final int A8_HP_MAX = 99;
    private final int greedDamage;
    private final int wrathDamage;
    private final int envyDamage;
    private final int envyFrail;

    public DoomedSoul() {
        this(0.0f, 0.0f);
    }

    public DoomedSoul(final float x, final float y) {
        super(DoomedSoul.NAME, ID, HP_MAX, -5.0F, 0, 205.0f, 225.0f, IMG, x, y);
        this.type = EnemyType.NORMAL;
        if (AbstractDungeon.ascensionLevel >= 8) {
            this.setHp(A8_HP_MIN, A8_HP_MAX);
        } else {
            this.setHp(HP_MIN, HP_MAX);
        }

        if (AbstractDungeon.ascensionLevel >= 2) {
            this.greedDamage = A2_GREED_DAMAGE;
            this.wrathDamage = A2_WRATH_DAMAGE;
            this.envyDamage = A2_ENVY_DAMAGE;
        } else {
            this.greedDamage = GREED_DAMAGE;
            this.wrathDamage = WRATH_DAMAGE;
            this.envyDamage = ENVY_DAMAGE;
        }
        this.damage.add(new DamageInfo(this, this.greedDamage));
        this.damage.add(new DamageInfo(this, this.wrathDamage));
        this.damage.add(new DamageInfo(this, this.envyDamage));

        if (AbstractDungeon.ascensionLevel >= 17) {
            this.envyFrail = A17_ENVY_FRAIL;
        }
        else {
            this.envyFrail = ENVY_FRAIL;
        }
    }

    @Override
    public void usePreBattleAction() {
        this.addToBot(new ApplyPowerAction(this, this, new GreedWrathEnvyPower(this)));
    }

    @Override
    public void takeTurn() {
        if (this.firstMove) {
            this.firstMove = false;
        }
        switch (this.nextMove) {
            case GREED_ATTACK:
                this.addToBot(new AnimateFastAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(0), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                break;
            case WRATH_ATTACK:
                for (int i = 0; i < WRATH_HITS; i++) {
                    this.addToBot(new AnimateFastAttackAction(this));
                    this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(1), AbstractGameAction.AttackEffect.SLASH_VERTICAL));
                }
                break;
            case ENVY_ATTACK:
                this.addToBot(new AnimateFastAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(2), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new FrailPower(AbstractDungeon.player, this.envyFrail, true)));
                break;
        }
        this.addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        byte move;
        if (this.firstMove) {
            move = WRATH_ATTACK;
        }
        else if (this.lastMove(WRATH_ATTACK)) {
            move = num < 60 ? GREED_ATTACK : ENVY_ATTACK;
        }
        else if (this.lastMove(ENVY_ATTACK)) {
            move = num < 60 ? GREED_ATTACK : WRATH_ATTACK;
        }
        else if (this.lastMove(GREED_ATTACK) && this.lastMoveBefore(GREED_ATTACK)){
            move = num < 50 ? WRATH_ATTACK : ENVY_ATTACK;
        }
        else {
            move = num < 40 ? GREED_ATTACK : num < 70 ? WRATH_ATTACK : ENVY_ATTACK;
        }

        if (move == GREED_ATTACK) {
            this.setMove(MOVES[0], GREED_ATTACK, Intent.ATTACK, this.greedDamage);

        }
        else if (move == WRATH_ATTACK) {
            this.setMove(MOVES[1], WRATH_ATTACK, Intent.ATTACK, this.wrathDamage, WRATH_HITS, true);
        }
        else {
            this.setMove(MOVES[2], ENVY_ATTACK, Intent.ATTACK_DEBUFF, this.envyDamage);
        }
    }
}