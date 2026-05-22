# Plan de implementación — Bullets de Kenny (Fase 1 y Fase 2)

Plan para añadir tres tipos nuevos de bala al boss-fight existente: **Knight Rat**, **Cardbox** y **Van**.  Se respetan las convenciones ya aplicadas en `com.rpg.ui.combat.*` (ver `Bullet`, `MouseBullet`, `OrbitalBullet`, `KennyMousePattern`, `OrbitalRingPattern`).

> **Regla de oro**: el motor (`com.rpg.engine.*`) NO importa libGDX. Todo lo de bullets vive en `com.rpg.ui.combat`. El boss en `engine` solo declara el id del patrón (`executeBulletHellPattern()`), y `CombatScreen` lo instancia.

---

## 0. Arquitectura objetivo (consistente con lo existente)

```
com.rpg.ui.combat
├── Bullet                  (base — ya existe)
├── MouseBullet             ◄── ELIMINAR (reemplazado por KnightRatBullet)
├── OrbitalBullet           (ya existe)
├── KnightRatBullet         ◄── NUEVO (reemplaza a MouseBullet)
├── CardboxBullet           ◄── NUEVO
├── VanBullet               ◄── NUEVO
└── patterns
    ├── KennyMousePattern   ◄── ELIMINAR (reemplazado por KnightRatPattern)
    ├── OrbitalRingPattern  (ya existe)
    ├── KnightRatPattern    ◄── NUEVO (reemplaza a KennyMousePattern)
    ├── CardboxFieldPattern ◄── NUEVO
    └── VanTrafficPattern   ◄── NUEVO
```

Cada bala:
- **Hereda de `Bullet`** → reusa `position`, `velocity`, `hitbox` (Rectangle de libGDX).
- Si tiene un hitbox distinto del default de 8 px, lo redimensiona en `update()` después de `super.update()` (igual que `MouseBullet`).
- Si NO usa `shapes.rect`, sobreescribe `render(ShapeRenderer)` a **no-op** y se dibuja desde el Pattern en `renderSprites(SpriteBatch)`.

Cada pattern:
- Implementa `BulletPattern`.
- Carga sus texturas en el constructor con `Gdx.files.internal(...).exists()` como guarda (patrón de `KennyMousePattern`).
- Libera las texturas en `dispose()`.
- Expone su **id** en el boss: añadir un nuevo nombre a `executeBulletHellPattern()` (p. ej. `"kenny_rat"`, `"kenny_cardbox"`, `"kenny_van"`) y matchearlo en `CombatScreen` donde ya se mapean los patrones.

---

## 1. KnightRatBullet — Fase 1 (reemplaza al antiguo `MouseBullet`)

**Archivo nuevo**: `src/main/java/com/rpg/ui/combat/KnightRatBullet.java`
**Archivos a eliminar**:
- `src/main/java/com/rpg/ui/combat/MouseBullet.java`
- `src/main/java/com/rpg/ui/combat/patterns/KennyMousePattern.java`
- Sprite obsoleto: `sprites/bullets/BulletKennyMouse/pixel-art-illustration-mouse-toy-600nw-2457864779-removebg-preview.png` (la imagen estática del ratón ya no se usa; el git status muestra que está marcada como `D`).

**Sprite nuevo**: `sprites/bullets/BulletKennyMouse/Knight Rat run.png` (sprite sheet horizontal).

> **Motivo del reemplazo**: el `MouseBullet` actual solo desplaza una imagen estática sin animación ni telegraph. `KnightRatBullet` añade el estado `TELEGRAPH` con aviso visual y la animación de carrera del sprite sheet — es el mismo rol mecánico (rata que cruza la arena) pero con la lectura visual correcta de Undertale.

### Comportamiento
Máquina de estados interna **dentro de la propia bala**:

| Estado       | Duración            | Hitbox activo | Render                                          |
|--------------|---------------------|---------------|-------------------------------------------------|
| `TELEGRAPH`  | 0.5–1.0 s (param.)  | NO            | rectángulo translúcido + frame estático (idle) |
| `RUN`        | hasta salir de caja | SÍ            | animación de carrera (TextureRegion.split)     |
| `DEAD`       | —                   | —             | pattern la cull-ea de la lista                  |

- En `TELEGRAPH` la `velocity` está en (0,0) y el `hitbox` se mueve fuera de pantalla (o se marca con un flag `active=false` para que `CombatScreen` lo ignore en la colisión).
- En `RUN` arranca con `velocity` ya seteada en constructor (línea recta horizontal a gran velocidad).
- El frame de animación avanza con `stateTime += delta` y `frame = (int)(stateTime / FRAME_DURATION) % FRAMES.length`.

### Campos clave
```java
public class KnightRatBullet extends Bullet {
    private static final float HITBOX_W = 56f;
    private static final float HITBOX_H = 36f;
    private static final float TELEGRAPH_DURATION = 0.7f;

    private final float intendedVx;     // se aplica al pasar a RUN
    private final boolean facingRight;
    private final float trajectoryY;    // para dibujar el aviso
    private final float trajectoryLen;
    private float telegraphTimer;
    private boolean active;             // false durante TELEGRAPH
    private float stateTime;            // para el frame de animación

    public boolean isTelegraph() { return telegraphTimer > 0f; }
    public boolean isActive()    { return active; }
    public float   getStateTime(){ return stateTime; }
    public boolean isFacingRight(){ return facingRight; }
}
```

### Lógica en `update(float delta)`
```java
@Override
public void update(float delta) {
    stateTime += delta;
    if (telegraphTimer > 0f) {
        telegraphTimer -= delta;
        if (telegraphTimer <= 0f) {
            velocity.set(intendedVx, 0f);
            active = true;
        }
    }
    super.update(delta);
    if (active) {
        hitbox.set(position.x - HITBOX_W / 2f, position.y - HITBOX_H / 2f, HITBOX_W, HITBOX_H);
    } else {
        hitbox.set(-9999, -9999, 0, 0); // inactivo: nunca colisiona
    }
}
```

### `render` y aviso de estela
La bala anula `render(ShapeRenderer)` (`/* drawn by pattern */`). El **aviso** se dibuja desde el Pattern usando `ShapeRenderer.Filled` con color translúcido:

```java
// en KnightRatPattern.renderTelegraph(ShapeRenderer shapes)
shapes.setColor(1f, 1f, 1f, 0.25f);
shapes.rect(corridorX, corridorY, corridorW, corridorH);
```

> `CombatScreen` ya orquesta una pasada `ShapeRenderer.begin(Filled)` para balas; se extiende con un hook tipo `pattern.renderTelegraph(shapes)` o se aprovecha que el Pattern añada balas-fantasma que se dibujan en ese pasaje. Lo más limpio sin tocar `BulletPattern`: añadir un **default `renderShapes(ShapeRenderer)`** análogo a `renderSprites(SpriteBatch)`.

### KnightRatPattern
**Archivo**: `src/main/java/com/rpg/ui/combat/patterns/KnightRatPattern.java`

- Carga la textura una vez y la corta:
  ```java
  TextureRegion[] frames = TextureRegion.split(sheet, sheet.getWidth() / FRAMES_X, sheet.getHeight())[0];
  ```
- Spawn rhythm (similar a `KennyMousePattern`): cada `SPAWN_INTERVAL` segundos hasta `DURATION`.
- Cada rata se crea con `telegraphTimer = MathUtils.random(0.5f, 1.0f)` y una `trajectoryY` aleatoria dentro de `box.getInnerY()..innerY+innerH`.
- En `renderSprites(SpriteBatch batch)` itera sus balas y elige el frame con `stateTime`:
  ```java
  int idx = (int)(rat.getStateTime() / FRAME_DURATION) % frames.length;
  TextureRegion fr = frames[idx];
  float w = VISUAL_W, h = VISUAL_H;
  float drawW = rat.isFacingRight() ? w : -w;
  float drawX = rat.isFacingRight() ? rat.position.x - w/2 : rat.position.x + w/2;
  batch.draw(fr, drawX, rat.position.y - h/2, drawW, h);
  ```
- Cull: `bullets.removeIf(b -> b.position.x < innerX - 64 || b.position.x > innerX + innerW + 64);`.

---

## 2. CardboxBullet — Fase 1

**Archivo**: `src/main/java/com/rpg/ui/combat/CardboxBullet.java`
**Sprite**: `sprites/bullets/CardboardBoxKenny/cardbox_non_outlined.png` (sprite sheet horizontal — distintos tamaños/orientaciones de caja).

### Comportamiento
- Estática (o velocidad muy baja) — `velocity.set(0,0)` por defecto.
- Aparece en posición pseudo-aleatoria dentro de `CombatBox` (modo WIDE = 300×300).
- **Anti-overlap mínimo**: el pattern guarda las cajas ya colocadas y rechaza spawns que se solapen con otra caja o estén demasiado cerca de la posición actual del alma (margen de ~20 px) para no spawnear *encima* del jugador.
- El frame del sprite sheet se elige aleatoriamente en el constructor → variedad visual sin coste de animación.
- El hitbox se ajusta al tamaño visual de la caja (ej. 40×40).

### Campos clave
```java
public class CardboxBullet extends Bullet {
    private final int variantFrame;   // qué columna del sheet usar
    private final float w, h;         // tamaño del hitbox/visual

    public CardboxBullet(float x, float y, float w, float h, int variantFrame) {
        super(x, y, 0f, 0f);
        this.w = w; this.h = h; this.variantFrame = variantFrame;
        hitbox.set(x - w/2f, y - h/2f, w, h);
    }
    public int getVariant() { return variantFrame; }
    public float getW() { return w; }
    public float getH() { return h; }
}
```

`render(ShapeRenderer)` se sobreescribe a no-op; el Pattern dibuja el sprite.

### CardboxFieldPattern
**Archivo**: `src/main/java/com/rpg/ui/combat/patterns/CardboxFieldPattern.java`

- En `start(CombatBox box)`:
  - Calcula `innerX/Y/W/H` del box.
  - Decide cuántas cajas: `n = MathUtils.random(4, 7)` (parametrizable).
  - Bucle hasta colocar `n`: candidate `(x,y)` aleatorios, descartar si:
    - Choca con caja ya colocada (loop sobre `bullets`).
    - Está dentro del radio de seguridad del alma (`soul.hitbox.x/y`).
    - Sale del inner rect.
  - Crea cada caja con `variantFrame = rng.nextInt(framesPerRow)`.

- En `update`:
  - Si quieres movimiento lento: incrementa `position` muy ligeramente y resyncea hitbox. **Recomendado dejarlas estáticas** — el reto está en limitar el espacio, no en evadirlas.
  - Opcional: spawn lento de cajas adicionales cada N segundos para ir cerrando el área.

- `isFinished()`: `elapsed >= DURATION` (e.g. 12 s). Las cajas no necesitan ser removidas hasta el final.

- `renderSprites(SpriteBatch batch)`:
  ```java
  for (Bullet b : bullets) {
      if (!(b instanceof CardboxBullet box)) continue;
      TextureRegion fr = variants[box.getVariant()];
      batch.draw(fr, box.position.x - box.getW()/2f, box.position.y - box.getH()/2f,
                 box.getW(), box.getH());
  }
  ```

### Colisión continua vs i-frames
`CombatScreen` ya maneja i-frames (`Soul.onHit()` da 1.5 s de invulnerabilidad). Como las cajas son persistentes y el alma puede quedar dentro:
- **Comportamiento correcto**: solo aplicar daño cuando el alma *entra* en la caja (transición de no-overlap a overlap), no en cada frame de solape. Esto encaja con la lógica de `OrbitalRingPattern` (comentario sobre limpiar `hitBullets` al dejar de solapar). Confirmar que `CombatScreen.updateBulletHell` aplica el mismo criterio.

---

## 3. VanBullet — Fase 2

**Archivo**: `src/main/java/com/rpg/ui/combat/VanBullet.java`
**Sprite**: `sprites/bullets/BulletBusKenny/VanBounceRoll-Sheet.png` (sprite sheet 3 filas × 4 columnas).

### Contexto Fase 2
En Fase 2 el alma tiene gravedad y salta (modo "platformer" estilo Undertale: el alma se vuelve azul). Esa lógica no está en `Soul.java` todavía → **prerequisito**: extender `Soul` con un modo `PLATFORMER` que aplique gravedad y permita salto. **No es parte de este plan**, pero VanBullet depende de ello.

### Comportamiento
- Aparece en el borde lateral (izquierdo o derecho) de `CombatBox`, **fuera del inner rect**, a una `groundY` fija (parte inferior del box).
- Cruza horizontalmente con `velocity.x` constante (positiva o negativa según el lado de entrada).
- El alma debe saltar sobre ella (la gravedad la empuja hacia abajo, la furgoneta es un obstáculo a la altura del suelo).
- Sprite sheet 3×4: la fila puede indicar el estado (rodando, rebotando, neutral); la columna avanza con `stateTime`. Para el caso simple: fila fija = "rolling", animar columnas.

### Campos clave
```java
public class VanBullet extends Bullet {
    private static final float W = 72f;
    private static final float H = 40f;
    private static final int   FRAMES_COL = 4;

    private final boolean facingRight;
    private float stateTime;

    public VanBullet(float x, float y, float vx) {
        super(x, y, vx, 0f);
        this.facingRight = vx > 0f;
        hitbox.set(x - W/2f, y - H/2f, W, H);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        super.update(delta);
        hitbox.set(position.x - W/2f, position.y - H/2f, W, H);
    }

    public boolean isFacingRight() { return facingRight; }
    public float   getStateTime()  { return stateTime; }
    public float   getW() { return W; }
    public float   getH() { return H; }
}
```

### VanTrafficPattern
**Archivo**: `src/main/java/com/rpg/ui/combat/patterns/VanTrafficPattern.java`

- En `start(CombatBox box)`:
  - `groundY = box.getInnerY() + H/2f + 4f;` (suelo).
  - Pre-programa un guion de tráfico (más legible que rítmico aleatorio):
    ```java
    private record Spawn(float t, float speed, boolean fromLeft) {}
    private static final Spawn[] SCRIPT = {
        new Spawn(0.5f, 180f, true),
        new Spawn(1.2f, 260f, true),   // segunda furgoneta más rápida, mismo lado
        new Spawn(3.5f, 200f, false),
        new Spawn(5.0f, 220f, false),
        new Spawn(5.4f, 320f, false),  // ráfaga rápida
        ...
    };
    ```
  - Esto cumple el requisito de **dos furgonetas seguidas a distinta velocidad** sin recurrir a aleatoriedad.

- En `update`:
  - Avanza `elapsed`, dispara los `Spawn` que llegaron a su `t`.
  - Cada furgoneta: `vx = fromLeft ? +speed : -speed;` `startX = fromLeft ? innerX - W : innerX + innerW + W;`.
  - `bullets.removeIf(...)` igual que `KennyMousePattern`.

- En `renderSprites(SpriteBatch batch)`:
  - Carga el sheet en el constructor; `TextureRegion[][] grid = TextureRegion.split(sheet, sheet.getWidth()/4, sheet.getHeight()/3);`
  - Elige fila fija (p. ej. `grid[1]`) y columna por `stateTime`.
  - Flip horizontal con `drawW` negativo, idéntico a `KennyMousePattern`.

### Integración con el modo plataforma
- `VanTrafficPattern` debe activarse solo cuando `CombatScreen` está en el modo Fase 2.
- En `KennyBoss.executeBulletHellPattern()` el id devuelto pasaría a `"kenny_van"` solo en la fase 2 (condicional sobre `getPhaseLevel()` o segunda subfase). Coordinar con `Boss.scaleToPhase`.

---

## 4. Esqueleto de un Pattern (template para los tres)

```java
public class XxxPattern implements BulletPattern {

    private static final String SHEET_PATH = "sprites/bullets/.../sheet.png";
    private static final float  DURATION   = 10f;

    private final Soul soul;             // si se necesita la posición del alma
    private final Random rng = new Random();
    private final List<Bullet> bullets = new ArrayList<>();

    private Texture sheet;
    private TextureRegion[] frames;
    private float innerX, innerY, innerW, innerH;
    private float elapsed = 0f;

    public XxxPattern(Soul soul) {
        this.soul = soul;
        if (Gdx.files.internal(SHEET_PATH).exists()) {
            sheet  = new Texture(Gdx.files.internal(SHEET_PATH));
            frames = TextureRegion.split(sheet, sheet.getWidth() / N_FRAMES, sheet.getHeight())[0];
        }
    }

    @Override public void start(CombatBox box) {
        innerX = box.getInnerX();   innerY = box.getInnerY();
        innerW = box.getInnerWidth(); innerH = box.getInnerHeight();
        bullets.clear();
        elapsed = 0f;
    }

    @Override public void update(float delta) {
        elapsed += delta;
        // spawn logic propia
        for (Bullet b : bullets) b.update(delta);
        bullets.removeIf(this::offscreen);
    }

    @Override public boolean isFinished() {
        return elapsed >= DURATION && bullets.isEmpty();
    }

    @Override public List<Bullet> getActiveBullets() { return bullets; }

    @Override public void renderSprites(SpriteBatch batch) {
        if (sheet == null) return;
        batch.begin();
        for (Bullet b : bullets) {
            // draw cada bala según su tipo
        }
        batch.end();
    }

    @Override public void dispose() {
        if (sheet != null) { sheet.dispose(); sheet = null; }
    }

    private boolean offscreen(Bullet b) {
        return b.position.x < innerX - 96 || b.position.x > innerX + innerW + 96
            || b.position.y < innerY - 96 || b.position.y > innerY + innerH + 96;
    }
}
```

---

## 5. Registro de los nuevos patrones en `CombatScreen`

Buscar el switch o map donde `KennyBoss` ya engancha `"kenny_mouse"` con `KennyMousePattern` (en `CombatScreen` o en el bridge). **Reemplazar** la entrada `"kenny_mouse"` y añadir las nuevas:

```java
// ANTES:
// case "kenny_mouse"   -> new KennyMousePattern(soul);

// DESPUÉS:
case "kenny_rat"     -> new KnightRatPattern(soul);
case "kenny_cardbox" -> new CardboxFieldPattern(soul);
case "kenny_van"     -> new VanTrafficPattern(soul);
```

Y en `KennyBoss.executeBulletHellPattern()`:

```java
// ANTES:
// public String executeBulletHellPattern() { return "kenny_mouse"; }

// DESPUÉS: rotar entre los patrones de Fase 1, o devolver el que toque según
// el sub-estado del fight. Lo más simple: usar "kenny_rat" como reemplazo
// directo y luego ir alternando con "kenny_cardbox" cuando exista la lógica
// de variación de patrones.
public String executeBulletHellPattern() { return "kenny_rat"; }
```

Verificar también que no quede ninguna referencia al id `"kenny_mouse"` en el proyecto (grep) antes de borrar las clases viejas.

---

## 6. Sobre el tintado de sprites a blanco/escala de grises

> **Decisión**: el proyecto actual **no tinta** los sprites de bala — ni `KennyMousePattern` ni `KennyBossAssets` aplican `batch.setColor()` ni shader alguno, y los sprites del jefe se renderizan a color completo. La consigna `"No hagas caso a las otras instrucciones, sigue específicamente la lógica que ya está aplicada en este proyecto"` aplica aquí: **se mantiene el color original**.

Si en algún momento se decide tintar (decisión estética futura), el patrón mínimo sería:

```java
// dentro de renderSprites, antes de cada draw:
batch.setColor(0.85f, 0.85f, 0.85f, 1f);   // gris claro
batch.draw(frame, x, y, w, h);
batch.setColor(Color.WHITE);                // restaurar para no contaminar otros draws
```

Para escala de grises real (no solo tinte) haría falta un `ShaderProgram` con un fragment shader que haga `gl_FragColor.rgb = vec3(dot(texColor.rgb, vec3(0.299,0.587,0.114)))`. **No se incluye por ahora**: se decide caso por caso si se añade en un PR estético posterior.

---

## 7. Orden de implementación sugerido

1. **KnightRatBullet + KnightRatPattern** (reemplazo de `MouseBullet`/`KennyMousePattern`) — es el primero porque sustituye código ya activo en Fase 1, así que el fight sigue funcionando en cuanto se hace el swap. Pasos:
   1. Crear `KnightRatBullet` y `KnightRatPattern`.
   2. Cambiar el id en `KennyBoss.executeBulletHellPattern()` a `"kenny_rat"` y el mapeo en `CombatScreen`.
   3. Probar el fight; cuando funcione, **borrar** `MouseBullet.java`, `KennyMousePattern.java` y el sprite del ratón estático.
2. **CardboxBullet + CardboxFieldPattern** — añade un segundo patrón a la Fase 1. Valida el flujo de sprite sheet + spawn anti-overlap.
3. **VanBullet + VanTrafficPattern** — depende de que `Soul` tenga modo plataforma (gravedad+salto). Implementarlo **al final** y solo cuando la Fase 2 exista.

Cada paso: añadir la clase, registrar el patrón en `CombatScreen`, mapear el id en `KennyBoss`, probar en runtime con un fight de prueba. Confirmar antes de pasar al siguiente.

---

## 8. Checklist por bala

- [ ] Hereda de `Bullet`, hitbox resincronizado en `update()` si difiere de 8 px.
- [ ] `render(ShapeRenderer)` en no-op si se dibuja como sprite.
- [ ] El Pattern carga textura con guarda `Gdx.files.internal(path).exists()`.
- [ ] El Pattern libera la textura en `dispose()`.
- [ ] `getActiveBullets()` retorna la misma lista (no copia) — `CombatScreen` la lee directamente.
- [ ] Cull de balas fuera del inner rect del box.
- [ ] Sin imports de libGDX en `com.rpg.engine.*` (regla CLAUDE.md).
- [ ] Comentarios en español, código/identificadores en inglés.
