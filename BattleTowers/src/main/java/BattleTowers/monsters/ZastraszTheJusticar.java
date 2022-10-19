package BattleTowers.monsters;

import BattleTowers.powers.BurnPower;
import BattleTowers.powers.InquisitorPower;
import BattleTowers.powers.JudgementPower;
import basemod.devcommands.power.Power;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.combat.LightningEffect;
import com.megacrit.cardcrawl.vfx.combat.WhirlwindEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class ZastraszTheJusticar extends AbstractBTMonster {
    public static final String ID = makeID(ZastraszTheJusticar.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;
    private ArrayList<InvisibleIntentDisplayer> DivineOrbs = new ArrayList<>();
    //name of the monster's moves
    private static final byte DIVINESMITE = 0;
    private static final byte SOLEMNVIGIL = 1;
    private static final byte JUDGEMENTOFJUSTICE = 2;
    private static final byte DIVINESTORM = 3;
    private static final byte TRIALBYFIRE = 4;
    private static boolean firsturn = true;

    private static int DS_AMT = 4;

    //defaults enemy placement to 0, 0
    public ZastraszTheJusticar() {
        this(0.0f, 0.0f);
    }

    public ZastraszTheJusticar(final float x, final float y, int newHealth) {
        this(x,y);
        setHp(newHealth);
    }


    public ZastraszTheJusticar(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 171, 0.0F, 0.0f, 270f, 400.0f, null, x, y);
        // HANDLE YOUR ANIMATION STUFF HERE
        // this.animation = Whatever your animation is
        setHp(calcAscensionTankiness(152));
        loadAnimation(BattleTowers.BattleTowers.makeMonsterPath("ZastraszTheJusticar/TheDragonkin.atlas"), BattleTowers.BattleTowers.makeMonsterPath("ZastraszTheJusticar/TheDragonkin.json"), 1.0F);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "animation", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        AnimationState.TrackEntry e1 = state.setAnimation(1, "WingFlap", true);
        addMove(DIVINESMITE,Intent.ATTACK_DEBUFF,calcAscensionDamage(11));
        addMove(SOLEMNVIGIL,Intent.DEFEND_BUFF);
        addMove(JUDGEMENTOFJUSTICE,Intent.ATTACK_BUFF,calcAscensionDamage(7),2);
        addMove(DIVINESTORM,Intent.ATTACK_BUFF,calcAscensionDamage(4),DS_AMT);
        addMove(TRIALBYFIRE,Intent.STRONG_DEBUFF);
        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
    }
    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(AbstractDungeon.player,this,new JudgementPower(AbstractDungeon.player)));
        addToBot(new ApplyPowerAction(this,this,new InquisitorPower(this)));
    }
    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.ELITE;
    }
    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }
        if (firsturn){
            firsturn = false;
        }
        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case DIVINESMITE: {
                addToBot(new VFXAction(new WhirlwindEffect(Color.SKY,true)));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HEAVY));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new BurnPower(AbstractDungeon.player, calcAscensionSpecial(4))));
                break;
            }
            case SOLEMNVIGIL:{
                addToBot(new GainBlockAction(this,calcAscensionSpecial(15)));
                addToBot(new ApplyPowerAction(this, this, new PlatedArmorPower(this, calcAscensionSpecial(5))));
                addToBot(new ApplyPowerAction(this,this,new StrengthPower(this,3)));
                break;
            }
            case JUDGEMENTOFJUSTICE:{
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HEAVY));
                addToBot(new VFXAction(new LightningEffect(AbstractDungeon.player.drawX,AbstractDungeon.player.drawY)));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.NONE));
                addToBot(new ApplyPowerAction(this, this, new RegenPower(this, calcAscensionSpecial(4))));
                addToBot(new HealAction(this,this,calcAscensionSpecial(4)));
                break;
            }
            case DIVINESTORM: {
                addToBot(new VFXAction(new WhirlwindEffect(Color.SKY,true)));
                for (int i = 0; i < DS_AMT; i++) {
                    addToBot(new VFXAction(new LightningEffect(AbstractDungeon.player.drawX,AbstractDungeon.player.drawY)));
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.LIGHTNING));
                }
                addToBot(new ApplyPowerAction(this,this,new StrengthPower(this,1)));
                DS_AMT += 1;
                addMove(DIVINESTORM,Intent.ATTACK_BUFF,calcAscensionDamage(4),DS_AMT);
                break;
            }
            case TRIALBYFIRE:{
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new BurnPower(AbstractDungeon.player, calcAscensionSpecial(6))));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new FrailPower(AbstractDungeon.player, calcAscensionSpecial(2), true)));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, calcAscensionSpecial(2), true)));
                break;
            }
        }
        if (!DivineOrbs.isEmpty()){
            for (InvisibleIntentDisplayer DivineOrb : DivineOrbs){
                DamageInfo Divineinfo = new DamageInfo(this,  this.moves.get(DivineOrb.nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
                switch (DivineOrb.nextMove) {
                    case DIVINESMITE: {
                        addToBot(new VFXAction(new WhirlwindEffect(Color.SKY,true)));
                        addToBot(new DamageAction(AbstractDungeon.player, Divineinfo, AbstractGameAction.AttackEffect.SLASH_HEAVY));
                        addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new BurnPower(AbstractDungeon.player, calcAscensionSpecial(4))));
                        break;
                    }
                    case SOLEMNVIGIL:{
                        addToBot(new GainBlockAction(this,calcAscensionSpecial(15)));
                        addToBot(new ApplyPowerAction(this, this, new PlatedArmorPower(this, calcAscensionSpecial(4))));
                        addToBot(new ApplyPowerAction(this,this,new StrengthPower(this,3)));
                        break;
                    }
                    case JUDGEMENTOFJUSTICE:{
                        addToBot(new DamageAction(AbstractDungeon.player, Divineinfo, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                        addToBot(new VFXAction(new LightningEffect(AbstractDungeon.player.drawX,AbstractDungeon.player.drawY)));
                        addToBot(new DamageAction(AbstractDungeon.player, Divineinfo, AbstractGameAction.AttackEffect.NONE));
                        addToBot(new ApplyPowerAction(this, this, new RegenPower(this, calcAscensionSpecial(4))));
                        addToBot(new HealAction(this,this,calcAscensionSpecial(6)));
                        break;
                    }
                    case DIVINESTORM: {
                        for (int i = 0; i < DS_AMT; i++) {
                            addToBot(new VFXAction(new LightningEffect(AbstractDungeon.player.drawX,AbstractDungeon.player.drawY)));
                            addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.LIGHTNING));
                        }
                        addToBot(new ApplyPowerAction(this,this,new StrengthPower(this,2)));
                        DS_AMT += 1;
                        addMove(DIVINESTORM,Intent.ATTACK_BUFF,calcAscensionDamage(4),DS_AMT);
                        break;
                    }
                }
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        //This is where we determine what move the monster should do next
        //Here, we add the possibilities to a list and randomly choose one with each possibility having equal weight
        ArrayList<Byte> possibilities = new ArrayList<>();

        if (!lastMove(SOLEMNVIGIL) && (lastMove(JUDGEMENTOFJUSTICE) || lastMove(DIVINESTORM) || lastMove(DIVINESMITE))){
            possibilities.add(SOLEMNVIGIL);
        }

        if ((lastMove(JUDGEMENTOFJUSTICE) || lastMove(SOLEMNVIGIL))) {
            possibilities.add(DIVINESTORM);
        }
        if ((lastMove(TRIALBYFIRE) || lastMove(DIVINESMITE))) {
            possibilities.add(JUDGEMENTOFJUSTICE);
        }
        if ((!lastMove(TRIALBYFIRE)|| !lastMoveBefore(TRIALBYFIRE)) && (lastMove(SOLEMNVIGIL) || lastMove(DIVINESTORM) || lastMove(JUDGEMENTOFJUSTICE))) {
            possibilities.add(TRIALBYFIRE);
        }
        if (possibilities.isEmpty()){
            possibilities.add(DIVINESMITE);
        }
        if (firsturn){
            possibilities.clear();
            possibilities.add(DIVINESMITE);
        }
        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
        if (!DivineOrbs.isEmpty()){
            for (InvisibleIntentDisplayer DivineOrb : DivineOrbs){
                possibilities.clear();
                possibilities.add(DIVINESMITE);
                possibilities.add(JUDGEMENTOFJUSTICE);
                possibilities.add(DIVINESTORM);
                possibilities.add(SOLEMNVIGIL);
                EnemyMoveInfo infobyte = moves.get(possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1)));
                if (infobyte.nextMove == DIVINESTORM){
                    DivineOrb.setIntent(Intent.ATTACK,calcAscensionDamage(2),infobyte.multiplier);
                } else if (infobyte.nextMove == JUDGEMENTOFJUSTICE){
                    DivineOrb.setIntent(Intent.ATTACK_BUFF,calcAscensionDamage(6),infobyte.multiplier);
                } else if (infobyte.nextMove == SOLEMNVIGIL) {
                    DivineOrb.setIntent(Intent.DEFEND_BUFF, -1);
                } else if (infobyte.nextMove == DIVINESMITE) {
                    DivineOrb.setIntent(Intent.ATTACK_DEBUFF, calcAscensionDamage(10));
                }
                DivineOrb.recordMove(infobyte.nextMove);
            }
        }
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
    public void GainDivineFavor(){
        int xmod = 1;
        int xshift = 0;
        if (DivineOrbs.size()%2 != 0){
            xmod = -1;
        }
        if (DivineOrbs.size() == 2){
            xshift = 160;
        } else xshift = -160;
        DivineOrbs.add(new InvisibleIntentDisplayer((-80 * xmod)+xshift, 250.0f));
        if (DivineOrbs.size() >= 2){
            addToBot(new RemoveSpecificPowerAction(AbstractDungeon.player,AbstractDungeon.player,JudgementPower.POWER_ID));
        }
        addToBot(new RollMoveAction(this));
    }
    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        if (!DivineOrbs.isEmpty()){
            for (InvisibleIntentDisplayer DivineOrb : DivineOrbs){
                if (DivineOrb.shouldRenderIntent){
                    DivineOrb.render(sb);
                }
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (!DivineOrbs.isEmpty()){
            for (InvisibleIntentDisplayer DivineOrb : DivineOrbs){
                if (DivineOrb.shouldRenderIntent){
                    DivineOrb.update();
                }
            }
        }
    }
    @Override
    public void applyPowers() {
        super.applyPowers();
        int damage;
        if (!DivineOrbs.isEmpty()) {
            for (InvisibleIntentDisplayer DivineOrb : DivineOrbs) {
                if (this.moves.get(DivineOrb.nextMove) != null) {
                    DamageInfo info = new DamageInfo(this, this.moves.get(DivineOrb.nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
                    if (info.base > -1) {
                        info.applyPowers(this, AbstractDungeon.player);
                    }
                    DivineOrb.updateIntent(info.output);
                }
            }
        }
    }
    public AbstractGameAction.AttackEffect getAttackEffectForMultiHit(){
        ArrayList<AbstractGameAction.AttackEffect> Effects = new ArrayList<>();
        Effects.add(AbstractGameAction.AttackEffect.BLUNT_LIGHT);
        Effects.add(AbstractGameAction.AttackEffect.BLUNT_HEAVY);
        Effects.add(AbstractGameAction.AttackEffect.SLASH_DIAGONAL);
        return Effects.get(AbstractDungeon.miscRng.random(Effects.size()-1));
    }
}