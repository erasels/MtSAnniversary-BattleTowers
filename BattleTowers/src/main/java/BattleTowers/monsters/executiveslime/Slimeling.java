package BattleTowers.monsters.executiveslime;

import BattleTowers.BattleTowers;
import BattleTowers.monsters.AbstractBTMonster;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.powers.WeakPower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class Slimeling extends AbstractBTMonster implements ExecutiveMinion {
    public static final String ID = makeID("Slimeling");
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte SMACK = 0;
    private static final byte WEAK = 1;
    private static final byte SLIME = 2;

    //defaults enemy placement to 0, 0
    public Slimeling() {
        this(0.0f, 0.0f);
    }

    public Slimeling(final float x, final float y, int poisonAmount, int newHealth) {
        this(x,y);

        if (poisonAmount >= 1) {
            this.powers.add(new PoisonPower(this, this, poisonAmount));
        }
        setHp(newHealth);
    }

    public Slimeling(final float x, final float y) {
        super(NAME, ID, 10, 0.0F, -4.0f, 135f, 120.0f, null, x, y);

        loadAnimation(BattleTowers.makeMonsterPath("Slimeling/Slimeling.atlas"), BattleTowers.makeMonsterPath("Slimeling/Slimeling.json"), 1.0F);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        this.state.addListener(new com.megacrit.cardcrawl.helpers.SlimeAnimListener());

        setHp(AbstractDungeon.ascensionLevel >= 19 ? 12 : 8, AbstractDungeon.ascensionLevel >= 19 ? 16 : 10);

        addMove(SMACK, Intent.ATTACK, 8);
        addMove(WEAK, Intent.ATTACK_DEBUFF, AbstractDungeon.ascensionLevel >= 2 ? 7 : 6);
        addMove(SLIME, Intent.ATTACK_DEBUFF, AbstractDungeon.ascensionLevel >= 2 ? 6 : 5);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.NORMAL;

    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);

        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case SMACK:
                addToBot(new AnimateFastAttackAction(this));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SMASH));
                break;
            case WEAK:
                addToBot(new AnimateFastAttackAction(this));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, 1, true), 1));
                break;
            case SLIME:
                addToBot(new AnimateFastAttackAction(this));
                addToBot(new SFXAction("MONSTER_SLIME_ATTACK", 0.4f, true));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.POISON));
                addToBot(new MakeTempCardInDiscardAction(new Slimed(), 1));
                break;
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        if (AbstractDungeon.ascensionLevel >= 17 || GameActionManager.turn == 1) {
            byte move;
            if (this.moveHistory.isEmpty()) {
                move = (byte) (((GameActionManager.turn == 1 ? 1 : 2) + GameActionManager.turn + minionIndex) % 3);
            } else {
                move = (byte) ((this.moveHistory.get(this.moveHistory.size() - 1) + 1) % 3);
            }
            setMoveShortcut(move, MOVES[move]);
        }
        else {
            ArrayList<Byte> possibilities = new ArrayList<>();

            if (!this.lastTwoMoves(SMACK)) {
                possibilities.add(SMACK);
            }
            if (!this.lastMove(WEAK) && !this.lastMoveBefore(WEAK)) {
                possibilities.add(WEAK);
            }
            if (!this.lastMove(SLIME) && !this.lastMoveBefore(SLIME)) {
                possibilities.add(SLIME);
            }

            byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
            setMoveShortcut(move, MOVES[move]);
        }
    }

    private int minionIndex = 0;
    @Override
    public AbstractMonster setMinionIndex(int index) {
        this.minionIndex = index;
        return this;
    }

    @Override
    public int getMinionIndex() {
        return minionIndex;
    }
}