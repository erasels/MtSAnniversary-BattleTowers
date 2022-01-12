package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import basemod.BaseMod;
import basemod.abstracts.CustomRelic;
import basemod.cardmods.RetainMod;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.mod.stslib.actions.common.SelectCardsCenteredAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.CharacterManager;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;

import java.util.ArrayList;
import java.util.function.Predicate;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;
import static BattleTowers.util.UC.getRandomItem;

public class CardboardHeart extends CustomRelic {
    public static final String ID = makeID(CardboardHeart.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    private AbstractCard card1;
    private AbstractCard card2;
    private AbstractCard card3;

    public CardboardHeart() {
        super(ID, TextureLoader.getTexture(makeRelicPath("cardboard_heart.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        if (CardCrawlGame.isInARun()) {
            card1 = returnTrulyRandomPrediCard(c -> c.rarity == AbstractCard.CardRarity.UNCOMMON, true);
            if (AbstractDungeon.cardRandomRng.random(0, 9) == 9) {
                card2 = returnTrulyRandomPrediCard(c -> c.rarity == AbstractCard.CardRarity.RARE, true);
            }
            else {
                card2 = returnTrulyRandomPrediCard(c -> c.rarity == AbstractCard.CardRarity.UNCOMMON, true);
            }
            card3 = returnTrulyRandomPrediCard(c -> c.rarity == AbstractCard.CardRarity.UNCOMMON, true);
        }
        description = getUpdatedDescription();
        this.tips.clear();
        this.tips.add(new PowerTip(relicStrings.NAME, description));
    }

    @Override
    public void atBattleStart() {
        ArrayList<AbstractCard> myCards = new ArrayList<>();
        AbstractCard dupe1 = CardLibrary.getCopy(card1.cardID);
        AbstractCard dupe2 = CardLibrary.getCopy(card2.cardID);
        AbstractCard dupe3 = CardLibrary.getCopy(card3.cardID);
        CardModifierManager.addModifier(dupe1, new RetainMod());
        CardModifierManager.addModifier(dupe2, new RetainMod());
        CardModifierManager.addModifier(dupe3, new RetainMod());
        myCards.add(dupe1);
        myCards.add(dupe2);
        myCards.add(dupe3);
        addToBot(new SelectCardsCenteredAction(myCards, 1, DESCRIPTIONS[4], (cards) -> {
            AbstractCard newCard = CardLibrary.getCopy(cards.get(0).cardID);
            CardModifierManager.addModifier(newCard, new RetainMod());
            addToTop(new MakeTempCardInHandAction(newCard, 1));
        }));
    }

    public static ArrayList<AbstractCard> getCardsMatchingPredicate(Predicate<AbstractCard> pred, boolean allcards) {
        if (allcards) {
            ArrayList<AbstractCard> cardsList = new ArrayList<>();
            for (AbstractCard c : CardLibrary.getAllCards()) {
                if (pred.test(c)) cardsList.add(c.makeStatEquivalentCopy());
            }
            return cardsList;
        } else {
            ArrayList<AbstractCard> cardsList = new ArrayList<>();
            for (AbstractCard c : AbstractDungeon.srcCommonCardPool.group) {
                if (pred.test(c)) cardsList.add(c.makeStatEquivalentCopy());
            }
            for (AbstractCard c : AbstractDungeon.srcUncommonCardPool.group) {
                if (pred.test(c)) cardsList.add(c.makeStatEquivalentCopy());
            }
            for (AbstractCard c : AbstractDungeon.srcRareCardPool.group) {
                if (pred.test(c)) cardsList.add(c.makeStatEquivalentCopy());
            }
            return cardsList;
        }
    }

    public static AbstractCard returnTrulyRandomPrediCard(Predicate<AbstractCard> pred, boolean allCards) {
        return getRandomItem(getCardsMatchingPredicate(pred, allCards));
    }

    @Override
    public String getUpdatedDescription() {
        System.out.println(card1);
        System.out.println(card2);
        System.out.println(card3);
        if (card1 == null) {
            return DESCRIPTIONS[5];
        }
        else {
            String one = constructCardName(card1);
            String two = constructCardName(card2);
            String three = constructCardName(card3);
            return relicStrings.DESCRIPTIONS[0] + one + relicStrings.DESCRIPTIONS[1] + two + relicStrings.DESCRIPTIONS[2] + three + relicStrings.DESCRIPTIONS[3];
        }
     }

     private String constructCardName(AbstractCard c) {
         String name = c.name;
         StringBuilder sb = new StringBuilder();

         Color color = Color.YELLOW.cpy();
         for (AbstractPlayer p : CardCrawlGame.characterManager.getAllCharacters()) {
             if (p.getCardColor() == c.color) {
                 color = p.getCardTrailColor();
             }
         }
         for (String word : name.split(" ")) {
             sb.append("[#").append(color.toString()).append("]").append(word).append("[] ");
         }
         sb.setLength(sb.length() - 1);
         sb.append("[#").append(color.toString()).append("]");
         return sb.toString().trim();
     }
}
