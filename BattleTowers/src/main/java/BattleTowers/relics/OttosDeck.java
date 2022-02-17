package BattleTowers.relics;

import BattleTowers.cards.QueenCards.*;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class OttosDeck extends CustomRelic {
    public static final String ID = makeID(OttosDeck.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public OttosDeck() {
        super(ID, TextureLoader.getTexture(makeRelicPath("OttosDeck.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    @Override
    public void onEquip() {
        for (int i = 0; i < AbstractDungeon.player.masterDeck.size() - 1; i++) {
            AbstractCard c = AbstractDungeon.player.masterDeck.group.get(i);
            if (c.hasTag(AbstractCard.CardTags.STARTER_STRIKE) || c.hasTag(AbstractCard.CardTags.STARTER_DEFEND)) {
                AbstractDungeon.player.masterDeck.removeCard(c);
                i--;
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new PawnsAdvance(), (float) Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F));
            }
        }

        ArrayList<String> uncommons = new ArrayList<>();
        uncommons.add(KnightsManeuver.ID);
        uncommons.add(BishopsPrayer.ID);
        uncommons.add(RooksCharge.ID);
        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(CardLibrary.getCopy(uncommons.get(AbstractDungeon.cardRandomRng.random(uncommons.size() - 1))), Settings.WIDTH / 3.0F, Settings.HEIGHT / 2.0F));

        ArrayList<String> rares = new ArrayList<>();
        rares.add(KingsCommand.ID);
        rares.add(QueensGrace.ID);
        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(CardLibrary.getCopy(rares.get(AbstractDungeon.cardRandomRng.random(rares.size() - 1))), (Settings.WIDTH / 3.0F) * 2, Settings.HEIGHT / 2.0F));

    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
