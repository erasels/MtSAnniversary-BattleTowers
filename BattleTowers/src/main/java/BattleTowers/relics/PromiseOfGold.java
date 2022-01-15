package BattleTowers.relics;

import BattleTowers.events.RoarOfTheCrowd;
import BattleTowers.room.BattleTowerRoom;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.text.MessageFormat;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class PromiseOfGold extends CustomRelic {
    public static final String ID = makeID(PromiseOfGold.class.getSimpleName());

    public PromiseOfGold() {
        super(ID, TextureLoader.getTexture(makeRelicPath(PromiseOfGold.class.getSimpleName() + ".png")), TextureLoader.getTexture(makeRelicPath("outline/" + PromiseOfGold.class.getSimpleName() + ".png")), RelicTier.SPECIAL, LandingSound.FLAT);
        this.counter = 1;
    }

    @Override
    public void onVictory() {
        if (this.counter != -1 && AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            if (AbstractDungeon.getCurrRoom().monsters != null
                    && AbstractDungeon.getCurrRoom().monsters.monsters != null
                    && AbstractDungeon.getCurrRoom().monsters.monsters.stream().anyMatch(m -> m.type == AbstractMonster.EnemyType.BOSS)) {
                AbstractDungeon.getCurrRoom().addGoldToRewards(RoarOfTheCrowd.GOLD_AT_END);
                this.counter = -1;
                this.usedUp();
            }
        }
    }

    @Override
    public String getUpdatedDescription() {
        return MessageFormat.format(DESCRIPTIONS[0], RoarOfTheCrowd.GOLD_AT_END);
    }
}
