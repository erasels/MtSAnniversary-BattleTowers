package BattleTowers.monsters;

import BattleTowers.powers.ExtraHitsPower;
import BattleTowers.powers.RainbowPower;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.WeakPower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class RainbowLouse extends AbstractBTMonster {
    public static final String ID = makeID(RainbowLouse.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;


    private boolean isOpen = true;

    //name of the monster's moves
    private static final byte BUFF = 0;
    private static final byte STRUGGLE = 1;
    private static final byte DEBUFF = 2;
    private static final String CLOSED_STATE = "CLOSED";
    private static final String OPEN_STATE = "OPEN";
    private static final String REAR_IDLE = "REAR_IDLE";
    private static final String REAR = "REAR";

    //Monster stats
    private static final int MIN_HP = 16;
    private static final int MAX_HP = 22;
    private static final int STRUGGLE_DAMAGE = 2;
    private static final int BASE_STRUGGLE_HITS = 2;
    private final int STR_AMOUNT = calcAscensionSpecial(1);
    private final int DEBUFF_AMOUNT = 1;
    private boolean didDebuff;
    private final int timerOffset;


    public RainbowLouse(final float x, final float y) {
        super(NAME, ID, 1, 0.0F, 0.0f, 180.0F / 0.8F, 140.0F / 0.8F, null, x, y);
        this.loadAnimation("battleTowersResources/img/monsters/Louses/WhiteLouse/skeleton.atlas", "battleTowersResources/img/monsters/Louses/WhiteLouse/skeleton.json", 0.8F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        setHp(calcAscensionTankiness(MIN_HP), calcAscensionTankiness(MAX_HP));
        addMove(BUFF, Intent.BUFF);
        addMove(STRUGGLE, Intent.ATTACK, calcAscensionDamage(STRUGGLE_DAMAGE));
        addMove(DEBUFF, Intent.DEBUFF);
        timerOffset = AbstractDungeon.miscRng.random(0, 5000);
    }

    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new RainbowPower(this, timerOffset)));
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

        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (this.nextMove) {
            case BUFF: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, REAR));
                    addToBot(new WaitAction(1.2F));
                } else {
                    addToBot(new ChangeStateAction(this, REAR_IDLE));
                    addToBot(new WaitAction(0.9F));
                }
                addToBot(new ApplyPowerAction(this, this, new ExtraHitsPower(this, STR_AMOUNT)));
                break;
            }
            case STRUGGLE: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, OPEN_STATE));
                    addToBot(new WaitAction(0.5F));
                }
                for (int i = 0; i < multiplier; i++) {
                    addToBot(new AnimateFastAttackAction(this));
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT, true));
                }
                break;
            }
            case DEBUFF: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, REAR));
                    addToBot(new WaitAction(1.2F));
                } else {
                    addToBot(new ChangeStateAction(this, REAR_IDLE));
                    addToBot(new WaitAction(0.9F));
                }
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, DEBUFF_AMOUNT, true)));
                break;
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        ArrayList<Byte> possibilities = new ArrayList<>();
        if (this.currentHealth <= maxHealth/2.0F && !didDebuff) {
            possibilities.add(DEBUFF);
            didDebuff = true;
        } else {
            if (!this.lastMove(BUFF) && !this.lastMove(DEBUFF)) {
                possibilities.add(BUFF);
            }
        }
        possibilities.add(STRUGGLE);

        //randomly choose one with each possibility having equal weight
        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
        if (move == STRUGGLE) {
            EnemyMoveInfo info = moves.get(move);
            info.multiplier = BASE_STRUGGLE_HITS;
            if (this.hasPower(ExtraHitsPower.POWER_ID)) {
                info.multiplier += getPower(ExtraHitsPower.POWER_ID).amount;
            }
            info.isMultiDamage = info.multiplier > 1;
        }
        setMoveShortcut(move, MOVES[move]);
        firstMove = false;
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
    public void render(SpriteBatch sb) {
        if (!isDeadOrEscaped()) {
            this.tint.color.set(
                    (MathUtils.cosDeg((float) ((System.currentTimeMillis() + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                    (MathUtils.cosDeg((float) ((System.currentTimeMillis() + 1000L + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                    (MathUtils.cosDeg((float) ((System.currentTimeMillis() + 2000L + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                    this.tint.color.a);
        } else {
            this.tint.color.set(1.0F, 1.0F, 1.0F, this.tint.color.a);
        }
        super.render(sb);
    }
}
