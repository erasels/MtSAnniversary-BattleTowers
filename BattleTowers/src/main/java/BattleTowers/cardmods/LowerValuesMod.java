package BattleTowers.cardmods;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;

import static BattleTowers.BattleTowers.makeID;

public class LowerValuesMod extends AbstractCardModifier {

    public static String ID = makeID(LowerValuesMod.class.getSimpleName());

    public LowerValuesMod() {
        priority = 99;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        super.onInitialApplication(card);
        card.baseBlock -= 2;
        if (card.baseBlock < 0) card.baseBlock = 0;
        card.baseDamage -= 2;
        if (card.baseDamage < 0) card.baseDamage = 0;
    }

    public boolean shouldApply(AbstractCard card) {
        return !CardModifierManager.hasModifier(card, ID);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new LowerValuesMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
