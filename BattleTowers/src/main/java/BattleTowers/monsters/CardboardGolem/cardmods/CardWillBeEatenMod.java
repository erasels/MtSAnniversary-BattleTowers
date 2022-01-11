package BattleTowers.monsters.CardboardGolem.cardmods;

import BattleTowers.monsters.CardboardGolem.CardboardGolem;
import BattleTowers.monsters.CardboardGolem.powers.CardEaterPower;
import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class CardWillBeEatenMod extends AbstractCardModifier {

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        card.exhaust = true;
        action.exhaustCard = true;

        if (AbstractDungeon.getCurrRoom().monsters.monsters.get(0) instanceof CardboardGolem) {
            //GK don't look at this line
            AbstractDungeon.actionManager.addToBottom(new HealAction(AbstractDungeon.getCurrRoom().monsters.monsters.get(0), AbstractDungeon.getCurrRoom().monsters.monsters.get(0), AbstractDungeon.getCurrRoom().monsters.monsters.get(0).getPower(CardEaterPower.POWER_ID).amount));
        }
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new CardWillBeEatenMod();
    }
}
