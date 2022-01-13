package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.events.phases.TextPhase;
import BattleTowers.relics.Torch;
import BattleTowers.util.UC;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
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
        relics.addAll(UC.p().relics);
        Collections.shuffle(relics, new Random(AbstractDungeon.miscRng.randomLong()));
        this.relicChoice = relics.get(0);

        //set up event
        registerPhase(0, new TextPhase(DESCRIPTIONS[0] + DESCRIPTIONS[1] + DESCRIPTIONS[2]).addOption(OPTIONS[0] + FontHelper.colorString(relicChoice.name, "y") + OPTIONS[1], (i)->transitionKey("Relic")).addOption(OPTIONS[2], (i)->transitionKey("Card")));
        registerPhase("AfterRelic", new TextPhase(DESCRIPTIONS[4] + FontHelper.colorString(relicChoice.name, "y") + DESCRIPTIONS[5]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("AfterCard", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));

        registerPhase("Relic", new TextPhase(DESCRIPTIONS[7]).addOption(OPTIONS[5] + FontHelper.colorString(relicChoice.name, "y"), (i)->{
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
                    AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2), (Settings.HEIGHT / 2), new Torch(c));
                    AbstractDungeon.gridSelectScreen.selectedCards.clear();
                    AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(c, (Settings.WIDTH / 2), (Settings.HEIGHT / 2)));
                    AbstractDungeon.player.masterDeck.removeCard(c);
                    transitionKey("AfterCard");
                }
            }
        }.addOption(OPTIONS[3], (i)->{
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            AbstractDungeon.gridSelectScreen.open(AbstractDungeon.player.masterDeck.getPurgeableCards(), 1, OPTIONS[2], false, false, false, true);
        }));

        transitionKey(0);
    }

    public void onEnterRoom() {
        if (Settings.AMBIANCE_ON)
            CardCrawlGame.sound.play("EVENT_GOOP");
    }
}
