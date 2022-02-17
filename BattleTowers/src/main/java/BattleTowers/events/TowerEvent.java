package BattleTowers.events;

import BattleTowers.events.phases.*;
import BattleTowers.minimap.Minimap;
import BattleTowers.patches.map.TowerGeneration;
import BattleTowers.room.BattleTowerRoom;
import BattleTowers.towers.BattleTower;
import basemod.Pair;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.WhiteBeast;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.vfx.GameSavedEffect;

import java.util.ArrayList;
import java.util.List;

import static BattleTowers.BattleTowers.*;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.*;

public class TowerEvent extends PhasedEvent {
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(makeID("TowerEnter"));
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;
    private static final String imgUrl = "images/events/theNest.jpg";

    private static final String TOWER_CHOICE_PHASE = "TOWER_CHOICE";
    private static final String MAP_PHASE = "MAP";
    private static final String CHEST_PHASE = "CHEST";

    private static final int NUM_OPTIONS = 1; //it seems like it'll probably just be 1.
    public int chosenTower = -1; //For saving?
    public List<Pair<Integer, Integer>> pathTaken = new ArrayList<>();
    private boolean finishedRoom = false; //For loading

    private BattleTower tower;
    private MapHandler mapHandler;

    public TowerEvent(BattleTowerRoom room) {
        super(title, imgUrl);

        mapHandler = new MapHandler(this);

        TextPhase choice = new TextPhase(DESCRIPTIONS[0]);

        for (int i = 0; i < NUM_OPTIONS; ++i) {
            BattleTower t = new BattleTower(room.towerRng);
            choice.addOption(OPTIONS[0], (index)->{
                this.tower = t;
                this.mapHandler.setTower(t, room.towerRng);
                this.transitionKey(MAP_PHASE);
                if (chosenTower == -1) {
                    chosenTower = index;
                    save();
                }
            });
        }
        if (TowerGeneration.fullRowMode) {
            choice.addOption(OPTIONS[1], (index) -> {
                this.transitionKey(CHEST_PHASE);
            });
        }
        registerPhase(TOWER_CHOICE_PHASE, choice);
        registerPhase(MAP_PHASE, new InteractionPhase(mapHandler));
        registerPhase(CHEST_PHASE, new InteractionPhase(new ChestHandler()));

        transitionKey(TOWER_CHOICE_PHASE);
    }

    public void loadSave(Integer towerIndex, List<Pair<Integer, Integer>> pathTaken, boolean finishedRoom) {
        if (towerIndex != null && chosenTower == -1) {
            this.chosenTower = towerIndex;
            this.pathTaken.clear();
            this.pathTaken.addAll(pathTaken);
            this.finishedRoom = finishedRoom;
            logger.info("Loaded Battle Tower save:");
            logger.info(" - Tower Chosen: " + chosenTower);
            logger.info(" - Path Taken: " + pathTakenString());
            logger.info(" - Current room complete: " + finishedRoom);
        }
    }

    @Override
    public void onEnterRoom() {
        super.onEnterRoom();
        //This event isn't newly created upon entering the room, so this has to be done
        this.imageEventText.loadImage(imgUrl);

        if (chosenTower != -1) {
            EventPhase choice = getPhase(TOWER_CHOICE_PHASE);
            if (choice instanceof ImageEventPhase) {
                ((ImageEventPhase) choice).optionChosen(chosenTower);
                waitTimer = 0;
                started = true;

                mapHandler.map.loadPathTaken(pathTaken);

                if (mapHandler.map.isDone) {
                    if (mapHandler.map.current != null) {
                        mapHandler.map.current.taken = true;
                        //If post-combat, load at combat reward screen
                        mapHandler.transitionLoad(mapHandler.map.current, finishedRoom);
                    }
                }
            }
        }
    }

    public String pathTakenString() {
        if (pathTaken.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (Pair<Integer, Integer> pos : pathTaken) {
            sb.append("(").append(pos.getKey()).append(", ").append(pos.getValue()).append(")->");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public void dropReward(AbstractRoom room) {
        //Could also handle elite/boss relic rewards using this.
        if (currentPhase instanceof CombatPhase) {
            CombatPhase combatPhase = (CombatPhase) currentPhase;

            tower.getContents().addReward(room, combatPhase.getEncounterKey());

            if (combatPhase.isBoss()) {
                room.addGoldToRewards(AbstractDungeon.ascensionLevel >= 13 ? 100 : 150);
                if (!player.hasRelic(WhiteBeast.ID))
                    room.rewards.add(new RewardItem(AbstractDungeon.returnRandomPotion(AbstractPotion.PotionRarity.RARE, false)));
            }
        }
    }

    private static class MapHandler implements InteractionPhase.InteractionHandler {
        private final TowerEvent event;
        private final Minimap map;

        private float fadeTimer = 0, fadeTime = 0;
        private boolean fadingOut = false;
        //AbstractDungeon can handle fading back in with no issues.
        //This handler won't be updating at that point anyways.


        public MapHandler(TowerEvent event) {
            this.event = event;
            map = new Minimap();
        }

        @Override
        public void begin(PhasedEvent event) {
            map.show();
        }

        @Override
        public void update() {
            map.update();
            if (map.isDone && !fadingOut) {
                fadingOut = true;
                fadeTime = fadeTimer = Settings.FAST_MODE ? 0.25f : 0.7f;
            }

            if (fadingOut) { //out
                fadeTimer -= Gdx.graphics.getDeltaTime();
                AbstractDungeon.fadeColor.a = Interpolation.fade.apply(1.0F, 0.0F, fadeTimer / fadeTime);
                if (fadeTimer <= 0.0F) {
                    fadeTimer = 0.0F;
                    fadingOut = false;
                    AbstractDungeon.fadeColor.a = 1.0F;
                    if (map.mapCircleEffect != null) {
                        map.mapCircleEffect.isDone = true;
                        map.mapCircleEffect = null;
                    }
                    if (map.current != null) {
                        map.current.taken = true;
                        this.transition(map.current);
                    }
                    //else No more nodes. What the Heck. Why is the map open.
                }
            }
        }

        protected void transition(Minimap.MinimapNode target) {
            map.hideInstantly();
            AbstractDungeon.fadeIn();
            topPanel.unhoverHitboxes();
            combatRewardScreen.clear();
            gridSelectScreen.upgradePreviewCard = null;
            previousScreen = null;

            if (RestRoom.lastFireSoundId != 0L) {
                CardCrawlGame.sound.fadeOut("REST_FIRE_WET", RestRoom.lastFireSoundId);
            }
            if (player.stance != null) {
                player.stance.stopIdleSfx();
            }
            dynamicBanner.hide();
            player.resetControllerValues();
            resetPlayer();
            event.noCardsInRewards = false;

            event.pathTaken.add(new Pair<>(target.mapX, target.mapY));

            event.save();

            //multiply by value between -1 and 1 based on tower position
            double mod = (((((target.mapX + 3) * 467) + ((target.mapY + 7) * 311)) % 100) / 50.0f) - 1;
            logger.info("SeedMod: " + mod);
            long seed = Math.round((Settings.seed + (long)floorNum) * (mod));
            logger.info("Floor seed: " + seed);
            monsterHpRng = new Random(seed);
            aiRng = new Random(seed);
            shuffleRng = new Random(seed);
            cardRandomRng = new Random(seed);
            miscRng = new Random(seed);

            if (!actionManager.actions.isEmpty()) {
                logger.info("[WARNING] Action Manager was NOT clear! Clearing");
                actionManager.clear();
            }

            AbstractRoom dummy = getDummyRoom(target);
            if (dummy != null) {
                for (AbstractRelic r : player.relics) {
                    r.onEnterRoom(dummy);
                }
                for (AbstractRelic r : player.relics) {
                    r.justEnteredRoom(dummy);
                }

                AbstractDungeon.scene.nextRoom(dummy); //NOTE: Maybe not necessary if we do some custom stuff for the combat scene
            }

            //Can do rich presence or something here if you want
            event.transitionPhase(getPhase(target));
        }
        protected void transitionLoad(Minimap.MinimapNode current, boolean isComplete) {
            map.hideInstantly();
            topPanel.unhoverHitboxes();
            //combatRewardScreen.clear();
            gridSelectScreen.upgradePreviewCard = null;
            previousScreen = null;

            if (RestRoom.lastFireSoundId != 0L) {
                CardCrawlGame.sound.fadeOut("REST_FIRE_WET", RestRoom.lastFireSoundId);
            }
            if (player.stance != null) {
                player.stance.stopIdleSfx();
            }
            dynamicBanner.hide();
            player.resetControllerValues();
            resetPlayer();

            //multiply by value between -1 and 1 based on tower position
            double mod = (((((current.mapX + 3) * 467) + ((current.mapY + 7) * 311)) % 100) / 50.0f) - 1;
            logger.info("SeedMod: " + mod);
            long seed = Math.round((Settings.seed + (long)floorNum) * (mod));
            logger.info("Floor seed: " + seed);
            monsterHpRng = new Random(seed);
            aiRng = new Random(seed);
            shuffleRng = new Random(seed);
            cardRandomRng = new Random(seed);
            miscRng = new Random(seed);

            if (!actionManager.actions.isEmpty()) {
                logger.info("[WARNING] Action Manager was NOT clear! Clearing");
                actionManager.clear();
            }

            AbstractRoom dummy = getDummyRoom(current);
            if (dummy != null) {
                if (!isComplete) {
                    for (AbstractRelic r : player.relics) {
                        r.onEnterRoom(dummy);
                    }
                    for (AbstractRelic r : player.relics) {
                        r.justEnteredRoom(dummy);
                    }
                }

                AbstractDungeon.scene.nextRoom(dummy); //NOTE: Maybe not necessary if we do some custom stuff for the combat scene
            }

            //Can do rich presence or something here if you want
            EventPhase next = getPhase(current);
            if (isComplete && next instanceof CombatPhase) {
                ((CombatPhase) next).completed();
            }
            event.transitionPhase(next);
        }

        private EventPhase getPhase(Minimap.MinimapNode target) {
            Object followup = getFollowup(target);
            switch (target.getType()) {
                case SHOP:
                    return new ShopPhase().setNextKey(followup);
                case EVENT:
                    return new WrappedEventPhase(target.getKey()).setNextKey(followup);
                case REST:
                    return new MiniRestPhase().setNextKey(followup);
                case MONSTER:
                    return new CombatPhase(target.getKey(), true, true).setNextKey(followup);
                case ELITE:
                    return new CombatPhase(target.getKey(), true, true).setType(AbstractMonster.EnemyType.ELITE).setNextKey(followup);
                case BOSS:
                    return new CombatPhase(target.getKey(), true, true).setType(AbstractMonster.EnemyType.BOSS).setNextKey(followup);
            }
            return null;
        }
        private Object getFollowup(Minimap.MinimapNode from) {
            if (from.hasEdges()) {
                return MAP_PHASE;
            }
            return null;
        }

        private AbstractRoom getDummyRoom(Minimap.MinimapNode target) {
            switch (target.getType()) { //most rooms expect to be instantiated with no actual "setup" done on them yet
                case SHOP:
                    return new ShopRoom();
                case EVENT:
                    return new EventRoom();
                case REST:
                    return new RestRoom();
                case MONSTER:
                    return new MonsterRoom();
                case ELITE:
                    return new MonsterRoomElite();
                default:
                    return null;
            }
        }

        @Override
        public void render(SpriteBatch sb) {
            map.render(sb);
        }

        public void setTower(BattleTower t, Random towerRng) {
            map.generate(t, towerRng);
        }
    }

    private static class ChestHandler implements InteractionPhase.InteractionHandler {
        private final float fadeTime = Settings.FAST_MODE ? 0.2f : 0.6f;
        private float fadeTimer = fadeTime;

        @Override
        public void update() {
            if (fadeTimer > 0) {
                fadeTimer -= Gdx.graphics.getDeltaTime();
                AbstractDungeon.fadeColor.a = Interpolation.fade.apply(1.0F, 0.0F, fadeTimer / fadeTime);
                if (fadeTimer <= 0.0F) {
                    fadeTimer = 0.0F;
                    AbstractDungeon.fadeColor.a = 1.0F;
                    this.goToTreasureRoom();
                }
            }
        }

        private void goToTreasureRoom() {
            logger.info("Going to treasure room");
            GenericEventDialog.hide();
            AbstractDungeon.rs = AbstractDungeon.RenderScene.NORMAL;
            AbstractRoom currentRoom = AbstractDungeon.getCurrRoom();
            AbstractRoom newRoom = new TreasureRoom();
            newRoom.setMapSymbol(currentRoom.getMapSymbol());
            newRoom.setMapImg(currentRoom.getMapImg(), currentRoom.getMapImgOutline());
            AbstractDungeon.getCurrMapNode().room = newRoom;
            newRoom.onPlayerEntry();
            AbstractDungeon.fadeIn();
        }
    }

    /*
        Save process: Create save info that holds current data, then saves save info.
            - 5 save types. 2 are neow (one endless), 1 is post-boss, 1 is post-battle
            - The one that will be used is the "entering room" state. Post-battle (room complete) will be saved upon finishing the boss.
            - Saving will save nextRoom if it's not null, and the current room otherwise. Works fine.
            - Patch in before line 345 of SaveFile to save current room information if it implements an interface.

        Load process:
            - Creates an empty room as the current room and performs a transition into the "next room" which is the saved room.
            - Transition process is different depending on if room is complete or not.
            - Treating room as incomplete, have to adjust process to give this event the necessary information to load in the correct position.
     */

    public void save() {
        if (!Settings.isDemo) {
            SaveFile saveFile = new SaveFile(SaveFile.SaveType.ENTER_ROOM);
            //for now, no end-of-tower save.
            //If the boss has a reward, end-of-tower save can probably be handled by the normal post-combat reward save process.
            SaveAndContinue.save(saveFile);
            AbstractDungeon.effectList.add(new GameSavedEffect());
        }
    }
}
