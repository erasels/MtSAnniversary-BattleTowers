package BattleTowers.util;

import BattleTowers.BattleTowers;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UC {
    //Common references
    public static AbstractPlayer p() {
        return AbstractDungeon.player;
    }
    public static CardGroup hand() { return AbstractDungeon.player.hand;}
    public static CardGroup deck() { return AbstractDungeon.player.masterDeck;}

    private static DecimalFormat twoDecFormat = new DecimalFormat("#0.00");
    public static GlyphLayout layout = new GlyphLayout();

    //Checks


    //Actionmanager
    public static void atb(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }

    public static void att(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }

    //Do common effect
    public static void doDmg(AbstractCreature target, AbstractCard c) {
        doDmg(target, c.damage, c.damageTypeForTurn, AbstractGameAction.AttackEffect.NONE);
    }
    public static void doDmg(AbstractCreature target, AbstractCard c, AbstractGameAction.AttackEffect ae) {
        doDmg(target, c.damage, c.damageTypeForTurn, ae);
    }

    public static void doDmg(AbstractCreature target, AbstractCard c, AbstractGameAction.AttackEffect ae, boolean top) {
        doDmg(target, c.damage, c.damageTypeForTurn, ae, false, top);
    }

    public static void doDmg(AbstractCreature target, int amount) {
        doDmg(target, amount, DamageInfo.DamageType.NORMAL, AbstractGameAction.AttackEffect.NONE);
    }

    public static void doDmg(AbstractCreature target, int amount, DamageInfo.DamageType dt) {
        doDmg(target, amount, dt, AbstractGameAction.AttackEffect.NONE);
    }

    public static void doDmg(AbstractCreature target, int amount, AbstractGameAction.AttackEffect ae) {
        doDmg(target, amount, DamageInfo.DamageType.NORMAL, ae);
    }

    public static void doDmg(AbstractCreature target, int amount, DamageInfo.DamageType dt, AbstractGameAction.AttackEffect ae) {
        doDmg(target, amount, dt, ae, false);
    }

    public static void doDmg(AbstractCreature target, int amount, DamageInfo.DamageType dt, AbstractGameAction.AttackEffect ae, boolean fast) {
        doDmg(target, amount, dt, ae, fast, false);
    }

    public static void doDmg(AbstractCreature target, int amount, DamageInfo.DamageType dt, AbstractGameAction.AttackEffect ae, boolean fast, boolean top) {
        if (target == null) {
            target = AbstractDungeon.getRandomMonster();
        }
        if (top) {
            att(new DamageAction(target, new DamageInfo(p(), amount, dt), ae, fast));
        } else {
            atb(new DamageAction(target, new DamageInfo(p(), amount, dt), ae, fast));
        }
    }

    public static void dmg(AbstractCreature target, DamageInfo info, AbstractGameAction.AttackEffect effect, boolean top) {
        if(top){
            att(new DamageAction(target, info, effect));
        }
        else {
            atb(new DamageAction(target, info, effect));
        }
    }

    public static void dmg(AbstractCreature target, DamageInfo info) {
        dmg(target, info, AbstractGameAction.AttackEffect.NONE, false);
    }

    public static void doAllDmg(int amount, AbstractGameAction.AttackEffect ae, DamageInfo.DamageType dt, boolean top) {
        if (top) {
            att(new DamageAllEnemiesAction(p(), amount, dt, ae));
        } else {
            atb(new DamageAllEnemiesAction(p(), amount, dt, ae));
        }
    }

    public static void doAllDmg(AbstractCard c, AbstractGameAction.AttackEffect ae, boolean top) {
        if (top) {
            att(new DamageAllEnemiesAction(p(), c.multiDamage, c.damageTypeForTurn, ae));
        } else {
            atb(new DamageAllEnemiesAction(p(), c.multiDamage, c.damageTypeForTurn, ae));
        }
    }

    public static void doDef(AbstractCard c) {
        doDef(c.block, false);
    }

    public static void doDef(int amount) {
        doDef(amount, false);
    }

    public static void doDefTarget(AbstractCreature source, int amount) {
        atb(new GainBlockAction(source, source, amount));
    }

    public static void doDef(int amount, boolean top) {
        if (top) {
            att(new GainBlockAction(p(), p(), amount));
        } else {
            atb(new GainBlockAction(p(), p(), amount));
        }
    }

    public static void doDef(int amount, AbstractCreature source, boolean top) {
        if (top) {
            att(new GainBlockAction(p(), source, amount));
        } else {
            atb(new GainBlockAction(p(), source, amount));
        }
    }

    public static void doPow(AbstractPower p) {
        doPow(p.owner, p, false);
    }

    public static void doPow(AbstractCreature target, AbstractPower p) {
        doPow(target, p, false);
    }

    public static void doPow(AbstractCreature target, AbstractPower p, boolean top) {
        doPow(UC.p(), target, p, top);
    }

    public static void doPow(AbstractCreature source, AbstractCreature target, AbstractPower p, boolean top) {
        if (top) {
            att(new ApplyPowerAction(target, source, p, p.amount));
        } else {
            atb(new ApplyPowerAction(target, source, p, p.amount));
        }
    }

    public static void doVfx(AbstractGameEffect gameEffect) {
        atb(new VFXAction(gameEffect));
    }

    public static void doVfx(AbstractGameEffect gameEffect, float duration) {
        atb(new VFXAction(gameEffect, duration));
    }

    public static void doDraw(int number) {
        atb(new DrawCardAction(p(), number));
    }

    public static void doDraw(int number, AbstractGameAction follow) {
        atb(new DrawCardAction(number, follow));
    }

    public static void doDraw(int number, Consumer<ArrayList<AbstractCard>> callback) {
        atb(new DrawCardAction(number, new AbstractGameAction() {
            @Override
            public void update() {
                callback.accept(DrawCardAction.drawnCards);
                isDone = true;
            }
        }));
    }

    public static void generalPowerLogic(AbstractPower p) {
        generalPowerLogic(p, false);
    }

    public static void generalPowerLogic(AbstractPower p, boolean top) {
        if (p.amount < 1) {
            if (top) {
                att(new RemoveSpecificPowerAction(p.owner, p.owner, p));
            } else {
                atb(new RemoveSpecificPowerAction(p.owner, p.owner, p));
            }
        } else {
            if (top) {
                att(new ReducePowerAction(p.owner, p.owner, p, 1));
            } else {
                UC.reducePower(p);
            }
        }
    }

    public static void reducePower(AbstractPower p, int amount) {
        atb(new ReducePowerAction(p.owner, p.owner, p, amount));
    }

    public static void reducePower(AbstractPower p) {
        reducePower(p, 1);
    }

    public static void removePower(AbstractPower p, boolean top) {
        if(top) {
            att(new RemoveSpecificPowerAction(p.owner, p.owner, p));
        } else {
            atb(new RemoveSpecificPowerAction(p.owner, p.owner, p));
        }
    }
    public static void removePower(AbstractPower p) {
        removePower(p, false);
    }
    public static void copyCardPosition(AbstractCard original, AbstractCard target) {
        target.current_x = original.current_x;
        target.current_y = original.current_y;
        target.target_x = original.target_x;
        target.target_y = original.target_y;
        target.drawScale = original.drawScale;
        target.targetDrawScale = original.targetDrawScale;
        target.angle = original.angle;
        target.targetAngle = original.targetAngle;
        target.transparency = original.transparency;
        target.targetTransparency = original.targetTransparency;
    }

    //Getters
    public static boolean isAttacking(AbstractCreature m) {
        if(m instanceof AbstractMonster) {
            return ((AbstractMonster) m).getIntentBaseDmg() >= 0;
        }
        return false;
    }

    public static boolean isInCombat() {
        return CardCrawlGame.isInARun() && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    public static <T> T getRandomItem(ArrayList<T> list) {
        return getRandomItem(list, AbstractDungeon.cardRandomRng);
    }
    public static <T> T getRandomItem(ArrayList<T> list, Random rng) {
        return list.isEmpty() ? null : list.get(rng.random(list.size() - 1));
    }
    public static DamageInfo getDmg(AbstractCreature target, AbstractCard c) {
        return new DamageInfo(target, c.damage, c.damageTypeForTurn);
    }

    public static ArrayList<AbstractMonster> getAliveMonsters() {
        return AbstractDungeon.getMonsters().monsters.stream().filter(m -> !m.isDeadOrEscaped()).collect(Collectors.toCollection(ArrayList::new));
    }

    public static int getLogicalCardCost(AbstractCard c) {
        if (c.costForTurn > 0 && !c.freeToPlayOnce) {
            return c.costForTurn;
        }
        return 0;
    }

    public static Texture getTexture(String folder, String name) {
        return TextureLoader.getTexture(BattleTowers.makeImagePath(folder + "/" + name + ".png"));
    }

    public static TextureAtlas.AtlasRegion getTextureAtlas(String folder, String name) {
        return TextureLoader.getTextureAsAtlasRegion(BattleTowers.makeImagePath(folder + "/" + name + ".png"));
    }

    public static AbstractGameAction.AttackEffect getSpeedyAttackEffect() {
        int effect = MathUtils.random(0, 4);
        switch (effect) {
            case 0:
                return AbstractGameAction.AttackEffect.SLASH_HORIZONTAL;
            case 1:
                return AbstractGameAction.AttackEffect.SLASH_VERTICAL;
            case 2:
                return AbstractGameAction.AttackEffect.BLUNT_LIGHT;
            default:
                return AbstractGameAction.AttackEffect.SLASH_DIAGONAL;
        }
    }

    public static Color getRandomFireColor() {
        int i = MathUtils.random(3);
        switch (i) {
            case 0:
                return Color.ORANGE;
            case 1:
                return Color.YELLOW;
            default:
                return Color.RED;
        }
    }

    public static <T> T getModifiedObj(T t, String fieldKey, Object newValue, boolean isProtected) {
        if (!isProtected) {
            ReflectionHacks.setPrivate(t, t.getClass(), fieldKey, newValue);
        } else {
            ReflectionHacks.setPrivateInherited(t, t.getClass(), fieldKey, newValue);
        }
        return t;
    }

    public static AbstractCard upgCard(AbstractCard c) {
        c.upgrade();
        return c;
    }

    public static String get2DecString(float num) {
        if (num < 0) {
            num = 0;
        }
        return twoDecFormat.format(MathUtils.round(num));
    }

    public static int makePercentage(float in) {
        return MathUtils.floor(in * 100f);
    }

    public static int getPercentageInc(float val) {
        return MathUtils.floor((val - 1f) * 100f);
    }

    public static float gt() {
        return Gdx.graphics.getRawDeltaTime();
    }

    //Setters
    public static <T> boolean True(T t) {
        return true;
    }

    //Display
    public static void displayTimer(SpriteBatch sb, String msg, float y, Color color) {
        String tmp = msg.replaceAll("\\d", "0");
        layout.setText(FontHelper.SCP_cardEnergyFont, tmp);
        float baseBox = layout.width;
        layout.setText(FontHelper.SCP_cardEnergyFont, msg);
        sb.setColor(Settings.TWO_THIRDS_TRANSPARENT_BLACK_COLOR);
        //sb.draw(ImageMaster.WHITE_SQUARE_IMG, Settings.WIDTH / 2.0F - baseBox / 2.0F - 12.0F * Settings.scale, y - 24.0F * Settings.scale, baseBox + 24.0F * Settings.scale, layout.height * Settings.scale);
        FontHelper.renderFont(sb, FontHelper.SCP_cardEnergyFont, msg, (Settings.WIDTH / 2.0F) - baseBox / 2.0F, y + layout.height / 2.0F, color);
        //FontHelper.renderFontCentered(sb, FontHelper.SCP_cardEnergyFont, msg, Settings.WIDTH / 2.0F, y, color);
    }
}
