package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.events.phases.TextPhase;
import BattleTowers.relics.Torch;
import BattleTowers.util.UC;
import com.badlogic.gdx.math.MathUtils;
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
import java.util.Collections;
import java.util.Random;

import static BattleTowers.BattleTowers.makeID;

public class EmeraldFlame extends PhasedEvent {
    public static final String ID = makeID("EmeraldFlame");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    private AbstractRelic relicChoice;
    private int hpLoss;

    public EmeraldFlame() {
        super(title, BattleTowers.makeImagePath("events/bonfire.png"));

        ArrayList<AbstractRelic> relics = new ArrayList<>();
        relics.addAll(UC.p().relics);
        Collections.shuffle(relics, new Random(AbstractDungeon.miscRng.randomLong()));
        this.relicChoice = relics.get(0);
        this.hpLoss = MathUtils.round(UC.p().maxHealth * 0.05F);

        registerPhase(0, new TextPhase(DESCRIPTIONS[0] + DESCRIPTIONS[1] + DESCRIPTIONS[2])
        {
            @Override
            public void update() {
                if (!AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
                    AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.remove(0);
                    AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2), (Settings.HEIGHT / 2), new Torch(c));
                    AbstractDungeon.gridSelectScreen.selectedCards.clear();
                    AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(c, (Settings.WIDTH / 2), (Settings.HEIGHT / 2)));
                    UC.p().masterDeck.removeCard(c);
                    UC.p().damage(new DamageInfo(null, hpLoss));
                    transitionKey("AfterCard");
                }
            }
        }
            .addOption(OPTIONS[0] + relicChoice.name + OPTIONS[1], (i)->{
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                UC.p().loseRelic(relicChoice.relicId);
                AbstractDungeon.getCurrRoom().baseRareCardChance = 1000; //Always give rares
                AbstractDungeon.combatRewardScreen.open(DESCRIPTIONS[5]);
                transitionKey("AfterRelic");
            })
            .addOption(OPTIONS[2] + hpLoss + OPTIONS[3], (i)->{
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                AbstractDungeon.gridSelectScreen.open(UC.p().masterDeck.getPurgeableCards(), 1, OPTIONS[4], false, false, false, true);
            }));
        registerPhase("AfterRelic", new TextPhase(DESCRIPTIONS[6] + FontHelper.colorString(relicChoice.name, "y") + DESCRIPTIONS[7]).addOption(OPTIONS[5], (t)->this.openMap()));
        registerPhase("AfterCard", new TextPhase(DESCRIPTIONS[3] + DESCRIPTIONS[4]).addOption(OPTIONS[5], (t)->this.openMap()));

        transitionKey(0);
    }

    public void onEnterRoom() {
        if (Settings.AMBIANCE_ON)
            CardCrawlGame.sound.play("EVENT_GOOP");
    }
}
