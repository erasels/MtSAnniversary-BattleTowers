//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.cards.CursedTapestry;
import BattleTowers.relics.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.curses.Pain;
import com.megacrit.cardcrawl.cards.curses.Shame;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.ArrayList;
import java.util.Iterator;

import static BattleTowers.BattleTowers.makeID;

public class BannerSageEvent extends AbstractImageEvent {
    public static final String ID = makeID("BannerSageEvent");
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

    private AbstractRelic r1;
    private AbstractRelic r2;

    public BannerSageEvent() {
        super(NAME, DESCRIPTIONS[0], BattleTowers.makeImagePath("events/bannersage.png"));

        ArrayList<AbstractRelic> banners = new ArrayList<>();
        banners.add(new WarBannerLouse());
        banners.add(new WarBannerNob());
        banners.add(new WarBannerCultist());
        banners.add(new WarBannerSnecko());

        r1 = banners.get(AbstractDungeon.eventRng.random(0,3));
        banners.remove(r1);
        r2 = banners.get(AbstractDungeon.eventRng.random(0,2));


        if (AbstractDungeon.player.gold >= 100) {
            this.imageEventText.setDialogOption(OPTIONS[1] + r1.DESCRIPTIONS[1], r1);
            this.imageEventText.setDialogOption(OPTIONS[1] + r2.DESCRIPTIONS[1], r2);
        } else {
            this.imageEventText.setDialogOption(OPTIONS[0], true);
            this.imageEventText.setDialogOption(OPTIONS[0], true);
        }

        this.imageEventText.setDialogOption(OPTIONS[2], new CursedTapestry());
       // this.imageEventText.setDialogOption(OPTIONS[3]);


    }

    protected void buttonEffect(int buttonPressed) {
        switch (this.screenNum) {
            case 0:
                switch (buttonPressed) {
                    case 0:
                        AbstractDungeon.player.loseGold(100);
                        this.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2, Settings.HEIGHT / 2, r1);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[3]);
                        this.screenNum = 1;
                        return;
                    case 1:
                        AbstractDungeon.player.loseGold(100);
                        this.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2, Settings.HEIGHT / 2, r2);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[3]);
                        this.screenNum = 1;
                        return;
                    case 2:
                        this.screenNum = 1;
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new Pain(), Settings.WIDTH * .33F, Settings.HEIGHT / 2));
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new CursedTapestry(), Settings.WIDTH * .66F, Settings.HEIGHT / 2));
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[3]);
                        this.imageEventText.updateBodyText(DESCRIPTIONS[2]);
                        return;
                    case 3:
                        this.screenNum = 1;
                        this.imageEventText.updateBodyText(DESCRIPTIONS[3]);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[3]);
                        return;
                }
            case 1:

                this.openMap();
        }

    }
}
