package BattleTowers.cardmods;

import BattleTowers.events.ArmorerEvent;
import BattleTowers.util.UC;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.powers.WeakPower;

import static BattleTowers.BattleTowers.makeID;

public class ApplyWeakMod extends AbstractCardModifier {

    public static String ID = makeID(ApplyWeakMod.class.getSimpleName());

    public ApplyWeakMod() {
        priority = 99;
    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        super.onUse(card, target, action);
        UC.doPow(new WeakPower(target, 1, false));
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " +  CardCrawlGame.languagePack.getEventString(ArmorerEvent.ID).DESCRIPTIONS[1];
    }

    public boolean shouldApply(AbstractCard card) {
        return !CardModifierManager.hasModifier(card, ID);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new ApplyWeakMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
