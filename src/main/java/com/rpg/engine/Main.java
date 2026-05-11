package com.rpg.engine;

import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.combat.actions.ActAction;
import com.rpg.engine.combat.actions.FightAction;
import com.rpg.engine.combat.actions.ItemAction;
import com.rpg.engine.combat.actions.MercyAction;
import com.rpg.engine.combat.states.PlayerMenuState;
import com.rpg.engine.core.exceptions.ResourceNotFoundException;
import com.rpg.engine.core.exceptions.SaveCorruptionException;
import com.rpg.engine.entities.Boss;
import com.rpg.engine.entities.Character;
import com.rpg.engine.entities.Player;
import com.rpg.engine.items.Armor;
import com.rpg.engine.items.Consumable;
import com.rpg.engine.items.Weapon;
import com.rpg.engine.procedural.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    // -------------------------------------------------------------------------
    // Framework de consola
    // -------------------------------------------------------------------------

    private static int passed = 0;
    private static int failed = 0;

    static void check(String description, boolean condition) {
        if (condition) {
            System.out.println("  [OK]   " + description);
            passed++;
        } else {
            System.out.println("  [FAIL] " + description);
            failed++;
        }
    }

    static void checkThrows(String description, Runnable block) {
        try {
            block.run();
            System.out.println("  [FAIL] " + description + " (no lanzó excepción)");
            failed++;
        } catch (Exception e) {
            System.out.println("  [OK]   " + description + " (" + e.getClass().getSimpleName() + ")");
            passed++;
        }
    }

    static void section(String title) {
        System.out.println();
        System.out.println("================================================");
        System.out.println("  " + title);
        System.out.println("================================================");
    }

    static void subsection(String title) {
        System.out.println("  --- " + title + " ---");
    }

    // =========================================================================
    // MAIN
    // =========================================================================

    public static void main(String[] args) {
        System.out.println();
        System.out.println("  RPG ENGINE — Driver de consola");
        System.out.println("  Motor lógico estilo Undertale");

        testCharacter();
        testPlayer();
        testBoss();
        testItems();
        testBossFactory();
        testCombatManager();
        testHistoryAndStory();
        testPersistable();
        simulateFullGame();

        // --- Resumen final ---
        System.out.println();
        System.out.println("================================================");
        System.out.printf("  RESULTADO FINAL:  %d OK  |  %d FAIL%n", passed, failed);
        System.out.println("================================================");

        if (failed > 0) {
            System.out.println("  Revisa los [FAIL] anteriores.");
            System.exit(1);
        }
    }

    // =========================================================================
    // 1. CHARACTER
    // =========================================================================

    static void testCharacter() {
        section("1. Character — encapsulamiento de HP");

        // Subclase concreta mínima (Character es abstracto)
        Character c = new Boss("TestChar", 100, 1.0, 1, List.of());

        subsection("Constructor");
        check("HP inicial == maxHp",
                  c.getHp() == 100 && c.getMaxHp() == 100);
        check("Nombre correcto",
                      c.getName().equals("TestChar"));
        check("isAlive() == true al inicio",
          c.isAlive());

        checkThrows("maxHp = 0 lanza excepción",
            () -> new Boss("X", 0, 1.0, 1, List.of()));
        checkThrows("nombre vacío lanza excepción",
            () -> new Boss("", 100, 1.0, 1, List.of()));

        subsection("takeDamage");
        c = new Boss("Hero", 100, 1.0, 1, List.of());
        c.takeDamage(30);
        check("HP = 70 tras 30 de daño",      c.getHp() == 70);

        c.takeDamage(999);
        check("Clamp: HP nunca baja de 0",    c.getHp() == 0);
        check("isAlive() == false con HP=0",  !c.isAlive());

        c = new Boss("Hero", 100, 1.0, 1, List.of());
        c.takeDamage(-50);
        check("Daño negativo ignorado",        c.getHp() == 100);

        c.takeDamage(0);
        check("Daño cero ignorado",            c.getHp() == 100);

        subsection("heal");
        c.takeDamage(60); // HP = 40
        c.heal(20);
        check("HP = 60 tras curar 20",        c.getHp() == 60);

        c.heal(9999);
        check("Clamp: HP nunca supera maxHp", c.getHp() == 100);

        c.takeDamage(50);
        c.heal(-10);
        check("Curación negativa ignorada",    c.getHp() == 50);
    }

    // =========================================================================
    // 2. PLAYER
    // =========================================================================

    static void testPlayer() {
        section("2. Player — inventario, equipo y stats totales");

        Player p = new Player("Frisk", 100, 10, 5);

        subsection("Stats totales");
        check("getTotalAttack sin arma = baseAttack",    p.getTotalAttack() == 10);
        check("getTotalDefense sin armadura = baseDef",  p.getTotalDefense() == 5);

        Weapon sword = new Weapon("sword_01", "Espada", "Afilada.", 8);
        p.equipWeapon(sword);
        check("getTotalAttack con espada = 10+8 = 18",   p.getTotalAttack() == 18);

        Armor armor = new Armor("armor_01", "Coraza", "Pesada.", 7);
        p.equipArmor(armor);
        check("getTotalDefense con coraza = 5+7 = 12",   p.getTotalDefense() == 12);

        subsection("equipWeapon / equipArmor");
        Weapon sword2 = new Weapon("sword_02", "Espadón", "Mejor.", 15);
        Weapon displaced = p.equipWeapon(sword2);
        check("equipWeapon devuelve la previa",           displaced == sword);
        check("Nueva arma equipada correctamente",         p.getEquippedWeapon() == sword2);

        subsection("Inventario");
        Player p2 = new Player("Frisk", 100, 10, 5);
        Consumable potion = new Consumable("p1", "Poción", "Cura 30 HP.", 30);
        p2.addLoot(potion);
        check("addLoot añade al inventario",               p2.getInventory().size() == 1);

        p2.addLoot(null);
        check("addLoot ignora null",                       p2.getInventory().size() == 1);

        ArrayList<com.rpg.engine.items.Item> copy = p2.getInventory();
        copy.clear();
        check("getInventory() devuelve copia segura",      p2.getInventory().size() == 1);

        p = p2;

        subsection("useItem");
        p.takeDamage(50); // HP = 50
        p.useItem(potion);
        check("Consumable cura al usarse",                 p.getHp() == 80);
        check("Consumable se retira del inventario",       !p.getInventory().contains(potion));

        Weapon w = new Weapon("w_test", "Daga", "Pequeña.", 5);
        p.addLoot(w);
        p.useItem(w);
        check("Weapon se equipa al usarse",                p.getEquippedWeapon() == w);

        Consumable ghost = new Consumable("ghost", "Fantasma", "No está.", 10);
        int hpBefore = p.getHp();
        p.useItem(ghost); // no está en inventario
        check("useItem fuera de inventario es no-op",      p.getHp() == hpBefore);
    }

    // =========================================================================
    // 3. BOSS
    // =========================================================================

    static void testBoss() {
        section("3. Boss — escalado, mercy y prototipo");

        Boss boss = new Boss("Froggit", 100, 1.0, 1,
                List.of("¡Croac!", "¿Me perdonarás?", "Te observa."));

        subsection("scaleToPhase");
        boss.scaleToPhase(2);
        // 100 * (1 + 0.25*2) = 150
        check("HP escala a 150 en fase 2",                boss.getMaxHp() == 150);
        check("HP se resincroniza a maxHp tras escalar",  boss.getHp() == boss.getMaxHp());
        check("phaseLevel actualizado a 2",               boss.getPhaseLevel() == 2);
        // modifier: 1.0 * (1 + 0.15*2) = 1.30
        check("modifier escala a ~1.30",
            Math.abs(boss.getBaseDifficultyModifier() - 1.30) < 0.001);

        subsection("canBeSpared / registerActAttempt");
        boss = new Boss("Froggit", 100, 1.0, 1, List.of("¡Croac!"));
        check("No se puede perdonar sin ACTs",            !boss.canBeSpared());
        boss.registerActAttempt();
        boss.registerActAttempt();
        check("Tras 2 ACTs, aún no se puede perdonar",   !boss.canBeSpared());
        boss.registerActAttempt();
        check("Tras 3 ACTs, se puede perdonar",           boss.canBeSpared());
        check("actsPerformed = 3",                        boss.getActsPerformed() == 3);

        boss.setSpared(true);
        check("setSpared(true) funciona",                 boss.isSpared());

        subsection("nextDialogue");
        boss = new Boss("Froggit", 100, 1.0, 1,
                List.of("A", "B", "C"));
        String d1 = boss.nextDialogue();
        String d2 = boss.nextDialogue();
        String d3 = boss.nextDialogue();
        String d4 = boss.nextDialogue(); // vuelve al inicio
        check("Diálogos rotan correctamente",             d1.equals(d4));
        check("Diálogos son distintos",                   !d1.equals(d2) && !d2.equals(d3));

        Boss silent = new Boss("Silent", 50, 1.0, 1, List.of());
        check("Sin diálogos devuelve vacío",              silent.nextDialogue().isEmpty());

        subsection("dropLoot");
        boss = new Boss("Froggit", 100, 1.0, 3, List.of());
        var loot = boss.dropLoot();
        check("dropLoot no es null",                      loot != null);
        check("dropLoot es un Consumable",                loot instanceof Consumable);
        // healAmount = 20 + 3*10 = 50
        check("healAmount escala con fase (fase 3 → 50)", ((Consumable) loot).getHealAmount() == 50);

        subsection("copy — Patrón Prototype");
        boss = new Boss("Proto", 100, 1.0, 1, List.of("Hola"));
        Boss clone = boss.copy();
        check("copy() devuelve instancia distinta",       boss != clone);
        clone.takeDamage(clone.getMaxHp());
        check("Mutar clon NO afecta al prototipo",        boss.isAlive());
        clone = boss.copy();
        clone.scaleToPhase(4);
        check("Escalar clon NO cambia maxHp del proto",   boss.getMaxHp() == 100);

        subsection("computeAttackPower");
        boss = new Boss("Test", 100, 2.0, 1, List.of());
        // round(10.0 * 2.0) = 20
        check("computeAttackPower = round(10 × modifier)", boss.computeAttackPower() == 20);
    }

    // =========================================================================
    // 4. ITEMS
    // =========================================================================

    static void testItems() {
        section("4. Items — polimorfismo de uso");

        subsection("Consumable");
        Player p = new Player("Frisk", 100, 10, 5);
        Consumable pot = new Consumable("p1", "Poción", "Cura 25 HP.", 25);
        check("isSingleUse = true",          pot.isSingleUse());
        p.takeDamage(40);
        pot.use(p);
        check("use() cura al objetivo",      p.getHp() == 85);

        checkThrows("healAmount = 0 lanza excepción",
            () -> new Consumable("x", "X", "X", 0));

        subsection("Weapon");
        p = new Player("Frisk", 100, 10, 5);
        Weapon w = new Weapon("w1", "Espada", "Afilada.", 8);
        check("isSingleUse = false",         !w.isSingleUse());
        p.addLoot(w);
        p.useItem(w);
        check("use() equipa en el Player",   p.getEquippedWeapon() == w);
        check("getTotalAttack aumenta",      p.getTotalAttack() == 18);

        subsection("Armor");
        p = new Player("Frisk", 100, 10, 5);
        Armor a = new Armor("a1", "Coraza", "Pesada.", 6);
        check("isSingleUse = false",         !a.isSingleUse());
        p.addLoot(a);
        p.useItem(a);
        check("use() equipa en el Player",   p.getEquippedArmor() == a);
        check("getTotalDefense aumenta",     p.getTotalDefense() == 11);

        subsection("Weapon desplazada vuelve al inventario");
        Weapon old = new Weapon("w_old", "Daga", "Vieja.", 3);
        Weapon neo = new Weapon("w_neo", "Hacha", "Nueva.", 12);
        p = new Player("Frisk", 100, 10, 5);
        p.addLoot(old);
        p.useItem(old);        // equipa 'old'
        p.addLoot(neo);
        p.useItem(neo);        // equipa 'neo', 'old' vuelve al inventario
        check("Arma desplazada vuelve al inventario", p.getInventory().contains(old));
        check("Nueva arma equipada",                   p.getEquippedWeapon() == neo);
    }

    // =========================================================================
    // 5. BOSS FACTORY
    // =========================================================================

    static void testBossFactory() {
        section("5. BossFactory — Factory + Prototype con RNG");

        BossFactory factory = new BossFactory(new Random(42L));
        Boss proto = new Boss("Froggit", 100, 1.0, 1, List.of("¡Croac!"));
        factory.registerBoss(1, proto);

        subsection("poolSize");
        check("poolSize = 1 tras registrar",           factory.poolSize(1) == 1);
        check("poolSize = 0 para fase desconocida",    factory.poolSize(99) == 0);

        checkThrows("phaseLevel = 0 lanza excepción",
            () -> factory.registerBoss(0, proto));
        checkThrows("prototipo null lanza excepción",
            () -> factory.registerBoss(1, null));

        subsection("generateRandomBoss");
        try {
            checkThrows("Fase sin pool lanza ResourceNotFoundException",
                () -> {
                    try { factory.generateRandomBoss(99); }
                    catch (ResourceNotFoundException e) { throw new RuntimeException(e); }
                });

            Boss generated = factory.generateRandomBoss(1);
            check("Devuelve no-null",                      generated != null);
            check("Devuelve clon, no el prototipo",        generated != proto);

            generated.takeDamage(generated.getMaxHp());
            check("Mutar clon NO afecta al proto",         proto.isAlive());

            Boss g2 = factory.generateRandomBoss(1);
            // scaleToPhase(1): 100 * 1.25 = 125
            check("Boss escalado tiene maxHp > proto",     g2.getMaxHp() > proto.getMaxHp());
            check("Boss escalado empieza con HP completo", g2.getHp() == g2.getMaxHp());

        } catch (ResourceNotFoundException e) {
            System.out.println("  [FAIL] Excepción inesperada: " + e.getMessage());
            failed++;
        }

        subsection("Semilla fija → determinismo");
        factory.registerBoss(1, new Boss("Whimsun", 80, 0.8, 1, List.of()));
        BossFactory f1 = new BossFactory(new Random(42L));
        BossFactory f2 = new BossFactory(new Random(42L));
        f1.registerBoss(1, proto);
        f1.registerBoss(1, new Boss("Whimsun", 80, 0.8, 1, List.of()));
        f2.registerBoss(1, proto);
        f2.registerBoss(1, new Boss("Whimsun", 80, 0.8, 1, List.of()));

        try {
            Boss b1 = f1.generateRandomBoss(1);
            Boss b2 = f2.generateRandomBoss(1);
            check("Misma semilla → mismo boss generado", b1.getName().equals(b2.getName()));
        } catch (ResourceNotFoundException e) {
            System.out.println("  [FAIL] " + e.getMessage());
            failed++;
        }
    }

    // =========================================================================
    // 6. COMBAT MANAGER
    // =========================================================================

    static void testCombatManager() {
        section("6. CombatManager — Command + State");

        Player p     = new Player("Frisk", 200, 10, 5);
        Boss   boss  = new Boss("Toriel", 50, 1.0, 1,
                List.of("¿Por qué?", "¡Para!"));
        HistoryManager hist = new HistoryManager();
        CombatManager  mgr  = new CombatManager(p, boss, new PlayerMenuState(), hist);

        subsection("Estado inicial");
        check("Estado inicial = PlayerMenu",       mgr.getCurrentState().getName().equals("PlayerMenu"));
        check("isCombatOver = false al inicio",    !mgr.isCombatOver());
        check("qteMultiplier inicial = 1.0",       mgr.getQteMultiplier() == 1.0);

        subsection("FightAction");
        int bossHpBefore = boss.getHp();
        mgr.executeAction(new FightAction());
        check("FightAction reduce HP del boss",          boss.getHp() < bossHpBefore);
        check("Tras Fight → estado EnemyBulletHell",     mgr.getCurrentState().getName().equals("EnemyBulletHell"));
        mgr.tick();
        check("tick() vuelve a PlayerMenu",              mgr.getCurrentState().getName().equals("PlayerMenu"));

        subsection("Killing blow");
        boss = new Boss("Napstablook", 50, 1.0, 1, List.of("..."));
        mgr  = new CombatManager(p, boss, new PlayerMenuState(), hist);
        boss.takeDamage(boss.getMaxHp() - 1); // 1 HP restante
        mgr.executeAction(new FightAction());
        check("Boss muerto → isCombatOver = true",       mgr.isCombatOver());
        check("Boss muerto → estado permanece en Menu",  mgr.getCurrentState().getName().equals("PlayerMenu"));

        subsection("ActAction + MercyAction");
        p    = new Player("Frisk", 200, 10, 5);
        boss = new Boss("Asgore", 80, 1.0, 1, List.of("¡En guardia!", "¡Por el bien de todos!"));
        hist = new HistoryManager();
        mgr  = new CombatManager(p, boss, new PlayerMenuState(), hist);

        for (int i = 0; i < 3; i++) {
            mgr.executeAction(new ActAction("hablar"));
            mgr.tick();
        }
        check("Tras 3 ACTs, boss puede ser perdonado", boss.canBeSpared());

        mgr.executeAction(new MercyAction());
        check("Mercy exitoso → boss perdonado",         boss.isSpared());
        check("Mercy exitoso → combate termina",        mgr.isCombatOver());

        subsection("ItemAction");
        p    = new Player("Frisk", 200, 10, 5);
        boss = new Boss("Dummy", 50, 1.0, 1, List.of());
        mgr  = new CombatManager(p, boss, new PlayerMenuState(), new HistoryManager());
        p.takeDamage(60);
        Consumable pot = new Consumable("p1", "Poción", "Cura 30 HP.", 30);
        p.addLoot(pot);
        int hpBefore = p.getHp();
        mgr.executeAction(new ItemAction(pot));
        check("ItemAction cura al player",              p.getHp() == hpBefore + 30);

        subsection("handleVictoryLootDrop");
        p    = new Player("Frisk", 200, 10, 5);
        boss = new Boss("Final", 1, 1.0, 1, List.of());
        hist = new HistoryManager();
        mgr  = new CombatManager(p, boss, new PlayerMenuState(), hist);
        mgr.executeAction(new FightAction()); // mata al boss (1 HP)
        mgr.handleVictoryLootDrop();
        check("lootDrop añade ítem al inventario",      !p.getInventory().isEmpty());
        int invSize = p.getInventory().size();
        mgr.handleVictoryLootDrop(); // segunda llamada debe ser ignorada
        check("handleVictoryLootDrop es idempotente",   p.getInventory().size() == invSize);
        check("Registra decisión en HistoryManager",    hist.getRecords().size() == 1);

        subsection("QTE multiplier");
        p    = new Player("Frisk", 200, 10, 5);
        boss = new Boss("QTE Boss", 100, 1.0, 1, List.of());
        mgr  = new CombatManager(p, boss, new PlayerMenuState(), new HistoryManager());
        mgr.setQteMultiplier(2.0);
        int expectedDmg = Math.max(1, (int) Math.round(p.getTotalAttack() * 2.0)); // 20
        mgr.executeAction(new FightAction());
        check("FightAction usa el QTE multiplier",      boss.getHp() == 100 - expectedDmg);
        check("Multiplicador se resetea tras el Fight", mgr.getQteMultiplier() == 1.0);
    }

    // =========================================================================
    // 7. HISTORY MANAGER + STORY GENERATOR
    // =========================================================================

    static void testHistoryAndStory() {
        section("7. HistoryManager + StoryGenerator");

        HistoryManager hist = new HistoryManager();
        StoryGenerator story = new StoryGenerator(hist);
        Boss b = new Boss("Sans", 100, 1.0, 1, List.of());

        subsection("HistoryManager");
        check("Sin registros → lists vacía",            hist.getRecords().isEmpty());
        check("getSparedCount = 0",                     hist.getSparedCount() == 0);
        check("getKilledCount = 0",                     hist.getKilledCount() == 0);

        hist.recordDecision(b, true);
        hist.recordDecision(b, true);
        hist.recordDecision(b, false);
        check("Tras 3 registros → size = 3",            hist.getRecords().size() == 3);
        check("getSparedCount = 2",                     hist.getSparedCount() == 2);
        check("getKilledCount = 1",                     hist.getKilledCount() == 1);

        ArrayList<DecisionRecord> recCopy = hist.getRecords();
        recCopy.clear();
        check("getRecords() devuelve copia segura",        hist.getRecords().size() == 3);

        subsection("StoryGenerator — getCurrentRoute");
        HistoryManager hPacific  = new HistoryManager();
        HistoryManager hGenocide = new HistoryManager();
        HistoryManager hNeutral  = new HistoryManager();
        HistoryManager hEmpty    = new HistoryManager();

        hPacific.recordDecision(b,  true);
        hPacific.recordDecision(b,  true);
        hGenocide.recordDecision(b, false);
        hGenocide.recordDecision(b, false);
        hNeutral.recordDecision(b,  true);
        hNeutral.recordDecision(b,  false);

        check("Sin historial → NEUTRAL",               new StoryGenerator(hEmpty).getCurrentRoute()    == Route.NEUTRAL);
        check("Todos perdonados → PACIFIC",            new StoryGenerator(hPacific).getCurrentRoute()  == Route.PACIFIC);
        check("Todos eliminados → GENOCIDE",           new StoryGenerator(hGenocide).getCurrentRoute() == Route.GENOCIDE);
        check("Mix → NEUTRAL",                         new StoryGenerator(hNeutral).getCurrentRoute()  == Route.NEUTRAL);

        subsection("generateNarrativeForPhase");
        StoryGenerator sg = new StoryGenerator(hPacific);
        String n1 = sg.generateNarrativeForPhase(1);
        check("Narrativa PACIFIC no es null",          n1 != null && !n1.isBlank());
        check("Narrativa PACIFIC contiene [PACÍFICA]", n1.contains("PACÍFICA"));

        sg = new StoryGenerator(hGenocide);
        String n2 = sg.generateNarrativeForPhase(1);
        check("Narrativa GENOCIDE contiene [GENOCIDIO]", n2.contains("GENOCIDIO"));

        subsection("generateBossIntroDialogue");
        sg = new StoryGenerator(hPacific);
        String intro = sg.generateBossIntroDialogue(b);
        check("Intro menciona el nombre del boss",     intro.contains("Sans"));
    }

    // =========================================================================
    // 8. PERSISTABLE — ROUNDTRIP
    // =========================================================================

    static void testPersistable() {
        section("8. Persistable — roundtrip saveData / loadData");

        subsection("Player");
        try {
            Player original = new Player("Frisk", 100, 15, 8);
            original.takeDamage(35); // HP = 65

            String json = original.saveData();
            check("saveData() produce JSON no vacío", json != null && !json.isBlank());

            Player loaded = new Player("placeholder", 1, 0, 0);
            loaded.loadData(json);

            check("name preservado",       loaded.getName().equals("Frisk"));
            check("hp preservado",         loaded.getHp() == 65);
            check("maxHp preservado",      loaded.getMaxHp() == 100);
            check("baseAttack preservado", loaded.getBaseAttack() == 15);
            check("baseDefense preservado",loaded.getBaseDefense() == 8);

        } catch (SaveCorruptionException e) {
            System.out.println("  [FAIL] Excepción inesperada: " + e.getMessage());
            failed += 5;
        }

        checkThrows("JSON inválido → SaveCorruptionException",
            () -> {
                try { new Player("T", 100, 10, 5).loadData("no es json"); }
                catch (SaveCorruptionException e) { throw new RuntimeException(e); }
            });

        checkThrows("hp > maxHp → SaveCorruptionException",
            () -> {
                String bad = "{\"name\":\"T\",\"hp\":200,\"maxHp\":100,"
                           + "\"baseAttack\":10,\"baseDefense\":5,"
                           + "\"equippedWeaponId\":null,\"equippedArmorId\":null,"
                           + "\"inventoryIds\":[]}";
                try { new Player("T", 100, 10, 5).loadData(bad); }
                catch (SaveCorruptionException e) { throw new RuntimeException(e); }
            });

        subsection("PhaseManager");
        BossFactory factory = new BossFactory(new Random(42L));
        factory.registerBoss(1, new Boss("B1", 100, 1.0, 1, List.of()));
        factory.registerBoss(2, new Boss("B2", 120, 1.2, 2, List.of()));

        try {
            PhaseManager original = new PhaseManager(factory);
            original.advancePhase();
            original.advancePhase(); // fase = 2

            String json = original.saveData();
            PhaseManager loaded = new PhaseManager(factory);
            loaded.loadData(json);

            check("currentPhase preservado (= 2)", loaded.getCurrentPhase() == 2);

        } catch (SaveCorruptionException | ResourceNotFoundException e) {
            System.out.println("  [FAIL] " + e.getMessage());
            failed++;
        }

        checkThrows("fase fuera de rango → SaveCorruptionException",
            () -> {
                try { new PhaseManager(factory).loadData("{\"currentPhase\":6}"); }
                catch (SaveCorruptionException e) { throw new RuntimeException(e); }
            });

        subsection("HistoryManager");
        try {
            Boss b = new Boss("Papyrus", 100, 1.0, 2, List.of());
            HistoryManager original = new HistoryManager();
            original.recordDecision(b, true);
            original.recordDecision(b, false);

            String json = original.saveData();
            HistoryManager loaded = new HistoryManager();
            loaded.loadData(json);

            check("Número de registros preservado",  loaded.getRecords().size() == 2);
            check("sparedCount preservado",          loaded.getSparedCount() == 1);
            check("killedCount preservado",          loaded.getKilledCount() == 1);
            check("bossName preservado",             loaded.getRecords().get(0).bossName().equals("Papyrus"));
            check("wasSpared preservado",            loaded.getRecords().get(0).wasSpared());

        } catch (SaveCorruptionException e) {
            System.out.println("  [FAIL] " + e.getMessage());
            failed += 5;
        }

        checkThrows("JSON corrupto → SaveCorruptionException",
            () -> {
                try { new HistoryManager().loadData("corrupted!"); }
                catch (SaveCorruptionException e) { throw new RuntimeException(e); }
            });
    }

    // =========================================================================
    // 9. SIMULACIÓN COMPLETA — 5 FASES
    // =========================================================================

    static void simulateFullGame() {
        section("9. Simulación completa — 5 fases (RNG seed=42)");

        // Configurar fábrica con un prototipo por fase
        BossFactory factory = new BossFactory(new Random(42L));
        for (int i = 1; i <= 5; i++) {
            factory.registerBoss(i, new Boss(
                "Boss_F" + i, 30 + i * 10, 0.8 + i * 0.1, i,
                List.of("¡Turno " + i + "!", "¡Soy el jefe de la fase " + i + "!")
            ));
        }

        Player         player  = new Player("Frisk", 500, 20, 8);
        HistoryManager history = new HistoryManager();
        PhaseManager   phases  = new PhaseManager(factory);
        StoryGenerator story   = new StoryGenerator(history);

        System.out.println();
        System.out.println("  Jugador: " + player.getName()
            + "  HP=" + player.getHp() + "  ATK=" + player.getTotalAttack()
            + "  DEF=" + player.getTotalDefense());
        System.out.println();

        // Estrategia alterna: fases impares → MERCY, fases pares → FIGHT
        for (int phase = 1; phase <= 5; phase++) {
            try {
                Boss boss = phases.advancePhase();
                if (boss == null) break;

                System.out.printf("  [Fase %d] vs %s  HP=%d  ATK=%d%n",
                    phase, boss.getName(), boss.getMaxHp(), boss.computeAttackPower());
                System.out.println("  " + story.generateBossIntroDialogue(boss));

                CombatManager combat = new CombatManager(
                    player, boss, new PlayerMenuState(), history);

                if (phase % 2 != 0) {
                    // FASES IMPARES → ACT × 3 luego MERCY
                    for (int i = 0; i < 3 && !combat.isCombatOver(); i++) {
                        combat.executeAction(new ActAction("hablar"));
                        combat.tick();
                    }
                    combat.executeAction(new MercyAction());
                    System.out.println("  → Resultado: MERCY");
                } else {
                    // FASES PARES → FIGHT hasta la muerte
                    while (!combat.isCombatOver()) {
                        combat.executeAction(new FightAction());
                        if (!combat.isCombatOver()) combat.tick();
                    }
                    System.out.println("  → Resultado: FIGHT (boss derrotado)");
                }

                combat.handleVictoryLootDrop();

                System.out.printf("  Loot recibido: %s  |  Inventario: %d ítems%n",
                    player.getInventory().isEmpty() ? "ninguno"
                        : player.getInventory().getLast().getName(),
                    player.getInventory().size());
                System.out.printf("  HP del jugador: %d/%d%n",
                    player.getHp(), player.getMaxHp());
                System.out.println();

            } catch (ResourceNotFoundException e) {
                System.out.println("  [FAIL] " + e.getMessage());
                failed++;
            }
        }

        // Verificaciones de la simulación
        check("Se completaron 5 fases",
                          history.getRecords().size() == 5);
        check("PhaseManager en fase final",
                      phases.isFinalPhase());
        check("El jugador sigue vivo",
                           player.isAlive());
        check("Inventario tiene al menos 3 ítems",
               player.getInventory().size() >= 3);

        Route route = story.getCurrentRoute();
        check("Ruta NEUTRAL (mix fight/mercy)",          route == Route.NEUTRAL);

        System.out.println();
        System.out.println("  Ruta narrativa final: " + route);
        System.out.println("  Perdonados: " + history.getSparedCount()
            + "  |  Derrotados: " + history.getKilledCount());
        System.out.println("  " + story.generateNarrativeForPhase(5));
    }
}
