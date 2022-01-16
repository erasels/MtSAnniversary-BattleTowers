package BattleTowers.vfx;

import BattleTowers.interfaces.SpireElement;
import BattleTowers.util.TrigHelper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;

import static com.megacrit.cardcrawl.core.AbstractCreature.sr;

public class LouseMonsterParticle implements SpireElement {

    private Vector2 pos;
    private Vector2 vel = new Vector2(0f, 0f);
    private TextureAtlas atlas;
    private AnimationStateData stateData;
    private AnimationState state;
    private Skeleton skeleton;
    private Color color = Color.WHITE.cpy();
    private float rotation = 0f;
    private float rotationSpeed = 0f;
    private float life = MathUtils.random(15f, 25f);
    private float maxLife = life;
    private float scale = 1f;
    private LouseType type = getRandomLouseType();

    public LouseMonsterParticle(Vector2 pos, float maxDist) {
        float randomDist = MathUtils.random(0f, maxDist);
        this.pos = TrigHelper.generateRandomPointOnCircle(pos, randomDist);
        initShapeData();
        skeleton.getRootBone().setRotation(rotation);

        AnimationState.TrackEntry trackEntry = state.setAnimation(0, "idle", true);
        trackEntry.setTime(trackEntry.getEndTime() * MathUtils.random());
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

    @Override
    public void update() {
        float deltaTime = Gdx.graphics.getRawDeltaTime();

        rotation += rotationSpeed * deltaTime;

        life -= deltaTime;
        life = MathUtils.clamp(life, 0f, maxLife);

        state.update(deltaTime);
        state.apply(skeleton);

        pos.add(vel.scl(deltaTime).scl(Settings.scale));

        skeleton.setPosition(pos.x, pos.y);
        skeleton.getRootBone().setRotation(rotation);
        skeleton.setColor(color);

        // deal with scale stuff

        skeleton.setFlip(false, false);
        skeleton.findBone("shadow").setScale(0f);
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

    public void setDying() {
        life = 0f;
    }

    public boolean isDead() {
        return life <= 0f;
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
}
