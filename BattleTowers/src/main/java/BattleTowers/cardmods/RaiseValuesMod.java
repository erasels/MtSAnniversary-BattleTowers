package BattleTowers.cardmods;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;

import static BattleTowers.BattleTowers.makeID;

public class RaiseValuesMod extends AbstractCardModifier {

    public static String ID = makeID(RaiseValuesMod.class.getSimpleName());

    public RaiseValuesMod() {
        priority = 99;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        super.onInitialApplication(card);
        card.baseBlock += 2;
        card.upgradedBlock = true;
        card.baseDamage += 2;
        card.upgradedDamage = true;
    }

    public boolean shouldApply(AbstractCard card) {
        return !CardModifierManager.hasModifier(card, ID);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new RaiseValuesMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
