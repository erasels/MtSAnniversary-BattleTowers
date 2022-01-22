package BattleTowers.actions;

import BattleTowers.monsters.AspiringChampion;
import com.megacrit.cardcrawl.actions.AbstractGameAction;

public class BonkChampionAction extends AbstractGameAction {
    private AspiringChampion owner;
    
    public BonkChampionAction(AspiringChampion owner) {
        this.owner = owner;
        this.actionType = ActionType.DEBUFF;
    }
    
    @Override
    public void update() {
        this.owner.debuffAttack();
        this.isDone = true;
    }
}
