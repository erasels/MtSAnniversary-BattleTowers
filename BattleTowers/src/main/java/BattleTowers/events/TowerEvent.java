package BattleTowers.events;

import BattleTowers.events.phases.*;
import BattleTowers.minimap.Minimap;
import BattleTowers.room.BattleTowerRoom;
import BattleTowers.towers.BattleTower;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.*;

import static BattleTowers.BattleTowers.*;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.*;

public class TowerEvent extends PhasedEvent {
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(makeID("TowerEnter"));
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;
    private static final String imgUrl = "images/events/theNest.jpg";

    private static final int NUM_OPTIONS = 3; //it seems like it'll probably just be 1.
    public int chosenIndex = -1; //For saving?

    private MapHandler mapHandler;

    public TowerEvent(BattleTowerRoom room) {
        super(title, imgUrl);

        TextPhase initial = new TextPhase(DESCRIPTIONS[0]);

        for (int i = 0; i < NUM_OPTIONS; ++i) {
            BattleTower t = new BattleTower(room.towerRng);
            initial.addOption(OPTIONS[0] + t.getTitle() + "!", (index)->{
                chosenIndex = index;
                this.mapHandler.setTower(t, room.towerRng);
                this.transitionKey("map");
            });
        }
        registerPhase("enter", initial);
        registerPhase("map", new InteractionPhase(mapHandler = new MapHandler(this)));

        transitionKey("enter");
    }

    @Override
    public void onEnterRoom() {
        super.onEnterRoom();
        //This event isn't newly created upon entering the room, so this has to be done
        this.imageEventText.loadImage(imgUrl);
    }

    private static class MapHandler implements InteractionPhase.InteractionHandler {
        private final TowerEvent event;
        private final Minimap map;

        private BattleTower tower;

        private float fadeTimer = 0;
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
                fadeTimer = Settings.FAST_MODE ? 0.1f : 0.8f;
            }

            if (fadingOut) { //out
                fadeTimer -= Gdx.graphics.getDeltaTime();
                AbstractDungeon.fadeColor.a = Interpolation.fade.apply(1.0F, 0.0F, fadeTimer / 0.8F);
                if (fadeTimer <= 0.0F) {
                    fadeTimer = 0.0F;
                    fadingOut = false;
                    AbstractDungeon.fadeColor.a = 1.0F;
                    if (map.current != null) {
                        this.transition(map.current);
                    }
                    //else No more nodes. What the Heck. Why is the map open.
                }
            }
        }

        private void transition(Minimap.MinimapNode target) {
            map.hideInstantly();
            AbstractDungeon.fadeIn();
            topPanel.unhoverHitboxes();
            combatRewardScreen.clear();
            gridSelectScreen.upgradePreviewCard = null;

            if (RestRoom.lastFireSoundId != 0L) {
                CardCrawlGame.sound.fadeOut("REST_FIRE_WET", RestRoom.lastFireSoundId);
            }
            if (player.stance != null) {
                player.stance.stopIdleSfx();
            }
            gridSelectScreen.upgradePreviewCard = null;
            dynamicBanner.hide();
            player.resetControllerValues();
            resetPlayer();


            //SaveHelper.saveIfAppropriate(SaveFile.SaveType.ENTER_ROOM);

            //multiply by value between -1 and 1 based on tower position
            long seed = Math.round((Settings.seed + (long)floorNum) * (0.5));
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

        private EventPhase getPhase(Minimap.MinimapNode target) {
            switch (target.getType()) {
                case SHOP:
                    //return new ShopRoom();
                case EVENT:
                    return new WrappedEventPhase(target.getKey()).setNextKey("map");
                case REST:
                    //return new RestRoom();
                case MONSTER:
                case ELITE:
                    return new CombatPhase(target.getKey(), true).setNextKey("map");
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
            this.tower = t;
            map.generate(tower, towerRng);
        }
    }
}
