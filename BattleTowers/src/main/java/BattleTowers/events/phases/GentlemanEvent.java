package BattleTowers.events.phases;

import BattleTowers.BattleTowers;
import BattleTowers.events.PhasedEvent;
import BattleTowers.events.phases.CombatPhase;
import BattleTowers.events.phases.TextPhase;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.UpgradeShrine;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

import static BattleTowers.BattleTowers.makeID;

public class GentlemanEvent extends PhasedEvent {
    public static final String ID = makeID("GentlemanEvent");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    public GentlemanEvent() {
        super(title, BattleTowers.makeImagePath("events/figure.png"));

        AbstractRelic r =  AbstractDungeon.player.relics.get(0);
        StringBuilder newRelicName;
        String relicName = r.name;
        if (r.name.contains(" ")){
            newRelicName = new StringBuilder();
            String [] words = relicName.split(" ");
            for (int i=0;i<words.length; i++){
                words[i] = "#y"+words[i];
                newRelicName.append(words[i]);
                if (i != words.length-1){
                    newRelicName.append(" ");
                }
                System.out.println(newRelicName.toString());
            }
            relicName = newRelicName.toString();
        }

        //set up event
        registerPhase("Accepted", new TextPhase(DESCRIPTIONS[1])
                .addOption("Leave", (i) -> {
                    AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                    this.openMap();
                }));
        registerPhase("Declined", new TextPhase(DESCRIPTIONS[2])
                .addOption("Leave", (i) -> {
                    AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                    this.openMap();
                }));

        registerPhase(0, new TextPhase(DESCRIPTIONS[0])
                .addOption(OPTIONS[0] + relicName + OPTIONS[1], (i)-> {
                    AbstractDungeon.player.relics.remove(AbstractDungeon.player.relics.get(0));
                    AbstractDungeon.player.heal(AbstractDungeon.player.maxHealth);
                    transitionKey("Accepted");
                })
                .addOption(OPTIONS[2], (i)->transitionKey("Declined")));

        transitionKey(0); //starting point



    }
}

