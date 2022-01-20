package BattleTowers.actions;

import BattleTowers.monsters.AspiringChampion;
import com.megacrit.cardcrawl.actions.AbstractGameAction;

public class ChangeChampImageAction extends AbstractGameAction {
    private AspiringChampion aspiringChampion;
    public ChangeChampImageAction(AspiringChampion aspiringChampion) {
        this.aspiringChampion = aspiringChampion;
    }
    @Override
    public void update() {
        aspiringChampion.swapImage();
        this.isDone = true;
    }
}
