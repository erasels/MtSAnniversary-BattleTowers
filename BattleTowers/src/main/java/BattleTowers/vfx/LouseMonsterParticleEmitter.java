package BattleTowers.vfx;

import BattleTowers.interfaces.SpireElement;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class LouseMonsterParticleEmitter implements SpireElement {

    private static final float SPAWN_DELAY = 0.15f;

    private int maxSize;
    private float spawnTimer = 0;
    private Vector2 pos;

    private final ArrayList<LouseMonsterParticle> backgroundLouse = new ArrayList<>();
    private final ArrayList<LouseMonsterParticle> effectsLouse = new ArrayList<>();
    private final ArrayList<LouseMonsterParticle> foregroundLouse = new ArrayList<>();

    private ArrayList<ArrayList<LouseMonsterParticle>> louseList = new ArrayList<>();

    public LouseMonsterParticleEmitter(int maxSize, Vector2 pos) {
        this.maxSize = maxSize;
        this.pos = pos;

        louseList.add(backgroundLouse);
        louseList.add(effectsLouse);
        louseList.add(foregroundLouse);
    }

    public void setAnimation(String name) {
        louseList.forEach(list -> list.forEach(louse -> {
            louse.setAnimationState(name);
        }));
    }

    public void throwLouse() {
        LouseMonsterParticle middleLouse = effectsLouse.get(Math.round(effectsLouse.size() / 2.0f));

        middleLouse.hitPlayer();
        middleLouse.setAnimationState("force_curl");
    }

    public void takeDamage() {
        if (effectsLouse.size() == maxSize) {
            LouseMonsterParticle louse = effectsLouse.get(Math.round(effectsLouse.size() / 2f) + 1);
            louse.takeDamage();
            louse.setAnimationState("force_curl");
        }
    }

    @Override
    public void update() {
        spawnTimer -= Gdx.graphics.getRawDeltaTime();

        for (ArrayList<LouseMonsterParticle> list : louseList) {
            if (list.size() < maxSize && spawnTimer <= 0) {
                list.add(new LouseMonsterParticle(pos, 200f));
                list.sort(new LouseMonsterParticle.LouseMonsterParticleComparator());
            }

            list.forEach(LouseMonsterParticle::update);
            list.removeIf(LouseMonsterParticle::isDead);
        }

        if (spawnTimer < 0) {
            spawnTimer = SPAWN_DELAY;
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        backgroundLouse.forEach(louse -> louse.render(sb));
        effectsLouse.forEach(louse -> louse.render(sb));
        foregroundLouse.forEach(louse -> louse.render(sb));
    }
}
