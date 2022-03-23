package BattleTowers.patches.compatibility;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.ShopRoom;

@SpirePatch2(cls= "com.evacipated.cardcrawl.mod.hubris.patches.RugNotForSalePatch$Update", method = "startCombat", requiredModId = "hubris")
public class AntiRugPatch {
    @SpirePrefixPatch
    public static SpireReturn<?> patch() {
        if(AbstractDungeon.getCurrMapNode() != null && !(AbstractDungeon.getCurrRoom() instanceof ShopRoom)) {
            return SpireReturn.Return();
        }
        return SpireReturn.Continue();
    }
}
