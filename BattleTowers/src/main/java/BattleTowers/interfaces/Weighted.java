package BattleTowers.interfaces;

import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.random.Random;

import java.util.Collection;
import java.util.Iterator;

public interface Weighted {
    static <T extends Weighted> T roll(Collection<T> set, Random rng) {
        float sum = 0;
        for (T obj : set)
            sum += obj.getWeight();
        float roll = rng == null ? MathUtils.random(0.0f, sum) : rng.random(0.0f, sum);
        Iterator<T> objIterator = set.iterator();
        T obj = null;
        while (objIterator.hasNext()) {
            obj = objIterator.next();
            if (roll < obj.getWeight()) {
                return obj;
            }
            roll -= obj.getWeight();
        }
        return obj;
    }

    float getWeight();
}
