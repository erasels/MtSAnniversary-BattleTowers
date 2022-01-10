package BattleTowers.monsters;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class ExampleMonster extends AbstractBTMonster
{
    public static final String ID = makeID(ExampleMonster.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte SWEEP = 0;
    private static final byte SHRIEK = 1;
    private static final byte DOUBLE_HIT = 2;

    //calcAscensionSpecial automatically scales the number of status cards based on ascension and enemy type
    private final int STATUS = calcAscensionSpecial(2);

    //defaults enemy placement to 0, 0
    public ExampleMonster() {
        this(0.0f, 0.0f);
    }

    public ExampleMonster(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, 0.0F, 0.0f, 200.0f, 220.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(36), calcAscensionTankiness(42));

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(SWEEP, Intent.ATTACK, calcAscensionDamage(10));
        addMove(SHRIEK, Intent.DEBUFF);
        addMove(DOUBLE_HIT, Intent.ATTACK, calcAscensionDamage(5), 2);
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
        int multiplier = this.moves.get(nextMove).multiplier;

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case SWEEP: {
                useFastAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                break;
            }
            case SHRIEK: {
                addToBot(new MakeTempCardInDiscardAction(new Dazed(), STATUS));
                break;
            }
            case DOUBLE_HIT: {
                useFastAttackAnimation();
                for (int i = 0; i < multiplier; i++) {
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_VERTICAL));
                }
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
        if (!this.lastTwoMoves(SWEEP)) {
            possibilities.add(SWEEP);
        }

        //lastMove returns True if the move being passed was the most recently used move.
        if (!this.lastMove(SHRIEK)) {
            possibilities.add(SHRIEK);
        }

        //lastMoveBefore returns True if the move being passed was used 2 turns ago
        //Since we are doing !this.lastMove(DOUBLE_HIT) && !this.lastMoveBefore(DOUBLE_HIT),
        // That means we only add DOUBLE HIT to the possibilities if it wasn't used for either of the last 2 turns
        if (!this.lastMove(DOUBLE_HIT) && !this.lastMoveBefore(DOUBLE_HIT)) {
            possibilities.add(DOUBLE_HIT);
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

        //There are other ways you can handle monster AI aside from randomly choosing from possibilities
        //You can also have a fixed pattern for the monster
        //The below code has the monster SHRIEK twice, then DOUBLE_HIT once, then loops
        if (!lastTwoMoves(SHRIEK)) {
            setMoveShortcut(SHRIEK, MOVES[SHRIEK]);
        } else {
            setMoveShortcut(DOUBLE_HIT, MOVES[DOUBLE_HIT]);
        }
    }

}