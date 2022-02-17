package BattleTowers.monsters;

import BattleTowers.powers.AgainstTheWhirlwindPower;
import BattleTowers.powers.GatheringStormPower;
import BattleTowers.powers.TemporaryDeEnergizePower;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.combat.LightningEffect;
import com.megacrit.cardcrawl.vfx.combat.WhirlwindEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class ItozusTheWindwalker extends AbstractBTMonster {
    public static final String ID = makeID(ItozusTheWindwalker.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte AIRCUTTER = 0;
    private static final byte BREWINGSTORM = 1;
    private static final byte STORMSTRIKE = 2;
    private static final byte FLURRYOFBLOWS = 3;
    private static final byte DEATHTOUCH = 4;
    private static boolean firsturn = true;

    //defaults enemy placement to 0, 0
    public ItozusTheWindwalker() {
        this(0.0f, 0.0f);
    }

    public ItozusTheWindwalker(final float x, final float y, int newHealth) {
        this(x,y);
        setHp(newHealth);
    }


    public ItozusTheWindwalker(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 154, 0.0F, 0.0f, 270f, 380.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is
        loadAnimation(BattleTowers.BattleTowers.makeMonsterPath("ItozusTheWindwalker/TheWindWalker.atlas"), BattleTowers.BattleTowers.makeMonsterPath("ItozusTheWindwalker/TheWindWalker.json"), 1.0F);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        addMove(AIRCUTTER,Intent.ATTACK,calcAscensionDamage(12));
        addMove(BREWINGSTORM,Intent.DEFEND_BUFF);
        addMove(STORMSTRIKE,Intent.ATTACK_DEBUFF,calcAscensionDamage(4),2);
        addMove(FLURRYOFBLOWS,Intent.ATTACK,calcAscensionDamage(5),4);
        addMove(DEATHTOUCH,Intent.STRONG_DEBUFF);
        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.ELITE;
    }
    public void usePreBattleAction() {
       addToBot(new ApplyPowerAction(AbstractDungeon.player,this,new AgainstTheWhirlwindPower(AbstractDungeon.player)));
        addToBot(new ApplyPowerAction(this,this,new GatheringStormPower(this)));
    }
    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        AgainstTheWhirlwindPower Flurry = (AgainstTheWhirlwindPower) AbstractDungeon.player.getPower(AgainstTheWhirlwindPower.POWER_ID);
        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case AIRCUTTER: {
                addToBot(new VFXAction(new WhirlwindEffect(Color.SKY,true)));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HEAVY));
                if (Flurry != null){
                    addMove(STORMSTRIKE,Intent.ATTACK_DEBUFF,calcAscensionDamage(4),2+Flurry.amount2);
                    addMove(FLURRYOFBLOWS,Intent.ATTACK,calcAscensionDamage(5),4+Flurry.amount2);
                } else addToBot(new ApplyPowerAction(AbstractDungeon.player,this,new AgainstTheWhirlwindPower(AbstractDungeon.player)));
                break;
            }
            case BREWINGSTORM:{
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, calcAscensionSpecial(2))));
                addToBot(new GainBlockAction(this,calcAscensionSpecial(8)));
                if (Flurry != null){
                    Flurry.amount += 1;
                    addMove(STORMSTRIKE,Intent.ATTACK_DEBUFF,calcAscensionDamage(4),2+Flurry.amount2);
                    addMove(FLURRYOFBLOWS,Intent.ATTACK,calcAscensionDamage(5),4+Flurry.amount2);
                    Flurry.updateDescription();
                } else addToBot(new ApplyPowerAction(AbstractDungeon.player,this,new AgainstTheWhirlwindPower(AbstractDungeon.player)));
                break;
            }
            case STORMSTRIKE:{
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                addToBot(new VFXAction(new LightningEffect(AbstractDungeon.player.drawX,AbstractDungeon.player.drawY)));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE));
                if (Flurry.amount2 >0){
                    for (int i = 0; i < Flurry.amount2; i++){
                        if (i%2 != 0) {
                            addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                        } else {
                            addToBot(new VFXAction(new LightningEffect(AbstractDungeon.player.drawX,AbstractDungeon.player.drawY)));
                            addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE));
                        }
                    }
                    Flurry.Storm();
                    Flurry.flash();
                    addMove(STORMSTRIKE,Intent.ATTACK_DEBUFF,calcAscensionDamage(4),2);
                    addMove(FLURRYOFBLOWS,Intent.ATTACK,calcAscensionDamage(5),4);
                }
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new VulnerablePower(AbstractDungeon.player, calcAscensionSpecial(2), true)));
                break;
            }
            case FLURRYOFBLOWS: {
                addToBot(new VFXAction(new WhirlwindEffect(Color.SKY,true)));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToBot(new VFXAction(new LightningEffect(AbstractDungeon.player.drawX,AbstractDungeon.player.drawY)));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.LIGHTNING));
                if (Flurry.amount2 >0){
                    for (int i = 0; i < Flurry.amount2; i++){
                        addToBot(new DamageAction(AbstractDungeon.player, info, getAttackEffectForMultiHit()));
                    }
                    Flurry.Storm();
                    addMove(STORMSTRIKE,Intent.ATTACK_DEBUFF,calcAscensionDamage(4),2);
                    addMove(FLURRYOFBLOWS,Intent.ATTACK,calcAscensionDamage(5),4);
                }
                break;
            }
            case DEATHTOUCH:{
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new FrailPower(AbstractDungeon.player, calcAscensionSpecial(1), true)));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, calcAscensionSpecial(1), true)));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new DrawReductionPower(AbstractDungeon.player,calcAscensionSpecial(1))));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new TemporaryDeEnergizePower(AbstractDungeon.player,calcAscensionSpecial(1))));
                if (Flurry != null){
                    addMove(STORMSTRIKE,Intent.ATTACK_DEBUFF,calcAscensionDamage(4),2+Flurry.amount2);
                    addMove(FLURRYOFBLOWS,Intent.ATTACK,calcAscensionDamage(5),4+Flurry.amount2);
                } else addToBot(new ApplyPowerAction(AbstractDungeon.player,this,new AgainstTheWhirlwindPower(AbstractDungeon.player)));
            }
        }
        if (Flurry != null) {
            Flurry.updateDescription();
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
        if (this.lastMove(STORMSTRIKE) || this.lastMove(FLURRYOFBLOWS) || lastMove(AIRCUTTER)) {
            possibilities.add(BREWINGSTORM);
        }

        if (!this.lastMove(FLURRYOFBLOWS) && (lastMove(DEATHTOUCH) || lastMove(AIRCUTTER))) {
            possibilities.add(FLURRYOFBLOWS);
        }
        //lastMove returns True if the move being passed was the most recently used move.
        if (!lastMove(AIRCUTTER)){
            possibilities.add(AIRCUTTER);
        }

        if (!this.lastMove(STORMSTRIKE) && (lastMove(DEATHTOUCH) || lastMove(AIRCUTTER))) {
            possibilities.add(STORMSTRIKE);
        }
        if ((this.lastMove(BREWINGSTORM) || this.lastMove(FLURRYOFBLOWS) || lastMove(AIRCUTTER)) && !lastMove(DEATHTOUCH)&& !lastMoveBefore(DEATHTOUCH)) {
            possibilities.add(DEATHTOUCH);
        }
        //Since we are doing !this.lastMove(DOUBLE_HIT) && !this.lastMoveBefore(DOUBLE_HIT),
        // That means we only add DOUBLE HIT to the possibilities if it wasn't used for either of the last 2 turns

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

    public AbstractGameAction.AttackEffect getAttackEffectForMultiHit(){
        ArrayList<AbstractGameAction.AttackEffect> Effects = new ArrayList<>();
        Effects.add(AbstractGameAction.AttackEffect.BLUNT_LIGHT);
        Effects.add(AbstractGameAction.AttackEffect.BLUNT_HEAVY);
        Effects.add(AbstractGameAction.AttackEffect.SLASH_DIAGONAL);
        return Effects.get(AbstractDungeon.miscRng.random(Effects.size()-1));
    }
}
