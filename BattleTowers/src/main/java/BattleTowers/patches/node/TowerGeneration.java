package BattleTowers.patches.node;

import BattleTowers.room.BattleTowerRoom;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheCity;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import javassist.CtBehavior;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static BattleTowers.BattleTowers.logger;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.map;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.mapRng;

@SpirePatch(
        clz = AbstractDungeon.class,
        method = "generateMap"
)
public class TowerGeneration {
    public static boolean DEBUG = true;
    public static float appearRate = 1.0f; //config option?

    /*
        Purpose:
        Find a node to place the tower that can be connected to as many paths as possible before and after without having any crossover paths.
     */

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void GenerateTower() {
        //Should it be any act 2 or just the city?
        if ((DEBUG || TheCity.ID.equals(AbstractDungeon.id)) && mapRng.randomBoolean(appearRate)) {
            //Time to catalogue some paths
            Map<MapRoomNode, Set<MapRoomNode>> prior = scorePrior(8);
            Map<MapRoomNode, Set<MapRoomNode>> following = scoreFollowing(8);

            List<MapRoomNode> possible = new ArrayList<>();
            int highScore = 0, score;
            logger.info("Scoring:");
            for (MapRoomNode n : AbstractDungeon.map.get(8)) {
                score = prior.getOrDefault(n, Collections.emptySet()).size();
                score += following.getOrDefault(n, Collections.emptySet()).size();
                logger.info("("+n.x+","+n.y+") - " + score);
                if (score > highScore) {
                    possible.clear();
                    highScore = score;
                }
                if (score == highScore) {
                    possible.add(n);
                }
            }

            if (possible.isEmpty()) {
                logger.error("Somehow found no possible nodes?");
            }
            else {
                MapRoomNode chosen = possible.get(mapRng.random(possible.size() - 1));
                chosen.setRoom(new BattleTowerRoom());

                //add following edges
                Set<MapRoomNode> connected = following.get(chosen);
                possible.clear();
                for (MapRoomNode n : connected) {
                    if (n.y == chosen.y + 1)
                        possible.add(n);
                }

                if (!possible.isEmpty()) {
                    for (MapRoomNode n : possible) {
                        if (!chosen.isConnectedTo(n)) {
                            chosen.addEdge(new MapEdge(chosen.x, chosen.y, chosen.offsetX, chosen.offsetY, n.x, n.y, n.offsetX, n.offsetY, false));
                        }
                    }
                    chosen.getEdges().sort(MapEdge::compareTo);
                }
                else {
                    logger.error("Somehow found no possible following nodes?");
                }

                //add preceding edges
                connected = prior.get(chosen);
                possible.clear();
                for (MapRoomNode n : connected) {
                    if (n.y == chosen.y - 1)
                        possible.add(n);
                }

                if (!possible.isEmpty()) {
                    for (MapRoomNode n : possible) {
                        if (!n.isConnectedTo(chosen)) {
                            n.addEdge(new MapEdge(n.x, n.y, n.offsetX, n.offsetY, chosen.x, chosen.y, chosen.offsetX, chosen.offsetY, false));
                            n.getEdges().sort(MapEdge::compareTo);
                        }
                    }
                }
                else {
                    logger.error("Somehow found no possible preceding nodes?");
                }
            }
        }
    }

    //Score is equal to the number of unique nodes in possible paths before and after this node.
    //Counts number of prior and following nodes in each path.
    private static Map<MapRoomNode, Set<MapRoomNode>> scorePrior(int end) {
        Map<MapRoomNode, Set<MapRoomNode>> scored = new HashMap<>(), temp;

        for (MapRoomNode n : getExisting(0)) { //All the starting nodes have a score of 1.
            Set<MapRoomNode> takable = new HashSet<>();
            takable.add(n);
            scored.put(n, takable);
        }

        for (int i = 0; i < end - 1; ++i) {
            temp = new HashMap<>();
            for (Map.Entry<MapRoomNode, Set<MapRoomNode>> path : scored.entrySet()) {
                for (MapEdge connection : path.getKey().getEdges()) {
                    MapRoomNode dest = map.get(connection.dstY).get(connection.dstX);
                    if (dest.room != null) {
                        Set<MapRoomNode> takable = new HashSet<>(path.getValue());
                        takable.add(dest);
                        temp.merge(dest, takable, (a, b)->{a.addAll(b); return a;});
                    }
                }
            }
            scored = temp;
        }

        temp = new HashMap<>();
        int left, right, index;
        for (Map.Entry<MapRoomNode, Set<MapRoomNode>> path : scored.entrySet()) {
            left = Integer.MAX_VALUE;
            right = Integer.MIN_VALUE;
            for (MapEdge connection : path.getKey().getEdges()) { //Check the limits of the existing paths
                if (connection.dstX < left)
                    left = connection.dstX;
                if (connection.dstX > right)
                    right = connection.dstX;
            }
            index = path.getKey().x;
            int boundary = 0; //Check for the furthest left it can go without intersecting another edge
            while (index > 0) {
                --index;
                MapRoomNode toLeft = map.get(end - 1).get(index);
                if (toLeft.hasEdges()) {
                    for (MapEdge connection : toLeft.getEdges()) {
                        if (connection.dstX > boundary)
                            boundary = connection.dstX;
                    }
                    break;
                }
            }
            left = Math.min(left, boundary);

            index = path.getKey().x;
            boundary = map.get(end - 1).size() - 1; //Check for the furthest right it can go without intersecting another edge
            while (index < map.get(end - 1).size() - 1) {
                ++index;
                MapRoomNode toRight = map.get(end - 1).get(index);
                if (toRight.hasEdges()) {
                    for (MapEdge connection : toRight.getEdges()) {
                        if (connection.dstX < boundary)
                            boundary = connection.dstX;
                    }
                    break;
                }
            }
            right = Math.max(right, boundary);

            if (right >= left) {
                for (; left <= right; ++left) {
                    MapRoomNode possible = map.get(end).get(left);
                    Set<MapRoomNode> takable = new HashSet<>(path.getValue());
                    takable.add(possible);
                    temp.merge(possible, takable, (a, b)->{a.addAll(b); return a;});
                }
            }
        }
        return temp;
    }
    private static Map<MapRoomNode, Set<MapRoomNode>> scoreFollowing(int start) {
        List<MapRoomNode> starting = map.get(start);
        Map<MapRoomNode, Set<MapRoomNode>> scored = new HashMap<>();
        for (MapRoomNode n : starting) {
            scored.put(n, new HashSet<>());
        }

        int left, right, index;
        for (MapRoomNode n : starting) {
            left = Integer.MAX_VALUE;
            right = Integer.MIN_VALUE;
            for (MapEdge connection : n.getEdges()) { //Check the limits of the existing paths
                if (connection.dstX < left)
                    left = connection.dstX;
                if (connection.dstX > right)
                    right = connection.dstX;
            }
            index = n.x;
            int boundary = 0; //Check for the furthest left it can go without intersecting another edge
            while (index > 0) {
                --index;
                MapRoomNode toLeft = map.get(start).get(index);
                if (toLeft.hasEdges()) {
                    for (MapEdge connection : toLeft.getEdges()) {
                        if (connection.dstX > boundary)
                            boundary = connection.dstX;
                    }
                    break;
                }
            }
            left = Math.min(left, boundary);

            index = n.x;
            boundary = map.get(start).size() - 1; //Check for the furthest right it can go without intersecting another edge
            while (index < map.get(start).size() - 1) {
                ++index;
                MapRoomNode toRight = map.get(start).get(index);
                if (toRight.hasEdges()) {
                    for (MapEdge connection : toRight.getEdges()) {
                        if (connection.dstX < boundary)
                            boundary = connection.dstX;
                    }
                    break;
                }
            }
            right = Math.max(right, boundary);

            if (right >= left) {
                for (; left <= right; ++left) {
                    MapRoomNode possible = map.get(start + 1).get(left);
                    if (possible.hasEdges() && possible.room != null) {
                        scored.get(n).addAll(getConnected(possible));
                    }
                }
            }
        }
        return scored;
    }
    private static Set<MapRoomNode> getConnected(MapRoomNode n) {
        Set<MapRoomNode> connected = new HashSet<>();
        return getConnectedRecursive(n, connected);
    }
    private static Set<MapRoomNode> getConnectedRecursive(MapRoomNode n, Set<MapRoomNode> path) {
        path.add(n);
        for (MapEdge edge : n.getEdges()) {
            if (edge.dstY < map.size()) //boss edges are a No No
                getConnectedRecursive(map.get(edge.dstY).get(edge.dstX), path);
        }
        return path;
    }
    private static List<MapRoomNode> getExisting(int row) {
        List<MapRoomNode> existing = new ArrayList<>();
        for (MapRoomNode n : AbstractDungeon.map.get(row)) {
            if (n.getRoom() != null && n.hasEdges()) {
                existing.add(n);
            }
        }
        return existing;
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
