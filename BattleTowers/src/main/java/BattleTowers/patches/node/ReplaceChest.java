package BattleTowers.patches.node;

import BattleTowers.room.BattleTowerRoom;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheCity;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.TreasureRoom;
import javassist.CtBehavior;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.mapRng;

@SpirePatch(
        clz = AbstractDungeon.class,
        method = "generateMap"
)
public class ReplaceChest {
    public static float appearRate = 1.0f; //config option?

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void GenerateTower() {
        //Should it be any act 2 or just the city?
        if (TheCity.ID.equals(AbstractDungeon.id) && mapRng.randomBoolean(appearRate)) {
            List<MapRoomNode> row = AbstractDungeon.map.get(8);
            List<MapRoomNode> exists = new ArrayList<>();
            for (MapRoomNode n : row) {
                if (n.getRoom() != null && n.getRoom() instanceof TreasureRoom && n.hasEdges()) {
                    exists.add(n);
                }
            }

            if (!exists.isEmpty()) {
                exists.get(mapRng.random(exists.size() - 1)).setRoom(new BattleTowerRoom());
            }
        }
    }

    private static class Locator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
        {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(Logger.class, "info");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}
