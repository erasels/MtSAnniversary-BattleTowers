package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.relics.ClericsBlessing;
import BattleTowers.relics.PromiseOfGold;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.localization.EventStrings;

import java.text.MessageFormat;

import static BattleTowers.BattleTowers.makeID;

public class RoarOfTheCrowd extends AbstractImageEvent {
    public static final String ID = makeID(RoarOfTheCrowd.class.getSimpleName());
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;

    private static final int HP_LOSS = 10;
    private static final int A15_HP_LOSS = 12;
    private static final int GOLD_LOSS = 50;
    private static final int A15_GOLD_LOSS = 60;
    public static final int GOLD_AT_END = 100;
    public static final int DEXTERITY_FOR_TOWER = 2;

    private final int hpLoss;
    private final int goldLoss;

    public RoarOfTheCrowd() {
        super(eventStrings.NAME, DESCRIPTIONS[0], BattleTowers.makeImagePath("events/RoarOfTheCrowd.png"));

        this.hpLoss = Math.min(AbstractDungeon.player.currentHealth - 1, AbstractDungeon.ascensionLevel >= 15 ? A15_HP_LOSS : HP_LOSS);
        this.goldLoss = AbstractDungeon.ascensionLevel >= 15 ? A15_GOLD_LOSS : GOLD_LOSS;

        this.imageEventText.setDialogOption(MessageFormat.format(OPTIONS[0], hpLoss, GOLD_AT_END));
        if (AbstractDungeon.player.gold >= this.goldLoss) {
            this.imageEventText.setDialogOption(MessageFormat.format(OPTIONS[1], this.goldLoss, DEXTERITY_FOR_TOWER));
        }
        else {
            this.imageEventText.setDialogOption(MessageFormat.format(OPTIONS[2], this.goldLoss), true);
        }
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch (screenNum) {
            case 0:
                switch (buttonPressed) {
                    case 0: // Cultist
                        if (this.hpLoss > 0) {
                            AbstractDungeon.player.damage(new DamageInfo(null, this.hpLoss));
                        }
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float) (Settings.WIDTH / 2), (float) (Settings.HEIGHT / 2), new PromiseOfGold());

                        if (this.hpLoss > 0) {
                            this.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                        }
                        else {
                            this.imageEventText.updateBodyText(DESCRIPTIONS[2]);
                        }
                        this.screenNum = 1;
                        this.imageEventText.updateDialogOption(0, OPTIONS[3]);
                        this.imageEventText.clearRemainingOptions();
                        break;
                    case 1: // Cleric
                        AbstractDungeon.player.loseGold(this.goldLoss);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float) (Settings.WIDTH / 2), (float) (Settings.HEIGHT / 2), new ClericsBlessing());

                        this.imageEventText.updateBodyText(DESCRIPTIONS[3]);
                        this.screenNum = 1;
                        this.imageEventText.updateDialogOption(0, OPTIONS[3]);
                        this.imageEventText.clearRemainingOptions();
                        break;
                }
                break;
            default:
                this.openMap();
                break;
        }
    }
}
