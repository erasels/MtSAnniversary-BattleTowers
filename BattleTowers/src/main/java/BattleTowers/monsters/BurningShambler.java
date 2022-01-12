package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateShakeAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;

import static BattleTowers.BattleTowers.makeID;

public class BurningShambler extends AbstractBTMonster
{
    public static final String ID = makeID(BurningShambler.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = BurningShambler.monsterStrings.NAME;
    public static final String[] MOVES = BurningShambler.monsterStrings.MOVES;
    private static final String IMG = BattleTowers.makeImagePath("monsters/BurningShambler/BurningShambler.png");
    private boolean firstMove = true;
    private static final byte FLICKER_OF_FLAME_ATTACK = 1;
    private static final byte BONFIRE_DEBUFF = 2;
    private static final byte RUSH_ATTACK = 3;
    private static final int FLICKER_OF_FLAME_DAMAGE = 5;
    private static final int A2_FLICKER_OF_FLAME_DAMAGE = 6;
    private static final int FLICKER_OF_FLAME_BURNS = 1;
    private static final int BONFIRE_BURNS = 2;
    private static final int A17_BONFIRE_BURNS = 3;
    private static final int RUSH_DAMAGE = 8;
    private static final int A2_RUSH_DAMAGE = 9;
    private static final int HP_MIN = 37;
    private static final int HP_MAX = 40;
    private static final int A8_HP_MIN = 40;
    private static final int A8_HP_MAX = 43;
    private final int flickerOfFlameDamage;
    private final int bonfireBurns;
    private final int rushDamage;
    private boolean usedBonfire = false;

    public BurningShambler() {
        this(0.0f, 0.0f);
    }

    public BurningShambler(final float x, final float y) {
        super(BurningShambler.NAME, ID, HP_MAX, -5.0F, 0, 200.0f, 255.0f, IMG, x, y);
        this.type = EnemyType.NORMAL;
        if (AbstractDungeon.ascensionLevel >= 8) {
            this.setHp(A8_HP_MIN, A8_HP_MAX);
        } else {
            this.setHp(HP_MIN, HP_MAX);
        }

        if (AbstractDungeon.ascensionLevel >= 2) {
            this.flickerOfFlameDamage = A2_FLICKER_OF_FLAME_DAMAGE;
            this.rushDamage = A2_RUSH_DAMAGE;
        } else {
            this.flickerOfFlameDamage = FLICKER_OF_FLAME_DAMAGE;
            this.rushDamage = RUSH_DAMAGE;
        }
        this.damage.add(new DamageInfo(this, this.flickerOfFlameDamage));
        this.damage.add(new DamageInfo(this, this.rushDamage));

        if (AbstractDungeon.ascensionLevel >= 17) {
            this.bonfireBurns = A17_BONFIRE_BURNS;
        }
        else {
            this.bonfireBurns = BONFIRE_BURNS;
        }
    }

    @Override
    public void takeTurn() {
        if (this.firstMove) {
            this.firstMove = false;
        }
        switch (this.nextMove) {
            case FLICKER_OF_FLAME_ATTACK:
                this.addToBot(new AnimateFastAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(0), AbstractGameAction.AttackEffect.FIRE));
                if (AbstractDungeon.ascensionLevel >= 17) {
                    this.addToBot(new MakeTempCardInDrawPileAction(new Burn(), FLICKER_OF_FLAME_BURNS, true, true));
                }
                else {
                    this.addToBot(new MakeTempCardInDiscardAction(new Burn(), FLICKER_OF_FLAME_BURNS));
                }
                break;
            case BONFIRE_DEBUFF:
                this.addToBot(new AnimateShakeAction(this, 0.3f, 0.1f));
                this.addToBot(new MakeTempCardInDrawPileAction(new Burn(), this.bonfireBurns, true, true));
                this.usedBonfire = true;
                break;
            case RUSH_ATTACK:
                this.addToBot(new AnimateFastAttackAction(this));
                this.addToBot(new DamageAction(AbstractDungeon.player, this.damage.get(1), AbstractGameAction.AttackEffect.FIRE));
                break;
        }
        this.addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        if (this.firstMove || this.lastMove(RUSH_ATTACK)) {
            this.setMove(MOVES[0], FLICKER_OF_FLAME_ATTACK, Intent.ATTACK_DEBUFF, this.flickerOfFlameDamage);
        }
        else if (!this.usedBonfire) {
            this.setMove(MOVES[1], BONFIRE_DEBUFF, Intent.DEBUFF);
        }
        else {
            this.setMove(MOVES[2], RUSH_ATTACK, Intent.ATTACK, this.rushDamage);
        }
    }
}