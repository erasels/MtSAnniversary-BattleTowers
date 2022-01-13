package BattleTowers.relics;

import BattleTowers.room.BattleTowerRoom;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class Torch extends CustomRelic {
    public static final String ID = makeID(Torch.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public Torch() {
        super(ID, TextureLoader.getTexture(makeRelicPath("torch.png")), TextureLoader.getTexture(makeRelicPath("torch_outline.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        getUpdatedDescription();
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public void onEnterRoom(AbstractRoom room) {
        if(!(room instanceof BattleTowerRoom)) {
            setTextureOutline(TextureLoader.getTexture(makeRelicPath("unlitTorch.png")), TextureLoader.getTexture(makeRelicPath("unlitTorch_outline.png")));
        }
    }
}
