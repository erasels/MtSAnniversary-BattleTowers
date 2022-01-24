package BattleTowers.patches;

import BattleTowers.BattleTowers;
import BattleTowers.room.BattleTowerRoom;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import javassist.CtBehavior;

@SpirePatch(clz = OpenALMusic.class, method = "play")
public class ScrewAudioBuffersButSoftlyPleaseDontTakeMySoundAwayPleaseThanksPatch {
    @SpireInsertPatch(locator = Locator.class, localvars = {"errorCode"})
    public static SpireReturn<?> patch(OpenALMusic __instance, int errorCode) {
        if(errorCode == 40963 && CardCrawlGame.isInARun() && AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            BattleTowers.logger.info("Intercepted audio buffer crash in battle towers, Error Code: " + errorCode + "\n All sound is probably gone, please restart the game to get it back. I'm sorry :(");
            return SpireReturn.Return(null);
        }

        return SpireReturn.Continue();
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher finalMatcher = new Matcher.NewExprMatcher(GdxRuntimeException.class);
            return LineFinder.findInOrder(ctBehavior, finalMatcher);
        }
    }
}

