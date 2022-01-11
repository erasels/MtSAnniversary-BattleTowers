package BattleTowers.events;

import BattleTowers.events.phases.CombatPhase;
import BattleTowers.events.phases.TextPhase;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static BattleTowers.BattleTowers.makeID;

public class NewBonfireEvent extends PhasedEvent {
    public static final String ID = makeID("NewBonfireEvent");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    private AbstractRelic relicChoice;

    public NewBonfireEvent() {
        super(title, "images/events/theNest.jpg");

        ArrayList<AbstractRelic> relics = new ArrayList<>();
        relics.addAll(AbstractDungeon.player.relics);
        Collections.shuffle(relics, new Random(AbstractDungeon.miscRng.randomLong()));
        this.relicChoice = relics.get(0);

        //set up event
        registerPhase(0, new TextPhase(DESCRIPTIONS[0] + DESCRIPTIONS[1] + DESCRIPTIONS[2]).addOption(OPTIONS[0] + relicChoice.name + OPTIONS[1], (i)->transitionKey("Relic")).addOption(OPTIONS[2], (i)->transitionKey("Card")));
        registerPhase("Brazil", new TextPhase(DESCRIPTIONS[1]).addOption(OPTIONS[2], (i)->transitionKey("Antarctica")).addOption(OPTIONS[5], (i)->transitionKey(1)));
        registerPhase("Japan", new TextPhase(DESCRIPTIONS[2]).addOption(OPTIONS[0], (i)->transitionKey("Brazil")).addOption(OPTIONS[3], (i)->transitionKey("You can also use numbers")));
        registerPhase("Antarctica", new TextPhase(DESCRIPTIONS[3]).addOption(OPTIONS[3], (i)->transitionKey("You can also use numbers")));
        registerPhase("You can also use numbers", new TextPhase(DESCRIPTIONS[4]).addOption(OPTIONS[4], (t)->this.openMap()));

        registerPhase(1, new CombatPhase(MonsterHelper.CULTIST_ENC, true).setNextKey("Japan"));

        transitionKey(0); //starting point
    }
}
