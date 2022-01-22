package BattleTowers.monsters;

import BattleTowers.powers.ProtectedPower;
import basemod.ReflectionHacks;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.combat.FlyingDaggerEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class NinjaLouse extends AbstractBTMonster {
    public static final String ID = makeID(NinjaLouse.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    private boolean isOpen = true;

    //name of the monster's moves
    private static final byte BITE = 0;
    private static final byte BUFF = 1;
    private static final byte SCRATCH = 2;
    private static final String CLOSED_STATE = "CLOSED";
    private static final String OPEN_STATE = "OPEN";
    private static final String REAR_IDLE = "REAR_IDLE";
    private static final String REAR = "REAR";

    //Monster stats
    private static final int MIN_HP = 36;
    private static final int MAX_HP = 42;
    private static final int BITE_DAMAGE = 16;
    private static final int SCRATCH_DAMAGE = 4;
    private static final int HITS = 3;
    private final int BLOCK_AMOUNT = calcAscensionSpecial(10);
    private final int WEAK_AMOUNT = calcAscensionSpecial(1);
    private final int POISON_AMOUNT = calcAscensionSpecial(3);
    private final int STRENGTH = calcAscensionSpecial(1);
    private final int CURL_AMOUNT = AbstractDungeon.monsterHpRng.random(calcAscensionSpecial(calcAscensionTankiness(10)), calcAscensionSpecial(calcAscensionTankiness(14)));
    private final boolean START_INVIS;

    public NinjaLouse() {
        this(0.0F, 0.0F, false);
    }

    public NinjaLouse(final float x, final float y, boolean startInvis) {
        super(NAME, ID, 140, 0.0F, -5.0F, 180.0F, 140.0F, null, x, y);
        this.START_INVIS = startInvis;
        this.loadAnimation("battleTowersResources/img/monsters/Louses/NinjaLouse/skeleton.atlas", "battleTowersResources/img/monsters/Louses/NinjaLouse/skeleton.json", 1.0F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        setHp(calcAscensionTankiness(MIN_HP), calcAscensionTankiness(MAX_HP));
        addMove(BITE, Intent.ATTACK_DEBUFF, calcAscensionDamage(BITE_DAMAGE));
        addMove(BUFF, Intent.DEFEND_BUFF);
        addMove(SCRATCH, Intent.ATTACK, calcAscensionDamage(SCRATCH_DAMAGE), 3);
    }

    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new CurlUpPower(this, CURL_AMOUNT)));
        addToBot(new ApplyPowerAction(this, this, new EnvenomPower(this, 1)));
        if (START_INVIS) {
            IntangiblePower p = new IntangiblePower(this, 1);
            ReflectionHacks.setPrivate(p, IntangiblePower.class, "justApplied", false);
            addToBot(new ApplyPowerAction(this, this, p));
        }
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
        int multiplier = this.moves.get(nextMove).multiplier;

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case BITE: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, OPEN_STATE));
                    addToBot(new WaitAction(0.5F));
                }
                addToBot(new AnimateSlowAttackAction(this));
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, WEAK_AMOUNT, true)));
                break;
            }
            case BUFF: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, REAR));
                    addToBot(new WaitAction(1.2F));
                } else {
                    addToBot(new ChangeStateAction(this, REAR_IDLE));
                    addToBot(new WaitAction(0.9F));
                }
                addToBot(new GainBlockAction(this, this, BLOCK_AMOUNT));
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, STRENGTH)));
                break;
            }
            case SCRATCH: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, OPEN_STATE));
                    addToBot(new WaitAction(0.5F));
                }
                for (int i = 0; i < multiplier; i++) {
                    addToBot(new AbstractGameAction() {
                        @Override
                        public void update() {
                            NinjaLouse.this.useFastAttackAnimation();
                            isDone = true;
                        }
                    });
                    addToBot(new VFXAction(new FlyingDaggerEffect(this.hb.cX, this.hb.cY-this.hb_h/4, 0, this.hb.cX > AbstractDungeon.player.hb.cX), 0.15F));
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL, true));
                }
                break;
            }
        }

        if (!this.hasPower(IntangiblePower.POWER_ID)) {
            addToBot(new ApplyPowerAction(this, this, new IntangiblePower(this, 1)));
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        ArrayList<Byte> possibilities = new ArrayList<>();
        if (firstMove) {
            firstMove = false;
            possibilities.add(START_INVIS ? BUFF : SCRATCH);
        } else {
            if (GameActionManager.turn % 2 == (START_INVIS ? 0 : 1)) {
                possibilities.add(BUFF);
                //possibilities.add(BITE);
            } else {
                possibilities.add(SCRATCH);
            }
        }

        //randomly choose one with each possibility having equal weight
        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
        setMoveShortcut(move, MOVES[move]);
    }

    public void changeState(String stateName) {
        switch (stateName) {
            case CLOSED_STATE:
                this.state.setAnimation(0, "transitiontoclosed", false);
                this.state.addAnimation(0, "idle closed", true, 0.0F);
                this.isOpen = false;
                break;
            case OPEN_STATE:
                this.state.setAnimation(0, "transitiontoopened", false);
                this.state.addAnimation(0, "idle", true, 0.0F);
                this.isOpen = true;
                break;
            case REAR_IDLE:
                this.state.setAnimation(0, "rear", false);
                this.state.addAnimation(0, "idle", true, 0.0F);
                this.isOpen = true;
                break;
            default:
                this.state.setAnimation(0, "transitiontoopened", false);
                this.state.addAnimation(0, "rear", false, 0.0F);
                this.state.addAnimation(0, "idle", true, 0.0F);
                this.isOpen = true;
                break;
        }
    }

    @Override
    public void die() {
        super.die();
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (m != this && !m.isDeadOrEscaped() && m instanceof GoldenLouse && m.hasPower(ProtectedPower.POWER_ID)) {
                addToTop(new RemoveSpecificPowerAction(m, this, ProtectedPower.POWER_ID));
            }
        }
    }

    @Override
    public void damage(DamageInfo info) {
        if (info.output > 0 && hasPower(IntangiblePower.POWER_ID))
            info.output = 1;
        super.damage(info);
    }
}
