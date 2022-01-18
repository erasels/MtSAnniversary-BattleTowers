package BattleTowers.relics;

import BattleTowers.BattleTowers;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.combat.SmallLaserEffect;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static BattleTowers.BattleTowers.makeRelicPath;

public class SentryOrb extends CustomRelic {

    // ID, images, text.
    public static final String ID = BattleTowers.makeID("SentryOrb");

    private static final Texture IMG = TextureLoader.getTexture(makeRelicPath("SentryOrb.png"));
    private static final Texture OUTLINE = TextureLoader.getTexture(makeRelicPath("outline/SentryOrb.png"));

    public static final int DAMAGE = 9;
    public static final int STATUS = 1;
    private boolean firstTurn = true;

    HashMap<String, Integer> stats = new HashMap<>();
    private final String DAMAGE_STAT = DESCRIPTIONS[2];
    private final String PER_COMBAT_STRING = DESCRIPTIONS[3];
    private final String PER_TURN_STRING = DESCRIPTIONS[4];

    public SentryOrb() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.CLINK);
        resetStats();
    }

    // Description
    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0] + DAMAGE + DESCRIPTIONS[1];
    }

    @Override
    public void atBattleStartPreDraw() {
        firstTurn = true;
    }

    public void atTurnStart() {
        //For some reason, relics activate before the turn counter ticks over, so we need to check for even turn numbers and the first turn manually
        if (firstTurn || GameActionManager.turn % 2 == 0) {
            firstTurn = false;
            flash();
            addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    if (!AbstractDungeon.player.isDead) {
                        AbstractMonster m = AbstractDungeon.getRandomMonster();
                        if (m != null && !m.isDeadOrEscaped()) {
                            CardCrawlGame.sound.play("ATTACK_MAGIC_BEAM_SHORT", 0.5F);
                            AbstractDungeon.topLevelEffects.add(new SmallLaserEffect(m.hb.cX, m.hb.cY, SentryOrb.this.hb.cX-SentryOrb.this.hb.width/2, SentryOrb.this.hb.cY-SentryOrb.this.hb.height/2));
                            m.damage(new DamageInfo(null, DAMAGE, DamageInfo.DamageType.THORNS));
                            if (m.lastDamageTaken > 0) {
                                updateStats(m.lastDamageTaken);
                            }

                            if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                                AbstractDungeon.actionManager.clearPostCombatActions();
                            } else {
                                this.addToTop(new WaitAction(0.4F));
                            }
                        }
                    }
                    this.isDone = true;
                }
            });
        }
    }

    public void updateStats(int damageDealt) {
        stats.put(DAMAGE_STAT, stats.get(DAMAGE_STAT) + damageDealt);
    }

    public String getStatsDescription() {
        return DAMAGE_STAT + stats.get(DAMAGE_STAT);
    }

    public String getExtendedStatsDescription(int totalCombats, int totalTurns) {
        // You would just return getStatsDescription() if you don't want to display per-combat and per-turn stats
        StringBuilder builder = new StringBuilder();
        builder.append(getStatsDescription());
        float stat = (float)stats.get(DAMAGE_STAT);
        // Relic Stats truncates these extended stats to 3 decimal places, so we do the same
        DecimalFormat perTurnFormat = new DecimalFormat("#.###");
        builder.append(PER_TURN_STRING);
        builder.append(perTurnFormat.format(stat / Math.max(totalTurns, 1)));
        builder.append(PER_COMBAT_STRING);
        builder.append(perTurnFormat.format(stat / Math.max(totalCombats, 1)));
        return builder.toString();
    }

    public void resetStats() {
        stats.put(DAMAGE_STAT, 0);
    }

    public JsonElement onSaveStats() {
        // An array makes more sense if you want to store more than one stat
        Gson gson = new Gson();
        ArrayList<Integer> statsToSave = new ArrayList<>();
        statsToSave.add(stats.get(DAMAGE_STAT));
        return gson.toJsonTree(statsToSave);
    }

    public void onLoadStats(JsonElement jsonElement) {
        if (jsonElement != null) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            stats.put(DAMAGE_STAT, jsonArray.get(0).getAsInt());
        } else {
            resetStats();
        }
    }

    @Override
    public AbstractRelic makeCopy() {
        // Relic Stats will always query the stats from the instance passed to BaseMod.addRelic()
        // Therefore, we make sure all copies share the same stats by copying the HashMap.
        SentryOrb newRelic = new SentryOrb();
        newRelic.stats = this.stats;
        return newRelic;
    }
}
