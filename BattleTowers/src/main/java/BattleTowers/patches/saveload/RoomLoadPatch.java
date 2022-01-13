package BattleTowers.patches.saveload;

import BattleTowers.events.TowerEvent;
import BattleTowers.room.BattleTowerRoom;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CtBehavior;

@SpirePatch(
        clz = AbstractDungeon.class,
        method = "nextRoomTransition",
        paramtypez = { SaveFile.class }
)
public class RoomLoadPatch {
    @SpireInsertPatch(
            locator = Locator.class,
            localvars = { "isLoadingPostCombatSave" }
    )
    public static void LoadTower(AbstractDungeon __instance, SaveFile saveFile, @ByRef boolean[] isLoadingPostCombatSave) {
        if (saveFile != null && AbstractDungeon.nextRoom != null && AbstractDungeon.nextRoom.room instanceof BattleTowerRoom) {
            if (SaveFilePatch.Data.chosenTower.get(saveFile) != null) {
                isLoadingPostCombatSave[0] = true;

                if (AbstractDungeon.nextRoom.room.event instanceof TowerEvent) {
                    TowerEvent event = (TowerEvent) AbstractDungeon.nextRoom.room.event;

                    event.loadSave(SaveFilePatch.Data.chosenTower.get(saveFile), SaveFilePatch.Data.pathTaken.get(saveFile), saveFile.post_combat);
                }
            }
        }
    }

    private static class Locator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
        {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractDungeon.class, "nextRoom");
            return new int[] { LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher)[3] };
        }
    }
}
