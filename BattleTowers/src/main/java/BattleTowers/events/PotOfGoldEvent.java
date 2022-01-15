//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.cards.DarkEnchantment;
import BattleTowers.cards.Greedy;
import BattleTowers.relics.IronPotHelmet;
import BattleTowers.relics.Lucky;
import BattleTowers.relics.OttosDeck;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.curses.Shame;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.Iterator;

import static BattleTowers.BattleTowers.makeID;

public class PotOfGoldEvent extends AbstractImageEvent {
    public static final String ID = makeID("PotOfGoldEvent");
    public static final String NAME;
    public static final String[] DESCRIPTIONS;
    public static final String[] OPTIONS;
    private static final EventStrings eventStrings;

    static {
        eventStrings = CardCrawlGame.languagePack.getEventString(ID);
        NAME = eventStrings.NAME;
        DESCRIPTIONS = eventStrings.DESCRIPTIONS;
        OPTIONS = eventStrings.OPTIONS;
    }

    private int screenNum = 0;

    public PotOfGoldEvent() {
        super(NAME, DESCRIPTIONS[0], BattleTowers.makeImagePath("events/potofgold.png"));

        if (AbstractDungeon.player.gold >= 20) {
            this.imageEventText.setDialogOption(OPTIONS[0], new Lucky());
        } else {
            this.imageEventText.setDialogOption(OPTIONS[1], true);
        }

        this.imageEventText.setDialogOption(OPTIONS[2], new Greedy());
        this.imageEventText.setDialogOption(OPTIONS[3], new IronPotHelmet());
        this.imageEventText.setDialogOption(OPTIONS[4], new DarkEnchantment());

    }

    protected void buttonEffect(int buttonPressed) {
        switch (this.screenNum) {
            case 0:
                switch (buttonPressed) {
                    case 0:

                        this.screenNum = 1;

                        this.imageEventText.updateBodyText(DESCRIPTIONS[4]);
                        AbstractDungeon.player.loseGold(20);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2, Settings.HEIGHT / 2, new Lucky());

                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[5]);
                        return;
                    case 1:
                        this.screenNum = 1;
                        this.imageEventText.updateBodyText(DESCRIPTIONS[1] + DESCRIPTIONS[2]);
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new Greedy(), Settings.WIDTH / 2, Settings.HEIGHT / 2));

                        AbstractDungeon.player.gainGold(75);
                        AbstractDungeon.effectList.add(new com.megacrit.cardcrawl.vfx.RainingGoldEffect(75));

                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[5]);
                        return;
                    case 2:

                        this.screenNum = 1;
                        this.imageEventText.updateBodyText(DESCRIPTIONS[1] + DESCRIPTIONS[3]);
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new Greedy(), Settings.WIDTH * 0.66F, Settings.HEIGHT * 0.66F));
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new Greedy(), Settings.WIDTH * 0.33F, Settings.HEIGHT * 0.33F));

                        AbstractDungeon.player.gainGold(75);
                        AbstractDungeon.effectList.add(new com.megacrit.cardcrawl.vfx.RainingGoldEffect(75));

                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[5]);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2, Settings.HEIGHT / 2, new IronPotHelmet());

                        return;
                    case 3:
                        this.screenNum = 1;

                        this.imageEventText.updateBodyText(DESCRIPTIONS[5]);
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new DarkEnchantment(), Settings.WIDTH / 2, Settings.HEIGHT / 2));

                        AbstractDungeon.player.damage(new DamageInfo((AbstractCreature)null, 5, DamageInfo.DamageType.HP_LOSS));
                        CardCrawlGame.sound.play("BLOOD_SPLAT");
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[5]);

                        return;
                }
            case 1:

                this.openMap();
        }

    }

}
