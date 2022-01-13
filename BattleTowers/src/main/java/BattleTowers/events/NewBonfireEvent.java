package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.events.phases.TextPhase;
import BattleTowers.relics.Torch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

import static BattleTowers.BattleTowers.makeID;

public class NewBonfireEvent extends PhasedEvent {
    public static final String ID = makeID("NewBonfireEvent");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    private AbstractRelic relicChoice;

    public NewBonfireEvent() {
        super(title, BattleTowers.makeImagePath("events/bonfire.png"));

        ArrayList<AbstractRelic> relics = new ArrayList<>();
        relics.addAll(AbstractDungeon.player.relics);
        Collections.shuffle(relics, new Random(AbstractDungeon.miscRng.randomLong()));
        this.relicChoice = relics.get(0);

        String yellowRelicName = "#y" + Arrays.stream(relicChoice.name.split(" ")).collect(Collectors.joining(" #y"));

        //set up event
        registerPhase(0, new TextPhase(DESCRIPTIONS[0] + DESCRIPTIONS[1] + DESCRIPTIONS[2]).addOption(OPTIONS[0] + relicChoice.name + OPTIONS[1], (i)->transitionKey("Relic")).addOption(OPTIONS[2], (i)->transitionKey("Card")));
        registerPhase("Curse", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Basic", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Common", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Special", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Uncommon", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Rare", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("AfterRelic", new TextPhase(DESCRIPTIONS[4] + yellowRelicName + DESCRIPTIONS[5]).addOption(OPTIONS[4], (t)->this.openMap()));

        registerPhase("Relic", new TextPhase(DESCRIPTIONS[7]).addOption(OPTIONS[5] + yellowRelicName, (i)->{
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            AbstractDungeon.player.loseRelic(relicChoice.relicId);
            AbstractDungeon.getCurrRoom().baseRareCardChance = 1000; //Always give rares
            AbstractDungeon.combatRewardScreen.open(DESCRIPTIONS[8]);
            transitionKey("AfterRelic");
        }));

        registerPhase("Card", new TextPhase(DESCRIPTIONS[3])
        {
            @Override
            public void update() {
                if (!AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
                    AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.remove(0);

                    switch(c.rarity) {
                        case CURSE:
                            transitionKey("Curse");
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2), (Settings.HEIGHT / 2), new Torch(-1));
                            break;
                        case BASIC:
                            transitionKey("Basic");
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2), (Settings.HEIGHT / 2), new Torch(-1));
                            break;
                        case SPECIAL:
                            transitionKey("Special");
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2), (Settings.HEIGHT / 2), new Torch(3));
                            break;
                        case UNCOMMON:
                            transitionKey("Uncommon");
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2), (Settings.HEIGHT / 2), new Torch(2));
                            break;
                        case RARE:
                            transitionKey("Rare");
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2), (Settings.HEIGHT / 2), new Torch(3));
                            break;
                        default:
                            transitionKey("Common");
                            AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2), (Settings.HEIGHT / 2), new Torch(1));
                            break;
                    }
                    AbstractDungeon.gridSelectScreen.selectedCards.clear();
                    AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(c, (Settings.WIDTH / 2), (Settings.HEIGHT / 2)));
                    AbstractDungeon.player.masterDeck.removeCard(c);
                }
            }
        }.addOption(OPTIONS[3], (i)->{
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            AbstractDungeon.gridSelectScreen.open(AbstractDungeon.player.masterDeck.getPurgeableCards(), 1, OPTIONS[2], false, false, false, true);
        }));

        transitionKey(0);
    }
}
