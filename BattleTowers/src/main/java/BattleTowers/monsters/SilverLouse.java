package BattleTowers.monsters;

import BattleTowers.powers.ProtectedPower;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.CurlUpPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class SilverLouse extends AbstractBTMonster {
    public static final String ID = makeID(SilverLouse.class.getSimpleName());
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
    private static final int MIN_HP = 42;
    private static final int MAX_HP = 48;
    private static final int BITE_DAMAGE = 10;
    private static final int SCRATCH_DAMAGE = 4;
    private final int STR_AMOUNT = calcAscensionSpecial(2);
    private final int VULN_AMOUNT = calcAscensionSpecial(1);
    private final int CURL_AMOUNT = AbstractDungeon.monsterHpRng.random(calcAscensionSpecial(calcAscensionTankiness(10)), calcAscensionSpecial(calcAscensionTankiness(14)));


    public SilverLouse(final float x, final float y) {
        super(NAME, ID, 140, 0.0F, 0.0F, 180.0F/0.8F, 140.0F/0.8F, null, x, y);
        this.loadAnimation("battleTowersResources/img/monsters/Louses/SilverLouse/skeleton.atlas", "battleTowersResources/img/monsters/Louses/SilverLouse/skeleton.json", 0.8F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        setHp(calcAscensionTankiness(MIN_HP), calcAscensionTankiness(MAX_HP));
        addMove(BITE, Intent.ATTACK, calcAscensionDamage(BITE_DAMAGE));
        addMove(BUFF, Intent.BUFF);
        addMove(SCRATCH, Intent.ATTACK, calcAscensionDamage(SCRATCH_DAMAGE), 2, true);
    }

    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new CurlUpPower(this, CURL_AMOUNT)));
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
                //addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new VulnerablePower(AbstractDungeon.player, VULN_AMOUNT, true)));
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
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, STR_AMOUNT)));
                break;
            }
            case SCRATCH: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, OPEN_STATE));
                    addToBot(new WaitAction(0.5F));
                }
                addToBot(new AnimateSlowAttackAction(this));
                for (int i = 0; i < multiplier; i++) {
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                }
                break;
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        ArrayList<Byte> possibilities = new ArrayList<>();

        if (!this.lastTwoMoves(BITE)) {
            possibilities.add(BITE);
        }

        if (!this.lastMove(BUFF)) {
            possibilities.add(BUFF);
        }

        if (!this.lastMove(SCRATCH) && !this.lastMoveBefore(SCRATCH)) {
            possibilities.add(SCRATCH);
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
}
