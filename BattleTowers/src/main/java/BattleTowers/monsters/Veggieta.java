package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.actions.PepperSprayAction;
import BattleTowers.actions.PewcumberAction;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class Veggieta extends AbstractBTMonster
{
    public static final String ID = makeID(Veggieta.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;
    private static final String IMG = BattleTowers.makeImagePath("monsters/Veggieta/Veggieta.png");

    //name of the monster's moves
    private static final byte PEWCUMBER = 0;
    private static final byte PEPPER_SPRAY = 1;

    private static final int PEWCUMBER_DAMAGE = 15;
    private static final int PEPPER_SPRAY_DAMAGE = 3;
    private static final int PEPPER_SPRAY_MULT = 5;

    private static final int HEALTH_MIN = 50;
    private static final int HEALTH_MAX = 55;

    //defaults enemy placement to 0, 0
    public Veggieta() {
        this(0.0f, 0.0f);
    }

    public Veggieta(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, 0.0f, 0.0f, 200.0f, 485.0f, IMG, x, y);
        intentOffsetX = 15.0f;

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(HEALTH_MIN), calcAscensionTankiness(HEALTH_MAX));

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(PEWCUMBER, Intent.ATTACK, calcAscensionDamage(PEWCUMBER_DAMAGE));
        addMove(PEPPER_SPRAY, Intent.ATTACK, calcAscensionDamage(PEPPER_SPRAY_DAMAGE), PEPPER_SPRAY_MULT, true);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        type = EnemyType.NORMAL;
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        int multiplier = moves.get(nextMove).multiplier;

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (nextMove) {
            case PEWCUMBER: {
                addToBot(new PewcumberAction(this, info));
                break;
            }
            case PEPPER_SPRAY: {
                addToBot(new PepperSprayAction(this, info));
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
        //Since we are doing !lastTwoMoves(SWEEP), that means only add SWEEP as a possibility if it wasn't just used twice in a row
        if (!lastTwoMoves(PEWCUMBER)) {
            possibilities.add(PEWCUMBER);
        }

        //lastMove returns True if the move being passed was the most recently used move.
        if (!lastTwoMoves(PEPPER_SPRAY)) {
            possibilities.add(PEPPER_SPRAY);
        }

        //randomly choose one with each possibility having equal weight
        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));

        setMoveShortcut(move, MOVES[move]);
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
    }

    @Override
    public void update() {
        super.update();
    }
}