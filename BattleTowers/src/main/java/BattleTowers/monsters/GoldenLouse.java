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
import com.megacrit.cardcrawl.powers.*;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class GoldenLouse extends AbstractBTMonster {
    public static final String ID = makeID(GoldenLouse.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    private boolean isOpen = true;

    //name of the monster's moves
    private static final byte BITE = 0;
    private static final byte BUFF = 1;
    private static final byte HEAL = 2;
    private static final String CLOSED_STATE = "CLOSED";
    private static final String OPEN_STATE = "OPEN";
    private static final String REAR_IDLE = "REAR_IDLE";
    private static final String REAR = "REAR";

    //Monster stats
    private static final int MIN_HP = 26;
    private static final int MAX_HP = 32;
    private static final int BITE_DAMAGE = 4;
    private final int BUFF_AMOUNT = calcAscensionSpecial(1);
    private final int HEAL_AMOUNT = calcAscensionSpecial(10);
    private final int WEAK_AMOUNT = calcAscensionSpecial(1);
    private final int CURL_AMOUNT = AbstractDungeon.monsterHpRng.random(calcAscensionSpecial(6), calcAscensionSpecial(8));
    private int buffStacks = 0;
    private int maxBuffStacks = 3;


    public GoldenLouse(final float x, final float y) {
        super(NAME, ID, 140, 0.0F, 0.0f, 180.0f, 140.0f, null, x, y);
        this.loadAnimation("battleTowersResources/img/monsters/MetalLouses/GoldenLouse/skeleton.atlas", "battleTowersResources/img/monsters/MetalLouses/GoldenLouse/skeleton.json", 1.2F);
        AnimationState.TrackEntry e = this.state.setAnimation(0, "idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        setHp(calcAscensionTankiness(MIN_HP), calcAscensionTankiness(MAX_HP));
        addMove(BITE, Intent.ATTACK_DEBUFF, calcAscensionDamage(BITE_DAMAGE));
        addMove(BUFF, Intent.BUFF);
        addMove(HEAL, Intent.MAGIC);
    }

    public void usePreBattleAction() {
        addToBot(new ApplyPowerAction(this, this, new CurlUpPower(this, CURL_AMOUNT)));
        addToBot(new ApplyPowerAction(this, this, new ProtectedPower(this)));
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
                buffStacks++;
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, REAR));
                    addToBot(new WaitAction(1.2F));
                } else {
                    addToBot(new ChangeStateAction(this, REAR_IDLE));
                    addToBot(new WaitAction(0.9F));
                }
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    addToBot(new ApplyPowerAction(m, this, new BufferPower(m, BUFF_AMOUNT)));
                }
                addToBot(new ApplyPowerAction(this, this, new CurlUpPower(this, CURL_AMOUNT)));
                break;
            }
            case HEAL: {
                if (!this.isOpen) {
                    addToBot(new ChangeStateAction(this, REAR));
                    addToBot(new WaitAction(1.2F));
                } else {
                    addToBot(new ChangeStateAction(this, REAR_IDLE));
                    addToBot(new WaitAction(0.9F));
                }
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    addToBot(new HealAction(m, this, HEAL_AMOUNT));
                }
                break;
            }
        }
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(int i) {
        //This is where we determine what move the monster should do next
        //Here, we add the possibilities to a list and randomly choose one with each possibility having equal weight
        ArrayList<Byte> possibilities = new ArrayList<>();
        boolean needHeal = false;
        boolean canBuff = buffStacks < maxBuffStacks;
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (m.maxHealth - m.currentHealth >= HEAL_AMOUNT) {
                needHeal = true;
                break;
            }
        }

        //HEAL if we need to, but not more than twice in a row, else alternate BUFF and BITE if we can BUFF
        if (!this.lastTwoMoves(HEAL) && needHeal) {
            possibilities.add(HEAL);
        } else {
            if (!this.lastMove(BUFF) && canBuff) {
                possibilities.add(BUFF);
            }

            if (!this.lastMove(BITE)) {
                possibilities.add(BITE);
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
}
