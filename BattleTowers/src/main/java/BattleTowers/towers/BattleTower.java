package BattleTowers.towers;

import BattleTowers.events.*;
import BattleTowers.events.GentlemanEvent;
import BattleTowers.interfaces.Weighted;
import BattleTowers.monsters.*;
import BattleTowers.monsters.CardboardGolem.CardboardGolem;
import BattleTowers.monsters.chess.queen.Queen;
import BattleTowers.monsters.executiveslime.ExecutiveSlime;
import BattleTowers.relics.*;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static BattleTowers.BattleTowers.makeUIPath;

public class BattleTower {
    private final TowerLayout layout;
    private final TowerContents content;

    protected String title; //probably won't be used

    public BattleTower(Random rng) {
        this.layout = Weighted.roll(layouts, rng).randomize(rng);
        this.content = contents.get(rng.random(contents.size() - 1)).copy();
        this.title = layout.title + " | " + content.title;
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

        layouts.add(new TowerLayout("Basic", 1)
                .addRow(NodeType.MONSTER)
                .addRow(NodeType.EVENT)
                .addRow(NodeType.ELITE)
                .addRow(NodeType.MONSTER, NodeType.MONSTER)
                .addRow(NodeType.REST, NodeType.SHOP).addAlternate(NodeType.SHOP, NodeType.REST)
                .connect(0).connect(1).connect(2).connect(3)
        );
        layouts.add(new TowerLayout("DoubleSplit", 1)
                .addRow(NodeType.MONSTER)
                .addRow(NodeType.EVENT, NodeType.MONSTER).addAlternate(NodeType.MONSTER, NodeType.EVENT).addAlternate(NodeType.MONSTER, NodeType.MONSTER)
                .addRow(NodeType.ELITE)
                .addRow(NodeType.EVENT).addAlternate(NodeType.EVENT).addAlternate(NodeType.MONSTER)
                .addRow(NodeType.REST, NodeType.SHOP).addAlternate(NodeType.SHOP, NodeType.REST)
                .connect(0).connect(1).connect(2).connect(3)
        );
        layouts.add(new TowerLayout("FatefulChoice", 0.6f)
                .addRow(NodeType.MONSTER, NodeType.MONSTER)
                .addRow(NodeType.EVENT, NodeType.EVENT).addAlternate(NodeType.MONSTER, NodeType.EVENT).addAlternate(NodeType.EVENT, NodeType.MONSTER) //event biased
                .addRow(NodeType.ELITE, NodeType.ELITE)
                .addRow(NodeType.MONSTER, NodeType.MONSTER).addAlternate(NodeType.MONSTER, NodeType.EVENT).addAlternate(NodeType.EVENT, NodeType.MONSTER) //monster biased
                .addRow(NodeType.REST, NodeType.SHOP).addAlternate(NodeType.SHOP, NodeType.REST).addAlternate(NodeType.REST, NodeType.REST)
                .connect(0, 0, 0).connect(0, 1, 1)
                .connect(1, 0, 0).connect(1, 1, 1)
                .connect(2, 0, 0).connect(2, 1, 1)
                .connect(3, 0, 0).connect(3, 1, 1)
        );
        layouts.add(new TowerLayout("TripleDouble", 1)
                .addRow(NodeType.MONSTER, NodeType.MONSTER, NodeType.MONSTER)
                .addRow(NodeType.EVENT).addAlternate(NodeType.EVENT).addAlternate(NodeType.REST)
                .addRow(NodeType.ELITE)
                .addRow(NodeType.MONSTER, NodeType.MONSTER)
                .addRow(NodeType.REST, NodeType.SHOP, NodeType.EVENT).addAlternate(NodeType.REST, NodeType.EVENT, NodeType.SHOP)
                .addAlternate(NodeType.SHOP, NodeType.EVENT, NodeType.REST).addAlternate(NodeType.SHOP, NodeType.REST, NodeType.EVENT)
                .addAlternate(NodeType.EVENT, NodeType.SHOP, NodeType.REST).addAlternate(NodeType.EVENT, NodeType.REST, NodeType.SHOP)
                .connect(0).connect(1).connect(2)
                .connect(3, 0, 0, 1).connect(3, 1, 1, 2)
        );
        layouts.add(new TowerLayout("DoubleTripleA", 0.5f)
                .addRow(NodeType.MONSTER)
                .addRow(NodeType.MONSTER)
                .addRow(NodeType.ELITE, NodeType.ELITE)
                .addRow(NodeType.EVENT)
                .addRow(NodeType.REST, NodeType.SHOP, NodeType.EVENT).addAlternate(NodeType.REST, NodeType.EVENT, NodeType.SHOP)
                .addAlternate(NodeType.SHOP, NodeType.EVENT, NodeType.REST).addAlternate(NodeType.SHOP, NodeType.REST, NodeType.EVENT)
                .addAlternate(NodeType.EVENT, NodeType.SHOP, NodeType.REST).addAlternate(NodeType.EVENT, NodeType.REST, NodeType.SHOP)
                .connect(0).connect(1).connect(2).connect(3)
        );
        layouts.add(new TowerLayout("DoubleTripleB", 0.5f)
                .addRow(NodeType.MONSTER)
                .addRow(NodeType.EVENT)
                .addRow(NodeType.ELITE, NodeType.ELITE)
                .addRow(NodeType.MONSTER)
                .addRow(NodeType.REST, NodeType.SHOP, NodeType.EVENT).addAlternate(NodeType.REST, NodeType.EVENT, NodeType.SHOP)
                .addAlternate(NodeType.SHOP, NodeType.EVENT, NodeType.REST).addAlternate(NodeType.SHOP, NodeType.REST, NodeType.EVENT)
                .addAlternate(NodeType.EVENT, NodeType.SHOP, NodeType.REST).addAlternate(NodeType.EVENT, NodeType.REST, NodeType.SHOP)
                .connect(0).connect(1).connect(2).connect(3)
        );
        layouts.add(new TowerLayout("Hex", 1)
                .addRow(NodeType.MONSTER, NodeType.MONSTER)
                .addRow(NodeType.EVENT, NodeType.SHOP).addAlternate(NodeType.SHOP, NodeType.EVENT).addAlternate(NodeType.EVENT, NodeType.EVENT)
                .addRow(NodeType.ELITE)
                .addRow(NodeType.EVENT, NodeType.MONSTER).addAlternate(NodeType.MONSTER, NodeType.EVENT).addAlternate(NodeType.MONSTER, NodeType.MONSTER)
                .addRow(NodeType.REST, NodeType.EVENT).addAlternate(NodeType.EVENT, NodeType.REST).addAlternate(NodeType.REST, NodeType.REST)
                .connect(0, 0, 0).connect(0, 1, 1)
                .connect(1).connect(2)
                .connect(3, 0, 0).connect(3, 1, 1)
        );


        layouts.add(new TowerLayout("Chaos", 0.02f)
                .addRow(NodeType.REST, NodeType.MONSTER, NodeType.EVENT).addAlternate(NodeType.MONSTER, NodeType.SHOP, NodeType.REST).addAlternate(NodeType.EVENT, NodeType.EVENT, NodeType.MONSTER).addAlternate(NodeType.MONSTER, NodeType.MONSTER, NodeType.MONSTER).addAlternate(NodeType.MONSTER, NodeType.REST, NodeType.EVENT).addAlternate(NodeType.EVENT, NodeType.ELITE, NodeType.EVENT)
                .addRow(NodeType.EVENT, NodeType.MONSTER).addAlternate(NodeType.MONSTER, NodeType.MONSTER).addAlternate(NodeType.EVENT, NodeType.EVENT).addAlternate(NodeType.MONSTER, NodeType.REST).addAlternate(NodeType.REST, NodeType.EVENT).addAlternate(NodeType.MONSTER, NodeType.SHOP)
                .addRow(NodeType.ELITE)
                .addRow(NodeType.EVENT, NodeType.MONSTER).addAlternate(NodeType.MONSTER, NodeType.EVENT).addAlternate(NodeType.MONSTER, NodeType.MONSTER).addAlternate(NodeType.EVENT, NodeType.EVENT).addAlternate(NodeType.REST, NodeType.MONSTER).addAlternate(NodeType.EVENT, NodeType.REST).addAlternate(NodeType.SHOP, NodeType.EVENT).addAlternate(NodeType.MONSTER, NodeType.ELITE).addAlternate(NodeType.ELITE, NodeType.EVENT)
                .addRow(NodeType.REST, NodeType.MONSTER).addAlternate(NodeType.MONSTER, NodeType.REST).addAlternate(NodeType.EVENT, NodeType.EVENT).addAlternate(NodeType.EVENT, NodeType.REST).addAlternate(NodeType.REST, NodeType.REST).addAlternate(NodeType.REST, NodeType.EVENT).addAlternate(NodeType.EVENT, NodeType.SHOP).addAlternate(NodeType.REST, NodeType.ELITE)
                .connect(0, 0, 0).connect(0, 1, 0, 1).connect(0, 2, 1)
                .connect(1).connect(2)
                .connect(3, 0, 0).connect(3, 1, 1)
        );
        layouts.add(new TowerLayout("Debug", 0)
                .addRow(NodeType.REST)
                .addRow(NodeType.SHOP)
                .addRow(NodeType.EVENT)
                .addRow(NodeType.MONSTER)
                .addRow(NodeType.ELITE)
                .connect(0).connect(1).connect(2).connect(3)
        );

        contents = new ArrayList<>();

        contents.add(new TowerContents("Default")
                .addNormalEncounter(Encounters.METAL_LOUSES)
                .addNormalEncounter(Encounters.ICE_AND_FIRE_SLIME)
                .addNormalEncounter(DoomedSoul.ID)
                .addNormalEncounter(Trenchcoat.ID)
                .addNormalEncounter(Encounters.MINOTAUR_GLADIATOR_AND_FRIEND)
                .addNormalEncounter(tneisnarT.ID)
                .addNormalEncounter(LouseHorde.ID)
                .addNormalEncounter(PrismGuardian.ID)
                .addNormalEncounter(Encounters.MAGUS_AND_ASSASSIN)
                .addNormalEncounter(Romeo.ID)
                .addNormalEncounter(Encounters.NINJA_LOUSES)
                .addNormalEncounter(Encounters.RAINBOW_LOUSES)
                .addEliteEncounter(Encounters.ELEMENTAL_SENTRIES, addReward(SentryOrb::new))
                .addEliteEncounter(VoodooDoll.ID, addReward(CursedDoll::new))
                .addEliteEncounter(GiantArm.ID, addReward(SweatyArmband::new))
                .addEliteEncounter(Gorgon.ID, addReward(GorgonHead::new))
                .addEliteEncounter(GigaSlime.ID, addReward(SlimeFilledFlask::new))
                .addEliteEncounter(ItozusTheWindwalker.ID, addReward(JadeIdol::new))
                .addEliteEncounter(ZastraszTheJusticar.ID, addReward(RubyFragment::new))
                .addBoss(CardboardGolem.ID, addReward(CardboardHeart::new))
                .addBoss(ExecutiveSlime.ID, addReward(BucketOfSlime::new))
                .addBoss(Dijinn.ID, addReward(DijinnLamp::new))
                .addBoss(AlphabetBoss.ID, (room)->{}) //This one's kinda fucked and would be a pain to separate from the enemy
                .addBoss(Queen.ID, randomRelic(AbstractRelic.RelicTier.RARE))
                .addBoss(Necrototem.ID, addReward(ScaryNail::new))
                .addEvent(OttoEvent.ID)
                .addEvent(BannerSageEvent.ID)
                .addEvent(EmeraldFlame.ID)
                .addEvent(GenieLampEvent.ID)
                .addEvent(VoidShrine.ID)
                .addEvent(RoarOfTheCrowd.ID)
                .addEvent(ArmorerEvent.ID)
                .addEvent(GentlemanEvent.ID)
        );
    }


    public static class TowerLayout implements Weighted {
        List<List<Node[]>> layout = new ArrayList<>();

        private final String title;
        private final float weight;
        public TowerLayout(String title, float weight) {
            this.title = title;
            this.weight = weight;
        }
        public TowerLayout(String title) {
            this(title, 1.0f);
        }

        public String getTitle() {
            return title;
        }

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
            TowerLayout singleResult = new TowerLayout(title);

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

        @Override
        public float getWeight() {
            return weight;
        }
    }

    public static class TowerContents {
        private final List<String> srcNormalEncounters = new ArrayList<>(), normalEncounters = new ArrayList<>();
        private final List<String> srcEliteEncounters = new ArrayList<>(), eliteEncounters = new ArrayList<>();
        private final List<String> srcEvents = new ArrayList<>(), events = new ArrayList<>();
        private final List<BossInfo> srcBosses = new ArrayList<>(), bosses = new ArrayList<>();

        private final Map<String, Consumer<AbstractRoom>> rewards = new HashMap<>();

        private final String title;

        public TowerContents(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public TowerContents addNormalEncounter(String id) {
            srcNormalEncounters.add(id);
            normalEncounters.add(id);
            return this;
        }
        public TowerContents addEliteEncounter(String id) {
            srcEliteEncounters.add(id);
            eliteEncounters.add(id);
            return this;
        }
        public TowerContents addNormalEncounter(String id, Consumer<AbstractRoom> addRewards) {
            srcNormalEncounters.add(id);
            normalEncounters.add(id);
            rewards.put(id, addRewards);
            return this;
        }
        public TowerContents addEliteEncounter(String id, Consumer<AbstractRoom> addRewards) {
            srcEliteEncounters.add(id);
            eliteEncounters.add(id);
            rewards.put(id, addRewards);
            return this;
        }
        public TowerContents addEvent(String key) {
            srcEvents.add(key);
            events.add(key);
            return this;
        }
        public TowerContents addBoss(String id) {
            return addBoss(id, randomRelic(AbstractRelic.RelicTier.RARE));
        }
        public TowerContents addBoss(String id, Consumer<AbstractRoom> addRewards) {
            return addBoss(id, makeUIPath("OldBossIcon.png"), makeUIPath("OldBossIconOutline.png"), addRewards);
        }
        public TowerContents addBoss(String id, String mapIcon, String mapIconOutline, Consumer<AbstractRoom> addRewards) {
            BossInfo info = new BossInfo(id, mapIcon, mapIconOutline);
            srcBosses.add(info);
            bosses.add(info);
            rewards.put(id, addRewards);
            return this;
        }

        public TowerContents copy() {
            TowerContents copy = new TowerContents(title);
            copy.srcNormalEncounters.addAll(srcNormalEncounters);
            copy.normalEncounters.addAll(srcNormalEncounters);
            copy.srcEliteEncounters.addAll(srcEliteEncounters);
            copy.eliteEncounters.addAll(srcEliteEncounters);
            copy.srcEvents.addAll(srcEvents);
            copy.events.addAll(srcEvents);
            copy.srcBosses.addAll(srcBosses);
            copy.bosses.addAll(srcBosses);
            copy.rewards.putAll(rewards);
            return copy;
        }

        public String getNormalEncounter(Random rng) {
            if (normalEncounters.isEmpty())
                normalEncounters.addAll(srcNormalEncounters);
            return normalEncounters.remove(rng.random(normalEncounters.size() - 1));
        }

        public String getEliteEncounter(Random rng) {
            if (eliteEncounters.isEmpty())
                eliteEncounters.addAll(srcEliteEncounters);
            return eliteEncounters.remove(rng.random(eliteEncounters.size() - 1));
        }

        public String getEvent(Random rng) {
            if (events.isEmpty())
                events.addAll(srcEvents);
            return events.remove(rng.random(events.size() - 1));
        }

        public BossInfo getBoss(Random rng) {
            if (bosses.isEmpty())
                bosses.addAll(srcBosses);
            return bosses.remove(rng.random(bosses.size() - 1));
        }

        public void addReward(AbstractRoom room, String encounterKey) {
            Consumer<AbstractRoom> addReward = rewards.get(encounterKey);
            if (addReward != null) {
                addReward.accept(room);
            }
        }
    }

    private static Consumer<AbstractRoom> addReward(Supplier<AbstractRelic> relic) {
        return (room)->room.rewards.add(new RewardItem(relic.get()));
    }
    private static Consumer<AbstractRoom> randomRelic(AbstractRelic.RelicTier tier) {
        return (room)->room.addRelicToRewards(tier);
    }

    public static class BossInfo {
        public final String id;
        private final String bossMap;
        private final String bossMapOutline;

        private BossInfo(String id, String mapIcon, String mapIconOutline) {
            this.id = id;
            this.bossMap = mapIcon;
            this.bossMapOutline = mapIconOutline;
        }

        public Texture loadBossIcon() {
            return ImageMaster.loadImage(this.bossMap);
        }

        public Texture loadBossIconOutline() {
            return ImageMaster.loadImage(this.bossMapOutline);
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
        SHOP,
        BOSS
    }
}
