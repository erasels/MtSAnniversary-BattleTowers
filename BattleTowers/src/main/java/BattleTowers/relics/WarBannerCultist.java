package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.powers.ConfusionPower;
import com.megacrit.cardcrawl.powers.RitualPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class WarBannerCultist extends CustomRelic {
    public static final String ID = makeID(WarBannerCultist.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public WarBannerCultist() {
        super(ID, TextureLoader.getTexture(makeRelicPath("WarBannerCultist.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    public void atBattleStart() {

        flash();
        addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new StrengthPower(AbstractDungeon.player, -2), -2));
        addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new RitualPower(AbstractDungeon.player, 1, true)));

        addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
