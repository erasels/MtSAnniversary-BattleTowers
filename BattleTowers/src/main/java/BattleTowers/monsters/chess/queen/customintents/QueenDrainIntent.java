package BattleTowers.monsters.chess.queen.customintents;

import BattleTowers.RazIntent.CustomIntent;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeUIPath;

public class QueenDrainIntent extends CustomIntent {

    public static final String ID = makeID("MassAttackIntent");

    private static final UIStrings uiStrings;
    private static final String[] TEXT;


    public QueenDrainIntent() {
        super(IntentEnums.QUEEN_DRAIN, TEXT[0],
                makeUIPath("areaIntent_L.png"),
                makeUIPath("areaIntent.png"));
    }

    @Override
    public String description(AbstractMonster mo) {
        String result = TEXT[1];
        return result;
    }

    static {
        uiStrings = CardCrawlGame.languagePack.getUIString(ID);
        TEXT = uiStrings.TEXT;
    }
}