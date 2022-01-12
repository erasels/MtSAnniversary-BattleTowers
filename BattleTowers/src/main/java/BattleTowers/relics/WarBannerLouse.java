package BattleTowers.relics;

import BattleTowers.powers.PlayerCurlUpPower;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.powers.ConfusionPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class WarBannerLouse extends CustomRelic {
    public static final String ID = makeID(WarBannerLouse.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public WarBannerLouse() {
        super(ID, TextureLoader.getTexture(makeRelicPath("WarBannerLouse.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    public void atBattleStart() {

        flash();
        addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new PlayerCurlUpPower(AbstractDungeon.player, 10), 10));
        addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
