package BattleTowers.monsters.chess.queen.customintents;

import BattleTowers.RazIntent.CustomIntent;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeUIPath;

public class QueenDrainIntent extends CustomIntent {

    public static final String ID = makeID("QueenDrainIntent");

    private static final UIStrings uiStrings;
    private static final String[] TEXT;


    public QueenDrainIntent() {
        super(IntentEnums.QUEEN_DRAIN_ATTACK, TEXT[0],
                makeUIPath("queenDrain_L.png"),
                makeUIPath("queenDrain.png"));
    }

    @Override
    public String description(AbstractMonster mo) {
        String result = TEXT[1];
        result += mo.getIntentDmg();
        result += TEXT[2];
        int hitCount;
        if ((Boolean) ReflectionHacks.getPrivate(mo, AbstractMonster.class, "isMultiDmg")) {
            hitCount = (Integer) ReflectionHacks.getPrivate(mo, AbstractMonster.class, "intentMultiAmt");
        } else {
        }

        return result;
    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString(ID);
        TEXT = uiStrings.TEXT;
    }
}