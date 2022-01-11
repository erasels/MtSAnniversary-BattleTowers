package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.powers.WeakPower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class IceSlimeM extends AbstractBTMonster {
    public static final String ID = makeID(IceSlimeM.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte STATUSHIT = 0;
    private static final byte DAMAGE = 1;
    private static final byte DEBUFF = 2;

    //defaults enemy placement to 0, 0
    public IceSlimeM() {
        this(0.0f, 0.0f);
    }

    public IceSlimeM(final float x, final float y, int poisonAmount, int newHealth) {
        this(x,y);

       if (poisonAmount >= 1) {
               this.powers.add(new PoisonPower(this, this, poisonAmount));
          }
       setHp(newHealth);
    }


    public IceSlimeM(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, 0.0F, 0.0f, 170f, 130.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is
        loadAnimation(BattleTowers.makeMonsterPath("iceSlimeM/skeleton.atlas"), BattleTowers.makeMonsterPath("iceSlimeM/skeleton.json"), 1.0F);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
           e.setTime(e.getEndTime() * MathUtils.random());
          this.state.addListener(new com.megacrit.cardcrawl.helpers.SlimeAnimListener());

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(DEBUFF, Intent.DEBUFF);
        addMove(STATUSHIT, Intent.ATTACK_DEBUFF, calcAscensionDamage(7));
        addMove(DAMAGE, Intent.ATTACK, calcAscensionDamage(10));

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
            case DEBUFF: {
                useSlowAttackAnimation();
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, 1, true), 1));
                break;
            }
            case STATUSHIT: {

                useSlowAttackAnimation();
                addToBot(new SFXAction("MONSTER_SLIME_ATTACK"));
                addToBot(new DamageAction(AbstractDungeon.player,
                        info, AbstractGameAction.AttackEffect.FIRE));
                addToBot(new MakeTempCardInDiscardAction(new Burn(), 1));
                break;
            }
            case DAMAGE: {
                useSlowAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                break;
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        //This is where we determine what move the monster should do next
        //Here, we add the possibilities to a list and randomly choose one with each possibility having equal weight
        ArrayList<Byte> possibilities = new ArrayList<>();

        //lastTwoMoves returns True if the move being passed was consecutively used for the last 2 turns
        //Since we are doing !this.lastTwoMoves(SWEEP), that means only add SWEEP as a possibility if it wasn't just used twice in a row
        if (!this.lastTwoMoves(DEBUFF)) {
            possibilities.add(DEBUFF);
        }

        //lastMove returns True if the move being passed was the most recently used move.
        if (!this.lastMove(DAMAGE)) {
            possibilities.add(DAMAGE);
        }

        //lastMoveBefore returns True if the move being passed was used 2 turns ago
        //Since we are doing !this.lastMove(DOUBLE_HIT) && !this.lastMoveBefore(DOUBLE_HIT),
        // That means we only add DOUBLE HIT to the possibilities if it wasn't used for either of the last 2 turns
        if (!this.lastMove(STATUSHIT)) {
            possibilities.add(STATUSHIT);
        }

        //randomly choose one with each possibility having equal weight
        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));

        // set the monster's new move
        // MOVES[move] is the name of the move, and is the text that appears when the monster uses the move.
        // You can pass null instead if you don't care about the move name
        // This system assumes that your MOVES json is in the same order as the bytes you assigned the moves, aka
        // if your MOVES JSON looks like this:
        /* "MOVES": [
          "Sweep",
          "Shriek",
          "Double Hit"
        ],
        then your move bytes should look like this
        private static final byte SWEEP = 0;
        private static final byte SHRIEK = 1;
        private static final byte DOUBLE_HIT = 2;
        since it is using the value of the byte to get the corresponding text from the MOVES array
        */
        setMoveShortcut(move, MOVES[move]);

    }

}