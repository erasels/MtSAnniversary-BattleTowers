package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.events.phases.TextPhase;
import BattleTowers.relics.VoidBlight;
import BattleTowers.util.UC;
import basemod.cardmods.EtherealMod;
import basemod.cardmods.ExhaustMod;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;

import static BattleTowers.BattleTowers.makeID;

public class VoidShrine extends PhasedEvent {
    public static final String ID = makeID("VoidShrine");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    private static final int GOLD_COST = 25;
    private static final int CARD_REMOVAL = 2;
    private int hpLoss;
    private boolean forExhaust = false;
    private boolean forEthereal = false;
    private boolean forRemoval = false;

    public VoidShrine() {
        super(title, BattleTowers.makeImagePath("events/VoidShrine.png"));
        this.hpLoss = MathUtils.round(UC.p().maxHealth * 0.08F);
        registerPhase(0, new TextPhase(DESCRIPTIONS[0]).
                addOption(OPTIONS[0] + FontHelper.colorString(OPTIONS[1] + hpLoss + OPTIONS[2], "r") + " " + FontHelper.colorString(OPTIONS[3], "g"), (i)->{
                    CardCrawlGame.sound.play("BLUNT_FAST");  // Play a hit sound
                    UC.p().damage(new DamageInfo(null, hpLoss));
                    forExhaust = true;
                    AbstractDungeon.gridSelectScreen.open(UC.p().masterDeck, 1, OPTIONS[11], false, false, false, false);
                    transitionKey("Blood");
                }).
                addOption(new TextPhase.OptionInfo(hasEnoughGold() ? (OPTIONS[4] + FontHelper.colorString(OPTIONS[1] + GOLD_COST + OPTIONS[5], "r") + " " + FontHelper.colorString(OPTIONS[6], "g")) : OPTIONS[13]).enabledCondition(this::hasEnoughGold), (i)->{
                    AbstractDungeon.player.loseGold(GOLD_COST);
                    forEthereal = true;
                    AbstractDungeon.gridSelectScreen.open(UC.p().masterDeck, 1, OPTIONS[11], false, false, false, false);
                    transitionKey("Wealth");
                }).
                addOption(OPTIONS[7] + FontHelper.colorString(OPTIONS[8] + CARD_REMOVAL + OPTIONS[9], "g") + " " + FontHelper.colorString(OPTIONS[10], "r"), new VoidBlight(), (i)->{
                    AbstractDungeon.getCurrRoom().spawnRelicAndObtain((Settings.WIDTH / 2f), (Settings.HEIGHT / 2f), new VoidBlight());
                    forRemoval = true;
                    AbstractDungeon.gridSelectScreen.open(UC.p().masterDeck.getPurgeableCards(), CARD_REMOVAL, OPTIONS[11], false, false, false, false);
                    transitionKey("Soul");
                }));

        registerPhase("Blood", new TextPhase(DESCRIPTIONS[1]).addOption(OPTIONS[12], (t)->this.openMap()));
        registerPhase("Wealth", new TextPhase(DESCRIPTIONS[2]).addOption(OPTIONS[12], (t)->this.openMap()));
        registerPhase("Soul", new TextPhase(DESCRIPTIONS[3]).addOption(OPTIONS[12], (t)->this.openMap()));

        transitionKey(0);
    }

    private boolean hasEnoughGold() {
        return AbstractDungeon.player.gold >= GOLD_COST;
    }

    @Override
    public void update() {
        super.update();
        if (!AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            for (AbstractCard c : AbstractDungeon.gridSelectScreen.selectedCards) {
                if (forExhaust) {
                    CardModifierManager.addModifier(c, new ExhaustMod());
                } else if (forEthereal) {
                    CardModifierManager.addModifier(c, new EtherealMod());
                } else if (forRemoval) {
                    AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(c, (Settings.WIDTH / 2), (Settings.HEIGHT / 2)));
                    UC.p().masterDeck.removeCard(c);
                }
            }
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
        }
    }
}
