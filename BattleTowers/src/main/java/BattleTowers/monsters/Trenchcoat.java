package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.actions.NonStackingStasisAction;
import BattleTowers.powers.NonStackingStasisPower;
import BattleTowers.powers.TrenchcoatPower;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
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
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.*;
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

    private final float saveX;
    private final float saveY;

    private boolean noDamageLastTurn;

    private final InvisibleIntentDisplayer firstOne = new InvisibleIntentDisplayer(0F, 180F);
    private final InvisibleIntentDisplayer secondOne = new InvisibleIntentDisplayer(120F, 180F);

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
        setHp(calcAscensionTankiness(150));

        this.saveX = x;
        this.saveY = y;

        intentOffsetX = -120F * Settings.scale;

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(STASIS, Intent.STRONG_DEBUFF);
        addMove(BLOCK, Intent.DEFEND, calcAscensionDamage(10));
        addMove(DAMAGE, Intent.ATTACK, calcAscensionDamage(10), 1, false);
        addMove(STRENGTH, Intent.BUFF);
        addMove(SPLIT, Intent.UNKNOWN);

        firstOne.addMove(STASIS, Intent.STRONG_DEBUFF);
        firstOne.addMove(BLOCK, Intent.DEFEND, calcAscensionDamage(10));
        firstOne.addMove(DAMAGE, Intent.ATTACK, calcAscensionDamage(10));
        firstOne.addMove(STRENGTH, Intent.BUFF);

        secondOne.addMove(STASIS, Intent.STRONG_DEBUFF);
        secondOne.addMove(BLOCK, Intent.DEFEND, calcAscensionDamage(10));
        secondOne.addMove(DAMAGE, Intent.ATTACK, calcAscensionDamage(10));
        secondOne.addMove(STRENGTH, Intent.BUFF);

        this.splitTriggered = false;

    }

    @Override
    public void usePreBattleAction() {
        addToTop(new ApplyPowerAction(this, this, new TrenchcoatPower(this,40), 40));

    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.NORMAL;
    }

    public void takeTurnActions(byte move, DamageInfo info) {
        switch (move) {
            case STASIS: {
                AbstractDungeon.actionManager.addToBottom(new NonStackingStasisAction(this));
                break;
            }
            case BLOCK: {
                AbstractDungeon.actionManager.addToBottom(new GainBlockAction(this, 10));
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
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, 2), 2));
                break;
            }
            case SPLIT: {


                AbstractPower stasis1 = null;
                AbstractPower stasis2 = null;
                AbstractPower stasis3 = null;
                int count = 0;
                for (AbstractPower p : powers) {
                    if (p instanceof NonStackingStasisPower) {
                        switch (count) {
                            case 0:
                                stasis1 = p;
                            case 1:
                                stasis2 = p;
                            case 2:
                                stasis3 = p;
                        }
                        count++;
                    }
                }
                if (stasis1 != null) this.powers.remove(stasis1);
                if (stasis2 != null) this.powers.remove(stasis2);
                if (stasis3 != null) this.powers.remove(stasis3);

                addToBot(new CannotLoseAction());
                addToBot(new AnimateShakeAction(this, 1.0F, 0.1F));
                addToBot(new com.megacrit.cardcrawl.actions.utility.HideHealthBarAction(this));
                addToBot(new SuicideAction(this, false));
                addToBot(new WaitAction(1.0F));
                addToBot(new SFXAction("SLIME_SPLIT"));

                int str = 0;
                if (this.hasPower(StrengthPower.POWER_ID)) str = this.getPower(StrengthPower.POWER_ID).amount;

                addToBot(new SpawnMonsterAction(new TrenchcoatOrb(this.saveX - 200F, this.saveY +

                        MathUtils.random(-4.0F, 4.0F), 0, this.currentHealth / 3, str, stasis1), false));

                addToBot(new SpawnMonsterAction(new TrenchcoatOrb(this.saveX, this.saveY +

                        MathUtils.random(-4.0F, 4.0F), 0, this.currentHealth / 3, str, stasis2), false));

                addToBot(new SpawnMonsterAction(new TrenchcoatOrb(this.saveX + 200F, this.saveY +

                        MathUtils.random(-4.0F, 4.0F), 0, this.currentHealth / 3, str, stasis3), false));


                addToBot(new com.megacrit.cardcrawl.actions.unique.CanLoseAction());
                firstOne.shouldRenderIntent = false;
                secondOne.shouldRenderIntent = false;
                setMove(MOVES[SPLIT], (byte) 3, Intent.UNKNOWN);
                break;
            }
        }
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move

        DamageInfo info;
        int damageModded = 0;

        damageModded = this.moves.get(nextMove).baseDamage;
        info = new DamageInfo(this, damageModded, DamageInfo.DamageType.NORMAL);
        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }
        takeTurnActions(this.nextMove, info);

        if (this.nextMove != SPLIT) {
            damageModded = this.moves.get(firstOne.nextMove).baseDamage;
            if (this.nextMove == STRENGTH) damageModded++;
            info = new DamageInfo(this, damageModded, DamageInfo.DamageType.NORMAL);
            if (info.base > -1) {
                info.applyPowers(this, AbstractDungeon.player);
            }
            addToBot(new IntentFlashAction(firstOne));
            takeTurnActions(firstOne.nextMove, info);
        }

        if (this.nextMove != SPLIT) {
            damageModded = this.moves.get(nextMove).baseDamage;
            if (this.nextMove == STRENGTH) damageModded++;
            if (firstOne.nextMove == STRENGTH) damageModded++;
            info = new DamageInfo(this, damageModded, DamageInfo.DamageType.NORMAL);
            if (info.base > -1) {
                info.applyPowers(this, AbstractDungeon.player);
            }
            addToBot(new IntentFlashAction(secondOne));
            takeTurnActions(secondOne.nextMove, info);
        }


        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        int damagemodifier = 0;
        boolean pickedDmg = false;
        byte leftmove;
        //This is where we determine what move the monster should do next
        //Here, we add the possibilities to a list and randomly choose one with each possibility having equal weight
        ArrayList<Byte> possibilities = new ArrayList<>();

        if (!this.firstMove) {
            if (!this.lastTwoMoves(BLOCK)) {
                possibilities.add(BLOCK);
            }


            if (!this.lastMove(DAMAGE)) {
                possibilities.add(DAMAGE);
            }


            if (!this.lastMove(STRENGTH)) {
                possibilities.add(STRENGTH);
            }
        } else {

            setMoveShortcut(STASIS, MOVES[STASIS]);
            firstOne.setIntent(Intent.STRONG_DEBUFF,0);
            firstOne.recordMove(STASIS);
            secondOne.setIntent(Intent.STRONG_DEBUFF,0);
            secondOne.recordMove(STASIS);
            this.firstMove = false;
            return;
        }

        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
        if (move == DAMAGE) pickedDmg = true;
        if (move == STRENGTH) damagemodifier = damagemodifier + 2;
        leftmove = move;
        setMoveShortcut(move, MOVES[move]);

        possibilities.clear();

        boolean forceRightToChange = false;

        if (!splitTriggered) {
            for (int i = 0; i < 2; i++) {
                InvisibleIntentDisplayer inviso;
                if (i == 0) {
                    inviso = firstOne;
                } else {
                    inviso = secondOne;
                }

                    if (i == 1 && forceRightToChange) {
                        if (leftmove == BLOCK) {
                            possibilities.add(STRENGTH);
                            possibilities.add(DAMAGE);
                        } else if (leftmove == STRENGTH) {
                            possibilities.add(DAMAGE);
                            possibilities.add(BLOCK);
                        } else {
                            possibilities.add(STRENGTH);
                            possibilities.add(BLOCK);
                        }
                    } else {
                        if (i == 1 && noDamageLastTurn && !pickedDmg) {
                            possibilities.add(DAMAGE);
                            noDamageLastTurn = false;
                            pickedDmg = true;
                        } else {
                            if (!inviso.returnLastTwoMoves(BLOCK)) {
                                possibilities.add(BLOCK);
                            }

                            if (!inviso.returnLastTwoMoves(DAMAGE)) {
                                possibilities.add(DAMAGE);
                            }

                            if (!inviso.returnLastTwoMoves(STRENGTH)) {
                                possibilities.add(STRENGTH);
                            }
                        }
                    }


                move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));

                if (move == DAMAGE) pickedDmg = true;
                if (i == 0 && move == leftmove) forceRightToChange = true;

                EnemyMoveInfo infobyte = this.moves.get(move);

                DamageInfo info = new DamageInfo(this, this.moves.get(move).baseDamage, DamageInfo.DamageType.NORMAL);
                if (info.base > -1) {
                    info.applyPowers(this, AbstractDungeon.player);
                }

                inviso.setIntent(infobyte.intent, info.output + damagemodifier);
                inviso.recordMove(move);
                if (move == DAMAGE) pickedDmg = true;
                if (!pickedDmg) {noDamageLastTurn = true;}

                if (i == 0 && move == STRENGTH) damagemodifier = damagemodifier + 2;
                possibilities.clear();
            }
        }



    }

    @Override
    public void applyPowers() {
        super.applyPowers();
        if (!splitTriggered) {
            int damagemodified;

            for (int i = 0; i < 2; i++) {
                InvisibleIntentDisplayer inviso;
                if (i == 0) {
                    inviso = firstOne;
                } else {
                    inviso = secondOne;
                }

                DamageInfo info = new DamageInfo(this, this.moves.get(inviso.nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
                if (info.base > -1) {
                    info.applyPowers(this, AbstractDungeon.player);
                }

                damagemodified = info.output;
                if (this.nextMove == STRENGTH) damagemodified = damagemodified + 2;
                if (i == 1 && firstOne.nextMove == STRENGTH) damagemodified = damagemodified + 2;

                inviso.updateIntent(damagemodified);

            }
        }
    }

    public void topple() {

        if ((!this.isDying) && (this.nextMove != SPLIT) && (!this.splitTriggered)) {

            setMove(MOVES[SPLIT], SPLIT, Intent.UNKNOWN);
            createIntent();
            addToBot(new RemoveSpecificPowerAction(this, this, TrenchcoatPower.POWER_ID));
            addToBot(new TextAboveCreatureAction(this, TextAboveCreatureAction.TextType.INTERRUPTED));
            addToBot(new SetMoveAction(this, MOVES[SPLIT], SPLIT, Intent.UNKNOWN));
            firstOne.setIntent(Intent.UNKNOWN, 0);
            secondOne.setIntent(Intent.UNKNOWN, 0);
            this.splitTriggered = true;
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        if (firstOne.shouldRenderIntent) firstOne.render(sb);
        if (secondOne.shouldRenderIntent) secondOne.render(sb);
    }


    @Override
    public void update() {
        super.update();
        if (firstOne.shouldRenderIntent) firstOne.update();
        if (secondOne.shouldRenderIntent) secondOne.update();
    }
}