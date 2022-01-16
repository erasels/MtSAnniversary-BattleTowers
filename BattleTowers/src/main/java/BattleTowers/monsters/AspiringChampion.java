package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.powers.ClunkyPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.MalleablePower;
import com.megacrit.cardcrawl.powers.ReactivePower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;

public class AspiringChampion extends AbstractBTMonster{
    
    public static final String ID = makeID(AspiringChampion.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;
    private static final int MAX_HEALTH = 225;
    private static final String IMG = BattleTowers.makeImagePath("monsters/AspiringChampion/AspiringChampion.png");
    
    
    private static final byte COPYCAT_A = 0;
    private static final byte COPYCAT_B = 1;
    private static final byte HEAVY_BLOWS = 2;
    private static final byte RECOVER = 3;
    private static final byte HEAVY_BLOWS_DEBUFFED = 4;
    private static final byte COPYCAT_B_DEBUFFED = 5;
    private static final byte COPYCAT_B_HIGH_ASCENSION = 6;
    
    public AspiringChampion() {
        this(0.0f, 0.0f);
    }
    
    public AspiringChampion(float hb_x, float hb_y) {
        super(NAME, ID, MAX_HEALTH, hb_x, hb_y, 400.0F, 410.0F, IMG);
        addMove(COPYCAT_A, Intent.UNKNOWN);
        addMove(COPYCAT_B, Intent.ATTACK, calcAscensionDamage(0), 2);
        addMove(HEAVY_BLOWS, Intent.ATTACK, calcAscensionDamage(15), 2);
        addMove(RECOVER, Intent.BUFF);
    }
    
    public void usePreBattleAction() {
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new ClunkyPower(this)));
    }
    
    @Override
    public void takeTurn() {
    
    }
    
    @Override
    protected void getMove(int i) {
        if(moveHistory.isEmpty())
        {
            ArrayList<Byte> possibilities = new ArrayList<>();
            possibilities.add(COPYCAT_A);
            possibilities.add(HEAVY_BLOWS);
            byte move = possibilities.get(AbstractDungeon.monsterRng.random(possibilities.size() - 1));
            setMoveShortcut(move, MOVES[move]);
        }
        else if(lastMove(COPYCAT_A))
        {
            if(AbstractDungeon.ascensionLevel < 18) {
                setMoveShortcut(COPYCAT_B, MOVES[COPYCAT_B]);
            }
            else
            {
                setMoveShortcut(COPYCAT_B_HIGH_ASCENSION, MOVES[COPYCAT_B_HIGH_ASCENSION]);
            }
        }
        else if(lastMove(HEAVY_BLOWS) || lastMove(HEAVY_BLOWS_DEBUFFED))
        {
            setMoveShortcut(RECOVER);
        }
        else if(!(lastTwoMoves(HEAVY_BLOWS) || lastTwoMoves(HEAVY_BLOWS_DEBUFFED)))
        {
            setMoveShortcut(HEAVY_BLOWS, MOVES[HEAVY_BLOWS]);
        }
        else
        {
            setMoveShortcut(COPYCAT_A, MOVES[COPYCAT_A]);
        }
    }
}
