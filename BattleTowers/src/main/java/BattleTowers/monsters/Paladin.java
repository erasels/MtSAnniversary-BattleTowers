package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.GainBlockRandomMonsterAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.FrailPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class Paladin extends AbstractBTMonster
{
    public static final String ID = makeID(Paladin.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte SWEEP = 0;
    private static final byte SMITE = 1;
    private static final byte FURY = 2;
    private static final byte BUFF = 3;

    //calcAscensionSpecial automatically scales the number of status cards based on ascension and enemy type
    private final int STATUS = calcAscensionSpecial(2);
    private final int HEAL = AbstractDungeon.ascensionLevel >= 17 ? 13 : 10;
    private final int BLOCK = AbstractDungeon.ascensionLevel >= 17 ? 20 : 15;

    //defaults enemy placement to 0, 0
    public Paladin() {
        this(0.0f, 0.0f);
    }

    public Paladin(final float x, final float y) {
        super(NAME, ID, 140, 0.0F, 0.0f, 250.0f, 330.0f, null, x, y);

        loadAnimation(BattleTowers.makeMonsterPath("Paladin/skeleton.atlas"), BattleTowers.makeMonsterPath("Paladin/skeleton.json"), 1.0F);

        setHp(calcAscensionTankiness(78), calcAscensionTankiness(83));

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(SWEEP, Intent.ATTACK_DEBUFF, calcAscensionDamage(13));
        addMove(SMITE, Intent.ATTACK_BUFF, calcAscensionDamage(14));
        addMove(FURY, Intent.ATTACK, calcAscensionDamage(7), 3);
        addMove(BUFF, Intent.DEFEND_BUFF);

        AnimationState.TrackEntry e = this.state.setAnimation(0, "Idle", true);
        e.setTime(e.getEndTime() * MathUtils.random());
        this.stateData.setMix("Hit", "Idle", 0.2F);
        this.state.setTimeScale(0.8F);
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
            case SWEEP: {
                firstMove = false;
                useFastAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this,
                         new FrailPower(AbstractDungeon.player, 2, true), 2));
                break;
            }
            case SMITE: {
                useSwingAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToBot(new HealAction(this, this, HEAL));
                break;
            }
            case FURY: {
                for (int i = 0; i < multiplier; i++) {
                    useSwingAnimation();
                    addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                }
                break;
            }
            case BUFF: {
                addToBot(new WaitAction(0.25F));
                addToBot(new GainBlockRandomMonsterAction(this, BLOCK));
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, STATUS), STATUS));
            }
        }

        addToBot(new RollMoveAction(this));
    }

    private void playSfx() {
        int roll = MathUtils.random(1);
        if (roll == 0) {
            addToBot(new SFXAction("VO_TANK_1A"));
        } else if (roll == 1) {
            addToBot(new SFXAction("VO_TANK_1B"));
        } else {
            addToBot(new SFXAction("VO_TANK_1C"));
        }
    }

    private void useSwingAnimation() {
        playSfx();
        addToBot(new ChangeStateAction(this, "MACE_HIT"));
        addToBot(new WaitAction(0.3F));
    }

    @Override
    public void changeState(String stateName) {
        if (stateName.equals("MACE_HIT")) {
            this.state.setAnimation(0, "Attack", false);
            this.state.addAnimation(0, "Idle", true, 0.0F);
        }
    }

    @Override
    protected void getMove(final int num) {
        if (firstMove) {
            setMoveShortcut(SWEEP);
            return;
        }
        //This is where we determine what move the monster should do next
        //Here, we add the possibilities to a list and randomly choose one with each possibility having equal weight
        ArrayList<Byte> possibilities = new ArrayList<>();

        // Only Sweep if player isn't frailed, and HP > 50%. Won't use 2x in a row (in case of Artifact).
        if (currentHealth >= maxHealth / 2 && !AbstractDungeon.player.hasPower(FrailPower.POWER_ID) && !this.lastMove(SWEEP)) {
            possibilities.add(SWEEP);
        }

        // Smite if it would not overheal from the effect. Cannot use 3x in a row.
        if (this.currentHealth <= this.maxHealth - HEAL && !this.lastTwoMoves(SMITE)) {
            possibilities.add(SMITE);
        }

        if (!this.lastMove(BUFF)) {
            possibilities.add(BUFF);
        }

        // if HP >= 50%, only Fury if haven't used it in the last 2 turns.
        if (currentHealth >= maxHealth / 2 && !this.lastMove(FURY) && !this.lastMoveBefore(FURY)) {
            possibilities.add(FURY);
        }
        // if HP < 50%, Fury is available whenever, and has increased chances. Cannot use 3x in a row.
        if (currentHealth < maxHealth / 2 && !this.lastTwoMoves(FURY)) {
            possibilities.add(FURY);
            possibilities.add(FURY);
            possibilities.add(FURY);
        }

        //randomly choose one with each possibility having equal weight
        byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));

        setMoveShortcut(move, "");
    }
}