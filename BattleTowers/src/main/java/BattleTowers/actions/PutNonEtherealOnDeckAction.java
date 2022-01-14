package BattleTowers.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class PutNonEtherealOnDeckAction extends AbstractGameAction {
    private boolean first;

    public PutNonEtherealOnDeckAction() {
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = Settings.FAST_MODE ? 0.1f : 0.5f;
        first = true;
    }

    public void update() {
        if (first) {
            first = false;

            int i = 0;
            while (i < AbstractDungeon.player.hand.size()) {
                AbstractCard c = AbstractDungeon.player.hand.getNCardFromTop(i);
                if (!c.isEthereal) {
                    AbstractDungeon.player.hand.moveToDeck(c, false);
                }
                else {
                    ++i;
                }
            }
        }

        this.tickDuration();
    }
}