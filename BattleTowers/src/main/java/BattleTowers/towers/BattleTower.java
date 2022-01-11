package BattleTowers.towers;

import com.megacrit.cardcrawl.events.shrines.PurificationShrine;
import com.megacrit.cardcrawl.events.shrines.Transmogrifier;
import com.megacrit.cardcrawl.events.shrines.UpgradeShrine;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.random.Random;

import java.util.ArrayList;
import java.util.List;

public class BattleTower {
    private final TowerLayout layout;
    private final TowerContents content;

    protected String title; //probably won't be used

    public BattleTower(Random rng) {
        this.layout = layouts.get(rng.random(layouts.size() - 1)).randomize(rng);
        this.content = contents.get(rng.random(contents.size() - 1)).copy();
        this.title = " Random int: " + rng.random(0, 1000);
    }

    public String getTitle() {
        return title;
    }

    public TowerLayout getLayout() {
        return layout;
    }

    public TowerContents getContents() {
        return content;
    }



    private static final List<TowerLayout> layouts;
    private static final List<TowerContents> contents;
    static {
        layouts = new ArrayList<>();

        layouts.add(new TowerLayout()
                .addRow(NodeType.MONSTER)
                .addRow(NodeType.EVENT)
                .addRow(NodeType.ELITE)
                .addRow(NodeType.MONSTER, NodeType.MONSTER)
                .addRow(NodeType.REST, NodeType.SHOP).addAlternate(NodeType.SHOP, NodeType.REST)
                .connect(0).connect(1).connect(2).connect(3)
        );

        contents = new ArrayList<>();

        contents.add(new TowerContents()
                .addNormalEncounter(MonsterHelper.CULTIST_ENC)
                .addNormalEncounter(MonsterHelper.JAW_WORM_ENC)
                .addNormalEncounter(MonsterHelper.THREE_DARKLINGS_ENC)
                .addEliteEncounter(MonsterHelper.GREMLIN_NOB_ENC)
                .addEliteEncounter(MonsterHelper.LAGAVULIN_ENC)
                .addEvent(UpgradeShrine.ID)
                .addEvent(Transmogrifier.ID)
                .addEvent(PurificationShrine.ID)
        );
    }


    public static class TowerLayout {
        List<List<Node[]>> layout = new ArrayList<>();

        public TowerLayout addRow(NodeType... nodes) {
            List<Node[]> row = new ArrayList<>();
            row.add(fromTypes(nodes));
            layout.add(row);
            return this;
        }
        public TowerLayout addAlternate(NodeType... nodes) {
            layout.get(layout.size() - 1).add(fromTypes(nodes));
            return this;
        }
        private Node[] fromTypes(NodeType[] types) {
            Node[] row = new Node[types.length];
            for (int i = 0; i < types.length; ++i)
                row[i] = new Node(types[i]);
            return row;
        }

        public TowerLayout connect(int startRow, int startIndex, int... endIndex) {
            for (Node[] row : layout.get(startRow)) {
                row[startIndex].connect(endIndex);
            }
            return this;
        }
        public TowerLayout connect(int row) { //connects all to all
            int[] all = new int[layout.get(row + 1).get(0).length];
            for (int i = 0; i < all.length; ++i)
                all[i] = i;

            for (Node[] possibility : layout.get(row)) {
                for (Node n : possibility) {
                    n.connect(all);
                }
            }
            return this;
        }

        public TowerLayout randomize(Random rng) {
            TowerLayout singleResult = new TowerLayout();

            List<Node[]> row;
            for (List<Node[]> possible : layout) {
                row = new ArrayList<>();
                row.add(possible.get(rng.random(possible.size() - 1)));
                singleResult.layout.add(row);
            }

            return singleResult;
        }

        public List<List<Node[]>> getRows() {
            return layout;
        }
    }

    public static class TowerContents {
        private final List<String> normalEncounters = new ArrayList<>();
        private final List<String> eliteEncounters = new ArrayList<>();
        private final List<String> events = new ArrayList<>();

        public TowerContents() {

        }

        public TowerContents addNormalEncounter(String key) {
            normalEncounters.add(key);
            return this;
        }
        public TowerContents addEliteEncounter(String key) {
            eliteEncounters.add(key);
            return this;
        }
        public TowerContents addEvent(String key) {
            events.add(key);
            return this;
        }

        public TowerContents copy() {
            TowerContents copy = new TowerContents();
            copy.normalEncounters.addAll(normalEncounters);
            copy.eliteEncounters.addAll(eliteEncounters);
            copy.events.addAll(events);
            return copy;
        }

        public String getNormalEncounter(Random towerRng) {
            return normalEncounters.remove(towerRng.random(normalEncounters.size() - 1));
        }

        public String getEliteEncounter(Random towerRng) {
            return eliteEncounters.remove(towerRng.random(eliteEncounters.size() - 1));
        }

        public String getEvent(Random towerRng) {
            return events.remove(towerRng.random(events.size() - 1));
        }
    }

    public static class Node {
        public NodeType type;
        public List<Integer> connections;

        public Node(NodeType type) {
            this.type = type;
            connections = new ArrayList<>();
        }
        public void connect(int[] indices) {
            for (int i : indices) {
                connections.add(i);
            }
        }
    }

    public enum NodeType {
        MONSTER,
        ELITE,
        EVENT,
        REST,
        SHOP
    }
}
