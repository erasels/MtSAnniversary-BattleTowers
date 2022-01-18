package BattleTowers.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.AbstractGameAction.ActionType;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class SpecialLoseAllBlockAction extends AbstractGameAction {
    private static final float DUR = 0.25F;

    public SpecialLoseAllBlockAction(AbstractCreature target, AbstractCreature source) {
        this.setValues(target, source, this.amount);
        this.actionType = ActionType.BLOCK;
        this.duration = 0.25F;
    }

    public void update() {
        if (!this.target.isDying && !this.target.isDead && this.duration == 0.25F && this.target.currentBlock > 0) {
            this.target.loseBlock();
        }

        this.tickDuration();
    }
}
