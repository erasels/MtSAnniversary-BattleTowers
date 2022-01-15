package BattleTowers.cardmods;

import BattleTowers.events.ArmorerEvent;
import BattleTowers.util.UC;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;

import static BattleTowers.BattleTowers.makeID;

public class GainVulnerableMod extends AbstractCardModifier {

    public static String ID = makeID(GainVulnerableMod.class.getSimpleName());

    public GainVulnerableMod() {
        priority = 99;
    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        super.onUse(card, target, action);
        UC.doPow(new VulnerablePower(UC.p(), 1, false));
    }
    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " +  CardCrawlGame.languagePack.getEventString(ArmorerEvent.ID).DESCRIPTIONS[4];
    }
    public boolean shouldApply(AbstractCard card) {
        return !CardModifierManager.hasModifier(card, ID);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainVulnerableMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
