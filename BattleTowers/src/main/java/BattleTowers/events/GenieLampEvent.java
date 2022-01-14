package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.blights.GreedBlight;
import BattleTowers.cards.Knowledge;
import BattleTowers.events.phases.TextPhase;
import BattleTowers.events.phases.WrappedEventPhase;
import BattleTowers.patches.EventWrapping;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import static BattleTowers.BattleTowers.makeID;

public class GenieLampEvent extends PhasedEvent {
    public static final String ID = makeID("GenieLamp");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    private static final int GOLD_GAIN = 200;

    public GenieLampEvent() {
        super(title, BattleTowers.makeImagePath("events/GenieLamp.png"));

        registerPhase(0, new TextPhase(DESCRIPTIONS[0])
                .addOption(OPTIONS[0], (i)->{transitionKey("Knowledge");
                    AbstractDungeon.topLevelEffects.add(new ShowCardAndObtainEffect(new Knowledge(), Settings.WIDTH/2f, Settings.HEIGHT/2f));
                })
                .addOption(String.format(OPTIONS[1], getGoldGain()), (i)->{transitionKey("Wealth");
                    AbstractDungeon.effectList.add(new RainingGoldEffect(getGoldGain()));
                    AbstractDungeon.player.gainGold(getGoldGain());
                    AbstractDungeon.getCurrRoom().spawnBlightAndObtain((Settings.WIDTH / 2f), (Settings.HEIGHT / 2f), new GreedBlight());
                })
                .addOption(OPTIONS[2], (i)-> {transitionKey("Freedom");
                    CardCrawlGame.sound.play("POWER_ENTANGLED", 0.05F);
                }));
        registerPhase("Knowledge", new TextPhase(DESCRIPTIONS[1]).addOption(OPTIONS[3], (t)->this.openMap()));
        registerPhase("Wealth", new TextPhase(DESCRIPTIONS[2]).addOption(OPTIONS[3], (t)->this.openMap()));
        registerPhase("Freedom", new TextPhase(DESCRIPTIONS[3]).addOption(OPTIONS[3], (t)-> {
            WrappedEventPhase wrapper = EventWrapping.Field.wrapper.get(this);
            if (wrapper != null) {
                wrapper.followupKey = null;
            }
            this.openMap();
        }));

        transitionKey(0);
    }

    private int getGoldGain() {
        if(AbstractDungeon.ascensionLevel >= 15) {
            return MathUtils.round(GOLD_GAIN * 0.9f);
        } else {
            return GOLD_GAIN;
        }
    }

    public void onEnterRoom() {
        if (Settings.AMBIANCE_ON)
            CardCrawlGame.sound.play("EVENT_TOME");
    }
}
