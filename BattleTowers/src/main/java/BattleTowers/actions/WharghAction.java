package BattleTowers.actions;

import BattleTowers.BattleTowers;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.SpeechBubble;

public class WharghAction extends AbstractGameAction {
    private static final String message = "I'm powering up! WHARGH!";
    private AbstractMonster monster;
    private static final float DURATION = 2.0F;
    private static final float BUBBLE_DUR = 2.0F;

    public WharghAction(AbstractMonster monster) {
        this.monster = monster;
        duration = DURATION;
        actionType = ActionType.TEXT;
    }

    public void update() {
        if (duration == DURATION) {
            AbstractDungeon.effectList.add(new SpeechBubble(monster.hb.cX + monster.dialogX,
                    monster.hb.cY + monster.dialogY, BUBBLE_DUR, message, false));
            CardCrawlGame.sound.play(BattleTowers.WHARGH_KEY);
        }
        tickDuration();
    }
}