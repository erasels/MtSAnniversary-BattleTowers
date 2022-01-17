package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.cards.Chilled;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateShakeAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.CannotLoseAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.FrailPower;
import com.megacrit.cardcrawl.powers.SplitPower;
import com.megacrit.cardcrawl.powers.WeakPower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class IceSlimeL extends AbstractBTMonster {
    public static final String ID = makeID(IceSlimeL.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte STATUSHIT = 0;
    private static final byte DAMAGE = 1;
    private static final byte DEBUFF = 2;
    private static final byte SPLIT = 3;

    //calcAscensionSpecial automatically scales the number of status cards based on ascension and enemy type
    private final int STATUS = calcAscensionSpecial(2);

    private float saveX;
    private float saveY;
    private boolean splitTriggered;

    //defaults enemy placement to 0, 0
    public IceSlimeL() {
        this(0.0f, 0.0f);
    }

    public IceSlimeL(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, 0.0F, 0.0f, 300.0f, 180.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is
        loadAnimation(BattleTowers.makeMonsterPath("iceSlimeL/skeleton.atlas"), BattleTowers.makeMonsterPath("iceSlimeL/skeleton.json"), 1.0F);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        this.state.addListener(new com.megacrit.cardcrawl.helpers.SlimeAnimListener());

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(62), calcAscensionTankiness(65));

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(DEBUFF, Intent.DEBUFF);
        addMove(STATUSHIT, Intent.ATTACK_DEBUFF, calcAscensionDamage(7));
        addMove(DAMAGE, Intent.ATTACK, calcAscensionDamage(12));
        addMove(SPLIT, Intent.UNKNOWN);

        this.splitTriggered = false;

        this.saveX = x;
        this.saveY = y;

        this.powers.add(new SplitPower(this));
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
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new FrailPower(AbstractDungeon.player, STATUS, true), STATUS));

                break;
            }
            case STATUSHIT: {

                useSlowAttackAnimation();
                addToBot(new SFXAction("MONSTER_SLIME_ATTACK"));
                addToBot(new DamageAction(AbstractDungeon.player,
                        info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                addToBot(new MakeTempCardInDiscardAction(new Chilled(), 2));
                break;
            }
            case DAMAGE: {
                useSlowAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                break;
            }
            case SPLIT: {

                addToBot(new CannotLoseAction());
                addToBot(new AnimateShakeAction(this, 1.0F, 0.1F));
                addToBot(new com.megacrit.cardcrawl.actions.utility.HideHealthBarAction(this));
                addToBot(new SuicideAction(this, false));
                addToBot(new WaitAction(1.0F));
                addToBot(new SFXAction("SLIME_SPLIT"));

                addToBot(new SpawnMonsterAction(new IceSlimeM(this.saveX - 134.0F, this.saveY +

                        MathUtils.random(-4.0F, 4.0F), 0, this.currentHealth), false));

                addToBot(new SpawnMonsterAction(new IceSlimeM(this.saveX + 134.0F, this.saveY +

                        MathUtils.random(-4.0F, 4.0F), 0, this.currentHealth), false));


                addToBot(new com.megacrit.cardcrawl.actions.unique.CanLoseAction());
                setMove(MOVES[SPLIT], (byte) 3, Intent.UNKNOWN);
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

    public void damage(DamageInfo info) {
        super.damage(info);

        if ((!this.isDying) && (this.currentHealth <= this.maxHealth / 2.0F) && (this.nextMove != SPLIT) && (!this.splitTriggered)) {

            setMove(MOVES[SPLIT], SPLIT, Intent.UNKNOWN);
            createIntent();
            addToBot(new TextAboveCreatureAction(this, TextAboveCreatureAction.TextType.INTERRUPTED));
            addToBot(new SetMoveAction(this, MOVES[SPLIT], SPLIT, Intent.UNKNOWN));
            this.splitTriggered = true;
        }
    }

}