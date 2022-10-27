package BattleTowers.relics;

import BattleTowers.cards.WindStrike;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import basemod.helpers.CardPowerTip;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class JadeIdol extends CustomRelic {
    public static final String ID = makeID(JadeIdol.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);
    private boolean used = false;
    public JadeIdol() {
        super(ID, TextureLoader.getTexture(makeRelicPath("JadeIdol.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        this.tips.add(new CardPowerTip(new WindStrike()));
    }
    public void atBattleStart() {
        used =false;
    }
    public void onEquip() {
        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(new WindStrike(), (float) Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F));
    }
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        if (targetCard.hasTag(AbstractCard.CardTags.STRIKE) && !used){
            used = true;
            this.flash();
            AbstractMonster m = null;
            if (useCardAction.target != null) {
                m = (AbstractMonster)useCardAction.target;
            }

            AbstractCard tmp = targetCard.makeSameInstanceOf();
            AbstractDungeon.player.limbo.addToBottom(tmp);
            tmp.current_x = targetCard.current_x;
            tmp.current_y = targetCard.current_y;
            tmp.target_x = (float)Settings.WIDTH / 2.0F - 300.0F * Settings.scale;
            tmp.target_y = (float)Settings.HEIGHT / 2.0F;
            if (m != null) {
                tmp.calculateCardDamage(m);
            }

            tmp.purgeOnUse = true;
            AbstractDungeon.actionManager.addCardQueueItem(new CardQueueItem(tmp, m, targetCard.energyOnUse, true, true), true);
        }
    }
    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}