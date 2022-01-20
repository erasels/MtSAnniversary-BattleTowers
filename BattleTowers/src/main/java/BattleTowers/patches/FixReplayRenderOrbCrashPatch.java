package BattleTowers.patches;

import BattleTowers.util.UC;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;

@SpirePatch2(cls = "replayTheSpire.patches.ReplayOrbRenderTextPatch", method = "Prefix", requiredModId = "ReplayTheSpireMod")
public class FixReplayRenderOrbCrashPatch {
    @SpireInsertPatch(rloc = 3)
    public static SpireReturn<?> patch() {
        if(UC.p().orbs.isEmpty()) {
            return SpireReturn.Return();
        }
        return SpireReturn.Continue();
    }
}
