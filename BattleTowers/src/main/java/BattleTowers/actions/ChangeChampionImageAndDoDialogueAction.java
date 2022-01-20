package BattleTowers.actions;

import BattleTowers.monsters.AspiringChampion;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.MegaSpeechBubble;

public class ChangeChampionImageAndDoDialogueAction extends AbstractGameAction {
    private AspiringChampion aspiringChampion;
    private String msg;
    public ChangeChampionImageAndDoDialogueAction(AspiringChampion aspiringChampion, String msg) {
        this.aspiringChampion = aspiringChampion;
        this.source = aspiringChampion;
        this.msg = msg;
    }
    @Override
    public void update() {
        AbstractDungeon.effectList.add(new MegaSpeechBubble(this.source.hb.cX + this.source.dialogX, this.source.hb.cY + this.source.dialogY, 2.0f, this.msg, false));
        aspiringChampion.swapImage();
        this.isDone = true;
    }
}
