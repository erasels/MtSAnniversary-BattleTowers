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

public class DrawCardMod extends AbstractCardModifier {

    public static String ID = makeID(DrawCardMod.class.getSimpleName());

    public DrawCardMod() {
        priority = 99;
    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        super.onUse(card, target, action);
        UC.doDraw(1);
    }
    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " +  CardCrawlGame.languagePack.getEventString(ArmorerEvent.ID).DESCRIPTIONS[2];
    }

    public boolean shouldApply(AbstractCard card) {
        return !CardModifierManager.hasModifier(card, ID);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new DrawCardMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
