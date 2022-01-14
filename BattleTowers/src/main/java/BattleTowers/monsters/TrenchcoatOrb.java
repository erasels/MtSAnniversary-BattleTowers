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
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.city.BronzeOrb;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.powers.SplitPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class TrenchcoatOrb extends AbstractBTMonster {
    public static final String ID = makeID(TrenchcoatOrb.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(BronzeOrb.ID);
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

    private boolean hasStasis;

    //defaults enemy placement to 0, 0
    public TrenchcoatOrb() {
        this(0.0f, 0.0f);
    }

    public TrenchcoatOrb(final float x, final float y, int poisonAmount, int newHealth, int strengthAmount, AbstractPower stasisPower) {
        this(x,y);

        if (stasisPower != null) {
            this.powers.add(stasisPower);
            stasisPower.owner = this;
            hasStasis = true;
        }
        if (strengthAmount >= 1) {
            this.powers.add(new StrengthPower(this, strengthAmount));
        }
        if (poisonAmount >= 1) {
            this.powers.add(new PoisonPower(this, this, poisonAmount / 3));
        }
        setHp(newHealth);
    }

    public TrenchcoatOrb(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, 0.0F, 0.0f, 160.0F, 160.0F,  "images/monsters/theCity/automaton/orb.png", x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(87), calcAscensionTankiness(90));

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(STASIS, Intent.STRONG_DEBUFF);
        addMove(BLOCK, Intent.DEFEND, calcAscensionDamage(8));
        addMove(DAMAGE, Intent.ATTACK, calcAscensionDamage(8), 1, false);
        addMove(STRENGTH, Intent.BUFF);
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
                hasStasis = true;
                break;
            }
            case BLOCK: {
                AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, 8));
                break;
            }
            case DAMAGE: {
                addToBot(new SFXAction("ATTACK_MAGIC_BEAM_SHORT", 0.5F));
                addToBot(new VFXAction(new BorderFlashEffect(Color.SKY)));
                   addToBot(new VFXAction(new SmallLaserEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, this.hb.cX, this.hb.cY), 0.3F));

                      addToBot(new DamageAction(AbstractDungeon.player,
                        info, AbstractGameAction.AttackEffect.NONE));
                break;
            }
            case STRENGTH: {
                    addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 1), 1));
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        //This is where we determine what move the monster should do next
        //Here, we add the possibilities to a list and randomly choose one with each possibility having equal weight
        ArrayList<Byte> possibilities = new ArrayList<>();

        if (hasStasis) {
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
            firstMove = false;
        }

        //randomly choose one with each possibility having equal weight
        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));

        setMoveShortcut(move);

    }

}