package BattleTowers.actions;

import BattleTowers.vfx.LoseMaxHPEffect;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class DecreaseMaxHPAction extends AbstractGameAction {
    public DecreaseMaxHPAction(AbstractCreature target, int amount) {
        this.target = target;
        this.amount = amount;
    }

    @Override
    public void update() {
        target.decreaseMaxHealth(amount);
        AbstractDungeon.effectList.add(new LoseMaxHPEffect(target, amount));

        isDone = true;
    }
}
