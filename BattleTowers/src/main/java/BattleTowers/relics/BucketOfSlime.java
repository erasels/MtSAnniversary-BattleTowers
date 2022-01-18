package BattleTowers.relics;

import BattleTowers.cardmod.SlimyCardmod;
import BattleTowers.interfaces.ModifyCombatCardsRelic;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class BucketOfSlime extends CustomRelic implements ModifyCombatCardsRelic {
    public static final String ID = makeID(BucketOfSlime.class.getSimpleName());
    //On combat deck initialization add a card mod that gives 1 block to each card?
    //Probably a bit more reliable than checking through at start of combat.

    public BucketOfSlime() {
        super(ID, TextureLoader.getTexture(makeRelicPath("SlimeBucket.png")), TextureLoader.getTexture(makeRelicPath("SlimeBucketOutline.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    @Override
    public void modifyCombatDeck(CardGroup cards) {
        for (AbstractCard c : cards.group) {
            CardModifierManager.addModifier(c, new SlimyCardmod());
        }
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
