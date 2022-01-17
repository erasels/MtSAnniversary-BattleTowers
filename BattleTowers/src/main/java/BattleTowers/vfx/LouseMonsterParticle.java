package BattleTowers.vfx;

import BattleTowers.interfaces.SpireElement;
import BattleTowers.util.TrigHelper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;

import static com.megacrit.cardcrawl.core.AbstractCreature.sr;

public class LouseMonsterParticle implements SpireElement {

    public Vector2 pos;
    private Vector2 startPos;
    private TextureAtlas atlas;
    private AnimationStateData stateData;
    private AnimationState state;
    private Skeleton skeleton;
    private Color color = Color.WHITE.cpy();
    private float rotation = 0f;
    private float rotationSpeed = 0f;
    private float life = 10000f;
    private float maxLife = life;
    private float scale = 1f;
    private LouseType type = getRandomLouseType();

    private ArrayList<Animation> animations = new ArrayList<>();

    public LouseMonsterParticle(Vector2 pos, float maxDist) {
        float randomDist = MathUtils.random(0f, maxDist);
        this.pos = TrigHelper.generateRandomPointOnCircle(pos, randomDist);
        this.startPos = pos.cpy();
        this.pos.y = this.pos.y / 2 + this.pos.y / 4;
        initShapeData();
        skeleton.getRootBone().setRotation(rotation);
        AnimationState.TrackEntry trackEntry = state.setAnimation(0, "idle", true);
        trackEntry.setTime(trackEntry.getEndTime() * MathUtils.random());
        enterScreen();
    }

    private void initShapeData() {
        atlas = getAtlas(type.textureName);
        SkeletonJson json = new SkeletonJson(this.atlas);
        json.setScale(Settings.scale);
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal(getSkeletonData(type.textureName)));
        skeleton = new Skeleton(skeletonData);
        skeleton.setColor(color);
        stateData = new AnimationStateData(skeletonData);
        state = new AnimationState(stateData);
    }

    private TextureAtlas getAtlas(String name) {
       return new TextureAtlas(
               Gdx.files.internal("images/monsters/theBottom/" + name + "/skeleton.atlas")
       );
    }

    private String getSkeletonData(String name) {
        return "images/monsters/theBottom/" + name + "/skeleton.json";
    }

    public void hitPlayer() {
        animations.add(new Animation(Interpolation.swingIn, 2f, (ani, interp) -> {
            pos.x = ani.easing.apply(startPos.x, AbstractDungeon.player.hb.cX, interp);
            pos.y = ani.easing.apply(startPos.y, AbstractDungeon.player.hb.cY, interp);
        }));
        animations.add(new Animation(Interpolation.exp5Out, 2f, (ani, interp) -> {
            pos.x = ani.easing.apply(AbstractDungeon.player.hb.cX, Settings.WIDTH / 2f, interp);
            pos.y = ani.easing.apply(AbstractDungeon.player.hb.cY, Settings.HEIGHT + 64f * Settings.scale, interp);
        }));
    }

    public void enterScreen() {
        final Vector2 sPos = new Vector2(Settings.WIDTH, Settings.HEIGHT);
        final Vector2 endPos = pos.cpy();

        animations.add(new Animation(Interpolation.exp5In, 1f, (ani, interp) -> {
            pos.x = ani.easing.apply(sPos.x, endPos.x, interp);
            pos.y = Interpolation.bounceOut.apply(sPos.y, endPos.y, interp);
        }));
    }

    public void takeDamage() {
        animations.add(new Animation(Interpolation.linear, 5f, (ani, interp) -> {
            pos.x = ani.easing.apply(startPos.x, Settings.WIDTH + 64f * Settings.scale, interp);
            pos.y = ani.easing.apply(startPos.y, Settings.HEIGHT / 2f, interp);
        }));
    }

    public void setDying() {
        life = 0f;
    }

    public boolean isDead() {
        return life <= 0f || (
            pos.y > Settings.HEIGHT ||
            pos.y < 0 ||
            pos.x > Settings.WIDTH ||
            pos.x < 0
        );
    }

    public void setAnimationState(String name) {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                switch (name.toLowerCase()) {
                    case "curlup":
                        state.setAnimation(0, "transitiontoclosed", false);
                        state.addAnimation(0, "idle closed", true, 0f);
                        break;
                    case "open":
                        state.setAnimation(0, "transitiontoopened", false);
                        state.addAnimation(0, "idle", true, 0f);
                        break;
                    case "attack":
                        state.setAnimation(0, "rear", false);
                        state.addAnimation(0, "idle", true, 0f);
                        break;
                    case "attack_curled":
                        state.setAnimation(0, "transitiontoopened", false);
                        state.addAnimation(0, "rear", false, 0f);
                        state.addAnimation(0, "idle", true, 0f);
                    case "force_curl":
                        state.setAnimation(0, "idle closed", true);
                        break;
                }
            }
        };

        timer.schedule(task, MathUtils.random(500));
    }

    @Override
    public void update() {
        float deltaTime = Gdx.graphics.getRawDeltaTime();

        rotation += rotationSpeed * deltaTime;

        if (animations.size() > 0) {
            animations.get(0).update();
            animations.removeIf(Animation::done);
        }

        life -= deltaTime;
        life = MathUtils.clamp(life, 0f, maxLife);

        state.update(deltaTime);
        state.apply(skeleton);

        skeleton.setPosition(pos.x, pos.y);
        skeleton.getRootBone().setRotation(rotation);
        skeleton.setColor(color);

        skeleton.setFlip(false, false);
//        skeleton.findBone("shadow").setScale(0f);
        skeleton.updateWorldTransform();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.end();
        CardCrawlGame.psb.begin();
        sr.draw(CardCrawlGame.psb, skeleton);
        CardCrawlGame.psb.end();
        sb.begin();
        sb.setBlendFunction(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
    }

    public enum LouseType {
        NORMAL("louseRed"),
        DEFENSIVE("louseGreen");

        public final String textureName;

        LouseType(String textureName) {
            this.textureName = textureName;
        }
    }

    private static LouseType getRandomLouseType() {
        return MathUtils.randomBoolean() ? LouseType.NORMAL : LouseType.DEFENSIVE;
    }

    private class Animation {
        private BiConsumer<Animation, Float> onUpdate;
        public Interpolation easing;
        public float speed;
        public float interp = 0;

        public Animation(Interpolation easing, float speed, BiConsumer<Animation, Float> consumer) {
            this.onUpdate = consumer;
            this.easing = easing;
            this.speed = speed;
        }

        public void update() {
            float deltaTime = Gdx.graphics.getRawDeltaTime();

            interp += deltaTime * speed;

            if (interp > 1f) {
                interp = 1f;
            }

            onUpdate.accept(this, interp);
        }

        public boolean done() {
            return interp >= 1f;
        }
    }

    public static class LouseMonsterParticleComparator implements Comparator<LouseMonsterParticle> {
        @Override
        public int compare(LouseMonsterParticle a, LouseMonsterParticle b) {
            return Float.compare(b.pos.y, a.pos.y);
        }
    }
}
