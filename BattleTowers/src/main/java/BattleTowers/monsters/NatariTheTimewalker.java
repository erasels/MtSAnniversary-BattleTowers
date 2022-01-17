package BattleTowers.monsters;

import BattleTowers.powers.DoubleTimePower;
import BattleTowers.powers.HalfTime;
import BattleTowers.powers.TimeStop;
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
import com.megacrit.cardcrawl.powers.StrengthPower;
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
    private static final byte KILLINGDOLL = 2;
    private static final byte SHOREUP = 3;
    private static final byte FASTFORWARD = 4;
    private static final byte THEWORLD = 5;
    private static final byte BULLETTIME = 6;
    private static final byte HOLDINGTIME = 7;
    private static int TimewindTimer = 0;
    private TimeState Timestate = TimeState.NORMAL;
    private static InvisibleIntentDisplayer firstOne = new InvisibleIntentDisplayer(90F, 180F);
    private static InvisibleIntentDisplayer secondOne = new InvisibleIntentDisplayer(0F, 220F);
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
        loadAnimation(BattleTowers.BattleTowers.makeMonsterPath("ZastraszTheJusticar/TheDragonkin.atlas"), BattleTowers.BattleTowers.makeMonsterPath("ZastraszTheJusticar/TheDragonkin.json"), 1.0F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "animation", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        AnimationState.TrackEntry e1 = state.setAnimation(1, "WingFlap", true);
        addMove(FASTERTHANLIGHT, Intent.ATTACK, calcAscensionDamage(15));
        addMove(SANDBURST, Intent.ATTACK_DEFEND, calcAscensionDamage(12));
        addMove(KILLINGDOLL, Intent.ATTACK_DEBUFF, calcAscensionDamage(4), 5);
        addMove(SHOREUP, Intent.BUFF);
        addMove(FASTFORWARD, Intent.BUFF);
        addMove(THEWORLD, Intent.STRONG_DEBUFF);
        addMove(BULLETTIME, Intent.DEBUFF);
        addMove(HOLDINGTIME, Intent.UNKNOWN);
        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
    }

    public void usePreBattleAction() {
        firstMove = true;
        firstOne = new InvisibleIntentDisplayer(90F, 180F);
        secondOne = new InvisibleIntentDisplayer(0F, 220F);
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
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        if (firstMove){
            addToBot(new TalkAction(this,DIALOG[0]));
        }
        takeTurnActions(nextMove,info);
        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }
        if (Timestate == TimeState.DOUBLETIME && moves.get(secondOne.nextMove) != null){
            info = new DamageInfo(this,moves.get(secondOne.nextMove).baseDamage);
            takeTurnActions(secondOne.nextMove,info);
        }
        if (Timestate != TimeState.HALFTIME && moves.get(firstOne.nextMove) != null){
            info = new DamageInfo(this,moves.get(firstOne.nextMove).baseDamage);
            takeTurnActions(firstOne.nextMove,info);
        }
        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
    }
    public void takeTurnActions(byte move, DamageInfo info) {
        switch (move) {
            case FASTFORWARD: {
                addToBot(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, HalfTime.POWER_ID));
                addToBot(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, TimeStop.POWER_ID));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new DoubleTimePower(AbstractDungeon.player)));
                Timestate = TimeState.DOUBLETIME;
                break;
            }
            case BULLETTIME: {
                addToBot(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, DoubleTimePower.POWER_ID));
                addToBot(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, TimeStop.POWER_ID));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new HalfTime(AbstractDungeon.player)));
                Timestate = TimeState.HALFTIME;
                break;
            }
            case THEWORLD: {
                addToBot(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, DoubleTimePower.POWER_ID));
                addToBot(new RemoveSpecificPowerAction(AbstractDungeon.player, AbstractDungeon.player, HalfTime.POWER_ID));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new TimeStop(AbstractDungeon.player)));
                Timestate = TimeState.TIMESTOP;
            }
            case FASTERTHANLIGHT: {
                addToBot(new VFXAction(new WhirlwindEffect(Color.SKY, true)));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                break;
            }
            case SANDBURST: {
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToBot(new GainBlockAction(this, calcAscensionSpecial(10)));
                break;
            }
            case KILLINGDOLL: {
                for (int i = 0; 1 < moves.get(nextMove).multiplier; i++) {
                    addToBot(new DamageAction(AbstractDungeon.player, info, getAttackEffectForMultiHit()));
                }
                break;
            }
            case SHOREUP: {
                addToBot(new HealAction(this, this, calcAscensionSpecial(8)));
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, calcAscensionSpecial(2))));
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
        firstOne = new InvisibleIntentDisplayer(90F, 180F);
        secondOne = new InvisibleIntentDisplayer(0F, 220F);
        byte move = 0;
        if (firstMove){
            possibilities.add(FASTERTHANLIGHT);
            firstOne.setIntent(Intent.BUFF,-1);
            firstOne.recordMove(FASTFORWARD);
            move = FASTERTHANLIGHT;
        }

        if (!firstMove){
            if (Timestate == TimeState.TIMESTOP || (Timestate == TimeState.DOUBLETIME && AbstractDungeon.ascensionLevel >= 19)){
              possibilities.add(KILLINGDOLL);
            }
            if (Timestate == TimeState.HALFTIME){
                possibilities.add(THEWORLD);
                possibilities.add(FASTFORWARD);
            }
            if (!lastTwoMoves(FASTERTHANLIGHT)){
                possibilities.add(FASTERTHANLIGHT);
            }
            if (!lastMove(SANDBURST)){
                possibilities.add(SANDBURST);
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
            move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
            if (Timestate != TimeState.HALFTIME) {
                possibilities.clear();
                possibilities.add(HOLDINGTIME);
                possibilities.add(HOLDINGTIME);
                possibilities.add(BULLETTIME);
                if (Timestate == TimeState.TIMESTOP) {
                    possibilities.add(FASTFORWARD);
                }
                if (Timestate == TimeState.DOUBLETIME) {
                    possibilities.add(THEWORLD);
                }
                EnemyMoveInfo infobyte = moves.get(possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1)));
                if (infobyte.nextMove == HOLDINGTIME) {
                    firstOne.setIntent(Intent.UNKNOWN, -1);
                }
                if (infobyte.nextMove == FASTFORWARD) {
                    firstOne.setIntent(Intent.BUFF, -1);
                }
                if (infobyte.nextMove == THEWORLD) {
                    firstOne.setIntent(Intent.STRONG_DEBUFF, -1);
                }
                if (infobyte.nextMove == BULLETTIME) {
                    firstOne.setIntent(Intent.DEBUFF, -1);
                }
                firstOne.recordMove(infobyte.nextMove);
            }
            if (Timestate == TimeState.DOUBLETIME){
                possibilities.clear();
                if (!lastTwoMoves(FASTERTHANLIGHT)){
                    possibilities.add(FASTERTHANLIGHT);
                }
                if (!lastMove(SANDBURST)){
                    possibilities.add(SANDBURST);
                }
                if (!lastMove(SHOREUP) && !lastMoveBefore(SHOREUP)){
                    possibilities.add(SHOREUP);
                }
                EnemyMoveInfo infobyte = moves.get(possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1)));
                if (infobyte.nextMove == FASTERTHANLIGHT) {
                    secondOne.setIntent(Intent.ATTACK, calcAscensionDamage(15));
                }
                if (infobyte.nextMove == SANDBURST) {
                    secondOne.setIntent(Intent.ATTACK_DEFEND, calcAscensionDamage(12));
                }
                if (infobyte.nextMove == SHOREUP) {
                    secondOne.setIntent(Intent.BUFF, -1);
                }
                secondOne.recordMove(infobyte.nextMove);
            }
        }
        firstMove = false;

        setMoveShortcut(move, MOVES[move]);

    }
    @Override
    public void applyPowers() {
        super.applyPowers();
        int damage;
        for (int i = 0; i < 2; i++) {
            InvisibleIntentDisplayer inviso;
            if (i == 0) {
                inviso = firstOne;
            } else {
                inviso = secondOne;
            }
            if (this.moves.get(inviso.nextMove) != null) {
                DamageInfo info = new DamageInfo(this, this.moves.get(inviso.nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
                if (info.base > -1) {
                    info.applyPowers(this, AbstractDungeon.player);
                }
                inviso.updateIntent(info.output);
            }
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
        if (firstOne.shouldRenderIntent) firstOne.update();
        if (secondOne.shouldRenderIntent) secondOne.update();
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