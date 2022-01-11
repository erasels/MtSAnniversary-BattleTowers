package BattleTowers.events;

import BattleTowers.events.phases.CombatPhase;
import BattleTowers.events.phases.TextPhase;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
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
        registerPhase(0, new TextPhase(DESCRIPTIONS[0]).addOption(OPTIONS[0], (i)->transitionKey("Brazil")).addOption(OPTIONS[1], (i)->transitionKey("Japan")));
        registerPhase("Brazil", new TextPhase(DESCRIPTIONS[1]).addOption(OPTIONS[2], (i)->transitionKey("Antarctica")).addOption(OPTIONS[5], (i)->transitionKey(1)));
        registerPhase("Japan", new TextPhase(DESCRIPTIONS[2]).addOption(OPTIONS[0], (i)->transitionKey("Brazil")).addOption(OPTIONS[3], (i)->transitionKey("You can also use numbers")));
        registerPhase("Antarctica", new TextPhase(DESCRIPTIONS[3]).addOption(OPTIONS[3], (i)->transitionKey("You can also use numbers")));
        registerPhase("You can also use numbers", new TextPhase(DESCRIPTIONS[4]).addOption(OPTIONS[4], (t)->this.openMap()));

        registerPhase(1, new CombatPhase(MonsterHelper.CULTIST_ENC, true).setNextKey("Japan"));

        transitionKey(0); //starting point
    }
}
