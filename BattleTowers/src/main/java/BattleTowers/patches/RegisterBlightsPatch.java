package BattleTowers.patches;

import BattleTowers.blights.GreedBlight;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.helpers.BlightHelper;

@SpirePatch2(clz = BlightHelper.class, method = "getBlight")
public class RegisterBlightsPatch {
    @SpirePrefixPatch
    public static SpireReturn<AbstractBlight> returnBlight(String id) {
        if (id.equals(GreedBlight.ID)) {
            return SpireReturn.Return(new GreedBlight());
        }

        return SpireReturn.Continue();
    }
}
