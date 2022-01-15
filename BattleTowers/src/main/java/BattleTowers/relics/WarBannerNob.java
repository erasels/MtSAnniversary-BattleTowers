package BattleTowers.relics;

import BattleTowers.powers.PlayerEnragePower;
import BattleTowers.util.TextureLoader;
import BattleTowers.util.UC;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class WarBannerNob extends CustomRelic {
    public static final String ID = makeID(WarBannerNob.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);
    private static final int ENRG_AMT = 3, VULN_AMT = 2;

    public WarBannerNob() {
        super(ID, TextureLoader.getTexture(makeRelicPath("WarBannerNob.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    @Override
    public void onPlayerEndTurn() {
        AbstractPower p = UC.p().getPower(PlayerEnragePower.POWER_ID);
        if (p != null) {
            int i = (int) UC.getAliveMonsters().stream()
                    .filter(m -> m.getIntentBaseDmg() <= 0)
                    .count();
            UC.doPow(new StrengthPower(UC.p(), i));
            for (int j = 0; j < i; j++) {
                p.onSpecificTrigger();
            }
        }
    }

    public void atBattleStart() {
        flash();
        UC.doPow(new PlayerEnragePower(UC.p(), ENRG_AMT));
        UC.doPow(new WeakPower(UC.p(), VULN_AMT, false));

        addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
    }

    @Override
    public String getUpdatedDescription() {
        return String.format(DESCRIPTIONS[0], ENRG_AMT, VULN_AMT);
    }
}
