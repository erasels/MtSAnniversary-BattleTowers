package BattleTowers.actions;

import BattleTowers.util.UC;
import BattleTowers.vfx.LoseMaxHPEffect;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class DecreaseMaxHPAction extends AbstractGameAction {
    public DecreaseMaxHPAction(AbstractCreature target, int amount) {
        this.target = target;
        this.amount = amount;
    }

    @Override
    public void update() {
        target.decreaseMaxHealth(amount);
        UC.doVfx(new LoseMaxHPEffect(amount));

        isDone = true;
    }
}
