package BattleTowers.relics;

import BattleTowers.cards.*;
import BattleTowers.powers.PlayerEnragePower;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.powers.RitualPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class WarBannerNob extends CustomRelic {
    public static final String ID = makeID(WarBannerNob.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public WarBannerNob() {
        super(ID, TextureLoader.getTexture(makeRelicPath("WarBannerNob.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    @Override
    public void onTrigger() {
        if (AbstractDungeon.player.hasPower(PlayerEnragePower.POWER_ID)) {
            addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new StrengthPower(AbstractDungeon.player, 1), 1));
            AbstractDungeon.player.getPower(PlayerEnragePower.POWER_ID).onSpecificTrigger();

        }

    }

    public void atBattleStart() {

        flash();
        addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new PlayerEnragePower(AbstractDungeon.player), 3));
        addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new VulnerablePower(AbstractDungeon.player, 2, false), 2));

        addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
