package com.rpg.ui.combat;

import java.util.List;

public interface BulletPattern {
    void start(CombatBox box);
    void update(float delta);
    boolean isFinished();
    List<Bullet> getActiveBullets();
}
