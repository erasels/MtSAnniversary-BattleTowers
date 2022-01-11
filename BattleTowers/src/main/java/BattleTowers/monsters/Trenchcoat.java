package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateShakeAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.ApplyStasisAction;
import com.megacrit.cardcrawl.actions.unique.CannotLoseAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.SplitPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class Trenchcoat extends AbstractBTMonster {
    public static final String ID = makeID(Trenchcoat.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte STASIS = 0;
    private static final byte DAMAGE = 1;
    private static final byte BLOCK = 2;
    private static final byte STRENGTH = 3;
    private static final byte SPLIT = 4;

    //calcAscensionSpecial automatically scales the number of status cards based on ascension and enemy type
    private final int STATUS = calcAscensionSpecial(2);

    private boolean splitTriggered;

    private float saveX;
    private float saveY;

    private Intent secondIntent;
    private Intent thirdIntent;

    private InvisibleIntentDisplayer firstOne = new InvisibleIntentDisplayer(-50, 10);
    private InvisibleIntentDisplayer secondOne = new InvisibleIntentDisplayer(-10, 50);

    //defaults enemy placement to 0, 0
    public Trenchcoat() {
        this(0.0f, 0.0f);
    }

    public Trenchcoat(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, 0.0F, 0.0f, 250.0f, 400.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is
        loadAnimation(BattleTowers.makeMonsterPath("trenchcoat/skeleton.atlas"), BattleTowers.makeMonsterPath("trenchcoat/skeleton.json"), 1.25F);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        this.state.addListener(new com.megacrit.cardcrawl.helpers.SlimeAnimListener());

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(87), calcAscensionTankiness(90));

        this.saveX = x;
        this.saveY = y;

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(STASIS, Intent.STRONG_DEBUFF);
        addMove(BLOCK, Intent.DEFEND, calcAscensionDamage(8));
        addMove(DAMAGE, Intent.ATTACK, calcAscensionDamage(8));
        addMove(STRENGTH, Intent.BUFF);
        addMove(SPLIT, Intent.UNKNOWN);

        this.splitTriggered = false;

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
            case STASIS: {
                AbstractDungeon.actionManager.addToBottom(new ApplyStasisAction(this));
                AbstractDungeon.actionManager.addToBottom(new ApplyStasisAction(this));
                AbstractDungeon.actionManager.addToBottom(new ApplyStasisAction(this));
                break;
            }
            case BLOCK: {
                AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, 12));
                break;
            }
            case DAMAGE: {
                addToBot(new com.megacrit.cardcrawl.actions.utility.SFXAction("ATTACK_MAGIC_BEAM_SHORT", 0.5F));
                addToBot(new VFXAction(new BorderFlashEffect(Color.SKY)));
                   addToBot(new VFXAction(new SmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY), 0.3F));

                      addToBot(new DamageAction(AbstractDungeon.player,
                        info, com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect.NONE));
                break;
            }
            case STRENGTH: {
                    addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 1), 1));
            }
            case SPLIT: {

                addToBot(new CannotLoseAction());
                addToBot(new AnimateShakeAction(this, 1.0F, 0.1F));
                addToBot(new com.megacrit.cardcrawl.actions.utility.HideHealthBarAction(this));
                addToBot(new SuicideAction(this, false));
                addToBot(new WaitAction(1.0F));
                addToBot(new SFXAction("SLIME_SPLIT"));

                int str = 0;
                if (this.hasPower(StrengthPower.POWER_ID)) str = this.getPower(StrengthPower.POWER_ID).amount;

                //TODO: Carry over the Stasis powers, one into each Orb.
                addToBot(new SpawnMonsterAction(new TrenchcoatOrb(this.saveX - 134.0F, this.saveY +

                        MathUtils.random(-4.0F, 4.0F), 0, this.currentHealth /3, str), false));

                addToBot(new SpawnMonsterAction(new TrenchcoatOrb(this.saveX, this.saveY +

                        MathUtils.random(-4.0F, 4.0F), 0, this.currentHealth /3, str), false));

                addToBot(new SpawnMonsterAction(new TrenchcoatOrb(this.saveX + 134.0F, this.saveY +

                        MathUtils.random(-4.0F, 4.0F), 0, this.currentHealth /3, str), false));


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

        if (!this.firstMove) {
            //lastTwoMoves returns True if the move being passed was consecutively used for the last 2 turns
            //Since we are doing !this.lastTwoMoves(SWEEP), that means only add SWEEP as a possibility if it wasn't just used twice in a row
            if (!this.lastTwoMoves(BLOCK)) {
                possibilities.add(BLOCK);
            }

            //lastMove returns True if the move being passed was the most recently used move.
            if (!this.lastMove(DAMAGE)) {
                possibilities.add(DAMAGE);
            }

            //lastMoveBefore returns True if the move being passed was used 2 turns ago
            //Since we are doing !this.lastMove(DOUBLE_HIT) && !this.lastMoveBefore(DOUBLE_HIT),
            // That means we only add DOUBLE HIT to the possibilities if it wasn't used for either of the last 2 turns
            if (!this.lastMove(STRENGTH)) {
                possibilities.add(STRENGTH);
            }
        } else {
            possibilities.add(STASIS);
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

        if ((!this.isDying) && (info.output >= 20) && (this.nextMove != SPLIT) && (!this.splitTriggered)) {

            setMove(MOVES[SPLIT], SPLIT, Intent.UNKNOWN);
            createIntent();
            addToBot(new TextAboveCreatureAction(this, TextAboveCreatureAction.TextType.INTERRUPTED));
            addToBot(new SetMoveAction(this, MOVES[SPLIT], SPLIT, Intent.UNKNOWN));
            this.splitTriggered = true;
        }
    }

}