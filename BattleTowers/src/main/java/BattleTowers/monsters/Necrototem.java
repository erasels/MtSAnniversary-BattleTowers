package BattleTowers.monsters;

import BattleTowers.powers.CursedTotemPower;
import BattleTowers.powers.StrengthTotemPower;
import BattleTowers.util.UC;
import basemod.helpers.VfxBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.actions.common.SpawnMonsterAction;
import com.megacrit.cardcrawl.actions.common.SuicideAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DrawReductionPower;
import com.megacrit.cardcrawl.powers.MinionPower;
import com.megacrit.cardcrawl.vfx.combat.ShockWaveEffect;

import static BattleTowers.BattleTowers.*;

public class Necrototem extends AbstractBTMonster
{
    public static final String ID = makeID(Necrototem.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte SUMMON = 0;
    private static final byte CURSE = 1;
    private static final byte NAILS = 2;

    private static final ShaderProgram shader = new ShaderProgram(
            Gdx.files.internal(makePath("shaders/totem/vertexShader.vs")),
            Gdx.files.internal(makePath("shaders/totem/fragShader.fs"))
    );

    private final Texture faceImg;
    private final int strengthBoost;

    public Necrototem() {
        this(0.0f, 0.0f);
    }

    public Necrototem(final float x, final float y) {
        super(NAME, ID, 140, -8.0F, 10.0f, 230.0f, 300.0f, makeImagePath("monsters/Necrototem/necromanticTotem.png"), x, y);

        faceImg = ImageMaster.loadImage(makeImagePath("monsters/Necrototem/necromanticTotemFace.png"));

        setHp(calcAscensionTankiness(50));

        addMove(SUMMON, Intent.UNKNOWN);
        addMove(CURSE, Intent.ATTACK_DEBUFF, calcAscensionDamage(21));
        addMove(NAILS, Intent.ATTACK, 4, 0, true);

        if (AbstractDungeon.ascensionLevel >= 19) {
            strengthBoost = 3;
        } else {
            strengthBoost = 2;
        }
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        // we set the enemy type here so the calcAscensionMethods are called after the enemy type is set
        this.type = EnemyType.BOSS;
    }

    @Override
    public void usePreBattleAction()
    {
        UC.doPow(this, this, new CursedTotemPower(this), false);
    }

    @Override
    public void takeTurn() {
        //Automatically grabs the damage values and number of hits value from the moves hashmap based on the currently set move
        DamageInfo info = new DamageInfo(this, this.moves.get(nextMove).baseDamage, DamageInfo.DamageType.NORMAL);
        int multiplier = this.moves.get(nextMove).multiplier;

        if (info.base > -1) {
            info.applyPowers(this, AbstractDungeon.player);
        }

        switch (this.nextMove) {
            case SUMMON: {
                final float yOffset = 300;
                UC.doVfx(
                        new VfxBuilder(ImageMaster.vfxAtlas.findRegion("combat/stake"), hb.cX, hb.cY, 1f)
                                .setColor(Color.PURPLE)
                                .setX(drawX - 416 * Settings.scale)
                                .setY(drawY + (12 + yOffset) * Settings.scale)
                                .scale(0.005f, 0.7f, VfxBuilder.Interpolations.SWING)
                                .andThen(0.2f)
                                .moveY(drawY + (12 + yOffset) * Settings.scale, drawY + 12 * Settings.scale, VfxBuilder.Interpolations.SWINGIN)
                                .playSoundAt(0.18f, "BLUNT_HEAVY")
                                .build(),
                        0.1f
                );
                UC.doVfx(
                        new VfxBuilder(ImageMaster.vfxAtlas.findRegion("combat/stake"), hb.cX, hb.cY, 1f)
                                .setColor(Color.PURPLE)
                                .setX(drawX - 233 * Settings.scale)
                                .setY(drawY + (-12 + yOffset) * Settings.scale)
                                .scale(0.005f, 0.7f, VfxBuilder.Interpolations.SWING)
                                .andThen(0.2f)
                                .moveY(drawY + (-12 + yOffset) * Settings.scale, drawY + -12 * Settings.scale, VfxBuilder.Interpolations.SWINGIN)
                                .playSoundAt(0.18f, "BLUNT_HEAVY")
                                .build(),
                        1.0f
                );

                AbstractMonster m1 = new NecrototemMinion(-416, 12f);
                AbstractMonster m2 = new NecrototemMinion(-233, -12f);

                addToBot(new SpawnMonsterAction(m1, true));
                UC.doPow(m1, m1, new CursedTotemPower(m1), false);
                UC.doPow(m1, m1, new StrengthTotemPower(m1, strengthBoost), false);

                addToBot(new SpawnMonsterAction(m2, true));
                UC.doPow(m2, m2, new CursedTotemPower(m2), false);
                UC.doPow(m2, m2, new StrengthTotemPower(m2, strengthBoost), false);

                moves.get(NAILS).multiplier += 2;
                break;
            }
            case CURSE: {
                UC.doVfx(new ShockWaveEffect(hb.cX, hb.cY, new Color(0.3f, 0.2f, 0.4f, 1f), ShockWaveEffect.ShockWaveType.CHAOTIC), 0.3f);
                addToBot(new DamageAction(UC.p(), info, AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                UC.doPow(this, UC.p(), new DrawReductionPower(UC.p(), 1), false);
                break;
            }
            case NAILS: {
                float angle = new Vector2(UC.p().hb.cX, UC.p().hb.cY).sub(new Vector2(hb.cX, hb.y + hb.height + 120 * Settings.scale)).angle() + 90;
                for (int i = 0; i < multiplier; ++i) {
                    UC.doVfx(
                            new VfxBuilder(ImageMaster.vfxAtlas.findRegion("combat/stake"), hb.cX, hb.cY, 0.4f)
                                    .setColor(Color.PURPLE)
                                    .scale(0f, 0.7f, VfxBuilder.Interpolations.SWING)
                                    .moveY(hb.cY, hb.y + hb.height + 120 * Settings.scale)
                                    .rotateTo(MathUtils.random(-45f, 45f), angle, VfxBuilder.Interpolations.EXP10IN)
                                    .andThen(0.2f)
                                    .moveX(hb.cX, UC.p().hb.cX, VfxBuilder.Interpolations.EXP10IN)
                                    .moveY(hb.y + hb.height + 120 * Settings.scale, UC.p().hb.cY, VfxBuilder.Interpolations.EXP10IN)
                                    .playSoundAt(0.18f, "BLUNT_HEAVY")
                                    .build(),
                    0.4f);
                    addToBot(new DamageAction(UC.p(), info, AbstractGameAction.AttackEffect.NONE));
                }
                break;
            }
        }

        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        byte move;

        if (firstMove || noMinionsAlive()) {
            move = SUMMON;
            firstMove = false;
        } else if (lastMove(SUMMON) || lastMove(CURSE)) {
            move = NAILS;
        } else if (lastTwoMoves(NAILS)) {
            move = CURSE;
        } else if (num < 40) {
            move = CURSE;
        } else {
            move = NAILS;
        }

        setMoveShortcut(move, MOVES[move]);
    }

    private boolean noMinionsAlive()
    {
        long count = UC.getAliveMonsters().stream()
                .filter(m -> NecrototemMinion.ID.equals(m.id))
                .count();
        return count <= 0;
    }

    @Override
    public void damage(DamageInfo info)
    {
        if (info.output > 1 && hasPower(CursedTotemPower.POWER_ID)) {
            info.output = 1;
        }
        super.damage(info);
    }

    @Override
    public void die(boolean triggerRelics)
    {
        super.die(triggerRelics);
        UC.getAliveMonsters().stream()
                .filter(m -> m.hasPower(MinionPower.POWER_ID))
                .forEach(m -> addToBot(new SuicideAction(m, false)));
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);

        if (MathUtils.random(100) < 10) {
            animX = MathUtils.random(-10f, 5f);
            if (animX <= -9.9f) {
                animX = -15f;
            } else if (animX < -5f) {
                animX = -5f;
            }
            animX *= Settings.scale;
            animY = MathUtils.random(-3f, 3f) * Settings.scale;
        }
        renderFace(sb);
        animX = 0;
        animY = 0;
    }


    private float renderTimer = 0;
    private void renderFace(SpriteBatch sb)
    {
        renderTimer += Gdx.graphics.getRawDeltaTime() * MathUtils.random(0.5f, 2f);

        sb.end();
        shader.begin();
        shader.setUniformf("timer", renderTimer);
        shader.setUniformf("fadeIn", 1);
        shader.setUniformf("white", animX < -4f * Settings.scale ? 0.25f : 0f);
        sb.setShader(shader);
        sb.begin();

        sb.setColor(tint.color);
        sb.draw(
                faceImg,
                drawX - faceImg.getWidth() * Settings.scale / 2f + animX,
                drawY + animY + AbstractDungeon.sceneOffsetY,
                faceImg.getWidth() * Settings.scale,
                faceImg.getHeight() * Settings.scale,
                0,
                0,
                faceImg.getWidth(),
                faceImg.getHeight(),
                flipHorizontal,
                flipVertical
        );

        sb.end();
        shader.end();
        sb.setShader(null);
        sb.begin();
        sb.setColor(Color.WHITE);
    }
}