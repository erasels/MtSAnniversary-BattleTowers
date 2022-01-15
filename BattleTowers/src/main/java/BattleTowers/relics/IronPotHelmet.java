package BattleTowers.relics;

import BattleTowers.BattleTowers;
import BattleTowers.util.TextureLoader;
import BattleTowers.vfx.Hats;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import com.esotericsoftware.spine.Skeleton;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.RitualPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class IronPotHelmet extends CustomRelic {
    public static final String ID = makeID(IronPotHelmet.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    private boolean hatLoaded;

    public IronPotHelmet() {
        super(ID, TextureLoader.getTexture(makeRelicPath("IronPotHelmet.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }

    @Override
    public void onPlayCard(AbstractCard c, AbstractMonster m) {
        super.onPlayCard(c, m);
        if (c.hasTag(AbstractCard.CardTags.STARTER_DEFEND)){
            this.flash();
            addToBot(new GainBlockAction(AbstractDungeon.player, 3));
        }
    }

    //Doing this on update() instead of onEquip() since onEquip won't trigger on a game load.
    @Override
    public void update() {
        super.update();
        if (!hatLoaded){
            if (AbstractDungeon.player != null) {
                Skeleton sk = null;
                sk = ReflectionHacks.getPrivate(AbstractDungeon.player, AbstractCreature.class, "skeleton");
                if (sk != null) {
                    //BattleTowers.logger.info(sk.toString());
                    loadHat();
                    hatLoaded = true;
                }
            }
        }
    }

    public void loadHat() {
        super.onEquip();

        Hats.addHat(AbstractDungeon.player,
                ReflectionHacks.getPrivate(AbstractDungeon.player, AbstractCreature.class,"skeleton"),
                "ironPotHelmet",
                makeRelicPath("IronPotHelmet_Hat.png"));
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
