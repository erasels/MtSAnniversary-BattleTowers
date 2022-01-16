package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.status.VoidCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class VoidBlight extends CustomRelic {
    public static final String ID = makeID(VoidBlight.class.getSimpleName());

    public VoidBlight() {
        super(ID, TextureLoader.getTexture(makeRelicPath("VoidBlight.png")), RelicTier.SPECIAL, LandingSound.HEAVY);
        description = getUpdatedDescription();
    }

    @Override
    public void atBattleStart() {
        AbstractDungeon.actionManager.addToBottom(new MakeTempCardInDrawPileAction(new VoidCard(), 1, true, true));
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}