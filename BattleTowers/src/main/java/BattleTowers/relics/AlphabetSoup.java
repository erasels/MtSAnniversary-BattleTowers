package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import BattleTowers.util.UC;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.mod.stslib.actions.common.SelectCardsCenteredAction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;
import static BattleTowers.util.UC.atb;
import static BattleTowers.util.UC.att;

public class AlphabetSoup extends CustomRelic implements CustomSavable<String> {
    public static final String ID = makeID(AlphabetSoup.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    private static Map<String, CardStrings> cardsENG;

    private String letter = "";

    public AlphabetSoup() {
        super(ID, TextureLoader.getTexture(makeRelicPath("AlphabetSoup.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
        if (cardsENG == null) {
            String langPackDir = "localization" + File.separator + "eng";
            String cardPath = langPackDir + File.separator + "cards.json";
            Type cardType = (new TypeToken<Map<String, CardStrings>>() {
            }).getType();
            Gson gson = new Gson();
            cardsENG = (Map) gson.fromJson(Gdx.files.internal(cardPath).readString(String.valueOf(StandardCharsets.UTF_8)), cardType);
        }
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
            ArrayList<AbstractCard> valid = CardboardHeart.getCardsMatchingPredicate(c -> {
                if (cardsENG.containsKey(c.cardID)) {
                    String s = cardsENG.get(c.cardID).NAME.toLowerCase();
                    return (s.startsWith(letter.toLowerCase()) && c.rarity != AbstractCard.CardRarity.SPECIAL && c.type != AbstractCard.CardType.STATUS && c.rarity != AbstractCard.CardRarity.CURSE && c.type != AbstractCard.CardType.CURSE);
                }
                return false;
            }, true);
            Collections.shuffle(valid);
            ArrayList<AbstractCard> set = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                if (valid.size() - 1 > i) {
                    set.add(valid.get(i).makeCopy());
                }
            }
            //In case there are no cards of the letter in the entire pool.
            if (set.size() == 0) {
                ArrayList<AbstractCard> valid3 = CardboardHeart.getCardsMatchingPredicate(c -> {
                    return c.rarity != AbstractCard.CardRarity.SPECIAL && c.type != AbstractCard.CardType.STATUS && c.rarity != AbstractCard.CardRarity.CURSE && c.type != AbstractCard.CardType.CURSE;
                }, true);
                set.add(valid3.get(0));
                set.add(valid3.get(1));
                set.add(valid3.get(2));
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
                atb(new GainEnergyAction(1));
            }
        }
    }

    @Override
    public String getUpdatedDescription() {
        if (letter == "") {
            return DESCRIPTIONS[0];
        } else {
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
