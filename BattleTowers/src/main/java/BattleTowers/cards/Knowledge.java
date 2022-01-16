package BattleTowers.cards;

import BattleTowers.util.UC;
import basemod.abstracts.CustomCard;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.AutoplayField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.unique.AddCardToDeckAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.BorderFlashEffect;
import com.megacrit.cardcrawl.vfx.combat.MiracleEffect;

import java.util.ArrayList;
import java.util.HashSet;

import static BattleTowers.BattleTowers.makeCardPath;
import static BattleTowers.BattleTowers.makeID;

public class Knowledge extends CustomCard {
    public static final String ID = makeID(Knowledge.class.getSimpleName());
    public static final String IMG = makeCardPath(Knowledge.class.getSimpleName() + ".png");
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    public static final String NAME = cardStrings.NAME;
    public static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final int COST = 0;

    public Knowledge() {
        super(ID, NAME, IMG, COST, DESCRIPTION, CardType.SKILL, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.SELF);
        SoulboundField.soulbound.set(this, true);
        AutoplayField.autoplay.set(this, true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        UC.atb(new AbstractGameAction() {
            @Override
            public void update() {
                HashSet<String> cards = new HashSet<>();
                ArrayList<AbstractCard> prunedDeck = new ArrayList<>(UC.deck().group);

                for (AbstractCard c : UC.deck().group) {
                    if (cards.contains(c.cardID)) {
                        prunedDeck.removeIf(card -> c.cardID.equals(card.cardID));
                    } else {
                        cards.add(c.cardID);
                    }
                }

                if (!prunedDeck.isEmpty()) {
                    AbstractCard card = UC.getRandomItem(prunedDeck, AbstractDungeon.cardRandomRng);
                    UC.att(new AddCardToDeckAction(card.makeCopy()));
                    UC.att(new MakeTempCardInHandAction(card.makeCopy()));
                    UC.att(new VFXAction(new MiracleEffect()));
                    if (!Settings.DISABLE_EFFECTS)
                        UC.att(new VFXAction(new BorderFlashEffect(Color.GOLDENROD, true)));
                }
                isDone = true;
            }
        });
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    public void upgrade() {
        if (!upgraded) {
            upgradeName();
        }
    }
}
