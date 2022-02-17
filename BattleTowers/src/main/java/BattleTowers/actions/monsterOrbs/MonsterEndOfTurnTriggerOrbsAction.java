package BattleTowers.actions.monsterOrbs;

import BattleTowers.monsters.OrbUsingMonster;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.orbs.AbstractOrb;

public class MonsterEndOfTurnTriggerOrbsAction extends AbstractGameAction {
    protected OrbUsingMonster owner;
    public MonsterEndOfTurnTriggerOrbsAction(OrbUsingMonster onwer) {
        this.owner = onwer;
    }

    @Override
    public void update() {
        for (AbstractOrb o : owner.orbs) {
            o.onEndOfTurn();
        }

        isDone = true;
    }
}
