package BattleTowers.events;

import BattleTowers.events.phases.TextPhase;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.EventStrings;

import static BattleTowers.BattleTowers.makeID;

public class CoolExampleEvent extends PhasedEvent {
    public static final String ID = makeID("Example");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    public CoolExampleEvent() {
        super(title, "images/events/theNest.jpg");

        //set up event
        registerPhase(0, new TextPhase(DESCRIPTIONS[0]).addOption(OPTIONS[0], ()->transitionKey("Brazil")).addOption(OPTIONS[1], ()->transitionKey("Japan")));
        registerPhase("Brazil", new TextPhase(DESCRIPTIONS[1]).addOption(OPTIONS[2], ()->transitionKey("Antarctica")));
        registerPhase("Japan", new TextPhase(DESCRIPTIONS[2]).addOption(OPTIONS[0], ()->transitionKey("Brazil")).addOption(OPTIONS[3], ()->transitionKey("You can also use numbers")));
        registerPhase("Antarctica", new TextPhase(DESCRIPTIONS[3]).addOption(OPTIONS[3], ()->transitionKey("You can also use numbers")));
        registerPhase("You can also use numbers", new TextPhase(DESCRIPTIONS[4]).addOption(OPTIONS[4], this::openMap));

        transitionKey(0); //starting point
    }
}
