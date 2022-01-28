package BattleTowers.monsters;

import BattleTowers.powers.*;
import basemod.devcommands.power.Power;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.FrailPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.WhirlwindEffect;

import java.sql.Time;
import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class NatariTheTimewalker extends AbstractBTMonster {
    public static final String ID = makeID(NatariTheTimewalker.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;
    //name of the monster's moves
    private static final byte FASTERTHANLIGHT = 0;
    private static final byte SANDBURST = 1;
    private static final byte CHECKMATE = 2;
    private static final byte KILLINGDOLL = 3;
    private static final byte SHOREUP = 4;
    private static final byte MIRRORSHOT = 5;
    private static final byte ROAROFTIME = 6;
    private static final byte FASTFORWARD = 7;
    private static final byte THEWORLD = 8;
    private static final byte STUN = 9;
    private static int TimewindTimer = 0;
    private TimeState Timestate = TimeState.NORMAL;
    private  boolean Roar = false;
    private  boolean justRoared = false;
    private boolean AntiLoop = false;
    public enum TimeState{
        NORMAL,HALFTIME,DOUBLETIME,TIMESTOP
    }

    //defaults enemy placement to 0, 0
    public NatariTheTimewalker() {
        this(0.0f, 0.0f);
    }

    public NatariTheTimewalker(final float x, final float y, int newHealth) {
        this(x, y);
        setHp(newHealth);
    }


    public NatariTheTimewalker(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 310, 0.0F, 0.0f, 270f, 400.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is
        setHp(calcAscensionTankiness(310));
        loadAnimation(BattleTowers.BattleTowers.makeMonsterPath("NatariTheTimewalker/TheTimewalker.atlas"), BattleTowers.BattleTowers.makeMonsterPath("NatariTheTimewalker/TheTimewalker.json"), 1.0F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "animtion0", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        addMove(FASTERTHANLIGHT, Intent.ATTACK_BUFF, calcAscensionDamage(15));
        addMove(CHECKMATE, Intent.ATTACK_DEBUFF,calcAscensionDamage(5),calcAscensionSpecial(4));
        addMove(SANDBURST, Intent.ATTACK_DEFEND, calcAscensionDamage(12));
        addMove(KILLINGDOLL, Intent.ATTACK, calcAscensionDamage(4), calcAscensionSpecial(5));
        addMove(SHOREUP, Intent.BUFF);
        addMove(MIRRORSHOT, Intent.ATTACK_DEBUFF,calcAscensionDamage(8),2);
        addMove(ROAROFTIME, Intent.ATTACK_BUFF,calcAscensionDamage(24),calcAscensionSpecial(2));
        addMove(STUN, Intent.UNKNOWN);
        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
    }

    public void usePreBattleAction() {
        firstMove = true;
        Timestate = TimeState.NORMAL;

        //AbstractDungeon.scene.fadeOutAmbiance();
        //CardCrawlGame.music.playTempBgmInstantly("TimewalkerBattle.ogg");
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.BOSS;
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info;
        info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }
        switch (this.nextMove) {
            case FASTERTHANLIGHT: {
                addToTop(new VFXAction(new WhirlwindEffect(Color.SKY, true)));
                addToTop(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                if (Timestate!= TimeState.HALFTIME){
                    addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, HalfTime.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(this, this, HalfTime.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, TimeStop.POWER_ID));
                addToTop(new ApplyPowerAction(AbstractDungeon.player, this, new DoubleTimePower(AbstractDungeon.player)));
                }
                Timestate = TimeState.DOUBLETIME;
                break;
            }
            case SANDBURST : {
                addToTop(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToTop(new GainBlockAction(this,calcAscensionSpecial(10)));
                addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player,AbstractDungeon.player, DoubleTimePower.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(this,this, DoubleTimePower.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player,AbstractDungeon.player, TimeStop.POWER_ID));
                addToTop(new ApplyPowerAction(AbstractDungeon.player,this,new HalfTime(AbstractDungeon.player)));
                Timestate = TimeState.HALFTIME;
                break;
            }
            case CHECKMATE: {
                if (Timestate!= TimeState.HALFTIME) {
                    addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, DoubleTimePower.POWER_ID));
                    addToTop(new RemoveSpecificPowerAction(this, this, DoubleTimePower.POWER_ID));
                    addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, HalfTime.POWER_ID));
                    addToTop(new RemoveSpecificPowerAction(this, this, HalfTime.POWER_ID));
                    addToTop(new ApplyPowerAction(AbstractDungeon.player, this, new TimeStop(AbstractDungeon.player)));
                    addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, 2, true)));
                    addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new VulnerablePower(AbstractDungeon.player, 2, true)));
                    Timestate = TimeState.TIMESTOP;
                }
                for (int i = 0; i < moves.get(nextMove).multiplier ; i++){
                    addToTop(new DamageAction(AbstractDungeon.player, info, getAttackEffectForMultiHit()));
                }
                break;
            }
            case KILLINGDOLL : {
                for (int i = 0; i < moves.get(nextMove).multiplier ; i++){
                    addToTop(new DamageAction(AbstractDungeon.player, info, getAttackEffectForMultiHit()));
                }
                break;
            }
            case SHOREUP : {
                addToTop(new HealAction(this,this, calcAscensionSpecial(8)));
                addToTop(new ApplyPowerAction(this,this,new StrengthPower(this,calcAscensionSpecial(2))));
                break;
            }
            case MIRRORSHOT: {
                for (int i = 0; 1 < moves.get(nextMove).multiplier; i++) {
                    addToBot(new DamageAction(AbstractDungeon.player, info, getAttackEffectForMultiHit()));
                }
                addToBot(new ApplyPowerAction(AbstractDungeon.player,this,new FrailPower(AbstractDungeon.player,1,true)));
                break;
            }
            case ROAROFTIME: {
                for (int i = 0; 1 < moves.get(nextMove).multiplier; i++) {
                    addToBot(new DamageAction(AbstractDungeon.player, info, getAttackEffectForMultiHit()));
                }
                if (!Roar) {
                    addToBot(new TalkAction(this, DIALOG[1]));
                    Roar = false;
                }
                justRoared = true;
                break;
            }
            case FASTFORWARD: {
                addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, HalfTime.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(this, this, HalfTime.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, TimeStop.POWER_ID));
                addToTop(new ApplyPowerAction(AbstractDungeon.player, this, new DoubleTimePower(AbstractDungeon.player)));
                addToTop(new ApplyPowerAction(this, this, new DoubleTimePower(AbstractDungeon.player)));
                break;
            }
            case  THEWORLD: {
                addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, DoubleTimePower.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(this, this, DoubleTimePower.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, HalfTime.POWER_ID));
                addToTop(new RemoveSpecificPowerAction(this, this, HalfTime.POWER_ID));
                addToTop(new ApplyPowerAction(AbstractDungeon.player, this, new TimeStop(AbstractDungeon.player)));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, 2, true)));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new VulnerablePower(AbstractDungeon.player, 2, true)));
                break;
            }

        }
        if (firstMove){
            addToBot(new TalkAction(this,DIALOG[0]));
        }
        if (Timestate == TimeState.DOUBLETIME && !AntiLoop){
            takeTurn();
            AntiLoop = true;
        }
        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
    }
    @Override
    protected void getMove(final int num) {
        //This is where we determine what move the monster should do next
        //Here, we add the possibilities to a list and randomly choose one with each possibility having equal weight
        ArrayList<Byte> possibilities = new ArrayList<>();
        byte move = 0;
        if (firstMove) {
            possibilities.add(FASTERTHANLIGHT);
            move = FASTERTHANLIGHT;
        }

        if (!firstMove) {
            if (Timestate == TimeState.TIMESTOP || (Timestate == TimeState.DOUBLETIME && AbstractDungeon.ascensionLevel >= 19)) {
                possibilities.add(KILLINGDOLL);
                possibilities.add(CHECKMATE);
            }
            if (!lastTwoMoves(FASTERTHANLIGHT)) {
                possibilities.add(FASTERTHANLIGHT);
            }
            if (!lastTwoMoves(SANDBURST)) {
                possibilities.add(SANDBURST);
            }
            if (!lastTwoMoves(MIRRORSHOT)) {
                possibilities.add(MIRRORSHOT);
            }
            if (Timestate == TimeState.HALFTIME){
                possibilities.add(THEWORLD);
                possibilities.add(FASTFORWARD);
            }
            if (AbstractDungeon.ascensionLevel < 19) {
                if (!lastMove(SHOREUP) && (!lastMoveBefore(SHOREUP))) {
                    possibilities.add(SHOREUP);
                }
            } else {
                if (!lastMove(SHOREUP)) {
                    possibilities.add(SHOREUP);
                }
            }
            if ((!Roar || AbstractDungeon.ascensionLevel >= 19) && this.currentHealth >= this.maxHealth/2){
                if (!Roar) {
                    possibilities.clear();
                }
                possibilities.add(ROAROFTIME);

            }
            if (lastMove(ROAROFTIME)) {
                possibilities.clear();
                possibilities.add(STUN);
            }
            move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
            justRoared = false;
            AntiLoop = true;
            firstMove = false;

            setMoveShortcut(move, MOVES[move]);

        }
    }



    @Override
    public void update() {
        super.update();
        boolean shouldwhirlwind = true;
        if (Timestate == TimeState.NORMAL){
            shouldwhirlwind = false;
        }
        if (TimewindTimer < 18){
            shouldwhirlwind = false;
        }
        if (shouldwhirlwind){
            boolean direction;
            if(Timestate == TimeState.DOUBLETIME){
                direction = false;
            } else direction = true;
            AbstractDungeon.effectsQueue.add(new WhirlwindEffect(Color.GOLD,direction));
            TimewindTimer = 0;
        }
        TimewindTimer++;
    }

    public AbstractGameAction.AttackEffect getAttackEffectForMultiHit() {
        ArrayList<AbstractGameAction.AttackEffect> Effects = new ArrayList<>();
        Effects.add(AbstractGameAction.AttackEffect.SLASH_HORIZONTAL);
        Effects.add(AbstractGameAction.AttackEffect.SLASH_VERTICAL);
        Effects.add(AbstractGameAction.AttackEffect.SLASH_HEAVY);
        Effects.add(AbstractGameAction.AttackEffect.SLASH_DIAGONAL);
        Effects.add(AbstractGameAction.AttackEffect.BLUNT_HEAVY);
        return Effects.get(AbstractDungeon.miscRng.random(Effects.size() - 1));
    }
}