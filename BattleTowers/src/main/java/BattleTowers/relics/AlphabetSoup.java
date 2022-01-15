package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import BattleTowers.util.UC;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import basemod.cardmods.RetainMod;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.mod.stslib.actions.common.SelectCardsCenteredAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;
import static BattleTowers.util.UC.*;

public class AlphabetSoup extends CustomRelic implements CustomSavable<String> {
    public static final String ID = makeID(AlphabetSoup.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    private String letter = "";

    public AlphabetSoup() {
        super(ID, TextureLoader.getTexture(makeRelicPath("cardboard_heart.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    public void setLetter(String letter) {
        this.letter = letter;
        description = getUpdatedDescription();
        this.tips.clear();
        this.tips.add(new PowerTip(relicStrings.NAME, description));
    }

    @Override
    public void atBattleStart() {
        if (letter != "") {
            ArrayList<AbstractCard> valid = CardboardHeart.getCardsMatchingPredicate(c -> c.name.toLowerCase().startsWith(letter.toLowerCase()), true);
            Collections.shuffle(valid);
            ArrayList<AbstractCard> set = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                if (valid.size() - 1 > i) {
                    set.add(valid.get(i).makeCopy());
                }
            }
            atb(new SelectCardsCenteredAction(set, DESCRIPTIONS[4], (cards) -> {
                att(new MakeTempCardInHandAction(cards.get(0).makeCopy()));
            }));
        }
    }

    @Override
    public void onPlayCard(AbstractCard c, AbstractMonster m) {
        if (letter != "") {
            if (c.name.toLowerCase().startsWith(letter.toLowerCase())) {
                flash();
                atb(new GainBlockAction(UC.p(), 4));
            }
        }
    }

    @Override
    public String getUpdatedDescription() {
        if (letter == "") {
            return DESCRIPTIONS[0];
        }
        else {
            return DESCRIPTIONS[1] + letter + DESCRIPTIONS[2] + letter + DESCRIPTIONS[3];
        }
    }

    @Override
    public String onSave() {
        return letter;
    }

    @Override
    public void onLoad(String s) {
        setLetter(s);
    }
}
