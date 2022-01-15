package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.actions.NonStackingStasisAction;
import BattleTowers.actions.monsterOrbs.MonsterChannelAction;
import BattleTowers.actions.monsterOrbs.MonsterIncreaseMaxOrbAction;
import BattleTowers.cards.Chilled;
import BattleTowers.orbs.monster.MonsterFrost;
import BattleTowers.orbs.monster.MonsterLightning;
import BattleTowers.powers.*;
import BattleTowers.relics.AlphabetSoup;
import BattleTowers.relics.CardboardHeart;
import BattleTowers.util.TextureLoader;
import BattleTowers.util.UC;
import basemod.helpers.VfxBuilder;
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
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.monsters.beyond.TimeEater;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.CollectorCurseEffect;
import com.megacrit.cardcrawl.vfx.combat.RipAndTearEffect;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import static BattleTowers.BattleTowers.makeID;

public class AlphabetBoss extends OrbUsingMonster {
    public static final String ID = makeID(AlphabetBoss.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    private InvisibleLetterShower letterShower = new InvisibleLetterShower(-300F, 180F);

    //name of the monster's moves
    private static final byte AMPLIFY = 0;
    private static final int AMPLIFYDMG = 6;

    private static final byte DEEPFREEZE = 1;
    private static final int DEEPFREEZEDMG = 5;
    private static final int DEEPFREEZECARDS = 2;

    private static final byte ENRAGE = 2;
    private static final int ENRAGESTACKS = 2;
    private static final int ENRAGEDMG = 4;

    private static final byte HEATUP = 3;
    private static final int HEATUPDMG = 4;
    private static final int HEATUPCARDS = 2;

    private static final byte JACKOFALLTRADES = 4;
    private static final int JACKOFALLTRADESDMGBLOCK = 10;
    private static final int JACKOFALLTRADESSTR = 1;

    private static final byte KILLINGBLOW = 5;
    private static final int KILLINGBLOWDMG = 22;

    private static final byte LASH = 6;
    private static final int LASHDMG = 3;

    private static final byte MULTIHIT = 7;
    private static final int MULTIHITDMG = 6;

    private static final byte ORBPOWER = 8;
    private static final int ORBPOWERDMG = 12;
    private static final int ORBPOWERFOCUS = 3;

    private static final byte RAPIDFIRE = 9;
    private static final int RAPIDFIREDMG = 2;

    private static final byte SLICEANDDICE = 10;
    private static final int SLICEANDDICEDMG = 8;

    private static final byte UNSTABLE = 11;
    private static final int UNSTABLEDMG = 8;
    private static final int UNSTABLESTR = 2;
    private static final int UNSTABLEWEAK = 2;

    private static final byte ZAP = 12;
    private static final int ZAPDMG = 7;

    private static final byte BLUR = 13;
    private static final int BLURBLOCK = 15;

    private static final byte CHAOS = 14;

    private static final byte FRAIL = 15;
    private static final int FRAILSTACKS = 3;

    private static final byte GUARD = 16;
    private static final int GUARDBLOCK = 12;
    private static final int GUARDMETALLICIZE = 3;

    private static final byte ICY = 17;
    private static final int ICYBLOCK = 5;

    private static final byte NULLIFY = 18;
    private static final int NULLIFYBLOCK = 8;
    private static final int NULLIFYARTIFACT = 3;

    private static final byte PLATEDARMOR = 19;
    private static final int PLATEDARMORSTACKS = 8;

    private static final byte QUASH = 20;
    private static final int QUASHBLOCK = 10;
    private static final int QUASHBUFFER = 2;

    private static final byte TIMEMAZE = 21;

    private static final byte VULNERABLE = 22;
    private static final int VULNERABLESTACKS = 3;

    private static final byte WEAK = 23;
    private static final int WEAKSTACKS = 3;

    private static final byte XCOSTSPREE = 24;
    private static final int XCOSTSTRENGTH = 2;
    private static final int XCOSTCARDS = 3;

    private static final byte YOUAREMINE = 25;
    private static final int YOUAREMINESTACKS = 2;

    /**
     * Set DEBUGMODE to true to cycle through all 26 attacks, one by one in ascending byte order.
     * Start a specific one by setting DEBUGMOVE to the byte listed above associated with that attack.
     */

    private boolean DEBUGMODE = false;
    private byte DEBUGMOVE = 0;

    private boolean gotOrbSlots = false;

    private boolean lastMoveWasAttack = false;

    private final InvisibleIntentDisplayer firstOne = new InvisibleIntentDisplayer(0F, 180F);
    private final InvisibleIntentDisplayer secondOne = new InvisibleIntentDisplayer(120F, 180F);

    //defaults enemy placement to 0, 0
    public AlphabetBoss() {
        this(0.0f, 0.0f);
    }

    public AlphabetBoss(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, 0.0F, -10.0f, 400.0F, 540.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is
        loadAnimation(BattleTowers.makeMonsterPath("AlphabetBoss/skeleton.atlas"), BattleTowers.makeMonsterPath("AlphabetBoss/skeleton.json"), .7F);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        this.stateData.setMix("Hit", "Idle", 0.2F);
        e.setTimeScale(0.8F);

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(250));

        maxOrbsCap = 3;

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(AMPLIFY, Intent.ATTACK_BUFF, calcAscensionDamage(AMPLIFYDMG), 1, false);
        addMove(DEEPFREEZE, Intent.ATTACK_DEBUFF, calcAscensionDamage(DEEPFREEZEDMG), 1, false);
        addMove(ENRAGE, Intent.ATTACK_BUFF, calcAscensionDamage(HEATUPDMG), 1, false);
        addMove(HEATUP, Intent.ATTACK_DEBUFF, calcAscensionDamage(HEATUPDMG), 1, false);
        addMove(JACKOFALLTRADES, Intent.ATTACK_DEFEND, calcAscensionDamage(JACKOFALLTRADESDMGBLOCK), 1, false);
        addMove(KILLINGBLOW, Intent.ATTACK, calcAscensionDamage(KILLINGBLOWDMG), 1, false);
        addMove(LASH, Intent.ATTACK, calcAscensionDamage(LASHDMG), 4);
        addMove(MULTIHIT, Intent.ATTACK, calcAscensionDamage(MULTIHITDMG), 3);
        addMove(ORBPOWER, Intent.ATTACK_BUFF, calcAscensionDamage(ORBPOWERDMG), 1, false);
        addMove(RAPIDFIRE, Intent.ATTACK, calcAscensionDamage(RAPIDFIREDMG), 5);
        addMove(SLICEANDDICE, Intent.ATTACK, calcAscensionDamage(SLICEANDDICEDMG), 2);
        addMove(UNSTABLE, Intent.ATTACK_BUFF, calcAscensionDamage(UNSTABLEDMG), 1, false);
        addMove(ZAP, Intent.ATTACK_BUFF, calcAscensionDamage(ZAPDMG), 1, false);
        addMove(BLUR, Intent.DEFEND_BUFF);
        addMove(CHAOS, Intent.BUFF);
        addMove(FRAIL, Intent.DEBUFF);
        addMove(GUARD, Intent.DEFEND_BUFF);
        addMove(ICY, Intent.DEFEND_BUFF);
        addMove(NULLIFY, Intent.DEFEND_BUFF);
        addMove(PLATEDARMOR, Intent.BUFF);
        addMove(QUASH, Intent.DEFEND_BUFF);
        addMove(TIMEMAZE, Intent.BUFF);
        addMove(VULNERABLE, Intent.DEBUFF);
        addMove(WEAK, Intent.DEBUFF);
        addMove(XCOSTSPREE, Intent.BUFF);
        addMove(YOUAREMINE, Intent.STRONG_DEBUFF);

        lastMoveWasAttack = AbstractDungeon.monsterRng.randomBoolean();

    }

    @Override
    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new NotificationPower(this), 1));
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.BOSS;

    }

    public void initializeOrbs(){
        if (!gotOrbSlots){
            UC.atb(new MonsterIncreaseMaxOrbAction(this, 3));
            gotOrbSlots= true;
        }
    }

    public void takeTurnActions(byte move, DamageInfo info, boolean isAmplify) {

        switch (move) {
            case AMPLIFY: {
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(AMPLIFYDMG), AbstractGameAction.AttackEffect.FIRE);
                UC.doPow(new AlphabetAmplifyPower(this));
                break;
            }
            case BLUR: {
                UC.doDefTarget(this,BLURBLOCK);
                if (isAmplify) {
                    UC.doPow(new BlurPower(this, 1));
                } else {
                    UC.doPow(new BlurPower(this, 2));
                }
                break;
            }
            case CHAOS: {
                initializeOrbs();
                if (AbstractDungeon.cardRandomRng.randomBoolean()) {
                    UC.atb(new MonsterChannelAction(this, new MonsterLightning(this)));
                } else {
                    UC.atb(new MonsterChannelAction(this, new MonsterFrost(this)));
                }
                if (AbstractDungeon.cardRandomRng.randomBoolean()) {
                    UC.atb(new MonsterChannelAction(this, new MonsterLightning(this)));
                } else {
                    UC.atb(new MonsterChannelAction(this, new MonsterFrost(this)));
                }
                break;
            }
            case DEEPFREEZE: {
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                UC.doDmg(AbstractDungeon.player, DEEPFREEZEDMG, AbstractGameAction.AttackEffect.BLUNT_LIGHT);
                UC.atb(new MakeTempCardInDiscardAction(new Chilled(), DEEPFREEZECARDS));
                break;
            }
            case ENRAGE: {
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(ENRAGEDMG), AbstractGameAction.AttackEffect.SMASH);
                UC.doPow(new AngerPower(this, 1));
                UC.doPow(new EnrageStopPower(this, ENRAGESTACKS));

                break;
            }
            case FRAIL: {
                UC.doPow(new FrailPower(AbstractDungeon.player, FRAILSTACKS, true));
                break;
            }
            case GUARD: {
                UC.doDefTarget(this,GUARDBLOCK);
                UC.doPow(new MetallicizePower(this, GUARDMETALLICIZE));
                break;
            }
            case HEATUP: {
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(HEATUPDMG), AbstractGameAction.AttackEffect.FIRE);
                UC.atb(new MakeTempCardInDrawPileAction(new Burn(), HEATUPCARDS, true, true));
                break;
            }
            case ICY: {
                initializeOrbs();
                UC.doDefTarget(this,ICYBLOCK);
                UC.atb(new MonsterChannelAction(this, new MonsterFrost(this)));
                break;
            }
            case JACKOFALLTRADES: {
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(JACKOFALLTRADESDMGBLOCK), AbstractGameAction.AttackEffect.SLASH_DIAGONAL);
                UC.doDefTarget(this,JACKOFALLTRADESDMGBLOCK);
                UC.doPow(new StrengthPower(this, JACKOFALLTRADESSTR));
                break;
            }
            case KILLINGBLOW: {
                UC.atb(new ChangeStateAction(this, "ATTACK_2"));
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(KILLINGBLOWDMG), AbstractGameAction.AttackEffect.SLASH_HEAVY);
                break;
            }
            case LASH: {
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(LASHDMG), AbstractGameAction.AttackEffect.SLASH_DIAGONAL);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(LASHDMG), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(LASHDMG), AbstractGameAction.AttackEffect.SLASH_DIAGONAL);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(LASHDMG), AbstractGameAction.AttackEffect.SLASH_VERTICAL);
                break;
            }
            case MULTIHIT: {
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(MULTIHITDMG), AbstractGameAction.AttackEffect.SMASH);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(MULTIHITDMG), AbstractGameAction.AttackEffect.BLUNT_LIGHT);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(MULTIHITDMG), AbstractGameAction.AttackEffect.BLUNT_HEAVY);
                break;
            }
            case NULLIFY: {
                UC.doDefTarget(this,NULLIFYBLOCK);
                UC.doPow(new ArtifactPower(this, NULLIFYARTIFACT));
                break;
            }
            case ORBPOWER: {
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(ORBPOWERDMG), AbstractGameAction.AttackEffect.LIGHTNING);
                UC.doPow(new FocusPower(this, ORBPOWERFOCUS));
                break;
            }
            case PLATEDARMOR: {
                UC.doPow(new PlatedArmorPower(this, PLATEDARMORSTACKS));
                break;
            }
            case QUASH: {
                UC.doDefTarget(this,QUASHBLOCK);
                UC.doPow(new BufferPower(this, QUASHBUFFER));
                break;
            }
            case RAPIDFIRE: {
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(RAPIDFIREDMG), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(RAPIDFIREDMG), AbstractGameAction.AttackEffect.SLASH_VERTICAL);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(RAPIDFIREDMG), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(RAPIDFIREDMG), AbstractGameAction.AttackEffect.SLASH_VERTICAL);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(RAPIDFIREDMG), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL);
                break;
            }
            case SLICEANDDICE: {
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                addToBot(new VFXAction(new RipAndTearEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY, Color.RED, Color.GOLD)));

                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(SLICEANDDICEDMG), AbstractGameAction.AttackEffect.NONE);
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(SLICEANDDICEDMG), AbstractGameAction.AttackEffect.NONE);
                break;
            }
            case TIMEMAZE: {
                UC.doPow(new TimeWarpPower(this));
                UC.doPow(new TimeWarpStopPower(this));
                break;
            }
            case UNSTABLE: {
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(UNSTABLEDMG), AbstractGameAction.AttackEffect.NONE);
                UC.doPow(new StrengthPower(this, UNSTABLESTR));
                UC.doPow(new WeakPower(this, UNSTABLEWEAK, true));
                break;
            }
            case VULNERABLE: {
                UC.doPow(new VulnerablePower(AbstractDungeon.player, VULNERABLESTACKS, true));
                break;
            }
            case WEAK: {
                UC.doPow(new WeakPower(AbstractDungeon.player, WEAKSTACKS, true));
                break;
            }
            case XCOSTSPREE: {
                UC.doPow(new StrengthPower(this, XCOSTSTRENGTH));
                ///x cost stuff
                ArrayList<AbstractCard> xcosts = new ArrayList<>();
                xcosts = CardboardHeart.getCardsMatchingPredicate(c -> c.cost == -1, true);
                Collections.shuffle(xcosts);
                for (int i = 0; i < XCOSTCARDS; i++) {
                    AbstractCard c = xcosts.get(i).makeCopy();
                    addToBot(new MakeTempCardInHandAction(c, true));
                }

                break;
            }
            case YOUAREMINE: {
                UC.atb(new SFXAction("MONSTER_COLLECTOR_DEBUFF"));
                UC.atb(new VFXAction(new CollectorCurseEffect(AbstractDungeon.player.hb.cX, AbstractDungeon.player.hb.cY), 2.0F));

                UC.doPow(new WeakPower(AbstractDungeon.player, YOUAREMINESTACKS, true));
                UC.doPow(new VulnerablePower(AbstractDungeon.player, YOUAREMINESTACKS, true));
                UC.doPow(new FrailPower(AbstractDungeon.player, YOUAREMINESTACKS, true));
                break;
            }
            case ZAP: {
                initializeOrbs();
                UC.atb(new ChangeStateAction(this, "ATTACK"));
                UC.doDmg(AbstractDungeon.player, calcAscensionDamage(ZAPDMG), AbstractGameAction.AttackEffect.BLUNT_HEAVY);
                UC.atb(new MonsterChannelAction(this, new MonsterLightning(this)));
                break;
            }
        }
    }

    public void changeState(String stateName) {
        switch (stateName) {
            case "ATTACK":
                this.state.setAnimation(0, "Attack", false);
                this.state.addAnimation(0, "Idle", true, 0.0F);
                break;
            case "ATTACK_2":
                this.state.setAnimation(0, "ATTACK", false);
                this.state.addAnimation(0, "Idle", true, 0.0F);
        }

    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move

        letterShower.shouldRenderIntent = false;
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        } else {
            if (this.hasPower(AlphabetAmplifyPower.POWER_ID)) {
                takeTurnActions(this.nextMove, info, true);
                UC.atb(new RemoveSpecificPowerAction(this, this, this.getPower(AlphabetAmplifyPower.POWER_ID)));
            }
        }
        takeTurnActions(this.nextMove, info, false);
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        if (DEBUGMODE) {
            byte move = (byte) DEBUGMOVE;
            setMoveShortcut(move, MOVES[move]);
            DEBUGMOVE++;
            if (DEBUGMOVE == 26) DEBUGMOVE = 0;
        } else {
            if (lastMoveWasAttack) {
                byte move = (byte) AbstractDungeon.monsterRng.random(13, 25);
                while (lastTwoMoves(move)) {
                    move = (byte) AbstractDungeon.monsterRng.random(13, 25);
                }
                setMoveShortcut(move, MOVES[move]);
                lastMoveWasAttack = false;
            } else {

                byte move = (byte) AbstractDungeon.monsterRng.random(0, 12);
                while (lastTwoMoves(move)) {
                    move = (byte) AbstractDungeon.monsterRng.random(0, 12);
                }
                setMoveShortcut(move, MOVES[move]);
                lastMoveWasAttack = true;
            }
        }

        String letter = this.moveName.toString().substring(0, 1);
        letterShower.lastKnownLetter = letter.toLowerCase();
        letterShower.shouldRenderIntent = true;
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        letterShower.render(sb);
    }


    @Override
    public void update() {
        super.update();
        letterShower.update();
    }

    @Override
    public void die() {
        String letter = this.moveName.toString().substring(0, 1);
        AbstractRelic r = new AlphabetSoup();
        ((AlphabetSoup)r).setLetter(letter);
        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(r));
        super.die();
    }
}