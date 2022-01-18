package BattleTowers.util;

import com.badlogic.gdx.math.Vector2;

public class TrigHelper {
    public static Vector2 generateRandomPointOnCircle(float x, float y, float radius) {
        float angle = (float)Math.random() * (float)Math.PI * 2f;
        float pX = (float)(Math.cos(angle) * radius) + x;
        float pY = (float)(Math.sin(angle) * radius) + y;
        return new Vector2(pX, pY);
    }

    public static Vector2 generateRandomPointOnCircle(Vector2 pos, float radius) {
        return generateRandomPointOnCircle(pos.x, pos.y, radius);
    }
}
