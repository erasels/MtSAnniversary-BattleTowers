package BattleTowers.relics;

import BattleTowers.cards.WindStrike;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import basemod.helpers.CardPowerTip;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class JadeIdol extends CustomRelic {
    public static final String ID = makeID(JadeIdol.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public JadeIdol() {
        super(ID, TextureLoader.getTexture(makeRelicPath("JadeIdol.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        this.tips.clear();
        this.tips.add(new PowerTip(name, description));
        this.tips.add(new CardPowerTip(new WindStrike(), relicStrings.DESCRIPTIONS[1], ""));
        this.initializeTips();
    }

    /*@Override
    public void onEquip() {
        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new WindStrike(), (float) Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F));
    }*/

    public void atBattleStart() {
        addToBot(new MakeTempCardInHandAction(new WindStrike(),1));
    }
    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}