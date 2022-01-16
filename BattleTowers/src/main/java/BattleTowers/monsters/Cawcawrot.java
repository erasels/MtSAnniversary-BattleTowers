package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.actions.WharghAction;
import BattleTowers.powers.SuperRitualPower;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.DexterityPower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class Cawcawrot extends AbstractBTMonster
{
    public static final String ID = makeID(Cawcawrot.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;
    private static final String IMG = BattleTowers.makeImagePath("monsters/Cawcawrot/Cawcawrot.png");

    private boolean firstMove = true;

    //name of the monster's moves
    private static final byte RITUAL = 0;
    private static final byte CAWCAW = 1;
    private static final int RITUAL_AMT = 2;
    private static final int A_2_RITUAL_AMT = 3;
    private static final int A_17_RITUAL_AMT = 4;
    private int ritualAmount;
    private int BASE_ATTACK = 6;
    private int BASE_BLOCK = 6;
    private int HEALTH_MIN = 100;
    private int HEALTH_MAX = 109;

    //defaults enemy placement to 0, 0
    public Cawcawrot() {
        this(0.0f, 0.0f);
    }

    public Cawcawrot(final float x, final float y) {
        // maxHealth param doesn't matter, we will override it with setHP
        // hb_x and hb_y shifts the monster's AND its health bar's position around on the screen, usually you don't need to change these values
        // hb_w affects how wide the monster's health bar is. hb_h affects how far up the monster's intent image is. Adjust these values until they look good
        super(NAME, ID, 140, -8.0F, 0.0f, 260.0f, 350.0f, IMG, x, y);
        intentOffsetX = -50.0f;

        if (AbstractDungeon.ascensionLevel >= 17)
            ritualAmount = A_17_RITUAL_AMT;
        else if (AbstractDungeon.ascensionLevel >= 2)
            ritualAmount = A_2_RITUAL_AMT;
        else
            ritualAmount = RITUAL_AMT;

        // calcAscensionTankiness automatically scales HP based on ascension and enemy type
        // passing 2 values makes the game randomly select a value in between the ranges for the HP
        // if you pass only 1 value to set HP it will use that as the HP value
        setHp(calcAscensionTankiness(HEALTH_MIN), calcAscensionTankiness(HEALTH_MAX));

        // Add these moves to the move hashmap, we will be using them later in getMove
        // calc AscensionDamage automatically scales damage based on ascension and enemy type
        addMove(RITUAL, Intent.BUFF);
        addMove(CAWCAW, Intent.ATTACK_DEFEND, calcAscensionDamage(BASE_ATTACK));
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        type = EnemyType.NORMAL;
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        int multiplier = moves.get(nextMove).multiplier;

        if(info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        //carries out actions based on the current move
        //useFastAttackAnimation causes the monster to jump forward when it attacks
        switch (nextMove) {
            case RITUAL: {
                addToBot(new WharghAction(this));
                addToBot(new ApplyPowerAction(this, this, new SuperRitualPower(this, ritualAmount, false)));
                break;
            }
            case CAWCAW: {
                useFastAttackAnimation();
                addToBot(new DamageAction(AbstractDungeon.player, info, AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                int block_amt = BASE_BLOCK;
                if (hasPower(DexterityPower.POWER_ID))
                    block_amt = BASE_BLOCK + getPower(DexterityPower.POWER_ID).amount;
                addToBot(new GainBlockAction(this, block_amt));
                break;
            }
        }

        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        ArrayList<Byte> possibilities = new ArrayList<>();

        byte move = CAWCAW;

        if (firstMove) {
            firstMove = false;
            move = RITUAL;
        }

        setMoveShortcut(move, MOVES[move]);
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
    }

    @Override
    public void update() {
        super.update();
    }
}